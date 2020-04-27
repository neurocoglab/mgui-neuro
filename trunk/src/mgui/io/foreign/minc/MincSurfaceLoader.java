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


package mgui.io.foreign.minc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import foxtrot.Job;
import foxtrot.Worker;

/***********************************************
 * Reads a MINC obj file into a Mesh3D shape.
 * <p>See: http://www.bic.mni.mcgill.ca/~david/FAQ/polygons_format.txt
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class MincSurfaceLoader extends SurfaceFileLoader {

	public MincSurfaceLoader(){
		
	}
	
	public MincSurfaceLoader(File file){
		setFile(file);
	}
	
	@Override
	public Mesh3DInt loadSurface() throws IOException{
		
		String line;
		//we only care about n_points
		StringTokenizer tokens;
		String token = "";
		
		//get nodes
		ArrayList<Point3f> nodes;
		float[] pt;
		Mesh3D mesh = new Mesh3D();
		
		//try{
			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			//P 0.3 0.7 0.5 100 1 65538         # amb   diff  spec    spec    opacity n_points
	        //									# coef  coef  coef  exponent
		
			tokens = new StringTokenizer(in.readLine());
			while (tokens.hasMoreTokens())
				token = tokens.nextToken();
			//apparently: treat normals as points?
			int n_points = Integer.valueOf(token).intValue();
			
			nodes = new ArrayList<Point3f>(n_points);
			
			// 63.7483 -0.0488046 25.3558     #  x y z  for point 0
			//-62.3075 0.616629 25.0492       #  x y z  for point 1
			
			int prog_int = (int)(0.01 * (float)(n_points * 3));
			int last_prog = 0;
			
			for (int i = 0; i < n_points; i++){
				pt = new float[3];
				//line = in.readLine();
				tokens = new StringTokenizer(in.readLine());
				for (int j = 0; j < 3; j++)
					pt[j] = Float.valueOf(tokens.nextToken()).floatValue();
				
					nodes.add(new Point3f(pt[0], pt[1], pt[2]));
				}
			
			mesh.addVertices(nodes);
			
			//skip normals
			//0.889197 -0.441812 0.118871     #  x y z  for normal for point 0
			//-0.771675 -0.275487 -0.573259   #  x y z  for normal for point 1
			for (int i = 0; i < n_points + 2; i++)
				in.readLine();
			
			//get face count
			//131072                          # number of triangles
			//2 0.321569 0 0 1                # 2 == one colour per point  r g b a for point0
			//                                # 0 == one colour for whole set of polygons
			tokens = new StringTokenizer(in.readLine());
			int n_faces = Integer.valueOf(tokens.nextToken()).intValue();
			int n = n_faces / 8;
			
			//System.out.println(n_faces + " faces...");
			
			//are there node-wise colours?
			//TODO: make option to load colours as data...
			//line = in.readLine();
			tokens = new StringTokenizer(in.readLine());
			token = tokens.nextToken();
			boolean hasColours = (token.equals("2"));
			
			//blank line
			in.readLine();
			
			//if has colours, skip for now
			//0.305882 0 0 1                  # r g b a for point 1   (alpha (a) is always 1)
			//0.0431373 0 0 1                 # r g b a for point 2
			if (hasColours)
				for (int i = 0; i <= n_points; i++)
					in.readLine();
			
			//skip random crap
			//3 6 9 12 15 18 21 24            # 131072 integers which can be ignored
			//27 30 33 36 39 42 45 48         # since they are multiples of 3
			//NOTE: 8 per line = 16384 lines
			for (int i = 0; i < n; i++)
				in.readLine();
			
			//blank line
			in.readLine();
			
			//get faces (finally)
			//0 16386 16388 16386 4098 16387 16388 16386      # 3 * 131072 integers where
			//16387 16388 16387 4100 4098 16389 16391 16389   # 1 2 3 are indices of tri 1
			//           .                                    # 4 5 6 are indices of tri 2
			//           .                                          etc.
			int j = 0;
			int[] face = new int[3];
			int count = n_points;
			
			line = in.readLine();
			while (line != null){
				count++;
				tokens = new StringTokenizer(line);
				//token = tokens.nextToken();
				while (tokens.hasMoreTokens()){
					face[j++] = Integer.valueOf(tokens.nextToken()).intValue();
					if (j == 3){
						mesh.addFace(face[0], face[1], face[2]);
						j = 0;
						}
					}
				line = in.readLine();
				//skip blank line
				if (line != null && line.equals("")){
					line = in.readLine();
					j = 0;
					}
					
				}
			
			in.close();
			
			Mesh3DInt mesh_int = new Mesh3DInt(mesh);
			mesh_int.setFileLoader(getIOType());
			mesh_int.setUrlReference(dataFile.toURI().toURL());
			return mesh_int;
	
	}
	
	@Override
	public Mesh3DInt loadSurface(final ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		
		if (progress_bar == null)
			return loadSurfaceBlocking(null);
		
		Object obj = Worker.post(new Job(){
			
			public Object run(){
				try{
					return loadSurfaceBlocking(progress_bar);
				}catch (IOException ex){
					return ex;
					}
			}
			
		});
		
		if (obj instanceof Mesh3DInt)
			return (Mesh3DInt)obj;
		
		// Rethrow exception
		throw (IOException)obj;
		
	}
	
	
	protected Mesh3DInt loadSurfaceBlocking(ProgressUpdater progress_bar) throws IOException{
		//format, see: http://www.bic.mni.mcgill.ca/~david/FAQ/polygons_format.txt
		String line;
		//we only care about n_points
		StringTokenizer tokens;
		String token = "";
		
		//get nodes
		ArrayList<Point3f> nodes;
		float[] pt;
		Mesh3D mesh = new Mesh3D();
		
			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			//P 0.3 0.7 0.5 100 1 65538         # amb   diff  spec    spec    opacity n_points
	        //									# coef  coef  coef  exponent
		
			line = in.readLine();
			if (line == null){
				InterfaceSession.log("MincSurfaceLoader: No data in file '" + dataFile.getAbsolutePath() + "'.", LoggingType.Errors);
				return null;
				}
			tokens = new StringTokenizer(line);
			while (tokens.hasMoreTokens())
				token = tokens.nextToken();
			//apparently: treat normals as points?
			int n_points = Integer.valueOf(token).intValue();
			
			if (progress_bar != null && progress_bar.allowChanges()){
				progress_bar.setMessage("Loading minc mesh '" + dataFile.getName() + "': ");
				progress_bar.setMinimum(0);
				progress_bar.setMaximum(n_points * 3);
				progress_bar.reset();
			}
			
			nodes = new ArrayList<Point3f>(n_points);
			
			// 63.7483 -0.0488046 25.3558     #  x y z  for point 0
			//-62.3075 0.616629 25.0492       #  x y z  for point 1
			int last_prog = 0;
			
			for (int i = 0; i < n_points; i++){
				
				if (progress_bar != null){
					progress_bar.update(i);
				}
				
				pt = new float[3];
				//line = in.readLine();
				tokens = new StringTokenizer(in.readLine());
				for (int j = 0; j < 3; j++)
					pt[j] = Float.valueOf(tokens.nextToken()).floatValue();
				
					nodes.add(new Point3f(pt[0], pt[1], pt[2]));
				}
			
			last_prog = n_points;
			mesh.addVertices(nodes);
			
			//skip normals
			//0.889197 -0.441812 0.118871     #  x y z  for normal for point 0
			//-0.771675 -0.275487 -0.573259   #  x y z  for normal for point 1
			for (int i = 0; i < n_points + 2; i++)
				in.readLine();
			
			//get face count
			//131072                          # number of triangles
			//2 0.321569 0 0 1                # 2 == one colour per point  r g b a for point0
			//                                # 0 == one colour for whole set of polygons
			tokens = new StringTokenizer(in.readLine());
			int n_faces = Integer.valueOf(tokens.nextToken()).intValue();
			int n = n_faces / 8;
			
			//are there node-wise colours?
			//TODO: make option to load colours as data...
			//line = in.readLine();
			tokens = new StringTokenizer(in.readLine());
			token = tokens.nextToken();
			boolean hasColours = (token.equals("2"));
			
			//blank line
			in.readLine();
			
			//if has colours, skip for now
			//0.305882 0 0 1                  # r g b a for point 1   (alpha (a) is always 1)
			//0.0431373 0 0 1                 # r g b a for point 2
			if (hasColours)
				for (int i = 0; i <= n_points; i++)
					in.readLine();
			
			//skip random crap
			//3 6 9 12 15 18 21 24            # 131072 integers which can be ignored
			//27 30 33 36 39 42 45 48         # since they are multiples of 3
			//NOTE: 8 per line = 16384 lines
			for (int i = 0; i < n; i++)
				in.readLine();
			
			//blank line
			in.readLine();
			
			//get faces (finally)
			//0 16386 16388 16386 4098 16387 16388 16386      # 3 * 131072 integers where
			//16387 16388 16387 4100 4098 16389 16391 16389   # 1 2 3 are indices of tri 1
			//           .                                    # 4 5 6 are indices of tri 2
			//           .                                          etc.
			int j = 0;
			int[] face = new int[3];
			int count = n_points;
			
			line = in.readLine();
			while (line != null){
				count++;
				if (progress_bar != null) { // && count > last_prog && count < n_points * 3){
					progress_bar.update(Math.min(last_prog + count, progress_bar.getMaximum()));
					}
				
				tokens = new StringTokenizer(line);
				while (tokens.hasMoreTokens()){
					face[j++] = Integer.valueOf(tokens.nextToken()).intValue();
					if (j == 3){
						mesh.addFace(face[0], face[1], face[2]);
						j = 0;
						}
					}
				line = in.readLine();
				//skip blank line
				if (line != null && line.equals("")){
					line = in.readLine();
					j = 0;
					}
					
				}
			
				in.close();
				InterfaceSession.log("Minc mesh loaded: " + mesh.n + " nodes, " + mesh.f + " faces",
									 LoggingType.Verbose);
				
				Mesh3DInt mesh_int = new Mesh3DInt(mesh);
				mesh_int.setFileLoader(getIOType());
				InterfaceIOType complement = this.getWriterComplement();
				if (complement != null)
					mesh_int.setFileWriter(complement);
				mesh_int.setUrlReference(dataFile.toURI().toURL());
				return mesh_int;
			
			
		
		
	}
	
	@Override
	public InterfaceIOType getWriterComplement(){
		return (new MincSurfaceWriter()).getIOType();
	}
	
	
}