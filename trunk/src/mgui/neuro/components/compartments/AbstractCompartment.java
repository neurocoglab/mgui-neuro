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

import java.lang.reflect.Constructor;

import mgui.interfaces.attributes.Attribute;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.models.dynamic.DynamicModelException;
import mgui.models.dynamic.functions.Function;
import mgui.models.dynamic.ode.DynamicModelODEComponent;
import mgui.neuro.components.AbstractNeuroComponent;
import mgui.neuro.components.AbstractNeuronalComponent;
import mgui.neuro.exceptions.NeuroException;
import mgui.neuro.exceptions.ODEException;

import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ODESolver;
import org.opensourcephysics.numerics.RK4;


/***************************************
 * Basic compartment for compartmental model components.  
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public abstract class AbstractCompartment extends AbstractNeuronalComponent
										implements DynamicModelODEComponent {

	public double potential;
	public double[] state;
	public ODESolver solver;
	
	//set state variables in subclass
	//override should call super.init()
	protected void init(){
		super.init();
		attributes.add(new Attribute("ODESolver", "RK4"));
		try{
			resetSolver();
		}catch (ODEException e){
			e.printStackTrace();
			}
	}
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException{
		
		return true;
	}
	
	public void timeElapsed(double time) {
		clock += time;
		solver.setStepSize(time);
		solver.step();
		updateFromClock();
	}
	
	public boolean updateFromClock(){
		return false;
	}
	
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//TODO set solver type from attributes
	public void resetSolver() throws ODEException{
		String s = (String)attributes.getValue("ODESolver");
		//default
		
		//set from attributes
		if (s.equals("RK4"))
			solver = new RK4(this);
		
		if (solver == null) throw new ODEException("No solver for string: '" + s + "'");
		
	}
	
	/**************************
	 * Reset the compartment's temporary variables
	 *
	 */
	public abstract void reset();
	
	public ODESolver getODESolver(){
		return solver;
	}
	
	//override this
	public abstract void getRate(double[] state, double[] rate);
	
	public double[] getState() {
		return state;
	}

}