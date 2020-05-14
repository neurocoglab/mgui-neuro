/*
* Copyright (C) 2020 Andrew Reid and the ModelGUI Project <http://www.modelgui.org>
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


/***********************************
 * Default class for dendrites. Contains a list of dendrite objects which can be
 * connected to other dendrites. A dendrite can only be connected to one other dendrite,
 * thus running connectDendrites will disconnect any existing connections d1 has. A
 * dendrite can receive any number of connections; thus the set of connected dendrites
 * is either disjoint or a tree. All dendrites without forward connections should be
 * connected to a soma by the instance of Neuron that contains them.
 * 
 * NOTE: the tree constraint can be broken by overriding methods, if this is for some
 * reason desirable.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public abstract class AbstractDendrites extends AbstractNeuronalComponent implements Dendrites {

	public ArrayList<AbstractDendrite> components = new ArrayList<AbstractDendrite>();
	
	protected void init(){
		super.init();
		hasSubComponents = true;
	}
	
	public void addDendrite(Dendrite d) throws NeuroException{
		if (!(d instanceof AbstractDendrite)) throw new NeuroException("AbstractDendrites" +
				"can only contain dendrites which are instances of AbstractDendrite");
		AbstractDendrite d2 = (AbstractDendrite)d;
		components.add(d2);
		if (parent != null)
			d2.setParent(parent);
	}
	
	public void addDendrite(Dendrite d, Dendrite d2) throws NeuroException{
		if (!(d instanceof AbstractDendrite && d2 instanceof AbstractDendrite)) 
			throw new NeuroException("AbstractDendrites can only contain dendrites which"
					+ "are instances of AbstractDendrite");
		AbstractDendrite d3 = (AbstractDendrite)d;
		AbstractDendrite d4 = (AbstractDendrite)d2;
		components.add(d3);
		d3.connectTo(d4);
		d3.setParent(parent);
	}
	
	public void removeDendrite(Dendrite d) throws NeuroException{
		if (!(d instanceof AbstractDendrite)) return;
		AbstractDendrite d2 = (AbstractDendrite)d;
		if (components.remove(d2)) d2.setParent(null);
		
	}
	
	public void setParent(AbstractNeuron n){
		parent = n;
		//set all components
		try{
			for (int i = 0; i < components.size(); i++)
				components.get(i).setParent(parent);
		}catch (NeuroException e){
			e.printStackTrace();
			}
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		for (int i = 0; i < components.size(); i++){
			//components.get(i).setTreeNode();
			treeNode.add(components.get(i).issueTreeNode());
			}
	}
	
	public ArrayList<DynamicModelComponent> getSubComponents(){
		ArrayList<DynamicModelComponent> list = new ArrayList<DynamicModelComponent>(); 
		for (int i = 0; i < components.size(); i++){
			list.add(components.get(i));
			if (components.get(i).hasSubComponents())
				list.addAll(components.get(i).getSubComponents());
			}
		return list;
	}
	
	public ArrayList<Synapse> getSynapses(){
		ArrayList<Synapse> list = new ArrayList<Synapse>();
		for (int i = 0; i < components.size(); i++)
			list.addAll(components.get(i).getSynapses());
		return list;
	}
	
	public String toString(){
		return "Dendrites [" + getID() + "]";
	}
	
}