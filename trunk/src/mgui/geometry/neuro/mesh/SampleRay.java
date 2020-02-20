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

import mgui.geometry.Vector3D;
import mgui.neuro.stats.SignalProcessing;

/**********************************************
 * Represents a ray vector and regular samples along its trajectory (at a frequency of n / length). 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SampleRay {

	protected Vector3D ray;
	protected ArrayList<Double> samples = new ArrayList<Double>();
	
	public SampleRay(Vector3D ray){
		this.ray = ray;
	}
	
	public SampleRay(Vector3D ray, ArrayList<Double> samples){
		this.ray = ray;
		this.samples = samples;
	}
	
	public SampleRay(SampleRay ray){
		this.ray = ray.getRay();
		this.samples = ray.getSamples();
	}
	
	public Vector3D getRay(){
		return new Vector3D(ray);
	}
	
	public void addSampleValues(ArrayList<Double> values){
		for (int i = 0; i < samples.size(); i++){
			//if (Double.isNaN(samples.get(i)))
			//	i += 0;
			samples.set(i, samples.get(i) + values.get(i));
			if (Double.isNaN(samples.get(i)))
				i += 0;
			}
	}
	
	public ArrayList<Double> getSamples(){
		return new ArrayList<Double>(samples);
	}
	
	public void setSamples(ArrayList<Double> samples){
		this.samples = new ArrayList<Double>(samples);
	}
	
	public Double getSample(int index){
		if (index < 0 || index >= samples.size()) return Double.NaN;
		return samples.get(index);
	}
	
	public void setSample(int index, double value){
		if (index < 0 || index >= samples.size()) return;
		samples.set(index, value);
	}
	
	public int getSize(){
		return samples.size();
	}
	
	public Double getNearestSample(double position){
		if (position < 0) position = 0;
		if (position > 1) position = 1;
		int index = (int)Math.round((double)samples.size() * position); 
		return samples.get(index);
	}
	
	public Double getInterpolatedValue(double position){
		if (position < 0) position = 0;
		if (position > 1) position = 1;
		double index = (double)(samples.size() - 1) * position;
		int before = (int)Math.floor(index);
		double inter = index - before;
		double sample1 = samples.get(before);
		double sample2 = samples.get(before + 1);
		double interpolated = (((1 - inter) * sample1) + (inter * sample2)) / 2.0;
		return interpolated;
	}
	
	public void resample(int n){
		samples = SignalProcessing.getResampledCurve(samples, n, 1);
	}
	
}