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


package mgui.neuro.stats;

import mgui.models.dynamic.DynamicModelEnvironmentEvent;
import mgui.models.dynamic.DynamicModelException;
import mgui.models.dynamic.stats.DynamicModelStatistics;
import mgui.models.environments.SimpleEnvironment;
import mgui.neuro.exceptions.NeuroException;
import mgui.numbers.NumberFunctions;

public class SimpleStatistics implements DynamicModelStatistics {

	public double[] testSet;
	public double[] currentSet;
	public double[] previousSet;
	public boolean updated = false;
	
	public double[] getError() {
		int n = Math.min(testSet.length, currentSet.length);
		double[] error = new double[n];
		for (int i = 0; i < n; i++)
			error[i] = testSet[i] - currentSet[i];
		return error;
	}

	public double getMean() {
		return NumberFunctions.getSum(currentSet) / getN();
	}
	
	public boolean isUpdated(){
		if (!updated) return false;
		updated = false;
		return true;
	}

	public int getN() {
		return currentSet.length;
	}

	public double getRootMeanSquaredError() {
		return Math.sqrt(NumberFunctions.getSum(getSquaredError()));
	}

	public double[] getSquaredError() {
		double[] error = getError();
		for (int i = 0; i < error.length; i++)
			error[i] *= error[i];
		return error;
	}

	public double[] getTestSet() {
		return testSet;
	}

	public void setTestSet(double[] set) {
		testSet = set;
		if (currentSet == null){
			currentSet = testSet;
			previousSet = testSet;
			}
		updated = true;
	}
	
	public double[] getCurrentSet(){
		return currentSet;
	}
	public void setCurrentSet(double[] set){
		previousSet = currentSet;
		currentSet = set;
		updated = true;
	}

	public void environmentUpdated(DynamicModelEnvironmentEvent e) throws NeuroException {
		//must be an instance of SimpleEnvironment
		if (!(e.getEnvironment() instanceof SimpleEnvironment))
			throw new NeuroException("SimpleStatistics can only be updated by an environment"
					+ "which is an instance of SimpleEnvironment");
		SimpleEnvironment env = (SimpleEnvironment)e.getEnvironment();
		currentSet = env.getObservableState();
		updated = true;
	}

}