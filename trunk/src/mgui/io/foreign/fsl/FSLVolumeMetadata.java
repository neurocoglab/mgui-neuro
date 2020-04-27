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


package mgui.io.foreign.fsl;

import java.awt.image.DataBuffer;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.datasources.DataType;
import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.GridVertexDataColumn;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.util.EndianCorrectInputStream;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;

/************************************************************
 * Metadata for a FSL-style volume file (mgh/mgz).
 * 
 * <p>See <a href="http://ftp.nmr.mgh.harvard.edu/fswiki/FsTutorial/MghFormat">
 * http://ftp.nmr.mgh.harvard.edu/fswiki/FsTutorial/MghFormat</a>
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FSLVolumeMetadata implements VolumeMetadata {

	public static final int MGZ_TYPE_UCHAR = 0;
	public static final int MGZ_TYPE_SHORT = 4;
	public static final int MGZ_TYPE_INT = 1;
	public static final int MGZ_TYPE_FLOAT = 3;
	
	protected int[] data_dims;
	protected int data_type;
	protected Box3D bounds;
	protected int voxel_dim;
	
	protected File data_file;
	protected long header_offset = 284;
	
	public FSLVolumeMetadata(){
		
	}
	
	public FSLVolumeMetadata(File file){
		data_file = file;
	}
	
	@Override
	public void setFromMetadata(VolumeMetadata metadata){
		
		this.setOrigin(metadata.getOrigin());
		this.setDataType(metadata.getDataType());
		this.setDataDims(metadata.getDataDims());
		this.setGeomDims(metadata.getGeomDims());
		this.setAxes(metadata.getAxes());
		
	}
	
	@Override
	public int[] getDataDims() {
		return data_dims;
	}

	@Override
	public float[] getGeomDims() {
		if (bounds == null) return null;
		
		float[] geom_dims = new float[] {bounds.getSAxis().length(),
										 bounds.getTAxis().length(),
										 bounds.getRAxis().length()};
		
		return geom_dims;
	}
	
	@Override
	public Vector3f[] getAxes() {
		
		if (bounds == null) return null;
		
		return new Vector3f[]{bounds.getSAxis(),
							  bounds.getTAxis(),
							  bounds.getRAxis()};
	}

	@Override
	public Point3f getOrigin() {
		if (bounds == null) return null;
		return bounds.getBasePt();
	}

	@Override
	public int getDataType() {

		switch(data_type){
		
			case MGZ_TYPE_UCHAR:
				return DataBuffer.TYPE_USHORT;
			case MGZ_TYPE_SHORT:
				return DataBuffer.TYPE_SHORT;
			case MGZ_TYPE_INT:
				return DataBuffer.TYPE_INT;
			case MGZ_TYPE_FLOAT:
			default:
				return DataBuffer.TYPE_FLOAT;
			}
		
	}
	
	@Override
	public int getVoxelDim() {
		return voxel_dim;
	}

	@Override
	public Box3D getBounds() {
		
		return bounds;
	}

	@Override
	public void setDataDims(int[] dims) {
		this.data_dims = dims;
	}
	
	@Override
	public void setAxes(Vector3f[] axes) {
		
		if (bounds == null){
			bounds = new Box3D();
			}
		
		bounds.setSAxis(axes[0]);
		bounds.setSAxis(axes[1]);
		bounds.setSAxis(axes[2]);
		return;
		
	}

	@Override
	public void setGeomDims(float[] dims) {
		if (bounds == null){
			bounds = new Box3D();
			bounds.setSAxis(new Vector3f(dims[0],0,0));
			bounds.setSAxis(new Vector3f(0,dims[1],0));
			bounds.setSAxis(new Vector3f(0,0,dims[2]));
			return;
			}
		
		Vector3f v = bounds.getSAxis();
		v.scale(dims[0]);
		bounds.setSAxis(v);
		v = bounds.getTAxis();
		v.scale(dims[1]);
		bounds.setTAxis(v);
		v = bounds.getRAxis();
		v.scale(dims[2]);
		bounds.setRAxis(v);
		
	}

	@Override
	public void setVoxelDim(int t) {
		voxel_dim = t;
	}
	
	@Override
	public void setOrigin(Point3f origin) {
		if (bounds == null)
			bounds = new Box3D();
		bounds.setBasePt(origin);
	}

	@Override
	public void setDataType(int type) {
		
		switch(type){
		
			case DataBuffer.TYPE_BYTE:
			case DataBuffer.TYPE_USHORT:
				data_type = MGZ_TYPE_UCHAR;
				
			case DataBuffer.TYPE_SHORT:
				data_type = MGZ_TYPE_SHORT;
				
			case DataBuffer.TYPE_FLOAT:
			case DataBuffer.TYPE_DOUBLE:
				data_type = MGZ_TYPE_FLOAT;
				
			case DataBuffer.TYPE_INT:
			default:
				data_type = MGZ_TYPE_INT;
			
		}
		
	}

	@Override
	public void setFromVolume(Volume3DInt volume) {
		setFromVolume(volume, volume.getCurrentColumn());
	}

	@Override
	public void setFromVolume(Volume3DInt volume, String column) {
		
		Grid3D grid = volume.getGrid();
		data_dims = new int[]{grid.getSizeS(), grid.getSizeT(), grid.getSizeR()};
		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(column);
		setDataType(v_column.getDataTransferType());
		bounds = (Box3D)grid.clone();
		
	}

	/********************************************************
	 * Reads header information from {@code file}.
	 * 
	 * @param header
	 * 
	 * @throws IOException
	 */
	public void readHeader(File header) throws IOException{
		
		EndianCorrectInputStream ecs;
		
		// Note: always big endian
		if (header.getName().endsWith(".mgz"))
			ecs = new EndianCorrectInputStream(new GZIPInputStream(new FileInputStream(header)), true);
		else
			ecs = new EndianCorrectInputStream(new FileInputStream(header), true);
		
		// Version
		int version = ecs.readIntCorrect();
		
		// Width, height, depth
		data_dims = new int[3];
		data_dims[0] = ecs.readIntCorrect();
		data_dims[1] = ecs.readIntCorrect();
		data_dims[2] = ecs.readIntCorrect();
		
		// Frames (t)
		voxel_dim = ecs.readIntCorrect();
		
		// Data type
		data_type = ecs.readIntCorrect();
		
		// dof (not used)
		ecs.readIntCorrect();
		
		short s = ecs.readShortCorrect();
		boolean ras_flag = s > 0;
		
		// Cosines
		if (!ras_flag){
			
			setOrigin(new Point3f(-(float)data_dims[0]/2f,
								  -(float)data_dims[1]/2f,
								  -(float)data_dims[2]/2f));
			setGeomDims(new float[] {(float)data_dims[0],
									 (float)data_dims[1],
								 	 (float)data_dims[2]} );
			
			ecs.close();
			return;
			
			}
			
		float space_x = ecs.readFloatCorrect();
		float space_y = ecs.readFloatCorrect();
		float space_z = ecs.readFloatCorrect();
		
		Matrix4f M = new Matrix4f();
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				M.setElement(j, i, ecs.readFloatCorrect());
		
		Vector3f x_axis = new Vector3f(1,0,0);
		x_axis = GeometryFunctions.transform(x_axis, M);
		Vector3f y_axis = new Vector3f(0,1,0);
		y_axis = GeometryFunctions.transform(y_axis, M);
		Vector3f z_axis = new Vector3f(0,0,1);
		z_axis = GeometryFunctions.transform(z_axis, M);
		
		// C values represent center of image; origin is c minus half geometric dimension
		float c_x = ecs.readFloatCorrect();
		float c_y = ecs.readFloatCorrect();
		float c_z = ecs.readFloatCorrect();
		Point3f origin = new Point3f(-space_x * (float)data_dims[0] / 2f,
									 -space_y * (float)data_dims[1] / 2f,
									 -space_z * (float)data_dims[2] / 2f); 
		origin.set(GeometryFunctions.transform(origin, M));
		//origin.sub(new Point3f(c_x, c_y, c_z), origin);
		
		// Set bounds with these values
		bounds = new Box3D(origin, x_axis, y_axis, z_axis);
		setGeomDims(new float[] {space_x * (float)data_dims[0],
								 space_y * (float)data_dims[1],
							 	 space_z * (float)data_dims[2]} );
		
		ecs.close();
		
	}
	
	/**************************************************
	 * Reads the 3D volume at the specified volume {@code t}, from {@code data_file}.
	 * 
	 * @param data_file
	 * @param t
	 * @param data_type
	 * @return
	 * @throws IOException
	 */
	public ArrayList<MguiNumber> readVolume(File data_file, int t, DataType data_type, ProgressUpdater progress) throws IOException {
		this.data_file = data_file;
		return readVolume(t, data_type, progress);
	}
	
	@Override
	public ArrayList<MguiNumber> readVolume(int t, DataType data_type, ProgressUpdater progress) throws IOException {
		
		if (data_file == null || !data_file.exists())
			throw new IOException("FSLVolumeMetadata.readVolume: No file set or file does not exist.");
		
		// Find offset
		int byte_per_voxel = 8;
		
		switch (this.data_type){
			case MGZ_TYPE_UCHAR:
				byte_per_voxel = 1;
				break;
			case MGZ_TYPE_SHORT:
				byte_per_voxel = 2;
				break;
			case MGZ_TYPE_INT:
			case MGZ_TYPE_FLOAT:
				byte_per_voxel = 4;
				break;
			}
		
		int blob_size = byte_per_voxel * data_dims[0] * data_dims[1] * data_dims[2];
		byte[] blob = readVolBlob(header_offset, t, blob_size);
		
		EndianCorrectInputStream ecs = new EndianCorrectInputStream(new ByteArrayInputStream(blob), true);
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>(data_dims[0] * data_dims[1] * data_dims[2]);
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(data_dims[2]);
			progress.update(0);
			}
		
		// Convert bytes to correct values
		for (int i = 0; i < data_dims[0]; i++){
			for (int j = 0; j < data_dims[1]; j++)
				for (int k = 0; k < data_dims[2]; k++) {
					switch (this.data_type){
						case MGZ_TYPE_UCHAR:
							values.add(NumberFunctions.getInstance(data_type, (double)ecs.readUnsignedByte()));
							// Conversion necessary here?
							break;
						case MGZ_TYPE_SHORT:
							values.add(NumberFunctions.getInstance(data_type, (double)ecs.readShortCorrect()));
							break;
						case MGZ_TYPE_INT:
							values.add(NumberFunctions.getInstance(data_type, (double)ecs.readIntCorrect()));
							break;
						case MGZ_TYPE_FLOAT:
							values.add(NumberFunctions.getInstance(data_type, (double)ecs.readFloatCorrect()));
							break;
						}
					}

			if (progress != null)
				progress.update(i);
			}

		ecs.close();
		blob = null;
		
		return values;
	}
	
	
	private byte[] readVolBlob(long offset, int t, int blob_size) throws IOException {

		byte b[];
		RandomAccessFile raf;
		BufferedInputStream bis;
		
		b = new byte[blob_size];
		
		// read the volume from disk into a byte array
		// read compressed data with BufferedInputStream
		if (data_file.getAbsolutePath().endsWith(".mgz")) {
			bis = new BufferedInputStream(new GZIPInputStream(new FileInputStream(data_file)));
			bis.skip(offset + (long)t * (long)blob_size);
			bis.read(b,0,blob_size);
			bis.close();
		}else {
			raf = new RandomAccessFile(data_file, "r");
			raf.seek(offset + (long)t * (long)blob_size);
			raf.readFully(b,0,blob_size);
			raf.close();
			}

		return b;
		}
	
	
}