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


package mgui.interfaces.neuro;

import java.util.ArrayList;

import mgui.datasources.DataType;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.io.InterfaceIOType;
import mgui.io.domestic.shapes.VolumeInputOptions;

public class CorticalThicknessOptions extends InterfaceOptions {

	public CorticalThicknessOptions(InterfacePanel p){
		panel = p;
	}
	
	public String subject_dir = "";
	public String atlas_dir = "";
	public String subject_prefix = "";
	public String left_contains = "_left";
	public String right_contains = "_right";
	public String gm_contains = "gray_surface";
	public String wm_contains = "white_surface";
	public String mid_contains = "mid_surface";
	
	public InterfaceIOType mesh_loader;
	public InterfaceIOType mesh_data_loader;
	public InterfaceIOType volume_loader;
	
	public InterfaceIOType mesh_writer;
	public InterfaceIOType mesh_data_writer;
	public InterfaceIOType volume_writer;
	
	public InterfacePanel panel;
	
	public ArrayList<InterfaceNeuroAtlas> atlases;
	
	public VolumeInputOptions volume_options = new VolumeInputOptions();
	
	public boolean preserveVolExisting;
	public boolean prepend_subject_name = true;
	public boolean volumes_as_composite = true;
	public boolean show_volumes_3d = false;
	
	public boolean set_intensity_from_histogram = false;
	public double min_pct = 0;
	public double max_pct = 99;
	
	public boolean set_data_type;
	public int data_type;
	
	
}