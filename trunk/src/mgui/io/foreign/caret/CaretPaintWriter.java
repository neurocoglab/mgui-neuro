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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceDataFileWriter;
import mgui.io.domestic.shapes.SurfaceDataOutputOptions;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

/*****************************************************************
 * Writes a set of vertex-wise data columns to Caret paint files, binary format version 1.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CaretPaintWriter extends SurfaceDataFileWriter {

	String format = "0.00000000";
	
	private RandomAccessFile current_raf;
	
	@Override
	public void setFormat(String format) {
		this.format = format;
	}
	
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar){
		if (options == null || !(options instanceof SurfaceDataOutputOptions)) return false;
		
		SurfaceDataOutputOptions opts = (SurfaceDataOutputOptions)options;
		if (opts.mesh == null) return false;
		
		File dir = opts.files[0];
		
		boolean success = true;
		for (int j = 0; j < opts.columns.size(); j++){
			setFile(new File(dir.getAbsolutePath() + File.separator + opts.filenames.get(j)));
			setFormat(opts.formats.get(j));
			if (!writeValues(opts, opts.columns.get(j), progress_bar))
				success = false;
			}
		
		dataFile = dir;
		return success;
	}
	
	public boolean writeValues(SurfaceDataOutputOptions options, String column, ProgressUpdater progressBar){
		
		try{
			current_raf = new RandomAccessFile(dataFile, "rw");
			
			//first write header
			if (!writeHeader(options, column)){
				current_raf.close();
				current_raf = null;
				return false;
				}
			
			//now write binary integers
			boolean success = writeValues(options.mesh.getVertexData(column), progressBar);
			
			current_raf.close();
			current_raf = null;
			return success;
		
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return false;
		
	}
	
	/****************************************
	 * Overridden from {@link SurfaceDataFileWriter}. In the case of this writer, however, one should only use
	 * the <code>writeValues(SurfaceDataOutputOptions options, String column, ProgressUpdater progressBar)</code> and
	 * <code>write(InterfaceIOOptions options, ProgressUpdater progress_bar)</code> methods, as this format requires
	 * a header to be written in additional to vertex values. Used on its own, this method will do nothing and
	 * will always return <code>false</code>.
	 * 
	 * @param values the values to write
	 * @param progressBar the progress updater (not utilized here)
	 */
	@Override
	public boolean writeValues(ArrayList<MguiNumber> values, ProgressUpdater progressBar) {
		
		if (current_raf == null) return false;
		
		try{
			for (int j = 0; j < values.size(); j++)
				current_raf.writeInt((int)values.get(j).getValue());
			return true;
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return false;
	}
	
	protected boolean writeHeader(SurfaceDataOutputOptions options, String column){
		
		if (current_raf == null) return false;
		Mesh3D mesh = options.mesh.getMesh();
		
		try{
			current_raf.writeBytes("BeginHeader");
			current_raf.writeBytes("\nCaret-Version 5.6");
			current_raf.writeBytes("\ncomment Created by modelGUI v" + InterfaceEnvironment.getVersion());
			current_raf.writeBytes("\ndate " + InterfaceEnvironment.getNow("dd/mm/yyyy"));
			current_raf.writeBytes("\nencoding BINARY");
			current_raf.writeBytes("\nEndHeader");
			
			current_raf.writeBytes("\ntag-version 1");
			current_raf.writeBytes("\ntag-number-of-nodes " + mesh.n);
			current_raf.writeBytes("\ntag-number-of-columns " + options.columns.size());
			current_raf.writeBytes("\ntag-title ");
			
			int name_count = 0;
			NameMap n_map = options.mesh.getNameMap(column);
			if (n_map == null){
				n_map = getDefaultNameMap(options.mesh, column);
				}
			if (n_map != null){
				ArrayList<Integer> indices = new ArrayList<Integer>(n_map.getIndices());
				if (indices.size() > 0){
					Collections.sort(indices);
					name_count = indices.get(indices.size() - 1) + 1;
					}
				}
			
			current_raf.writeBytes("\ntag-number-of-paint-names " + name_count);
			current_raf.writeBytes("\ntag-column-name " + 0 + " " + column);
			current_raf.writeBytes("\ntag-BEGIN-DATA\n");
						
			ArrayList<Integer> indices = new ArrayList<Integer>(n_map.getIndices());
			Collections.sort(indices);
			
			//name maps; we need to pad unused indices here for whatever reason
			int pos = 0;
			if (n_map != null)
				for (int i = 0; i < indices.size(); i++){
					int index = indices.get(i);
					while (pos < index)
						current_raf.writeBytes("" + pos + " NA" + pos++ + "\n");
					current_raf.writeBytes("" + index + " " + n_map.get(index) + "\n");
					pos = index + 1;
					}
			
			return true;
			
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return false;
		
	}
	
	protected NameMap getDefaultNameMap(Mesh3DInt mesh, String column){
		
		ArrayList<MguiNumber> values = mesh.getVertexData(column);
		NameMap n_map = new NameMap();
		
		for (int i = 0; i < values.size(); i++){
			int index = (int)values.get(i).getValue();
			if (!n_map.contains(index))
				n_map.add(index, "" + index);
			}
			
		return n_map;
	}
	

}