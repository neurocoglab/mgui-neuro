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


package mgui.geometry.neuro.mesh;

import java.util.ArrayList;
import java.util.HashMap;

import mgui.geometry.Grid3D;
import mgui.geometry.Mesh3D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.util.Engine;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

/**************************************************
 * Engine for performing functions on cortical surfaces and other objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NeuroMeshEngine implements Engine {

	HashMap<String, AttributeList> attributes = new HashMap<String, AttributeList>();
	
	public NeuroMeshEngine(){
		init();
	}
	
	
	private void init(){
		
		AttributeList attribute_list = new AttributeList();
		
		attribute_list.add(new Attribute<String>("grid_channel", "default"));
		attribute_list.add(new Attribute<String>("mesh_column", "?"));
		attribute_list.add(new Attribute<MguiDouble>("sigma_normal", new MguiDouble(4)));
		attribute_list.add(new Attribute<MguiDouble>("sigma_tangent", new MguiDouble(3)));
		attribute_list.add(new Attribute<MguiDouble>("sigma_max_normal", new MguiDouble(2)));
		attribute_list.add(new Attribute<MguiDouble>("sigma_max_tangent", new MguiDouble(2)));
		attribute_list.add(new Attribute<MguiDouble>("thickness", new MguiDouble(0)));
		//attribute_list.add(new Attribute("vertex_thickness", VariableObject.class));
		attribute_list.add(new Attribute<MguiBoolean>("write matrix file", new MguiBoolean(false)));
		attribute_list.add(new Attribute<String>("watrix file", "c:\\matrix_file.mat"));
		attributes.put("Volume -> Cortex", attribute_list);
		
		attribute_list = new AttributeList();
		attribute_list.add(new Attribute<String>("grid_channel", "default"));
		attribute_list.add(new Attribute<String>("mesh_column", "?"));
		attribute_list.add(new Attribute<MguiDouble>("sigma_normal", new MguiDouble(4)));
		attribute_list.add(new Attribute<MguiDouble>("sigma_tangent", new MguiDouble(3)));
		attribute_list.add(new Attribute<MguiDouble>("sigma_max_normal", new MguiDouble(2)));
		attribute_list.add(new Attribute<MguiDouble>("sigma_max_tangent", new MguiDouble(2)));
		attribute_list.add(new Attribute<MguiDouble>("thickness", new MguiDouble(0)));
		attribute_list.add(new Attribute<MguiInteger>("debug_index", new MguiInteger(-1)));
		attribute_list.add(new Attribute<MguiBoolean>("normal_weight", new MguiBoolean(false)));
		attribute_list.add(new Attribute<MguiBoolean>("no_weight", new MguiBoolean(false)));
		//attribute_list.add(new Attribute("vertex_thickness", VariableObject.class));
		attribute_list.add(new Attribute<MguiBoolean>("normalize_values", new MguiBoolean(false)));
		attributes.put("Cortex -> Volume", attribute_list);
		
	}
	
	@Override
	public AttributeList getAttributes(String operation, String method){
		return attributes.get(operation);
	}
	
	@Override
	public ArrayList<String> getOperations(){
		return null;
	}
	
	@Override
	public ArrayList<String> getMethods(String operation){
		
		return null;
	}
	
	@Override
	public boolean callMethod(String operation, String method, ProgressUpdater progress){
		return false;
	}
	
	@Override
	public boolean callMethod(String operation, String method, ArrayList<?> params, ProgressUpdater progress){
		return false;
	}
	
	/**************************************************
	 * Maps cortical surface values to a volume
	 * 
	 * @param mesh
	 * @param grid
	 * @param thickness
	 */
	public Volume3DInt mapCortexToVolume(Mesh3DInt mesh_int, 
									Volume3DInt volume, 
									Object thickness, 
									ProgressUpdater progress){
		
		AttributeList list = attributes.get("Cortex -> Volume");
		
		NeuroMeshFunctions.debug_index = (int)((MguiInteger)list.getValue("debug_index")).getValue();
		NeuroMeshFunctions.normal_weight = ((MguiBoolean)list.getValue("normal_weight")).getTrue();
		
		return
		NeuroMeshFunctions.mapCortexToVolumeGaussian(mesh_int, 
													 volume, 
													 (String)list.getValue("mesh_column"), 
													 (String)list.getValue("grid_channel"), 
													 ((MguiDouble)list.getValue("sigma_normal")).getValue(), 
													 ((MguiDouble)list.getValue("sigma_tangent")).getValue(),
													 ((MguiDouble)list.getValue("sigma_max_normal")).getValue(), 
													 ((MguiDouble)list.getValue("sigma_max_tangent")).getValue(), 
													 thickness, 
													 false, 
													 progress);
		
	}
	
	
	/**************************************************
	 * Maps a volume to a cortical surface
	 * 
	 * @param mesh
	 * @param grid
	 * @param thickness
	 */
	public ArrayList<MguiNumber> mapVolumeToCortex(Mesh3D mesh, Volume3DInt volume, 
												 Object thickness, ProgressUpdater progress){
		
		AttributeList list = attributes.get("Volume -> Cortex");
		
		return
		NeuroMeshFunctions.mapVolumeToCortexGaussian(mesh, 
													 volume, 
													(String)list.getValue("grid_channel"), 
													((MguiDouble)list.getValue("sigma_normal")).getValue(), 
													((MguiDouble)list.getValue("sigma_tangent")).getValue(),
													((MguiDouble)list.getValue("sigma_max_normal")).getValue(), 
													((MguiDouble)list.getValue("sigma_max_tangent")).getValue(), 
													thickness, 
													false, 
													progress, 
													false, 
													null);
		
	}
	
	@Override
	public Attribute<?> getAttribute(String name) {
		
		return null;
	}

	@Override
	public AttributeList getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public AttributeList getAttributes(String name) {
		return attributes.get(name);
	}

	@Override
	public String getName() {
		return "Neuro Mesh Engine Instance";
	}
	
	@Override
	public void setName(String name){}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttributes(AttributeList attribute_list) {
		// TODO Auto-generated method stub

	}

}