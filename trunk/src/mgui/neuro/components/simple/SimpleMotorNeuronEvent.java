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

import mgui.models.dynamic.DynamicModelEvent;
import mgui.models.dynamic.DynamicModelOutputEvent;
import mgui.neuro.exceptions.NeuroException;

public class SimpleMotorNeuronEvent extends SimpleEvent implements DynamicModelOutputEvent {

	public double output;
	public int index;
	
	public SimpleMotorNeuronEvent(SimpleMotorNeuron n){
		output = n.getOutput();
		index = n.getIndex();
	}
	
	public SimpleMotorNeuronEvent(int index, double output){
		this.output = output;
		this.index = index;
	}
	
	public double getOutput() {
		return output;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setOutput(double output) {
		this.output = output;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

}