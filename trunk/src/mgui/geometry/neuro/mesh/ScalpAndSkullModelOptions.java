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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.Plane3D;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;

/*******************************************************
 * Parameters for skull and scalp surface approximation. Default values are also specified here.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 * @see NeuroMeshFunctions
 *
 */
public class ScalpAndSkullModelOptions extends InterfaceOptions {

	public Volume3DInt t1_volume;							//structural image
	public String brain_mask;								//mask for brain tissue
	public Plane3D ear_nasium_plane;						//plane below which to mask
	public Point3f center_of_mass;							//center of mass for the brain (will be center of start spheres)
	public Mesh3D brain_surface;
	public int n_nodes = 1000;								//node count for resulting surfaces
	public float initial_radius = 4;						//initial radius of spheres
	public float sample_rate = 5;							//samples per unit distance (mm)
	public float min_dist_B_IS = 1.5f;						//minimum distance from brain surface to inner skull surface
	public float min_dist_OS_S = 0.5f;						//minimum distance from outer skull surface to scalp surface
	public double min_intensity = 0.2;						//minimum intensity which, if encountered over a distance of 
	public float max_intensity_dist = 4;					//max_intensity_dist, stops sampling (this indicates we are 
															//outside the scalp)
	public double min_slope = 0.05;							//minimum slope for boundary
	public boolean apply_gaussian = false;					//smooth voxel values with gaussian
	public float sigma_normal = 1;							//sigma for gaussian smoothing in direction normal to surface
	public float sigma_tangent = 3;							//sigma for gaussian smoothing in direction tangent to surface
	public float gaussian_cutoff = 1;						//cutoff, as multiple of sigma, for gaussian smoothing
	public double max_skull_int = 0.2;
	public double stop_skull_int = 0.5;
	public float max_skull_width = 10;
	public float min_skull_width = 3;
	public double plateau_slope = 0.3;						//defines the proportion of the N1-N2 slope used to define a
	public int min_plateau_length = 10;						//plateau, if encountered in min_plateau_length consecutive samples
	public double max_plateau_slope = 0.012;				//maximum acceptable slope for consideration as a plateau
	
	public boolean average_neighbour_rays = true;			//if true, uses a weighted average of neighbouring sample rays
															//instead of their raw values; the weighting is determined by

	public double average_neighbour_weight = 1.0;			//average_neighbour_weight
	public float gradient_AP_A = 0.05f;						//anterior-posterior gradient, for y > 0
	public float gradient_IS_S = 0.0f;						//inferior-superior gradient, for z > 0
	
	public float threshold_stdev = 2;						//number of standard deviations beyond which a correction should
															//be applied
	public float correction_stdev = 0.5f;					//number of standard deviations to set a corrected vertex
	
	public ShapeSet3DInt shape_set;							//shape set to which the resulting surfaces will be added
	
	public String template_rays;
	public double subject_deviation_threshold = 2;			//threshold deviation of a subject ray from an average rays
															//beyond which a node is set to the average of its neighbours 
	
	public String debug_output_dir = null;					//if non-null, debug output is written to this folder
	
	public int resample_n = 100;							//resample rays to n samples
	public float bottom_z = -50;							//z value which signifies the bottom of the image
															//this is used to detect bottom-pointing rays
	public float bottom_dist = 5;							//distance to set skull and scalp surfaces from each
															//other, for bottom rays
	
	public boolean generate_control_meshes = false;			//whether to also output control point meshes
	
	/**********************************
	 * Sets the parameters from a parameter file, which is a text file where parameters are specified by the form:
	 * 
	 * <p>< param name >=< value >
	 * 
	 * <p>Only primivite-valued parameters (of type <code>boolean</code>, <code>float</code>, <code>int</code>, or
	 * <code>double</code>) will be set.
	 */
	public void setFromFile(String file) throws IOException{
		
		Field[] fields = getClass().getDeclaredFields();
		HashMap<String, Field> field_map = new HashMap<String, Field>();
		for (int i = 0; i < fields.length; i++)
			field_map.put(fields[i].getName(), fields[i]);
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		int i = 1;
		
		while (line != null){
			StringTokenizer tokens = new StringTokenizer(line, "=");
			if (tokens.countTokens() < 2){
				System.out.println("ScalpAndSkullModelOptions.setFromFile (line " + i + ") has bad input: '" + line + "'.");
			}else{
				String name = tokens.nextToken();
				String value = tokens.nextToken();
				Field field = field_map.get(name);
				if (field == null){
					System.out.println("ScalpAndSkullModelOptions.setFromFile (line " + i + ") field '" + name + "' not found.");
				}else{
					try{
					Class<?> type = field.getType();
						  if (type.equals(boolean.class)){
							  field.setBoolean(this, Boolean.valueOf(value));
					}else if (type.equals(double.class)){
							  field.setDouble(this, Double.valueOf(value));
					}else if (type.equals(float.class)){
							  field.setFloat(this, Float.valueOf(value));
					}else if (type.equals(int.class)){
							  field.setInt(this, Integer.valueOf(value));
					}else{
						System.out.println("ScalpAndSkullModelOptions.setFromFile (line " + i + ") could not " + 
										   "set value for statement '" + line + "'.");
						}
					}catch (Exception e){
						System.out.println("ScalpAndSkullModelOptions.setFromFile (line " + i + ") exception encountered.");
						e.printStackTrace();
						}
					}
				}
			line = reader.readLine();
			i++;
			}
	}
	
}