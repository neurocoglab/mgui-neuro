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


package mgui.geometry.neuro.connectivity;

import java.util.ArrayList;
import java.util.HashMap;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.Utility;
import mgui.interfaces.logs.LoggingType;
import mgui.numbers.MguiNumber;
import mgui.stats.StatFunctions;
import Jama.Matrix;

/***************************************************************
 * Utility class providing functions for determining neural connectivity from geometry.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NeuroConnectivityFunctions extends Utility {

	
	/*****************************************************************
	 * Computes correlations between "patches" of cortex, defined by a set of vertices and a patch
	 * function. Patch functions are one of:
	 * 
	 * <ul>
	 * <li>Radius 			All neighbours of vertex i within a fixed radius are included in the correlation
	 * 						computation. In the case of unequal vertex counts, the farthest vertices will be
	 * 						removed from the largest patch until both patches have the same size.
	 * <li>Neighbours 		N nearest neighbours of vertex i will be included in the patch
	 * <li>Gaussian			All neighbours of vertex i within a cut-off standard deviation value will be
	 * 						included in the patch. Values of these vertices will be scaled by a Gaussian
	 * 						kernel, as a function of their distance along the surface from vertex i. In the
	 * 						case of unequal vertex counts, the farthest vertices will be
	 * 						removed from the largest patch until both patches have the same size.
	 * <li>ROIs 			Regions-of-interest defined a priori. In the case of unequal vertex counts, 
	 * 						a random subset of the largest patch will be used to equalize the counts.
	 * </ul>
	 * 
	 * Patch functions can be modulated by general or specific parameters. For example, the general parameters
	 * {@code n_min} and {@code n_max} set lower and upper limits on the number of values used to compute
	 * a correlation.
	 * 
	 * <p>Parameters are specified using the {@code parameters} argument, which is a map of parameter names to
	 * values. Acceptable parameters are:
	 * 
	 * <ul>
	 * <li>n_min 			[Optional: {@code int}]
	 * 						Lower limit on the number of values used to compute correlations. Patches which
	 * 						have less vertices than this value are not used to compute; their Pearson
	 * 						correlation values will be assigned a value of {@code Double.NaN}. Default is 0.
	 * <li>n_max 			[Optional: {@code int}]
	 * 						Upper limit on the number of values used to compute correlations. Patches which
	 * 						have more vertices than this number will be subsampled to match it: according
	 * 						to distance from vertex i, or randomly, in the case of 'ROIs'
	 * 						Default is {@code Double.MAX_VALUE}.
	 * <li>patch_function	[Required: {@code String}
	 * 						Function with which to derive patches; see above.
	 * <li>patch_radius 	[Required for 'Radius': {@code double}]
	 * 						The radius determining the boundary of the patch.
	 * <li>n_neighbours 	[Required for 'Neighbours': {@code int}]
	 * 						The number of nearest neighbours to use for each patch.
	 * <li>gaussian_fwhm 	[Required for 'Gaussian' if {@code gaussian_sigma} is not defined: {@code double}]
	 * 						The full width at half max of the Gaussian kernel.
	 * <li>gaussian_sigma 	[Required for 'Gaussian' if {@code gaussian_fwhm} is not defined: {@code double}]
	 * 						The sigma for the Gaussian kernel.
	 * <li>gaussian_sigma_max 	[Required for 'Gaussian': {@code double}]
	 * 							The sigma value determining the boundary of the patch.
	 * <li>roi_values 		[Required for 'ROIs': {@code ArrayList<MguiInteger}]
	 * 						The vertex-wise ROI assignments determining the patches
	 * </ul>
	 * 
	 * @param mesh 			The mesh from which to derive connectivity
	 * @param v_data 		The values which are to be used to compute correlations
	 * @param parameters 	A map of parameter names to the corresponding values
	 * @return 				An M x M matrix, containing the Pearson coefficients for correlations between
	 * 						each pair of vertices i and j (or a subset of these, if the {@code filter_indices}
	 * 						parameter has been specified).
	 */
	public static Matrix computeCorticalPatchCorrelations(Mesh3D mesh, 
														  ArrayList<MguiNumber> v_data,
														  HashMap<String,Object> parameters){
		
		
		// Extract parameters
		String patch_function = (String)parameters.get("patch_function");
		if (patch_function == null){
			InterfaceSession.log("computeCorticalPatchCorrelations: Parameter 'patch_function'" +
								 " is not optional.", 
								 LoggingType.Errors);
			return null;
			}
		
		// General parameters
		int n_min = 0;
		if (parameters.containsKey("n_min"))
			n_min = (Integer)parameters.get("n_min");
		int n_max = 0;
		if (parameters.containsKey("n_max"))
			n_max = (Integer)parameters.get("n_max");
		
		// Determine the patches
		ArrayList<ArrayList<MguiNumber>> patches = new ArrayList<ArrayList<MguiNumber>>();
		
		if (patch_function.equals("ROIs")){
			
			
			
		}else if (patch_function.equals("Radius")){
			
			
		}else if (patch_function.equals("Neighbours")){
		
			
		}else if (patch_function.equals("Gaussian")){
			
			
		}else{
			InterfaceSession.log("computeCorticalPatchCorrelations: Unrecognized patch function: '" +
					 patch_function + "'.", 
					 LoggingType.Errors);
			return null;
			}
		
		// Patch statistics
		int n = patches.size();
		ArrayList<Double> patch_means = new ArrayList<Double>();
		ArrayList<Double> patch_std = new ArrayList<Double>();
		for (int i = 0; i < n; i++){
			double[] stats = StatFunctions.getBasicNormalStats(patches.get(i));
			patch_means.add(stats[0]);
			patch_std.add(stats[2]);
			}
		
		// Compute correlations
		Matrix M = new Matrix(n,n);
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				if (i != j){
					// Each pair of values
					ArrayList<MguiNumber> patch_i = patches.get(i);
					ArrayList<MguiNumber> patch_j = patches.get(j);
					double sum_of_diffs = 0;
					
					for (int k = 0; k < patch_i.size(); k++)
						for (int l = 0; l < patch_j.size(); l++){
							sum_of_diffs += ((patch_i.get(k).getValue() - patch_means.get(i)) -
											((patch_j.get(l).getValue() - patch_means.get(j))));
							}
					
					double r = sum_of_diffs / (patch_std.get(i) * patch_std.get(j));
					M.set(i, j, r);
					}
				}
			}
		
		
		
		return null;
	}
	
	
	
}