/*
* Copyright (C) 2014 Andrew Reid and the ModelGUI Project <http://mgui.wikidot.com>
* 
* This file is part of ModelGUI[neuro] (mgui-neuro).
* 
* ModelGUI[neuro] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* ModelGUI[neuro] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with ModelGUI[neuro]. If not, see <http://www.gnu.org/licenses/>.
*/


package mgui.neuro.components;

import java.util.ArrayList;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.neuro.InterfaceNeuroComponentListener;
import mgui.interfaces.neuro.NeuroComponentEvent;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.models.dynamic.DynamicModelEngine;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.models.networks.components.AbstractNetworkComponent;
import mgui.models.networks.components.InterfaceNetworkComponentListener;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;

import org.opensourcephysics.numerics.ODESolver;


/****************************************
 * <P>Abstract generic class for neuro model components to inherit. Contains two lists, one for
 * events and one for connections. This class forces an implementation of <code>clone()</code> by its 
 * subclasses; as a general policy clones of model components should ensure that components 
 * states are also preserved in a clone.</P>
 * 
 * <P>All subclasses of this class should add its parameters as <code>Attribute</code>s. These
 * parameters should have their own get and set methods implemented. Primitives should be wrapped
 * by instances of <code>arNumber</code> (java wrappers such as <code>Integer</code> are fine too, but do not allow their
 * values to be dynamically changed...)</P>
 * 
 * <P>Variables (e.g., clock) should be declared as individual class members.</P> 
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public abstract class AbstractNeuroComponent extends AbstractNetworkComponent{

	//protected double[] state;
	protected ODESolver solver;
	
	//public InterfaceTreeNode treeNode;
	public ArrayList<InterfaceNetworkComponentListener> listeners = 
									new ArrayList<InterfaceNetworkComponentListener>();
	
	protected ArrayList<DynamicModelEvent> events = new ArrayList<DynamicModelEvent>();
	protected ArrayList<DynamicModelComponent> connections = new ArrayList<DynamicModelComponent>();
	/*
	public static Comparator idComparator = new Comparator<AbstractNeuroComponent>(){
		public int compare(AbstractNeuroComponent c1, AbstractNeuroComponent c2){
			if (c1.id > c2.id) return 1;
			if (c1.id < c2.id) return -1;
			return 0;
			}
		};
	*/
	
	//*****VARIABLES*****
	protected double clock;
	//protected double step;
	
	protected AttributeList attributes = new AttributeList();
	protected long id;
	public boolean hasSubComponents;
	public boolean updated;
	
	int verbose = 0;		//no console output
	//int verbose = 1;		//output events
	
	protected void init(){
		attributes.addAttributeListener(this);
		attributes.add(new Attribute("Name", "no-name"));
		attributes.add(new Attribute("Delay", new MguiDouble(1)));
		//setSolver(new RK4(this));
		attributes.add(new Attribute("Solver", "org.opensourcephysics.numerics.RK4"));
		id = -1;
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void setSolver(){
		//does nothing unless overridden by a subclass which provides an appropriate ODE function 
	}
	
	public void setSolver(String str){
		attributes.setValue("Solver", str);
		setSolver();
	}
	
	public String getSolver(){
		return (String)attributes.getValue("Solver");
	}
	
	public void setODESolver(ODESolver solver){
		this.solver = solver;
		attributes.setValue("Solver", solver.getClass().getName());
		//setSolver(solver.getClass().getName());
	}
	
	public ODESolver getODESolver(){
		return solver;
	}
	
	public void setDelay(double delay){
		attributes.setValue("Delay", new MguiDouble(delay));
	}
	
	public double getDelay(){
		return ((MguiDouble)attributes.getValue("Delay")).getValue();
	}
	
	public void setID(long id){
		setID(id, true);
	}
	
	public void setID(long id, boolean update){
		this.id = id;
		if (update)
			fireListeners();
	}
	
	public ArrayList<DynamicModelComponent> getConnections(){
		return connections;
	}
	
	public long getID(){
		return id;
	}
	
	public void addListener(InterfaceNetworkComponentListener l){
		listeners.add(l);
	}
	
	public void removeListener(InterfaceNeuroComponentListener l){
		listeners.remove(l);
	}
	
	protected void fireListeners(){
		fireListeners(0);
	}
	
	protected void fireListeners(int code){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).componentUpdated(new NeuroComponentEvent(this, code));
	}
	
	public void addEvent(DynamicModelEvent e) {
		events.add(e);
	}
	
	public void addConnection(DynamicModelComponent c){
		connections.add(c);
	}
	
	public void removeConnection(DynamicModelComponent c){
		connections.remove(c);
	}
	
	public void reset(){
		events.clear();
		if (solver != null)
			solver.initialize(solver.getStepSize());
	}
	
	public boolean executeEvents(double step) {
		boolean executed = false;
		try{
			//while (events.size() > 0){
			//int j = 0;	//is next event to execute
			for (int j = 0; j < events.size();){
				events.get(j).timeElapsed(step);
				if (events.get(j).getDelay() <= 0){
					//executed |= executeEvent(events.get(j));
					updated = executeEvent(events.get(j));
					if (verbose == 1 && updated){
						System.out.println("Model Event: (" + 
										   toString() +
										   ") " + events.get(j).getClass().getSimpleName());
						}
					executed |= updated;
					events.remove(j);
					int x = events.size();
					x += 0;
				}else{
					//if (step > 0)
					//	events.get(j).timeElapsed(step);
					j++;
					}
			}
		}catch (NeuroException e){
			e.printStackTrace();
			return false;
			}
		return executed;
	}

	//update clock and call updateFromClock
	public void timeElapsed(double time) {
		//if (time <= 0) return;
		clock += time;
		if (solver != null && time != solver.getStepSize())
			solver.setStepSize(time);
		
		//updateComponent();
		
		//step = time;
		updateFromClock();
	}
	
	public void resetClock(){
		clock = 0;
		if (solver != null)
			solver.initialize(solver.getStepSize());
	}
	
	//methods to be overridden if necessary
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException {
		return false;
	}
	
	/*
	public boolean updateFromEnvironment(DynamicModelEnvironment environment){
		return false;
	}
	*/
	
	protected boolean updateFromClock(){
		//if (!updated) return false;
		//updated = false;
		return updateComponent();
	}
	
	protected boolean updateComponent(){
		return false;
	}
	
	public void getRate(double[] state, double[] rate) {
		
	}
	
	public void addToEngine(DynamicModelEngine e){
	}
	
	//force implementation of clone
	public abstract Object clone();
	
	//tree object method
	//TODO: make abstract?
	//Tree nodes should be implemented such that collapsing a branch destroys
	//that branch; since the network is not a tree, the tree structure is
	//arbitrary.
	
	//return a tree node instance
	/*
	public InterfaceTreeNode getTreeNode(){
		InterfaceTreeNode treeNode = new InterfaceTreeNode(this);
		treeNode.add(attributes.getTreeNode());
		return treeNode;
	}
	
	
	public void setTreeNode(){
		//treeNode = new InterfaceNeuroTreeNode(this);
		//treeNode.add(attributes.getTreeNode());
	}
	*/

	//attribute object methods
	public Attribute getAttribute(String attrName) {
		return attributes.getAttribute(attrName);
		//return attributes.getValue(attrName);
	}

	public AttributeList getAttributes() {
		return attributes;
	}

	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	public void attributeUpdated(AttributeEvent e){
		//TODO implement component listeners..
		fireListeners();
	}
	
	public int compareTo(DynamicModelComponent c) {
		if (c.getID() < this.id) return 1;
		if (c.getID() > this.id) return -1;
		return 0;
	}

	/********************************
	 * Returns a list of the complete subcomponent tree. Subclasses containing subcomponents
	 * should override this and provide an enumeration of all subcomponents, also calling this
	 * method for each subcomponent to ensure a complete tree is returned.
	 */
	public ArrayList<DynamicModelComponent> getSubComponents() {
		return null;
	}

	/************************************
	 * Indicates whether this component has subcomponents. Subclasses containing subcomponents 
	 * should set the hasSubComponents flag to true.
	 */
	public boolean hasSubComponents() {
		return hasSubComponents;
	}
	
}