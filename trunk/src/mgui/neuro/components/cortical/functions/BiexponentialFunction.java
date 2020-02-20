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


package mgui.neuro.components.cortical.functions;

import mgui.interfaces.attributes.Attribute;
import mgui.models.dynamic.functions.Function;
import mgui.models.dynamic.functions.SigmoidFunction;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;

/********************************
 * Simplistic bi-exponential representation of population activity in a cortical region, which responds to
 * input events. Maximum activity is modelled with a sigmoid function.
 * 
 * <p>State variables</p>
 * <ul>
 * <li>A - population activity; i.e., total PSPs, which is assumed to relate to total firing rate</li>
 * <li>J - input function; represents input events, which summate linearly (i.e., have no dependence upon
 * synaptic state, etc.)</li>
 * </ul>
 * <p>Parameters</p>
 * <ul>
 * <li>Tau - activity decay constant; describes the rate of decay for neuronal activation (PSPs)</li>
 * <li>TauS - synaptic decay constant; describes the average synaptic decay for the population</li>
 * <li>A_base - average baseline activity</li>
 * <li>A_max - maximum activity</li>
 * </ul>
 * 
 * <p>Adapted from:</p>
 * <p>Brette et al. (2007) Simulation of networks of spiking neurons: A review of tools and strategies.
 * <i>J Comput Neurosci</i></p>
 * ..and related material...
 * 
 * @author Andrew Reid
 *
 */

public class BiexponentialFunction extends CorticalFunction {
	
	double output;
	
	public BiexponentialFunction(double a_base, double a_max, double tau, double tau_s){
		init();
		setA_Base(a_base);
		setA_Max(a_max);
		setTau(tau);
		setTauS(tau_s);
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute("A_base", new MguiDouble(1)));
		attributes.add(new Attribute("A_max", new MguiDouble(1)));
		attributes.add(new Attribute("Tau", new MguiDouble(1)));
		attributes.add(new Attribute("TauS", new MguiDouble(1)));
		attributes.add(new Attribute("ApplySigmoid", new MguiBoolean(false)));
		attributes.add(new Attribute("Sigmoid", new SigmoidFunction()));
		
		reset();
	}
	
	public void reset(){
		state = new double[3];
	}
	
	public boolean getApplySigmoid(){
		return ((MguiBoolean)attributes.getValue("ApplySigmoid")).getTrue();
	}
	
	public void setApplySigmoid(boolean b){
		attributes.setValue("ApplySigmoid", new MguiBoolean(b));
	}
	
	public void setSigmoid(SigmoidFunction sf){
		attributes.setValue("Sigmoid", sf);
	}
	
	public SigmoidFunction getSigmoid(){
		return (SigmoidFunction)attributes.getValue("Sigmoid");
	}
	
	public void setA_Base(double v){
		attributes.setValue("A_base", new MguiDouble(v));
	}
	
	public void setA_Max(double v){
		attributes.setValue("A_max", new MguiDouble(v));
	}
	
	public void setTau(double v){
		attributes.setValue("Tau", new MguiDouble(v));
	}
	
	public void setTauS(double v){
		attributes.setValue("TauS", new MguiDouble(v));
	}
	
	public double getA_Base(){
		return ((MguiDouble)attributes.getValue("A_base")).getValue();
	}
	
	public double getA_Max(){
		return ((MguiDouble)attributes.getValue("A_max")).getValue();
	}
	
	public double getTau(){
		return ((MguiDouble)attributes.getValue("Tau")).getValue();
	}
	
	public double getTauS(){
		return ((MguiDouble)attributes.getValue("TauS")).getValue();
	}
	
	public double evaluate(double d) {
		
		return 0;
	}
	
	/********************
	 * Evaluates state as a biexponential function
	 */
	public double[] evaluate(double[] d) {
		//state is:
		//A - current activation level
		//J - synaptic activity
		
		//parameters are:
		//tau - activity decay constant
		//tau_s - synaptic time constant
		//a_base - baseline activity
		//a_max - maximum activity
		
		double tau = getTau();
		double tau_s = getTauS();
		double a_base = getA_Base();
		double a_max = getA_Max();
		
		double[] rates = new double[3];
		
		rates[0] = (a_base - d[0] + d[1]) / tau;	//bi-exponential
		rates[1] = -d[1] / tau_s;					//input state (synaptic activity)
		//rates[2] = d[0] * (1 - d[0]);				//sigmoid bounded on a_max
		rates[2] = 1;
		
		//update J with current input
		if (getApplySigmoid())
			input = getSigmoid().evaluate(input);
		d[1] += input; // * (tau - tau_s) / tau;
		//d[0] = a_max * d[2];
		
		output = d[0];
		
		
		
		return rates;
	}
	
	public double getOutput(){
		//output is biexponential modified with sigmoid
		//return state[0];
		return output;
	}
	
	public String toString(){
		return "Biexponential";
	}

}