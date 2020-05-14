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


package mgui.io.foreign.trackvis;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.ReadableByteChannel;

/*****************************************
 * Data structure for a TrackVis tract file header. See http://www.trackvis.org/docs/?subsect=fileformat.
 * 
 * @author Andrew Reid
 *
 */

public class TrackVisTractHeader {

															//  Bytes		Description
															//------------------------------
	public char[] id_string = new char[6];					//  6			ID string for track file. The first 5 characters must be "TRACK".
	public short[] dim = new short[3];						//	6			Dimension of the image volume.
	public float[] voxel_size = new float[3];				//	12			Voxel size of the image volume.
 	public float[] origin = new float[3];					//	12			Origin of the image volume. This field is not yet being used by TrackVis. That means the origin is always (0, 0, 0).
 	public short n_scalars = 0;								//	2			Number of scalars saved at each track point (besides x, y and z coordinates).
 	public char[][] scalar_name = new char[10][20];			//	200			Name of each scalar. Can not be longer than 20 characters each. Can only store up to 10 names.
 	public short n_properties = 0;							//	2			Number of properties saved at each track.
 	public char[][] property_name = new char[10][20];		//	200			Name of each property. Can not be longer than 20 characters each. Can only store up to 10 names.
 	//public char[] reserved = new char[508];				//	508			Reserved space for future version.
 	public char[] voxel_order = new char[4];				//	4			Storing order of the original image data.
	public char[] pad2 = new char[4];						//	4			Paddings.
	public float[] image_orientation_patient = new float[6];//	24			Image orientation of the original image. As defined in the DICOM header.
	public char[] pad1 = new char[2];						//	2			Paddings.
	public byte invert_x = 0;								//	1			Inversion/rotation flags used to generate this track file. For internal use only.
	public byte invert_y = 0;								//	1			Same as above.
	public byte invert_z = 0;								//	1			Same as above.
	public byte swap_xy = 0;								//	1			Same as above.
	public byte swap_yz = 0;								//	1			Same as above.
	public byte swap_zx = 0;								//	1			Same as above.
	public int n_count = 0;									//	4			Number of tracks stored in this track file. 0 means the number was NOT stored.
	public int version = 1;									//	4			Version number. Current version is 1.
	public int hdr_size = 1000;								//	4			Size of the header. Used to determine byte swap. Should be 1000.
	
	
	public TrackVisTractHeader(){
		
	}
	
	public void readHeader(File file) throws IOException {
		
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		//byte order?
		ByteOrder byte_order = ByteOrder.nativeOrder();
		ByteBuffer buffer = ByteBuffer.allocate(1000).order(byte_order);
		raf.seek(0);
		ReadableByteChannel channel = raf.getChannel();
		channel.read(buffer);
		buffer.position(1000 - 4);
		
		hdr_size = buffer.getInt();
		if (hdr_size != 1000){
			if (byte_order.equals(ByteOrder.BIG_ENDIAN))
				byte_order = ByteOrder.LITTLE_ENDIAN;
			else
				byte_order = ByteOrder.BIG_ENDIAN;
			}
		
		buffer = ByteBuffer.allocate(1000).order(byte_order);
		raf.seek(0);
		channel = raf.getChannel();
		channel.read(buffer);
		raf.close();
		
		buffer.position(0);
		
		//read in stuff
		for (int i = 0; i < id_string.length; i++)
			id_string[i] = (char)(buffer.get() + 128);
		for (int i = 0; i < dim.length; i++)
			dim[i] = buffer.getShort();
		for (int i = 0; i < voxel_size.length; i++)
			voxel_size[i] = buffer.getFloat();
		for (int i = 0; i < origin.length; i++)
			origin[i] = buffer.getFloat();
		n_scalars = buffer.getShort();
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 20; j++)
				scalar_name[i][j] = (char)(buffer.get() + 128);
		n_properties = buffer.getShort();
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 20; j++)
				property_name[i][j] = (char)(buffer.get() + 128);
		buffer.position(buffer.position() + 508);
		for (int i = 0; i < voxel_order.length; i++)
			voxel_order[i] = (char)(buffer.get() + 128);
		for (int i = 0; i < pad2.length; i++)
			pad2[i] = (char)(buffer.get() + 128);
		for (int i = 0; i < image_orientation_patient.length; i++)
			image_orientation_patient[i] = buffer.getFloat();
		for (int i = 0; i < pad1.length; i++)
			pad1[i] = (char)(buffer.get() + 128);
		buffer.position(buffer.position() + 6);
		n_count = buffer.getInt();
		version = buffer.getInt();
		hdr_size = buffer.getInt();
		
	}
	
	
}