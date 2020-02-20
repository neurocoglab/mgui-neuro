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


package mgui.neuro.networks;

import java.util.ArrayList;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.neuro.InterfaceNeuroComponentListener;
import mgui.interfaces.neuro.NeuroComponentEvent;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.models.networks.AbstractNetwork;
import mgui.models.networks.components.NetworkComponentEvent;
import mgui.neuro.components.AbstractSensoryNeuron;
import mgui.neuro.components.types.ConnectableNeuron;
import mgui.neuro.exceptions.NeuroNetException;
import mgui.neuro.graphs.NeuralNetGraph;


/************************************
 * Abstract class for all neural net classes to inherit.
 * 
 * @author Andrew Reid
 *
 */
public abstract class AbstractNeuralNet extends AbstractNetwork
										implements InterfaceNeuroComponentListener{

	public static final int MODE_TRAIN = 0;
	public static final int MODE_TEST = 1;	
	
	public ArrayList<ConnectableNeuron> units = new ArrayList<ConnectableNeuron>();
	public ArrayList<AbstractSensoryNeuron> inputs = new ArrayList<AbstractSensoryNeuron>();
	//protected IDFactory idFactory = new IDFactory();
	//public InterfaceTreeNode treeNode;
	//public AttributeList attributes = new AttributeList();
	//public ArrayList<NetworkListener> listeners = 
	//										new ArrayList<NetworkListener>();
	
	/********************************
	 * Constructs and returns a directed graph from the model components and their
	 * connections. DirectedGraph is a class provided in the JUNG API.
	 * 
	 * @return
	 */
	public NeuralNetGraph getGraph(){
		if (units == null) return null;
		
		NeuralNetGraph graph = new NeuralNetGraph(this);
		return graph;
	}
	
	protected void init(){
		attributes.add(new Attribute("Name", "no-name"));
	}
	
	public ArrayList<InterfaceDataSource> getDataSources(){
		return new ArrayList<InterfaceDataSource>();
	}
	
	public int getUnitCount(){
		return units.size();
	}
	
	public void componentUpdated(NetworkComponentEvent e){
		fireListeners();
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		//if (treeNode == null){
		//	removeListener((InterfaceNeuralNetTreeNode)treeNode);
		//else
		//	treeNode = new InterfaceNetworkTreeNode(this);
		//	addListener((InterfaceNetworkTreeNode)treeNode);
		//	}
		//treeNode.removeAllChildren();
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
		InterfaceTreeNode unitNode = new InterfaceTreeNode("Units");
		for (int i = 0; i < units.size(); i++){
			//units.get(i).setTreeNode();
			unitNode.add(units.get(i).issueTreeNode());
			}
		treeNode.add(unitNode);
	}
	
	//public InterfaceTreeNode getTreeNode(){
	//	if (treeNode == null) setTreeNode();
	//	return treeNode;
	//}
	
	public boolean addUnit(ConnectableNeuron unit) throws NeuroNetException{
		return addUnit(unit, true);
	}
	
	public boolean addUnit(ConnectableNeuron unit, boolean fire) throws NeuroNetException{
		units.add(unit);
		//TODO connect this unit?
		unit.addListener(this);
		unit.setID(idFactory.getID());
		fireUnitAdded(unit);
		if (fire) fireListeners();
		return true;
	}
	
	/*
	public void setEnvironment(DynamicModelEnvironment e) throws NeuroNetException{
		try{
			super.setEnvironment(e);
		}catch (DynamicModelException ex){
			throw new NeuroNetException(ex.getMessage());
			}
	}
	*/
	
	protected void fireUnitAdded(ConnectableNeuron n){
		fireComponentAdded(n);
		//add all subcomponents
		
	}
	
	public boolean removeUnit(ConnectableNeuron unit){
		if (!units.remove(unit)) return false;
		//TODO remove connections?
		unit.removeListener(this);
		
		fireListeners();
		return true;
	}
	
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	/***************************
	 * Returns the energy calculation for this neural network. I.e., the Lyapunov
	 * function. All instances of AbstractNeuralNet must implement this.
	 * @return
	 */
	public abstract double getEnergy();
	
	public ArrayList<DynamicModelComponent> getComponents(){
		ArrayList<DynamicModelComponent> list = new ArrayList<DynamicModelComponent>();
		for (int i = 0; i < units.size(); i++){
			list.add(units.get(i));
			list.addAll(units.get(i).getSubComponents());
			}
		for (int i = 0; i < inputs.size(); i++){
			list.add(inputs.get(i));
			list.addAll(inputs.get(i).getSubComponents());
			}
		return list;
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
	}

}