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


package mgui.io.foreign.caret;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceInputDialogBox;
import mgui.io.domestic.shapes.SurfaceInputOptions;

/******************************************************************
 * Dialog box for inputting a Caret format surface, comprised of "topo" and "coord" files. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CaretSurfaceInDialogBox extends SurfaceInputDialogBox {

	JLabel lblSurfaceName = new JLabel("Name:");
	JTextField txtSurfaceName = new JTextField("");
	
	public CaretSurfaceInDialogBox(){
		
	}
	
	public CaretSurfaceInDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		((SurfaceInputOptions)opts).shapeSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		init2();
	}
	
	void init2(){
		
		mainPanel.remove(lblSurfaces);
		
		LineLayoutConstraints c = new LineLayoutConstraints(3, 3, 0.05, 0.2, 1);
		mainPanel.add(lblSurfaceName, c);
		c = new LineLayoutConstraints(3, 3, 0.2, 0.75, 1);
		mainPanel.add(txtSurfaceName, c);
		
		this.setDialogSize(550,270);
		this.setTitle("Surface Intput Options");
	}
	

	
	protected void updateTable(){
		
	}
	
	
}