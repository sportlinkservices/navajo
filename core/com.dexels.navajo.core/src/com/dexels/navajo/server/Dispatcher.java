package com.dexels.navajo.server;

/**
 * Title:        Navajo
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Dexels
 * @author Arjen Schoneveld en Martin Bergman
 * @version $Id$
 *
 * DISCLAIMER
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL DEXELS BV OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dexels.navajo.adapter.navajomap.manager.NavajoMapManager;
import com.dexels.navajo.authentication.api.AAAQuerier;
import com.dexels.navajo.document.Header;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.events.NavajoEventRegistry;
import com.dexels.navajo.events.types.NavajoExceptionEvent;
import com.dexels.navajo.events.types.NavajoRequestEvent;
import com.dexels.navajo.events.types.NavajoResponseEvent;
import com.dexels.navajo.events.types.ServerReadyEvent;
import com.dexels.navajo.mapping.AsyncStore;
import com.dexels.navajo.mapping.RemoteAsyncAnswer;
import com.dexels.navajo.mapping.RemoteAsyncRequest;
import com.dexels.navajo.mapping.compiler.TslCompiler;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.AuthorizationException;
import com.dexels.navajo.script.api.ClientInfo;
import com.dexels.navajo.script.api.FatalException;
import com.dexels.navajo.script.api.Mappable;
import com.dexels.navajo.script.api.MappableException;
import com.dexels.navajo.script.api.NavajoClassSupplier;
import com.dexels.navajo.script.api.NavajoDoneException;
import com.dexels.navajo.script.api.SystemException;
import com.dexels.navajo.script.api.TmlRunnable;
import com.dexels.navajo.script.api.UserException;
import com.dexels.navajo.server.descriptionprovider.DescriptionProviderInterface;
import com.dexels.navajo.server.enterprise.integrity.WorkerInterface;
import com.dexels.navajo.server.enterprise.queue.RequestResponseQueueFactory;
import com.dexels.navajo.server.enterprise.scheduler.TaskInterface;
import com.dexels.navajo.server.enterprise.scheduler.TaskRunnerFactory;
import com.dexels.navajo.server.enterprise.scheduler.TaskRunnerInterface;
import com.dexels.navajo.server.enterprise.scheduler.WebserviceListenerFactory;
import com.dexels.navajo.server.enterprise.tribe.TribeManagerFactory;
import com.dexels.navajo.server.enterprise.xmpp.JabberWorkerFactory;
import com.dexels.navajo.server.global.GlobalManager;
import com.dexels.navajo.server.global.GlobalManagerRepository;
import com.dexels.navajo.server.global.GlobalManagerRepositoryFactory;
import com.dexels.navajo.server.jmx.SNMPManager;
import com.dexels.navajo.server.resource.ResourceManager;
import com.dexels.navajo.util.AuditLog;

/**
 * This class implements the general Navajo Dispatcher. This class handles
 * authorisation/authentication/logging/error handling/business rule validation
 * and finally dispatching to the proper dispatcher class.
 */

public class Dispatcher implements Mappable, DispatcherMXBean, DispatcherInterface {

    protected static int instances = 0;

    /**
     * Fields accessable by webservices
     */
    public Access[] users;
    public static final String FILE_VERSION = "$Id$";
    public static final String vendor = "Dexels BV";
    public static final String product = "Navajo Service Delivery Platform";
    public static final String NAVAJO_TOPIC = "navajo/request";
    
    public volatile static String edition;
    private final Map<String, GlobalManager> globalManagers = new HashMap<String, GlobalManager>();
    private AAAQuerier authenticator;
    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    static {
        try {
            Class.forName("com.dexels.navajo.tribe.TribeManager");
            edition = "Enterprise";
        } catch (Throwable e) {
            edition = "Standard";
        }
    }

    /**
     * Unique dispatcher instance.
     */
    // private static volatile Dispatcher instance = null;
    private static boolean servicesBeingStarted = false;
    private static boolean servicesStarted = false;

    protected boolean matchCN = false;
    public final Set<Access> accessSet = Collections.newSetFromMap(new ConcurrentHashMap<Access, Boolean>());

    public boolean useAuthorisation = true;
    public static java.util.Date startTime = new java.util.Date();

    public long requestCount = 0;
    private NavajoConfigInterface navajoConfig;

    private EventAdmin eventAdmin;

    private String keyStore;
    private String keyPassword;

    public static final int rateWindowSize = 20;
    public static final double requestRate = 0.0;
    private long[] rateWindow = new long[rateWindowSize];

    // private static Object semaphore = new Object();
    private int peakAccessSetSize = 0;

    /**
     * Registered SNMP managers.
     */
    private ArrayList<SNMPManager> snmpManagers = new ArrayList<SNMPManager>();

    protected boolean simulationMode;

    // optional, can be null
    // private BundleCreator bundleCreator;

    public Dispatcher(NavajoConfigInterface nc) {
        navajoConfig = nc;
    }

    /**
     * Constructor for usage in web service scripts.
     */
    public Dispatcher() {
        navajoConfig = null;
    }

    @Override
    public void generateServerReadyEvent() {
        NavajoEventRegistry.getInstance().publishEvent(new ServerReadyEvent());
    }

    private final void startUpServices() {

        // Clear temp space.
        clearTempSpace();

        // Bootstrap event registry.
        NavajoEventRegistry.getInstance();

        // Bootstrap async store.
        navajoConfig.getAsyncStore();

        // Startup user defined services.
        UserDaemon.startup();

        // Startup statistics runnner.
        navajoConfig.startStatisticsRunner();

        // Startup task runner.
        navajoConfig.startTaskRunner();

        // Startup queued adapter.
        RequestResponseQueueFactory.getInstance();

        // Bootstrap integrity worker.
        navajoConfig.getIntegrityWorker();

        // Start NavajoMapManager to register health of foreign (non-tribal)
        // Navajo Server instances.
        NavajoMapManager.getInstance();

        // Startup tribal status collector.
        TribeManagerFactory.startStatusCollector();

        // Startup Jabber
        JabberWorkerFactory.getJabberWorkerInstance();

    }

    // @Override
    // public void setBundleCreator(BundleCreator bc) {
    // this.bundleCreator = bc;
    // }
    //
    // protected void clearBundleCreator(BundleCreator bc) {
    // this.bundleCreator = null;
    // }
    // @Override
    // public BundleCreator getBundleCreator() {
    // return this.bundleCreator;
    // }

    protected final void init() {

        // Init tribe, if initializing it returns null, should not matter, let
        // it do its thing.
        if (!servicesBeingStarted && !servicesStarted) {
            servicesBeingStarted = true;
            if (TribeManagerFactory.getInstance() != null) {
                // After tribe exists, start other service (there are
                // dependencies on existence of tribe!).
                startUpServices();
            }
            servicesStarted = true;
            servicesBeingStarted = false;
        }

    }

    public Access[] getUsers() {
        Set<Access> all = new HashSet<Access>(com.dexels.navajo.server.DispatcherFactory.getInstance().getAccessSet());
        Iterator<Access> iter = all.iterator();
        ArrayList<Access> d = new ArrayList<Access>();
        while (iter.hasNext()) {
            Access a = iter.next();
            d.add(a);
        }
        Access[] ams = new Access[d.size()];
        return d.toArray(ams);
    }

    /**
     * Set the location of the certificate keystore.
     *
     * @param s
     */
    public final void setKeyStore(String s) {
        keyStore = s;
    }

    /**
     * Set the password for the certificate keystore.
     *
     * @param s
     */
    public final void setKeyPassword(String s) {
        keyPassword = s;
    }

    /**
     * Get the location of the keystore.
     *
     * @return
     */
    public final String getKeyStore() {
        return keyStore;
    }

    /**
     * Get the password of the keystore.
     *
     * @return
     */
    public final String getKeyPassword() {
        return keyPassword;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dexels.navajo.server.DispatcherMXBean#getRequestRate()
     */
    @Override
    public float getRequestRate() {
        if (rateWindow[0] > 0) {
            float time = (rateWindow[rateWindowSize - 1] - rateWindow[0]) / (float) 1000.0;
            float avg = rateWindowSize / time;
            return avg;
        }
        return 0.0f;
    }

    /**
     * Clears all instantiated Navajo Classloaders to support a reload of Navajo
     * Adapters.
     */
    @Override
    public synchronized final void doClearCache() {
        navajoConfig.doClearCache();
        GenericHandler.doClearCache();
        System.runFinalization();
        // System.gc();
    }

    /**
     * Clears only the script Navajo classloaders, and leaves the jar cache
     * alone
     */
    @Override
    public synchronized final void doClearScriptCache() {
        navajoConfig.doClearScriptCache();
        GenericHandler.doClearCache();
        // System.runFinalization();
        // System.gc();
    }

    /**
     * Update the Navajo repository that is used for
     * authentication/authorization.
     *
     * @param repositoryClass
     *            the fully specified classname of the repository.
     *
     * @throws java.lang.ClassNotFoundException
     *             deprecated! This will never work in OSGi (although you can
     *             easily do it in OSGi) I also seriously doubt if it ever
     *             worked well in J2EE
     */
    // public synchronized final void updateRepository(String repositoryClass)
    // throws
    // java.lang.ClassNotFoundException {
    // doClearCache();
    // Repository newRepository = RepositoryFactoryImpl.getRepository( (
    // NavajoClassLoader) navajoConfig.
    // getClassloader(), repositoryClass, navajoConfig);
    // System.err.println("New repository = " + newRepository);
    // if (newRepository == null) {
    // throw new ClassNotFoundException("Could not find repository class: " +
    // repositoryClass);
    // }
    // else {
    // navajoConfig.setRepository(newRepository);
    // }
    // }

    /*
     * Get the (singleton) NavajoConfig object reference.
     * 
     * @return
     */
    @Override
    public final NavajoConfigInterface getNavajoConfig() {
        return navajoConfig;
    }

    public void setNavajoConfig(NavajoConfigInterface nci) {
        logger.info("Setting NavajoConfig");
        this.navajoConfig = nci;
    }

    public void clearNavajoConfig(NavajoConfigInterface nci) {
        logger.info("Clearing NavajoConfig");
        this.navajoConfig = null;
    }

    /*
     * Get the default NavajoClassLoader. Note that when hot-compile is enabled
     * each webservice context uses its own NavajoClassLoader instance.
     * 
     * @return
     */
    public final NavajoClassSupplier getNavajoClassLoader() {
        if (navajoConfig == null) {
            return null;
        } else {
            return navajoConfig.getClassloader();
        }
    }

    /*
     * Process a webservice using a special handler class.
     * 
     * @param handler the class the will process the webservice.
     * 
     * @param in the request Navajo document.
     * 
     * @param access
     * 
     * @param parms
     * 
     * @return
     * 
     * @throws Exception
     */
    private final Navajo dispatch(Access access) throws Exception {

        WorkerInterface integ = null;
        Navajo out = null;
        if (access == null) {
            System.err.println("Null access!!!");
            return null;
        }

        Navajo in = access.getInDoc();

        if (in != null) {
            Header h = in.getHeader();
            if (h != null) {
                // Process client token:
                String clientToken = h.getHeaderAttribute("clientToken");
                if (clientToken != null) {
                    access.setClientToken(clientToken);
                }
                // Process client info:
                String clientInfo = h.getHeaderAttribute("clientInfo");
                if (clientInfo != null) {
                    access.setClientInfo(clientInfo);
                }

                // Process piggyback data:
                Set<Map<String, String>> s = h.getPiggybackData();
                if (s != null) {
                    for (Iterator<Map<String, String>> iter = s.iterator(); iter.hasNext();) {
                        Map<String, String> element = iter.next();
                        access.addPiggybackData(element);
                    }
                }
            }
        } else {
            throw NavajoFactory.getInstance().createNavajoException("Null input message in dispatch");
        }

        // Check for webservice transaction integrity.
        boolean integrityViolation = false;
        integ = navajoConfig.getIntegrityWorker();

        if (integ != null) {
            // Check for stored response or webservice that is still running.
            out = integ.getResponse(access, in);
            if (out != null) {
                integrityViolation = true;
                return out;
            }
        }

        try {

            ServiceHandler sh = createHandler(access);

            // If recompile is needed ALWAYS set expirationInterval to -1.
            // TODO: IMPLEMENT NEEDS RECOMPILE DIFFERENTLY: I DO NOT WANT
            // GENERICHANDLER DEPENDENCY @ THIS
            // POINT... ALSO THE CURRENT NEEDSRECOMPILE DOES NOT CHECK DIRTY
            // DEPENDENCIES!!
            // ALSO I DO NOT WANT CACHECONTROLLER DEPENDENCY @ THIS POINT.
            long expirationInterval = CacheController.getInstance().getExpirationInterval(access.rpcName);
            if (expirationInterval > 0 && sh.needsRecompile()) {
                expirationInterval = -1;
            }

            // Remove password from in to create password independent
            // persistenceKey.
            in.getHeader().setRPCPassword("");

            out = (Navajo) navajoConfig.getPersistenceManager().get(sh,
                    CacheController.getInstance().getCacheKey(access.rpcUser, access.rpcName, in), access.rpcName,
                    expirationInterval, (expirationInterval != -1));

            access.setOutputDoc(out);

            // Store response for integrity checking.
            if (integ != null && out != null && !integrityViolation) {
                integ.setResponse(in, out);
            }

            return out;
        } catch (java.lang.ClassNotFoundException cnfe) {
            throw new SystemException(-1, cnfe.getMessage(), cnfe);
        } catch (java.lang.IllegalAccessException iae) {
            throw new SystemException(-1, iae.getMessage(), iae);
        } catch (java.lang.InstantiationException ie) {
            throw new SystemException(-1, ie.getMessage(), ie);
        } finally {
            // Remove stored access from worker request list.
            if (integ != null) {
                integ.removeFromRunningRequestsList(in);
            }
        }
    }

    // Overridden for OSGi
    protected ServiceHandler createHandler(Access access) {
        return HandlerFactory.createHandler(navajoConfig, access, simulationMode);
    }

    public final boolean doMatchCN() {
        return matchCN;
    }

    /**
     * Handles errors.
     *
     * @param access
     * @param e
     * @param inMessage
     * @return the response error Navajo document.
     *
     * @throws FatalException
     */
    private final Navajo errorHandler(Access access, Throwable e, Navajo inMessage) throws FatalException {

        String message = "";

        if (access != null) {
            try {
                message = e.getClass().toString() + ": " + e.getMessage() + ", " + e.toString() + ", "
                        + e.getLocalizedMessage();

                if (message.equalsIgnoreCase("")) {
                    message = "Undefined Error";

                }
                StringWriter swriter = new StringWriter();
                PrintWriter writer = new PrintWriter(swriter);

                e.printStackTrace(writer);

                message += swriter.getBuffer().toString();

                message += "\n";

                swriter = new StringWriter();
                writer = new PrintWriter(swriter);
                // inMessage.getMessageBuffer().write(writer);
                inMessage.write(writer);

                message += swriter.getBuffer().toString();

            } catch (NavajoException tbe) {
                throw new FatalException(tbe.getMessage());
            }
        } else {
            // if no access was created, use the throwable message to be able to
            // say *anything*
            message += e.getMessage();
        }

        try {
            Navajo out = generateErrorMessage(access, message, SystemException.SYSTEM_ERROR, 1, e);
            if (access != null) {
                access.setOutputDoc(out);
            }
            return out;
        } catch (Exception ne) {
            throw new FatalException(ne.getMessage(), ne);
        }
    }

    /**
     * Generate a Navajo authorization error response.
     *
     * @param access
     *            Beware, might be null
     * @param ae
     * @return
     * @throws FatalException
     */
    private final Navajo generateAuthorizationErrorMessage(Access access, AuthorizationException ae, String rpcName)
            throws FatalException {

        try {
            Navajo outMessage = NavajoFactory.getInstance().createNavajo();
            // Make sure empty Header is constructed
            Header h = NavajoFactory.getInstance().createHeader(outMessage, "", "", "", -1);
            outMessage.addHeader(h);

            Message errorMessage = NavajoFactory.getInstance().createMessage(
                    outMessage,
                    (ae.isNotAuthorized() ? AuthorizationException.AUTHORIZATION_ERROR_MESSAGE
                            : AuthorizationException.AUTHENTICATION_ERROR_MESSAGE));
            outMessage.addMessage(errorMessage);

            Property prop = NavajoFactory.getInstance().createProperty(outMessage, "Message", Property.STRING_PROPERTY,
                    ae.getMessage(), 0, "Message", Property.DIR_OUT);

            errorMessage.addProperty(prop);
            prop = NavajoFactory.getInstance().createProperty(outMessage, "User", Property.STRING_PROPERTY,
                    ae.getUser(), 0, "User", Property.DIR_OUT);

            errorMessage.addProperty(prop);

            prop = NavajoFactory.getInstance().createProperty(outMessage, "Webservice", Property.STRING_PROPERTY,
                    rpcName, 0, "User", Property.DIR_OUT);

            errorMessage.addProperty(prop);

            if (access != null) {
                access.setException(ae);
                access.setOutputDoc(outMessage);
            }
            return outMessage;
        } catch (Exception e) {
            logger.error("Error: ", e);
            throw new FatalException(e.getMessage());
        }
    }

    /**
     * Generate a Navajo error message and log the error to the Database.
     */
    @Override
    public final Navajo generateErrorMessage(Access access, String message, int code, int level, Throwable t)
            throws FatalException {

        if (message == null) {
            message = "Null pointer exception";

        }
        if (t != null) {
            logger.error("Generating error message for: ", t);
        }
        
        try {
            Navajo outMessage = NavajoFactory.getInstance().createNavajo();

            // Make sure empty Header is constructed
            Header h = NavajoFactory.getInstance().createHeader(outMessage, "", "", "", -1);
            outMessage.addHeader(h);

            Message errorMessage = NavajoFactory.getInstance().createMessage(outMessage, Constants.ERROR_MESSAGE);

            outMessage.addMessage(errorMessage);

            Property prop = NavajoFactory.getInstance().createProperty(outMessage, "message", Property.STRING_PROPERTY,
                    message, 200, "Message", Property.DIR_OUT);

            errorMessage.addProperty(prop);

            prop = NavajoFactory.getInstance().createProperty(outMessage, "code", Property.INTEGER_PROPERTY, code + "",
                    100, "Code", Property.DIR_OUT);
            errorMessage.addProperty(prop);

            prop = NavajoFactory.getInstance().createProperty(outMessage, "level", Property.INTEGER_PROPERTY,
                    level + "", 100, "Level", Property.DIR_OUT);
            errorMessage.addProperty(prop);

            if (access != null) {
                prop = NavajoFactory.getInstance().createProperty(outMessage, "access_id", Property.STRING_PROPERTY,
                        access.accessID + "", 100, "Access id", Property.DIR_OUT);
                errorMessage.addProperty(prop);
                access.setException(t);
            }

            if (access != null) {
                access.setOutputDoc(outMessage);
            }
            return outMessage;
        } catch (Exception e) {
            throw new FatalException(e.getMessage(), e);
        }
    }

    private final Navajo generateScheduledMessage(Header h, String taskId, boolean rejected) {
        try {
            Navajo outMessage = NavajoFactory.getInstance().createNavajo();
            Header hnew = NavajoFactory.getInstance().createHeader(outMessage, h.getRPCName(), h.getRPCUser(), "", -1);

            if (!rejected) {
                hnew.setSchedule(taskId);
            } else {
                Message msg = NavajoFactory.getInstance().createMessage(outMessage, "Warning");
                outMessage.addMessage(msg);

                Property prop = NavajoFactory.getInstance().createProperty(outMessage, "Status",
                        Property.STRING_PROPERTY, "TimeExpired", 32, "Created by generateScheduledMessage",
                        Property.DIR_OUT);
                msg.addProperty(prop);
            }
            outMessage.addHeader(hnew);

            return outMessage;
        } catch (Exception e) {
            logger.error("Error: ", e);
            return null;
        }
    }

    @Override
    public final void setUseAuthorisation(boolean a) {
        useAuthorisation = a;
    }

    /**
     * Method can be used to remove special message before returning Navajo to
     * some client.
     * 
     * 
     * @param doc
     * @return
     */
    @Override
    public final Navajo removeInternalMessages(Navajo doc) {
        if (doc != null) {
            try {
                // if ( doc.getMessage("__globals__") != null ) {
                doc.removeMessage(doc.getMessage("__globals__"));
                // }
                // if ( doc.getMessage("__parms__") != null ) {
                doc.removeMessage("__parms__");
                // }
            } catch (Exception e) {
            }
        }
        return doc;
    }

    @Override
    public final Navajo handle(Navajo inMessage, String instance, boolean skipAuth, AfterWebServiceEmitter emit, ClientInfo clientInfo)
            throws FatalException {
        return processNavajo(inMessage, instance, null, clientInfo, skipAuth, null, emit);

    }

    @Override
    public final Navajo handle(Navajo inMessage, boolean skipAuth) throws FatalException {
        return processNavajo(inMessage, null, null, null, skipAuth, null, null);
    }

    @Override
    public final Navajo handle(Navajo inMessage, String instance, boolean skipAuth) throws FatalException {
        return processNavajo(inMessage, instance, null, null, skipAuth, null, null);
    }

    @Override
    public final Navajo handle(Navajo inMessage) throws FatalException {
        return processNavajo(inMessage, null, null, null, false, null, null);
    }

    @Override
    public String getThreadName(Access a) {
        return getApplicationId() + "/" + a.accessID;
    }

    @Override
    public final boolean isBusy() {
        return (accessSet.size() > navajoConfig.getMaxAccessSetSize());
    }

    private void setRequestRate(ClientInfo clientInfo, int accessSetSize) {
        if (accessSetSize > peakAccessSetSize) {
            peakAccessSetSize = accessSetSize;
        }

        requestCount++;

        for (int i = 0; i < rateWindowSize - 1; i++) {
            try {
                rateWindow[i] = rateWindow[i + 1];
            } catch (ArrayIndexOutOfBoundsException e) {
                rateWindow = new long[rateWindowSize];
            }
        }

        if (clientInfo != null) {
            rateWindow[rateWindowSize - 1] = clientInfo.created.getTime();
        } else {
            rateWindow[rateWindowSize - 1] = System.currentTimeMillis();
        }

    }

    /**
     * Entry point for HTTP Servlet Listener.
     * 
     * @param inMessage
     *            , the Navajo request message
     * @param userCertificate
     *            , optionally a certificate
     * @param clientInfo
     *            , a client info structure
     * @return
     * @throws FatalException
     */
    @Override
    public final Navajo handle(Navajo inMessage, String instance, Object userCertificate, ClientInfo clientInfo)
            throws FatalException {
        // Maybe use event to trigger handle event.... such that NavajoRequest
        // events can be proxied/intercepted by
        // other classes.
        return processNavajo(inMessage, instance, userCertificate, clientInfo, false, null, null);
    }

    /**
     * Handle a webservice.
     *
     * @param inMessage
     * @param userCertificate
     * @param clientInfo
     * @param origRunnable
     * @param skipAuth
     *            , always skip authorization part.
     * @return
     * @throws FatalException
     */
    private final Navajo processNavajo(Navajo inMessage, String instance, Object userCertificate,
            ClientInfo clientInfo, boolean skipAuth, TmlRunnable origRunnable, AfterWebServiceEmitter emit)
            throws FatalException {

        Access access = null;
        Navajo outMessage = null;
        String rpcName = "";
        String rpcUser = "";
        String rpcPassword = "";

        Throwable myException = null;
        String origThreadName = null;
        boolean scheduledWebservice = false;
        boolean afterWebServiceActivated = false;

        int accessSetSize = accessSet.size();
        setRequestRate(clientInfo, accessSetSize);

        Navajo result = handleCallbackPointers(inMessage,instance);
        if (result != null) {
            return result;
        }
        Header header = inMessage.getHeader();
        rpcName = header.getRPCName();
        rpcUser = header.getRPCUser();
        rpcPassword = header.getRPCPassword();

        boolean preventFinalize = false;

        try {
            /**
             * Phase II: Authorisation/Authentication of the user. Is the user
             * known and valid and may it use the specified service? Also log
             * the access.
             */

            long startAuth = System.currentTimeMillis();
            if (rpcName == null) {
                throw new FatalException("No script defined");
            }

            if (rpcName.equals("navajo_ping")) {
                // Ping!
                outMessage = NavajoFactory.getInstance().createNavajo();
                Header h = NavajoFactory.getInstance().createHeader(outMessage, "", "", "", -1);
                outMessage.addHeader(h);
                return outMessage;
            }

            access = new Access(1, 1, rpcUser, rpcName, "", "", "", userCertificate, false, null);
            access.setTenant(instance);
            access.rpcPwd = rpcPassword;
            access.setInDoc(inMessage);
            access.setApplication(header.getApplication());
            access.setOrganization(header.getOrganization());
            if (clientInfo != null) {
                access.ipAddress = clientInfo.getIP();
                access.hostName = clientInfo.getHost();
            }
            NavajoEventRegistry.getInstance().publishEvent(new NavajoRequestEvent(access));
            appendGlobals(inMessage, instance);

            if (useAuthorisation && !skipAuth) {
                try {

                    if (navajoConfig == null) {
                        System.err.println("EMPTY NAVAJOCONFIG, INVALID STATE OF DISPATCHER!");
                        throw new FatalException("EMPTY NAVAJOCONFIG, INVALID STATE OF DISPATCHER!");
                    }
                    if (instance == null) {
                        throw new SystemException(-1, "No tenant set -cannot authenticate!");
                    }
                    authenticator.process(access);

                } catch (AuthorizationException ex) {
                    outMessage = generateAuthorizationErrorMessage(access, ex, rpcName);
                    AuditLog.log(AuditLog.AUDIT_MESSAGE_AUTHORISATION, "(service=" + rpcName + ", user=" + rpcUser
                            + ", message=" + ex.getMessage(), Level.WARNING);
                    myException = ex;
                    access.setExitCode(Access.EXIT_AUTH_EXECPTION);
                    return outMessage;
                } catch (SystemException se) {
                    logger.error("SystemException on authenticateUser  {} for {}: ", rpcUser, rpcName, se);
                    outMessage = generateErrorMessage(access, se.getMessage(), SystemException.NOT_AUTHORISED, 1, new Exception("NOT AUTHORISED"));
                    AuditLog.log(AuditLog.AUDIT_MESSAGE_AUTHORISATION, "(service=" + rpcName + ", user=" + rpcUser + ", message=" + se.getMessage(),
                            Level.WARNING);
                    myException = se;
                    access.setExitCode(Access.EXIT_AUTH_EXECPTION);
                    return outMessage;
                }
            }

            if (clientInfo != null) {
                access.ipAddress = clientInfo.getIP();
                access.hostName = clientInfo.getHost();
                access.parseTime = clientInfo.getParseTime();
                access.queueTime = clientInfo.getQueueTime();
                access.requestEncoding = clientInfo.getEncoding();
                access.compressedReceive = clientInfo.isCompressedRecv();
                access.compressedSend = clientInfo.isCompressedSend();
                access.contentLength = clientInfo.getContentLength();
                access.created = clientInfo.getCreated();
                access.queueId = clientInfo.getQueueId();
                access.queueSize = clientInfo.getQueueSize();
                // Set the name of this thread.
                origThreadName = Thread.currentThread().getName();
                Thread.currentThread().setName(getThreadName(access));
            }

            final GlobalManager gm;
            if (instance != null) {
                gm = globalManagers.get(instance);
            } else {
                gm = globalManagers.get("default");
            }

            if (gm != null) {
                gm.initGlobals(inMessage);
            }

            // register TmlRunnable with access object:

            if (origRunnable != null) {
                access.setOriginalRunnable(origRunnable);
                // and vice versa, for the endTransaction;
                origRunnable.setAttribute("access", access);
            }

            String fullLog = inMessage.getHeader().getHeaderAttribute("fullLog");
            if ("true".equals(fullLog)) {
                System.err.println("Full debug detected. Accesshash: " + access.hashCode());
                access.setDebugAll(true);
            }

            if ((access.userID == -1) || (access.serviceID == -1)) { // ACCESS NOTGRANTED.

                String errorMessage = "";

                if (access.userID == -1) {
                    errorMessage = "Cannot authenticate user: " + rpcUser;
                } else {
                    errorMessage = "Cannot authorise use of: " + rpcName;
                }
                outMessage = generateErrorMessage(access, errorMessage, SystemException.NOT_AUTHORISED, 1,
                        new Exception("NOT AUTHORISED"));
                return outMessage;

            } else { // ACCESS GRANTED.

                access.authorisationTime = (int) (System.currentTimeMillis() - startAuth);
                accessSet.add(access);

                /**
                 * Add some MDC parameters to context
                 */

                MDC.put("accessId", access.accessID);
                MDC.put("rpcName", access.getRpcName());
                MDC.put("rpcUser", access.getRpcUser());
                if (access.getTenant() != null) {
                    MDC.put("tenant", access.getTenant());
                }
                MDC.put("rootPath", getNavajoConfig().getRootPath());
                MDC.put("instanceName", getNavajoConfig().getInstanceName());
                MDC.put("instanceGroup", getNavajoConfig().getInstanceGroup());

                /**
                 * Phase VIa: Check if scheduled webservice
                 */

                if (inMessage.getHeader().getSchedule() != null && !inMessage.getHeader().getSchedule().equals("")) {

                    if (validTimeSpecification(inMessage.getHeader().getSchedule())) {

                        scheduledWebservice = true;
                        logger.info("Scheduling webservice: {}  on {} ", inMessage.getHeader().getRPCName(), inMessage
                                .getHeader().getSchedule());
                        TaskRunnerInterface trf = TaskRunnerFactory.getInstance();
                        TaskInterface ti = trf.createTask();
                        try {
                            ti.setTrigger(inMessage.getHeader().getSchedule());
                            ti.setNavajo(inMessage);
                            ti.setPersisted(true); // Make sure task gets persisted in tasks.xml
                            if (inMessage.getHeader().getHeaderAttribute("keeprequestresponse") != null
                                    && inMessage.getHeader().getHeaderAttribute("keeprequestresponse").equals("true")) {
                                ti.setKeepRequestResponse(true);
                            }
                            trf.addTask(ti);
                            outMessage = generateScheduledMessage(inMessage.getHeader(), ti.getId(), false);
                        } catch (UserException e) {
                            logger.info("WARNING: Invalid trigger specified for task {}: {}", ti.getId(), inMessage
                                    .getHeader().getSchedule());
                            trf.removeTask(ti);
                            outMessage = generateErrorMessage(access, "Could not schedule task:" + e.getMessage(), -1,
                                    -1, e);
                        }
                    } else { // obsolete time specification
                        outMessage = generateScheduledMessage(inMessage.getHeader(), null, true);
                    }

                } else {

                    /**
                     * Phase VI: Dispatch to proper servlet.
                     */

                    // Create beforeWebservice event.
                    access.setInDoc(inMessage);
                    long b_start = System.currentTimeMillis();
                    Navajo useProxy = ( WebserviceListenerFactory.getInstance() != null ?  
                    		WebserviceListenerFactory.getInstance().beforeWebservice(rpcName, access) : null);
                    access.setBeforeServiceTime((int) (System.currentTimeMillis() - b_start));

                    if (useAuthorisation) {
                        if (useProxy == null) {
                            outMessage = dispatch(access);
                        } else {
                            rpcName = access.rpcName;
                            outMessage = useProxy;
                        }
                    } else {
                        throw new UnsupportedOperationException("I've removed this code because I assumed it wasn't used any more");
                    }
                }

            }
        } catch (NavajoDoneException e) {
            System.err.println("NavajoDone caught in dispatcher. Need to prevent finalize block!");
            preventFinalize = true;
            throw e;

        } catch (AuthorizationException aee) {
            outMessage = generateAuthorizationErrorMessage(access, aee, rpcName);
            AuditLog.log(AuditLog.AUDIT_MESSAGE_AUTHORISATION, "(service=" + rpcName + ", user=" + rpcUser
                    + ", message=" + aee.getMessage() + ")", Level.WARNING);
            myException = aee;
            access.setExitCode(Access.EXIT_AUTH_EXECPTION);
            return outMessage;
        } catch (UserException ue) {
            try {
                outMessage = generateErrorMessage(access, ue.getMessage(), ue.code, 1, (ue.t != null ? ue.t : ue));
                myException = ue;
                return outMessage;
            } catch (Exception ee) {
                logger.error("Error: ", ee);
                myException = ee;
                return errorHandler(access, ee, inMessage);
            }
        } catch (SystemException se) {
            logger.error("Error: ", se);
            myException = se;
            try {
                outMessage = generateErrorMessage(access, se.getMessage(), se.code, 1, (se.t != null ? se.t : se));
                return outMessage;
            } catch (Exception ee) {
                logger.error("Error: ", ee);
                return errorHandler(access, ee, inMessage);
            }
        } catch (Throwable e) {
            logger.error("Error: ", e);
            myException = e;
            return errorHandler(access, e, inMessage);
        } finally {
            if (!preventFinalize) {
                finalizeService(inMessage, access, rpcName, rpcUser, myException, origThreadName, scheduledWebservice,
                        afterWebServiceActivated, emit);
            }
        }

        return access.getOutputDoc();
    }

    private void appendGlobals(Navajo inMessage, String instance) {
        final GlobalManagerRepository globalManagerInstance = GlobalManagerRepositoryFactory.getGlobalManagerInstance();
        if (globalManagerInstance == null) {
            logger.debug("No global manager found");
        }
        GlobalManager gm = null;
        if (globalManagerInstance != null) {
            if (instance == null) {
                gm = globalManagerInstance.getGlobalManager("default");
            } else {
                gm = globalManagerInstance.getGlobalManager(instance);
            }
        }
        if (gm != null) {
            gm.initGlobals(inMessage);
        }
    }

    @Override
    public Navajo handleCallbackPointers(Navajo inMessage, String tenant) {
        // Check whether unkown callbackpointers are present that need to be
        // handled by another instance.
        if (inMessage.getHeader().hasCallBackPointers()) {
            String[] allRefs = inMessage.getHeader().getCallBackPointers();
            if (AsyncStore.getInstance().getInstance(allRefs[0]) == null) {
                RemoteAsyncRequest rasr = new RemoteAsyncRequest(allRefs[0]);
                
                logger.info("Broadcasting async pointer: "+allRefs[0]);
                RemoteAsyncAnswer rasa = (RemoteAsyncAnswer) TribeManagerFactory.getInstance().askAnybody(rasr);
                if (rasa != null) {
                    logger.info("ASYNC OWNER: " + rasa.getOwnerOfRef() + "(" + rasa.getHostNameOwnerOfRef()
                            + ")" + " FOR REF " + allRefs[0]);
                    try {
                        Navajo result = TribeManagerFactory.getInstance().forward(inMessage, rasa.getOwnerOfRef(),tenant);
                        return result;
                    } catch (Exception e) {
                        logger.error("Error: ", e);
                    }

                } else {
                    logger.warn("DID NOT FIND ANY OWNERS OF ASYNCREF...");
                }
            }
        }
        return null;
    }

    @Override
    public void finalizeService(Navajo inMessage, Access access, String rpcName, String rpcUser, Throwable myException,
            String origThreadName, boolean scheduledWebservice, boolean afterWebServiceActivated,
            AfterWebServiceEmitter emit) {
        if (access != null && !scheduledWebservice) {

            Navajo outMessage = access.getOutputDoc();
            try {
                // Always make sure header contains original rpcName and rpcUser
                // (BUT NOT PASSWORD!).
                Header h = outMessage.getHeader();
                if (h == null) {
                    h = NavajoFactory.getInstance().createHeader(outMessage, rpcName, rpcUser, "", -1);
                    outMessage.addHeader(h);
                } else {
                    h.setRPCName(rpcName);
                    h.setRPCUser(rpcUser);
                }
                // Set accessId to make sure it can be used as reference by
                // triggered tasks.
                h.setHeaderAttribute("accessId", access.getAccessID());

                // If emitter is specified, first fire emitter.
                if (emit != null) {
                    emit.emit(access.getOutputDoc());
                }

                // Call after web service event...
                if (access.getExitCode() != Access.EXIT_AUTH_EXECPTION) {
                    long a_start = System.currentTimeMillis();
                    afterWebServiceActivated = ( WebserviceListenerFactory.getInstance() != null ? 
                            WebserviceListenerFactory.getInstance().afterWebservice(rpcName, access) : false);
                    access.setAfterServiceTime((int) (System.currentTimeMillis() - a_start));

                    // Set access to finished state.
                    access.setFinished();
                }
            

                // Translate property descriptions.
                updatePropertyDescriptions(inMessage, outMessage, access.getTenant());
                access.storeStatistics(h);

                // Call Navajoresponse event.
                access.setException(myException);

                NavajoEventRegistry.getInstance().publishEvent(new NavajoResponseEvent(access));

                // Publish exception event if exception occurred.
                if (myException != null) {
                    NavajoEventRegistry.getInstance().publishEvent(
                            new NavajoExceptionEvent(rpcName, access.getAccessID(), rpcUser, myException));
                }

            } finally {
                // Remove access object from set of active webservices first.
                accessSet.remove(access);
            }
        }
        
        generateNavajoRequestEvent(myException != null);

        if (origThreadName != null) {
            Thread.currentThread().setName(origThreadName);
        }
    }

    private void generateNavajoRequestEvent(boolean hadException) {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (hadException) {
            properties.put("type", "navajoexception");
        } else {
            properties.put("type", "navajo");
        }
        Event event = new Event(Dispatcher.NAVAJO_TOPIC, properties);
        eventAdmin.postEvent(event);
        
    }

    private void updatePropertyDescriptions(Navajo inMessage, Navajo outMessage, String tenant) {
        final DescriptionProviderInterface descriptionProvider = navajoConfig.getDescriptionProvider();
        if (descriptionProvider == null) {
            return;
        }
        try {
            descriptionProvider.updatePropertyDescriptions(inMessage, outMessage,tenant);
        } catch (NavajoException e) {
            logger.error("Error: ", e);
        }
    }

    /**
     * Determine if WS is reserved Navajo webservice.
     *
     * @param name
     * @return
     */
    public static final boolean isSpecialwebservice(String name) {

        if (name == null) {
            return false;
        }
        return name.startsWith("navajo") || name.equals("InitNavajoStatus") || name.equals("navajo_logon");
    }

    @Override
    protected void finalize() {
        instances--;
    }

    @Override
    public String getServerId() {
        return TslCompiler.getHostName();
    }

    @Override
    public String getApplicationId() {
        return getNavajoConfig().getInstanceName();
    }

    @Override
    public String getApplicationGroup() {
        return getNavajoConfig().getInstanceGroup();
    }

    @Override
    public File getTempDir() {
        File tempDir = new File(System.getProperty("java.io.tmpdir") + "/" + getApplicationId());
        tempDir.mkdirs();
        return tempDir;
    }

    /**
     * Recursively delete files including directories.
     * 
     * @param f
     */
    private void deleteFiles(File f) {
        if (f != null && f.isDirectory()) {
            File[] dirs = f.listFiles();
            if (dirs != null && dirs.length > 0) {
                for (int i = 0; i < dirs.length; i++) {
                    deleteFiles(dirs[i]);
                }
            }
            f.delete();
        } else if (f != null) {
            f.delete();
        }
    }

    private void clearTempSpace() {
        File tempDir = new File(System.getProperty("java.io.tmpdir") + "/" + getApplicationId());
        File[] dirs = tempDir.listFiles();
        if (dirs != null && dirs.length > 0) {
            for (int i = 0; i < dirs.length; i++) {
                deleteFiles(dirs[i]);
            }
        }
    }

    @Override
    public File createTempFile(String prefix, String suffix) throws IOException {
        File f = File.createTempFile(prefix, suffix, getTempDir());
        // Don't use deleteOnExit until Java 1.6, lower version contain memory
        // leak (approx. 1K per call!).
        // f.deleteOnExit();
        return f;
    }

    public Access getAccessObject(String id) {

        Iterator<Access> iter = accessSet.iterator();
        boolean found = false;
        Access a = null;
        while (iter.hasNext() && !found) {
            a = iter.next();
            if (a.accessID.equals(id)) {
                System.err.println("FOUND ACCESS OBJECT!!!");
                found = true;
            }
        }
        return a;
    }

    @Override
    public int getAccessSetSize() {
        return DispatcherFactory.getInstance().getAccessSet().size();
    }

    @Override
    public void kill() {
    }

    @Override
    public void load(Access access) throws MappableException, UserException {

    }

    @Override
    public void store() throws MappableException, UserException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dexels.navajo.server.DispatcherMXBean#getPeakAccessSetSize()
     */
    @Override
    public int getPeakAccessSetSize() {
        return peakAccessSetSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dexels.navajo.server.DispatcherMXBean#resetAccessSetPeakSize()
     */
    @Override
    public void resetAccessSetPeakSize() {
        peakAccessSetSize = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dexels.navajo.server.DispatcherMXBean#getStarttime()
     */
    @Override
    public Date getStarttime() {
        return startTime;
    }

    public static int getInstances() {
        return instances;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dexels.navajo.server.DispatcherMXBean#getUptime()
     */
    @Override
    public long getUptime() {
        return (System.currentTimeMillis() - startTime.getTime());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dexels.navajo.server.DispatcherMXBean#getSnmpManangers()
     */
    @Override
    public String getSnmpManangers() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < snmpManagers.size(); i++) {
            SNMPManager snmp = snmpManagers.get(i);
            s.append(snmp.getHost() + ":" + snmp.getPort() + ":" + snmp.getSnmpVersion());
            s.append(',');
        }
        String result = s.toString();
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dexels.navajo.server.DispatcherMXBean#setSnmpManagers(java.lang.String
     * )
     */
    @Override
    public void setSnmpManagers(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        while (st.hasMoreTokens()) {
            snmpManagers.add(new SNMPManager(st.nextToken()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dexels.navajo.server.DispatcherMXBean#getRequestCount()
     */
    @Override
    public long getRequestCount() {
        return requestCount;
    }

    public static boolean validTimeSpecification(String dateString) {

        boolean result = false;

        try {
            if (dateString.startsWith("time:")) {
                dateString = dateString.substring(5);

                StringTokenizer tok = new StringTokenizer(dateString, "|");

                String field = null;

                long timeSpecified = 0;
                long now = 0;

                if (tok.hasMoreTokens()) {
                    field = tok.nextToken();
                    timeSpecified += 1000000L * (("*".equals(field)) ? 13 : Integer.parseInt(field));
                }
                if (tok.hasMoreTokens()) {
                    field = tok.nextToken();
                    timeSpecified += 10000L * (("*".equals(field)) ? 32 : Integer.parseInt(field));
                }
                if (tok.hasMoreTokens()) {
                    field = tok.nextToken();
                    timeSpecified += 100L * (("*".equals(field)) ? 25 : Integer.parseInt(field));
                }
                if (tok.hasMoreTokens()) {
                    field = tok.nextToken();
                    timeSpecified += 1L * (("*".equals(field)) ? 60 : Integer.parseInt(field));
                }
                if (tok.hasMoreTokens()) {
                    field = tok.nextToken();
                }
                if (tok.hasMoreTokens()) {
                    field = tok.nextToken();
                    timeSpecified += 100000000L * (("*".equals(field)) ? 9999 : Integer.parseInt(field));
                }

                Calendar current = Calendar.getInstance();

                now = 100000000L * current.get(Calendar.YEAR) + 1000000L * (current.get(Calendar.MONTH) + 1) + 10000L
                        * current.get(Calendar.DAY_OF_MONTH) + 100L * current.get(Calendar.HOUR_OF_DAY) + 1L
                        * current.get(Calendar.MINUTE);

                result = timeSpecified > now;
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }

        return result;
    }

    // public String getVersion() {
    // return "";
    // }
    //
    // public String getVendor() {
    // return vendor;
    // }
    //
    // public String getProduct() {
    // return product;
    // }
    //
    // public String getEdition() {
    // return edition;
    // }

    @Override
    public Set<Access> getAccessSet() {
        return accessSet;
    }

    @Override
    public java.util.Date getStartTime() {
        return startTime;
    }

    @Override
    public int getRateWindowSize() {
        return rateWindowSize;
    }

    @Override
    public double getCPULoad() {
        return getNavajoConfig().getCurrentCPUload();
    }

    private int health;

    @Override
    public int getHealth(String resourceId) {
        return health;
    }

    @Override
    public int getWaitingTime(String resourceId) {
        return 0;
    }

    @Override
    public void setHealth(String resourceId, int h) {
        System.err.println("Dispatcher.setHealth(" + resourceId + "," + h + ")");
        health = h;
    }

    public static ResourceManager getResourceManager(String id) {
        return DispatcherFactory.getInstance();
    }

    @Override
    public boolean isAvailable(String resourceId) {
        return true;
    }

    public void addGlobalManager(GlobalManager gm, Map<String, Object> settings) {
        globalManagers.put((String) settings.get("instance"), gm);
    }

    public GlobalManager getGlobalManager(String instance) {
        return globalManagers.get(instance);
    }

    public void removeGlobalManager(GlobalManager gm, Map<String, Object> settings) {
        globalManagers.remove(settings.get("instance"));
    }
    
    
    public void setAuthenticator(AAAQuerier a) {
        this.authenticator = a;
    }
    
    public void clearAuthenticator(AAAQuerier a) {
        this.authenticator = null;
    }


    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public void clearEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

}
