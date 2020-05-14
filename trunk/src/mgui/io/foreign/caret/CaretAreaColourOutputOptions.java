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

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.io.InterfaceIOOptions;

/**********************************************************
 * Options for outputting a {@code DiscreteColourMap} as a Caret areacolor file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CaretAreaColourOutputOptions extends InterfaceIOOptions {

	public DiscreteColourMap colour_map;
	public File output_file;
	
	@Override
	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	@Override
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Select output file for area colour");
		return fc;
	}

	@Override
	public File[] getFiles() {
		return new File[]{output_file};
	}

	@Override
	public void setFiles(File[] files) {
		output_file = files[0];
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		colour_map = (DiscreteColourMap)obj;
	}

}