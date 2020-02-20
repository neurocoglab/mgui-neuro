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


package mgui.neuro.graphs;

import java.util.Comparator;

import mgui.interfaces.graphs.DefaultGraphNode;
import mgui.neuro.components.AbstractNeuron;
import mgui.neuro.components.types.WeightedSynapse;

/****************************************
 * Extension of <code>DirectedSparseVertex</code> to specifically represent nodes in neural networks, i.e.,
 * containing references to instances of <code>Neuron</code>.
 *  
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class NeuralNetNode extends DefaultGraphNode {

	protected AbstractNeuron unit;
	
	public NeuralNetNode(AbstractNeuron n){
		unit = n;
	}
	
	public AbstractNeuron getUnit(){
		return unit;
	}
	
	/******************************
	 * <p>Returns the weight of the connection of this node with the target node, if it
	 * exists. Returns 0 if:</p>
	 * <ol>
	 * <li>this node's unit is null</li>
	 * <li>this node has no such target</li>
	 * <li>the target synapse is not an instance of WeightedSynapse</li>
	 * </ol>
	 * @param target
	 * @return weight of the connection to target
	 */
	public double getWeight(NeuralNetNode target){
		if (unit == null) return 0;
		//search synapses for target unit
		for (int i = 0; i < unit.axon.synapses.size(); i++)
			if (unit.axon.synapses.get(i).getParent().equals(target.unit))
				if (unit.axon.synapses.get(i) instanceof WeightedSynapse)
					return ((WeightedSynapse)unit.axon.synapses.get(i)).getWeight();
				else
					return 0;
		return 0;
	}

}