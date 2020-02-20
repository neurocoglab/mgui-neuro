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
import mgui.models.dynamic.DynamicModelEnvironmentSensor;
import mgui.neuro.components.types.ConnectableNeuron;


public abstract class AbstractSensoryNeuron extends ConnectableNeuron 
									implements DynamicModelEnvironmentSensor,
											   SensoryNeuron {

	public ArrayList<DynamicModelComponent> getSubComponents(){
		ArrayList<DynamicModelComponent> list = new ArrayList<DynamicModelComponent>();
		list.add(axon);
		if (axon.hasSubComponents())
			list.addAll(axon.getSubComponents());
		//list.addAll(dendrites.getSubComponents());
		list.add(soma);
		if (soma.hasSubComponents())
			list.addAll(soma.getSubComponents());
		return list;
	}
	
	public String toString(){
		return "Sensory Neuron [" + getID() + "]";
	}
	
}