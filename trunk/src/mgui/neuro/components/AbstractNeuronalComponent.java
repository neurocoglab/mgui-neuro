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

import mgui.neuro.exceptions.NeuroException;

/***********************************
 * Abstract class for all neuronal components (i.e., components of instances of <code>Neuron</code>)
 * to inherit. Implements methods to provide all such components to a reference to their
 * parent neuron.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public abstract class AbstractNeuronalComponent extends AbstractNeuroComponent implements NeuronalComponent {

	public AbstractNeuron parent;
	
	public Neuron getParent(){
		return parent;
	}
	
	public void setParent(Neuron n) throws NeuroException{
		if (!(n instanceof AbstractNeuron)) 
			throw new NeuroException("AbstractNeuronalComponent" +
				" must have parent which is instance of AbstractNeuron");
		parent = (AbstractNeuron)n;
	}
	
	public abstract Object clone();

}