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


package mgui.neuro.components.simple;

import mgui.interfaces.attributes.Attribute;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.models.dynamic.DynamicModelOutputEvent;
import mgui.neuro.components.AbstractMotorNeuron;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;

/***************************************************************
 * Simple implementation of a motor (output) neuron. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SimpleMotorNeuron extends AbstractMotorNeuron {

	public SimpleMotorNeuron(){
		init();
	}
	
	public SimpleMotorNeuron(int index, double output){
		init();
		//this.index = index;
		//this.output = output;
		setIndex(index);
		setOutput(output);
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute<MguiInteger>("Index", new MguiInteger(0)));
		attributes.add(new Attribute<MguiDouble>("Output", new MguiDouble(1)));
	}
	
	//create a basic simple motor neuron with one dendrite
	public static SimpleMotorNeuron getBasicUnit(){
		SimpleMotorNeuron neuron = new SimpleMotorNeuron();
		try{
			neuron.setSoma(new SimpleSoma(0, 1, 0));
			SimpleDendrites dendrites = new SimpleDendrites();
			dendrites.addDendrite(new SimpleDendrite(0));
			neuron.setDendrites(dendrites);
		}catch (NeuroException e){
			e.printStackTrace();
			return null;
			}
		return neuron;
	}
	
	public double getOutput() {
		return ((MguiDouble)attributes.getValue("Output")).getValue();
	}
	
	public int getIndex() {
		return ((MguiInteger)attributes.getValue("Index")).getInt();
	}
	
	public void setOutput(double output) {
		//((MguiDouble)attributes.getValue("Output")).value = output;
		//this.output = output;
		attributes.setValue("Output", new MguiDouble(output));
	}
	
	public void setIndex(int index) {
		//((MguiInteger)attributes.getValue("Index")).value = index;
		//this.index = index;
		attributes.setValue("Index", new MguiInteger(index));
	}
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException{
		if (e instanceof SimpleSomaEvent){
			setOutput(((SimpleSoma)getSoma()).
					transferFunction.evaluate(((SimpleSomaEvent)e).output));
			//fireOutputEvent();
			return true;
			}
		return false;
	}
	
	protected void fireOutputEvent(){
		fireOutputEvent(new SimpleMotorNeuronEvent(this));
	}
	
	protected boolean updateComponent(){
		fireOutputEvent();
		return true;
	}
	
	public Object clone() {
		SimpleMotorNeuron neuron = new SimpleMotorNeuron(getIndex(), getOutput());
		try{
			if (getSoma() != null) neuron.setSoma((SimpleSoma)getSoma().clone());
			if (getDendrites() != null) neuron.setDendrites((SimpleDendrites)getDendrites().clone());
			//preserved in case subclasses set an axon
			if (getAxon() != null) neuron.setAxon((SimpleAxon)getAxon().clone());
			neuron.environment = environment;
		}catch (NeuroException e){
			e.printStackTrace();
			return null;
			}
		return neuron;
	}

}