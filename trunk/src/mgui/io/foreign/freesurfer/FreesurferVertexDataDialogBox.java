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


package mgui.io.foreign.freesurfer;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceDataInputDialogBox;
import mgui.io.foreign.freesurfer.FreesurferVertexDataInOptions.Format;

/*********************************************************************
 * Extension of {@code SurfaceDataInputDialogBox} specifically for Freesurfer surface
 * data files.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FreesurferVertexDataDialogBox extends SurfaceDataInputDialogBox {

	JLabel lblFormat = new JLabel("Input format:");
	InterfaceComboBox cmbFormat = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
				  									   true, 500, true);
	
	
	public FreesurferVertexDataDialogBox(){
		
	}
	
	public FreesurferVertexDataDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super (frame, panel, options);
		_init();
	}
	
	private void _init(){
		
		this.setDialogSize(450,400 + InterfaceEnvironment.getLineHeight());
		
		LineLayoutConstraints c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		mainPanel.add(lblFormat, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		mainPanel.add(cmbFormat, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.9, 1);
		mainPanel.add(lblColumns, c);
		
		table_pos = 4;
		updateTable();
		
	}
	
	@Override
	public void showDialog(){
		
		fillFormatCombo();
		
		super.showDialog();
	}
	
	protected void fillFormatCombo(){
		
		cmbFormat.removeAllItems();
		
		cmbFormat.addItem(null);
		cmbFormat.addItem("Annotation");
		cmbFormat.addItem("Label");
		cmbFormat.addItem("Dense1");
		cmbFormat.addItem("Dense2");
		cmbFormat.addItem("Sparse");
		cmbFormat.addItem("Ascii");
		
		FreesurferVertexDataInOptions opts = (FreesurferVertexDataInOptions)options;
		
		if (opts != null){
			if (opts.format != null){
				cmbFormat.setSelectedItem(getFormatStr(opts.format));
			}else if (opts.getFiles() != null){
				File[] files = options.getFiles();
				if (files.length > 0){
					File file_0 = files[0];
					if (file_0.getAbsolutePath().endsWith(".annot")){
						cmbFormat.setSelectedItem("Annotation");
						
					}else if(file_0.getAbsolutePath().endsWith(".label")){
						cmbFormat.setSelectedItem("Label");
						}
					
					}
				}
			}
		
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e){
	
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			FreesurferVertexDataInOptions opts = (FreesurferVertexDataInOptions)options;
			opts.format = getCurrentFormat();
			if (opts.format == null){
				JOptionPane.showMessageDialog(this, 
											  "You must select an input file format!", 
											  "File format", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			}
		
		super.actionPerformed(e);
		
	}
	
	
	private Format getCurrentFormat(){
		
		if (cmbFormat.getSelectedItem() == null){
			return null;
			}
		
		switch((String)cmbFormat.getSelectedItem()){
			case "Annotation":
				return Format.Annotation;
			case "Label":
				return Format.Label;
			case "Dense1":
				return Format.Dense1;
			case "Dense2":
				return Format.Dense2;
			case "Ascii":
				return Format.Ascii;
			case "Sparse":
				return Format.Sparse;
			}
		
		return Format.Annotation;
	}
	
	private String getFormatStr(Format format){
		
		if (format == null) return null;
		
		switch (format){
		
			case Annotation:
				return "Annotation";
			case Label:
				return "Label";
			case Dense1:
				return "Dense1";
			case Dense2:
				return "Dense2";
			case Sparse:
				return "Sparse";
			case Ascii:
				return "Ascii";
				
			}
		
		// shouldn't get here
		return "?";
		
	}
	
}