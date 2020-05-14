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

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.neuro.exceptions.NeuroException;


public abstract class AbstractNeuron extends AbstractNeuroComponent implements Neuron {

	//*****COMPONENTS*****
	public AbstractAxon axon;
	public AbstractSoma soma;
	public AbstractDendrites dendrites;
	
	//all subclasses must call this from their constructor
	protected void init(){
		super.init();
		hasSubComponents = true;
	}
	
	public AbstractSoma getSoma(){
		//return (Soma)attributes.getValue("Soma");
		return soma;
	}
	
	public void setSoma(Soma s) throws NeuroException{
		if (!(s instanceof AbstractSoma)) throw new NeuroException("AbstractNeuron" +
				"can only contain soma which is instance of AbstractSoma");
		AbstractSoma s2 = (AbstractSoma)s;
		try{
			if (soma != null){
				soma.setParent(null);
				if (axon != null)
					soma.removeConnection(axon);
				}
			s2.setParent(this);
			//attributes.setValue("Soma", s);
			soma = s2;
			if (axon != null) s2.addConnection(axon);
			connectDendrites();
		}catch (NeuroException e){
			e.printStackTrace();
			}
	}
	
	public Dendrites getDendrites(){
		return dendrites;
		//return (Dendrites)attributes.getValue("Dendrites");
	}
	
	public void setDendrites(Dendrites d) throws NeuroException{
		if (!(d instanceof AbstractDendrites)) throw new NeuroException("AbstractNeuron" +
				"can only contain dendritic tree which is instance of AbstractDendrites");
		AbstractDendrites d2 = (AbstractDendrites)d;
		if (dendrites != null) dendrites.setParent(null);
		d2.setParent(this);
		//attributes.setValue("Dendrites", d);
		dendrites = d2;
		connectDendrites();
	}
	
	public Axon getAxon(){
		//return (Axon)attributes.getValue("Axon");
		return axon;
	}
	
	public static AbstractNeuron getBasicUnit(){return null;}
	
	public void setAxon(Axon a) throws NeuroException{
		if (!(a instanceof AbstractAxon)) throw new NeuroException("AbstractNeuron" +
			"can only contain axon which is instance of AbstractAxon");
		AbstractAxon a2 = (AbstractAxon)a;
		if (axon != null){
			axon.setParent(null);
			if (soma != null)
				soma.removeConnection(axon);
			}
		a2.setParent(this);
		//attributes.setValue("Axon", a);
		axon = a2;
		if (soma != null) soma.addConnection(a2);
	}
	
	/*************************
	 * Resets the dendrite connectivity for this neuron by connecting all dendrites
	 * which are not connected to other dendrites to this neuron's soma. If soma or
	 * dendrites are null this does nothing, obviously :P 
	 */
	public void connectDendrites(){
		//remove any existing connections to somata
		//and add connection to this soma
		AbstractSoma soma = getSoma();
		AbstractDendrites dendrites = (AbstractDendrites)getDendrites();
		try{
			if (soma == null || dendrites == null) return;
			for (int i = 0; i < dendrites.components.size(); i++)
				if (!dendrites.components.get(i).isConnectedToDendrite())
					dendrites.components.get(i).connectTo(soma);
		}catch(NeuroException e){
			e.printStackTrace();
			}
	}

	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		//soma.setTreeNode();
		treeNode.add(soma.issueTreeNode());
		//axon.setTreeNode();
		treeNode.add(axon.issueTreeNode());
		//dendrites.setTreeNode();
		treeNode.add(dendrites.issueTreeNode());
		return;
	}
	
	public ArrayList<DynamicModelComponent> getSubComponents(){
		ArrayList<DynamicModelComponent> list = new ArrayList<DynamicModelComponent>();
		list.add(axon);
		if (axon.hasSubComponents())
			list.addAll(axon.getSubComponents());
		list.addAll(dendrites.getSubComponents());
		list.add(soma);
		if (soma.hasSubComponents())
			list.addAll(soma.getSubComponents());
		return list;
	}
	
	public String toString(){
		return "Neuron [" + getID() + "]";
	}
	
}