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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteOrder;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.ShapeInputOptions;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import foxtrot.Job;
import foxtrot.Worker;


/************************************
 * Loads a Caret surface from topology and coordinate files, in either binary or ASCII format.
 * <p>See http://brainmap.wustl.edu/caret/caret_help/file_formats/file_formats.html
 * <p>TODO: represent versions
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public class CaretSurfaceLoader extends SurfaceFileLoader {

	public static final int FORMAT_BINARY = 0;
	public static final int FORMAT_ASCII = 1;
	//add version constants..?
	
	public File coordFile;
	public URL coordURL;
	
	ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
	
	protected int format = FORMAT_BINARY;
	
	public CaretSurfaceLoader(){
		
	}
	
	/**********************************************
	 * Instantiates a new loader with the given topo file, with format
	 * {@code FORMAT_BINARY}. Uses a coord file with the same name.
	 * 
	 * @param topoFile
	 */
	public CaretSurfaceLoader(File topoFile){
		setTopoFile(topoFile);
		String name = topoFile.getAbsolutePath();
		if (!name.endsWith(".topo")){
			InterfaceSession.log("CaretSurfaceLoader: topo file must have the extension .topo", 
								 LoggingType.Errors);
			return;
			}
		name = name.substring(0, name.length() - 5) + ".coord";
		setCoordFile(new File(name));
	}
	
	/***********************************************
	 * Instantiates a new loader with the given topo and coord files, with format
	 * {@code FORMAT_BINARY}.
	 * 
	 * @param topoFile
	 * @param coordFile
	 */
	public CaretSurfaceLoader(File topoFile, File coordFile){
		this(topoFile, coordFile, FORMAT_BINARY);
	}
	
	/***********************************************
	 * Instantiates a new loader with the given topo and coord files, and the specified format
	 * (one of {@code FORMAT_BINARY=0, FORMAT_ASCII=1}).
	 * 
	 * @param topoFile
	 * @param coordFile
	 * @param frmt
	 */
	public CaretSurfaceLoader(File topoFile, File coordFile, int frmt){
		setTopoFile(topoFile);
		setCoordFile(coordFile);
		format = frmt;
	}

	public void setTopoFile(File file){
		setFile(file);
	}
	
	public void setCoordFile(File file){
		coordFile = file;
	}
	
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		if (options == null) return false;
		
		CaretSurfaceInOptions opts = (CaretSurfaceInOptions)options;
		
		setTopoFile(opts.getTopoFile());
		setCoordFile(opts.getCoordFile());
		
		try{
			Mesh3DInt mesh = loadSurface(progress_bar);
			if (mesh == null) return false;
			if (opts.names != null)
				mesh.setName(opts.names[0]);
			
			opts.shapeSet.addShape(mesh, true);
			return true;
		}catch (IOException ex){
			InterfaceSession.log("CaretSurfaceLoader: Exception encountered while loading '" + dataFile.getAbsolutePath() + 
					 "'.\nDetails: " + ex.getMessage());
			return false;
			}
		
		
	}
	
	@Override
	public Mesh3DInt loadSurface(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		//TODO throw error here
		if ((dataFile == null && dataURL == null) ||
			(coordFile == null && coordURL == null)) return null;
		if (format == FORMAT_BINARY)
			return loadBinarySurface(progress_bar);
		//TODO implement ASCII loader
		return null;
	}
	
	protected Mesh3DInt loadBinarySurface(final ProgressUpdater progress_bar){
		//Mesh3DInt retMesh = null;
		//from Caret website (topo):
		//tag-version 1
		//<4-byte-integer-number-of-tiles>
		//<3 4-byte-integers-per-tile>
		//(coord):
		//<4-byte-integer-number-of-coordinates>
		//<3 32-bit-floating-point-numbers-per-coordinate>
		
		return
			((Mesh3DInt)Worker.post(new Job(){
			
			public Mesh3DInt run(){
		
				if (dataFile == null) return null;
				
			try{
				Mesh3DInt retMesh = null;
			
				//byte[] data = new byte[4];
				int a, b, c;
				float x, y, z;
				RandomAccessFile topo = new RandomAccessFile(dataFile, "r");
				System.out.println("Reading topo from: " + dataFile.getAbsolutePath());
				RandomAccessFile coord = new RandomAccessFile(coordFile, "r");
				System.out.println("Reading coords from: " + coordFile.getAbsolutePath());
				
				//skip header (for now)
				topo.seek(0);
				String test = topo.readLine();
				BufferedWriter debug = new BufferedWriter(new FileWriter(dataFile.getParentFile().getAbsolutePath() + "\\debug_topo.txt")); 
				//System.out.println(test);
				debug.write(test + "\n");
				while (!test.equals("EndHeader")){
					test = topo.readLine();
					if (test != null)
					debug.write(test + "\n");
					//System.out.println(test);
					}
				//read tag line
				test = topo.readLine();
				debug.write(test + "\n");
				debug.close();
				debug = new BufferedWriter(new FileWriter(dataFile.getParentFile().getAbsolutePath() + "\\debug_coord.txt")); 
				//while (!topo.readLine().equals("tag-version 1"));
				//while (!topo.readUTF().equals("tag-version 1"));
				int noTiles = topo.readInt();
				
				coord.seek(0);
				//skip header (also for now)
				test = coord.readLine();
				debug.write(test + "\n");
				while (!test.equals("EndHeader")){
					test = coord.readLine();
					debug.write(test + "\n");
					}
				debug.close();
				//while (!coord.readLine().equals("EndHeader"));
				
				int noNodes = coord.readInt();
				
				retMesh = new Mesh3DInt(new Mesh3D());
				Mesh3D mesh = (Mesh3D)retMesh.shape3d;
				
				int prog_int = (int)(0.01 * (float)(noNodes * 3));
				int last_prog = 0;
				
				if (progress_bar != null){
					progress_bar.setMessage("Loading Caret mesh '" + dataFile.getName() + "': ");
					progress_bar.setMinimum(0);
					progress_bar.setMaximum(noNodes + noTiles);
					progress_bar.reset();
				}
				
				//load nodes
				for (int i = 0; i < noNodes; i++){
					x = coord.readFloat();
					y = coord.readFloat();
					z = coord.readFloat();
					
					mesh.addVertex(new Point3f(x, y, z));
					if (progress_bar != null){
						progress_bar.update(i);
						//last_prog ++; //= prog_int;
					}
				}
				
				//int count = noNodes;
				
				for (int i = 0; i < noTiles; i++){
					
					a = topo.readInt();
					b = topo.readInt();
					c = topo.readInt();
					
					//add nodes and face (note they are CCW, so reverse)
					mesh.addFace(a, b, c);
					
					//count++;
					if (progress_bar != null){ // && count > last_prog && count < noNodes * 3){
						progress_bar.update(noNodes + i);
						//last_prog += prog_int;
						}
					}
				
				topo.close();
				coord.close();
				
				System.out.println("Caret mesh loaded: " + mesh.n + " nodes, " + mesh.f + " faces");
				return retMesh;
				
			}catch(Exception e){
				e.printStackTrace();
				}
			return null;
			}
		}));
	}
	
	
}