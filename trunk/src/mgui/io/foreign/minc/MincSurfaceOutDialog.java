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

import javax.swing.JFrame;

import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.foreign.caret.CaretMetricOptionsDialog;


public class MincSurfaceOutDialog extends CaretMetricOptionsDialog {

	public MincSurfaceOutDialog(JFrame aFrame, InterfaceDialogUpdater updater){
		super(aFrame, updater, OUTPUT);
		erm();
	}
	
	protected void erm(){
		this.setDialogSize(450,300);
		this.setTitle("Output Minc Surface File Options");
	}
	
	
	
}