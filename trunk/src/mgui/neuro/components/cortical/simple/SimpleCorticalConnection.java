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

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.dynamic.DynamicModelEvent;
import mgui.neuro.components.cortical.AbstractCorticalConnection;
import mgui.neuro.components.cortical.AbstractCorticalRegion;
import mgui.neuro.exceptions.NeuroException;


public class SimpleCorticalConnection extends AbstractCorticalConnection {
	
	protected double firingRate;
	
	public SimpleCorticalConnection(){
		init();
	}
	
	public double getFiringRate(){
		return firingRate;
	}
	
	protected boolean executeEvent(DynamicModelEvent e) throws NeuroException {
		
		//handle input from region
		if (e instanceof SimpleCorticalRegionEvent){
			firingRate = ((SimpleCorticalRegionEvent)e).getFiringRate();
			return true;
			}
		
		updated = true;
		return false;
	}
	
	protected boolean updateComponent(){
		//if (!updated) return false;
		
		//notify target region
		for (int i = 0; i < connections.size(); i++)
			connections.get(i).addEvent(new SimpleCorticalConnectionEvent(this));
		
		return true;
	}
	
	public void connectTo(AbstractCorticalRegion target){
		connectTo(target, 1.0);
	}
	
	public void connectTo(AbstractCorticalRegion target, double weight){
		addConnection(target);
		setWeight(weight);
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.add(attributes.issueTreeNode());
		
		//only one target
		if (connections.size() > 0)
			treeNode.add(new DefaultMutableTreeNode("Target:" + 
									((SimpleCorticalRegion)connections.get(0)).getName()));
	}
	
	public void reset(){
		super.reset();
		firingRate = 0;
	}
	
	public String toString(){
		return "Cortical Connection [" + id + "]";
	}
	
}