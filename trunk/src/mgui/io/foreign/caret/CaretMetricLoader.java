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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.StringTokenizer;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceDataFileLoader;
import mgui.io.foreign.caret.CaretPaintLoader.Header;
import mgui.io.util.BufferedRandomAccessFile;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;


public class CaretMetricLoader extends SurfaceDataFileLoader {

	Header header;
	
	public CaretMetricLoader(){
		
	}
	
	public CaretMetricLoader(File file){
		setFile(file);
	}
	
	public Header getHeader(){
		if (header == null) loadHeader();
		return header;
	}
	
	protected void loadHeader(){
		if (dataFile != null){
			header = new Header(dataFile);
			return;
			}
		if (dataURL != null)
			header = new Header(dataURL);
			
	}
	
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadAllValues(progress_bar);
	}
	
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		boolean success = true;
		if (!(options instanceof CaretMetricInOptions)){
			System.out.println("Bad options");
			return false;
		}
		CaretMetricInOptions opts = (CaretMetricInOptions)options;
		if (opts.mesh == null){
			System.out.println("Mesh null");
			return false;
		}
		int p = 0;
		
		System.out.println("Loading Caret metric files...");
		
		for (int i = 0; i < opts.files.length; i++){
			setFile(opts.files[i]);
			loadHeader();
			ArrayList<ArrayList<MguiNumber>> values = loadAllValues();
			if (values == null){
				success = false;
				System.out.println("Error: No values loaded for Caret metric file '" + opts.files[i] + "'");
			}else{
				for (int j = 0; j < values.size(); j++)
					if (opts.include[p + j])
						opts.mesh.addVertexData(opts.names[p + j],
							     			  values.get(j));
				p += values.size();
				}
			}
		opts.mesh.fireShapeModified();
		System.out.println("Values loaded");
		return success;
	}
	
	public ArrayList<MguiNumber> loadValues(ProgressUpdater progress_bar){
		return null;
	}
	
	public ArrayList<ArrayList<MguiNumber>> loadAllValues(){
		return loadAllValues(null);
	}
	
	public ArrayList<ArrayList<MguiNumber>> loadAllValues(ProgressUpdater progress_bar){
		if (header == null) loadHeader();
		
		try{
			RandomAccessFile in = new RandomAccessFile(dataFile, "r");
			boolean isStart = false, isDone = false;
			String line;
			while (!isStart && !isDone){
				line = in.readLine();
				if (line == null) isDone = true;
				
				if (!isDone && line.length() > 13)
					isStart = line.substring(0, 14).equals("tag-BEGIN-DATA");
				}
			
			if (isDone){
				//throw exception
				System.out.println("Exited abnormally: before tag-BEGIN-DATA");
				return null;
				}
			
			ArrayList<ArrayList<MguiNumber>> values = new ArrayList<ArrayList<MguiNumber>> (header.getColumnCount());
			for (int j = 0; j < header.getColumnCount(); j++)
				values.add(new ArrayList<MguiNumber>());
			
			if (header.encoding.equals("BINARY")){
				loadBinaryValues(in, values);
			}else{
			
				StringTokenizer tokens;
				for (int i = 0; i < header.nodes; i++){
					tokens = new StringTokenizer(in.readLine());
					//skip index
					tokens.nextToken();
					for (int j = 0; j < header.getColumnCount(); j++){
						MguiDouble f = new MguiDouble(tokens.nextToken());
						values.get(j).add(f);
						}
					}
				}
			
			System.out.println(header.nodes + " rows loaded successfully.");
			return values;
			
		}catch (IOException e){
			e.printStackTrace();
			return null;
			}

	}

	void loadBinaryValues(RandomAccessFile in, ArrayList<ArrayList<MguiNumber>> values){
		
		long pos = 0;
		long length = 0;
		try {
			pos = in.getFilePointer();
			length = in.length();
			System.out.println("Offset: " + pos);
			BufferedRandomAccessFile braf = new BufferedRandomAccessFile(in, 1000000, ByteOrder.BIG_ENDIAN, pos);
			
			float max = -Float.MAX_VALUE;
			float min = Float.MAX_VALUE;
			
			//should be all 32-bit float, according to https://cbi.nyu.edu/svn/mrTools/trunk/mrUtilities/File/mlrSurf2Caret.m
			for (int i = 0; i < header.nodes; i++){
				//int _i = (int)braf.readFloat();
				//int _i = (int)in.readFloat() + 1;
				
				//if (i != _i){
				//	System.out.println("Error reading metric file '" + dataFile.getAbsolutePath() + "': " + i + " != " + _i);
					//return;
				//	}
				
				for (int j = 0; j < header.getColumnCount(); j++){
					float f = braf.readFloat();
					max = Math.max(f, max);
					min = Math.min(f, min);
					//float f = in.readFloat();
					values.get(j).add(new MguiFloat(f));
					pos = in.getFilePointer();
					}
				}
			
			System.out.println("Metric values loaded from '" + dataFile.getAbsolutePath() + "'. Max: " + max + ", Min: " + min);
			
		}catch (IOException e){
			e.printStackTrace();
			System.out.println("File pos: " + pos + "; size = " + length);
			}
		
	}
	
	public class Header {
		
		public int nodes;
		public ArrayList<String> columns;
		public String encoding = "ASCII";
		
		public Header(File file){
			
			try{
				RandomAccessFile in = new RandomAccessFile(file, "r");
				int cols = -1;
				
				//load stuff here
				//TODO determine version
				String line;
				int a;
				boolean beginData = false;
				while (!beginData){
					line = in.readLine();
					if (line.startsWith("encoding"))
						encoding = line.substring(line.lastIndexOf(" ") + 1).toUpperCase();
					if (line.equals("tag-BEGIN-DATA")) beginData = true;
					if (line.length() > 19 && line.substring(0, 19).equals("tag-number-of-nodes")){
						nodes = Integer.valueOf(line.substring(20)).intValue();
						System.out.println("Metric header; no. nodes: " + nodes);
						}
					if (line.length() > 21 && line.substring(0, 21).equals("tag-number-of-columns")){
						cols = Integer.valueOf(line.substring(22)).intValue();
						columns = new ArrayList<String>(cols);
						System.out.println("Metric header; no. cols: " + cols);
						}
					if (line.length() > 15 && line.substring(0, 15).equals("tag-column-name")){
						a = line.substring(16).indexOf(' ');
						if (columns != null)
							columns.add(line.substring(17 + a));
						}
					}
				
			}catch (IOException e){
				e.printStackTrace();
				}
			
		}
		
		public Header(URL url){
			
			}
		
		public int getColumnCount(){
			if (columns == null) return 0;
			return columns.size();
		}
		
	}
	
}