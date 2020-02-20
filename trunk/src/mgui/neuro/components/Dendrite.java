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
import mgui.neuro.exceptions.NeuroException;


public interface Dendrite extends NeuronalComponent {

	public void addSynapse(Synapse s) throws NeuroException;
	public void removeSynapse(Synapse s);
	public void connectTo(Dendrite d) throws NeuroException;
	public void connectTo(Soma s) throws NeuroException;
	public void disconnect();
	public ArrayList<Synapse> getSynapses();
	public int getSynapseCount();
	public boolean isConnectedToDendrite();
	public boolean isConnectedToSoma();
	
}