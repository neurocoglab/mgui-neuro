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


package mgui.neuro.components.cortical.functions;

import mgui.models.dynamic.functions.ODEFunction;

public abstract class CorticalFunction extends ODEFunction {

	double input; //, output;
	
	public void setInput(double input){
		this.input = input;
	}
	
	//public abstract void setOutputState();
	
	public abstract double getOutput();

	public void getRate(double[] state, double[] rate){
		double[] rates = evaluate(state);
		for (int i = 0; i < Math.max(rate.length, rates.length); i++)
			rate[i] = rates[i];
	}
	
}