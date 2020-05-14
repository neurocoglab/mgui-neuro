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
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;

/********************************************************
 * Options for a Caret metric file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CaretMetricOptions extends InterfaceIOOptions {

	public InterfaceDisplayPanel displayPanel;
	public Mesh3DInt mesh;
	
	public boolean isMultiFileEnabled() {
		return true;
	}
	
	public File[] getFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFiles(File[] files) {
		// TODO Auto-generated method stub
		
	}

	public void setDisplayPanel(InterfaceDisplayPanel p){
		displayPanel = p;
	}
	
	public InterfaceDisplayPanel getDisplayPanel(){
		return displayPanel;
	}
	
	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return fc;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		InterfaceSession.log(this.getClass().getCanonicalName() + ": setObject not implemented.", LoggingType.Warnings);
	}
	
}