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


package mgui.io.foreign.caret;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import mgui.io.domestic.shapes.SurfaceDataInputOptions;


public class CaretMetricInOptions extends SurfaceDataInputOptions {

	public int[] columns;						//columns per file
	public boolean[] include;
	
	public void setFiles(File[] files) {
		this.files = files;
		ArrayList<String> f_names = new ArrayList<String>();
		CaretMetricLoader loader = new CaretMetricLoader();
		columns = new int[files.length];
		
		for (int i = 0; i < files.length; i++){
			loader.setFile(files[i]);
			loader.loadHeader();
			CaretMetricLoader.Header header = loader.getHeader();
			if (header != null){
				for (int j = 0; j < header.columns.size(); j++)
					f_names.add(((String)header.columns.get(j)));
				columns[i] = header.columns.size();
				}
			
			}
		if (f_names.size() > 0){
			names = new String[f_names.size()];
			f_names.toArray(names);
			include = new boolean[f_names.size()];
			Arrays.fill(include, true);
			}
	}
	
	
}