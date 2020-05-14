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


package mgui.neuro.networks;

import java.util.ArrayList;
import java.util.Collections;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.neuro.InterfaceNeuroComponentListener;
import mgui.interfaces.neuro.NeuroComponentEvent;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelException;
import mgui.models.environments.SimpleEnvironment;
import mgui.models.networks.AbstractNetwork;
import mgui.models.networks.components.NetworkComponentEvent;
import mgui.neuro.components.cortical.AbstractCorticalRegion;
import mgui.neuro.components.cortical.simple.SimpleCorticalInput;
import mgui.neuro.graphs.CorticalNetworkGraph;


public class CorticalNetwork extends AbstractNetwork implements InterfaceNeuroComponentListener {

	public ArrayList<AbstractCorticalRegion> regions = new ArrayList<AbstractCorticalRegion>();
	
	public CorticalNetwork(){
		this("No-name");
	}
	
	public CorticalNetwork(String name){
		init();
		try{
			setEnvironment(new SimpleEnvironment());
			setName(name);
		}catch (DynamicModelException e){
			e.printStackTrace();
			}
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute("Name", "no-name"));
	}
	
	public void setEnvironment(DynamicModelEnvironment e) throws DynamicModelException{
		super.setEnvironment(e);
		for (int i = 0; i < regions.size(); i++)
			regions.get(i).getOutput().setEnvironment(e);
		e.setObservableSize(regions.size());
	}
	
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
	}

	public InterfaceAbstractGraph getGraph() {
		if (regions == null) return null;
		
		CorticalNetworkGraph graph = new CorticalNetworkGraph(this);
		return graph;
	}

	public boolean addInput(String r){
		AbstractCorticalRegion region = getRegion(r);
		if (region == null) return false;
		SimpleCorticalInput input = new SimpleCorticalInput();
		input.connectTo(region);
		this.addSensor(input);
		return true;
	}
	
	public boolean addInput(AbstractCorticalRegion region){
		SimpleCorticalInput input = new SimpleCorticalInput();
		if (!hasRegion(region)) return false;
		input.connectTo(region);
		this.addSensor(input);
		return true;
	}
	
	public boolean addRegion(AbstractCorticalRegion region){
		return addRegion(region, true);
	}
	
	public boolean addRegion(AbstractCorticalRegion region, boolean fire){
		//add sorted
		int index = Collections.binarySearch(regions, region, AbstractCorticalRegion.getComparator());
		if (index >= 0) return false;
		
		region.setID(idFactory.getID());
		index = -index - 1;
		
		regions.add(index, region);
		if (environment != null){
			region.getOutput().setIndex(index);
			region.getOutput().setEnvironment(environment);
			environment.setObservableSize(regions.size());
			}
	
		if (fire) fireListeners();
		return true;
	}
	
	public <T extends AbstractCorticalRegion> boolean addRegions(ArrayList<T> regions, boolean fire){
		boolean ok = true;
		
		for (int i = 0; i < regions.size(); i++)
			ok &= addRegion(regions.get(i), false);
		
		if (fire) fireListeners();
		return ok;
	}
	
	public boolean hasRegion(AbstractCorticalRegion region){
		int index = Collections.binarySearch(regions, region);
		return index >= 0;
	}
	
	public AbstractCorticalRegion getRegion(String str){
		int index = Collections.binarySearch(regions, str, AbstractCorticalRegion.getStrComparator());
		if (index < 0) return null;
		return regions.get(index);
	}
	
	public boolean connect(String region1, String region2, double weight){
		return connect(getRegion(region1), getRegion(region2), weight);
	}
	
	public boolean connect(int region1, int region2, double weight){
		return connect(regions.get(region1), regions.get(region2), weight);
	}
	
	protected boolean connect(AbstractCorticalRegion r1, AbstractCorticalRegion r2, double weight){
		if (r1 == null || r2 == null) return false;
		r1.connectTo(r2, weight);
		return true;
	}
	
	protected void sort(){
		Collections.sort(regions, AbstractCorticalRegion.getComparator());
	}
	
	public void connectRegions(String region1, String region2){
		
	}
	
	public void componentUpdated(NetworkComponentEvent e) {
		fireListeners();
	}

	public ArrayList<DynamicModelComponent> getComponents() {
		ArrayList<DynamicModelComponent> components = new ArrayList<DynamicModelComponent>();
		
		components.addAll(regions);
		
		for (int i = 0; i < regions.size(); i++){
			ArrayList<DynamicModelComponent> subs = regions.get(i).getSubComponents();
			if (subs != null)
				components.addAll(subs);
			}

		return components;
	}
	
	public Object clone() {
		CorticalNetwork net = new CorticalNetwork();
		SimpleEnvironment env = (SimpleEnvironment)((SimpleEnvironment)net.getEnvironment()).clone();
		
		try{
			net.setEnvironment(env);
			
			//add all components
			
			
			
			
		
		}catch (Exception e){
			e.printStackTrace();
			}
		
		return null;
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
		if (getEnvironment() != null)
			treeNode.add(((SimpleEnvironment)getEnvironment()).issueTreeNode());
		
		InterfaceTreeNode unitNode = new InterfaceTreeNode("Regions");
		for (int i = 0; i < regions.size(); i++){
			//regions.get(i).setTreeNode();
			unitNode.add(regions.get(i).issueTreeNode());
			}
		treeNode.add(unitNode);
	}
	
	public String toString(){
		return "Cortical Network [" + getName() + "]";
	}

}