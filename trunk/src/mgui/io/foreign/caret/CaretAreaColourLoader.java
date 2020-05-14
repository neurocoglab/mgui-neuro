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


package mgui.io.foreign.caret;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.maps.DiscreteColourMapLoader;
import mgui.util.Colour4f;


/************************************************
 * A hack to read Caret area colour files. This should really be done using an XML parser,
 * but I haven't the patience :P
 * 
 * @author Andrew Reid
 *
 */
public class CaretAreaColourLoader extends DiscreteColourMapLoader {

	protected NameMap nameMap;
	
	public CaretAreaColourLoader(){
		
	}
	
	public CaretAreaColourLoader(File file){
		this.setFile(file);
	}
	
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		CaretAreaColourInOptions opts = (CaretAreaColourInOptions)options;
		File[] files = opts.getFiles();
		if (opts.names == null) opts.setNamesFromFiles();
		
		boolean success = true;
		try{
			
		for (int i = 0; i < files.length; i++){
			dataFile = files[i];
			map = loadMap(opts.nameMap);
			map.setName(opts.names[i]);
			InterfaceEnvironment.addColourMap(map);
			}
		
		}catch (IOException e){
			InterfaceSession.log("Error loading area colour file.\nDetails: " + e.getMessage(), 
								 LoggingType.Errors);
			success = false;
			}
		
		return success;
	}
	
	public DiscreteColourMap loadMap(NameMap nameMap) throws IOException{
		
		/****************
		 * Strategy: simply find <color> tags and extract names/colours
		 * 
		 */
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = reader.readLine();
		
		DiscreteColourMap map = new DiscreteColourMap();
		map.nameMap = nameMap;
		
		int index = 0;
		
		while (line != null){
			
			while (line != null && !line.contains("<Color>"))
				line = reader.readLine();
			
			if (line == null) break;
			line = reader.readLine();
			
			int pos = line.indexOf("CDATA[");
			if (pos > 0 && pos < line.length() - 6){
				//assume the rest in well-formed
				String name = line.substring(pos + 6, line.indexOf("]"));
				
				line = reader.readLine();
				float red = Integer.valueOf(line.substring(line.indexOf(">") + 1, line.indexOf("</")));
				line = reader.readLine();
				float green = Integer.valueOf(line.substring(line.indexOf(">") + 1, line.indexOf("</")));
				line = reader.readLine();
				float blue = Integer.valueOf(line.substring(line.indexOf(">") + 1, line.indexOf("</")));
				line = reader.readLine();
				float alpha = Integer.valueOf(line.substring(line.indexOf(">") + 1, line.indexOf("</")));
				
				if (nameMap != null)
					map.setColour(name, new Colour4f(red / 255f, green / 255f, blue / 255f, alpha / 255f));
				else
					map.setColour(index++, new Colour4f(red / 255f, green / 255f, blue / 255f, alpha / 255f));
				}
			
			}
		
		reader.close();
		return map;
	}
	
}