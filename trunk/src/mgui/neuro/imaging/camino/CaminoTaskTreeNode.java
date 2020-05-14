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


package mgui.neuro.imaging.camino;

import java.awt.image.ImageObserver;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import mgui.interfaces.pipelines.trees.PipelineTree;
import mgui.interfaces.pipelines.trees.TaskTreeNode;
import mgui.interfaces.pipelines.trees.TaskTreeNodeListener;
import mgui.pipelines.PipelineTask;
import mgui.pipelines.PipelineTaskListener;


public class CaminoTaskTreeNode extends TaskTreeNode 
								implements PipelineTaskListener,
										   ImageObserver{

	JTree tree;
	DefaultTreeModel model;
	public boolean is_destroyed;
	
	Vector<TaskTreeNodeListener> listeners = new Vector<TaskTreeNodeListener>();
	
	public CaminoTaskTreeNode(PipelineTask task, JTree tree){
		super(task, (PipelineTree)tree);
		//task.addListener(this);
		//this.addTreeNodeListener(task);
		//this.model = (DefaultTreeModel)tree.getModel();
		//this.tree = tree;
	}
	
	/*
	public void taskStatusChanged(TaskEvent e){
		model.nodeChanged(this);
	}
	
	public void addTreeNodeListener(TreeNodeListener l){
		listeners.add(l);
	}
	
	public void removeTreeNodeListener(TreeNodeListener l){
		listeners.add(l);
	}
	
	public Task getTask(){
		return (Task)getUserObject();
	}
	
	public void detach(){
		TreeNodeEvent e = new TreeNodeEvent(this);
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).treeNodeDetached(e);
		listeners.clear();
		
		//detach all children
		Enumeration children = this.children();
		while (children.hasMoreElements()){
			Object obj = children.nextElement();
			if (obj instanceof CaminoTaskTreeNode)
				((CaminoTaskTreeNode)obj).detach();
			}
		
		((DefaultMutableTreeNode)getParent()).remove(this);
	}
	
	public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
	      if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
	    	  TreePath path = new TreePath(model.getPathToRoot(this));
	    	  Rectangle rect = tree.getPathBounds(path);
	    	  if (rect != null) {
	    		  tree.repaint(rect);
	    	  	  }
	      	  }
	      return (flags & (ALLBITS | ABORT)) == 0;
	}
	 */
	
}