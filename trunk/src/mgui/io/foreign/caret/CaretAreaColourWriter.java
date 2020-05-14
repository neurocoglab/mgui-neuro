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


package mgui.io.foreign.caret;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.util.Colour;

/****************************************************
 * Writes a {@link DiscreteColourMap} to a Caret area colour file, which associates colours (RGB) with names.
 * 
 * <p>See <a href="http://brainvis.wustl.edu/CaretHelpAccount/caret5_help/file_formats/file_formats.html#areaColor">
 * http://brainvis.wustl.edu/CaretHelpAccount/caret5_help/file_formats/file_formats.html#areaColor</a>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CaretAreaColourWriter extends FileWriter {

	
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progressBar) {
		
		CaretAreaColourOutputOptions _options = (CaretAreaColourOutputOptions)options;
		
		setFile(_options.output_file);
		return writeColourMap(_options.colour_map);
		
	}
	
	/**************************************
	 * Writes <code>colour_map</code> to file. The map should have an associated name map; otherwise its
	 * indices will be used as names.
	 * 
	 * @param colour_map The colour map to write
	 * @return
	 */
	public boolean writeColourMap(DiscreteColourMap colour_map){
		
		NameMap nmap = colour_map.getNameMap();
		
		try{
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
			
			if (nmap == null){
				ArrayList<Integer> indices = colour_map.getIndices();
				
				for (int i = 0; i < indices.size(); i++){
					writer.write(indices.get(i) + "\t");
					Colour colour = colour_map.getColour(indices.get(i));
					writer.write((int)(colour.getRed() * 255f) + "\t");
					writer.write((int)(colour.getGreen() * 255f) + "\t");
					writer.write((int)(colour.getBlue() * 255f) + "\n");
					}
				
			}else{
				ArrayList<Integer> indices = nmap.getIndices();
				
				for (int i = 0; i < indices.size(); i++){
					writer.write(nmap.get(indices.get(i)) + "\t");
					Colour colour = colour_map.getColour(indices.get(i));
					writer.write((int)(colour.getRed() * 255f) + "\t");
					writer.write((int)(colour.getGreen() * 255f) + "\t");
					writer.write((int)(colour.getBlue() * 255f) + "\n");
					}
				
				}
			
			writer.close();
			return true;
		
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return false;
	}

}