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


package mgui.io.foreign.freesurfer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileWriter;
import mgui.io.util.EndianCorrectOutputStream;

/**********************************************************
 * Writes a Freesurfer surface to file, in binary or Ascii format.
 * <p>See: <a href="http://www.grahamwideman.com/gw/brain/fs/surfacefileformats.htm">
 * http://www.grahamwideman.com/gw/brain/fs/surfacefileformats.htm</a>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FreesurferSurfaceWriter extends SurfaceFileWriter {
	
	static final int MAGIC = -2 & 0x00ffffff;
	
	public FreesurferSurfaceWriter(){
		
	}
	
	/**********************************************
	 * Instantiates a writer with the specified output file.
	 * 
	 * @param file
	 */
	public FreesurferSurfaceWriter(File file){
		setFile(file);
	}
	
	@Override
	public boolean writeSurface(Mesh3DInt mesh, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		if (dataFile == null){
			InterfaceSession.log("FreesurferSurfaceWriter: No output file specified.", LoggingType.Errors);
			return false;
			}
		
		FreesurferSurfaceOutputOptions _options = (FreesurferSurfaceOutputOptions)options;
		
		if (_options.format == FreesurferSurfaceOutputOptions.FORMAT_ASCII) 
			return writeSurfaceAscii(mesh, options, progress_bar);
		
		Mesh3D mesh3d = mesh.getMesh();
		
		try{
			RandomAccessFile raf = new RandomAccessFile(dataFile, "rw");
			ByteBuffer buffer = ByteBuffer.allocate(1000);
			buffer.order(_options.byte_order);
			
			if (progress_bar != null){
				progress_bar.setMinimum(0);
				progress_bar.setMaximum(mesh3d.getFaceCount() + mesh3d.getVertexCount());
				}
			
			// Position 0: magic
			raf.write(get_int3(MAGIC, _options.byte_order));
			
			String str = _options.create_string + "\n\n";
			// Position 3: creation string
			buffer.put(str.getBytes());
			
			// Position ?: Vertex count
			buffer.putInt(mesh3d.getVertexCount());
			
			// Position ?: Face count
			buffer.putInt(mesh3d.getFaceCount());
			
			buffer.flip();
			raf.getChannel().write(buffer);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(mesh3d.getVertexCount() * 12 + mesh3d.getFaceCount() * 12);
			EndianCorrectOutputStream eos = new EndianCorrectOutputStream(baos, 
																		  _options.byte_order.equals(ByteOrder.BIG_ENDIAN));
			
			// Write vertices
			ArrayList<Point3f> vertices = mesh3d.getVertices();
			Point3f p;
			for (int i = 0; i < vertices.size(); i++){
				p = vertices.get(i);
				eos.writeFloatCorrect(p.getX());
				eos.writeFloatCorrect(p.getY());
				eos.writeFloatCorrect(p.getZ());
				if (progress_bar != null) progress_bar.iterate();
				}
			
			// Write faces
			ArrayList<MeshFace3D> faces = mesh3d.getFaces();
			for (int i = 0; i < faces.size(); i++){
				MeshFace3D face = faces.get(i);
				eos.writeIntCorrect(face.A);
				eos.writeIntCorrect(face.B);
				eos.writeIntCorrect(face.C);
				if (progress_bar != null) progress_bar.iterate();
				}
			
			raf.write(baos.toByteArray());
			eos.close();
			baos.close();
			raf.close();
			return true;
			
		}catch (Exception ex){
			InterfaceSession.log("FreesurferSurfaceWriter: Error writing surface. Reason: " + ex.getMessage(), 
					 LoggingType.Errors);
			}
		
		return false;
	}

	private byte[] get_int3(int v, ByteOrder byte_order){
		byte b1,b2,b3;
		b3 = (byte)(v & 0xFF);
		b2 = (byte)((v >> 8) & 0xFF);
		b1 = (byte)((v >> 16) & 0xFF);
		if (byte_order.equals(ByteOrder.BIG_ENDIAN))
			return new byte[]{ b1, b2, b3 };
		else
			return new byte[]{ b3, b2, b1 };
	}
	
	protected boolean writeSurfaceAscii(Mesh3DInt mesh, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		InterfaceSession.log("FreesurferSurfaceWriter: Ascii writer not implemented yet..", 
				 LoggingType.Errors);
		return false;
	}
	
}