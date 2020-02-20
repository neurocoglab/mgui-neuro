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


package mgui.neuro.components.cortical;

import mgui.models.dynamic.DynamicModelEnvironmentSensor;

public abstract class AbstractCorticalInput extends AbstractCorticalConnection
											implements DynamicModelEnvironmentSensor{

	public int index;
	public double input;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int i) {
		index = i;
	}

	public void setInput(double i){
		input = i;
	}
	
	public double getInput(){
		return input;
	}
	
	public void reset(){
		super.reset();
		input = 0;
	}

}