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


package mgui.neuro.graphs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import mgui.graphs.networks.AbstractNetworkGraph;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.models.networks.AbstractNetwork;
import mgui.models.networks.NetworkEvent;
import mgui.models.networks.NetworkException;
import mgui.neuro.components.cortical.AbstractCorticalConnection;
import mgui.neuro.components.cortical.AbstractCorticalRegion;
import mgui.neuro.components.cortical.simple.SimpleCorticalConnection;
import mgui.neuro.exceptions.NeuroNetException;
import mgui.neuro.networks.CorticalNetwork;


public class CorticalNetworkGraph extends AbstractNetworkGraph {

	public CorticalNetwork network;
	
	public CorticalNetworkGraph(){
		init();
	}
	
	protected void init(){
		super.init();
		//attributes.add(new Attribute("Name", "noname"));
	}
	
	public CorticalNetworkGraph(CorticalNetwork net){
		init();
		try{
			setFromNetwork(net);
		}catch (NetworkException ex){
			ex.printStackTrace();
			return;
			}
	}
	
	public boolean setFromNetwork(AbstractNetwork net) throws NetworkException{
		
		if (!(net instanceof CorticalNetwork)) throw new NeuroNetException("CorticalNetworkGraph can only be set by" +
		" instance of CorticalNetwork.");

		//TODO if network exists, remove listener
		network = (CorticalNetwork)net;
		
		if (network.regions == null) return false;
		
		this.removeAllEdges();
		this.removeAllNodes();
		
		//create a node for each unit in this network and add to graph
		for (int i = 0; i < network.regions.size(); i++)
			addVertex(new CorticalNetworkGraphNode(network.regions.get(i)));
		
		TreeSet<AbstractGraphNode> nodes = new TreeSet<AbstractGraphNode>(getNodes());
		
		//create edges
		Iterator<AbstractGraphNode> itr = nodes.iterator();
		while (itr.hasNext()){
			CorticalNetworkGraphNode node = (CorticalNetworkGraphNode)itr.next();
			ArrayList<DynamicModelComponent> conns = node.getRegion().getConnections();
			for (int i = 0; i < conns.size(); i++){
				ArrayList<AbstractCorticalRegion> targets = 
								((AbstractCorticalConnection)conns.get(i)).getTargets();
				for (int j = 0; j < targets.size(); j++)
					addGraphEdge(new CorticalNetworkGraphConnection((SimpleCorticalConnection)conns.get(i),
															   node, 
															   getNode(targets.get(j))));
						   // node,
						   // getNode(targets.get(j)));
				}
			}
				
		return true;
	}
	
	public CorticalNetworkGraphNode getNode(AbstractCorticalRegion region){
		
		//search vertices and return if found
		TreeSet<AbstractGraphNode> nodes = new TreeSet<AbstractGraphNode>(getNodes());
		Iterator<AbstractGraphNode> itr = nodes.iterator();
		
		while (itr.hasNext()){
			CorticalNetworkGraphNode node = (CorticalNetworkGraphNode)itr.next();
			if (node.getRegion().equals(region))
				return node;
			}
		
		return null;
	}
	
	public void networkUpdated(NetworkEvent e){
		try{
			setFromNetwork(network);
		}catch (NetworkException ex){
			ex.printStackTrace();
			return;
			}
		fireGraphListeners();
	}
	
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	public String toString(){
		return "Neural Net Graph: " + getName();
	}
	
	
	
	

}