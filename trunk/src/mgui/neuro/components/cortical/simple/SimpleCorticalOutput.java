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

import mgui.interfaces.attributes.Attribute;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.models.dynamic.DynamicModelOutputEvent;
import mgui.neuro.components.cortical.AbstractCorticalOutput;
import mgui.neuro.components.cortical.AbstractCorticalRegion;
import mgui.neuro.components.simple.SimpleMotorNeuronEvent;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiInteger;

public class SimpleCorticalOutput extends AbstractCorticalOutput {

	public DynamicModelEnvironment environment;
	public double output;
	
	public SimpleCorticalOutput(){
		init();
	}
	
	public SimpleCorticalOutput(double delay){
		init();
		setDelay(delay);
	}
	
	public SimpleCorticalOutput(String name, double delay){
		init();
		setName(name);
		setDelay(delay);
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute("Index", new MguiInteger(0)));
	}
	
	public double getOutput(){
		return output;
	}
	
	public void reset(){
		super.reset();
		output = 0;
	}
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException {
		
		if (e instanceof SimpleCorticalRegionEvent){
			SimpleCorticalRegionEvent ev = (SimpleCorticalRegionEvent)e;
			
			//set output
			output = ev.getFiringRate();
			return true;
		}
		
		return false;
	} 
	
	protected boolean updateComponent(){
		fireOutputEvent();
		return true;
	}
	
	protected void fireOutputEvent(){
		fireOutputEvent(new SimpleCorticalOutputEvent(this));
	}
	
	public void setEnvironment(DynamicModelEnvironment e){
		if (environment != null)
			environment.removeObservableName(getIndex());
		environment = e;
		e.setObservableName(getIndex(), getName());
	}
	
	public DynamicModelEnvironment getEnvironment(){
		return environment;
	}
	
	public void setName(String name){
		if (environment != null)
			environment.removeObservableName(name);
		super.setName(name);
		if (environment != null)
			environment.setObservableName(getIndex(), name);
	}
	
	public void setIndex(int i){
		if (environment != null)
			environment.removeObservableName(i);
		attributes.setValue("Index", new MguiInteger(i));
		if (environment != null)
			environment.setObservableName(i, getName());
	}
	
	public int getIndex(){
		return ((MguiInteger)attributes.getValue("Index")).getInt();
	}
	
	protected void fireOutputEvent(DynamicModelOutputEvent e){
		environment.handleOutputEvent(e);
	}

	public String toString(){
		return "Cortical Output [" + getID() + "]";
	}

}