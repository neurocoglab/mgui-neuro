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


package mgui.neuro.components.compartments;

import mgui.neuro.components.Neuron;
import mgui.neuro.components.NeuronalComponent;
import mgui.neuro.exceptions.NeuroException;

public abstract class NeuronalCompartmentalComponent extends CompartmentalComponent 
												  implements NeuronalComponent {

	public CompartmentalNeuron parent;
	
	public Neuron getParent() {
		return parent;
	}

	public void setParent(Neuron n) throws NeuroException {
		if (!(n instanceof CompartmentalNeuron)) throw new NeuroException(
				"NeuronalCompartmentalComponent must have parent which is a instance" +
				"of CompartmentalNeuron");
		parent = (CompartmentalNeuron)n;
	}

}