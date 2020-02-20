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

import javax.swing.JFrame;

import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceDataInputDialogBox;
import mgui.io.domestic.shapes.SurfaceDataInputOptions;

/**************************************************************
 * Input data for a MINC (obj) surface 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MincSurfaceDataInDialogBox extends SurfaceDataInputDialogBox {

	public MincSurfaceDataInDialogBox(JFrame frame, InterfaceIOPanel panel, SurfaceDataInputOptions opts){
		super(frame, panel, opts);
		//init();
	}
	
	protected void init(){
		super.init();
		
		this.setDialogSize(450,300);
		this.setTitle("Input Minc Surface Data Options");
		
	}
	
	
	
	
}