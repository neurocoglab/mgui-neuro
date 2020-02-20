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
import mgui.models.networks.AbstractNetwork;
import mgui.models.networks.NetworkEvent;
import mgui.models.networks.NetworkListener;

public class InterfaceNetworkTreeNode extends InterfaceTreeNode 
										implements NetworkListener {

	public InterfaceNetworkTreeNode(AbstractNetwork source){
		super(source);
	}
	
	public void networkUpdated(NetworkEvent e){
		//getSource().setTreeNode();
		objectChanged(e.getNet());
		//fireListeners();
	}
	
	public AbstractNetwork getSource(){
		return (AbstractNetwork)this.getUserObject();
	}
	
	
}