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


package mgui.neuro.components.types;

import mgui.neuro.components.AbstractAxon;
import mgui.neuro.components.AbstractDendrite;
import mgui.neuro.components.AbstractDendrites;
import mgui.neuro.components.AbstractNeuron;
import mgui.neuro.components.AbstractSynapse;
import mgui.neuro.exceptions.NeuroException;

public abstract class ConnectableNeuron extends AbstractNeuron {

	public boolean connectTo(ConnectableNeuron neuron, AbstractSynapse synapse) throws NeuroException{
		/*
		if (!(neuron instanceof SimpleNeuron)) throw new NeuroException("Can only connect" +
				" neurons which are instances of SimpleNeuron to SimpleNeuron");
		*/
		if (neuron.dendrites == null) throw new NeuroException("Connecting to" +
				" neuron which has no dendrites..");
		
		if (neuron.dendrites.components.size() == 0) return false;
		
		//connect to open dendrite if one exists
		//if (connectOpenDendrite((SimpleNeuron)neuron, weight)) return true;
		//SimpleNeuron n = (SimpleNeuron)neuron;
		if (neuron.connectOpenDendrite(this, synapse)) return true;
		
		//otherwise connect to first dendrite
		return neuron.connectDendrite(this, synapse, 0);
	}
	
	public boolean connectDendrite(ConnectableNeuron neuron, AbstractSynapse synapse, int index) throws NeuroException{
		if (index >= dendrites.components.size()) return false;
		try{
			AbstractDendrite dendrite = dendrites.components.get(index);
			//SimpleSynapse synapse = new SimpleSynapse(weight, dendrite.getRestingV());
			dendrite.addSynapse(synapse);
			((AbstractAxon)neuron.getAxon()).connectSynapse(synapse);
			return true;
		}catch (NeuroException e){
			e.printStackTrace();
			return false;
			}
		
	}
	
	/*******************************
	 * Connect the passed SimpleNeuron to this neuron's first open (synapse-free)
	 * dendrite. If no dendrites exist, no dendrites are open, or no axon exists in the
	 * connecting neuron, returns false. Otherwise connects the neurons and returns true.
	 * Sets the intermediating synapse's weight to <weight> and its restingV to that of 
	 * the dendrite.
	 * @param neuron
	 * @return
	 */
	public boolean connectOpenDendrite(ConnectableNeuron neuron, AbstractSynapse synapse) throws NeuroException{
		AbstractDendrites dendrites = (AbstractDendrites)getDendrites();
		AbstractAxon axon = (AbstractAxon)neuron.getAxon();
		if (dendrites == null || axon == null) return false;
		//make synapse and connect axon to open dendrite
		for (int i = 0; i < dendrites.components.size(); i++)
			if (dendrites.components.get(i).getSynapseCount() == 0){
				//this dendrite is free
				try{
					AbstractDendrite dendrite = dendrites.components.get(i);
					//SimpleSynapse synapse = new SimpleSynapse(weight, dendrite.getRestingV());
					dendrite.addSynapse(synapse);
					axon.connectSynapse(synapse);
					return true;
				}catch (NeuroException e){
					e.printStackTrace();
					return false;
					}
				}
		return false;
	}
	
}