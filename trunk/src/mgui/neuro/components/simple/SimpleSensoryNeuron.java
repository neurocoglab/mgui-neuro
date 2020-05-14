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
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEnvironmentEvent;
import mgui.models.environments.SimpleEnvironment;
import mgui.models.environments.SimpleEnvironmentEvent;
import mgui.models.updaters.SimpleEnvironmentUpdater;
import mgui.neuro.components.AbstractSensoryNeuron;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;

/*************************************
 * A simple input (i.e., sensory) neuron.
 * Simple input neurons have a soma and axon; the respond to environmental state and their
 * activity is determined by this state (rather than dendrites). Simple sensory neurons
 * are limited to one discrete sample from their environments, designated by the parameter
 * <code>input</code>.
 * 
 * @author Andrew Reid
 *
 */
public class SimpleSensoryNeuron extends AbstractSensoryNeuron {

	//public static SimpleSensoryNeuron BasicSimpleSensoryNeuron = getBasicUnit();
	
	//*****PARAMETERS*****
	//int input;				//where to sample from environment
	//double tuning;			//magnification of the input value
	
	//*****VARIABLES*****
	
	public SimpleSensoryNeuron(){
		init();
	}
	
	public SimpleSensoryNeuron(int input, double tuning){
		init();
		setIndex(input);
		setTuning(tuning);
	}
	
//	create a basic simple neuron with one dendrite
	public static SimpleSensoryNeuron getBasicUnit(){
		SimpleSensoryNeuron neuron = new SimpleSensoryNeuron();
		try{
			neuron.setSoma(new SimpleSoma(0, 1, 0));
			//SimpleDendrites dendrites = new SimpleDendrites();
			//dendrites.addDendrite(new SimpleDendrite(0));
			//neuron.setDendrites(dendrites);
			neuron.setAxon(new SimpleAxon());
		}catch (NeuroException e){
			e.printStackTrace();
			return null;
			}
		return neuron;
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute<MguiInteger>("Index", new MguiInteger(0)));
		attributes.add(new Attribute<MguiDouble>("Tuning", new MguiDouble(1.0)));
		
	}
	
	public void setIndex(int i){
		//((MguiInteger)attributes.getValue("Index")).value = i;
		attributes.setValue("Index", new MguiInteger(i));
	}
	
	public int getIndex(){
		return ((MguiInteger)attributes.getValue("Index")).getInt();
	}
	
	public void setTuning(double t){
		//((MguiDouble)attributes.getValue("Tuning")).value = t;
		attributes.setValue("Index", new MguiDouble(t));
	}
	
	public double getTuning(){
		return ((MguiDouble)attributes.getValue("Tuning")).getValue();
	}
	
	protected static SimpleSensoryNeuron getBasicSensoryNeuron(){
		SimpleSensoryNeuron neuron = new SimpleSensoryNeuron();
		try{
			neuron.setAxon(new SimpleAxon());
			neuron.setSoma(new SimpleSoma(0, 1, 0));
		}catch (NeuroException e){
			e.printStackTrace();
			return null;
			}
		return neuron;
	}
	
	public SimpleSensoryNeuron getBasicInstance(int dendCount, double restingV){
		return null;
	}
	
	//respond to environment
	public boolean stimulate(DynamicModelEnvironmentEvent e){
		try {
			return update(e.getEnvironment());
		}catch (NeuroException ex){
			ex.printStackTrace();
			}
		return false;
	}
	
	protected boolean update(DynamicModelEnvironment environment) throws NeuroException{
		if (!(environment instanceof SimpleEnvironment)) throw new NeuroException(
				"SimpleSensoryNeuron can only be updated by environment which is an instance" +
				"of SimpleEnvironment");
		
		((SimpleSoma)getSoma()).addEvent(new SimpleEnvironmentEvent((SimpleEnvironment)environment));
		return true;
	}
	
	public Object clone() {
		SimpleSensoryNeuron neuron = new SimpleSensoryNeuron();
		try{
			if (getSoma() != null) neuron.setSoma((SimpleSoma)getSoma().clone());
			if (getDendrites() != null) neuron.setDendrites((SimpleDendrites)getDendrites().clone());
			if (getAxon() != null) neuron.setAxon((SimpleAxon)getAxon().clone());
			neuron.setIndex(getIndex());
			neuron.setTuning(getTuning());
		}catch (NeuroException e){
			e.printStackTrace();
			return null;
			}
		return neuron;
	}

	
}