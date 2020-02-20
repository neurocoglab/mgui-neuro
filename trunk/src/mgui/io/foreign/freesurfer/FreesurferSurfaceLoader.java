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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

import javax.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import mgui.io.util.EndianCorrectInputStream;
import mgui.io.util.IoFunctions;
import foxtrot.Job;
import foxtrot.Worker;


/*************************************
 * Reads a Freesurfer surface file, binary or ASCII format.
 * <p>See: <a href="http://www.grahamwideman.com/gw/brain/fs/surfacefileformats.htm">
 * http://www.grahamwideman.com/gw/brain/fs/surfacefileformats.htm</a>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FreesurferSurfaceLoader extends SurfaceFileLoader {

	public FreesurferSurfaceLoader(){
		
	}
	
	public FreesurferSurfaceLoader(File file){
		setFile(file);
	}
	
	@Override
	public Mesh3DInt loadSurface(final ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		if (progress_bar == null)
			return loadSurfaceBlocking(null);
		
		Mesh3DInt mesh3d = ((Mesh3DInt)Worker.post(new Job(){
			
			public Mesh3DInt run(){
				return loadSurfaceBlocking(progress_bar);
			}
			
		}));
		
		return mesh3d;
	}
	
	Mesh3DInt loadSurfaceBlocking(ProgressUpdater progress_bar) {
		
		if (dataFile == null){
			System.out.println("No data file specified for Freesurfer surface loader.");
			return null;
			}
		
		try{
			RandomAccessFile raf = new RandomAccessFile(dataFile, "r");
			
			//binary or ascii?
			char test = raf.readChar();
			if (test == '#'){
				raf.close();
				return loadAsciiSurface();
				}
			raf.seek(3);
			//read remaining 2 "magic" bytes
			long length = raf.length();
		
			//bypass comment string; terminates with \n
			while (raf.getFilePointer() < length && test != '\n') 
				test = (char)raf.readByte();
				//test = raf.readChar();
			if (raf.getFilePointer() == length){
				System.out.println("Expected end of Freesurfer surface file '" + dataFile.getAbsolutePath() + "'");
				raf.close();
				return null;
				}
			while (test == '\n') 
				test = (char)raf.readByte();
			raf.seek(raf.getFilePointer() - 1);
			
			//number of vertices and faces
			int nCount = readInt(raf);
			int fCount = readInt(raf);
			
			byte b[] = new byte[nCount*12 + fCount*12];
			raf.readFully(b);
			raf.close();
			EndianCorrectInputStream ecs = new EndianCorrectInputStream(new ByteArrayInputStream(b), true);
			
			if (progress_bar != null){
				progress_bar.setMaximum(nCount + fCount); 
				}
			
			Mesh3D mesh = new Mesh3D(); 
			
			//read data
			for (int i = 0; i < nCount; i++){
				mesh.addVertex(new Point3f(ecs.readFloatCorrect(), ecs.readFloatCorrect(), ecs.readFloatCorrect()));
				//mesh.addVertex(new Point3f(readFloat(raf), readFloat(raf), readFloat(raf)));
				if (progress_bar != null)
					progress_bar.update(i);
				}
			
			for (int i = 0; i < fCount; i++){
				mesh.addFace(ecs.readIntCorrect(), ecs.readIntCorrect(), ecs.readIntCorrect());
				//mesh.addFace(readInt(raf), readInt(raf), readInt(raf));
				if (progress_bar != null)
					progress_bar.update(nCount + i);
				}
			
			ecs.close();
			
			InterfaceSession.log("FreesurferSurfaceLoader: Surface loaded. " + nCount + " vertices. " + fCount + " faces.", 
								 LoggingType.Verbose);
			
			return new Mesh3DInt(mesh);
			
		}catch (Exception e){
			InterfaceSession.log("FreesurferSurfaceLoader: Error loading surface. Reason: " + e.getMessage(), 
								 LoggingType.Errors);
			e.printStackTrace();
			}
		
		return null;
	}
	
	int readInt(RandomAccessFile raf) throws IOException{
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++)
			b[i] = raf.readByte();
		return IoFunctions.byteArrayToInt(b, ByteOrder.BIG_ENDIAN);
	}
	
	float readFloat(RandomAccessFile raf) throws IOException{
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++)
			b[i] = raf.readByte();
		return IoFunctions.byteArrayToFloat(b, ByteOrder.BIG_ENDIAN);
	}
	
	// TODO: implement me
	protected Mesh3DInt loadAsciiSurface() {
		InterfaceSession.log("FreesurferSurfaceLoader: Ascii loader not implemented yet..", 
							 LoggingType.Errors);
		return null;
	}

}