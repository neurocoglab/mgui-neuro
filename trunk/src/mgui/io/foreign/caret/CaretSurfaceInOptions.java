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

import java.io.File;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceEnvironment;
import mgui.io.domestic.shapes.SurfaceInputOptions;


/*********************************
 * Options specifying how to read a Caret surface
 * 
 * @author Andrew Reid
 *
 */

public class CaretSurfaceInOptions extends SurfaceInputOptions {

	public CaretSurfaceInOptions(){
		
	}
	
	public void setFiles(File[] files) {
		if (files.length != 2) return;
		this.files = new File[2];
		names = new String[1];
		if (files[0].getName().endsWith("topo")){
			this.files[0] = files[0];
			this.files[1] = files[1];
		}else{
			this.files[0] = files[1];
			this.files[1] = files[0];
			}
		names[0] = files[0].getName();
		if (names[0].lastIndexOf(".") > 0)
			names[0] = names[0].substring(0, names[0].lastIndexOf("."));
	}
	
	public File getTopoFile(){
		return files[0];
	}
	
	public File getCoordFile(){
		return files[1];
	}
	
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = super.getFileChooser(f);
		fc.setDialogTitle("Select Caret surface files to input");
		return fc;
	}
	
}