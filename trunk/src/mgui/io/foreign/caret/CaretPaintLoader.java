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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.maps.ValueMap;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceDataFileLoader;
import mgui.io.domestic.shapes.SurfaceDataInputOptions;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;


/***********************************
 * Loads a Caret Paint file
 * 
 * From Caret site:
 * tag-version 1
 * tag-number-of-nodes 71723
 * tag-number-of-columns 5
 * tag-title 
 * tag-number-of-paint-names 148
 * tag-column-name 0 Lobes
 * tag-column-name 1 Geography
 * tag-column-name 2 Functional Stuff
 * tag-column-name 3 Brodmann
 * tag-column-name 4 Modality
 * tag-BEGIN-DATA
 * 0 ???
 * 1 SUL
 * 2 DEEP_SUL
 * ...
 * 147 AREA.MTplus
 * 0 58 0 0 100 42
 * 1 58 0 0 100 42
 * 2 58 0 0 82 134
 * ...
 * 71722 21 0 0 0 128
 * 
 * 
 * TODO: accommodate versions
 * @author Andrew Reid
 * @version 1.0
 *
 */
public class CaretPaintLoader extends SurfaceDataFileLoader {

	Header header;
	
	public CaretPaintLoader(){
		
	}
	
	public CaretPaintLoader(File file){
		setFile(file);
	}
	
	public CaretPaintLoader(URL url){
		setURL(url);
	}
	
	public Header getHeader(){
		if (header == null) loadHeader();
		return header;
	}
	
	public ArrayList<MguiNumber> loadValues(ProgressUpdater progress_bar){
		return null;
	}
	
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		boolean success = true;
		if (!(options instanceof CaretPaintInOptions)){
			System.out.println("Bad options");
			return false;
		}
		CaretPaintInOptions opts = (CaretPaintInOptions)options;
		if (opts.mesh == null){
			System.out.println("Mesh null");
			return false;
		}
		int p = 0;
		
		System.out.println("Loading Caret paint files...");
		
		for (int i = 0; i < opts.files.length; i++){
			setFile(opts.files[i]);
			loadHeader();
			if (opts.set_name_map && header.nameMap != null)
				header.nameMap.setName(opts.name_map_prefix + header.nameMap.getName());
			ArrayList<ArrayList<MguiNumber>> values = loadAllValues();
			if (values == null){
				success = false;
				System.out.println("Error: No values loaded for Caret paint file '" + opts.files[i] + "'");
			}else{
				for (int j = 0; j < values.size(); j++)
					if (opts.include[p + j])
						if (opts.set_name_map)
							opts.mesh.addVertexData(opts.names[p + j],
												     values.get(j),
												     header.nameMap);
						else
							opts.mesh.addVertexData(opts.names[p + j],
								     				 values.get(j));
				if (opts.set_name_map && header.nameMap != null && header.nameMap.getSize() > 0){
					InterfaceEnvironment.addNameMap(header.nameMap);
					System.out.println("Name map '" + header.nameMap.getName() + "' added.");
				}else{
					System.out.println("Set name map:" + opts.set_name_map);		
					}
				p += values.size();
				}
			}
		opts.mesh.fireShapeModified();
		System.out.println("Values loaded");
		return success;
	}
	
	public ArrayList<ArrayList<MguiNumber>> loadAllValues(){
		if (header == null) loadHeader();
		
		try{
			RandomAccessFile in = new RandomAccessFile(dataFile, "r");
			boolean isStart = false, isDone = false;
			String line;
			while (!isStart && !isDone){
				line = in.readLine();
				if (line == null) 
					isDone = true;
				else
					isStart = line.startsWith("tag-BEGIN-DATA");
				}
			
			if (isDone){
				//throw exception
				System.out.println("Exited abnormally: before tag-BEGIN-DATA");
				in.close();
				return null;
				}
			
			//skip names
			for (int i = 0; i < header.getNameCount() && !isDone; i++)
				if (in.readLine() == null) isDone = true;
			
			if (isDone){
				//throw exception
				System.out.println("Exited abnormally: before values");
				in.close();
				return null;
				}
			
			//load values
			ArrayList<ArrayList<MguiNumber>> values = new ArrayList<ArrayList<MguiNumber>> (header.getColumnCount());
			for (int j = 0; j < header.getColumnCount(); j++)
				values.add(new ArrayList<MguiNumber>());
			
			for (int i = 0; i < header.nodes; i++){
				for (int j = 0; j < header.getColumnCount(); j++)
					values.get(j).add(new MguiInteger(in.readInt()));
				}
			
			in.close();
			System.out.println(header.nodes + " rows loaded successfully.");
			return values;
			
		}catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	protected void loadHeader(){
		if (dataFile == null && dataURL == null) return;
		if (dataFile != null)
			header = new Header(dataFile);
		else
			header = new Header(dataURL);
	}

	public class Header {
		
		public int nodes;
		public ValueMap columns = new ValueMap();
		//public ValueMap names = new ValueMap();
		public NameMap nameMap = new NameMap();
		
		public Header(File file){
			try{
				
				RandomAccessFile in = new RandomAccessFile(file, "r");
				//nodes
				String name = file.getName();
				int n = name.indexOf(".");
				if (n > 0) name = name.substring(0, n);
				nameMap.setName(name);
				
				String line = in.readLine();
				while (!line.equals("EndHeader"))
					line = in.readLine();
				//while (!in.readLine().equals("EndHeader"));
				in.readLine();
				line = in.readLine();
				nodes = Integer.valueOf(line.substring(line.indexOf(' ') + 1)).intValue();
				line = in.readLine();
				//columns
				int colcount = Integer.valueOf(line.substring(line.indexOf(' ') + 1)).intValue();
				in.readLine(); 
				line = in.readLine();
				
				//paint names
				int namecount = Integer.valueOf(line.substring(line.indexOf(' ') + 1)).intValue();
				int a, b;
				//names
				//find first instance of tag-column-name
				
				line = in.readLine();
				boolean isColumn = false, isDone = false;
				if (line.length() > 15) isColumn = line.substring(0, 15).equals("tag-column-name");
				while (!isColumn){
					line = in.readLine();
					if (line.length() > 15) isColumn = line.substring(0, 15).equals("tag-column-name");
					}
				
				//load column names
				for (int i = 0; i < colcount && !isDone; i++){
					//line = in.readLine();
					line = line.substring(16, line.length());
					a = line.indexOf(' ');
					b = Integer.valueOf(line.substring(0, a));
					columns.addItem(b, line.substring(a + 1));
					
					isColumn = false;
					while (!isColumn && !isDone){
						line = in.readLine();
						if (line == null){
							//throw exception?
							System.out.println("Exited abnormally: column i=" + i + "/" + colcount);
							in.close();
							return;
						}else{
							if (line.length() > 13) isDone = line.substring(0, 14).equals("tag-BEGIN-DATA");
							if (!isDone && line.length() > 15) isColumn = line.substring(0, 15).equals("tag-column-name");
							}
						}
					}
				
				while (!isDone){
					line = in.readLine();
					if (line == null){
						//throw exception
						System.out.println("Exited abnormally: before tag-BEGIN-DATA");
						in.close();
						return;
						}
					isDone = line.substring(0, 14).equals("tag-BEGIN-DATA");
					}
				
				//load value map
				for (int i = 0; i < namecount; i++){
					line = in.readLine();
					a = line.indexOf(' ');
					b = Integer.valueOf(line.substring(0, a));
					nameMap.add(b, line.substring(a + 1));
					//names.addItem(b, line.substring(a + 1));
					}
				
				in.close();
				
			}catch (IOException e){
				e.printStackTrace();
			}
		}
		
		public Header(URL url){
			
		}
		
		public int getNodeCount(){
			return nodes;
		}
		
		public int getColumnCount(){
			return columns.items.size();
		}
		
		public int getNameCount(){
			//return names.items.size();
			return nameMap.getSize();
		}
		
		public void printAll(){
			System.out.println("Caret Paint File Header");
			System.out.println("Node count: " + nodes);
			for (int i = 0; i < columns.items.size(); i++)
				System.out.println("Column: " + columns.items.get(i).keyValue + " " 
								   + (String)columns.items.get(i).objValue);
			Iterator<Integer> itr = nameMap.getIndices().iterator(); 
			while (itr.hasNext()){
				int index = itr.next();
				System.out.println("Name: " + index + " " + nameMap.get(index));
				}
		}
		
	}
	
}