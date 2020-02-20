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


package mgui.io.foreign.minc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import mgui.interfaces.ProgressUpdater;
import mgui.io.domestic.shapes.SurfaceDataFileWriter;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;

/**************************************************************
 * Writes a Minc surface data file, which is simply a text file list of vertex-wise values, one per line, corresponding
 * to a surface mesh.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MincSurfaceDataWriter extends SurfaceDataFileWriter {

	public String number_format;
	//public File dataFile;
	
	public MincSurfaceDataWriter(){
		this("0.0000####");
	}
	
	public MincSurfaceDataWriter(String format){
		setFormat(format);
	}
	
	public MincSurfaceDataWriter(File file){
		this(file, "0.0000####");
	}
	
	public MincSurfaceDataWriter(File file, String format){
		setFile(file);
		setFormat(format);
	}
	
	
	
	public void setFormat(String number_format){
		this.number_format = number_format;
	}
	
	@Override
	public boolean writeValues(ArrayList<MguiNumber> values, ProgressUpdater progress_bar) {
		
		//TODO: throw exception
		if (dataFile == null) return false;
		
		//simple line-for-line
		try{
			if (!dataFile.exists() && !dataFile.createNewFile()){
				System.out.println("Couldn't create output file '" + dataFile.getAbsolutePath() + "'");
				return false;
				}
		
			BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));
			
			for (int i = 0; i < values.size(); i++){
				String valStr = MguiDouble.getString(values.get(i).getValue(), number_format);
				out.write(valStr + "\n");
				}
			
			out.close();
			
			return true;
			
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return false;
	}
	
	

}