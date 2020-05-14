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


package mgui.io.foreign.minc;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.MeshOptionsDialogBox;
import mgui.io.domestic.shapes.SurfaceDataOutputDialogBox;
import mgui.io.domestic.shapes.SurfaceDataOutputOptions;


public class MincSurfaceDataOutDialogBox extends SurfaceDataOutputDialogBox {

	
	public MincSurfaceDataOutDialogBox() {
		super();
	}
	
	public MincSurfaceDataOutDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		//init();
	}
	
	protected void init(){
		super.init();
		this.setDialogSize(550,380);
		this.setTitle("Minc Surface Data Output Options");
		
	}
	
	
	
}