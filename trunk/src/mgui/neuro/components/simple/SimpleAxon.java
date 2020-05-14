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

import mgui.interfaces.attributes.Attribute;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.neuro.components.AbstractAxon;
import mgui.neuro.components.AbstractSynapse;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;

public class SimpleAxon extends AbstractAxon {

	//*****PARAMETERS*****
	
	
	//*****VARIABLES*****
	//public double output;
	
	public SimpleAxon(){
		init();
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute("Output", new MguiDouble(1)));
		//setDelay(0);
	}
	
	public void connectSynapse(AbstractSynapse s) throws NeuroException{
		if (!(s instanceof SimpleSynapse)) throw new NeuroException(
				"SimpleAxon can only connect to a synapse which is an instance of SimpleSynapse");
		super.connectSynapse(s);
	}
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException{
		
		if (e instanceof SimpleSomaEvent){
			setOutput(((SimpleSomaEvent)e).output);
			//for (int i = 0; i < connections.size(); i++)
			//	connections.get(i).addEvent(new SimpleAxonEvent(this));
			return true;
			}
		
		return false;
	}
	
	public void setOutput(double o){
		//((MguiDouble)attributes.getValue("Output")).value = o;
		attributes.setValue("Output", new MguiDouble(o));
	}
	
	public double getOutput(){
		return ((MguiDouble)attributes.getValue("Output")).getValue();
	}
	
	//send AP event to synapses
	protected boolean updateComponent(){
		
		//stimulate synapses
		//this.addEvent(new SimpleNeuroModelEvent(this, SimpleNeuroModelEvent.S_AXN_OUTPUT));
		for (int i = 0; i < connections.size(); i++)
			connections.get(i).addEvent(new SimpleAxonEvent(this));
		
		return true;
		
	}
	
	public Object clone(){
		SimpleAxon axon = new SimpleAxon();
		axon.setOutput(getOutput());
		return axon;
	}
}