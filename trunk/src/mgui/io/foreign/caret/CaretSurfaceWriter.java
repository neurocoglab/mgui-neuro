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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileWriter;


/*****************************
 * Writes a mesh to Caret format (two files, topo and coord)
 * <p>See http://brainmap.wustl.edu/caret/caret_help/file_formats/file_formats.html
 * 
 * @author Andrew Reid
 *
 */
public class CaretSurfaceWriter extends SurfaceFileWriter {

	public static final int FORMAT_BINARY = 0;
	public static final int FORMAT_ASCII = 1;
	
	public File coordFile;
	public URL coordURL;
	
	CaretSurfaceOutOptions options;
	
	public int format = FORMAT_BINARY;
	
	public CaretSurfaceWriter(){
		
	}
	
	public CaretSurfaceWriter(File file){
		setFile(file);
	}
	
	public CaretSurfaceWriter(File topoFile, File coordFile){
		setTopoFile(topoFile);
		setCoordFile(coordFile);
	}
	
	public CaretSurfaceWriter(CaretSurfaceOutOptions options){
		setOptions(options);
	}
	
	public void setOptions(CaretSurfaceOutOptions options){
		String dir = "";
		if (options.getOutputDir() != null)
			dir = options.getOutputDir().getAbsolutePath() + "\\";
		
		setTopoFile(new File(dir + options.topoFile + ".topo"));
		setCoordFile(new File(dir + options.coordFile + ".coord"));
		this.options = options;
		if (options.info.encoding.equals("BINARY"))
			format = FORMAT_BINARY;
		else
			format = FORMAT_ASCII;
	}
	
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar){
		if (options == null) return false;
		if (!(options instanceof CaretSurfaceOutOptions)) return false;
		
		CaretSurfaceOutOptions opts = (CaretSurfaceOutOptions)options;
		setOptions(opts);
		
		return writeSurface(opts.mesh, options, progress_bar);
	}
	
	public void setTopoFile(File file){
		setFile(file);
	}
	
	public void setCoordFile(File file){
		coordFile = file;
	}
	
	public boolean writeSurface(Mesh3DInt mesh, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		if (mesh == null) return false;
		
		//TODO throw error here
		if ((dataFile == null && dataURL == null) ||
			(coordFile == null && coordURL == null)) return false;
		
		try{
			if (format == FORMAT_BINARY)
				return writeBinarySurface(mesh, progress_bar);
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return false;
	}
	
	protected boolean writeBinarySurface(Mesh3DInt mesh3d, ProgressUpdater progress_bar) throws IOException{
		
		
		
		try{
			if (dataFile.exists()) dataFile.delete();
			if (coordFile.exists()) coordFile.delete();
			
			RandomAccessFile topo = new RandomAccessFile(dataFile, "rw");
			System.out.println("Writing topo to: " + dataFile.getAbsolutePath());
			RandomAccessFile coord = new RandomAccessFile(coordFile, "rw");
			System.out.println("Writing coords to: " + coordFile.getAbsolutePath());
			
			Mesh3D mesh = mesh3d.getMesh();
			
			//from Caret website (topo):
			//<4-byte-integer-number-of-tiles>
			//<3 4-byte-integers-per-tile>
			//(coord):
			//<4-byte-integer-number-of-coordinates>
			//<3 32-bit-floating-point-numbers-per-coordinate>

			if (options == null) options = getDefaultOptions();
			
			topo.seek(0);
			
			//topo header
			topo.writeBytes("BeginHeader\n");
			topo.writeBytes("comment " + options.info.topo_comment + "\n");
			topo.writeBytes("date " + options.info.date + "\n");
			topo.writeBytes("encoding " + options.info.encoding + "\n");
			topo.writeBytes("perimeter_id " + options.info.topo_perimeter_id + "\n");
			topo.writeBytes("resolution " + options.info.topo_resolution + "\n");
			topo.writeBytes("sampling " + options.info.topo_sampling + "\n");
			topo.writeBytes("EndHeader\n");
			topo.writeBytes("tag-version 1\n");
			
			//no of tiles
			topo.writeInt(mesh.f);
			
			//add indices
			for (int i = 0; i < mesh.f * 3; i++)
				topo.writeInt(mesh.faces[i]);
			
			topo.close();
			
			coord.seek(0);
			
			//coord header
			coord.writeBytes("BeginHeader\n");
			coord.writeBytes("caret-version " + options.info.version + "\n");
			coord.writeBytes("comment " + options.info.coord_comment + "\n");
			coord.writeBytes("configuration_id " + options.info.coord_configuration_id + "\n");
			coord.writeBytes("coordframe_id " + options.info.coord_coordframe_id + "\n");
			coord.writeBytes("date " + options.info.date + "\n");
			coord.writeBytes("encoding " + options.info.encoding + "\n");
			coord.writeBytes("orientation " + options.info.coord_orientation + "\n");
			coord.writeBytes("structure " + options.info.coord_structure + "\n");
			coord.writeBytes("topo_file " + dataFile.getName() + "\n");
			coord.writeBytes("EndHeader\n");
			coord.writeInt(mesh.n);
			
			//add coords
			for (int i = 0; i < mesh.n * 3; i++)
				coord.writeFloat(mesh.nodes[i]);
				
			coord.close();
		
			return true;
			
		}catch (IOException e){
			e.printStackTrace();
			}
		return false;
		
	}
	
	protected CaretSurfaceOutOptions getDefaultOptions(){
		CaretSurfaceOutOptions options = new CaretSurfaceOutOptions();
		options.info.date = InterfaceEnvironment.getNow("dd/mm/yyyy");
		return options;
	}

}