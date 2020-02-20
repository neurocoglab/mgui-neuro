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


package mgui.interfaces.neuro.imaging.camino;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.pipelines.trees.PipelineTreeTransferHandler;
import mgui.neuro.imaging.camino.CaminoTaskTreeNode;
import mgui.pipelines.InterfacePipeline;
import mgui.pipelines.PipelineProcessInstance;
import mgui.pipelines.PipelineTask;


public class CaminoPipelineTree extends JTree implements PopupMenuObject, MouseListener {

	//private MouseEvent current_event;
	transient InterfaceCaminoPanel panel;
	
	public enum PopupObject{
		Pipeline,
		Task;
	}
	
	protected PopupObject current_popup;
	protected Point current_location;
	
	public CaminoPipelineTree(DefaultTreeModel model){
		this(model, null);
	}
	
	public CaminoPipelineTree(DefaultTreeModel model, InterfaceCaminoPanel panel){
		super(model);
		this.setTransferHandler(new PipelineTreeTransferHandler());
		addMouseListener(this);
		this.panel = panel;
	}
	
	public InterfacePopupMenu getPopupMenu(){
		return null;
	}
	
	public InterfacePopupMenu getPopupMenu(MouseEvent e) {
		//if (current_event == null) return null;
		TreePath path = this.getPathForLocation(e.getX(), e.getY());
		if (path == null || path.getPathCount() == 0) return null;
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		
		if (node.getUserObject() instanceof InterfacePipeline){
			return getPipelineMenu();
			}
		
		if (node.getUserObject() instanceof PipelineTask){
			return getTaskMenu();
			}
		
		return null;
		
	}
	
	protected InterfacePopupMenu getPipelineMenu(){
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Camino Pipeline", 0));
		menu.add(new JSeparator(),1);
		menu.addMenuItem(new JMenuItem("Rename"));
		menu.addMenuItem(new JMenuItem("Copy"));
		menu.addMenuItem(new JMenuItem("Cut"));
		menu.addMenuItem(new JMenuItem("Delete"));
		
		current_popup = PopupObject.Pipeline;
		this.clearSelection();
		this.addSelectionPath(this.getClosestPathForLocation(current_location.x, current_location.y));
		
		return menu;
		
	}
	
	protected InterfacePopupMenu getTaskMenu(){
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Camino Task", 0));
		menu.add(new JSeparator(),1);
		menu.addMenuItem(new JMenuItem("Edit"));
		menu.addMenuItem(new JMenuItem("Copy"));
		menu.addMenuItem(new JMenuItem("Cut"));
		menu.addMenuItem(new JMenuItem("Delete"));
		
		current_popup = PopupObject.Task;
		this.clearSelection();
		this.addSelectionPath(this.getClosestPathForLocation(current_location.x, current_location.y));
		
		return menu;
		
	}

	public void handlePopupEvent(ActionEvent e) {
		
		if (current_popup == null || current_location == null) return;
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		TreePath path = this.getSelectionPath();
		if (path.getPathCount() == 0) return;
		
		switch (current_popup){
		
			case Pipeline:
				if (!(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject() instanceof InterfacePipeline)) return;
				
				if (item.getText().equals("Rename")){
					if (panel == null) return;
					InterfacePipeline pipeline = (InterfacePipeline)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
					
					String new_name = JOptionPane.showInputDialog(panel, "Rename pipeline:", pipeline.getName());
					if (new_name == null) return;
					
					pipeline.setName(new_name);
					this.updateUI();
					return;
					}
				
				if (item.getText().equals("Delete")){
					if (panel == null) return;
					panel.actionPerformed(new ActionEvent(this, 0, "Pipeline Remove"));
					return;
					}
				
				break;
				
			case Task:
				if (!(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject() instanceof PipelineTask)) return;
				
				if (item.getText().equals("Edit")){
					if (panel == null) return;
					panel.actionPerformed(new ActionEvent(this, 0, "Pipeline Process Edit"));
					return;
					}
				
				if (item.getText().equals("Delete")){
					if (panel == null) return;
					panel.actionPerformed(new ActionEvent(this, 0, "Pipeline Process Remove"));
					return;
					}
				
				break;
		
			}
		
	}

	
	public void showPopupMenu(MouseEvent e) {
		//current_event = e;
		current_location = e.getPoint();
		InterfacePopupMenu menu = getPopupMenu(e);
		if (menu == null) return;
		
		
		menu.show(e);
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}
	
	public ArrayList<InterfacePipeline> getSelectedPipelines(){
		
		ArrayList<InterfacePipeline> pipes = new ArrayList<InterfacePipeline>();
		TreePath[] paths = getSelectionPaths();
		if (paths == null) return pipes;
		
		for (int i = 0; i < paths.length; i++){
			TreePath path = paths[i];
			if (path != null)
				for (int j = 1; j < path.getPathCount(); j++){
					Object o = path.getPathComponent(j);
					if (o instanceof CaminoTaskTreeNode &&
						((CaminoTaskTreeNode)o).getTask() instanceof InterfacePipeline){
						pipes.add((InterfacePipeline)((CaminoTaskTreeNode)o).getTask());
						}
					}
			}
		
		return pipes;
	}
	
	public void expandPipelineNode(InterfacePipeline pipeline){
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		Enumeration children = root.children();
		
		while (children.hasMoreElements()){
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
			if (child.getUserObject().equals(pipeline)){
				this.expandPath(new TreePath(child.getPath()));
				break;
				}
			}
		
		updateUI();
	}
	
	public ArrayList<PipelineProcessInstance> getSelectedTasks(){
		
		TreePath[] paths = getSelectionPaths();
		ArrayList<PipelineProcessInstance> tasks = new ArrayList<PipelineProcessInstance>();
		
		for (int i = 0; i < paths.length; i++){
			TreePath path = paths[i];
			if (path != null)
				for (int j = 1; j < path.getPathCount(); j++){
					Object o = path.getPathComponent(j);
					if (o instanceof CaminoTaskTreeNode &&
						((CaminoTaskTreeNode)o).getTask() instanceof PipelineProcessInstance){
						tasks.add((PipelineProcessInstance)((CaminoTaskTreeNode)o).getTask());
						}
					}
			}
		
		return tasks;
	}
	
	public InterfacePipeline getSelectedPipeline(){
		TreePath path = getSelectionPath();
		if (path == null) return null;
		
		for (int i = 1; i < path.getPathCount(); i++){
			Object o = path.getPathComponent(i);
			if (o instanceof CaminoTaskTreeNode &&
				((CaminoTaskTreeNode)o).getTask() instanceof InterfacePipeline){
				return (InterfacePipeline)((CaminoTaskTreeNode)o).getTask();
				}
			}
		return null;
	}
	
	public PipelineProcessInstance getSelectedTask(){
		
		TreePath path = getSelectionPath();
		if (path == null) return null;
		
		for (int i = 1; i < path.getPathCount(); i++){
			Object o = path.getPathComponent(i);
			if (o instanceof CaminoTaskTreeNode &&
					!(((CaminoTaskTreeNode)o).getTask() instanceof InterfacePipeline)){
				return (PipelineProcessInstance)((CaminoTaskTreeNode)o).getTask();
				}
			}
		return null;
		
	}
	
}