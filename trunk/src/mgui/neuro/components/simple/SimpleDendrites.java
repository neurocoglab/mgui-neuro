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


package mgui.neuro.components.simple;

import mgui.neuro.components.*;
import mgui.neuro.exceptions.NeuroException;

/********************************
 * Basic input unit for, e.g., <code>SimpleNeuron</code>. Contains a set of <code>SimpleDendrite<code>s.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class SimpleDendrites extends AbstractDendrites {

	public SimpleDendrites(){
		init();
	}
	
	public void addDendrite(Dendrite d) throws NeuroException{
		if (!(d instanceof SimpleDendrite)) throw new NeuroException(
				"Can only add dendrites which are instances of SimpleDendrite to SimpleDendrites");
		super.addDendrite(d);
	}
	
	public void addDendrites(Dendrite d, Dendrite d2) throws NeuroException{
		if (!(d instanceof SimpleDendrite && d2 instanceof SimpleDendrite)) throw new NeuroException(
		"Can only add dendrites which are instances of SimpleDendrite to SimpleDendrites");
		super.addDendrite(d, d2);
	}
	
	public Object clone(){
		SimpleDendrites dendrites = new SimpleDendrites();
		int[] conns = new int[components.size()];
		
		//set components
		for (int i = 0; i < components.size(); i++){
			conns[i] = -1;
			if (components.get(i).isConnectedToDendrite())
				conns[i] = components.indexOf(components.get(i).next);
			dendrites.components.add((SimpleDendrite)components.get(i).clone());
			}
		
		//set connections
		try{
			for (int i = 0; i < components.size(); i++)
				if (components.get(i).isConnectedToDendrite())
					dendrites.components.get(i).connectTo(dendrites.components.get(conns[i]));
		}catch (NeuroException e){
			e.printStackTrace();
			}
		return dendrites;
	}
	
}