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

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceOutputOptions;


public class CaretSurfaceOutOptions extends SurfaceOutputOptions {

	public String topoFile;
	public String coordFile;
	
	public CaretSurfaceInfo info = new CaretSurfaceInfo();
	
	public JFileChooser getFileChooser() {
		return getFileChooser(null);
	}

	public JFileChooser getFileChooser(File f) {
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Select output directory for Caret surface");
		return fc;
	}
	
	public File getOutputDir(){
		if (files == null || files.length == 0) return null;
		return files[0];
	}
	
}