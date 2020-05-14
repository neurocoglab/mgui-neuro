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


package mgui.neuro.components.cortical.simple;

import mgui.neuro.components.simple.SimpleEvent;

public class SimpleCorticalInputEvent extends SimpleEvent {

	public double input;
	
	public SimpleCorticalInputEvent(SimpleCorticalInput i){
		setInput(i.getInput());
	}
	
	public void setInput(double i){
		input = i;
	}
	
	public double getInput(){
		return input;
	}
	
}