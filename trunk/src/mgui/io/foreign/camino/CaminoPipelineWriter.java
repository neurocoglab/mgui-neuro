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


package mgui.io.foreign.camino;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.pipelines.InterfacePipeline;


public class CaminoPipelineWriter extends FileWriter {

	
	public CaminoPipelineWriter(){
		
	}
	
	public CaminoPipelineWriter(File file){
		setFile(file);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options,
						 ProgressUpdater progress_bar) {
		
		return false;
	}
	
	
	public boolean writePipelineXML(InterfacePipeline pipeline){
		
		if (dataFile == null) return false;
		
		try{
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
			pipeline.writeXML(0, writer);
			writer.close();
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
		
		return true;
	}
	
	
	

}