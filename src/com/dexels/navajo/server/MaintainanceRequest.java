package com.dexels.navajo.server;


import java.util.*;
import com.dexels.navajo.document.*;
import com.dexels.navajo.server.*;
import com.dexels.navajo.util.*;

public class MaintainanceRequest extends Request {

    public static final String NAVAJO_VERSION = "Navajo v2";

    // Navajo method names
    public static final String METHOD_NAVAJO = "navajo";
    public static final String METHOD_NAVAJO_LOGON = METHOD_NAVAJO + "_logon";
    public static final String METHOD_NAVAJO_LOGON_SEND = METHOD_NAVAJO
            + "_logon_send";
    public static final String METHOD_NAVAJO_PING = METHOD_NAVAJO + "_ping";

    private Repository repository = null;

    public MaintainanceRequest(HashMap rb, Repository repository) {
        super(rb);
        this.repository = repository;
    }

    public void addServicesToMessage(Access access, Parameters parms, Navajo inMessage, boolean multiple) throws SystemException, UserException, java.sql.SQLException, java.io.IOException,
            org.xml.sax.SAXException, NavajoException {

        Message services = Util.getMessage(inMessage, "services", true);

        String all[] = repository.getServices(access);

        if (all != null) {
            String card = (multiple) ? "+" : "1";

            Property serviceprop = NavajoFactory.getInstance().createProperty(inMessage, "all_services", card, "Alle diensten", Property.DIR_IN);

            for (int i = 0; i < all.length; i++) {
                Selection sel = NavajoFactory.getInstance().createSelection(inMessage, all[i], i + "", false);
                serviceprop.addSelection(sel);
            }

            services.addProperty(serviceprop);
        } else {
            // Add free text field for servicename if database not available.
            Property serviceprop = NavajoFactory.getInstance().createProperty(inMessage, "service", Property.STRING_PROPERTY, "", 25, "Requested Navajo service", Property.DIR_IN);
            services.addProperty(serviceprop);
        }
    }

    public Navajo logonSend(Access access, Parameters parms, Navajo inMessage)
            throws SystemException, UserException, java.sql.SQLException, java.io.IOException,
            org.xml.sax.SAXException, NavajoException {
        Navajo outMessage = null;

        Message identification = Util.getMessage(inMessage, "identification", true);
        String username = Util.getProperty(identification, "username", true).getValue();
        String password = Util.getProperty(identification, "password", true).getValue();

        Message services = Util.getMessage(inMessage, "services", true);
        Property serviceProp = Util.getProperty(services, "all_services", false);
        String service = "";

        if (serviceProp == null) {
            service = Util.getProperty(services, "service", true).getValue();
        } else {
            ArrayList selectedServices = serviceProp.getAllSelectedSelections();

            service = ((Selection) selectedServices.get(0)).getName();
        }

        Access newAccess = null;

        try {
          newAccess = repository.authorizeUser(username, password, service,
                                               inMessage, null);
        }
        catch (Exception ex) {

        }

        if ((newAccess.userID != -1) && (newAccess.serviceID != -1))
            outMessage = getThanksMessage("geauthoriseerd");
        else
            throw new UserException(SystemException.NOT_AUTHORISED, "");

        return outMessage;
    }

}
