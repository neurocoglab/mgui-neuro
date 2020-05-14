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


package mgui.io.foreign.fsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileLoader;


public class FSLSurfaceLoader extends SurfaceFileLoader {

	
	public FSLSurfaceLoader(){
		
	}
	
	public FSLSurfaceLoader(File file){
		setFile(file);
	}
	
	@Override
	public Mesh3DInt loadSurface(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		/***************************
		 * Ascii Format:
		 * 
		 * OFF
		 * 
		 * n [# of nodes] f [# of faces] 0
		 * 1 x_0 y_0 z_0
		 * ...
		 * i x_i y_i z_i
		 * ...
		 * n x_n y_n z_n
		 * 
		 * 1 a_0 b_0 c_0
		 * ...
		 * i a_i b_i c_i
		 * ...
		 * f a_f b_f b_f
		 * 
		 ***********************/
		
		if (dataFile == null){
			System.out.println("Mesh3DLoader: No input file specified..");
			return null;
			}
			
		if (!dataFile.exists()){
			System.out.println("Mesh3DLoader: Cannot find file '" + dataFile.getAbsolutePath() + "'");
			return null;
			}
		
		String line;
		StringTokenizer tokens;
		Mesh3D mesh = new Mesh3D();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(dataFile));
			//skip first line
			br.readLine();
			line = br.readLine().trim();
			
			tokens = new StringTokenizer(line);
			
			//first line is # nodes # faces
			int n = Integer.valueOf(tokens.nextToken()).intValue();
			int f = Integer.valueOf(tokens.nextToken()).intValue();
			
			//read nodes
			for (int i = 0; i < n; i++){
				line = br.readLine();
				tokens = new StringTokenizer(line);
				//skip index
				//tokens.nextToken();
				mesh.addVertex(new Point3f(Float.valueOf(tokens.nextToken()).floatValue(),
										 Float.valueOf(tokens.nextToken()).floatValue(),
										 Float.valueOf(tokens.nextToken()).floatValue()));
				}
				
			//read faces
			for (int i = 0; i < f; i++){
				line = br.readLine();
				tokens = new StringTokenizer(line);
				//skip node code (should be 3)
				tokens.nextToken();
				mesh.addFace(Integer.valueOf(tokens.nextToken()).intValue(),
							 Integer.valueOf(tokens.nextToken()).intValue(), 
							 Integer.valueOf(tokens.nextToken()).intValue());
				}
			
			br.close();
			
			mesh.finalize();
			
			System.out.println("Off file '" + dataFile.getAbsolutePath() + "' loaded.");
			System.out.println("Faces: " + mesh.f + " (" + mesh.faces.length / 3f + ")");
			System.out.println("Nodes: " + mesh.n + " (" + mesh.nodes.length / 3f + ")");
			
			return new Mesh3DInt(mesh);
			
		}catch (Exception e){
			e.printStackTrace();
			}
		
		return null;
	}

}