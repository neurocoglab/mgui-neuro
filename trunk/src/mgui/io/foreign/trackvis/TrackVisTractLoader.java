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


package mgui.io.foreign.trackvis;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Polygon3D;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.LPolygon3DInt;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Polygon3DInt;
import mgui.interfaces.shapes.PolygonSet3DInt;
import mgui.io.domestic.shapes.PolygonSet3DLoader;
import mgui.io.util.BufferedRandomAccessFile;

import foxtrot.Job;
import foxtrot.Worker;


public class TrackVisTractLoader extends PolygonSet3DLoader {

	public TrackVisTractLoader(){
		
	}
	
	public TrackVisTractLoader(File file){
		setFile(file);
	}
	
	protected PolygonSet3DInt loadPolygonSetBlocking(InterfaceProgressBar progress_bar) throws IOException{
	
		TrackVisTractHeader header = loadHeader();
		PolygonSet3DInt polygons = new PolygonSet3DInt();
		
		System.out.println("Loading TrackVis tracts: n_count:" + header.n_count);
		int record_size = ((header.n_scalars + 3) * 4) + (header.n_properties * 4);
		
		//load data
		int allocation = 10000000;
		ByteOrder byte_order = ByteOrder.nativeOrder();
		RandomAccessFile raf = new RandomAccessFile(dataFile, "r");
		BufferedRandomAccessFile buffer = new BufferedRandomAccessFile(raf, allocation, byte_order, 1000);
		
		int count = 0;
		int i_skip = 0;
		int skip_count = 0;
		
		if (progress_bar != null){
			progress_bar.setMessage("Loading TrackVis tracts '" + dataFile.getName() + "': ");
			progress_bar.progressBar.setMinimum(0);
			progress_bar.progressBar.setMaximum(header.n_count);
			progress_bar.reset();
			}
		
		int n_pts = 0;
		for (int i = 0; i < header.n_count; i++){
			try{
				n_pts = buffer.readInt();
				int r_size = 4 * (3 * n_pts + header.n_scalars + header.n_properties);
				if ((skip_lines && i_skip < skip) || (skip_min_nodes && n_pts < min_nodes)){
					i_skip++;
					buffer.skip(r_size);
					skip_count++;
				}else{
					i_skip = 0;
					Polygon3D polygon = new Polygon3D();
					float[] nodes = new float[n_pts * 3];
					
					for (int j = 0; j < n_pts; j++){
						nodes[j * 3] = buffer.readFloat();
						nodes[(j * 3) + 1] = buffer.readFloat();
						nodes[(j * 3) + 2] = buffer.readFloat();
						//don't load scalars for now
						//TODO: load scalars into Polygon3DInt
						for (int s = 0; s < header.n_scalars; s++)
							buffer.readFloat();
						
						//don't load properties for now
						for (int p = 0; p < header.n_properties; p++)
							buffer.readFloat();
						}
					polygon.nodes = nodes;
					polygon.n = n_pts;
					count++;
					polygons.addShape(new LPolygon3DInt(polygon), false, false);
					}
				if (progress_bar != null)
					progress_bar.update(i);
			}catch (IOException e){
				e.printStackTrace();
				break;
				}
			}
		
		//System.out.println(count + " tracts loaded. " + skip_count + " skipped..");
		
		buffer.close();
		return polygons;
	}
	
	public TrackVisTractHeader loadHeader() throws IOException{
		if (dataFile == null) return null;
		TrackVisTractHeader header = new TrackVisTractHeader();
		header.readHeader(dataFile);
		return header;
	}

}