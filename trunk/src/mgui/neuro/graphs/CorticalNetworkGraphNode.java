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


package mgui.neuro.graphs;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import mgui.graphs.networks.AbstractNetworkGraphNode;
import mgui.interfaces.ProgressUpdater;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.neuro.components.cortical.AbstractCorticalConnection;
import mgui.neuro.components.cortical.AbstractCorticalRegion;


public class CorticalNetworkGraphNode extends AbstractNetworkGraphNode {

	public AbstractCorticalRegion region;
	
	public CorticalNetworkGraphNode(AbstractCorticalRegion region){
		this.region = region;
		this.setLocation(region.getLocation());
		region.addListener(this);
	}
	
	public double getWeight(AbstractNetworkGraphNode target){
		
		if (!(target instanceof CorticalNetworkGraphNode)) return Double.NaN;
		CorticalNetworkGraphNode node = (CorticalNetworkGraphNode)target;
		
		ArrayList<DynamicModelComponent> connections = region.getConnections();
		ArrayList<AbstractCorticalRegion> targets;
		
		for (int i = 0; i < connections.size(); i++){
			targets = ((AbstractCorticalConnection)connections.get(i)).getTargets();
			for (int j = 0; j < targets.size(); j++)
				if (targets.get(j).equals(node.region))
					return ((AbstractCorticalConnection)connections.get(i)).getWeight();
			}

		return Double.NaN;
	}
	
	public AbstractCorticalRegion getRegion(){
		return region;
	}
	
	public String getLabel(){
		return getRegion().getName();
	}
	
	@Override
	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getLocalName() {
		return "CorticalNetworkNetworkGraph";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions optionsXMLOutputOptions, ProgressUpdater progress_bar) throws IOException {
		//TODO
	}
	
	
	
}