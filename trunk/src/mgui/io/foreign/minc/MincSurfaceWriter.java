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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileWriter;
import mgui.numbers.MguiBoolean;

import com.sun.j3d.utils.geometry.NormalGenerator;

import foxtrot.Job;
import foxtrot.Worker;


/********************************
 * Writes a mesh to disk in MINC obj format. 
 * <p>See: http://www.bic.mni.mcgill.ca/~david/FAQ/polygons_format.txt 
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public class MincSurfaceWriter extends SurfaceFileWriter {

	
	public MincSurfaceWriter(){
		
	}
	
	public MincSurfaceWriter(File surfaceFile){
		dataFile = surfaceFile;
	}
	
	public boolean writeSurface(final Mesh3DInt mesh){
		if (mesh == null){
			System.out.println("MincSurfaceWriter: mesh is null..");
			return false;
			}
		//TODO throw error here
		if (dataFile == null && dataURL == null){
			System.out.println("MincSurfaceWriter: no output file set..");
			return false;
			}

		Mesh3D mesh3d = mesh.getMesh();
		
		
		try{
			//normals
			ArrayList<Vector3f> normals = mesh3d.getNormals();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile));
			
			//TODO: set these appearance parameters with options
			String header = "P 0.3 0.7 0.5 100 1 " + mesh3d.n;
			String delim = " ";
			bw.write(header + "\n");
			
			for (int i = 0; i < mesh3d.n ; i++){
				Point3f pt = mesh3d.getVertex(i);
				bw.write(pt.x + delim + pt.y + delim + pt.z + "\n");
				}
			//add blank line between points and "normals" section
			bw.write("\n");
				
			
			
			if (normals.size() == mesh3d.n * 3)
				for (int i = 0; i < normals.size(); i+=3){
					bw.write(normals.get(i) + delim + normals.get(i + 1) + delim + normals.get(i + 2) + "\n");
					
					}
			else
				for (int i = 0; i < mesh3d.n; i++){
					bw.write("0" + delim + "0" + delim + "0\n");
					
					}
				
			bw.write("\n");
			
			//131072                          # number of triangles
			//2 0.321569 0 0 1                # 2 == one colour per point  r g b a for point0
			//                                # 0 == one colour for whole set of polygons
			
			bw.write("" + mesh3d.f + "\n");
			bw.write("0 1 1 1 1\n");
			bw.write("\n");
			
			//write random crap
			//3 6 9 12 15 18 21 24            # 131072 integers which can be ignored
			//27 30 33 36 39 42 45 48         # since they are multiples of 3
			//NOTE: 8 per line = 16384 lines
			
			int c = 3, x = 0;
			for (int i = 0; i < mesh3d.f; i++){
				if (x == 8){
					x = 0;
					bw.write("\n");
					}
				bw.write(c + delim);
				c += 3;
				x++;
				}
			
			bw.write("\n\n");
			//write faces, 8 ints per line (don't ask why)
			x = 0;
			for (int i = 0; i < mesh3d.f; i++){
			
				for (int k = 0; k < 3; k++){
					if (x == 8){
						x = 0;
						bw.write("\n");
						}
					bw.write(mesh3d.faces[i * 3 + k] + delim);
					x++;
					}
				}
			
			//done
			bw.close();
			
			return true;
		}catch (IOException e){
			e.printStackTrace();
			return false;
		}
					
	}
	
	@Override
	public boolean writeSurface(final Mesh3DInt mesh, final InterfaceIOOptions options, final ProgressUpdater progress_bar) {
		
		if (progress_bar == null) return writeSurface(mesh);
		
		if (mesh == null) return false;
//		TODO throw error here
		if (dataFile == null && dataURL == null)
			return false;

		return ((MguiBoolean)Worker.post(new Job(){
			
			public MguiBoolean run(){
		
				Mesh3D mesh3d = mesh.getMesh();
				
				try{
					//normals
					ArrayList<Vector3f> normals = mesh3d.getNormals();
					
					if (progress_bar != null){
						progress_bar.setMessage("Writing minc surface '" + mesh.getName() +"': ");
						progress_bar.setMinimum(0);
						progress_bar.setMaximum(mesh3d.n + mesh3d.f + normals.size());
						progress_bar.reset();
						}
					
					BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile));
					
					//TODO: set these appearance parameters with options
					String header = "P 0.3 0.7 0.5 100 1 " + mesh3d.n;
					String delim = " ";
					bw.write(header + "\n");
					
					for (int i = 0; i < mesh3d.n ; i++){
						Point3f pt = mesh3d.getVertex(i);
						bw.write(pt.x + delim + pt.y + delim + pt.z + "\n");
						if (progress_bar != null)
							progress_bar.update(i);
						}
					//add blank line between points and "normals" section
					bw.write("\n");
						
					
					
					if (normals.size() == mesh3d.n * 3)
						for (int i = 0; i < normals.size(); i+=3){
							bw.write(normals.get(i) + delim + normals.get(i + 1) + delim + normals.get(i + 2) + "\n");
							if (progress_bar != null)
								progress_bar.update(mesh3d.n + i);
							}
					else
						for (int i = 0; i < mesh3d.n; i++){
							bw.write("0" + delim + "0" + delim + "0\n");
							if (progress_bar != null)
								progress_bar.update(mesh3d.n + i);
							}
						
					bw.write("\n");
					
					//131072                          # number of triangles
					//2 0.321569 0 0 1                # 2 == one colour per point  r g b a for point0
					//                                # 0 == one colour for whole set of polygons
					
					bw.write("" + mesh3d.f + "\n");
					bw.write("0 1 1 1 1\n");
					bw.write("\n");
					
					//write random crap
					//3 6 9 12 15 18 21 24            # 131072 integers which can be ignored
					//27 30 33 36 39 42 45 48         # since they are multiples of 3
					//NOTE: 8 per line = 16384 lines
					
					int c = 3, x = 0;
					for (int i = 0; i < mesh3d.f; i++){
						if (x == 8){
							x = 0;
							bw.write("\n");
							}
						bw.write(c + delim);
						c += 3;
						x++;
						}
					
					bw.write("\n\n");
					//write faces, 8 ints per line (don't ask why)
					x = 0;
					for (int i = 0; i < mesh3d.f; i++){
						if (progress_bar != null)
							progress_bar.update(i);
						for (int k = 0; k < 3; k++){
							if (x == 8){
								x = 0;
								bw.write("\n");
								}
							bw.write(mesh3d.faces[i * 3 + k] + delim);
							x++;
							}
						}
					
					//done
					bw.close();
					return new MguiBoolean(true);
				}catch (IOException e){
					e.printStackTrace();
					return new MguiBoolean(false);
					}
			}})).getTrue();
		
	}

	
	@Override
	public InterfaceIOType getLoaderComplement(){
		return (new MincSurfaceLoader()).getIOType();
	}

}