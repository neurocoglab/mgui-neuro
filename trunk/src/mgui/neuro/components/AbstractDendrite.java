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


public abstract class AbstractDendrite extends AbstractNeuronalComponent implements Dendrite {

	public ArrayList<Synapse> synapses = new ArrayList<Synapse>();
	public AbstractDendrite next;
	public AbstractSoma soma;
	
	protected void init(){
		super.init();
		hasSubComponents = true;
	}
	
	/*************************
	 * Add a synapse and create connection between this dendrite and synapse
	 * @param s synapse to add
	 */
	public void addSynapse(Synapse s) throws NeuroException{
		if (!(s instanceof AbstractSynapse)) throw new NeuroException("AbstractDendrite" +
				"can only add synapses which are instances of AbstractSynapse");
		AbstractSynapse s2 = (AbstractSynapse)s;
		synapses.add(s2);
		s2.addConnection(this);
		s2.setParent(parent);
	}
	
	/*************************
	 * Remove a synapse along with its connection to this dendrite
	 * @param s synapse to remove
	 */
	public void removeSynapse(Synapse s){
		if (!(s instanceof AbstractSynapse)) return;
		AbstractSynapse s2 = (AbstractSynapse)s;
		if (!synapses.remove(s2)) return;
		s2.removeConnection(this);
		try{
			s2.setParent(null);
		} catch (NeuroException e){
			e.printStackTrace();
			}
	}
	/*************************
	 * Connects this dendrite to another dendrite. All existing connections are removed.
	 * 
	 * @param d dendrite to connect to
	 */
	public void connectTo(Dendrite d) throws NeuroException{
		if (!(d instanceof AbstractDendrite)) throw new NeuroException("AbstractDendrite" +
				"can only connect to dendrites which are instances of AbstractDendrite");
		AbstractDendrite d2 = (AbstractDendrite)d;
		if (soma != null)
			removeConnection(soma);
		if (next != null)
			disconnect();
		addConnection(d2);
		next = d2;
	}
	
	/*************************
	 * Connects this dendrite to a soma. All existing connections are removed.
	 * 
	 * @param s soma to connect to
	 */
	public void connectTo(Soma s) throws NeuroException{
		if (!(s instanceof AbstractSoma)) throw new NeuroException("AbstractDendrite" +
				"can only connect to somata which are instances of AbstractSoma");
		AbstractSoma s2 = (AbstractSoma)s;
		if (soma != null)
			removeConnection(soma);
		if (next != null)
			disconnect();
		addConnection(s2);
		soma = s2;
	}
	
	public void disconnect(){
		if (next != null) removeConnection(next);
		next = null;
	}
	
	public boolean isConnectedToDendrite(){
		return next != null;
	}
	
	public boolean isConnectedToSoma(){
		return soma != null;
	}
	
	public int getSynapseCount(){
		return synapses.size();
	}
	
	public ArrayList<Synapse> getSynapses(){
		return synapses;
	}
	
	public void setParent(Neuron n) throws NeuroException {
		try{
			super.setParent(n);
		}catch (NeuroException e){
			throw e;
			}
		
		//set all components
		try{
			for (int i = 0; i < synapses.size(); i++)
				((AbstractSynapse)synapses.get(i)).setParent(parent);
		}catch (NeuroException e){
			e.printStackTrace();
			}
	}
	
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		for (int i = 0; i < synapses.size(); i++)
			treeNode.add(((AbstractSynapse)synapses.get(i)).issueTreeNode());
		//TODO expand next dendrite on demand
		if (isConnectedToDendrite())
			treeNode.add(new InterfaceTreeNode("Next>dendrite"));
		if (isConnectedToSoma())
			treeNode.add(new InterfaceTreeNode("Next>soma"));
	}
	
	public ArrayList<DynamicModelComponent> getSubComponents(){
		ArrayList<DynamicModelComponent> list = new ArrayList<DynamicModelComponent>(); 
		for (int i = 0; i < synapses.size(); i++){
			list.add(((AbstractSynapse)synapses.get(i)));
			if (((AbstractSynapse)synapses.get(i)).hasSubComponents())
				list.addAll(((AbstractSynapse)synapses.get(i)).getSubComponents());
			}
		return list;
	}
	
	public String toString(){
		return "Dendrite [" + getID() + "]";
	}
	
}