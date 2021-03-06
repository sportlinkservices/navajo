/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.queuemanager.health;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.RequestQueue;
import com.dexels.navajo.script.api.TmlScheduler;
import com.dexels.navajo.server.DispatcherFactory;

public class ThreadPoolHealthMonitor {
    private static final int THREAD_SLEEP_TIME_NORMAL = 500;
    private static final int THREAD_SLEEP_TIME_LONG = 10000;

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolHealthMonitor.class);
    private TmlScheduler tmlScheduler;
    private SchedulerHealthCheckThread healthThread;

    public void setPriorityTmlScheduler(TmlScheduler sched) {
        this.tmlScheduler = sched;
    }

    public void clearPriorityTmlScheduler(TmlScheduler sched) {
        this.tmlScheduler = null;
    }

    public void activate() {
        logger.info("Activating ThreadPoolHealthMonitor...");
        healthThread = new SchedulerHealthCheckThread();
        healthThread.start();
    }

    public void deactivate() {
        logger.info("Deactivating ThreadPoolHealthMonitor...");
        if (healthThread != null) {
            healthThread.setKeepRunning(false);
        }
    }

    /*
     * Inner class
     */

    private final class SchedulerHealthCheckThread extends Thread {

        private boolean keepRunning = true;

        public void setKeepRunning(boolean keepRunning) {
            this.keepRunning = keepRunning;
        }

        @Override
        public void run() {
            while (keepRunning) {
                int sleepTime = THREAD_SLEEP_TIME_NORMAL;
                try {
                    RequestQueue queue = tmlScheduler.getDefaultQueue();
                    // If a thread just finished, the activeRequestCount might be 1-2 less than the maximum
                    // Yet we can still have queued requests. Thus we check if the number of threads left is
                    // very small, and we have queued requests.
                    int threadsLeft = queue.getMaximumActiveRequestCount() - queue.getActiveRequestCount();
                    if (threadsLeft < 3 && queue.getQueueSize() > 0) {
                        logPoolInfo();
                        
                        // The server is not very happy, and to prevent flooding logs we can wait a bit longer 
                        // before logging again
                        sleepTime = THREAD_SLEEP_TIME_LONG;
                    }
                } catch (Throwable t) {
                    logger.error("Exception in SchedulerHealthCheckThread", t);
                    sleepTime = THREAD_SLEEP_TIME_LONG;
                }
                
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    logger.warn("Thread interrupted!");
                    keepRunning = false;
                } catch (Throwable t) {
                    logger.error("Exception in SchedulerHealthCheckThread", t);
                    // Avoid tight loops in error state
                    try {
						Thread.sleep(THREAD_SLEEP_TIME_LONG);
					} catch (InterruptedException e) {
					}
                }
            }
        }

        private void logPoolInfo() {
            Set<Access> all = new HashSet<Access>(DispatcherFactory.getInstance().getAccessSet());
            long currenttime = System.currentTimeMillis();

            String logmsg = "Running requests: ";
            for (Access a : all) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n");
                sb.append("Access ");
                sb.append(a.getAccessID());
                sb.append("; service ");
                sb.append(a.getRpcName());
                sb.append("; user ");
                sb.append(a.getRpcUser());
                sb.append("; runtime ");
                sb.append(currenttime - a.created.getTime());
                logmsg += sb.toString();
            }
            // Skipping async requests for now

            logger.info(logmsg);

        }

    }

}
