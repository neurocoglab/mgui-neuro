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


package mgui.neuro.stats;

import java.util.ArrayList;

import mgui.interfaces.Utility;
import mgui.numbers.NumberFunctions;

/*******************************************************************
 * Utility class for signal processing functions.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */

public class SignalProcessing extends Utility {

	/****************************************************
	 * Smooths <code>curve</code> using a moving-average algorithm, with a window width
	 * <code>n</code> = 2m + 1; where n is an odd number. Each data point is assigned a value 
	 * according to the function:
	 * 
	 * <p>
	 * y(k)_s = SUM[i = -m : m](y_k+1) / n 
	 * </p>
	 * <p>
	 * See http://www.chem.uoa.gr/applets/appletsmooth/appl_smooth2.html
	 * </p>
	 * 
	 * @param curve
	 * @param n
	 * @return
	 */
	public static ArrayList<Double> smoothCurveMovingAverage(ArrayList<Double> curve, int n){
		
		if (NumberFunctions.isEven(n)) return curve;
		
		ArrayList<Double> smoothed = new ArrayList<Double>(curve.size());
		int m = (n - 1) / 2;
		
		for (int i = 0; i < curve.size(); i++){
			double v = 0;
			int c = 0;
			for (int k = -m; k < m; k++)
				if (i + k >= 0 && i + k < curve.size()){
					v += curve.get(i + k);
					c++;
					}
			smoothed.add(v / (double)c);
			}
		
		return smoothed;
	}
	
	
	/******************************************************
	 * Returns a set of data points which represent the <code>order</code>th order derivative of
	 * <code>curve</code>. The result will have <code>order</code> less elements than <code>curve</code>.
	 * 
	 * @param curve the curve for which to compute the derivative
	 * @param order the order of the derivative to compute
	 * @return the derivative for <code>curve</code>, with n - 1 elements
	 */
	public static ArrayList<Double> getDerivative(ArrayList<Double> curve, int order){
		
		ArrayList<Double> smoothed = new ArrayList<Double>(curve.size());
		
		for (int i = 1; i < curve.size(); i++)
			smoothed.add(curve.get(i) - curve.get(i - 1));
		
		if (order > 1) return getDerivative(smoothed, order - 1);
		
		return smoothed;
		
	}
	
	/***************************
	* Resample (interpolate with order) curve to specified number of samples. Currently only resamples with
	* linear interpolation.
	*
	* @param curve the curve to resample
	* @param samples the number of samples in the resulting curve
	* @param the order of the interpolation (currently does nothing as only linear interpolation is
	* 		 implemented.
	* @return the resampled curve
	*/
	public static ArrayList<Double> getResampledCurve(ArrayList<Double> curve, int samples, int order){
		
		int length = curve.size();
		double rate = (double)length / (double)samples;
		ArrayList<Double> resampled = new ArrayList<Double>();
		double m = 0;
		
		for (double i = 0; i < samples; i++){
			int k = (int)Math.floor(m);
			if (k < length - 1){
				double p = m - (double)k;
				double diff = curve.get(k + 1) - curve.get(k);
				resampled.add(curve.get(k) + (p * diff));
			}else{
				resampled.add(curve.get(k));
				}
			if (Double.isNaN(resampled.get((int)i)))
				m += 0;
			m += rate;
			}
		
		return resampled;
		
	}
	
	/****************************
	 * Normalizes this curve to the max and min of its values.
	 * 
	 * @param curve the curve to normalize
	 * @param min the minimum for the normalization
	 * @param max the maximum for the normalization
	 * @return the normalized curve
	 */
	public static ArrayList<Double> getNormalizedCurve(ArrayList<Double> curve){
		
		double max = -Double.MAX_VALUE;
		double min = Double.MAX_VALUE;
		
		for (int i = 0; i < curve.size(); i++){
			max = Math.max(max, curve.get(i));
			min = Math.min(min, curve.get(i));
			}
		
		return getNormalizedCurve(curve, min, max);
		
	}
	
	/****************************
	 * Normalizes this curve to max and min.
	 * 
	 * @param curve the curve to normalize
	 * @param min the minimum for the normalization
	 * @param max the maximum for the normalization
	 * @return the normalized curve
	 */
	public static ArrayList<Double> getNormalizedCurve(ArrayList<Double> curve, double min, double max){
		
		ArrayList<Double> normalized = new ArrayList<Double>(curve.size());
		if (NumberFunctions.compare(min, max, 5) == 0){
			for (int i = 0; i < curve.size(); i++)
				normalized.add(0.0);
		}else{
			for (int i = 0; i < curve.size(); i++){
				double d = (curve.get(i) - min) / (max - min);
				normalized.add(d);
				if (Double.isNaN(d))
					d += 0;
				}
			}
		
		return normalized;
	}
	
	
}