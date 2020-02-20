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


package mgui.neuro.imaging.tasks;

import java.io.IOException;

import mgui.datasources.DataTypes;
import mgui.io.imaging.ImagingIOFunctions;
import mgui.neuro.imaging.camino.CaminoFunctions;


/*******************************************
 * 
 * Read in an rgb raw image and write out a png image
 * 
 * @author Andrew Reid
 *
 */
public class ConvertRawToPNG {

	public static void main(String[] args){
		
		String input = null, output = null;
		int width = -1, height = -1, data_type = -1;
		String colorspace = null;
		//String debug = "none";
		
		//System.out.println("ConvertRGBtoPNG: " +  CaminoFunctions.getArgString(args));
		
		for (int i = 0; i < args.length; i++){
			if (args[i].equals("-colorspace")){
				colorspace = args[i + 1].toLowerCase();
				}
			if (args[i].equals("-inputfile")){
				input = args[i + 1];
				}
			if (args[i].equals("-outputfile")){
				output = args[i + 1];
				}
			if (args[i].equals("-width")){
				width = Integer.parseInt(args[i + 1]);
				}
			if (args[i].equals("-height")){
				height = Integer.parseInt(args[i + 1]);
				}
			if (args[i].equals("-datatype")){
				data_type = DataTypes.getDataBufferType(args[i + 1]);
				//debug = args[i + 1];
				}
			}
		
		//System.out.println("Data type '" + debug + "' = " + data_type);
		
		if (input == null || output == null || width < 0 || height < 0 || data_type < 0 || colorspace == null){
			System.out.println("Usage: ConvertRawToPNG -colorspace <colorspace> -inputfile <input-file> -outputfile <output-file> " +
								"-width <width> -height <height> -datatype <data-type>");
			return;
			}
			
		try{
			if (colorspace.equals("rgb"))
				ImagingIOFunctions.convertRawRGBtoPNG(input, output, width, height, data_type);
			if (colorspace.equals("grey"))
				ImagingIOFunctions.convertRawGreytoPNG(input, output, width, height, data_type);
		
		}catch (IOException e){
			e.printStackTrace();
			}
		
		
	}
	
}