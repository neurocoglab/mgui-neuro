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


package mgui.io.foreign.amira;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.Polygon3D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.LPolygon3DInt;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.PolygonSet3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.domestic.shapes.PolygonSet3DLoader;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import mgui.numbers.MguiBoolean;


public class AmiraTractLoader extends PolygonSet3DLoader {

	public static final int FORMAT_BINARY = 0;
	public static final int FORMAT_ASCII = 1;
	
	protected int format = FORMAT_ASCII;
	
	public AmiraTractLoader(File file){
		setFile(file);
	}
	
	@Override
	public ShapeSet3DInt loadPolygonSetBlocking(ProgressUpdater progress_bar) throws IOException {
		//TODO throw error here
		if (dataFile == null && dataURL == null)
			throw new IOException("AmiraTractLoader: No input file set!");
		if (format == FORMAT_ASCII)
			return loadAsciiPolygons();
		return null;
	}

	protected ShapeSet3DInt loadAsciiPolygons() {
		
		try{
			RandomAccessFile file = new RandomAccessFile(dataFile, "r");
			AmiraHeader header = new AmiraHeader(file);
			
			System.out.println("Amira Mesh 1.0 File");
			System.out.println("Nodes: " + header.vertices + "\tEdges: " + header.edges);
			
			//Mesh3D mesh = new Mesh3D();
			ArrayList<Point3f> nodes = new ArrayList<Point3f>();
			ShapeSet3DInt polySet = new ShapeSet3DInt();
			
			//move to tags and load data
			file.seek(0);
			String line = file.readLine();
			boolean vertDone = false, edgeDone = false;
			boolean doVert = false, doEdge = false;
			boolean isNew = false;
			Point3f thisPt = new Point3f();
			Polygon3D thisPoly = new Polygon3D();
			int coord = 0;
			
			while (line != null && !(vertDone && edgeDone)){
				while (line != null && !line.startsWith("@")){
					if (doVert || doEdge){
						StringTokenizer tokens = new StringTokenizer(line);
						if (doVert){
							while (tokens.hasMoreTokens()){
								if (coord == 3){
									coord = 0;
									nodes.add(thisPt);
									//mesh.addNode(thisPt);
									thisPt = new Point3f();
									}
								setNode(thisPt, coord++, Float.valueOf(tokens.nextToken()).floatValue());
								}
						}else{
							while (tokens.hasMoreTokens()){
								if (isNew){
									LPolygon3DInt intPoly = new LPolygon3DInt(thisPoly);
									polySet.addShape(intPoly, false, false);
									//mesh.addFace(thisFace);
									//thisFace = mesh.new MeshFace3D();
									thisPoly = new Polygon3D();
									isNew = false;
									}
								thisPoly.addNode(nodes.get(Float.valueOf(tokens.nextToken()).intValue()));
								//setFace(thisFace, coord++, Float.valueOf(tokens.nextToken()).intValue());
								}
							}
						}
					line = file.readLine();
					if (line != null){
						if (line.startsWith(header.vertTag)){
							doVert = true;
							coord = 0;
							thisPt = new Point3f();
							line = file.readLine();
							}
						if (line.startsWith(header.edgeTag)){
							coord = 0;
							doEdge = true;
							thisPoly = new Polygon3D();
							//thisFace = mesh.new MeshFace3D();
							line = file.readLine();
							}
						if (line.startsWith("-1")){
							isNew = true;
							line = file.readLine(); 
							}
						}
					}
				//add last point
				if (doVert && thisPt != null) nodes.add(thisPt);
				vertDone |= doVert;
				edgeDone |= doEdge;
				if (doVert)
					System.out.println("" + nodes.size() + " nodes added..");
				if (doEdge)
					System.out.println("" + polySet.getSize() + " polygons added..");
				doVert = false;
				doEdge = false;
				line = file.readLine();
			}
			
			
			//return new Mesh3DInt(mesh);
			polySet.setAttribute("IsClosed", new MguiBoolean(false));
			//polySet.updateMembers();
			polySet.updateShape();
			return polySet;
		
		}catch (IOException e){
			e.printStackTrace();
			return null;
			}
	}
	
	protected void setNode(Point3f pt, int coord, float val){
		switch (coord){
			case 0: 
				pt.x = val;
				break;
			case 1:
				pt.y = val;
				break;
			case 2:
				pt.z = val;
			}
	}
	
	protected void setFace(Mesh3D.MeshFace3D face, int coord, int val){
		switch (coord){
			case 0: 
				face.A = val;
				break;
			case 1:
				face.B = val;
				break;
			case 2:
				face.C = val;
			}
	}
	
	class AmiraHeader{
		
		public long vertices;
		public long edges;
		public String vertTag;
		public String edgeTag;
		
		public AmiraHeader(RandomAccessFile file) throws IOException{
			//TODO check version, etc.
			//TODO implement vertex-wise data loading
			try{
				String line = file.readLine();
				if (!line.startsWith("# AmiraMesh ASCII 1.0")) throw new
					IOException("File does not appear to be of type # AmiraMesh ASCII 1.0");
				
				line = file.readLine();
				
				//first, get node and edge counts
				while (line != null && !line.startsWith("Parameters")){
					StringTokenizer tokens = new StringTokenizer(line);
					if (tokens.hasMoreTokens()){
						String token = tokens.nextToken();
						if (token.equals("define")){
							token = tokens.nextToken();
							if (token.equals("Vertices"))
								vertices = Long.valueOf(tokens.nextToken());
							if (token.equals("Lines"))
								edges = Long.valueOf(tokens.nextToken());
							}
						}
					line = file.readLine();
					}
				
				//second, get node and edge tags (@#)
				while (line != null && !line.startsWith("@")){
					if (line.contains("Vertices") && line.contains("Coordinates"))
						vertTag = line.substring(line.lastIndexOf("@"));
					if (line.contains("Lines"))
						edgeTag = line.substring(line.lastIndexOf("@"));
					line = file.readLine();
					}
			
			}catch (IOException e){
				throw e;
				}
		}
		
	}
	
}