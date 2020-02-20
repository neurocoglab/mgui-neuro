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


package mgui.neuro.components.cortical.simple;

import mgui.models.dynamic.DynamicModelEnvironmentEvent;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.neuro.components.cortical.AbstractCorticalInput;
import mgui.neuro.components.cortical.AbstractCorticalRegion;
import mgui.neuro.exceptions.NeuroException;

public class SimpleCorticalInput extends AbstractCorticalInput {

	public SimpleCorticalInput(){
		init();
	}

	public void connectTo(AbstractCorticalRegion target) {
		addConnection(target);
	}

	public boolean stimulate(DynamicModelEnvironmentEvent e) {
		setInput(e.getEnvironment().getInputState(getIndex()));
		SimpleCorticalInputEvent ev = new SimpleCorticalInputEvent(this);
		for (int i = 0; i < connections.size(); i++)
			connections.get(i).addEvent(ev);
		return true;
	}

}