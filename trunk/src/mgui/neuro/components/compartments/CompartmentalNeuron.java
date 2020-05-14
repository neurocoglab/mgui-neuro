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


package mgui.neuro.components.compartments;

import mgui.neuro.components.Axon;
import mgui.neuro.components.Dendrites;
import mgui.neuro.components.Neuron;
import mgui.neuro.components.Soma;
import mgui.neuro.exceptions.NeuroException;

public class CompartmentalNeuron extends CompartmentalComponent
							  implements Neuron{

	@Override
	public void setCompartments() {
		// TODO Auto-generated method stub
		
	}

	public void connectDendrites() {
		// TODO Auto-generated method stub
		
	}

	public Axon getAxon() {
		// TODO Auto-generated method stub
		return null;
	}

	public Dendrites getDendrites() {
		// TODO Auto-generated method stub
		return null;
	}

	public Soma getSoma() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAxon(Axon a) throws NeuroException {
		// TODO Auto-generated method stub
		
	}

	public void setDendrites(Dendrites d) throws NeuroException {
		// TODO Auto-generated method stub
		
	}

	public void setSoma(Soma s) throws NeuroException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transmit(AbstractCompartment c1, AbstractCompartment c2)
			throws NeuroException {
		// TODO Auto-generated method stub

	}

}