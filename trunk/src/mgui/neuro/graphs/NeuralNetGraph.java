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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.models.networks.NetworkEvent;
import mgui.neuro.components.AbstractAxon;
import mgui.neuro.components.AbstractNeuron;
import mgui.neuro.components.AbstractSynapse;
import mgui.neuro.networks.AbstractNeuralNet;

/**********************************
 * Extension of JUNG's DirectedSparseGraph designed to specifically represent instances
 * of NeuralNet. Requires vertices to be instances of NeuralNetNode and edges to be
 * instances of NeuralNetConnection.
 * 
 * @author Andrew Reid
 * @version 1.0
 */

public class NeuralNetGraph extends InterfaceAbstractGraph{

	//public InterfaceTreeNode treeNode;
	public AbstractNeuralNet network;
	
	
	public NeuralNetGraph(){
		init();
	}
	
	public NeuralNetGraph(AbstractNeuralNet net){
		init();
		setFromNetwork(net);
	}
	
	protected void init(){
		super.init();
		attributes.add(new Attribute("Name", "noname"));
	}
	
	public boolean setFromNetwork(AbstractNeuralNet network){
		//TODO if network exists, remove listener
		this.network = network;
		
		//TODO add listener
		if (network.units == null) return false;
		
		this.removeAllEdges();
		this.removeAllNodes();
		
		//create a node for each unit in this network and add to graph
		//NeuralNetGraph graph = new NeuralNetGraph();
		for (int i = 0; i < network.units.size(); i++)
			addVertex(new NeuralNetNode(network.units.get(i)));
		
		//create a directed edge for each axon -> synapse connection
		ArrayList<AbstractGraphNode> nodes = new ArrayList<AbstractGraphNode>(getVertices());
		AbstractAxon thisAxon;
		
		//comparator for nodes and neurons
		Comparator nodeComp = new Comparator(){
			public int compare(Object n1, Object n2){
				long id1 = 0, id2 = 0;
				if (n1 instanceof NeuralNetNode) id1 = ((NeuralNetNode)n1).getUnit().getID();
				if (n1 instanceof AbstractNeuron) id1 = ((AbstractNeuron)n1).getID();
				if (n2 instanceof NeuralNetNode) id2 = ((NeuralNetNode)n2).getUnit().getID();
				if (n2 instanceof AbstractNeuron) id2 = ((AbstractNeuron)n2).getID();
				if (id1 > id2) return 1;
				if (id1 < id2) return -1;
				return 0;
				}
			};
		
		Collections.sort(nodes, nodeComp);
			
		for (int i = 0; i < nodes.size(); i++){
			//connections for all axon/synapse pairs
			thisAxon = (AbstractAxon)((NeuralNetNode)nodes.get(i)).getUnit().getAxon();
			if (thisAxon != null)
				for (int j = 0; j < thisAxon.synapses.size(); j++){
					//get synapse parent
					int index = Collections.binarySearch(nodes, 
														 ((AbstractSynapse)thisAxon.synapses.get(j)).parent,
														 nodeComp);
					if (index >= 0)
						addEdge(new NeuralNetConnection((NeuralNetNode)nodes.get(i),
														(NeuralNetNode)nodes.get(index)),
								nodes.get(i),
								nodes.get(index));
					}
			}
		
		//network.removeListener(this);
		//network.addListener(this);
		
		return true;
	}
	
	public Attribute getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}

	public AttributeList getAttributes() {
		return attributes;
	}

	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}

	/*
	public void setTreeNode(){
		if (treeNode == null)
			treeNode = new InterfaceTreeNode(this);
		treeNode.removeAllChildren();
		//regenerate button
		treeNode.add(new RegenButton(this));
		
		if (attributes != null){
			//attributes.setTreeNode();
			treeNode.add(attributes.getTreeNode());
			}
		
		if (network != null)
			treeNode.add(new InterfaceTreeNode(network));
			//treeNode.add(network.getTreeNode());
	}
	*/
	
	public void networkUpdated(NetworkEvent e){
		setFromNetwork(network);
		fireGraphListeners();
	}
	
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public String toString(){
		return "Neural Net Graph: " + getName();
	}
	
	class RegenButton extends DefaultMutableTreeNode implements ActionListener{
		
		JButton button = new JButton("Regen Graph");
		
		public RegenButton(NeuralNetGraph graph){
			this.setUserObject(graph);
			button.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e){
			((NeuralNetGraph)this.getUserObject()).fireGraphListeners();
		}
		
		class Renderer extends DefaultTreeCellRenderer{
			public Component getTreeCellRendererComponent(JTree tree,
				    Object value,
				    boolean sel,
				    boolean expanded,
				    boolean leaf,
				    int row,
				    boolean hasFocus){
			
				return button;
			}
		}
	}
	
	
	@Override
	public String getLocalName() {
		return "NeuralNetGraph";
	}
	
	
}