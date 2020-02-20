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


package mgui.io.foreign.mricro;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.maps.DiscreteColourMapInOptions;
import mgui.io.domestic.maps.DiscreteColourMapLoader;
import mgui.io.domestic.maps.NameMapLoader;
import mgui.util.Colour3f;
import mgui.util.Colour4f;


public class MricroLutLoader extends DiscreteColourMapLoader {
	
	public MricroLutLoader(){
		
	}
	
	public MricroLutLoader(File file){
		setFile(file);
	}
	
	/****************************************
	 * Loads a discrete colour map from the specified format.
	 * 
	 * @param format
	 * @return the map
	 * @throws IOException
	 */
	public DiscreteColourMap loadMap(DiscreteColourMapInOptions.Format format, boolean normalized) throws IOException{
		return loadMap();
	}
	
		
	
	public DiscreteColourMap loadMap() throws IOException{
		
		DiscreteColourMap map = new DiscreteColourMap();
		
		try{
			RandomAccessFile raf = new RandomAccessFile(dataFile, "r");
			
			//read 256 reds, blues, greens
			int[] reds = new int[256];
			int[] greens = new int[256];
			int[] blues = new int[256];
			
			for (int i = 0; i < 256; i++)
				reds[i] = raf.read();
			for (int i = 0; i < 256; i++)
				greens[i] = raf.read();
			for (int i = 0; i < 256; i++)
				blues[i] = raf.read();
				
			for (int i = 0; i < 256; i++)
				map.setColour(i, new Colour4f(new Color(reds[i], greens[i], blues[i])));
			
		}catch (IOException e){
			e.printStackTrace();
			return null;
			}
		
		return map;
	}
	
}