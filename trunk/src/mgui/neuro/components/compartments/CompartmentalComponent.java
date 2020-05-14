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

import java.util.ArrayList;

import mgui.interfaces.attributes.Attribute;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.neuro.components.AbstractNeuroComponent;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiInteger;


/***************************************
 * Base abstract class for all components using a compartmental model.
 * 
 * @author Andrew Reid
 *
 */

public abstract class CompartmentalComponent extends AbstractNeuroComponent {
	
	public ArrayList<AbstractCompartment> compartments = new ArrayList<AbstractCompartment>();
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException {
		//compartment event
		if (e instanceof CompartmentEvent){
			//transmit signals between compartments on source component and this one 
			try{
				transmit(((CompartmentEvent)e).getCompartment(), compartments.get(0));
			}catch (NeuroException ex){
				ex.printStackTrace();
				return false;
				}
			return true;
			}
		
		//input event
		
		return false;
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute("N", new MguiInteger(10)));
		setN(10);
	}
	
	public void timeElapsed(double step){
		
		//solve diff eqns for all compartments
		for (int i = 0; i < getN(); i++)
			compartments.get(i).timeElapsed(step);
		
		//update chain
		try{
			for (int i = 0; i < getN(); i++)
				compartments.get(i).reset();
			for (int i = 0; i < getN() - 1; i++)
				transmit(compartments.get(i), compartments.get(i + 1));
		}catch (NeuroException e){
			e.printStackTrace();
			return;
			}
		
		//send events to neighbours
		for (int i = 0; i < connections.size(); i++)
			if (connections.get(i) instanceof CompartmentalComponent)
				((CompartmentalComponent)connections.get(i)).addEvent(
						new CompartmentEvent(compartments.get(getN() - 1)));
			
		
	}
	
	/********************
	* Transmit signals between compartments; should update appropriate state and/or temporary
	* variables.
	*/
	public abstract void transmit(AbstractCompartment c1, AbstractCompartment c2) throws NeuroException;
	
	public void setN(int n){
		attributes.setValue("N", new MguiInteger(n));
		setCompartments();	
	}
	
	/*********************************
	 * Set/reset list of compartments to size N 
	 */
	public abstract void setCompartments();
	
	public int getN(){
		return ((MguiInteger)attributes.getValue("N")).getInt();
	}
	
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

}