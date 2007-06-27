package com.dexels.navajo.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import com.dexels.navajo.scheduler.IllegalTask;
import com.dexels.navajo.scheduler.IllegalTrigger;
import com.dexels.navajo.scheduler.Task;
import com.dexels.navajo.server.Access;
import com.dexels.navajo.server.NavajoConfig;
import com.dexels.navajo.server.Parameters;
import com.dexels.navajo.server.UserException;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.mapping.Mappable;
import com.dexels.navajo.mapping.MappableException;

/**
 * A State consists of transitions and tasks.
 * 
 * A task contains: A webservice with an optional trigger and an optional condition.
 * A transition contains: (1) A 'task' without a webservice but only a trigger with an optinal condition.
 *                        (2) A next state to enter when transition was taken.
 * @author arjen
 *
 */
public class State implements Serializable, Mappable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1658431106536428135L;
	
	public  String id;
	public  Date entryDate = null;
	public  Date leaveDate = null;
	public  boolean killed = false;
	public  Transition [] transitions = null;
	
	private final HashSet<Transition> myTransitions = new HashSet<Transition>();
	private final HashSet<WorkFlowTask> myTasks = new HashSet<WorkFlowTask>();
	private final WorkFlow myWorkFlow;
	
	protected State(String s, WorkFlow wf) {
		id = s;
		myWorkFlow = wf;
	}
	
	public void addTask(String webservice, String trigger, String condition) throws IllegalTrigger, IllegalTask  {
		if ( trigger == null || trigger.equals("")) {
			trigger = "time:now";
		}
		Task task = new Task(webservice, myWorkFlow.getInitiatingAccess().rpcUser, "", null, trigger, null);
		task.setWorkflowDefinition(myWorkFlow.getDefinition());
		task.setWorkflowId(myWorkFlow.getMyId());
		WorkFlowTask wft = new WorkFlowTask(this, task);
		myTasks.add(wft);
	}
	
	public Transition addTransition(String nextState, String trigger, String condition, String webservice) throws IllegalTrigger, IllegalTask  {
		Task task = new Task(webservice, myWorkFlow.getInitiatingAccess().rpcUser, "", null, trigger, null);
		task.setWorkflowDefinition(myWorkFlow.getDefinition());
		task.setWorkflowId(myWorkFlow.getMyId());
		Transition t = new Transition(this, nextState, task, condition);	
		myTransitions.add(t);
		return t;
	}
	
	public Transition addTransition(String nextState, String trigger, String condition) throws IllegalTrigger, IllegalTask  {
		Task task = new Task("", myWorkFlow.getInitiatingAccess().rpcUser, "", null, trigger, null);
		task.setWorkflowDefinition(myWorkFlow.getDefinition());
		task.setWorkflowId(myWorkFlow.getMyId());
		Transition t = new Transition(this, nextState, task, condition);	
		myTransitions.add(t);
		return t;
	}
	
	/**
	 * Method to be called when entering this state.
	 *
	 */
	public void enter() {
       
		// Set entry date.
		entryDate = new Date();
		
	    // Activate tasks.
		Iterator<WorkFlowTask> tks = myTasks.iterator();
		
		while ( tks.hasNext() ) {
			WorkFlowTask wtf = tks.next();
			if (!wtf.isFinished()) { // In case of revival, a workflowtask could already have been executed(!)
				wtf.activate();
			} else {
				System.err.println("Do not activate workflowtask..............it was already finished");
			}
		}
		// Activate transitions.
		Iterator<Transition> i = myTransitions.iterator();
		while ( i.hasNext() ) {
			Transition t = i.next();
			t.activate();
		}	
		
		 // Persist workflow instance. This method will be entry point for resurection.
		WorkFlowManager.getInstance().persistWorkFlow(myWorkFlow);
		
	}
	
	/**
	 * Method to be called when this is the initial state of a workflow.
	 *
	 */
	public void waitForAction() {
        // Activate transitions.
		Iterator<Transition> i = myTransitions.iterator();
		while ( i.hasNext() ) {
			Transition t = i.next();
			t.activate();
		}
	}
	
	/**
	 * Method to be called when leaving this state.
	 *
	 */
	public void leave() {
		// Clean up all tasks/triggers.
		Iterator<WorkFlowTask> i2 = myTasks.iterator();
		while ( i2.hasNext() ) {
			WorkFlowTask t = i2.next();
			t.cleanup();
		}
		Iterator<Transition> i = myTransitions.iterator();
		while ( i.hasNext() ) {
			Transition t = i.next();
			t.cleanup();
		}
		leaveDate = new Date();
		
		myWorkFlow.historicStates.add(this);
	}

	public String getId() {
		return id;
	}
	
	public WorkFlow getWorkFlow() {
		return myWorkFlow;
	}

	public void setKill() {
		leave();
		killed = true;
	}
	
	public void kill() {
		
	}

	public void load(Parameters parms, Navajo inMessage, Access access, NavajoConfig config) throws MappableException, UserException {
		// TODO Auto-generated method stub
		
	}

	public void store() throws MappableException, UserException {
		// TODO Auto-generated method stub
		
	}

	public Transition[] getTransitions() {
		HashSet<Transition> copy = new HashSet<Transition>();
		copy.addAll(myTransitions);
		transitions = new Transition[copy.size()];
		transitions = copy.toArray(transitions);
		
		return transitions;
		
	}

	public Date getEntryDate() {
		return entryDate;
	}

	public Date getLeaveDate() {
		return leaveDate;
	}

	public boolean getKilled() {
		return killed;
	}
	
}
