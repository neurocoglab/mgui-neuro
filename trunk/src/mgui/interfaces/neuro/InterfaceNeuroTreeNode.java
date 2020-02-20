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


package mgui.interfaces.neuro;

import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.networks.components.NetworkComponentEvent;
import mgui.neuro.components.AbstractNeuroComponent;

public class InterfaceNeuroTreeNode extends InterfaceTreeNode 
									implements InterfaceNeuroComponentListener {

	//public AbstractNeuroComponent source;
	
	public InterfaceNeuroTreeNode(AbstractNeuroComponent source){
		super(source);
		//this.source = source;
	}
	
	public void componentUpdated(NetworkComponentEvent e){
		//getSource().setTreeNode();
	}
	
	public AbstractNeuroComponent getSource(){
		return (AbstractNeuroComponent)this.getUserObject();
	}
	
}