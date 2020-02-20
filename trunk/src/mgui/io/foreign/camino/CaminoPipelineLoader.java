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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.pipelines.PipelineXMLHandler;
import mgui.pipelines.InterfacePipeline;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class CaminoPipelineLoader extends FileLoader {

	public CaminoPipelineLoader(){
		
	}
	
	public CaminoPipelineLoader(File file){
		setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options,
			ProgressUpdater progress_bar) {
		
		return false;
	}
	
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadCaminoPipelineXML();
	}

	public InterfacePipeline loadCaminoPipelineXML() throws IOException{
		
		if (dataFile == null) throw new IOException("CaminoPipelineLoader: No input file set..");
		if (!dataFile.exists()) throw new IOException("CaminoPipelineLoader: Cannot find file '" + dataFile.getAbsolutePath() +"'..");
				
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			PipelineXMLHandler handler = new PipelineXMLHandler();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
			return handler.getLastPipeline();
			
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}
		
		
	}
	
	
}