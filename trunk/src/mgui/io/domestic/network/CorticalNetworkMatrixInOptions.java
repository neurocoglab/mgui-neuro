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


package mgui.io.domestic.network;

import java.io.File;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;
import mgui.neuro.components.cortical.simple.SimpleCorticalRegion;

/***************************************************
 * Inputs a matrix as a weighted cortical network.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CorticalNetworkMatrixInOptions extends InterfaceIOOptions {

	public enum Format {
		Raw,
		XML,
		Detect;
	}
	
	public Format format = Format.Detect;
	public String[] names;
	public File[] files;
	
	public String name = "no-name";
	public double max = 1, min = 0;
	public SimpleCorticalRegion prototype = new SimpleCorticalRegion();
	public boolean setWeights = true;
	public boolean normalizeWeights = true;
	public boolean createGraph = true;
	public boolean addAllConnections = false;
	
	public CorticalNetworkMatrixInOptions(){
		super();
	}
	
	public CorticalNetworkMatrixInOptions(Format format){
		super();
		this.format = format;
	}
	
	public SimpleCorticalRegion getRegion(String name){
		SimpleCorticalRegion region = (SimpleCorticalRegion)prototype.clone();
		region.setName(name);
		return region;
	}
	
	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
		names = new String[files.length];
		for (int i = 0; i < files.length; i++){
			String name = files[i].getName();
			if (name.lastIndexOf(".") > 0)
				name = name.substring(0, name.lastIndexOf("."));
			names[i] = name;
			}
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
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Select surface data files to input");
		return fc;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		InterfaceSession.log(this.getClass().getCanonicalName() + ": setObject not implemented.", LoggingType.Warnings);
	}
	
}