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
import mgui.models.dynamic.functions.Function;
import mgui.models.dynamic.functions.HardLimitFunction;
import mgui.models.environments.SimpleEnvironmentEvent;
import mgui.neuro.components.AbstractSoma;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;

/******************************
 * Simple soma for, e.g., <code>SimpleNeuron</code>. Fires an "action potential" of magnitude specified
 * by the output <code>parameter</code>, if <code>potential</code> > <code>threshold</code>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class SimpleSoma extends AbstractSoma {

	public Function transferFunction = new HardLimitFunction(-1, 0, 1);
	
	//*****VARIABLES*****
	public double potential;
	
	public SimpleSoma(double threshold, double output, double restingV){
		init();
		setThreshold(threshold);
		setOutput(output);
		setRestingV(restingV);
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute<MguiDouble>("Threshold", new MguiDouble(0.0)));
		attributes.add(new Attribute<MguiDouble>("Output", new MguiDouble(0.0)));
		attributes.add(new Attribute<MguiDouble>("RestingV", new MguiDouble(0.0)));
	}
	
	//setters & getters
	public void setThreshold(double t){
		attributes.setValue("Threshold", new MguiDouble(t));
	}
	
	public double getThreshold(){
		return ((MguiDouble)attributes.getValue("Threshold")).getValue();
	}
	
	public void setOutput(double o){
		attributes.setValue("Output", new MguiDouble(o));
	}
	
	public double getOutput(){
		return ((MguiDouble)attributes.getValue("Output")).getValue();
	}
	
	public void setRestingV(double r){
		attributes.setValue("RestingV", new MguiDouble(r));
	}
	
	public double getRestingV(){
		return ((MguiDouble)attributes.getValue("RestingV")).getValue();
	}
	
	
	//integrate potentials from all inputs
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException{
		
		if (e instanceof SimpleEnvironmentEvent){
			if (getParent() == null || !(getParent() instanceof SimpleSensoryNeuron))
				throw new NeuroException("SimpleEnvironment event sent to SimpleSoma with" +
						"parent neuron which is either null or is not an instance of" +
						"SimpleSensoryNeuron");
			SimpleSensoryNeuron in = (SimpleSensoryNeuron)getParent();
			potential += ((SimpleEnvironmentEvent)e).getInputState()[in.getIndex()] * in.getTuning();
			return true;
		}
		
		if (e instanceof SimpleDendriteEvent){
			potential += ((SimpleDendriteEvent)e).potential;
			return true;
		}
		
		return false;
	}
	
	
	//will fire an action potential if potential >= threshold
	protected boolean updateComponent(){
		if (potential <= getThreshold()) return false;
		
		//fire action potential
		for (int i = 0; i < connections.size(); i++)
			connections.get(i).addEvent(new SimpleSomaEvent(this));
		
		//reset potential
		//TODO use decay function
		potential = getRestingV();
		
		return true;
	}
	
	public Object clone(){
		SimpleSoma soma = new SimpleSoma(getThreshold(), getOutput(), getRestingV());
		//soma.output = output;
		return soma;
	}
	
	
}