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

import mgui.models.dynamic.DynamicModelComponent;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelOutputEvent;
import mgui.neuro.components.types.ConnectableNeuron;
import mgui.neuro.exceptions.NeuroException;


/***********************************
 * Abstract class for a motor (output) neuron. Note that a motor neuron acts similarly to a
 * muscle cell; i.e., it does not set an axon and instead acts directly on its environment
 * by passing an <code>MotorOutputEvent</code>. This is based on the idea that most nerve-muscle 
 * junctions are highly robust. If a more realistic model is desired (i.e., one that models this
 * junction explicitly), the setAxon method must be overridden by the subclass; otherwise an
 * attempt to set the axon will throw a <code>NeuroException</code> 
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public abstract class AbstractMotorNeuron extends ConnectableNeuron implements MotorNeuron {

	public DynamicModelEnvironment environment;
	
	public void setAxon(AbstractAxon a) throws NeuroException{
		throw new NeuroException("MotorNeuron cannot set an axon; it must act directly on its" +
								 "environment (see specification)");
	}
	
	public ArrayList<DynamicModelComponent> getSubComponents(){
		ArrayList<DynamicModelComponent> list = new ArrayList<DynamicModelComponent>();
		list.addAll(dendrites.getSubComponents());
		list.add(soma);
		if (soma.hasSubComponents())
			list.addAll(soma.getSubComponents());
		return list;
	}
	
	public void setEnvironment(DynamicModelEnvironment e){
		environment = e;
	}
	
	public DynamicModelEnvironment getEnvironment(){
		return environment;
	}
	
	protected void fireOutputEvent(DynamicModelOutputEvent e){
		environment.handleOutputEvent(e);
	}

	public String toString(){
		return "Motor Neuron [" + getID() + "]";
	}
	
}