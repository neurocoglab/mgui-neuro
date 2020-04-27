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

import java.awt.image.DataBuffer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Grid3D;
import mgui.geometry.Mesh3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Vector3D;
import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.mesh.NeighbourhoodMesh;
import mgui.geometry.util.GeometryFunctions;
import mgui.geometry.volume.VolumeFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.Utility;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.VectorSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.io.domestic.shapes.Mesh3DWriter;
import mgui.io.domestic.variables.DefaultMatrixFileWriter;
import mgui.io.domestic.variables.MatrixOutOptions;
import mgui.neuro.stats.SignalProcessing;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiNumber;
import mgui.stats.Histogram;
import mgui.stats.StatFunctions;
import foxtrot.Job;
import foxtrot.Worker;

/********************************************************
 * Utility class for mesh functions related to neuroscience applications.
 * 
 * <P>TODO: remove all obsolete functions; add status tags
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NeuroMeshFunctions extends Utility {

	public static int debug_index = -1;
	public static boolean normal_weight = false;
	public static boolean no_weight = false;
	
	public static boolean modelSkullAndScalp2(){
		
		return false;
	}
	
	/*****************************************
	 * Resamples the source mesh with the vertices of the resample mesh (typically a sphere). Uses a ray tracing
	 * approach; source mesh must be a convex surface.
	 * 
	 * @param source_mesh			Mesh to resample
	 * @param resample_mesh			Mesh whose vertices are used for resampling
	 * @return the resampled mesh
	 */
	public static Mesh3D resampleMesh(Mesh3D source_mesh, Mesh3D resample_mesh){
		
		Mesh3DInt source_int = new Mesh3DInt(source_mesh);
		Point3f center_source = (source_int).getCenterOfGravity();
		Point3f center_resample = (new Mesh3DInt(resample_mesh)).getCenterOfGravity();
		Mesh3D new_mesh = (Mesh3D)resample_mesh.clone();
		float radius = source_int.getBoundBox().getMinPt().distance(source_int.getBoundBox().getMaxPt());
		
		for (int i = 0; i < new_mesh.n; i++){
			Vector3f v = new Vector3f(new_mesh.getVertex(i));
			v.sub(center_resample);
			v.normalize();
			v.scale(radius * 2);
			
			Point3f new_node = MeshFunctions.getIntersectionPoint(source_mesh, center_source, v);
			new_mesh.setVertex(i, new_node);
			}
		
		return new_mesh;
		
	}
	
	public static boolean modelSkullAndScalp2Blocking(ScalpAndSkullModelOptions options,
													  String average_ray_file,
													  String subject_dir,
													  String subject,
													  String output_prefix,
													  int[][] A,
													  ArrayList<Double> scales,
													  boolean avr_out){
		
		String output_file = output_prefix + File.separator + subject;
		
		//3. Get normalized & unnormalized rays for subject
		ArrayList<ArrayList<Double>> unnormalized = new ArrayList<ArrayList<Double>>();
		Mesh3D hull_mesh = new Mesh3D();
		
		System.out.print("Loading ray curves..");
		ArrayList<ArrayList<Double>> normalized = NeuroMeshFunctions.getRayCurves(options, 
																				  subject, 
																				  output_file, 
																				  unnormalized,
																				  hull_mesh,
																				  null);
		System.out.println("done.");
		
		if (scales == null)
			scales = new ArrayList<Double>();
		
		//2. Load average rays if not already loaded
		if (A == null)
			A = loadAverageRays(average_ray_file, true, options, hull_mesh, scales);
		
		if (A == null){
			System.out.println("Couldn't load average rays from file..");
			return false;
			}
		
		//4. Get S[] points from normalized curves
		System.out.print("Setting skull indices..");
		int[][] S = null; //NeuroMeshFunctions.getSkullAndScalpIndices(A, normalized);
		System.out.println("done.");
		ArrayList<Boolean> tagged = new ArrayList<Boolean>();
		
		//5. Convert normalized indices to unnormalized
		System.out.print("Unnormalizing..");
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		ArrayList<MguiNumber> u_list = new ArrayList<MguiNumber>();
		for (int i = 0; i < normalized.size(); i++){
			float u_size = (float)unnormalized.get(i).size();
			max = Math.max(max, u_size);
			min = Math.min(min, u_size);
			u_list.add(new MguiFloat(u_size));
			
			//if (u_size > 300){
			//	System.out.println("u_size > 300 for ray " + i);
			//	writeRayToFile(unnormalized.get(i), output_file + "_unnorm_ray_" + i + ".txt");
			//	writeRayToFile(normalized.get(i), output_file + "_norm_ray_" + i + ".txt");
			//	}
			
			float n_size = (float)normalized.get(i).size();
			//float scale = (float)unnormalized.get(i).size() / (float)normalized.get(i).size();
			float scale = u_size / n_size;
			float a_scale = scales.get(i).floatValue();
			
			tagged.add(scale / a_scale > options.subject_deviation_threshold); 
			
			S[i][0] *= scale;
			S[i][1] *= scale;
			S[i][2] *= scale;
			
			A[i][0] *= scale;
			A[i][1] *= scale;
			A[i][2] *= scale;
			}
		
		Histogram h = new Histogram();
		h.set(u_list, 20, min, max);
		h.toFile(new File(output_file + "_unorm_histogram.txt"));
		
		System.out.println("done.");
		
		//6. Warp and output spheres for scalp, inner skull, and outer skull
		System.out.print("Constructing meshes..");
		float scale = 1f / options.sample_rate;
		Mesh3D[] meshes = getSkullAndScalpMeshes(hull_mesh, S, scale, options.center_of_mass, tagged);
		String[] suffixes = new String[]{"_inner_skull.tri", "_outer_skull.tri", "_scalp.tri"};
		System.out.println("done.");
		
		boolean success = true;
		
		System.out.print("Writing to '" + output_file + "_xxx.obj..");
		for (int m = 0; m < 3; m++){
			File out = new File(output_file + suffixes[m]);
			Mesh3DWriter writer = new Mesh3DWriter(out);
			success &= writer.writeSurface(new Mesh3DInt(meshes[m]));
			}
		if (avr_out){
			System.out.print("Constructing average meshes..");
			Mesh3D[] avr_meshes = getAverageSkullAndScalpMeshes(hull_mesh, A, scales, options.center_of_mass);
			suffixes = new String[]{"_avr_A1.tri", "_avr_A2.tri", "_avr_A3.tri"};
			for (int m = 0; m < 3; m++){
				File out = new File(output_file + suffixes[m]);
				Mesh3DWriter writer = new Mesh3DWriter(out);
				success &= writer.writeSurface(new Mesh3DInt(avr_meshes[m]));
				}
			System.out.println("done.");
			}
		if (success)
			System.out.println("Success.");
		else
			System.out.println("Failure.");
		
		return success;
	}
	
	static int[][] getSkullAndScalpIndices(int[][] A,
	   		   		 ArrayList<ArrayList<Double>> subject_rays){
	
		int P = subject_rays.size();
		int[][] N = new int[P][4];
		int[][] S = new int[P][3];
		
		for (int j = 0; j < P; j++){
			S[j][2] = 100;		//scalp surface is end of this ray
			ArrayList<Double> ray = subject_rays.get(j);
			//int M = subject_rays.size();
			int M = ray.size();
			//1. Determine N1 as max_edge( {A3, A4} ).
			N[j][0] = getMaxEdge(ray, A[j][2], A[j][3], false);
			//2. Determine N2 as min_edge( {A2, N1} ).
			N[j][1] = getMinEdge(ray, A[j][1], N[j][0], false);
			//3. Determine S_outer as first x to left of N1 where y(x) = average(y(N2),y(N1)).
			S[j][1] = searchRayLeft(ray, N[j][0], 0, (ray.get(N[j][0]) + ray.get(N[j][1])) / 2.0);
			//4. If N2 = 0 set S_inner to N2.
			if (N[j][0] == 0){
				S[j][0] = 0;
			}else{
				//a. Determine N3 as max_edge_left( {0, N2} ).
				N[j][2] = getMaxEdge(ray, 0, N[j][1], true);
				//b. Determine N4 as min_edge_left( {N3, N2} ).
				N[j][3] = getMinEdge(ray, N[j][2], N[j][1], true);
				//c. Determine S_inner as first x to right of N3 where y(x) = average(y(N3),y(N4)).
				S[j][0] = searchRayRight(ray, N[j][2], M - 1, (ray.get(N[j][2]) + ray.get(N[j][3])) / 2.0);
				}
			}
		
		return S;
		
	}


	
	public static void writeRayToFile(ArrayList<Double> ray, String output_file){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(output_file));
			writer.write(getDelimitedTestLine(ray));
			writer.close();
		}catch (IOException e){
			e.printStackTrace();
			}
		
	}
	
	/*************************************
	 * Returns average meshes for the control points in <code>A</code>. Returns an array:
	 * 
	 * 0: A1 surface
	 * 1: A2 surface
	 * 2: A3 surface
	 * 
	 * @param hull_mesh
	 * @param A
	 * @param resample_scales
	 * @param center_pt
	 * @return
	 */
	public static Mesh3D[] getAverageSkullAndScalpMeshes(Mesh3D hull_mesh,
													   	 int[][] A,
													   	 ArrayList<Double> resample_scales,
													   	 Point3f center_pt){

		Mesh3D[] meshes = new Mesh3D[3];
		
		//1. Translate each vertex along its center-vertex ray by values in A
		for (int m = 0; m < 3; m++){
			Mesh3D mesh = new Mesh3D(hull_mesh);
			meshes[m] = mesh;
			for (int i = 0; i < mesh.n; i++){
				Point3f p = hull_mesh.getVertex(i);
				Vector3f v = new Vector3f(p);
				v.sub(center_pt);
				v.normalize();
				v.scale((float)(resample_scales.get(i) * A[i][m]));
				p.add(v);
				mesh.setVertex(i, p);
				}
			}
		
		return meshes;
	
	}
	
	//use st. dev of each ray's neighbours to smooth it if necessary
	static int[][] smoothAverageRays(int[][] A,
									 Mesh3D mesh,
									 ScalpAndSkullModelOptions options){
		
		
		float scale = 1f / options.sample_rate;
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		int[][] A2 = new int[mesh.n][4];
		float[] maxes = new float[4];
		int[] max_idx = new int[4];
		
		for (int m = 0; m < 4; m++){
			//maxes[m] = -Float.MAX_VALUE;
			//max_idx[m] = -1;
			ArrayList<Float> A_i = new ArrayList<Float>(mesh.n);
			for (int i = 0; i < mesh.n; i++)
				A_i.add((float)A[i][m] * scale);
			
			for (int i = 0; i < mesh.n; i++){
				float distance2 = correctSphereNodeDistance(i, n_mesh, A_i, 
															options.threshold_stdev, 
															options.correction_stdev);
				//if (distance2 > maxes[m]){
				//	maxes[m] = distance2;
				//	max_idx[m] = i;
				//	}
				A2[i][m] = (int)Math.round(distance2 / scale);
				}
			
			
			}
		
		//for (int m = 0; m <4; m++){
		//	System.out.println("Max distance [" + max_idx[m] + "]: " + maxes[m]);
		//	}
		
		return A2;
		
	}
	
	static Mesh3D[] getSkullAndScalpMeshes(Mesh3D hull_mesh,
										   int[][] S,
										   float scale,
										   Point3f center_pt,
										   ArrayList<Boolean> tagged){
		
		Mesh3D[] meshes = new Mesh3D[3];
		
		
		//1. Translate each vertex along its center-vertex ray by values in S
		for (int m = 0; m < 3; m++){
			Mesh3D mesh = new Mesh3D(hull_mesh);
			meshes[m] = mesh;
			for (int i = 0; i < mesh.n; i++){
				Point3f p = hull_mesh.getVertex(i);
				Vector3f v = new Vector3f(p);
				v.sub(center_pt);
				v.normalize();
				v.scale(scale * S[i][m]);
				p.add(v);
				mesh.setVertex(i, p);
				}
			}
		
		//if nodes are tagged (deviate to far from average), set them to mean of neighbours 
		if (tagged != null){
			NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(hull_mesh);
			for (int m = 0; m < 3; m++){
				Mesh3D mesh = meshes[m];
				for (int i = 0; i < mesh.n; i++){
					if (tagged.get(i)){
						int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
						if (nbrs.length > 0){
							Point3f p = new Point3f(mesh.getVertex(nbrs[0]));
							for (int j = 1; j < nbrs.length; j++)
								p.add(mesh.getVertex(nbrs[j]));
							p.scale(1f/(float)nbrs.length);
							mesh.setVertex(i, p);
							}
						}
					}
				}
			}
		
		return meshes;
		
	}
	
	public static int[][] loadAverageRays(String average_ray_file, ArrayList<Double> scales){
		return loadAverageRays(average_ray_file, false, null, null, scales);
	}
	
	public static int[][] loadAverageRays(String average_ray_file, boolean smooth, 
										  ScalpAndSkullModelOptions options, Mesh3D mesh,
										  ArrayList<Double> scales){
		
		ArrayList<ArrayList<Double>> average_rays = new ArrayList<ArrayList<Double>>();
		File afile = new File(average_ray_file);
		if (!afile.exists()){
			System.out.println("File '" + average_ray_file + "' not found..");
			return null;
			}
		boolean debug = false;
		if (options.debug_output_dir != null){
			File out = new File(options.debug_output_dir);
			if (!out.exists() && !out.mkdir())
				System.out.println("NeuroMeshFunctions.loadAverageRays: cannot create debug dir '" + 
									options.debug_output_dir +"'..");
			else
				debug = true;
			}
		try{
			BufferedReader reader = new BufferedReader(new FileReader(afile));
			String line = reader.readLine();
			int i = 0;
			while (line != null){
				ArrayList<Double> ray = new ArrayList<Double>();
				average_rays.add(ray);
				StringTokenizer tokens = new StringTokenizer(line);
				if (tokens.hasMoreTokens())
					scales.add(Double.valueOf(tokens.nextToken()));
				while (tokens.hasMoreTokens())
					ray.add(Double.valueOf(tokens.nextToken()));
				line = reader.readLine();
				if (debug)
					writeRayToFile(ray, options.debug_output_dir + File.separator + "average_ray_" + i + ".txt");
				i++;
				}
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}
		
		int[][] A = NeuroMeshFunctions.getAverageRayPoints(average_rays);
		if (smooth)
			A = smoothAverageRays(A, mesh, options);
		
		return A;
		
	}
	
	
	public static int[][] getAverageRayPoints(ArrayList<ArrayList<Double>> average_rays){
		int P = average_rays.size();
		int[][] A = new int[P][5];
		
		//For each ray in average_rays
		for (int i = 0; i < P; i++){
			ArrayList<Double> ray = average_rays.get(i);
			//int M = average_rays.size();
			int M = ray.size();
			//1. Determine A1 as max_edge( {M/2, M} ).
			A[i][0] = getMaxEdge(ray, M / 2, M - 1, false);
			//2. Determine A2 as min_edge( {0, A1} ).
			A[i][1] = getMinEdge(ray, 0, A[i][0], false);
			//3. Determine A3 as first x to left of A1 where y(x) = average(y(A1),y(A2)).
			A[i][2] = searchRayLeft(ray, A[i][0], A[i][1], (ray.get(A[i][0]) + ray.get(A[i][1])) / 2.0);
			//4. Determine A4 as first x to right of A1 where y(x) = y(A3), or M if not found.
			A[i][3] = searchRayRight(ray, A[i][0], M - 1, ray.get(A[i][2]));
			//if (A[i][3] < 0) A[i][3] = M - 1;
			A[i][4] = Math.round((float)A[i][1] / 2f);
			}
		
		return A;
	}
	
	
	
	/************************************
	 * Searches a sample ray from <code>start</code> to <code>end</code>, where <code>start</code> > <code>end</code>,
	 * and returns the first point where <code>value</code> if encountered, the rightmost of two data points between 
	 * which <code>value</code> is intermediate.  
	 * 
	 * @param ray A set of samples along a ray
	 * @param start The start index
	 * @param end The end index
	 * @param value The search value
	 * @return the index, -1 if <code>start</code> < <code>end</code>, or <code>end</code> if <code>value</code> was
	 * not found
	 */
	static int searchRayLeft(ArrayList<Double> ray, int start, int end, double value){
		
		if (start < end) 
			return -1;
		if (start == end) 
			return start;
		
		double v0 = ray.get(start);
		for (int i = start - 1; i > end; i--){
			double v1 = ray.get(i);
			if (v0 < value && v1 >= value ||
				v1 < value && v0 >= value)
					return i;
			v0 = v1;
			}
		
		return end;
		
		
		//return searchRay(ray, start, end, value, -1);
	}
	
	static int searchRayRight(ArrayList<Double> ray, int start, int end, double value){
		return searchRayRight(ray, start, end, value, -Double.MAX_VALUE, 0, 0, false, null);
	}
	
	/************************************
	 * Searches a sample ray from <code>start</code> to <code>end</code>, where <code>start</code> < <code>end</code>,
	 * and returns the first point where <code>value</code> if encountered, the leftmost of two data points between 
	 * which <code>value</code> is intermediate; OR the point where the line slope is less than <code>slope_threshold</code>.
	 * 
	 * @param ray A set of samples along a ray
	 * @param start The start index
	 * @param end The end index
	 * @param value The search value
	 * @param slope_threshold The minimum slope, beyond which this method returns
	 * @param min_plateau_length The minimum length of consecutive samples at which to define a plateau 
	 * @param max_plateau slope The maximum slope at which to consider a plateau
	 * @param find_last Returns the last occurrence of the search value, rather than the first
	 * @return the index of the first occurrence (or last, if <code>find_last == true</code>), 
	 * 		   -1 if <code>start</code> > <code>end</code>, 
	 * 		   or <code>end</code> if <code>value</code> was not found
	 */
	static int searchRayRight(ArrayList<Double> ray, int start, int end, double value, 
							  double slope_threshold, 
							  int min_plateau_length,
							  double max_plateau_slope,
							  boolean find_last,
							  MguiBoolean has_plateau){
		
		if (start > end) 
			return -1;
		if (start == end) 
			return start;
		
		int plateau_length = 0;
		int last_found = end;
		
		double v0 = ray.get(start);
		for (int i = start + 1; i < end - 1; i++){
			double v1 = ray.get(i);
			//test value
			if (v0 < value && v1 >= value ||
				v1 <= value && v0 > value){
					if (find_last)
						last_found = i;
					else
						return i;
				}
			//test for a plateau
			double m = (v1 - v0);
			if (m < slope_threshold && m < max_plateau_slope){
				plateau_length++;
				if (plateau_length == min_plateau_length){
					if (has_plateau != null)
						has_plateau.setTrue(true);
					return i - min_plateau_length;
					}
			}else{
				plateau_length = 0;
				}
			v0 = v1;
			}
		
		return last_found;
		
		//return searchRay(ray, start, end, value, 1);
	}
	
	static int searchRay(ArrayList<Double> ray, int start, int end, double value, int incr){
		//if (start > end) return -1;
		if (start == end) 
			return start;
			//if (ray.get(start) == value)
			//	return start;
			//else
			//	return -1;
		end += incr;
		if (end < -1) end = -1;
		if (end > ray.size()) end = ray.size();
		
		double v0 = ray.get(start);
		for (int i = start + incr; i != end - incr; i += incr){
			double v1 = ray.get(i);
			if (v0 < value && v1 >= value ||
				v1 < value && v0 >= value)
				return i;
			v0 = v1;
			}
		if (incr > 0)
			return end;
		return start;
	}
	
	/**********************************************
	 * Return the absolute maximum over the interval [<code>start, end</code>].
	 * 
	 * @param ray The set of samples along a ray
	 * @param start The start index of the interval
	 * @param end The end index of the interval
	 * @param first If <code>true</code>, returns the leftmost maximum.
	 * @return
	 */
	static int getMaxEdge(ArrayList<Double> ray, int start, int end, boolean first){
		//degenerate cases
		if (start > end) 
			return -1;
		if (start == end) 
			return start;
		int max = -1;
		//edge case: if first slope is negative, set as max
		double slope = ray.get(start + 1) - ray.get(start);
		if (slope < 0)
			if (first)
				return start;
			else
				max = start;
		boolean was_positive; // = false;
		for (int i = start + 1; i < end; i++){
			was_positive = (slope >= 0);
			slope = ray.get(i) - ray.get(i - 1);
			if (was_positive && slope < 0)
				if (first)
					return i;
				else if (max < 0)
					max = i;
				else if (ray.get(i) > ray.get(max))
					max = i;
			}
		if (max < 0 || ray.get(end) > ray.get(max)) max = end;
		return max;
	}
	
	/**********************************************
	 * Return the absolute minimum over the interval [<code>start, end</code>].
	 * 
	 * @param ray The set of samples along a ray
	 * @param start The start index of the interval
	 * @param end The end index of the interval
	 * @param first If <code>true</code>, returns the leftmost minimum.
	 * @return
	 */
	static int getMinEdge(ArrayList<Double> ray, int start, int end, boolean first){
		//degenerate cases
		if (start > end) 
			return -1;
		if (start == end) 
			return start;
		int min = -1;
		//edge case: if first slope is negative, set as max
		double slope = ray.get(start + 1) - ray.get(start);
		if (slope > 0)
			if (first)
				return start;
			else
				min = start;
		boolean was_negative; // = false;
		for (int i = start + 1; i < end; i++){
			was_negative = (slope <= 0);
			slope = ray.get(i) - ray.get(i - 1);
			if (was_negative && slope > 0)
				if (first)
					return i;
				else if (min < 0)
					min = i;
				else if (ray.get(i) < ray.get(min))
					min = i;
			}
		if (min < 0) min = end;
		return min;
	}
	
	public static ArrayList<ArrayList<Double>> getRayCurves(ScalpAndSkullModelOptions options,
															String subject,
															String output_file){
		return getRayCurves(options, subject, output_file, null, null, null);
	}
	
	public static ArrayList<ArrayList<Double>> getRayCurves(ScalpAndSkullModelOptions options,
															String subject,
															String output_file,
															ArrayList<ArrayList<Double>> unnormalized,
															Mesh3D hull_mesh,
															ArrayList<Double> scales){
		
		Volume3DInt t1_volume = options.t1_volume;
		Grid3D t1_grid = t1_volume.getGrid();
		Mesh3D test_sphere = MeshFunctions.getMeanSphereMesh(options.center_of_mass, 
													 		 options.initial_radius, 
													 		 options.n_nodes, 
													 		 0, 
													 		 1000);
		if (hull_mesh != null)
			hull_mesh.setFromMesh(test_sphere);
		
		BufferedWriter rays_out = null;
		if (output_file != null){
			try{
				System.out.println("Writing rays to '" + output_file + ".txt.");
				rays_out = new BufferedWriter(new FileWriter(output_file + ".txt"));
			}catch (IOException e){
				e.printStackTrace();
				}
		} else {
			System.out.println("Not writing rays to file..");
			}
		
		//set start points (i.e., hull intersection points) array
		ArrayList<Point3f> start_points = new ArrayList<Point3f>();
		ArrayList<ArrayList<Double>> curves = new ArrayList<ArrayList<Double>>();
		
		double data_max = t1_volume.getDataMax();
		double data_min = t1_volume.getDataMin();
		
		//For each vertex in test sphere
		for (int i = 0; i < test_sphere.n; i++){
			//get test vector as radius through node i
			Point3f test_point = new Point3f(test_sphere.getVertex(i));
			Vector3f test_vector = new Vector3f(test_point);
			test_vector.sub(options.center_of_mass);
			test_vector.scale(100000);
			
			//start at intersection with convex hull surface
			Point3f int_pt = MeshFunctions.getIntersectionPoint(options.brain_surface, test_point, test_vector);
			start_points.add(new Point3f(int_pt));
			test_point.set(int_pt);
			if (hull_mesh != null)
				hull_mesh.setVertex(i, new Point3f(int_pt));
			
			//move test_point the minimum distance from the brain surface
			test_vector.normalize();
			test_vector.scale(1f / options.sample_rate);
			Vector3f min_vect = new Vector3f(test_vector);
			min_vect.normalize();
			min_vect.scale(options.min_dist_B_IS);
			test_point.add(min_vect);
			
			//sample voxels along radius
			ArrayList<Double> samples = new ArrayList<Double>();
			double value = Double.NaN;
			if (options.apply_gaussian){
				int[] voxel = t1_grid.getEnclosingVoxel(test_point);
				value = VolumeFunctions.getGaussianSmoothedValue(t1_volume, voxel, 0, test_vector, 
																 options.sigma_normal, 
																 options.sigma_tangent, 
																 options.gaussian_cutoff);
			}else{
				value = t1_volume.getInterpolatedValueAtPoint(test_point);
				}
			if (Double.isInfinite(value) || Double.isNaN(value))
				value = t1_volume.getDataMin();
			
			samples.add(value);
			
			//continue sampling while in bounds
			test_point.add(test_vector);
			boolean in_bounds = t1_grid.getBoundBox().contains(test_point);
			while (in_bounds){
				if (options.apply_gaussian){
					int[] voxel = t1_grid.getEnclosingVoxel(test_point);
					value = VolumeFunctions.getGaussianSmoothedValue(t1_volume, voxel, 0, test_vector, 
																	 options.sigma_normal, 
																	 options.sigma_tangent, 
																	 options.gaussian_cutoff);
				}else{
					value = t1_volume.getInterpolatedValueAtPoint(test_point);
					}
				if (Double.isNaN(value) || Double.isInfinite(value))
					value = t1_volume.getDataMin();
				samples.add(value);
				
				//sample next point
				test_point.add(test_vector);
				in_bounds = t1_grid.getBoundBox().contains(test_point);
				}
			
			//smooth curve with half window the size of sample_rate + 1
			ArrayList<Double> curve = SignalProcessing.smoothCurveMovingAverage(samples, (int)(options.sample_rate * 2) + 1);
			
			//normalize curve
			ArrayList<Double> norm = SignalProcessing.getNormalizedCurve(curve);
			
			//cull curve to scalp
			curve = removeAir(norm, 
							  1.0 / options.sample_rate, 
							  options.min_intensity, 
							  options.max_intensity_dist);
			
			ArrayList<Double> resampled = null;
			double scale = 1.0 / options.sample_rate;
			
			//resample curve and store scale
			if (unnormalized != null)
				unnormalized.add(curve);
			resampled = SignalProcessing.getResampledCurve(curve, options.resample_n, 1);
			scale *= (double)curve.size() / (double)resampled.size();
			
			if (output_file != null)
				try{
					rays_out.write(scale + "\t");
					rays_out.write(getDelimitedTestLine(resampled) + "\n");
				}catch (Exception e){
					e.printStackTrace();
					}
			
			curves.add(resampled);
			if (scales != null) scales.add(scale);
			}
		
		if (rays_out != null)
			try{
				rays_out.close();
			}catch (Exception e){
				e.printStackTrace();
				}
		
		return curves;
	}
	
	public static ArrayList<ArrayList<Double>> getRayCurvesBak(ScalpAndSkullModelOptions options,
									 						String subject,
									 						String output_file,
									 						ArrayList<ArrayList<Double>> unnormalized,
									 						Mesh3D hull_mesh){
		
		Volume3DInt t1_volume = options.t1_volume;
		Grid3D t1_grid = t1_volume.getGrid();
		
		Mesh3D test_sphere = MeshFunctions.getMeanSphereMesh(options.center_of_mass, 
													 		 options.initial_radius, 
													 		 options.n_nodes, 
													 		 0, 
													 		 1000);
		
		if (hull_mesh != null)
			hull_mesh.setFromMesh(test_sphere);
	
		BufferedWriter rays_out = null;
		
		if (output_file != null){
			try{
				//rays_out = new BufferedWriter(new FileWriter(output_file + ".txt"));
				System.out.println("Writing rays to '" + output_file + ".txt.");
				rays_out = new BufferedWriter(new FileWriter(output_file + ".txt"));
				
				//rays_out_resample = new BufferedWriter(new FileWriter(output_file + "_resample.txt"));
				//rays_out_norm = new BufferedWriter(new FileWriter(output_file + "_norm.txt"));
			}catch (IOException e){
				e.printStackTrace();
				}
		} else {
			System.out.println("Not writing rays to file..");
			}
		
		//set start points (i.e., hull intersection points) array
		ArrayList<Point3f> start_points = new ArrayList<Point3f>();
		ArrayList<ArrayList<Double>> curves = new ArrayList<ArrayList<Double>>();
		
		//3. For each vertex in test sphere
		for (int i = 0; i < test_sphere.n; i++){
			//a. Extend along radius to edge of brain mask
			Point3f test_point = new Point3f(test_sphere.getVertex(i));
			Vector3f test_vector = new Vector3f(test_point);
			test_vector.sub(options.center_of_mass);
			test_vector.normalize();
			test_vector.scale(1f / options.sample_rate);
			
			boolean in_bounds = true;
			
			while (in_bounds){
				test_point.add(test_vector);
				in_bounds = t1_grid.getBoundBox().contains(test_point);
				}
			
			Vector3f v = new Vector3f(test_point);
			test_point.set(test_sphere.getVertex(i));
			v.sub(test_point);
			Point3f int_pt = MeshFunctions.getIntersectionPoint(options.brain_surface, test_point, v);
			
			if (int_pt == null) 
				test_point.add(v);
			else
				test_point.set(int_pt);
			
			start_points.add(new Point3f(test_point));
			if (hull_mesh != null)
				hull_mesh.setVertex(i, new Point3f(test_point));
			
			in_bounds = true;
			
			if (!in_bounds){
				test_point.sub(test_vector);
				curves.add(new ArrayList<Double>());
			}else{
				//remove mask
				//t1_grid.mask = null;
				
				//move test_point the minimum distance from the brain surface
				Vector3f min_vect = new Vector3f(test_vector);
				min_vect.normalize();
				min_vect.scale(options.min_dist_B_IS);
				test_point.add(min_vect);
				
				//b. Sample voxels along radius
				Point3f sample_start = new Point3f(test_point);
				ArrayList<Double> samples = new ArrayList<Double>();
				double value = 0;
				if (options.apply_gaussian){
					int[] voxel = t1_grid.getEnclosingVoxel(test_point);
					value = VolumeFunctions.getGaussianSmoothedValue(t1_volume, voxel, 0, test_vector, 
																	 options.sigma_normal, 
																	 options.sigma_tangent, 
																	 options.gaussian_cutoff);
					//value = t1_grid.getScaledValue(value);
				}else{
					value = t1_volume.getInterpolatedValueAtPoint(test_point);
					}
				
				if (Double.isInfinite(value) || Double.isNaN(value)){
					value = t1_volume.getDataMin();
					double test = t1_volume.getInterpolatedValueAtPoint(test_point);
					value += 0;
					}
				
				samples.add(value);
				
				test_point.add(test_vector);
				in_bounds = t1_grid.getBoundBox().contains(test_point);
				int debug_count_int_steps = 0;
				
				//c. Stop sampling when min threshold is > min distance, or at edge of volume
				while (in_bounds){
					if (options.apply_gaussian){
						int[] voxel = t1_grid.getEnclosingVoxel(test_point);
						value = VolumeFunctions.getGaussianSmoothedValue(t1_volume, voxel, 0, test_vector, 
																		 options.sigma_normal, 
																		 options.sigma_tangent, 
																		 options.gaussian_cutoff);
						//value = t1_grid.getScaledValue(value);
					}else{
						value = t1_volume.getInterpolatedValueAtPoint(test_point);
						}
					
					if (Double.isNaN(value) || Double.isInfinite(value))
						value = t1_volume.getDataMin();
					samples.add(value);
					
					//sample next point
					test_point.add(test_vector);
					in_bounds = t1_grid.getBoundBox().contains(test_point);
					}
				
				//d. Determine vertex locations for inner skull, outer skull, and scalp
				
				//smooth curve with half window the size of sample_rate + 1
				ArrayList<Double> curve = SignalProcessing.smoothCurveMovingAverage(samples, (int)(options.sample_rate * 2) + 1);
				
				//remove air outside scalp
				ArrayList<Double> norm = SignalProcessing.getNormalizedCurve(curve); 
				curve = removeAir(norm, 
								  1.0 / options.sample_rate, 
								  options.min_intensity, 
								  options.max_intensity_dist);
				
				ArrayList<Double> resampled = null;
				double scale = 0;
				
				if (curve.size() > 0){
					if (unnormalized != null)
						unnormalized.add(curve);
					
					//resample to fixed interval
					resampled = SignalProcessing.getResampledCurve(curve, 100, 1);
					scale = (double)curve.size() / (double)resampled.size();
				}else{
					resampled = new ArrayList<Double>();
					for (int k = 0; k < 100; k++)
						resampled.add(0.0);
					}
					
				if (output_file != null)
					try{
						//rays_out.write(getDelimitedTestLine(curve) + "\n");
						//rays_out_resample.write(getDelimitedTestLine(resampled) + "\n");
						//rays_out_norm.write(getDelimitedTestLine(norm) + "\n");
						rays_out.write(scale + "\t");
						rays_out.write(getDelimitedTestLine(resampled) + "\n");
					}catch (Exception e){
						e.printStackTrace();
						}
				
				//scalp_distances.add((float)curve.size() * test_vector.length());
				//curves.add(curve);
				curves.add(resampled);
				
				
				}
			}
		
		if (rays_out != null)
			try{
				//rays_out_resample.close();
				//rays_out_norm.close();
				rays_out.close();
			}catch (Exception e){
				
			}
		
		return curves;
		
	}
	
	
	
	public static boolean modelScalpAndSkullJob(final ScalpAndSkullModelOptions options, final InterfaceProgressBar progress_bar){
		return modelScalpAndSkullJob(options, progress_bar, "");
	}
	
	public static boolean modelScalpAndSkullJob(final ScalpAndSkullModelOptions options, 
												final InterfaceProgressBar progress_bar,
												final String subject){
		return modelScalpAndSkullJob(options, progress_bar, subject, true);
	}
	
	public static boolean modelScalpAndSkullJob(final ScalpAndSkullModelOptions options, 
												final InterfaceProgressBar progress_bar,
												final String subject,
												final boolean set_points){
		if (progress_bar != null){
			progress_bar.progressBar.setMinimum(0);
			progress_bar.progressBar.setMaximum(options.n_nodes * 4);
			progress_bar.setValue(0);
			}
		
		/**********************
		 * Steps:
		 * 1. Get mask union
		 * 2. Create spheres
		 * 3. For each vertex in test sphere
		 * 		a. Extend along radius to edge of brain mask
		 * 		b. Sample voxels along radius
		 * 		c. Stop sampling when min threshold is > min distance, or at edge of volume
		 * 		d. Determine vertex locations for inner skull, outer skull, and scalp
		 * 4. Smooth resulting surfaces with Laplacian
		 * 
		 */
		
		//1. Get mask union
		Volume3DInt t1_volume = options.t1_volume;
		Grid3D t1_grid = t1_volume.getGrid();
		boolean[][][] brain_mask = t1_volume.getMask(options.brain_mask);
		boolean is_above = false;
		Plane3D plane = options.ear_nasium_plane;
		
		int count = 0;
		int x_size = t1_grid.getSizeS();
		int y_size = t1_grid.getSizeT();
		int z_size = t1_grid.getSizeR();
		
		//copy mask
		boolean[][][] mask = new boolean[x_size][y_size][z_size];
		for (int i = 0; i < x_size; i++)
			for (int j = 0; j < y_size; j++)
				for (int k = 0; k < z_size; k++){
					mask[i][j][k] = brain_mask[i][j][k];
					if (mask[i][j][k]) count++;
					}
					
		System.out.println("Brain mask; masked: " + count);
		ShapeFunctions.unionMaskVolumeWithPlane(brain_mask, t1_volume, plane, is_above);
		
		//2. Create spheres
		Mesh3D[] spheres = new Mesh3D[3];
		Mesh3D test_sphere = MeshFunctions.getMeanSphereMesh(options.center_of_mass, 
													 		 options.initial_radius, 
													 		 options.n_nodes, 
													 		 0, 
													 		 1000);
		for (int i = 0; i < 3; i++)
			spheres[i] = new Mesh3D(test_sphere);
		
		//TEST output test lines
		BufferedWriter writer_success_before = null;
		BufferedWriter writer_success_after = null;
		BufferedWriter writer_success_deriv = null;
		BufferedWriter writer_failure = null;
		BufferedWriter writer_mins_deriv = null;
		BufferedWriter writer_maxes_deriv = null;
		BufferedWriter writer_mins_line = null;
		BufferedWriter writer_maxes_line = null;
		try{
			writer_success_before = new BufferedWriter(new FileWriter(
				new File("C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Modelling\\neuro model\\success_lines_before_" + subject + ".txt")));
			writer_success_after = new BufferedWriter(new FileWriter(
				new File("C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Modelling\\neuro model\\success_lines_after_" + subject + ".txt")));
			writer_success_deriv = new BufferedWriter(new FileWriter(
				new File("C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Modelling\\neuro model\\success_lines_deriv_" + subject + ".txt")));
			writer_failure = new BufferedWriter(new FileWriter(
				new File("C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Modelling\\neuro model\\failure_lines_" + subject + ".txt")));
			
			writer_mins_deriv = new BufferedWriter(new FileWriter(
				new File("C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Modelling\\neuro model\\mins_deriv_" + subject + ".txt")));
			writer_maxes_deriv = new BufferedWriter(new FileWriter(
				new File("C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Modelling\\neuro model\\maxes_deriv_" + subject + ".txt")));
			writer_mins_line = new BufferedWriter(new FileWriter(
				new File("C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Modelling\\neuro model\\mins_line_" + subject + ".txt")));
			writer_maxes_line = new BufferedWriter(new FileWriter(
				new File("C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Modelling\\neuro model\\maxes_line_" + subject + ".txt")));
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
		
		ArrayList<Point3f> start_points = new ArrayList<Point3f>();
		ArrayList<ArrayList<Double>> curves = new ArrayList<ArrayList<Double>>();
		ArrayList<Float> scalp_distances = new ArrayList<Float>();
		float[] averages = new float[2];
		
		int avr_count = 0;
		int p_count = 0;
		
		//3. For each vertex in test sphere
		for (int i = 0; i < test_sphere.n; i++){
			//a. Extend along radius to edge of brain mask
			Point3f test_point = new Point3f(test_sphere.getVertex(i));
			Vector3f test_vector = new Vector3f(test_point);
			test_vector.sub(options.center_of_mass);
			test_vector.normalize();
			test_vector.scale(1f / options.sample_rate);
			
			boolean in_bounds = true;
			//t1_grid.mask = brain_mask;
			t1_volume.addMask("brain_mask", brain_mask);
			
			while (in_bounds && 
				   !t1_volume.isMaskedAtPoint(test_point)){
				test_point.add(test_vector);
				in_bounds = t1_grid.getBoundBox().contains(test_point);
				}
			
			/*
			while (in_bounds &&
				   MeshFunctions.intersects(options.brain_surface, test_point, test_vector)){
				test_point.add(test_vector);
				in_bounds = t1_grid.getBoundBox().contains(test_point);
				}
			*/
			while (in_bounds){
				test_point.add(test_vector);
				in_bounds = t1_grid.getBoundBox().contains(test_point);
				}
			
			Vector3f v = new Vector3f(test_point);
			test_point.set(test_sphere.getVertex(i));
			v.sub(test_point);
			Point3f int_pt = MeshFunctions.getIntersectionPoint(options.brain_surface, test_point, v);
			
			if (int_pt == null) 
				test_point.add(v);
			else
				test_point.set(int_pt);
			
			start_points.add(new Point3f(test_point));
			
			in_bounds = true;
			
			if (!in_bounds){
				test_point.sub(test_vector);
				spheres[0].setVertex(i, test_point);
				spheres[1].setVertex(i, test_point);
				spheres[2].setVertex(i, test_point);
				curves.add(new ArrayList<Double>());
				
				//System.out.println("Model scalp and skull error: test vector reached edge of volume before mask.." );
				//return false;
			}else{
			
				//remove mask
				t1_volume.removeMask("brain_mask");
				
				//move test_point the minimum distance from the brain surface
				Vector3f min_vect = new Vector3f(test_vector);
				min_vect.normalize();
				min_vect.scale(options.min_dist_B_IS);
				test_point.add(min_vect);
				
				//b. Sample voxels along radius
				Point3f sample_start = new Point3f(test_point);
				ArrayList<Double> samples = new ArrayList<Double>();
				double value = 0;
				if (options.apply_gaussian){
					int[] voxel = t1_grid.getEnclosingVoxel(test_point);
					value = VolumeFunctions.getGaussianSmoothedValue(t1_volume, voxel, 0, test_vector, 
																	 options.sigma_normal, 
																	 options.sigma_tangent, 
																	 options.gaussian_cutoff);
					//value = t1_grid.getScaledValue(value);
				}else{
					value = t1_volume.getInterpolatedValueAtPoint(test_point);
					}
				
				samples.add(value);
				
				test_point.add(test_vector);
				in_bounds = t1_grid.getBoundBox().contains(test_point);
				int debug_count_int_steps = 0;
				
				//c. Stop sampling when min threshold is > min distance, or at edge of volume
				while (in_bounds){
					if (options.apply_gaussian){
						int[] voxel = t1_grid.getEnclosingVoxel(test_point);
						value = VolumeFunctions.getGaussianSmoothedValue(t1_volume, voxel, 0, test_vector, 
																		 options.sigma_normal, 
																		 options.sigma_tangent, 
																		 options.gaussian_cutoff);
						//value = t1_grid.getScaledValue(value);
					}else{
						value = t1_volume.getInterpolatedValueAtPoint(test_point);
						}
					samples.add(value);
					
					//sample next point
					test_point.add(test_vector);
					in_bounds = t1_grid.getBoundBox().contains(test_point);
					}
				
				//d. Determine vertex locations for inner skull, outer skull, and scalp
				
				int n_last = samples.size();
				//smooth curve with half window the size of sample_rate + 1
				ArrayList<Double> curve = SignalProcessing.smoothCurveMovingAverage(samples, (int)(options.sample_rate * 2) + 1);
				
				//remove air outside scalp
				curve = removeAir(curve, 
								  1.0 / options.sample_rate, 
								  options.min_intensity, 
								  options.max_intensity_dist);
				
				scalp_distances.add((float)curve.size() * test_vector.length());
				curves.add(curve);
				
				try{
					writer_success_before.write(getDelimitedTestLine(SignalProcessing.smoothCurveMovingAverage(samples, (int)(options.sample_rate * 2) + 1)) + "\n");
				}catch (Exception e){
					
					}
				
				}
			
			if (progress_bar != null)
				progress_bar.update(i);
			}
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(test_sphere);
		p_count += test_sphere.n;
		
		for (int i = 0; i < test_sphere.n; i++){
			
			Point3f sample_start = start_points.get(i);
			
			Vector3f test_vector = new Vector3f(sample_start);
			test_vector.sub(options.center_of_mass);
			test_vector.normalize();
			test_vector.scale(1f / options.sample_rate);
			
			ArrayList<Double> curve = curves.get(i);
			
			float distance = scalp_distances.get(i);
			float distance2 = correctSphereNodeDistance(i, n_mesh, scalp_distances, options.threshold_stdev, options.correction_stdev);
			
			if (distance != distance2){
				//System.out.println("Line " + i + ": distance " + distance + " != " + distance2);
				int cut_point = (int) (distance2 / (test_vector.length()));
				if (curve.size() > cut_point){
					curve = cullCurve(curve, cut_point);
					curves.set(i, curve);
					}
				}
			 
			float interval = test_vector.length();
			float[] avr = getAverageSkullPoints(i, 
												curve, 
												options.max_skull_int, 
												options.stop_skull_int,
												options.max_skull_width,
												options.min_skull_width,
												interval);
			for (int j = 0; j < averages.length; j++)
				averages[j] += avr[j];
			avr_count++;
				
			//debug: output max and mins
			ArrayList<Double> D1 = SignalProcessing.getDerivative(curve, 1);
			if (D1.size() > 0)	D1.add(0, D1.get(0));
			D1 = SignalProcessing.smoothCurveMovingAverage(D1, (int)(options.sample_rate * 2) + 1);
			ArrayList<Integer> mins = getLocalMinima(D1, Double.MAX_VALUE);
			ArrayList<Integer> maxes = getLocalMaxima(D1, options.min_slope);
			ArrayList<Integer> line_mins = getLocalMinima(curve, Double.MAX_VALUE);
			ArrayList<Integer> line_maxes = getLocalMaxima(curve, -Double.MAX_VALUE);
			
			if (progress_bar != null)
				progress_bar.update(p_count + i);
			
			try{
				
				writer_success_after.write(getDelimitedTestLine(curve) + "\n");
				writer_success_deriv.write(getDelimitedTestLine(SignalProcessing.smoothCurveMovingAverage(
																SignalProcessing.getDerivative(curve, 1), (int)(options.sample_rate * 2) + 1)) + "\n");

				writer_mins_deriv.write(getDelimitedTestLineInt(mins) + "\n");
				writer_maxes_deriv.write(getDelimitedTestLineInt(maxes) + "\n");
				writer_mins_line.write(getDelimitedTestLineInt(line_mins) + "\n");
				writer_maxes_line.write(getDelimitedTestLineInt(line_maxes) + "\n");
				
			}catch (IOException e){
				e.printStackTrace();
				return false;
				}
			}
		
		boolean[][] failed = new boolean[3][test_sphere.n];
		ArrayList<Integer> failures = new ArrayList<Integer>();
		
		for (int i = 0; i < averages.length; i++)
			averages[i] /= avr_count;
		
		ArrayList<ArrayList<Float>> distances = new ArrayList<ArrayList<Float>>();
		distances.add(new ArrayList<Float>());
		distances.add(new ArrayList<Float>());
		
		if (set_points){
		
		for (int i = 0; i < test_sphere.n; i++){
			
			Point3f test_point = new Point3f(test_sphere.getVertex(i));
			Vector3f test_vector = new Vector3f(test_point);
			test_vector.sub(options.center_of_mass);
			test_vector.normalize();
			test_vector.scale(1f / options.sample_rate);
			
			Point3f sample_start = start_points.get(i);
			ArrayList<Double> curve = curves.get(i);
			
			//set skull and scalp points
			if (!setSkullAndScalpPoints(i, averages, curve, spheres, sample_start, options.center_of_mass,
								   		test_vector, options.min_slope, options.min_dist_OS_S, 
								   		options.max_skull_int, options.stop_skull_int, 
								   		options.max_skull_width, options.min_skull_width,
								   		options.gradient_AP_A, options.gradient_IS_S,
								   		options.min_dist_B_IS,
								   		failed,
								   		options.sample_rate,
								   		distances)){
				failures.add(i);
				}
			
			if (progress_bar != null)
				progress_bar.update(p_count + i);
			}
		
		//update failed nodes
		for (int s = 0; s < 2; s++){
			p_count += test_sphere.n;	
			for (int i = 0; i < test_sphere.n; i++){
				float distance = correctSphereNodeDistance(i, n_mesh, distances.get(s), options.threshold_stdev, options.correction_stdev);
				
				Point3f test_point = new Point3f(start_points.get(i));
				Vector3f test_vector = new Vector3f(test_point);
				test_vector.sub(options.center_of_mass);
				test_vector.normalize();
				test_vector.scale(distance);
				//test_point.set(options.center_of_mass);
				test_point.add(test_vector);
				
				spheres[s].setVertex(i, test_point);
				
				if (progress_bar != null)
					progress_bar.update(p_count + i);
				}
					
			}
		
		/*
		for (int i = 0; i < 3; i++){
			if (!fixFailedNodes(spheres[i], failed[i], options.center_of_mass, 3, progress_bar))
				System.out.println("Skull and scalp modelling: Could not fix sphere " + i +"..");
			}
		*/
		
		options.shape_set.addShape(new Mesh3DInt(spheres[0], "inner_skull_surface"));
		options.shape_set.addShape(new Mesh3DInt(spheres[1], "outer_skull_surface"));
		options.shape_set.addShape(new Mesh3DInt(spheres[2], "scalp_surface"));
		
		}
		
		//restore mask
		/*
		for (int i = 0; i < t1_grid.xSize; i++)
			for (int j = 0; j < t1_grid.ySize; j++)
				for (int k = 0; k < t1_grid.zSize; k++)
					brain_mask[i][j][k] = mask[i][j][k];
		*/
		
		System.out.println("Skull and scalp modelling finished with " + failures.size() + " failures.");
		
		try{
			writer_failure.close();
			writer_success_after.close();
			writer_success_before.close();
			writer_success_deriv.close();
			writer_mins_deriv.close();
			writer_maxes_deriv.close();
			writer_mins_line.close();
			writer_maxes_line.close();
			
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return failures.size() == 0;
	}
	
	static float correctSphereNodeDistance(int index,
									 	   NeighbourhoodMesh n_mesh,
									 	   ArrayList<Float> distances, 
									 	   float threshold_stdevs,
									 	   float new_stdev){
		
		//get mean, stdev of neighbours
		//if distance is > threshold_stdev from mean,
		//reduce this to new_stdev
		
		int[] nbrs = n_mesh.getNeighbourhood(index).getNeighbourList();
		
		float distance = distances.get(index);
		if (nbrs.length == 0) return distance;
		
		float mean = 0;
		for (int i = 0; i < nbrs.length; i++)
			mean += distances.get(nbrs[i]);
		
		mean /= nbrs.length;
		
		float s_squares = 0;
		for (int i = 0; i < nbrs.length; i++)
			s_squares += Math.pow((distances.get(nbrs[i]) - mean), 2);
		
		float st_dev = (float)Math.sqrt(s_squares / nbrs.length);
		float st_dev_i = (float)Math.abs(distances.get(index) - mean);
		
		//only change if this node is the most deviant of its neighbourhood
		for (int i = 0; i < nbrs.length; i++){
			float st_dev_nbr = (float)Math.abs(distances.get(nbrs[i]) - mean);
			if (st_dev_nbr > st_dev_i) return distance;
			}
		
		float sign = 1;
		if (distance < mean) sign = -1;
		
		if (st_dev_i / st_dev > threshold_stdevs){
			//System.out.print("Corrected distance [" + index + "]: " + distance + " ->");
			distance = mean + (sign * new_stdev);
			//System.out.println(" " + distance + "; stdev_i = " + (st_dev_i / st_dev));
			}
			
		return distance;
	}
	
	public static boolean modelScalpAndSkull(final ScalpAndSkullModelOptions options, final InterfaceProgressBar progress_bar){
		
		if (progress_bar != null)
			progress_bar.register();
		
		boolean b = (Boolean)Worker.post(new Job(){
			
			public Boolean run(){
				
				return modelScalpAndSkullJob(options, progress_bar);
			}
			
		});
		
		if (progress_bar != null)
			progress_bar.deregister();
		
		return b;
		
	}
	
	static float[] getAverageSkullPoints(int index, 
									  	 ArrayList<Double> curve,
									  	 double max_skull_int,
									  	 double stop_skull_int,
									  	 float max_skull_width,
									  	 float min_skull_width,
									  	 float interval){
		
		int first = 0, last = -1, stop = -1;
		boolean done = false;
		
		for (int i = 0; i < curve.size() && !done; i++){
			done = (interval * (float)(i - first) > max_skull_width);
			if (first > 0){
				if (!done){
					if (curve.get(i) > max_skull_int && last < 0)
						last = i - 1;
					if (curve.get(i) < max_skull_int)
						last = -1;
					if (curve.get(i) > stop_skull_int && stop < 0)
						if (last > 0)
							stop = last;
						else
							stop = i - 1;
				}else if (last < 0){
					last = i - 1;
					}
			}else if (curve.get(i) < max_skull_int){
				first = i;
				}
			}
		
		if (first < -1)
			first = 0;
		
		if (stop > -1)
			last = stop;
		
		if (interval * (float)(last - first) < min_skull_width)
			last = (int)Math.ceil(min_skull_width / interval) + first;
		
		//System.out.println("First: " + first + ", last: " + last);
		
		return new float[]{(float)first / (float)curve.size(), (float)last / (float)curve.size()};
		
	}
	
	/************************************************
	 * Updates <code>spheres</code> - ordered as inner skull, outer skull, scalp - with the given
	 * set of samples <code>samples</code>, taken along the line through the <code>i</code>th sphere node,
	 * starting at the brain surface and ending at the scalp (i.e., trimmed with <code>removeAir()</code>).
	 * See MSc. thesis, Rianne Hupse, 2006
	 * 
	 * @param index
	 * @param samples
	 * @param spheres
	 * @param start
	 * @param test_vector
	 * @param min_slope
	 * @param min_scalp
	 * 
	 */
	static boolean setSkullAndScalpPoints(int index,
										   float[] average_points,
										   ArrayList<Double> curve, 
										   Mesh3D[] spheres, 
										   Point3f start,
										   Point3f center,
										   Vector3f test_vector,
										   double min_slope,
										   float min_scalp,
										   double max_skull_int,
										   double stop_skull_int,
										   float max_skull_width,
										   float min_skull_width,
										   float gradient_AP_A,
										   float gradient_IS_S,
										   float min_dist_B_IS,
										   boolean[][] failed,
										   float sample_rate,
										   ArrayList<ArrayList<Float>> distances){
		
		if (curve.size() == 0){
			System.out.println("Skull and scalp modelling: Curve " + index + " has zero length (scalp detection failed)..");
			failed[0][index] = true;
			failed[1][index] = true;
			failed[2][index] = true;
			return false;
			}
		
		Point3f p = new Point3f(start);
		Vector3f v = new Vector3f(test_vector);
		
		//first set node for scalp
		v.scale(curve.size());
		p.add(v);
		spheres[2].setVertex(index, p);
		
		//obtain first derivative
		ArrayList<Double> D1 = SignalProcessing.getDerivative(curve, 1);
		//insert element to keep curve sizes equal
		D1.add(0, D1.get(0));
		D1 = SignalProcessing.smoothCurveMovingAverage(D1, (int)(sample_rate * 2) + 1);
		
		//System.out.println("Setting vector " + index);
		
		//float length = test_vector.length() * curve.size();
		
		for (int i = 0; i < 2; i++){
			int average_outer = (int)(average_points[i] * curve.size());
			p = new Point3f(center);
			v = new Vector3f(test_vector);
			v.scale(average_outer);
			//Vector3f v2 = new Vector3f(start);
			//v2.sub(center);
			v = adjustWithGradients(v, gradient_AP_A, gradient_IS_S);
			//v.add(v2);
			distances.get(i).add(v.length());
			//p.add(v2);
			//p.add(v);
			//spheres[i].setNode(index, p);
			}
		
		
		/*
		//obtain local extrema
		ArrayList<Integer> mins = getLocalMinima(D1, Double.MAX_VALUE);
		ArrayList<Integer> maxes = getLocalMaxima(D1, min_slope);
		ArrayList<Integer> line_mins = getLocalMinima(curve, Double.MAX_VALUE);
		ArrayList<Integer> line_maxes = getLocalMaxima(curve, -Double.MAX_VALUE);
		
		
		v.set(test_vector);
		v.normalize();
		v.scale(min_dist_B_IS);
		v = adjustWithGradients(v, gradient_AP_A, gradient_IS_S);
		int start_i = (int) (v.length() * sample_rate);
		max_skull_width = (float)adjustWithGradients(max_skull_width, v, gradient_AP_A, gradient_IS_S);
		min_skull_width = (float)adjustWithGradients(min_skull_width, v, gradient_AP_A, gradient_IS_S);
		
		float interval = test_vector.length();
		//p.set(start);
		
		//System.out.println("Max skull width: " + max_skull_width);
		
		//watershed strategy: find bounds of region < max_skull_int
		int first = start_i, last = -1, stop = -1;
		boolean done = false;
		
		for (int i = start_i; i < curve.size() && !done; i++){
			done = (interval * (float)(i - first) > max_skull_width);
			if (first > start_i){
				if (!done){
					if (curve.get(i) > max_skull_int && last < 0)
						last = i - 1;
					if (curve.get(i) < max_skull_int)
						last = -1;
					if (curve.get(i) > stop_skull_int && stop < 0)
						if (last > 0)
							stop = last;
						else
							stop = i - 1;
				}else if (last < 0){
					last = i - 1;
					}
			}else if (curve.get(i) < max_skull_int){
				first = i;
				i += (int) (min_skull_width * sample_rate);
				}
			}
		
		//stop = Math.min(stop, line_max);
		
		if (first < -1)
			first = 0;
		
		if (stop > -1)
			last = stop;
		
		if (interval * (float)(last - first) < min_skull_width)
			last = (int)Math.ceil(min_skull_width / interval) + first;
		
		if (first > -1){
			v.set(test_vector);
			v.scale(first);
			//p.set(start);
			//p.add(v);
			//Vector3f v2 = new Vector3f(start);
			//v2.sub(center);
			//v2 = adjustWithGradients(v2, gradient_AP_A, gradient_IS_S);
			//v.add(v2);
			//p.set(center);
			//p.add(v);
			//spheres[0].setNode(index, p);
			distances.get(0).add(v.length());
		}else{
			failed[0][index] = true;
			distances.get(0).add(0f);
		}
			
		if (last > -1){
			v.set(test_vector);
			v.scale(last);
			//p.set(start);
			//p.add(v);
			
			//v.set(test_vector);
			//v.scale(last);
			//Vector3f v2 = new Vector3f(start);
			//v2.sub(center);
			//v2 = adjustWithGradients(v2, gradient_AP_A, gradient_IS_S);
			//v.add(v2);
			//p.set(center);
			//p.add(v);
			//p.add(v2);
			//spheres[1].setNode(index, p);
			distances.get(1).add(v.length());
		}else{
			failed[1][index] = true;
			distances.get(1).add(0f);
		}
		
		//if (line_maxes.size() == 0){
		//	failed[0][index] = true;
		//	failed[1][index] = true;
		//	return true;
		//	}
		
		/*
		//find line max
		int line_max = Integer.MIN_VALUE;
		double _max = -Double.MAX_VALUE;
		for (int i = 0; i < line_maxes.size(); i++){
			if (curve.get(line_maxes.get(i)) > _max){
				line_max = line_maxes.get(i);
				_max = curve.get(line_max);
				}
			}
		
		if (line_max > -1){
			v.set(test_vector);
			v.scale(line_max);
			p.set(start);
			p.add(v);
			spheres[1].setNode(index, p);
		}else{
			failed[1][index] = true;
		}
		
		//find line min
		int line_min = Integer.MIN_VALUE;
		double _min = Double.MAX_VALUE;
		for (int i = 0; i < line_mins.size(); i++){
			if (curve.get(line_mins.get(i)) < _min){
				line_min = line_mins.get(i);
				_min = curve.get(line_min);
				}
			}
		
		if (line_min > -1){
			v.set(test_vector);
			v.scale(line_min);
			p.set(start);
			p.add(v);
			spheres[0].setNode(index, p);
		}else{
			failed[0][index] = true;
			}	
		*/
		
		return true;
	}
	
	static Vector3f adjustWithGradients(Vector3f v, float gradient_AP_A, float gradient_IS_S){
		//assume y is AP and z is IS
		Vector3f v2 = new Vector3f(v);
		v2.normalize();
		float scale = 1;
		//if (v2.y > 0 && v2.z > 0){
		//	scale += v2.y * gradient_AP_A;
		//	scale += v2.z * gradient_IS_S;
		//	}
		if (v2.y > 0)
			scale += v2.y * gradient_AP_A;
		if (v2.z > 0)
			scale += v2.z * gradient_IS_S;
		v2.set(v);
		v2.scale(scale);
		return v2;
		
	}
	
	static double adjustWithGradients(double f, Vector3f v, float gradient_AP_A, float gradient_IS_S){
		//assume y is AP and z is IS
		Vector3f v2 = new Vector3f(v);
		v2.normalize();
		float scale = 1;
		if (v2.y > 0)
			scale += v2.y * gradient_AP_A;
		if (v2.z > 0)
			scale += v2.z * gradient_IS_S;
		return scale * f;
		
	}
	
	static int getScalpDistance(ArrayList<Double> curve, float delta_x, double threshold_v, double threshold_dist) {
		
		float distance = 0;
		int cut_point = -1;
		int i = curve.size() - 1;
		boolean is_below = true;
		
		while (distance < threshold_dist && i > 0){
			if (curve.get(i) < threshold_v){
				is_below = true;
				distance = 0;
			}else{
				if (is_below){
					distance = 0;
					cut_point = i ;
					is_below = false;
				}else{
					distance += delta_x;
					}
				}
			i--;
			}
		
		return cut_point;
	}
	
	static ArrayList<Double> cullCurve(ArrayList<Double> curve, int cut_point){
		
		ArrayList<Double> culled = new ArrayList<Double>();
		
		for (int k = 0; k < cut_point; k++)
			culled.add(curve.get(k));
	
		return culled;
	}
	
	//remove the air beyond the scalp; curve should be smoothed for best results
	static ArrayList<Double> removeAir(ArrayList<Double> curve, double delta_x, double threshold_v, double threshold_dist){
		
		double distance = 0;
		int cut_point = -1;
		int i = curve.size() - 1;
		boolean is_below = true;
		
		while (distance < threshold_dist && i > 0){
			if (curve.get(i) < threshold_v){
				is_below = true;
				distance = 0;
			}else{
				if (is_below){
					distance = 0;
					cut_point = i ;
					is_below = false;
				}else{
					distance += delta_x;
					}
				}
			i--;
			}
		
		if (i == 0) return new ArrayList<Double>(curve);
		
		ArrayList<Double> culled = new ArrayList<Double>();
		for (int k = 0; k < cut_point; k++)
			culled.add(curve.get(k));
		
		return culled;
		
	}
	
	static ArrayList<Integer> getLocalMinima(ArrayList<Double> curve, double max){
		
		ArrayList<Integer> mins = new ArrayList<Integer>();
		boolean is_smaller = false;
		int m_start = -1;
		
		for (int i = 1; i < curve.size(); i++){
			if (curve.get(i) < curve.get(i - 1)){
				is_smaller = true;
				m_start = i;
				}
			if (curve.get(i) > curve.get(i - 1))
				if (is_smaller){
					is_smaller = false;
					int v = (i + m_start - 1) / 2;
					if (curve.get(i) < max)
						mins.add(v);
					}
			}
		
		return mins;
	}
	
	static ArrayList<Integer> getLocalMaxima(ArrayList<Double> curve, double min){
		
		ArrayList<Integer> maxes = new ArrayList<Integer>();
		
		boolean is_bigger = false;
		int m_start = -1;
		
		for (int i = 1; i < curve.size(); i++){
			if (curve.get(i) > curve.get(i - 1)){
				is_bigger = true;
				m_start = i;
				}
			if (curve.get(i) < curve.get(i - 1))
				if (is_bigger){
					is_bigger = false;
					int v = (i + m_start - 1) / 2;
					if (curve.get(i) > min)
						maxes.add(v);
					}
			}
				
		return maxes;
	}
	
	static String getDelimitedTestLine(ArrayList<Double> samples){
		
		if (samples.size() == 0) return "";
		
		String line = samples.get(0).toString();
		
		for (int i = 1; i < samples.size(); i++)
			line = line + "\t" + samples.get(i);
	
		return line;
	}
	
	static String getDelimitedTestLineInt(ArrayList<Integer> samples){
		
		if (samples.size() == 0) return "";
		
		String line = samples.get(0).toString();
		
		for (int i = 1; i < samples.size(); i++)
			line = line + "\t" + samples.get(i);
	
		return line;
	}
	
	static boolean fixFailedNodes(Mesh3D mesh, boolean[] failed, Point3f center, int max_itrs, 
								  InterfaceProgressBar progress_bar){
		
		boolean all_good = false;
		boolean success = true;
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		int itrs = 0;
		int p = 0;
		if (progress_bar != null)
			p = progress_bar.progressBar.getValue();
		
		while (!all_good && itrs < max_itrs){
			all_good = true;
			for (int i = 0; i < mesh.n; i++){
				if (failed[i]){
					int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
					if (nbrs.length == 0){
						//isolated node means this mesh is bad 
						success = false;
					}else{
						int good = 0;
						Point3f mp = new Point3f(0, 0, 0);
						for (int n = 0; n < nbrs.length; n++)
							if (!failed[nbrs[n]]){
								good++;
								mp.add(mesh.getVertex(nbrs[n]));
								}
						if (good > 0){
							failed[i] = false;
							mp.scale(1f / (float)good);
							//project mean point onto vector for this node
							Point3f pt = mesh.getVertex(i);
							Vector3f v0 = new Vector3f(pt);
							v0.sub(center);
							v0.normalize();
							Vector3f v1 = new Vector3f(mp);
							v1.sub(center);
							v0 = GeometryFunctions.getProjectedVector(v1, v0);
							pt.add(v0);
							mesh.setVertex(i, pt);
						}else{
							//no good neighbours, we need to reiterate
							all_good = false;
							}
						}
					}
				if (progress_bar != null)
					progress_bar.update(p + i);
				}
			}
		
		return success && itrs < max_itrs;
	}
	
	/**************************************************************
	 * Generates a set of sample rays from an instance of {@link ScalpAndSkullModelOptions}, including a brain hull
	 * surface and a T1-weighted brain image. Two sets of rays are generated:
	 * 
	 * <ol>
	 * <li>From the hull surface to the edge of the image
	 * <li>From the hull surface to the scalp surface
	 * </ol>
	 * 
	 * @param options The parameters used to sample the image
	 * @param rays_1 Sample ray from the hull surface to the edge of the image
	 * @param rays_2 Sample ray from the hull surface to the scalp surface
	 * @param progress Progress updater (can be <code>null</code>)
	 * @throws NeuroMeshFunctionsException
	 */
	public static void getRays(final ScalpAndSkullModelOptions options,
							   final ArrayList<SampleRay> rays_1,
							   final ArrayList<SampleRay> rays_2,
							   final ProgressUpdater progress) throws NeuroMeshFunctionsException {
		
		if (progress == null){
			getRaysBlocking(options, rays_1, rays_2, null);
			return;
			}
		
		if (!(Boolean)Worker.post(new Job(){
			
			public Boolean run(){
				try{
					progress.setMessage("Generating sample rays: ");
					getRaysBlocking(options, rays_1, rays_2, progress);
					return true;
				}catch (NeuroMeshFunctionsException e){
					e.printStackTrace();
					return false;
					}
				}
			
			})){
			throw new NeuroMeshFunctionsException("NeuroMeshFunctions.getRays failed.");
			}
		
	}
	
	/****************************************************************
	 * Generates and fills three sets of sample rays based upon the parameters in <code>options</code>.
	 * ArrayList arguments should already be instantiated.
	 * 
	 * @param options Specifies the source T1, brain mask hull, thresholds, etc.
	 * @param rays_1 Rays from mask hull to T1 boundary
	 * @param rays_2 Rays from mask hull to scalp surface
	 * @throws NeuroMeshFunctionsException
	 */
	public static void getRaysBlocking(ScalpAndSkullModelOptions options,
									   ArrayList<SampleRay> rays_1,
									   ArrayList<SampleRay> rays_2,
									   ProgressUpdater progress) throws NeuroMeshFunctionsException {
		
		Volume3DInt t1_volume = options.t1_volume;
		Grid3D t1_grid = t1_volume.getGrid();
		Mesh3D test_sphere = MeshFunctions.getMeanSphereMesh(options.center_of_mass, 
													 		 options.initial_radius, 
													 		 options.n_nodes, 
													 		 0, 
													 		 1000);
		
		//set start points (i.e., hull intersection points) array
		ArrayList<Point3f> start_points = new ArrayList<Point3f>();
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(test_sphere.n);
			progress.update(0);
			}
		
		int debug;
		
		for (int i = 0; i < test_sphere.n; i++){
			try{
				//get test vector as radius through node i
				Point3f test_point = new Point3f(test_sphere.getVertex(i));
				
				Vector3f test_vector = new Vector3f(test_point);
				test_vector.sub(options.center_of_mass);
				test_vector.scale(100000);
				
				//start at intersection with convex hull surface
				Point3f int_pt = MeshFunctions.getIntersectionPoint(options.brain_surface, test_point, test_vector);
				test_point.set(int_pt);
			
				//move test_point the minimum distance from the brain surface
				test_vector.normalize();
				test_vector.scale(1f / options.sample_rate);
				Vector3f min_vect = new Vector3f(test_vector);
				min_vect.normalize();
				min_vect.scale(options.min_dist_B_IS);
				test_point.add(min_vect);
				int_pt.set(test_point);
				start_points.add(new Point3f(int_pt));
				
				//sample voxels along radius
				ArrayList<Double> samples = new ArrayList<Double>();
				boolean in_bounds = t1_grid.getBoundBox().contains(test_point);
				while (in_bounds){
					double value = Double.NaN;
					if (options.apply_gaussian){
						int[] voxel = t1_grid.getEnclosingVoxel(test_point);
						if (voxel != null)
							value = VolumeFunctions.getGaussianSmoothedValue(t1_volume, voxel, 0, test_vector, 
																			 options.sigma_normal, 
																			 options.sigma_tangent, 
																			 options.gaussian_cutoff);
					}else{
						value = t1_volume.getDatumAtPoint(test_point).getValue();
						}
					if (Double.isNaN(value) || Double.isInfinite(value) || value > t1_volume.getDataMax())
						value = t1_volume.getDataMin();
					samples.add(value);
					
					//sample next point
					test_point.add(test_vector);
					in_bounds = t1_grid.getBoundBox().contains(test_point);
					}
				
				//smooth curve with half window the size of sample_rate + 1
				ArrayList<Double> curve = SignalProcessing.smoothCurveMovingAverage(samples, (int)(options.sample_rate * 2) + 1);
				
				//normalize curve
				//ArrayList<Double> norm = SignalProcessing.getNormalizedCurve(curve);
				double max = -Double.MAX_VALUE;
				for (int j = 0; j < curve.size(); j++){
					max = Math.max(max, curve.get(j));
					}
				ArrayList<Double> norm = SignalProcessing.getNormalizedCurve(curve, t1_volume.getDataMin(), max);
				
				//step test_point back one so it is in bounds
				Vector3f vector = new Vector3f(test_vector);
				vector.scale(-1);
				test_point.add(vector);
				
				//get ray 1
				vector.set(test_point);
				vector.sub(int_pt);
				
				SampleRay ray1 = new SampleRay(new Vector3D(int_pt, vector), norm);
				ray1.resample(100);
				rays_1.add(ray1);
				
				if (i == 885)
					i += 0;
				
				//use curve normalized to volume min and max for scalp determination
				//curve = SignalProcessing.getNormalizedCurve(curve, t1_grid.getDataMin(), t1_grid.getDataMax());
				
				//cull curve to scalp
				curve = removeAir(norm, 
								  1.0 / options.sample_rate, 
								  options.min_intensity, 
								  options.max_intensity_dist);
				
				//get ray 2
				float ratio = (float)curve.size() / (float)norm.size();
				//vector.set(test_vector);
				vector.scale(ratio);
				SampleRay ray2 = new SampleRay(new Vector3D(int_pt, vector), curve);
				ray2.resample(100);
				rays_2.add(ray2);
				if (progress != null)
					progress.update(i);
			}catch (Exception e){
				e.printStackTrace();
				throw new NeuroMeshFunctionsException("Ray sampling failed..");
				}
			}
		
	}
	
	/********************************************************
	 * Generates a vector 3D set from a list of sample rays; adds sample data to shapes.
	 * 
	 * @param rays
	 * @param name
	 */
	public static VectorSet3DInt generateRayShapes(ArrayList<SampleRay> rays, String name){
		
		VectorSet3DInt vector_set = new VectorSet3DInt(name);
		ArrayList<MguiNumber> data = new ArrayList<MguiNumber>();
		
		for (int i = 0; i < rays.size(); i++){
			vector_set.addVector(rays.get(i).getRay());
			ArrayList<Double> samples = rays.get(i).getSamples();
			for (int j = 0; j < samples.size(); j++)
				data.add(new MguiDouble(samples.get(j)));
			}
		
		vector_set.addVertexData("intensity", data);
		return vector_set;
	}
	
	/*******************************************************
	 * Determines a set of control points which can be used to constrain the search for skull and
	 * scalp surfaces. Returned array is n * {A0, A1, A2, A3}.
	 * 
	 * @param rays
	 * @return
	 */
	public static int[][] getRayControlPoints(ArrayList<SampleRay> rays){
		int P = rays.size();
		int[][] A = new int[P][5];
		
		//For each ray in average_rays
		for (int i = 0; i < P; i++){
			ArrayList<Double> ray = rays.get(i).getSamples();
			//int M = average_rays.size();
			int M = ray.size();
			//1. Determine A1 as max_edge( {M/2, M} ).
			A[i][0] = getMaxEdge(ray, M / 2, M - 1, false);
			//2. Determine A2 as min_edge( {0, A1} ).
			A[i][1] = getMinEdge(ray, 0, A[i][0], false);
			//3. Determine A3 as first x to left of A1 where y(x) = average(y(A1),y(A2)).
			A[i][2] = searchRayLeft(ray, A[i][0], A[i][1], (ray.get(A[i][0]) + ray.get(A[i][1])) / 2.0);
			//4. Determine A4 as first x to right of A1 where y(x) = y(A3), or M if not found.
			A[i][3] = searchRayRight(ray, A[i][0], M - 1, ray.get(A[i][2]));
			//5. A5 is mid point between 0 and A2
			A[i][4] = Math.round((float)A[i][1] / 2f);
			//if (A[i][3] < 0) A[i][3] = M - 1;
			}
		
		return A;
	}
	
	/*************************************************************************
	 * Generates a set of indices into the given sample rays, representing control points defining skull and
	 * scalp surfaces. Requires a set of average control points as a constraint. Returns points representing:
	 * 
	 * <ol>
	 * <li>Inner skull
	 * <li>Outer skull
	 * <li>Scalp
	 * </ol>
	 * 
	 * ..and subject control points.
	 * 
	 * @param A Average control points
	 * @param subject_rays Subject sample rays
	 * @param slope_cutoff The proportion of the N1-N2 slope at which to define the outer skull (use
	 * <code>Double.NaN</code>, or a very high value, to bypass this test).
	 * @return Indices representing 1. skull and scalp points and 2. subject control points
	 */
	static ArrayList<int[][]> getSkullAndScalpControlPoints(int[][] A,
	   					   		   		   		 			ArrayList<SampleRay> subject_rays,
	   					   		   		   		 			double plateau_slope,
	   					   		   		   		 			int min_plateau_length,
	   					   		   		   		 			double max_plateau_slope,
	   					   		   		   		 			ArrayList<MguiBoolean> has_plateau){
		
		boolean search_for_plateaus = has_plateau != null;
		
		if (!search_for_plateaus)
			plateau_slope = -Double.MAX_VALUE;
		
		int P = subject_rays.size();
		int[][] N = new int[P][4];
		int[][] S = new int[P][3];
		
		for (int j = 0; j < P; j++){
			S[j][2] = 100;		//scalp surface is end of this ray
			ArrayList<Double> ray = subject_rays.get(j).getSamples();
			
			if (j == 45)
				j += 0;		//breakpoint
			
			//int M = subject_rays.size();
			int M = ray.size();
			//1. Determine N1 as max_edge( {A3, A4} ).
			N[j][0] = getMaxEdge(ray, A[j][2], A[j][3], false);
			//2. Determine N2 as min_edge( {A2, N1} ).
			N[j][1] = getMinEdge(ray, A[j][1], N[j][0], false);
			//3. Determine S_outer as first x to right of N2 where y(x) = average(y(N2),y(N1));
			//   OR where slope = 10% of slope(N2, N1)
			int N1 = N[j][0];
			int N2 = N[j][1];
			double y_N1 = ray.get(N1);
			double y_N2 = ray.get(N2);
			//slope threshold is plateau_slope (as a proportion of N1-N2 slope)
			double slope = (y_N1 - y_N2) / (double)(N1 - N2);
			double slope_threshold = slope * plateau_slope;
			//search value is mean of N1 and N2
			double search_value = (y_N1 + y_N2) / 2.0;
			MguiBoolean _has_plateau = null; 
			if (search_for_plateaus){
				_has_plateau = new MguiBoolean(false);
				has_plateau.add(_has_plateau);
				}
			
			//find last mid-point above z == 0; otherwise find first
			boolean find_last = subject_rays.get(j).getRay().getStart().z > 0;
			S[j][1] = searchRayRight(ray, N2, N1, search_value, 
									 slope_threshold, min_plateau_length, max_plateau_slope,
									 find_last, _has_plateau);
			
			if (S[j][1] < 0)
				j += 0;
			//4. If N2 = 0 set S_inner to N2.
			if (N2 == 0){
				S[j][0] = 0;
			}else{
				S[j][0] = 0;
				/*
				//a. Determine N3 as max_edge_left( {0, N2} ).
				N[j][2] = 0; // getMaxEdge(ray, 0, N[j][1], true);
				//b. Determine N4 as min_edge_left( {N3, N2} ).
				N[j][3] = getMinEdge(ray, N[j][2], N[j][1], false);
				//c. Determine S_inner as first x to right of N3 where y(x) = average(y(N3),y(N4)).
				S[j][0] = searchRayRight(ray, N[j][2], N[j][3], (ray.get(N[j][2]) + ray.get(N[j][3])) / 2.0);
				if (S[j][0] < 0)
					j += 0;
				*/
				}
			}
		
		ArrayList<int[][]> points = new ArrayList<int[][]>();
		points.add(S);
		points.add(N);
		return points;
	}
	
	/*******************************************************
	 * Generates four meshes corresponding to the average control points.
	 * 
	 * @param A average control points
	 * @param rays
	 * @param options
	 * @return
	 */
	public static Mesh3D[] getControlPointMeshes(int[][] A, 
												 ArrayList<SampleRay> rays,
												 ScalpAndSkullModelOptions options){
		
		//get starting meshes
		Mesh3D[] meshes = new Mesh3D[5];
		Mesh3D test_sphere = MeshFunctions.getMeanSphereMesh(options.center_of_mass, 
													 		 options.initial_radius, 
													 		 options.n_nodes, 
													 		 0, 
													 		 1000);
		
		for (int i = 0; i < 5; i++)
			meshes[i] = new Mesh3D(test_sphere);
		
		//for each ray, get points corresponding to control points
		for (int i = 0; i < rays.size(); i++){
			float n = rays.get(i).getSamples().size();
			Vector3D ray = rays.get(i).getRay();
			
			for (int j = 0; j < 5; j++){
				Point3f node = new Point3f(ray.getStart());
				Vector3f vector = new Vector3f(ray.getVector());
				vector.scale((float)A[i][j] / n);
				node.add(vector);
				meshes[j].setVertex(i, node);
				}
			}
		
		return meshes;
	}
	
	/************************************************************
	 * Generates meshes, given the control points; array is ordered as:
	 * 
	 * <ul>
	 * <li>inner skull
	 * <li>outer skull
	 * <li>scalp
	 * </ul>
	 * 
	 * @param A Average control points
	 * @param rays
	 * @return Four meshes, as above
	 */
	public static Mesh3D[] getSkullAndScalpMeshes(int[][] A, ArrayList<SampleRay> scalp_rays, ArrayList<SampleRay> full_rays, ScalpAndSkullModelOptions options){
		
		return getSkullAndScalpMeshes(A, scalp_rays, full_rays, options, null);
		
	}
	
	/************************************************************
	 * Generates meshes, given the control points; array is ordered as:
	 * 
	 * <ul>
	 * <li>inner skull
	 * <li>outer skull
	 * <li>scalp
	 * <li>control_point_A1
	 * <li>control_point_A2
	 * <li>control_point_A3
	 * <li>control_point_A4
	 * </ul>
	 * 
	 * @param A Average control points
	 * @param rays
	 * @return Four meshes, as above
	 */
	public static Mesh3D[] getSkullAndScalpMeshes(final int[][] A, 
												  final ArrayList<SampleRay> scalp_rays,
												  final ArrayList<SampleRay> full_rays,
												  final ScalpAndSkullModelOptions options,
												  final ProgressUpdater updater){
		
		if (updater == null)
			return getSkullAndScalpMeshesBlocking(A, scalp_rays, full_rays, options, null);
		
		return (Mesh3D[])Worker.post(new Job(){
			public Mesh3D[] run(){
				return getSkullAndScalpMeshesBlocking(A, scalp_rays, full_rays, options, updater);
			}
		});
		
	}
	
	
	/************************************************************
	 * Generates meshes, given the control points; array is ordered as:
	 * 
	 * <ol>
	 * <li>inner skull
	 * <li>outer skull
	 * <li>scalp
	 * <li>control_point_A1
	 * <li>control_point_A2
	 * <li>control_point_A3
	 * <li>control_point_A4
	 * </ol>
	 * 
	 * ..and 4 additional meshes for subject control points
	 * 
	 * @param A Average control points
	 * @param rays
	 * @return Four meshes, as above
	 */
	public static Mesh3D[] getSkullAndScalpMeshesBlocking(int[][] A, 
														  ArrayList<SampleRay> scalp_rays,
														  ArrayList<SampleRay> full_rays,
														  ScalpAndSkullModelOptions options,
														  ProgressUpdater updater){
		
		Mesh3D test_sphere = MeshFunctions.getMeanSphereMesh(options.center_of_mass, 
													 		 options.initial_radius, 
													 		 options.n_nodes, 
													 		 0, 
													 		 1000);
		
		if (options.average_neighbour_rays)
			scalp_rays = getNeighbourAveragedSampleRays(test_sphere, scalp_rays, options.average_neighbour_weight);
		
		ArrayList<MguiBoolean> has_plateau = null;
		if (!Double.isNaN(options.plateau_slope))
			has_plateau = new ArrayList<MguiBoolean>();
		
		ArrayList<int[][]> points = getSkullAndScalpControlPoints(A, scalp_rays, 
																  options.plateau_slope, 
																  options.min_plateau_length, 
																  options.max_plateau_slope,
																  has_plateau);
		
		//average all neighbours of "plateau" nodes with their neighbours to smooth
		//transition
		if (has_plateau != null){
			//get all plateau neighbours
			NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(test_sphere);
			ArrayList<Integer> tagged = new ArrayList<Integer>();
			for (int i = 0; i < has_plateau.size(); i++){
				if (has_plateau.get(i).getTrue()){
					int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
					for (int j = 0; j < nbrs.length; j++)
						if (!has_plateau.get(nbrs[j]).getTrue())
							tagged.add(nbrs[j]);
					}
				}
			
			//average plateau neighbours
			for (int i = 0; i < tagged.size(); i++){
				int index = tagged.get(i);
				int[] nbrs = n_mesh.getNeighbourhood(index).getNeighbourList();
				float avr = points.get(0)[index][1];
				for (int j = 0; j < nbrs.length; j++)
					avr += points.get(0)[nbrs[j]][1];
				avr /= (float)(nbrs.length + 1);
				points.get(0)[index][1] = Math.round(avr);
				}
			}
		
		int[][] S = points.get(0);
		int[][] N = points.get(1);
		
		//set all bottom-pointing ray control points to a fixed value for outer skull and scalp
		ArrayList<Integer> bottom_rays = getBottomPointingRays(full_rays, options.bottom_z);
		
		
		for (int i = 0; i < bottom_rays.size(); i++){
			int index = bottom_rays.get(i);
			//distance is relative to scalp ray, so scale
			float dist = Math.round(options.bottom_dist * options.sample_rate);
			dist *= full_rays.get(index).getRay().getLength() / scalp_rays.get(index).getRay().getLength();
			S[index][1] = (int)dist;
			S[index][2] = (int)dist * 2;
		}
		
		if (updater != null){
			updater.setMinimum(0);
			updater.setMaximum(scalp_rays.size());
			updater.update(0);
			}
		
		//Generate meshes
		Mesh3D[] meshes = new Mesh3D[7];
		for (int i = 1; i < 7; i++)
			meshes[i] = new Mesh3D(test_sphere);
		
		Vector3f vector = new Vector3f(), v2 = new Vector3f();
		Point3f point = new Point3f();
		
		for (int i = 0; i < scalp_rays.size(); i++){
			//add to meshes
			vector.set(scalp_rays.get(i).getRay().getVector());
			float scale = vector.length() / scalp_rays.get(i).getSize();
			v2.set(vector);
			if (v2.length() > 0){
				v2.normalize();
				v2.scale(scale);
				v2.scale(S[i][2]);
				}
			point.set(scalp_rays.get(i).getRay().getStart());
			
			//scalp node
			Point3f p = new Point3f(point);
			//p.add(vector);
			p.add(v2);
			meshes[2].setVertex(i, p);
			
			//float scale = vector.length() / scalp_rays.get(i).getSize();
			if (scale < 0 || Double.isNaN(scale))
				i += 0;
			
			//inner skull is simple expansion of hull
			meshes[0] = MeshFunctions.getMeshExpandedAlongNormals(options.brain_surface, options.min_dist_B_IS);
			
			//outer skull
			//for (int j = 1; j < 2; j++){
			v2.set(vector);
			if (v2.length() > 0){
				v2.normalize();
				v2.scale(scale);
				v2.scale(S[i][1]);
				}
			p.set(point);
			p.add(v2);
			//if (Float.isNaN(p.x) || Float.isNaN(p.y) || Float.isNaN(p.z))
			//	j += 0;
			meshes[1].setVertex(i, p);
			//	}
			
			//control point nodes
			for (int j = 0; j < 4; j++){
				v2.set(vector);
				if (v2.length() > 0){
					v2.normalize();
					v2.scale(scale);
					v2.scale(N[i][j]);
					}
				p.set(point);
				p.add(v2);
				if (Float.isNaN(p.x) || Float.isNaN(p.y) || Float.isNaN(p.z))
					j += 0;
				meshes[j + 3].setVertex(i, p);
				}
			
			if (updater != null)
				updater.update(i);
			}
		
		return meshes;
	}
	
	/******************************************************
	 * Returns a list of indices corresponding to rays which project to a point with a z coordinate below
	 * <code>bottom_z</code>.
	 * 
	 * @param rays
	 * @param bottom_z
	 * @return
	 */
	protected static ArrayList<Integer> getBottomPointingRays(ArrayList<SampleRay> rays, float bottom_z){
		
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		for (int i = 0; i < rays.size(); i++){
			if (rays.get(i).getRay().getEndPoint().z <= bottom_z)
				indices.add(i);
			}
		
		return indices;
		
	}
	
	/*****************************************
	 * Writes <code>rays</code> to two tab-delimited text files, one with its vectors:
	 * 
	 * <p>Line 1: [no. vectors]
	 * <br>Line i: [x] [y] [z]
	 *  
	 * <p>and the second rays; one ray per line, of the form:
	 * 
	 * <p>Line i: [sample_1] [sample_2] ... [sample_n]
	 * 
	 * @param rays
	 * @param file
	 */
	public static boolean writeRaysToFile(ArrayList<SampleRay> rays, String file_prefix, int precision) throws IOException{
		
		File file = new File(file_prefix + "_vectors.txt");
		if (file.exists() && !file.delete()){
			System.out.println("NeuroMeshFunctions.writeRaysToFile: Cannot delete existing file '" + file.getAbsolutePath() + "'.");
			return false;
			}
		
		BufferedWriter vector_writer = new BufferedWriter(new FileWriter(file));
		
		file = new File(file_prefix + "_samples.txt");
		if (file.exists() && !file.delete()){
			System.out.println("NeuroMeshFunctions.writeRaysToFile: Cannot delete existing file '" + file.getAbsolutePath() + "'.");
			return false;
			}
		
		BufferedWriter sample_writer = new BufferedWriter(new FileWriter(file));
		
		vector_writer.write("" + rays.size());
		
		for (int i = 0; i < rays.size(); i++){
			SampleRay ray = rays.get(i);
			Vector3f vector = ray.getRay().getVector();
			Point3f start = ray.getRay().getStart();
			vector_writer.write("\n" + start.x + "\t" + start.y + "\t" + start.z);
			vector_writer.write("\t" + vector.x + "\t" + vector.y + "\t" + vector.z);
			ArrayList<Double> samples = ray.getSamples();
			if (i > 0) sample_writer.write("\n");
			for (int j = 0; j < samples.size(); j++){
				if (j > 0) sample_writer.write("\t");
				sample_writer.write(MguiDouble.getString(samples.get(j), precision));
				}
			}
		
		vector_writer.close();
		sample_writer.close();
		return true;
		
	}

	/*************************************************************
	 * Returns a list of sample rays which are a weighted average of the original rays and their neighbours,
	 * as determined by <code>mesh</code>. Contributions of neighbouring samples are weighted by 
	 * <code>neighbour_wieght</code>.
	 * 
	 * @param mesh
	 * @param samples
	 * @param neighbour_weight
	 * @return
	 */
	static ArrayList<SampleRay> getNeighbourAveragedSampleRays(Mesh3D mesh, ArrayList<SampleRay> samples, 
														   	   double neighbour_weight){
	
		ArrayList<SampleRay> averages = new ArrayList<SampleRay>();
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		
		for (int i = 0; i < samples.size(); i++){
			SampleRay thisRay = new SampleRay(samples.get(i));
			int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
			for (int k = 0; k < thisRay.getSize(); k++){
				double value = thisRay.getSample(k);
				for (int j = 0; j < nbrs.length; j++)
					value += samples.get(nbrs[j]).getSample(k) * neighbour_weight;
				value /= (1.0 + ((double)nbrs.length * neighbour_weight));
				thisRay.setSample(k, value);
				}
			averages.add(thisRay);
			}
		
		return averages;
	}
	
	public static ArrayList<MguiNumber> mapGrid3DToCortexGaussian(Mesh3D mesh,
																Volume3DInt volume,
																String channel,
																double sigma_normal,
																double sigma_tangent,
																double sigma_max_normal,
																double sigma_max_tangent,
																double thickness,
																boolean normalize){

	return mapVolumeToCortexGaussian(mesh, volume, channel, 
									 sigma_normal, sigma_tangent, 
									 sigma_max_normal, sigma_max_tangent, 
									 thickness, 
									 normalize, 
									 null,
									 false,
									 null);

	}

	/*********************************** 
	 * Maps values from a <code>Grid3D</code> object to a mesh object representing a middle cortical surface,
	 * by applying a Gaussian 
	 * kernal to voxels in the vicinity of each mesh vertex. The Gaussian is defined in the 
	 * direction normal to the vertex, by <code>sigma_normal</code> (one standard deviation) and 
	 * <code>sigma_max_normal</code> (the distance, in standard deviations, at which to stop 
	 * considering voxels). It is defined in the plane tangent to the normal depending on the 
	 * value of <code>setSigmaT</code>:
	 * <p>
	 * <ul>
	 * <li>"Parameter": <code>sigma_tangent</code> is set to the passed parameter
	 * <li>"From mean area": for each vertex, <code>sigma_tangent</code> is set to sqrt(A_tri_mean 
	 * / Pi)
	 * <li>"From mean length": for each vertex, <code>sigma_tangent</code> is set to the mean length of all edges
	 * connecting the vertex
	 *  </ul>
	 *  The values returned will be those obtained by the <code>Grid3D.getDoubleValue</code> method, but can
	 *  alternatively be normalized by setting the <code>normalized</code> flag.
	 * 
	 * 
	 * @param mesh
	 * @param grid
	 * @param sigma_normal
	 * @param sigma_tangent
	 * @param sigma_max_normal
	 * @param sigma_max_tangent
	 * @param thickness				Either a double representing universal thickness, or an <code>ArrayList</code>,
	 * 								containing vertex-wise thickness values.
	 * @param normalize
	 * @return Vertex-wise mapped values, or <code>null</code> if process fails or was cancelled. 
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<MguiNumber> mapVolumeToCortexGaussian(final Mesh3D mesh,
															  final Volume3DInt volume,
															  final String channel,
															  final double sigma_normal,
															  final double sigma_tangent,
															  final double sigma_max_normal,
															  final double sigma_max_tangent,
															  final Object _thickness,
															  final boolean normalize,
															  final ProgressUpdater progress,
															  final boolean output_matrix,
															  final String matrix_file){
		
		if (progress == null || !(progress instanceof InterfaceProgressBar)) 
			return mapGrid3DToCortexGaussianBlocking(mesh, 
												 volume, 
												 channel, 
												 sigma_normal, 
												 sigma_tangent, 
												 sigma_max_normal, 
												 sigma_max_tangent, 
												 _thickness, 
												 normalize, 
												 progress, 
												 output_matrix, 
												 matrix_file);
		
		return (ArrayList<MguiNumber>)Worker.post(new Job(){
				@Override
				public ArrayList<MguiNumber> run(){
					return mapGrid3DToCortexGaussianBlocking(mesh, 
														 volume, 
														 channel, 
														 sigma_normal, 
														 sigma_tangent, 
														 sigma_max_normal, 
														 sigma_max_tangent, 
														 _thickness, 
														 normalize, 
														 progress, 
														 output_matrix, 
														 matrix_file);
				}
			});
		
	}
	
	static ArrayList<MguiNumber> mapGrid3DToCortexGaussianBlocking(final Mesh3D mesh,
															   final Volume3DInt volume,
															   final String channel,
															   final double sigma_normal,
															   final double sigma_tangent,
															   final double sigma_max_normal,
															   final double sigma_max_tangent,
															   final Object _thickness,
															   final boolean normalize,
															   final ProgressUpdater progress,
															   final boolean output_matrix,
															   final String matrix_file){
		
		Grid3D grid = volume.getGrid();
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
		for (int i = 0; i < mesh.n; i++)
			values.add(new MguiDouble(0));
		
		//list of normals
		ArrayList<Vector3f> normals = mesh.getNormals();
		
		double thickness = -1;
		ArrayList<MguiNumber> vertex_thickness = null;
		
		if (_thickness instanceof Double){
			thickness = (Double)_thickness;
		}else{
			vertex_thickness = (ArrayList<MguiNumber>)_thickness;
			}
		
		int null_count = 0;
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(mesh.n);
			}
		
		DefaultMatrixFileWriter writer = null;
		
		if (output_matrix){
			//then we need to open a file writer
			writer = new DefaultMatrixFileWriter(new File(matrix_file),
												 MatrixOutOptions.FORMAT_BINARY_SPARSE);
			//lots of precision
			writer.number_format = "#0.000000##";
			writer.precision = 6;
			writer.open();
			}
		
		int prog_int = (int)(0.01 * mesh.n);
		int last_prog = 0;
		
		//iterate through vertices
		for (int m = 0; m < mesh.n; m++){
			
			if (vertex_thickness != null)
				thickness = vertex_thickness.get(m).getValue();
			
			if (progress != null){
				if (progress.isCancelled()){
					return null;
					}
				if (m > last_prog){
					progress.update(m);
					last_prog += prog_int;
					}
				}
			Point3f node = mesh.getVertex(m);
			
			//First set sigma_t
			double sigma_t = sigma_tangent;
			
			//Next get normal for this node
			Vector3f normal = normals.get(m);
			
			//Next determine search bounds (thickness plus Gaussian decay)
			normal.normalize();
			normal.scale((float)(thickness + (sigma_normal * sigma_max_normal)));
			Point3f p = new Point3f();
			p.add(node, normal);
			float min_x = p.x;
			float min_y = p.y;
			float min_z = p.z;
			float max_x = p.x;
			float max_y = p.y;
			float max_z = p.z;
			p = new Point3f();
			p.sub(node, normal);
			min_x = Math.min(min_x, p.x);
			min_y = Math.min(min_y, p.y);
			min_z = Math.min(min_z, p.z);
			max_x = Math.max(max_x, p.x);
			max_y = Math.max(max_y, p.y);
			max_z = Math.max(max_z, p.z);
			
			//add/subtract max tangent bounds
			float max_t = (float)(sigma_tangent * sigma_max_tangent);
			min_x -= max_t;
			min_y -= max_t;
			min_z -= max_t;
			max_x += max_t;
			max_y += max_t;
			max_z += max_t;
			
			//Next get subvolume for bounds
			//(will be null if this bounds is outside grid bounds) 
			int[] sub_vol = grid.getSubGrid(new Point3f(min_x, min_y, min_z), 
							 				new Point3f(max_x, max_y, max_z));
			
			//int[] node_coords = grid.getEnclosingVoxel(node);
			
			//Next determine weights for all voxels in subvolume
			if (sub_vol != null){
				double denom = 0;
				double min_normal = Double.MAX_VALUE;
				double min_tangent = Double.MAX_VALUE;
				Point3f mp = new Point3f();
				Vector3f v_mp = new Vector3f();
				Vector3f v_proj = new Vector3f();
				Point3f ep = new Point3f();
				
				//For each voxel in bounds, add weighted value
				for (int i = sub_vol[0]; i < sub_vol[3]; i++)
					for (int j = sub_vol[1]; j < sub_vol[4]; j++)
						for (int k = sub_vol[2]; k < sub_vol[5]; k++){
							
							//normal weight
							mp.set(grid.getVoxelMidPoint(i, j, k));
							normal.normalize();
							v_mp.set(mp);
							v_mp.sub(node);
							
							//normal distance from node to voxel, minus cortical thickness
							v_proj.set(GeometryFunctions.getProjectedVector(v_mp, normal));
							min_normal = Math.min(min_normal, v_proj.length());
							if ((v_proj.length() - thickness / 2.0) / sigma_normal < sigma_max_normal){
								
								double w_normal = 0;
								if (v_proj.length() < (thickness / 2.0))
									w_normal = 1.0;
								else
									w_normal = StatFunctions.getGaussian2((v_proj.length() - thickness / 2.0), 0, sigma_normal);
								
								//tangent weight
								ep.set(node);
								ep.add(v_proj);
								
								//tangent distance from node to voxel
								v_mp.sub(mp, ep);
								min_tangent = Math.min(min_tangent, v_mp.length());
								if (v_mp.length() / sigma_t < sigma_max_tangent){
									
									//you've come a long way, baby
									double w_tangent = StatFunctions.getGaussian2(v_mp.length(), 0, sigma_t);
									
									//add weighted contribution
									values.get(m).add(volume.getDatumAtVoxel(channel, new int[]{i, j, k}).getValue() * w_normal * w_tangent);
									denom += w_normal * w_tangent;
									
									//write to matrix if necessary
									if (output_matrix){
										writer.writeLine(grid.getAbsoluteIndex(i, j, k), 
														 m, 
														 w_normal * w_tangent);
										
										}
									}
								}
							}
				//result is weighted average
				values.get(m).divide(denom);
				if (Double.isNaN(values.get(m).getValue()) || Double.isInfinite(values.get(m).getValue()))
					values.get(m).setValue(0);
				}
			}
		
		if (output_matrix){
			System.out.print("Writing transfer matrix to '" + matrix_file + "...");
			writer.finalize(grid.getSizeS() * grid.getSizeT() * grid.getSizeR(), mesh.n);
			System.out.println("done.");
			}
		System.out.println("\nAll done. " + null_count + " vertices not mapped");
		return values;
		
	}
	
	/**************************************************
	 * Projects vertex-wise data from a given {@link Mesh3DInt} representing a middle cortical surface 
	 * data column to a {@link Grid3D} channel, normal and transverse (tangent) Gaussian functions. 
	 * 
	 * @param mesh_int			The mesh from which to project
	 * @param grid				The target 3D grid
	 * @param mesh_column		The column from which to obtain the projected data
	 * @param grid_channel		The target grid channel 
	 * @param sigma_normal		The sigma defining a Gaussian normal to the vertex
	 * @param sigma_tangent		The sigma defining a Gaussian tangential to the vertex
	 * @param max_normal		The maximum distance to project data in the normal direction
	 * @param max_tangent		The maximum distance to project data in the tangential direction
	 * @param progress
	 * @return					A new <code>Grid3D</code> containing the projected values
	 */
	static Volume3DInt mapCortexToVolumeGaussian(final Mesh3DInt mesh_int,
										    final Volume3DInt volume,
										    final String mesh_column,
										    final String grid_channel,
										    final double sigma_normal,
										    final double sigma_tangent,
										    final double sigma_max_normal,
										    final double sigma_max_tangent,
										    final Object _thickness,
										    final boolean normalize,
										    final ProgressUpdater progress){
		
		if (progress == null || !(progress instanceof InterfaceProgressBar)) 
			return mapCortexToGrid3DGaussianBlocking(mesh_int, 
													 volume,
													 mesh_column,
													 grid_channel, 
													 sigma_normal, 
													 sigma_tangent, 
													 sigma_max_normal, 
													 sigma_max_tangent,
													 _thickness,
													 normalize, 
													 progress);
		
		return (Volume3DInt)Worker.post(new Job(){
				@Override
				public Volume3DInt run(){
					return mapCortexToGrid3DGaussianBlocking(mesh_int, 
															 volume,
															 mesh_column,
															 grid_channel, 
															 sigma_normal, 
															 sigma_tangent, 
															 sigma_max_normal, 
															 sigma_max_tangent,
															 _thickness,
															 normalize, 
															 progress);
				}
			});
		
	}
	
	/**************************************************
	 * Projects vertex-wise data from a given {@link Mesh3DInt} data column to a {@link Grid3D}
	 * channel, normal and transverse (tangent) Gaussian functions. 
	 * 
	 * @param mesh_int			The mesh from which to project
	 * @param grid				The target 3D grid
	 * @param mesh_column		The column from which to obtain the projected data
	 * @param grid_channel		The target grid channel 
	 * @param sigma_normal		The sigma defining a Gaussian normal to the vertex
	 * @param sigma_tangent		The sigma defining a Gaussian tangential to the vertex
	 * @param max_normal		The maximum distance to project data in the normal direction
	 * @param max_tangent		The maximum distance to project data in the tangential direction
	 * @param progress
	 * @return					A new <code>Grid3D</code> containing the projected values. The
	 * 							target channel will be of transfer type <code>DOUBLE</code>.
	 */
	static Volume3DInt mapCortexToGrid3DGaussianBlocking(final Mesh3DInt mesh_int,
												  	final Volume3DInt volume,
												  	final String mesh_column,
												  	final String grid_channel,
												  	final double sigma_normal,
												  	final double sigma_tangent,
												  	final double sigma_max_normal,
												  	final double sigma_max_tangent,
												  	final Object _thickness,
												  	final boolean normalize,
												  	final ProgressUpdater progress){
		
		Grid3D grid = volume.getGrid();
		Mesh3D mesh = mesh_int.getMesh();
		ArrayList<MguiNumber> values = mesh_int.getVertexData(mesh_column);
		
		//list of normals
		ArrayList<Vector3f> normals = mesh.getNormals();
		
		double thickness = -1;
		ArrayList<MguiNumber> vertex_thickness = null;
		
		if (_thickness instanceof Double){
			thickness = (Double)_thickness;
		}else{
			vertex_thickness = (ArrayList<MguiNumber>)_thickness;
			}
		
		int null_count = 0;
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(mesh.n);
			}
	
		
		Grid3D new_grid = new Grid3D(grid);
		Volume3DInt new_volume = new Volume3DInt(new_grid);
		
		new_volume.addVertexData(grid_channel, DataBuffer.TYPE_DOUBLE);
		new_volume.addVertexData("_denom", DataBuffer.TYPE_DOUBLE);
		
		int prog_int = (int)(0.01 * mesh.n);
		int last_prog = 0;
		
		int vertex_count = 0;
		
		//Temp debug
		BufferedWriter writer = null;
		if (debug_index > 0){
			try{
				writer = new BufferedWriter(new FileWriter(
					"C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Mean field\\ptrsa\\" + debug_index + ".txt"));
				writer.write("D_normal\tD_tangent\tW_normal\tWtangent\tValue\tWeight\tCumulative_value\tCumulative_denom");
			}catch (IOException e){
				e.printStackTrace();
				}
			}
		
		//iterate through indices
		for (int m = 0; m < mesh.n; m++){
			
			if (progress != null){
				if (progress.isCancelled()){
					return null;
					}
				if (m > last_prog){
					progress.update(m);
					last_prog += prog_int;
					}
				}
			
			if (vertex_thickness != null)
				thickness = vertex_thickness.get(m).getValue();
			
			double half_thickness = thickness / 2.0;
			
			Point3f node = mesh.getVertex(m);
			
			//Next get normal for this node
			Vector3f normal = normals.get(m);
			
			//Next determine search bounds
			normal.normalize();
			normal.scale((float)((thickness + sigma_normal) * sigma_max_normal));
			Point3f p = new Point3f();
			p.add(node, normal);
			float min_x = p.x;
			float min_y = p.y;
			float min_z = p.z;
			float max_x = p.x;
			float max_y = p.y;
			float max_z = p.z;
			p = new Point3f();
			p.sub(node, normal);
			min_x = Math.min(min_x, p.x);
			min_y = Math.min(min_y, p.y);
			min_z = Math.min(min_z, p.z);
			max_x = Math.max(max_x, p.x);
			max_y = Math.max(max_y, p.y);
			max_z = Math.max(max_z, p.z);
			
			//add/subtract max tangent bounds
			float max_t = (float)(sigma_normal * sigma_max_normal);
			min_x -= max_t;
			min_y -= max_t;
			min_z -= max_t;
			max_x += max_t;
			max_y += max_t;
			max_z += max_t;
			
			//Next get subvolume for bounds
			//(will be null if this bounds is outside grid bounds) 
			int[] sub_vol = grid.getSubGrid(new Point3f(min_x, min_y, min_z), 
							 				new Point3f(max_x, max_y, max_z));
			
			//Next determine weights for all voxels in subvolume
			if (sub_vol != null){
				double min_normal = Double.MAX_VALUE;
				double min_tangent = Double.MAX_VALUE;
				Point3f mp = new Point3f();
				Vector3f v_mp = new Vector3f();
				Vector3f v_proj = new Vector3f();
				Point3f ep = new Point3f();
				
				//For each voxel in bounds, add weighted value
				for (int i = sub_vol[0]; i < sub_vol[3]; i++)
					for (int j = sub_vol[1]; j < sub_vol[4]; j++)
						for (int k = sub_vol[2]; k < sub_vol[5]; k++){
							
							int index = grid.getAbsoluteIndex(i, j, k);
							
							//normal weight
							mp.set(grid.getVoxelMidPoint(i, j, k));
							normal.normalize();
							v_mp.set(mp);
							v_mp.sub(node);
							
							//normal distance from node to voxel
							v_proj.set(GeometryFunctions.getProjectedVector(v_mp, normal));
							min_normal = Math.min(min_normal, v_proj.length());
							
							if ((v_proj.length() - half_thickness) / sigma_normal < sigma_max_normal){
								
								double w_normal = 0;
								if (v_proj.length() < half_thickness)
									w_normal = 1.0;
								else
									w_normal = StatFunctions.getGaussian2((v_proj.length() - half_thickness), 0, sigma_normal);
								
							//if (v_proj.length() / sigma_normal < sigma_max_normal){
							//	double w_normal = StatFunctions.getGaussian(v_proj.length(), 0, sigma_normal);
								
								//tangent weight
								ep.set(node);
								ep.add(v_proj);
								
								Vector3f v_disp = new Vector3f(v_mp);
								
								//tangent distance from node to voxel
								v_mp.sub(mp, ep);
								min_tangent = Math.min(min_tangent, v_mp.length());
								if (v_mp.length() / sigma_tangent < sigma_max_tangent){
									
									if (index == debug_index){
										index += 0;	//breakpoint
										vertex_count++;
										try{
											
											BufferedWriter v_writer1 = new BufferedWriter(new FileWriter(
													"C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Mean field\\ptrsa\\disps_" + debug_index + ".vect3d", true));
											BufferedWriter v_writer2 = new BufferedWriter(new FileWriter(
													"C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Mean field\\ptrsa\\normals_" + debug_index + ".vect3d", true));
											BufferedWriter v_writer3 = new BufferedWriter(new FileWriter(
													"C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Mean field\\ptrsa\\tangents_" + debug_index + ".vect3d", true));
											BufferedWriter v_writer4 = new BufferedWriter(new FileWriter(
													"C:\\Documents and Settings\\atreid\\My Documents\\Projects\\Mean field\\ptrsa\\orig_normals_" + debug_index + ".vect3d", true));
											
											v_writer1.write("\n" + node.x + " " + node.y + " " + node.z + " ");
											v_writer1.write(v_disp.x + " " + v_disp.y + " " + v_disp.z + " ");
											v_writer2.write("\n" + node.x + " " + node.y + " " + node.z + " ");
											v_writer2.write(v_proj.x + " " + v_proj.y + " " + v_proj.z + " ");
											v_writer3.write("\n" + node.x + " " + node.y + " " + node.z + " ");
											v_writer3.write(v_mp.x + " " + v_mp.y + " " + v_mp.z + " ");
											v_writer4.write("\n" + node.x + " " + node.y + " " + node.z + " ");
											Vector3f temp = new Vector3f(normal);
											temp.scale(5f);
											v_writer4.write(normal.x + " " + normal.y + " " + normal.z + " ");
											v_writer1.close();
											v_writer2.close();
											v_writer3.close();
											v_writer4.close();
										}catch (IOException ex){
											
											}
										}
									
									//you've come a long way, baby
									double w_tangent = StatFunctions.getGaussian(v_mp.length(), 0, sigma_tangent);
									
									//add weighted contribution
									double value = values.get(m).getValue(); // * w_normal * w_tangent;
									//values.get(m).add(grid.getValue(channel, i, j, k, 0) * w_normal * w_tangent);
																		
									double g_value = new_volume.getDatumAtVoxel(grid_channel, new int[]{i, j, k}).getValue();
									if (Double.isInfinite(g_value) || Double.isNaN(g_value)) g_value = 0;
									g_value += value * w_normal * w_tangent;
									new_volume.setDatumAtVoxel(grid_channel, new int[]{i, j, k}, g_value);
									double d_value = new_volume.getDatumAtVoxel("_denom", new int[]{i, j, k}).getValue();
									if (Double.isInfinite(d_value) || Double.isNaN(d_value)) d_value = 0;
									
									if (no_weight){
										
									}else if (normal_weight){
										d_value += w_tangent * w_normal;
									}else{
										d_value += w_tangent;
										}
									
									new_volume.setDatumAtVoxel("_denom", new int[]{i, j, k}, d_value);
									
									if (index == debug_index){
										try{
											writer.write("\n" + v_proj.length() + "\t" + v_mp.length() +
													     "\t" + w_normal + "\t" + w_tangent + "\t" + value + "\t" +
														 w_normal * w_tangent + "\t" + g_value + "\t" + d_value);
										}catch (IOException e){}
										}
									
									}
								}
							}
				}
			}
		
		//result is weighted average
		for (int i = 0; i < new_grid.getSize(); i++){
			double value = new_volume.getDatumAtVertex(grid_channel, i).getValue();
			double denom = new_volume.getDatumAtVertex("_denom", i).getValue();
			if (i == debug_index){
				value = value + 0;	//breakpoint
				try{
					writer.write("\n\nCumulative value: " + value + 
								 "\nCumulative denom: " + denom +
								 "\nVertex count: " + vertex_count +
								 "\nFinal value: " + (value/denom));
				}catch (IOException e){}
				}
			if (denom > 0)
				value /= denom;
			if (Double.isNaN(value) || Double.isInfinite(value))
				value = 0;
			
			new_volume.setDatumAtVertex(grid_channel, i, value);
			}
		
		try{
			writer.close();
		}catch (IOException e){}
		
		InterfaceSession.log("All done. " + null_count + " voxels not mapped", LoggingType.Debug);
		return new_volume;
		
	}
	
	
}