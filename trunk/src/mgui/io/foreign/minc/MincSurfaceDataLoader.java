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


package mgui.io.foreign.minc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceDataFileLoader;
import mgui.io.domestic.shapes.SurfaceDataInputOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;


public class MincSurfaceDataLoader extends SurfaceDataFileLoader {

	public static final int FORMAT_FLOAT = 0;
	public static final int FORMAT_DOUBLE = 1;
	public static final int FORMAT_INT = 2;
	
	public int format = FORMAT_FLOAT;
	
	public MincSurfaceDataLoader(){
		
	}
	
	public MincSurfaceDataLoader(int format){
		setFormat(format);
	}
	
	public MincSurfaceDataLoader(File file){
		setFile(file);
	}
	
	public MincSurfaceDataLoader(File file, int format){
		setFile(file);
		setFormat(format);
	}
	
	public void setFormat(int format){
		this.format = format;
	}
	
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		boolean success = true;
		if (!(options instanceof SurfaceDataInputOptions)) return false;
		SurfaceDataInputOptions opts = (SurfaceDataInputOptions)options;
		if (opts.mesh == null) return false;
		for (int i = 0; i < opts.files.length; i++){
			setFile(opts.files[i]);
			ArrayList<MguiNumber> values = loadValues(progress_bar);
			if (values == null)
				success &= false;
			else
				success &= opts.mesh.addVertexData(opts.names[i], values);
			}
		opts.mesh.fireShapeModified();
		return success;
	}
	
	public ArrayList<MguiNumber> loadValues(ProgressUpdater progress_bar){
		//TODO: throw exception
		if (dataFile == null) return null;
		
		//simple line-for-line
		try{
			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			String line = in.readLine();
			ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
			
			while (line != null){
				switch (format) {
					case FORMAT_FLOAT:
						values.add(new MguiFloat(line));
						break;
					case FORMAT_DOUBLE:
						values.add(new MguiDouble(line));
						break;
					case FORMAT_INT:
						values.add(new MguiInteger(line));
					}
				line = in.readLine();
				}
		
			in.close();
			return values;
			
		}catch (IOException e){
			e.printStackTrace();
			return null;
		}
		
	}
	

}