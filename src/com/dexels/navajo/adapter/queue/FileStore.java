package com.dexels.navajo.adapter.queue;

import java.util.HashSet;
import java.util.Iterator;
import com.dexels.navajo.server.Dispatcher;
import com.dexels.navajo.server.NavajoConfig;
import com.dexels.navajo.server.enterprise.queue.Queuable;
import com.dexels.navajo.sharedstore.SharedStoreException;
import com.dexels.navajo.sharedstore.SharedStoreFactory;
import com.dexels.navajo.sharedstore.SharedStoreInterface;
import com.dexels.navajo.util.AuditLog;

/**
 * The FileStore uses the Navajo SharedStoreInterface for storing queued adapters.
 * 
 * @author arjen
 *
 */
public class FileStore implements MessageStore {

	private static String path = null; 
	private static String deadQueue = null;
	private static Object semaphore = new Object();
	private final HashSet<String> currentObjects = new HashSet<String>();
	private Iterator<String> objectPointer = null;
	
	private final static void setup() throws SharedStoreException {
		SharedStoreInterface ssi = SharedStoreFactory.getInstance();
		path = "/adapterqueue/" + Dispatcher.getInstance().getNavajoConfig().getInstanceName();
		ssi.createParent(path);
		// Define deadqueue to put in failures that have more than max retries. 
		// If some problem was solved in the mean time, simply put back file into normal queue.
		deadQueue = path + "/failures";
		ssi.createParent(deadQueue);
	}
	
	public FileStore() {
		synchronized (semaphore) {
			if ( path == null ) {
				try {
					setup();
				} catch (SharedStoreException e) {
					AuditLog.log(AuditLog.AUDIT_MESSAGE_SHAREDSTORE, e.getMessage());
				}
			}
		}
	}
	
	public void rewind() {
		currentObjects.clear();
		SharedStoreInterface ssi = SharedStoreFactory.getInstance();
		String [] files = ssi.getObjects(path);
		for (int i = 0; i < files.length; i++) {
				currentObjects.add(files[i]);
		}
		objectPointer = currentObjects.iterator();
	}
	
	public HashSet<QueuedAdapter> getQueuedAdapters() {
		HashSet<QueuedAdapter> queuedAdapters = new HashSet<QueuedAdapter>();
		synchronized ( path ) {
			
			SharedStoreInterface ssi = SharedStoreFactory.getInstance();
			String [] files = ssi.getObjects(path);

			for (int i = 0; i < files.length; i++) {
					NavajoObjectInputStream ois;
					try {
						ois = new NavajoObjectInputStream(ssi.getStream(path, files[i]), NavajoConfig.getInstance().getClassloader());
						Queuable q = (Queuable) ois.readObject();
						ois.close();
						QueuedAdapter qa = new QueuedAdapter(q);
						qa.ref = files[i];
						queuedAdapters.add(qa);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
		}
		return queuedAdapters;
	}
	
	public Queuable getNext() throws Exception {

		//System.err.println("In filestore getNext()");
		if ( objectPointer == null ) {
			throw new Exception("Call rewind() first before calling getNext()");
		}

		if (!objectPointer.hasNext() ) {
			objectPointer = null;
			currentObjects.clear();
			//System.err.println("I do not have any handlers...");
			return null;
		}
		
		Queuable q = null;
		String f = objectPointer.next();
		SharedStoreInterface ssi = SharedStoreFactory.getInstance();
		try {
			NavajoObjectInputStream ois = new NavajoObjectInputStream(ssi.getStream(path, f), NavajoConfig.getInstance().getClassloader());
			q = (Queuable) ois.readObject();
			ois.close();
			//System.err.println("Read object: " + q.getClass().getName() + ", retries " + q.getRetries() + ", max retries " + q.getMaxRetries());
			// Only return object if it is not sleeping
			if ( q.getWaitUntil() < System.currentTimeMillis() ) {
				ssi.remove(path, f);
//				System.err.println("Read object: " + q.getClass().getName() + ", retries " + q.getRetries() + ", max retries " + q.getMaxRetries());
//				System.err.println("Delete file");
				return q;
			}
			//System.err.println("This one is sleeping, try next object");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Could not read file: " + f + " from filestore: " + e.getMessage());
			e.printStackTrace();
		} 

		return getNext();
	}
	
	public void putMessage(Queuable handler, boolean failure) {
		//System.err.println(">> Putting work in store: " + handler.getClass().getName());
		
		synchronized ( path ) {
			if ( failure ) {
				// Reset retries if failure, such that it can easily be put back into normal queue..
				handler.resetRetries();
			}
			String f = handler.hashCode() + "_" + System.currentTimeMillis() + ".queue";
			SharedStoreInterface ssi = SharedStoreFactory.getInstance();
			try {
				ssi.store( ( failure ? deadQueue : path ), f, handler, false, false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	public void emptyQueue() {
		synchronized ( path ) {
			SharedStoreInterface ssi = SharedStoreFactory.getInstance();
			String [] files = ssi.getObjects(path);
			for (int i = 0; i < files.length; i++) {
				ssi.remove(path, files[i]);
			}
		}
	}
	
	public int getSize() {
		SharedStoreInterface ssi = SharedStoreFactory.getInstance();
		String [] files = ssi.getObjects(path);
		return files.length;
	}

	public HashSet<QueuedAdapter> getDeadQueue() {

		HashSet<QueuedAdapter> deadQueueAdapters = new HashSet<QueuedAdapter>();
		synchronized (deadQueue) {
			SharedStoreInterface ssi = SharedStoreFactory.getInstance();
			String [] files = ssi.getObjects(deadQueue);

			for (int i = 0; i < files.length; i++) {
					NavajoObjectInputStream ois;
					try {
						ois = new NavajoObjectInputStream(ssi.getStream(deadQueue, files[i]), NavajoConfig.getInstance().getClassloader());
						Queuable q = (Queuable) ois.readObject();
						ois.close();
						QueuedAdapter qa = new QueuedAdapter(q);
						qa.ref = files[i];
						deadQueueAdapters.add(qa);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
		}
		return deadQueueAdapters;

	}
	
	/**
	 * This method can be used to take over control of persisted workflow of another server.
	 * TODO: Figure out how to move Binary objects (file references!).
	 * 
	 * @param fromServer
	 */
	public void takeOverPersistedAdapters(String fromServer) {
		System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> In FileStore: takeOverPersistedWorkFlows(" + fromServer + ")");
		String [] queuedAdapters = SharedStoreFactory.getInstance().getObjects("/adapterqueue/" + fromServer);
		for (int i = 0; i < queuedAdapters.length; i++) {
			try {
				Queuable wf = (Queuable) SharedStoreFactory.getInstance().get("/adapterqueue/" + fromServer, queuedAdapters[i]);
				System.err.println(">>>>>>>>>>>>> MOVING WORKFLOW: " + wf.getClass().getName() + " FROM SERVER " + fromServer);
				// TODO: HOWTO MOVE Binary objects with their file references??
				putMessage(wf, false);
				SharedStoreFactory.getInstance().remove("/adapterqueue/" + fromServer, queuedAdapters[i]);
			} catch (SharedStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}

}
