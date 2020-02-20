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


package mgui.interfaces.shapes.neuro;

import java.awt.Color;
import java.util.ArrayList;

import javax.media.j3d.BranchGroup;
import javax.swing.ImageIcon;

import mgui.geometry.Plane3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.Mesh2DInt;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.ShapeSceneNode;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiNumber;

/**********************************************************
 * Representation of a set of cortical surfaces, including grey matter (GM) interfaces with white matter (WM)
 * and cerebrospinal fluid (CSF).  
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CorticalSurfaceSet3DInt extends Mesh3DInt {

	public CorticalHemisphereSet3DInt right, left;
	
	public static final int HEM_LEFT = 0;
	public static final int HEM_RIGHT = 0;
	
	public CorticalSurfaceSet3DInt(){
		init();
	}
	
	public CorticalSurfaceSet3DInt(String name){
		setName(name);
		init();
	}
	
	public CorticalSurfaceSet3DInt(String name, CorticalHemisphereSet3DInt left, CorticalHemisphereSet3DInt right){
		setName(name);
		if (left != null) setLeft(left);
		if (right != null) setRight(right);
		init();
	}
	
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/neuro/cortical_set_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			System.out.println("Cannot find resource: mgui/resources/icons/neuro/cortical_set_20.png");
	}
	
	public void setLeft(CorticalHemisphereSet3DInt left){
		this.left = left;
		left.setName("Left");
		updateShape();
		setScene3DObject();
		fireShapeModified();
		fireChildren2DModified();
		updateTreeNodes();
	}
	
	public void setRight(CorticalHemisphereSet3DInt right){
		this.right = right;
		right.setName("Right");
		setScene3DObject();
		updateShape();
		fireShapeModified();
		fireChildren2DModified();
		updateTreeNodes();
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	public void setHemispheres(CorticalHemisphereSet3DInt left, CorticalHemisphereSet3DInt right){
		setHemispheres(left, right, true);
	}
	
	public void setHemispheres(CorticalHemisphereSet3DInt left, CorticalHemisphereSet3DInt right, boolean update){
		this.left = left;
		if (left != null)
			left.setName("Left");
		this.right = right;
		if (right != null)
			right.setName("Right");
		if (update){
			updateShape();
			updateHemisphereAttributes();
			setScene3DObject();
			fireShapeModified();
			fireChildren2DModified();
			updateTreeNodes();
			}
	}
	
	protected void init(){
		super.init();
		
		attributes.add(new Attribute<MguiBoolean>("ShowWM", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowMid", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowGM", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowRight", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowLeft", new MguiBoolean(true)));
		attributes.add(new Attribute<Color>("FillClrWM", Color.RED));
		attributes.add(new Attribute<Color>("FillClrGM", Color.BLUE));
		attributes.add(new Attribute<Color>("FillClrMid", Color.GREEN));
		
 		updateShape();
	}
	
	public boolean hasLeft(){
		return left != null;
	}
	
	public boolean hasRight(){
		return right != null;
	}
	
	/*********************************
	 * Add node data to both hemispheres
	 * @param key
	 * @param data
	 *****************/
	public boolean addVertexData(String key, ArrayList<MguiNumber> data){
		if (!super.addVertexData(key, data)) return false;
		if (left != null) left.addVertexData(key, data);
		if (right != null) right.addVertexData(key, data);
		return true;
	}
	
	/*********************************
	 * Add node data to the specified hemisphere (specified by constants HEM_LEFT and
	 * HEM_RIGHT.
	 * @param key
	 * @param data
	 * @param hemi
	 *****************/
	public void addVertexData(String key, ArrayList<MguiNumber> data, int hemi){
		if (hemi == HEM_LEFT && left != null)
			left.addVertexData(key, data);
		if (hemi == HEM_RIGHT && right != null)
			right.addVertexData(key, data);
		super.addVertexData(key, data);
	}
	
	public void removeVertexData(String key){
		super.removeVertexData(key);
		if (left != null) left.removeVertexData(key);
		if (right != null) right.removeVertexData(key);
	}
	
	public void setColourMap(ColourMap cm, boolean update){
		if (left != null) left.setColourMap(cm, false);
		if (right != null) right.setColourMap(cm, false);
		super.setDefaultColourMap(cm, update);
	}
	
	public void setCurrentColumn(String key, boolean update){
		super.setCurrentColumn(key, false);
		if (left != null) left.setCurrentColumn(key, false);
		if (right != null) right.setCurrentColumn(key, false);
		if (showData() && update)
			setScene3DObject();
	}
	
	boolean passToMesh(Attribute a){
		if (a.getName().equals("Name"))
			return false;
		return true;
	}
	
	public void attributeUpdated(AttributeEvent e){
		
		// Propagate to members
		Attribute<?> a = e.getAttribute();
		if (passToMesh(a)){
			if (left != null){
				left.setAttribute(a.getName(), a.getValue());
				}
			if (right != null){
				right.setAttribute(a.getName(), a.getValue());
				}
			
			// If node has been reattached, regenerate the set node
			if ((a.getName().equals("ShowWM") ||
				 a.getName().equals("ShowGM") ||
				 a.getName().equals("ShowMid")) &&
					((MguiBoolean)a.getValue()).getTrue()){
				this.setScene3DObject();
				return;
				}
			}
		
	}
	
	void updateHemisphereAttributes(){
		ArrayList<Attribute<?>> list = attributes.getAsList();
		for (int i = 0; i < list.size(); i++){
			String name = list.get(i).getName();
			
			if (left != null){
				Attribute<String> a = (Attribute<String>)left.getAttributes().getAttribute(name);
				if (a != null && passToMesh(a))
					a.setValue(list.get(i).getValue(), false);
				left.updateMeshAttributes();
				}
			if (right != null){
				Attribute<String> a = (Attribute<String>)right.getAttributes().getAttribute(name);
				if (a != null && passToMesh(a))
					a.setValue(list.get(i).getValue(), false);
				right.updateMeshAttributes();
				}
			}
	}
	
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist){
		ShapeSet2DInt set = new ShapeSet2DInt();
		
		if (left != null)
			set.addShape(left.getShape2DInt(plane, above_dist, below_dist, false));
		if (right != null)
			set.addShape(right.getShape2DInt(plane, above_dist, below_dist, false));
		
		set.getAttributes().setIntersection(attributes);
		return set;
	}
	
	public void setScene3DObject(boolean make_live){
		super.setScene3DObject(false);
		if (!isVisible()) return;
		
		scene3DObject.removeAllChildren();
		//releaseScene3DChildren();
		
		if (left != null){
			
			left.setScene3DObject(false);
			BranchGroup scene_group = left.getScene3DObject();
			if (scene_group != null){
				//if (left.scene3DObject.getParent() != null)
				scene_group.detach();
				scene3DObject.addChild(scene_group);
				}
					
			}
		if (right != null){
			right.setScene3DObject(false);
			BranchGroup scene_group = right.getScene3DObject();
			if (scene_group != null){
				//if (right.scene3DObject.getParent() != null)
				scene_group.detach();
				scene3DObject.addChild(scene_group);
				}
					
			}
		
		if (make_live) setShapeSceneNode();
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		//String debug = "Cort set tree node: ";
		
		//hemispheres
		if (left != null){
			left.setTreeNodeLite(treeNode);
			//debug = debug + "left ";
			}
		if (right != null){
			right.setTreeNodeLite(treeNode);
			//debug = debug + "right";
			}
		
		//System.out.println(debug);
	}
	
	public String toString(){
		return "CorticalSurface3DInt: " + getName();
	}
	
	public void updateShape(){
		boundSphere = null;
		boundBox = null;
		if (left != null){
			left.updateShape();
			boundSphere = left.boundSphere;
			boundBox = left.boundBox;
			}
		if (right != null){
			right.updateShape();
			boundSphere = GeometryFunctions.getUnionSphere(boundSphere, right.boundSphere);
			boundBox = GeometryFunctions.getUnionBounds(boundBox, right.boundBox);
			}
		if (boundBoxNode != null)
			setBoundBoxNode();
	}
	
}