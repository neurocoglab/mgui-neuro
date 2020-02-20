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


package mgui.neuro.components.simple;

import mgui.interfaces.attributes.Attribute;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.neuro.components.*;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;

/************************************
 * Simple dendrite, e.g., for <code>SimpleNeuron</code>.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class SimpleDendrite extends AbstractDendrite {

	//*****PARAMETERS*****
	//double restingV;
	
	//*****VARIABLES*****
	double potential;
	
	public SimpleDendrite(double restingV){
		init();
		setRestingV(restingV);
		//this.restingV = restingV;
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute<MguiDouble>("RestingV", new MguiDouble(0)));
		setDelay(0);
		//attributes.add(new Attribute("Delay", new arDouble(0)));
	}
	
	public void setRestingV(double restingV){
		//((MguiDouble)attributes.getValue("RestingV")).value = restingV;
		attributes.setValue("RestingV", new MguiDouble(restingV));
	}
	
	public double getRestingV(){
		return ((MguiDouble)attributes.getValue("RestingV")).getValue();
	}
	
	
	
	//override super methods simply to check for correct class instances
	public void addSynapse(Synapse s) throws NeuroException{
		if (!(s instanceof SimpleSynapse)) throw new NeuroException(
				"Can only add synapse which is instance of SimpleSynapse to SimpleDendrite");
		super.addSynapse(s);
	}
	
	public void connectTo(Dendrite d) throws NeuroException{
		if (!(d instanceof SimpleDendrite)) throw new NeuroException(
				"Can only connect SimpleDendrite to dendrite which is instance of SimpleDendrite");
		super.connectTo(d);
	}
	
	public void connectTo(Soma s) throws NeuroException{
		if (!(s instanceof SimpleSoma)) throw new NeuroException(
				"Can only connect SimpleDendrite to soma which is instance of SimpleSoma");
		super.connectTo(s);
	}
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException{
		
		/*
		if (!(e instanceof SimpleNeuroModelEvent)) throw new NeuroException(
				"Events on SimpleDendrite must be instances of SimpleNeuroModelEvent");
			
		switch (e.getCode()){
			//axon input
			case SimpleNeuroModelEvent.S_SYN_POTENTIAL:
				//shouldn't occur
				if (!(e.getSource() instanceof SimpleSynapse)) throw new NeuroException(
						"S_SYN_POTENTIAL event fired from object that is not an instance of" +
						"SimpleSynapse!?");
				//simply integrate synaptic potentials
				potential += ((SimpleSynapse)e.getSource()).potential;
				return true;
				
			case SimpleNeuroModelEvent.S_DND_POTENTIAL:
				//shouldn't occur either
				if (!(e.getSource() instanceof SimpleDendrite)) throw new NeuroException(
						"S_DND_POTENTIAL event fired from object that is not an instance of" +
						"SimpleDendrite!?");
				//simply sum all inputs on dendritic tree
				potential += ((SimpleDendrite)e.getSource()).potential;
				return true;
			}
			*/
		
		if (e instanceof SimpleSynapseEvent){
			potential += ((SimpleSynapseEvent)e).potential;
			updated = true;
			return true;
			}
		
		if (e instanceof SimpleDendriteEvent){
			potential += ((SimpleDendriteEvent)e).potential;
			updated = true;
			return true;
			}
		
		return false;
	}
	
	//will execute on every time step since simple dendrite is instantaneous
	protected boolean updateComponent(){
		//if (!updated) return false;
		//send potential to all connected components (dendrites and somata)
		for (int i = 0; i < connections.size(); i++)
			connections.get(i).addEvent(new SimpleDendriteEvent(this));
		
		//reset potential
		potential = getRestingV();
		
		return true;
	}
	
	public Object clone(){
		SimpleDendrite dendrite = new SimpleDendrite(getRestingV());
		dendrite.potential = potential;
		return dendrite;
	}
	
}