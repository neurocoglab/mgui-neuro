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
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiNumber;


public class CorticalHemisphereSet3DInt extends Mesh3DInt {

	public Mesh3DInt wm_mesh, middle_mesh, gm_mesh;
	
	public CorticalHemisphereSet3DInt(){
		init();
	}
	
	public CorticalHemisphereSet3DInt(String name){
		init();
		setName(name);
		if (gm_mesh != null)
			gm_mesh.setName("GM");
			//gm_mesh.setName(name + ".gm");
		if (wm_mesh != null)
			wm_mesh.setName("WM");
			//gm_mesh.setName(name + ".wm");
		if (middle_mesh != null)
			middle_mesh.setName("MID");
			//middle_mesh.setName(name + ".mid");
	}
	
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/neuro/cortical_surf_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			System.out.println("Cannot find resource: mgui/resources/icons/neuro/cortical_surf_20.png");
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
		
		//if (wm_mesh != null) wm_mesh.setName(name + ".wm");
		//if (gm_mesh != null) gm_mesh.setName(name + ".gm");
		//if (middle_mesh != null) middle_mesh.setName(name + ".mid");
	}
	
	public void setMeshes(Mesh3DInt wm_mesh, Mesh3DInt middle_mesh, Mesh3DInt gm_mesh){
		setMeshes(wm_mesh, middle_mesh, gm_mesh, true);
	}
	
	public void setMeshes(Mesh3DInt wm_mesh, Mesh3DInt middle_mesh, Mesh3DInt gm_mesh, boolean update){
		if (wm_mesh != null) setMeshWM(wm_mesh, false);
		if (middle_mesh != null) setMeshMiddle(middle_mesh, false);
		if (gm_mesh != null) setMeshGM(gm_mesh, false);
		if (update){
			updateMeshAttributes();
			setScene3DObject();
			fireShapeModified();
			//fireShape2DListeners(new ShapeEvent(this, ShapeEvent.UPDATE_REGEN));
			}
	}
	
	public void setMeshWM(Mesh3DInt mesh){
		setMeshWM(mesh, true);
	}
	
	public void setMeshWM(Mesh3DInt mesh, boolean update){
		//if (wm_mesh != null) wm_mesh.removeShapeListener(this);
		
		wm_mesh = mesh;
		wm_mesh.setName("WM");
		//wm_mesh.setName(getName() + ".wm");
		//wm_mesh.addShapeListener(this);
		Attribute<MguiBoolean> a = (Attribute<MguiBoolean>)attributes.getAttribute("HasWM");
		a.setValue(new MguiBoolean(true), false);
		if (update){
			updateMeshAttributes();
			fireShapeModified();
			updateTreeNodes();
			}
		
		
	}
	
	public void setMeshMiddle(Mesh3DInt mesh){
		setMeshMiddle(mesh, true);
	}
	
	public void setMeshMiddle(Mesh3DInt mesh, boolean update){
		middle_mesh = mesh;
		middle_mesh.setName("MID");
		Attribute<MguiBoolean> a = (Attribute<MguiBoolean>)attributes.getAttribute("HasMid");
		a.setValue(new MguiBoolean(true), false);
		if (update){
			updateMeshAttributes();
			fireShapeModified();
			updateTreeNodes();
			}
		
	}

	public void setMeshGM(Mesh3DInt mesh){
		setMeshGM(mesh, true);
	}
	
	public void setMeshGM(Mesh3DInt mesh, boolean update){
		gm_mesh = mesh;
		gm_mesh.setName("GM");
		Attribute<MguiBoolean> a = (Attribute<MguiBoolean>)attributes.getAttribute("HasGM");
		a.setValue(new MguiBoolean(true), false);
		if (update){
			updateMeshAttributes();
			fireShapeModified();
			updateTreeNodes();
			}
		
	}
	
	protected void init(){
		super.init();
		
		attributes.add(new Attribute<MguiBoolean>("HasWM", new MguiBoolean(false), false, false));
		attributes.add(new Attribute<MguiBoolean>("HasMid", new MguiBoolean(false), false, false));
		attributes.add(new Attribute<MguiBoolean>("HasGM", new MguiBoolean(false), false, false));
		attributes.add(new Attribute<MguiBoolean>("ShowWM", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowMid", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowGM", new MguiBoolean(true)));
		attributes.add(new Attribute<Color>("FillClrWM", Color.RED));
		attributes.add(new Attribute<Color>("FillClrGM", Color.BLUE));
		attributes.add(new Attribute<Color>("FillClrMid", Color.GREEN));
		
 		updateShape();
	}
	
	public boolean addVertexData(String key, ArrayList<MguiNumber> data){
		if (!super.addVertexData(key, data)) return false;
		if (wm_mesh != null) wm_mesh.addVertexData(key, data);
		if (middle_mesh != null) middle_mesh.addVertexData(key, data);
		if (gm_mesh != null) gm_mesh.addVertexData(key, data);
		return true;
	}
	
	public void removeVertexData(String key){
		super.removeVertexData(key);
		if (wm_mesh != null) wm_mesh.removeVertexData(key);
		if (middle_mesh != null) middle_mesh.removeVertexData(key);
		if (gm_mesh != null) gm_mesh.removeVertexData(key);
	}
	
	public void setColourMap(ColourMap cm, boolean update){
		if (wm_mesh != null) wm_mesh.setDefaultColourMap(cm, false);
		if (middle_mesh != null) middle_mesh.setDefaultColourMap(cm, false);
		if (gm_mesh != null) gm_mesh.setDefaultColourMap(cm, false);
		super.setDefaultColourMap(cm, update);
	}
	
	public void setCurrentColumn(String key, boolean update){
		//currentData = nodeData.get(key);
		super.setCurrentColumn(key, false);
		if (wm_mesh != null) wm_mesh.setCurrentColumn(key, false);
		if (middle_mesh != null) middle_mesh.setCurrentColumn(key, false);
		if (gm_mesh != null) gm_mesh.setCurrentColumn(key, false);
		if (showData() && update)
			setScene3DObject();
	}
	
	public void attributeUpdated(AttributeEvent e){
		Attribute<?> a = e.getAttribute();
		
		if (wm_mesh != null){
			if (a.getName().equals("ShowWM")){ 
				wm_mesh.setAttribute("IsVisible", a.getValue());
				return;
				}
			if (a.getName().equals("FillClrWM")){ 
				wm_mesh.setAttribute("FillColour", a.getValue());
				wm_mesh.setAttribute("2D.LineColour", a.getValue());
				return;
				}
			if (passToMesh(a))
				wm_mesh.setAttribute(a.getName(), a.getValue());
			}
		
		if (gm_mesh != null){
			if (a.getName().equals("ShowGM")){ 
				gm_mesh.setAttribute("IsVisible", a.getValue());
				return;
				}
			if (a.getName().equals("FillClrGM")){ 
				gm_mesh.setAttribute("FillColour", a.getValue());
				gm_mesh.setAttribute("2D.LineColour", a.getValue());
				return;
				}
			if (passToMesh(a))
				gm_mesh.setAttribute(a.getName(), a.getValue());
			}
		
		if (middle_mesh != null){
			if (a.getName().equals("ShowMid")){ 
				middle_mesh.setAttribute("IsVisible", a.getValue());
				return;
				}
			if (a.getName().equals("FillClrMid")){ 
				middle_mesh.setAttribute("FillColour", a.getValue());
				middle_mesh.setAttribute("2D.LineColour", a.getValue());
				return;
				}
			if (passToMesh(a))
				middle_mesh.setAttribute(a.getName(), a.getValue());
			}
		
		if (!passToMesh(a))
			super.attributeUpdated(e);
	}
	
	public String getTreeLabel(){
		return getName();
	}
	
	void updateMeshAttributes(){
		if (wm_mesh != null){
			MguiBoolean show = (MguiBoolean)attributes.getValue("ShowWM");
			updateMeshAttributes(wm_mesh);
			show = new MguiBoolean(show.getTrue() && ((MguiBoolean)attributes.getValue("IsVisible")).getTrue());
			wm_mesh.getAttributes().setValue("IsVisible", show, false);
			wm_mesh.getAttributes().setValue("FillColour", attributes.getValue("FillClrWM"), false);
			}
		if (gm_mesh != null){
			MguiBoolean show = (MguiBoolean)attributes.getValue("ShowGM");
			updateMeshAttributes(gm_mesh);
			show = new MguiBoolean(show.getTrue() && ((MguiBoolean)attributes.getValue("IsVisible")).getTrue());
			gm_mesh.getAttributes().setValue("IsVisible", show, false);
			gm_mesh.getAttributes().setValue("FillColour", attributes.getValue("FillClrGM"), false);
			}
		if (middle_mesh != null){
			MguiBoolean show = (MguiBoolean)attributes.getValue("ShowMid");
			updateMeshAttributes(middle_mesh);
			show = new MguiBoolean(show.getTrue() && ((MguiBoolean)attributes.getValue("IsVisible")).getTrue());
			middle_mesh.getAttributes().setValue("IsVisible", show, false);
			middle_mesh.getAttributes().setValue("FillColour", attributes.getValue("FillClrMid"), false);
			}
	}
	
	void updateMeshAttributes(Mesh3DInt mesh){
		ArrayList<Attribute<?>> list = attributes.getAsList();
		for (int i = 0; i < list.size(); i++){
			String name = list.get(i).getName();
			Attribute<String> a = (Attribute<String>)mesh.getAttributes().getAttribute(name);
			if (a != null && passToMesh(a))
				a.setValue(list.get(i).getValue(), false);
			}
	}
	
	boolean passToMesh(Attribute<?> a){
		return (a.getName().equals("HasFill") ||
				a.getName().equals("ShowEdges") ||
				a.getName().equals("ShowNodes") ||
				a.getName().equals("LineColour") ||
				a.getName().equals("Show2D") ||
				a.getName().equals("NodeColour") ||
				a.getName().equals("ShowData") ||
				a.getName().equals("CurrentData") ||
				a.getName().equals("ColourMap") ||
				a.getName().equals("DataMin") ||
				a.getName().equals("DataMax"));
	}

	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist){
		ShapeSet2DInt set = new ShapeSet2DInt();
		
		if (wm_mesh != null)
			set.addShape(wm_mesh.getShape2DInt(plane, above_dist, below_dist, false));
		if (gm_mesh != null)
			set.addShape(gm_mesh.getShape2DInt(plane, above_dist, below_dist, false));
		if (middle_mesh != null)
			set.addShape(middle_mesh.getShape2DInt(plane, above_dist, below_dist, false));
		
		set.getAttributes().setIntersection(attributes);
		return set;
	}
	
	//Why the hell is this causing a memory leak?
	public void setScene3DObject(boolean make_live){
		super.setScene3DObject(false);
		
		if (!isVisible()) return;
		
		scene3DObject.removeAllChildren();
		if (group_node != null)
			this.group_node.detach();
		this.group_node = null;
		
		if (wm_mesh != null){
			wm_mesh.setScene3DObject(false);
			BranchGroup scene_group = wm_mesh.getScene3DObject();
			if (scene_group != null){
				//if (wm_mesh.scene3DObject.getParent() != null)
				scene_group.detach();
				scene3DObject.addChild(scene_group);
				}
					
			}
		if (middle_mesh != null){
			middle_mesh.setScene3DObject(false);
			BranchGroup scene_group = middle_mesh.getScene3DObject();
			if (scene_group != null){
				//if (middle_mesh.scene3DObject.getParent() != null)
				scene_group.detach();
				scene3DObject.addChild(scene_group);
				}
			}
		if (gm_mesh != null){
			gm_mesh.setScene3DObject(false);
			BranchGroup scene_group = gm_mesh.getScene3DObject();
			if (scene_group != null){
				//if (gm_mesh.scene3DObject.getParent() != null)
				scene_group.detach();
				scene3DObject.addChild(scene_group);
				}
					
			}
		
		if (make_live) setShapeSceneNode();
	}
	
	public void setTreeNodeLite(InterfaceTreeNode treeNode){
		InterfaceTreeNode node = new InterfaceTreeNode(this);
		//String debug = "Cort hemi tree node lite: ";
		treeNode.add(node);
		if (wm_mesh != null){
			node.add(new InterfaceTreeNode(wm_mesh));
			//debug = debug + "wm ";
			}
		if (middle_mesh != null){
			node.add(new InterfaceTreeNode(middle_mesh));
			//debug = debug + "mid ";
			}
		if (gm_mesh != null){
			node.add(new InterfaceTreeNode(gm_mesh));
			//debug = debug + "gm ";
			}
		//System.out.println(debug);
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		//String debug = "Cort hemi tree node: ";
		if (wm_mesh != null){
			treeNode.add(new InterfaceTreeNode(wm_mesh));
			//debug = debug + "wm ";
			}
		if (middle_mesh != null){
			treeNode.add(new InterfaceTreeNode(middle_mesh));
			//debug = debug + "mid ";
			}
		if (gm_mesh != null){
			treeNode.add(new InterfaceTreeNode(gm_mesh));
			//debug = debug + "gm ";
			}
		//System.out.println(debug);
	}
	
	public String toString(){
		return "CorticalHemisphere3DInt: " + getName();
	}
	
	public void updateShape(){
		boundSphere = null;
		boundBox = null;
		if (wm_mesh != null){
			wm_mesh.updateShape();
			boundSphere = wm_mesh.boundSphere;
			boundBox = wm_mesh.boundBox;
			}
		if (gm_mesh != null){
			gm_mesh.updateShape();
			boundSphere = GeometryFunctions.getUnionSphere(boundSphere, gm_mesh.boundSphere);
			boundBox = GeometryFunctions.getUnionBounds(boundBox, gm_mesh.boundBox);
			}
		if (middle_mesh != null){
			middle_mesh.updateShape();
			boundSphere = GeometryFunctions.getUnionSphere(boundSphere, middle_mesh.boundSphere);
			boundBox = GeometryFunctions.getUnionBounds(boundBox, middle_mesh.boundBox);
			}
		if (boundBoxNode != null)
			setBoundBoxNode();
	}
	
	
}