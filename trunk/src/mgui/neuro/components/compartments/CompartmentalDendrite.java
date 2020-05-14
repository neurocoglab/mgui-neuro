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
import mgui.neuro.components.AbstractDendrite;
import mgui.neuro.components.AbstractNeuron;
import mgui.neuro.components.AbstractSoma;
import mgui.neuro.components.AbstractSynapse;
import mgui.neuro.components.Dendrite;
import mgui.neuro.components.Neuron;
import mgui.neuro.components.Soma;
import mgui.neuro.components.Synapse;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


/*****************************************
 * Represents a dendrite whose activity is determined by the cable model (see Rawl etc.)
 * and a compartmental discrete (numerical) approximation of its differential functions.
 * See CompartmentalComponent.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CompartmentalDendrite extends NeuronalCompartmentalComponent 
								implements Dendrite {

	public ArrayList<Synapse> synapses = new ArrayList<Synapse>();
	public CompartmentalDendrite next;
	public CompartmentalSoma soma;
	
	public CompartmentalDendrite(){
		init();
	}
	
	public CompartmentalDendrite(int n, double Rm, double Ra, double Cm, 
								 double length, double diameter){
		init();
		setN(n);
		attributes.setValue("Rm", new MguiDouble(Rm));
		attributes.setValue("Ra", new MguiDouble(Ra));
		attributes.setValue("Cm", new MguiDouble(Cm));
		attributes.setValue("Length", new MguiDouble(length));
		attributes.setValue("Diam", new MguiDouble(diameter));
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute<MguiDouble>("Rm", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("Ra", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("Cm", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("Length", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("Diam", new MguiDouble(0)));
	}	
	
	public void setCompartments(){
		if (getN() < 1) return;
		compartments = new ArrayList<AbstractCompartment>(getN());
		//TODO set parameters according to continuous properties (i.e.,
		//	   based on adjacent components)
		for (int i = 0; i < compartments.size(); i++)
			compartments.add(new SimpleCompartment(getRm(), getRa(), getCm(), getLength(), getDiam()));
		
	}
	
	public double getRm(){
		return ((MguiDouble)attributes.getValue("Rm")).getValue();
	}
	
	public double getRa(){
		return ((MguiDouble)attributes.getValue("Ra")).getValue();
	}
	
	public double getCm(){
		return ((MguiDouble)attributes.getValue("Cm")).getValue();
	}
	
	public double getLength(){
		return ((MguiDouble)attributes.getValue("Length")).getValue();
	}
	
	public double getDiam(){
		return ((MguiDouble)attributes.getValue("Diam")).getValue();
	}

	public void addSynapse(Synapse s) throws NeuroException {
		if (!(s instanceof AbstractSynapse)) throw new NeuroException("CompartmentalDendrite" +
		"can only add synapses which are instances of AbstractSynapse");
		AbstractSynapse s2 = (AbstractSynapse)s;
		synapses.add(s2);
		s2.addConnection(this);
		s2.setParent(parent);
		
	}

	public void connectTo(Dendrite d) throws NeuroException {
		if (!(d instanceof CompartmentalDendrite)) throw new NeuroException("CompartmentalDendrite" +
			"can only connect to dendrites which are instances of CompartmentalDendrite");
		CompartmentalDendrite d2 = (CompartmentalDendrite)d;
		if (soma != null)
			removeConnection(soma);
		if (next != null)
			disconnect();
		addConnection(d2);
		next = d2;
		
	}

	public void connectTo(Soma s) throws NeuroException {
		if (!(s instanceof CompartmentalSoma)) throw new NeuroException("CompartmentalDendrite" +
		"can only connect to somata which are instances of CompartmentalSoma");
		CompartmentalSoma s2 = (CompartmentalSoma)s;
		if (soma != null)
			removeConnection(soma);
		if (next != null)
			disconnect();
		addConnection(s2);
		soma = s2;
	}

	public void disconnect() {
		if (next != null) removeConnection(next);
		next = null;
	}

	public int getSynapseCount() {
		return synapses.size();
	}

	public ArrayList<Synapse> getSynapses() {
		return synapses;
	}
	
	public void setParent(Neuron n) throws NeuroException {
		try{
			super.setParent(n);
			//set parent for synapses
			for (int i = 0; i < synapses.size(); i++)
				((AbstractSynapse)synapses.get(i)).setParent(parent);
		}catch (NeuroException e){
			throw e;
			}
		
	}

	public boolean isConnectedToDendrite() {
		return next != null;
	}

	public boolean isConnectedToSoma() {
		return soma != null;
	}

	public void removeSynapse(Synapse s) {
		if (!(s instanceof AbstractSynapse)) return;
		AbstractSynapse s2 = (AbstractSynapse)s;
		if (!synapses.remove(s2)) return;
		s2.removeConnection(this);
		try{
			s2.setParent(null);
		} catch (NeuroException e){
			e.printStackTrace();
			}
	}
	
	public void transmit(AbstractCompartment c1, AbstractCompartment c2) throws NeuroException {
		if (!(c1 instanceof SimpleCompartment && c2 instanceof SimpleCompartment))
			throw new NeuroException("CompartmentalDendrite must transmit between instances of"
					+ "SimpleCompartment");
		((SimpleCompartment)c1).Vadj += ((SimpleCompartment)c2).Vm;
		((SimpleCompartment)c2).Vadj += ((SimpleCompartment)c1).Vm;
	}

}