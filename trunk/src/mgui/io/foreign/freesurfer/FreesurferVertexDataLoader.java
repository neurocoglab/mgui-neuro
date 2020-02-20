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


package mgui.io.foreign.freesurfer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceDataFileLoader;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiLong;
import mgui.numbers.MguiNumber;
import mgui.util.Colour3f;

/****************************************************
 * Loads a Freesurfer surface data file. Format can be dense or sparse (i.e., old or new
 * "curv" format), which is specified by the "type" option of {@linkplain FreesurferVertexDataLoaderOptions}.
 * 
 * <p>See: <a href="http://www.grahamwideman.com/gw/brain/fs/surfacefileformats.htm">
 * http://www.grahamwideman.com/gw/brain/fs/surfacefileformats.htm</a>.
 * 
 * <p>See: <a href="http://surfer.nmr.mgh.harvard.edu/fswiki/LabelsClutsAnnotationFiles">
 * http://surfer.nmr.mgh.harvard.edu/fswiki/LabelsClutsAnnotationFiles</a>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FreesurferVertexDataLoader extends SurfaceDataFileLoader {

	FreesurferVertexDataInOptions options = new FreesurferVertexDataInOptions();
	private int values_per_vertex = 1;
	
	public ColourMap colour_map = null;
	
	public FreesurferVertexDataLoader(){
		
	}
	
	public FreesurferVertexDataLoader(File file){
		setFile(file);
	}
	
	public FreesurferVertexDataLoader(File file, FreesurferVertexDataInOptions options){
		setFile(file);
		this.options = options;
	}
	
	@Override
	public boolean load(InterfaceIOOptions _options, ProgressUpdater progress_bar) {
		
		if (!(_options instanceof FreesurferVertexDataInOptions)){
			InterfaceSession.log("FreesurferVertexDataLoader: options must be an instance of FreesurferVertexDataInOptions", 
								 LoggingType.Errors);
			return false;
			}
		
		options = (FreesurferVertexDataInOptions)_options;
		
		File[] files = options.getFiles();
		boolean success = true;
		for (int i = 0; i < files.length; i++){
			setFile(files[i]);
			ArrayList<MguiNumber> values = loadValues(progress_bar);
			boolean s = values != null;
			if (s){
				int n = values.size() / values_per_vertex;
				for (int j = 0; j < values_per_vertex; j++){
					ArrayList<MguiNumber> column = new ArrayList<MguiNumber>(values.subList(j, j + n));
					String c_name = options.names[i]; // + ".column" + (j + 1);
					options.mesh.addVertexData(c_name, column);
					if (options.load_colour_map && colour_map != null){
						colour_map.setName(c_name + "_cmap");
						InterfaceEnvironment.addColourMap(colour_map);
						options.mesh.setColourMap(c_name, colour_map);
						if (((DiscreteColourMap)colour_map).hasNameMap()){
							NameMap name_map = ((DiscreteColourMap)colour_map).getNameMap();
							name_map.setName(c_name + "_cmap");
							options.mesh.setNameMap(c_name, name_map);
							}
						}
					}
				}
			success &= s;
			}
		
		return success;
	}

	@Override
	public Object loadObject(ProgressUpdater progress_bar) throws IOException {
		return loadValues(progress_bar);
	}

	@Override
	public ArrayList<MguiNumber> loadValues(ProgressUpdater progress_bar) {
		
		colour_map = null;
		
		switch (options.format){
			case Annotation:
				return loadValuesAnnotation(progress_bar);
			case Label:
				return loadValuesLabel(progress_bar);
			case Dense1:
				return loadValuesDense1(progress_bar);
			case Dense2:
				return loadValuesDense2(progress_bar);
			case Sparse:
				return loadValuesSparse(progress_bar);
			case Ascii:
				return loadValuesAscii(progress_bar);
			}
		
		return null;
	}
		
	protected ArrayList<MguiNumber> loadValuesDense1(ProgressUpdater progress_bar){
		try{
			RandomAccessFile raf = new RandomAccessFile(dataFile, "r");
			
			// First int is size
			int n = raf.readInt();
			
			if (progress_bar != null){
				progress_bar.setMinimum(0);
				progress_bar.setMaximum(n);
				}
			
			// Second int is useless (faces)
			raf.readInt();
			
			ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
			
			// Read values
			for (int i = 0; i <= n; i++){
				float value = raf.readFloat();
				switch (options.data_type){
					case Integer:
						values.add(new MguiInteger(value));
						break;
					case Long:
						values.add(new MguiLong(value));
						break;
					case Float:
						values.add(new MguiFloat(value));
						break;
					case Double:
						values.add(new MguiDouble(value));
						break;
					case Boolean:
						values.add(new MguiBoolean(value - 1 < 0.00001));
						break;
					}
				
				if (progress_bar != null){
					progress_bar.update(i);
					}
				}
			
			raf.close();
			return values;
			
		}catch (Exception ex){
			InterfaceSession.log("FreesurferVertexDataLoader: Error reading from '" + dataFile.getAbsolutePath() + "'" +
								 "\nDetails: " + ex.getMessage(), 
								 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			}
		
		return null;
	}
	
	private int readInt3(RandomAccessFile raf) throws IOException {
		
		byte[] b3 = new byte[3];
		raf.read(b3);
		
		return (b3[0] & 0xff) << 16 | (b3[1] & 0xff) << 8 | (b3[2] & 0xff);
		
	}
	
	static final int NEW_VERSION_MAGIC_NUMBER = 16777215;
	
	protected ArrayList<MguiNumber> loadValuesDense2(ProgressUpdater progress_bar){
		try{
			RandomAccessFile raf = new RandomAccessFile(dataFile, "r");
			
			// First int is magic (should be == 16777215); test this?
			int magic = readInt3(raf);
			
			int n = 0, m = 0;
			
			if (magic == NEW_VERSION_MAGIC_NUMBER) {
				// Second int is # vertices
				n = raf.readInt();
				// Third int is useless (faces)
				raf.readInt();
				m = raf.readInt();
				
			}else{
				// Second int is # vertices
				n = magic;
				m = readInt3(raf);
				}
			
			if (progress_bar != null){
				progress_bar.setMinimum(0);
				progress_bar.setMaximum(n);
				}
						
			ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
			float value = 0;
			
			// Read values
			for (int i = 0; i < n; i++){
				for (int j = 0; j < m; j++){
					if (magic == NEW_VERSION_MAGIC_NUMBER) {
						value = raf.readFloat();
					}else{
						value = (float)raf.readShort() / 100;
						}
					switch (options.data_type){
						case Integer:
							values.add(new MguiInteger(value));
							break;
						case Long:
							values.add(new MguiLong(value));
							break;
						case Float:
							values.add(new MguiFloat(value));
							break;
						case Double:
							values.add(new MguiDouble(value));
							break;
						case Boolean:
							values.add(new MguiBoolean(value - 1 < 0.00001));
							break;
						}
					}
				if (progress_bar != null){
					progress_bar.update(i);
					}
				}
			
			raf.close();
			return values;
			
		}catch (Exception ex){
			InterfaceSession.log("FreesurferVertexDataLoader: Error reading from '" + dataFile.getAbsolutePath() + "'" +
								 "\nDetails: " + ex.getMessage(), 
								 LoggingType.Errors);
			}
		
		return null;
	}
	
	protected ArrayList<MguiNumber> loadValuesAnnotation(ProgressUpdater progress_bar){
		try{
			RandomAccessFile raf = new RandomAccessFile(dataFile, "r");
			
			// First int is size
			int n = raf.readInt();
			
			if (progress_bar != null){
				progress_bar.setMinimum(0);
				progress_bar.setMaximum(n*2);
				}
			
			ArrayList<Integer> rgbs = new ArrayList<Integer>();
			
			// Read vertex values
			for (int i = 0; i < n; i++){
				int value = raf.readInt(); // Vertex #, ignored as we assume all vertices in order
				value = raf.readInt();
				rgbs.add(value);
				
				if (progress_bar != null){
					progress_bar.update(i);
					}
				
				}
			
			// Now the fun (and baffling) part... read LUT, deconstruct RGBs and discover indices
			
			// tag
			int tag = raf.readInt();
			// ctabversion
			raf.readInt();
			// maxstruct
			raf.readInt();
			// len
			int len = raf.readInt();
			// fname
			for (int i = 0; i < len; i++)
				raf.readByte();
			
			// LUT entries
			int n_entries = raf.readInt();
			NameMap name_map = new NameMap();
			DiscreteColourMap colour_map = new DiscreteColourMap(name_map);
			HashMap<Integer,Integer> label_map = new HashMap<Integer,Integer>();
			
			for (int i = 0; i < n_entries; i++){
				
				int label = raf.readInt();
				len = raf.readInt();
				byte[] bytes = new byte[len];
				raf.read(bytes);
				//CharBuffer cbuff = ByteBuffer.wrap(bytes).asCharBuffer();
				String name = new String(bytes,0,len,"US-ASCII");
				name_map.add(label, name);
				
				int r = raf.readInt();
				int g = raf.readInt();
				int b = raf.readInt();
				int a = raf.readInt();
				colour_map.setColour(i, name, new Colour3f((float)r/255f,
														   (float)g/255f,
														   (float)b/255f));
				label_map.put(r + g*256 + b*256*256, label);
				
				}
			
			ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
			for (int i = 0; i < n; i++){
				Integer value = rgbs.get(i);
				value = label_map.get(value);
				double dval = 0;
				if (value == null){
					if (options.fail_on_bad_rgb){
						InterfaceSession.log("FreesurferVertexDataLoader: value " + rgbs.get(i) +
											 " not in RGB table!", 
											 LoggingType.Errors);
						raf.close();
						return null;
					}else{
						dval = options.missing_value;
						}
				}else{
					dval = (double)value;
					}
				
				switch (options.data_type){
					case Integer:
						values.add(new MguiInteger(dval));
						break;
					case Long:
						values.add(new MguiLong(dval));
						break;
					case Float:
						values.add(new MguiFloat(dval));
						break;
					case Double:
						values.add(new MguiDouble(dval));
						break;
					case Boolean:
						values.add(new MguiBoolean(GeometryFunctions.compareDouble(dval, 0) != 0));
						break;
					}
				
				if (progress_bar != null){
					progress_bar.update(i+n);
					}
				
				}
			
			raf.close();
			this.colour_map = colour_map;
			return values;
			
		}catch (Exception ex){
			InterfaceSession.log("FreesurferVertexDataLoader: Error reading from '" + dataFile.getAbsolutePath() + "'" +
								 "\nDetails: " + ex.getMessage(), 
								 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			}
		
		return null;
	}
	
	protected int[] getAnnotRGB(int value){
		
		// RGB is stored as  (R) + (G * 256) + (B * 256^2)
		
		int[] rgb = new int[3];
		rgb[0] = (int)Math.floor(value / (256*256));
		int remainder = value % (256*256);
		rgb[1] = (int)Math.floor(remainder / 256);
		rgb[2] = remainder % 256;
		
		return rgb;
		
	}
	
	protected ArrayList<MguiNumber> loadValuesLabel(ProgressUpdater progress_bar){
		
		return null;
	}
	
	protected ArrayList<MguiNumber> loadValuesSparse(ProgressUpdater progress_bar){
		InterfaceSession.log("FreesurferVertexDataLoader: Load for sparse format not implemented.", 
				 LoggingType.Errors);
		return null;
	}
	
	protected ArrayList<MguiNumber> loadValuesAscii(ProgressUpdater progress_bar){
		InterfaceSession.log("FreesurferVertexDataLoader: Load for ascii format not implemented.", 
				 LoggingType.Errors);
		return null;
	}

}