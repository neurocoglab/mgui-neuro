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


package mgui.io.foreign.interfile;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JProgressBar;

import mgui.geometry.Grid3D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.domestic.shapes.ShapeIOException;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.domestic.shapes.VolumeInputOptions;


/*********************************
 * Loads volume data stored in Interfile format. Header information is stored in an ASCII file with
 * extension 'hdr' or 'ihr'. Voxel data is stored in a binary file with the extension 'img'. The data
 * type for the img file is specified in the header file.
 * 
 * @author Andrew Reid
 * @version 1.0
 */

public class InterfileLoader extends VolumeFileLoader {

	

	InterfileHeader header;

	public boolean setGrid3D(Grid3D grid){
		if (header == null) setHeader();
		
		
		return false;
	}
	
	public void setHeader(){
		
	}
	
	public VolumeMetadata getVolumeMetadata() throws IOException, FileNotFoundException{
		return null;
	}
	
	//stores header information
	public class InterfileHeader {
		
	}

	@Override
	protected boolean setVolume3DBlocking(Volume3DInt volume, String column,
										  int v, VolumeInputOptions options, ProgressUpdater progress)
										  throws ShapeIOException {
		
		
		
		
		return false;
	}
	
}