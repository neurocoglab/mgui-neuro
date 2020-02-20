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


package mgui.neuro.components.compartments;

public class SimpleCompartment extends AbstractCompartment {

	//***PARAMETERS***
	public double Rm;		//membrane resistance
	public double Cm;		//membrane capacitance
	public double Ra;		//axial (cytosol) resistance
	public double diameter;	//compartment diameter
	public double length;	//compartment length
	
	//***VARIABLES***
	public double Vm;		//membrane voltage
	public double Iinj;		//injected current?
	
	//***TEMPORARY***
	public double Vadj;		//sum of voltage of adjacent compartments
	public int adj;			//number of neighbours
	
	public SimpleCompartment(){
		//state variables are:
		//1. membrane voltage in this compartment V[i]
		//2. time (trivial)
		
		init();
		
	}
	
	public SimpleCompartment(double Rm, double Ra, double Cm, 
			 				 double length, double diameter){
		
		this.Rm = Rm;
		this.Ra = Ra;
		this.Cm = Cm;
		this.length = length;
		this.diameter = diameter;
		
		init();
	}
	
	protected void init(){
		state = new double[2];
	}
	
	public void getRate(double[] state, double[] rate) {
		// rate equations
		rate[0] = (Vadj - (adj * state[0])) / Ra;	//current from adj comps
		rate[0] -= (state[0] / Rm) + Iinj;			//passive membrane current + inj current
		rate[0] /= Cm;								//membrane capacitance
		rate[1] = 1;								//trivial time derivative
	}
	
	public void reset(){
		Vadj = 0;
	}

}