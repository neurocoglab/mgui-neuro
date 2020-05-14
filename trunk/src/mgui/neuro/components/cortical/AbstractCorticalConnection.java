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


package mgui.neuro.components.cortical;

import java.util.ArrayList;

import mgui.interfaces.attributes.Attribute;
import mgui.neuro.components.AbstractNeuroComponent;
import mgui.numbers.MguiDouble;

/******************************************************
 * Abstract class serving as a base for all cortical connections.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class AbstractCorticalConnection extends AbstractNeuroComponent {

	public void init(){
		super.init();
		attributes.add(new Attribute<MguiDouble>("Weight", new MguiDouble(0)));
	}
	
	public double getWeight(){
		return (((MguiDouble)attributes.getValue("Weight"))).getValue();
	}
	
	public void setWeight(double weight){
		attributes.setValue("Weight", new MguiDouble(weight));
	}
	
	public abstract void connectTo(AbstractCorticalRegion target);
	
	public ArrayList<AbstractCorticalRegion> getTargets(){
		
		ArrayList<AbstractCorticalRegion> targets = 
			new ArrayList<AbstractCorticalRegion>(connections.size());
		
		for (int i = 0; i < connections.size(); i++)
			if (connections.get(i) instanceof AbstractCorticalRegion)
				targets.add((AbstractCorticalRegion)connections.get(i));
		
		return targets;
	}
	
	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

}