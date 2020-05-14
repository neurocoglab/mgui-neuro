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


package mgui.neuro.components.cortical.functions;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.numbers.MguiDouble;

/****************************************
 * Regional cortical model based upon Morris and Lecar (1981), and Breakspear et al.
 * (2003). Defines a set of three ODEs representing neuronal population activity.
 * 
 * TODO: extend to allow coupling between cortical columns
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class MorrisLecarFunction extends CorticalFunction {
	
	double output;
	
	public MorrisLecarFunction(){
		init();
	}
	
	protected void init(){
		super.init();
		
		attributes.add(new Attribute<MguiDouble>("g_Ca[S/cm2?]", new MguiDouble(1)));		//max Ca conductance
		attributes.add(new Attribute<MguiDouble>("g_Na[S/cm2?]", new MguiDouble(6.7)));	//max Na conductance
		attributes.add(new Attribute<MguiDouble>("g_K[S/cm2?]", new MguiDouble(2)));		//max K conductance
		attributes.add(new Attribute<MguiDouble>("g_L[S/cm2?]", new MguiDouble(0.5)));	//max leak conductance
		attributes.add(new Attribute<MguiDouble>("r_NMDA", new MguiDouble(0.25)));		//ratio NMDA:AMPA
		attributes.add(new Attribute<MguiDouble>("V_Ca[mV]", new MguiDouble(1)));			//Ca Nernst potential
		attributes.add(new Attribute<MguiDouble>("V_Na[mV]", new MguiDouble(0.53)));		//Na Nernst potential
		attributes.add(new Attribute<MguiDouble>("V_K[mV]", new MguiDouble(-0.7)));		//K Nernst potential
		attributes.add(new Attribute<MguiDouble>("V_L[mV]", new MguiDouble(-0.5)));		//Leak potential
		attributes.add(new Attribute<MguiDouble>("V_T_V[mV]", new MguiDouble(0)));		//um..? inhibitory
		attributes.add(new Attribute<MguiDouble>("V_T_Z[mV]", new MguiDouble(0)));		//? excitatory
		attributes.add(new Attribute<MguiDouble>("V_T_Ca[mV]", new MguiDouble(-0.01)));	//?Ca
		attributes.add(new Attribute<MguiDouble>("V_T_Na[mV]", new MguiDouble(1)));		//?Na
		attributes.add(new Attribute<MguiDouble>("V_T_K[mV]", new MguiDouble(0)));		//?K
		attributes.add(new Attribute<MguiDouble>("Q_V_max[hz]", new MguiDouble(1)));		//max excitatory firing rate
		attributes.add(new Attribute<MguiDouble>("Q_Z_max[hz]", new MguiDouble(1)));		//max inhibitory firing rate
		attributes.add(new Attribute<MguiDouble>("var_Ca[%]", new MguiDouble(0.15)));		//Ca open channel variance
		attributes.add(new Attribute<MguiDouble>("var_Na[%]", new MguiDouble(0.15)));		//Na open channel variance
		attributes.add(new Attribute<MguiDouble>("var_K[%]", new MguiDouble(0.3)));		//K open channel variance
		attributes.add(new Attribute<MguiDouble>("var_V[mV]", new MguiDouble(0.65)));		//excit mem pot'l variance
		attributes.add(new Attribute<MguiDouble>("var_Z[mV]", new MguiDouble(0.65)));		//inhib mem pot'l variance
		attributes.add(new Attribute<MguiDouble>("a_ee[?]", new MguiDouble(0.36)));		//e-e synaptic strength
		attributes.add(new Attribute<MguiDouble>("a_ei[?]", new MguiDouble(0.1)));		//e-i synaptic strength
		attributes.add(new Attribute<MguiDouble>("a_ie[?]", new MguiDouble(2)));			//i-e synaptic strength
		attributes.add(new Attribute<MguiDouble>("a_ne[?]", new MguiDouble(1)));			//n?-e synaptic strength
		attributes.add(new Attribute<MguiDouble>("a_ni[?]", new MguiDouble(0.4)));		//n?-i synaptic strength
		attributes.add(new Attribute<MguiDouble>("I_delta[?]", new MguiDouble(0.3)));		//?
		attributes.add(new Attribute<MguiDouble>("phi[?]", new MguiDouble(0.7)));			//temperature scaling factor
		attributes.add(new Attribute<MguiDouble>("tau[?]", new MguiDouble(1)));			//relaxation time constant
		
		reset();
		
	}
	
	public void reset(){
		state = new double[3];
	}
	
	//getters & setters
	
	public double getG_Ca(){
		return ((MguiDouble)attributes.getValue("g_Ca[S/cm2?]")).getValue();
	}

	public void setG_Ca(double v){
		attributes.setValue("g_Ca[S/cm2?]", new MguiDouble(v));
	}
	
	public double getG_Na(){
		return ((MguiDouble)attributes.getValue("g_Na[S/cm2?]")).getValue();
	}

	public void setG_Na(double v){
		attributes.setValue("g_Na[S/cm2?]", new MguiDouble(v));
	}
	
	public double getG_K(){
		return ((MguiDouble)attributes.getValue("g_K[S/cm2?]")).getValue();
	}

	public void setG_K(double v){
		attributes.setValue("g_K[S/cm2?]", new MguiDouble(v));
	}
	
	public double getG_L(){
		return ((MguiDouble)attributes.getValue("g_L[S/cm2?]")).getValue();
	}

	public void setG_L(double v){
		attributes.setValue("g_L[S/cm2?]", new MguiDouble(v));
	}
	
	public double getR_NMDA(){
		return ((MguiDouble)attributes.getValue("r_NMDA")).getValue();
	}

	public void setR_NMDA(double v){
		attributes.setValue("r_NMDA", new MguiDouble(v));
	}
	
	public double getV_Ca(){
		return ((MguiDouble)attributes.getValue("V_Ca[mV]")).getValue();
	}

	public void setV_Ca(double v){
		attributes.setValue("V_Ca[mV]", new MguiDouble(v));
	}
	
	public double getV_Na(){
		return ((MguiDouble)attributes.getValue("V_Na[mV]")).getValue();
	}

	public void setV_Na(double v){
		attributes.setValue("V_Na[mV]", new MguiDouble(v));
	}
	
	public double getV_T(){
		return ((MguiDouble)attributes.getValue("V_Na[mV]")).getValue();
	}

	public void setV_T(double v){
		attributes.setValue("V_T[mV]", new MguiDouble(v));
	}
	
	public double getV_K(){
		return ((MguiDouble)attributes.getValue("V_K[mV]")).getValue();
	}

	public void setV_K(double v){
		attributes.setValue("V_K[mV]", new MguiDouble(v));
	}
	
	public double getV_L(){
		return ((MguiDouble)attributes.getValue("V_L[mV]")).getValue();
	}

	public void setV_L(double v){
		attributes.setValue("V_L[mV]", new MguiDouble(v));
	}
	
	public double getV_T_V(){
		return ((MguiDouble)attributes.getValue("V_T_V[mV]")).getValue();
	}

	public void setV_T_V(double v){
		attributes.setValue("V_T_V[mV]", new MguiDouble(v));
	}
	
	public double getV_T_Z(){
		return ((MguiDouble)attributes.getValue("V_T_Z[mV]")).getValue();
	}

	public void setV_T_Z(double v){
		attributes.setValue("V_T_Z[mV]", new MguiDouble(v));
	}
	
	public double getV_T_Ca(){
		return ((MguiDouble)attributes.getValue("V_T_Ca[mV]")).getValue();
	}

	public void setV_T_Ca(double v){
		attributes.setValue("V_T_Ca[mV]", new MguiDouble(v));
	}
	
	public double getV_T_Na(){
		return ((MguiDouble)attributes.getValue("V_T_Na[mV]")).getValue();
	}

	public void setV_T_Na(double v){
		attributes.setValue("V_T_Na[mV]", new MguiDouble(v));
	}
	
	public double getV_T_K(){
		return ((MguiDouble)attributes.getValue("V_T_K[mV]")).getValue();
	}

	public void setV_T_K(double v){
		attributes.setValue("V_T_K[mV]", new MguiDouble(v));
	}
	
	public double getQ_V_max(){
		return ((MguiDouble)attributes.getValue("Q_V_max[hz]")).getValue();
	}

	public void setQ_V_max(double v){
		attributes.setValue("Q_V_max[hz]", new MguiDouble(v));
	}
	
	public double getQ_Z_max(){
		return ((MguiDouble)attributes.getValue("Q_Z_max[hz]")).getValue();
	}

	public void setQ_Z_max(double v){
		attributes.setValue("Q_Z_max[hz]", new MguiDouble(v));
	}
	
	public double getVar_Ca(){
		return ((MguiDouble)attributes.getValue("var_Ca[%]")).getValue();
	}

	public void setVar_Ca(double v){
		attributes.setValue("var_Ca[%]", new MguiDouble(v));
	}
	
	public double getVar_Na(){
		return ((MguiDouble)attributes.getValue("var_Na[%]")).getValue();
	}

	public void setVar_Na(double v){
		attributes.setValue("var_Na[%]", new MguiDouble(v));
	}
	
	public double getVar_K(){
		return ((MguiDouble)attributes.getValue("var_K[%]")).getValue();
	}

	public void setVar_K(double v){
		attributes.setValue("var_K[%]", new MguiDouble(v));
	}
	
	public double getVar_V(){
		return ((MguiDouble)attributes.getValue("var_V[mV]")).getValue();
	}

	public void setVar_V(double v){
		attributes.setValue("var_V[mV]", new MguiDouble(v));
	}
	
	public double getVar_Z(){
		return ((MguiDouble)attributes.getValue("var_Z[mV]")).getValue();
	}

	public void setVar_Z(double v){
		attributes.setValue("var_Z[mV]", new MguiDouble(v));
	}
	
	public double getA_ee(){
		return ((MguiDouble)attributes.getValue("a_ee[?]")).getValue();
	}

	public void setA_ee(double v){
		attributes.setValue("a_ee[?]", new MguiDouble(v));
	}
	
	public double getA_ei(){
		return ((MguiDouble)attributes.getValue("a_ei[?]")).getValue();
	}

	public void setA_ei(double v){
		attributes.setValue("a_ei[?]", new MguiDouble(v));
	}
	
	public double getA_ie(){
		return ((MguiDouble)attributes.getValue("a_ie[?]")).getValue();
	}

	public void setA_ie(double v){
		attributes.setValue("a_ie[?]", new MguiDouble(v));
	}
	
	public double getA_ne(){
		return ((MguiDouble)attributes.getValue("a_ne[?]")).getValue();
	}

	public void setA_ne(double v){
		attributes.setValue("a_ne[?]", new MguiDouble(v));
	}
	
	public double getA_ni(){
		return ((MguiDouble)attributes.getValue("a_ni[?]")).getValue();
	}

	public void setA_ni(double v){
		attributes.setValue("a_ni[?]", new MguiDouble(v));
	}
	
	public double getI_delta(){
		return ((MguiDouble)attributes.getValue("I_delta[?]")).getValue();
	}

	public void setI_delta(double v){
		attributes.setValue("I_delta[?]", new MguiDouble(v));
	}
	
	public double getPhi(){
		return ((MguiDouble)attributes.getValue("phi[?]")).getValue();
	}

	public void setPhi(double v){
		attributes.setValue("phi[?]", new MguiDouble(v));
	}
	
	public double getTau(){
		return ((MguiDouble)attributes.getValue("tau[?]")).getValue();
	}

	public void setTau(double v){
		attributes.setValue("tau[?]", new MguiDouble(v));
	}
	

	public double evaluate(double d) {
		// TODO Auto-generated method stub
		return 0;
	}

	/****************************
	 * Evaluate the ODEs for the given state:
	 * <ol>
	 * <li>V - mean excitatory membrane potential
	 * <li>Z - mean inhibitory membrane potential
	 * <li>W - mean number of open K channels
	 * </ol>
	 * 
	 * Returns the rates for these variables
	 */
	public double[] evaluate(double[] d) {
		
		double V = d[0];
		double Z = d[1];
		double W = d[2];
		double Q_v_in = 0;
		double Q_v_sum = 0;
		
		//set incoming firing rate
		Q_v_in = input;
		
		double rates[] = new double[3];
		
		//VT, ZT?
		double V_T = 0;
		double Z_T = 0;
		
		//b???
		double b = 0.1;
		
		//Qv
		double Q_v = 0.5 * getQ_V_max() * (1 + Math.tanh((V - V_T) / getVar_V()));
		double Q_z = 0.5 * getQ_Z_max() * (1 + Math.tanh((Z - Z_T) / getVar_Z()));
		
		//input and intrinsic excitation act as competitive agonists
		Q_v_in = Math.min(getQ_V_max(), Q_v_in);
		double c = Q_v_in / getQ_V_max();
		
		Q_v_sum = (1 - c) * Q_v +  c * Q_v_in;
		
		//adjust sum to assure it does not exceed max
		//Q_v = Math.min(getQ_V_max(), Q_v_sum);
		
		//if (Q_v + Q_v_in > getQ_V_max()){
		//	double x = getQ_V_max() / (Q_v + Q_v_in);
		//	Q_v *= x;
		//	Q_v_in *= x;
		//	}
		
		//m_ion
		//double num = 1 + Math.tanh(V - V_T / getVar_Ca());
		double m_Ca = 0.5 * (1 + Math.tanh(V - V_T / getVar_Ca()));
		double m_Na = 0.5 * (1 + Math.tanh(V - V_T / getVar_Na()));
		double m_K = 0.5 * (1 + Math.tanh(V - V_T / getVar_K()));
		
		//V'
		rates[0] = -(getG_Ca() + getR_NMDA() * getA_ee() * Q_v_sum) * m_Ca * (V - getV_Ca()) -	//Ca current 
				    (getG_Na() * m_Na + getA_ee() * Q_v_sum) * (V - getV_Na()) -				//Na current
				    getG_K() * W * (V - getV_K()) -												//K current
				    getG_L() * (V - getV_L()) +
				    getA_ie() * Z * Q_z +
				    getA_ne() * getI_delta();
		
		//Z'
		rates[1] = b * (getA_ni() * getI_delta() + getA_ei() * V * Q_v);
				    
		//W'
		rates[2] = getPhi() * (m_K - W) / getTau();
		
		//output is new firing rate
		//output = 0.5 * getQ_V_max() * (1 + Math.tanh((rates[0] + V - V_T) / getVar_V()));
		output = state[0];
		
		if (Double.isNaN(output) || Double.isNaN(state[0]) || Double.isNaN(state[1]) || Double.isNaN(state[2])){
			System.out.println("MorrisLecarFunction: NaN encountered for state [" + state[0] + ", " + state[1] + ", " + state[2] + "] and output " + output);
			}
		
		return rates;
	}
	
	public double getOutput(){
		return output;
	}
	
	/*
	public void getRate(double[] state, double[] rate){
		double[] rates = evaluate(state);
		for (int i = 0; i < Math.max(rate.length, rates.length); i++)
			rate[i] = rates[i];
	}
	*/
	
	public double[] getState() {
		return state;
	}

	public Object clone(){
		MorrisLecarFunction function = new MorrisLecarFunction();
		function.attributes = (AttributeList)this.attributes.clone();
		return function;
	}
	
	public String toString(){
		return "Morris-Lecar Function";
	}
	
}