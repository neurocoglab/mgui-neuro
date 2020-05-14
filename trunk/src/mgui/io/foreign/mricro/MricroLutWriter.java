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


package mgui.io.foreign.mricro;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import mgui.interfaces.maps.DiscreteColourMap;
import mgui.io.domestic.maps.DiscreteColourMapWriter;

/******************************************************
 * Writes a discrete colour map to an MriCRO-compatible lookup table (LUT) file. If map is larger than
 * 256 indices, it will be truncated.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MricroLutWriter extends DiscreteColourMapWriter {

	public MricroLutWriter(){
		
	}
	
	public MricroLutWriter(File file){
		setFile(file);
	}
	
	public boolean writeMap(DiscreteColourMap map, boolean alpha){
		
		if (dataFile == null) return false;
		
		try{
			RandomAccessFile raf = new RandomAccessFile(dataFile, "r");
			
			byte[] bmap = map.getDiscreteMap(256, 1);
			int n = bmap.length / 4;
			
			for (int c = 0; c < 3; c++)
				for (int i = 0; i < n; i++){
					raf.writeByte(bmap[(i * 4) + c]);
					}
			
			raf.close();
			
			return true;
			
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return false;
	}
	
}