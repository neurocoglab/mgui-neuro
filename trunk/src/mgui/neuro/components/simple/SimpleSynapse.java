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
import mgui.neuro.components.AbstractSynapse;
import mgui.neuro.components.types.WeightedSynapse;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;

/**********************************
 * Simple synapse for, e.g., <code>SimpleNeuron</code>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class SimpleSynapse extends AbstractSynapse implements WeightedSynapse {
	
	//*****PARAMETERS***** (set as Attributes)
	//double weight;
	//double restingV;
	
	//*****VARIABLES*****
	protected double potential;
	
	public SimpleSynapse(double weight, double restingV){
		init();
		//this.weight = weight;
		//this.restingV = restingV;
		setWeight(weight);
		setRestingV(restingV);
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute<MguiDouble>("Weight", new MguiDouble(0.0)));
		attributes.add(new Attribute<MguiDouble>("RestingV", new MguiDouble(0.0)));
	}
	
	public double getWeight(){
		return ((MguiDouble)attributes.getValue("Weight")).getValue();
	}
	
	public void setWeight(double weight){
		//((MguiDouble)attributes.getValue("Weight")).value = weight;
		attributes.setValue("Weight", new MguiDouble(weight));
	}
	
	public void setRestingV(double r){
		//((MguiDouble)attributes.getValue("RestingV")).value = r;
		attributes.setValue("RestingV", new MguiDouble(r));
	}
	
	public double getRestingV(){
		return ((MguiDouble)attributes.getValue("RestingV")).getValue();
	}
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException{
		
		/*
		if (!(e instanceof SimpleNeuroModelEvent)) throw new NeuroException(
				"Events on SimpleDendrite must be instances of SimpleNeuroModelEvent");

		switch (e.getCode()){
			//axon input
			case SimpleNeuroModelEvent.S_AXN_OUTPUT:
				if (!(e.getSource() instanceof SimpleAxon)) throw new NeuroException(
						"Axon input to SimpleSynapse must originate from axon that is " +
						"instance of SimpleAxon");
				potential += ((SimpleAxon)e.getSource()).output * weight;
				return true;
			}
			*/
		
		//integrate AP output and synaptic weight
		if (e instanceof SimpleAxonEvent){
			potential += ((SimpleAxonEvent)e).output * getWeight();
			return true;
			}
		
		return false;
	}
	
	protected boolean updateComponent(){
		//send potential to all connected components (dendrite)
		//this.addEvent(new SimpleNeuroModelEvent(this, SimpleNeuroModelEvent.S_SYN_POTENTIAL));
		for (int i = 0; i < connections.size(); i++)
			connections.get(i).addEvent(new SimpleSynapseEvent(this));
		
		//reset potential
		//TODO implement decay function
		potential = getRestingV();
		return true;
	}
	
	public Object clone(){
		SimpleSynapse syn = new SimpleSynapse(getWeight(), getRestingV());
		syn.potential = potential;
		return syn;
	}
}