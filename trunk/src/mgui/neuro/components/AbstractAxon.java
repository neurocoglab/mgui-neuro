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

import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.neuro.exceptions.NeuroException;


/*******************************************
 * Default class for an axon. An axon can be connected to any number of synapses. 
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public abstract class AbstractAxon extends AbstractNeuronalComponent implements Axon {
	
	public ArrayList<Synapse> synapses = new ArrayList<Synapse>();
	
	protected void init(){
		super.init();
		hasSubComponents = true;
	}
	
	public void connectSynapse(Synapse s) throws NeuroException{
		if (!(s instanceof AbstractSynapse)) throw new NeuroException("AbstractAxon" +
			"can only connect to synapses which are instances of AbstractSynapse");
		AbstractSynapse s2 = (AbstractSynapse)s;
		synapses.add(s2);
		addConnection(s2);
	}
	
	public void disconnectSynapse(Synapse s){
		synapses.remove(s);
		removeConnection(s);
	}
	
	public ArrayList<Synapse> getSynapses(){
		return synapses;
	}
	
	public void disconnectAll(){
		for (int i = 0; i < connections.size(); i++)
			if (connections.get(i) instanceof AbstractSynapse){
				connections.remove(i);
				i--;
				}
	}
	
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		for (int i = 0; i < synapses.size(); i++)
			treeNode.add(((AbstractSynapse)synapses.get(i)).issueTreeNode());
	}
	
	
	public ArrayList<DynamicModelComponent> getSubComponents(){
		ArrayList<DynamicModelComponent> list = new ArrayList<DynamicModelComponent>(); 
		for (int i = 0; i < synapses.size(); i++){
			list.add(synapses.get(i));
			if (synapses.get(i).hasSubComponents())
				list.addAll(synapses.get(i).getSubComponents());
			}
		return list;
	}
	
	public String toString(){
		return "Axon [" + getID() + "]";
	}
	
}