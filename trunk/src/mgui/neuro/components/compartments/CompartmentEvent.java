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

import mgui.neuro.components.simple.SimpleEvent;

/*********************************
 * Basic event for compartments of a compartmental model. Event holds a reference to
 * the source compartment, and updating of target will also update source (transmission
 * between compartments is bidirectional)
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class CompartmentEvent extends SimpleEvent {

	public AbstractCompartment compartment;
	
	public CompartmentEvent(AbstractCompartment c){
		compartment = c;
	}
	
	public AbstractCompartment getCompartment(){
		return compartment;
	}
	
}