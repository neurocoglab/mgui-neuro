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


package mgui.neuro.components;

import mgui.models.dynamic.DynamicModelComponent;
import mgui.neuro.exceptions.NeuroException;

public interface Neuron extends DynamicModelComponent {

	public Soma getSoma();
	public void setSoma(Soma s) throws NeuroException;
	public Dendrites getDendrites();
	public void setDendrites(Dendrites d) throws NeuroException;
	public Axon getAxon();
	public void setAxon(Axon a) throws NeuroException;
	public void connectDendrites();
	
}