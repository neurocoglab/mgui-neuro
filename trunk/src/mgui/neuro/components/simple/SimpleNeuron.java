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

import mgui.neuro.components.AbstractAxon;
import mgui.neuro.components.AbstractDendrite;
import mgui.neuro.components.AbstractDendrites;
import mgui.neuro.components.AbstractNeuron;
import mgui.neuro.components.AbstractSoma;
import mgui.neuro.components.AbstractSynapse;
import mgui.neuro.components.types.ConnectableNeuron;
import mgui.neuro.exceptions.NeuroException;

/*************************************
 * Simple McCulloch-Pitts model of an artificial neuron.
 * See http://www.fulton.asu.edu/~nsfadp/ieeecis/Emil_M_Petriu.pdf.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */


public class SimpleNeuron extends ConnectableNeuron {

	//public static SimpleNeuron BasicSimpleNeuron = getBasicSimpleNeuron();
	
	public SimpleNeuron(){
		super.init();
	}
	
	//create a basic simple neuron with one dendrite
	public static SimpleNeuron getBasicUnit(){
		SimpleNeuron neuron = new SimpleNeuron();
		try{
			neuron.setSoma(new SimpleSoma(0, 1, 0));
			SimpleDendrites dendrites = new SimpleDendrites();
			dendrites.addDendrite(new SimpleDendrite(0));
			neuron.setDendrites(dendrites);
			neuron.setAxon(new SimpleAxon());
		}catch (NeuroException e){
			e.printStackTrace();
			return null;
			}
		return neuron;
	}
	
	/*******************************
	 * Returns an instance of a basic simple neuron with dendCount basic (non branching)
	 * dendrites, each with a resting potential of restingV.
	 * @param dendCount number of dendrites to add
	 * @param restingV resting potential for these dendrites
	 * @return a SimpleNeuron
	 */
	public SimpleNeuron getBasicInstance(int dendCount, double restingV){
		//SimpleNeuron neuron = (SimpleNeuron)BasicSimpleNeuron.clone();
		SimpleNeuron neuron = getBasicUnit();
		if (dendCount <= 1) return neuron;
		
		//add dendCount dendrites
		SimpleDendrite thisDendrite  = new SimpleDendrite(restingV);
		SimpleDendrites dendrites = new SimpleDendrites();
		try{
			for (int i = 0; i < dendCount; i++)
				dendrites.addDendrite((SimpleDendrite)thisDendrite.clone());
			neuron.setDendrites(dendrites);
		}catch (NeuroException e){
			e.printStackTrace();
			return null;
			}
		
		return neuron;	
	}
	
	//override super methods simply to validate class instances
	public void setSoma(AbstractSoma s) throws NeuroException{
		if (!(s instanceof SimpleSoma)) throw new NeuroException(
				"Can only add soma which is instance of SimpleSoma to SimpleNeuron");
		super.setSoma(s);
	}
	
	public void setDendrites(AbstractDendrites d) throws NeuroException{
		if (!(d instanceof SimpleDendrites)) throw new NeuroException(
				"Can only add dendrites which are instance of SimpleDendrites to SimpleNeuron");
		super.setDendrites(d);
	}
	
	public void setAxon(AbstractAxon a) throws NeuroException{
		if (!(a instanceof SimpleAxon)) throw new NeuroException(
				"Can only add axon which is instance of SimpleAxon to SimpleNeuron");
		super.setAxon(a);
	}
	
	public Object clone(){
		SimpleNeuron neuron = new SimpleNeuron();
		neuron.setID(id);
		try{
		if (getSoma() != null) neuron.setSoma((SimpleSoma)getSoma().clone());
		if (getDendrites() != null) neuron.setDendrites((SimpleDendrites)getDendrites().clone());
		if (getAxon() != null) neuron.setAxon((SimpleAxon)getAxon().clone());
		}catch (NeuroException e){
			e.printStackTrace();
			return null;
			}
		return neuron;
	}
	
}