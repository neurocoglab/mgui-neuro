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

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.models.dynamic.functions.DecayFunction;
import mgui.models.dynamic.functions.Function;
import mgui.neuro.components.cortical.AbstractCorticalOutput;
import mgui.neuro.components.cortical.AbstractCorticalRegion;
import mgui.neuro.components.cortical.functions.BiexponentialFunction;
import mgui.neuro.components.cortical.functions.CorticalFunction;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;

import org.opensourcephysics.numerics.ODESolver;
import org.opensourcephysics.numerics.RK4;


public class SimpleCorticalRegion extends AbstractCorticalRegion {

	//public CorticalFunction transferFunction;
	
	//variables
	protected double input;
	protected double firingRate;
	protected double[] state;
	
	public SimpleCorticalOutput output;
	
	public SimpleCorticalRegion(){
		this("no-name", new BiexponentialFunction(0,1,2,1));
	}
	
	public SimpleCorticalRegion(CorticalFunction transferFunction){
		init();
		setTransferFunction(transferFunction);
		
	}
	
	public SimpleCorticalRegion(CorticalFunction transferFunction, Function inputFunction){
		this("no-name", transferFunction, inputFunction);
	}
	
	public SimpleCorticalRegion(String name){
		this(name, new BiexponentialFunction(0,1,2,1));	
	}
	
	public SimpleCorticalRegion(String name, CorticalFunction transferFunction){
		init();
		setName(name);
		setTransferFunction(transferFunction);
	}
	
	public SimpleCorticalRegion(String name, CorticalFunction transferFunction, Function inputFunction){
		init();
		setName(name);
		setTransferFunction(transferFunction);
		setInputFunction(inputFunction);
	}
	
	protected void init(){
		super.init();
		
		attributes.add(new Attribute("Transfer Fcn", null));
		attributes.add(new Attribute("Input Fcn", null));
		
		setOutput(new SimpleCorticalOutput());
		output = new SimpleCorticalOutput();
		
		reset();
		
	}
	
	public void reset(){
		super.reset();
		input = 0;
		firingRate = 0;
		if (output != null)
			output.reset();
		if (getTransferFunction() != null)
			getTransferFunction().reset();
	}
	
	public void setOutput(SimpleCorticalOutput o){
		output = o;
		output.setName(getName() + ".output");
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
		if (output != null) output.setName(name + ".output");
	}
	
	public AbstractCorticalOutput getOutput(){
		return output;
	}
	
	public SimpleCorticalRegion getBasicSimpleCorticalRegion(){
		SimpleCorticalRegion region = new SimpleCorticalRegion();
		return region;
	}
	
	public void setInputFunction(Function f){
		attributes.setValue("Input Fcn", f);
	}
	
	public Function getInputFunction(){
		return (Function)attributes.getValue("Input Fcn");
	}
	
	public void setTransferFunction(CorticalFunction f){
		//transferFunction = f;
		attributes.setValue("Transfer Fcn", f);
		setSolver();
		
	}
	
	public CorticalFunction getTransferFunction(){
		return (CorticalFunction)attributes.getValue("Transfer Fcn");
	}
	
	public double getInput(){
		return input;
	}
	
	public double getFiringRate(){
		return firingRate;
	}
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException {
		
		//handle connection event
		if (e instanceof SimpleCorticalConnectionEvent){
			double o = ((SimpleCorticalConnectionEvent)e).getOutput();
			//if (getInputFunction() != null)
			//	o = getInputFunction().evaluate(o);
			if (Double.isNaN(o)){
				System.out.println("SimpleCorticalRegion: NaN input encountered @" + clock + "ms.");
				}
			input += o;
			return true;
			}
		
		if (e instanceof SimpleCorticalInputEvent){
			input += ((SimpleCorticalInputEvent)e).getInput();
			return true;
			}
		
		updated = true;
		return false;
	}
	
	protected boolean updateComponent(){
		
		getTransferFunction().setInput(input);
		solver.step();
		
		setFiringRate();
		
		//notify connections
		for (int i = 0; i < connections.size(); i++)
			connections.get(i).addEvent(new SimpleCorticalRegionEvent(this));
		
		//notify outputs
		output.addEvent(new SimpleCorticalRegionEvent(this));
		
		input = 0;
		return true;
	}
	
	public void setSolver(){
		String solverStr = getSolver();
		
		try{
			Constructor[] constr =  Class.forName(solverStr).getDeclaredConstructors();
			
			for (int i = 0; i < constr.length; i++){
				Class[] classes = constr[i].getParameterTypes();
				if (classes.length == 1 && classes[i].getSimpleName().contains("ODE")){
					solver = (ODESolver)constr[i].newInstance(new Object[]{getTransferFunction()});
					return;
					}
			}
			
		} catch (Exception e){
			e.printStackTrace();
			}
		return;
	}
	
	/*
	public void getRate(double[] state, double[] rate){
		double[] rates = transferFunction.evaluate(state);
		for (int i = 0; i < Math.max(rate.length, rates.length); i++)
			rate[i] = rates[i];
		
	}
	*/
	
	protected void setFiringRate(){
		firingRate = getTransferFunction().getOutput();
	}
	
	public void connectTo(AbstractCorticalRegion target, double weight){
		
		SimpleCorticalConnection conn = new SimpleCorticalConnection();
		addConnection(conn);
		conn.connectTo(target);
		conn.setWeight(weight);
	}
	
	public ArrayList<DynamicModelComponent> getSubComponents() {
		ArrayList<DynamicModelComponent> subs = new ArrayList<DynamicModelComponent>();
		subs.addAll(connections);
		subs.add(output);
		return subs;
	}
	
	public String toString(){
		return "Cortical Region [" + id + "]";
	}
	
	@Override
	public Object clone() {
		//NOTE: clone has no connections
		
		SimpleCorticalRegion region = new SimpleCorticalRegion();
		region.setAttributes((AttributeList)attributes.clone());
		
		return region;
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.add(attributes.issueTreeNode());
		//add connections
		for (int i = 0; i < connections.size(); i++)
			treeNode.add(((SimpleCorticalConnection)connections.get(i)).issueTreeNode());
	}

}