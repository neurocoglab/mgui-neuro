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



package mgui.io.standard.gifti;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceInputDialogBox;

/****************************************************************
 * Dialog box for inputting a GIFTI-format surface. Can also handle GIFTI files with no coordinate
 * information, if a pre-existing {@linkplain Mesh3DInt} object is specified.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GiftiSurfaceInDialogBox extends SurfaceInputDialogBox {

	JCheckBox chkSurface = new JCheckBox("Use existing mesh: ");
	InterfaceComboBox cmbSurface = new InterfaceComboBox(RenderMode.LongestItem, true, 500);
	
	Mesh3DInt current_surface = null;
	
	public GiftiSurfaceInDialogBox(){
		super();
	}
	
	public GiftiSurfaceInDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		_init();
	}
	
	private void _init(){
		
		this.setDialogSize(570,460);
		this.setTitle("Surface Input Options");
		
		chkSurface.addActionListener(this);
		chkSurface.setActionCommand("Set Surface");
		
		GiftiInputOptions _options = (GiftiInputOptions)options;
		current_surface = _options.current_mesh;
		
		updateSurfaceCombo();
		
		LineLayoutConstraints c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		mainPanel.add(chkSurface, c);
		c = new LineLayoutConstraints(3, 3, 0.35, 0.6, 1);
		mainPanel.add(cmbSurface, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.4, 1);
		
		
		// Shift table
		c = new LineLayoutConstraints(5, 5, 0.05, 0.9, 1);
		mainPanel.add(lblSurfaces, c);
		
		table_offset = 2;
//		c = new LineLayoutConstraints(6, 10, 0.05, 0.9, 1);
//		mainPanel.add(scrColumns, c);
//		lineLayout.setFlexibleComponent(scrColumns);
	}
	
	void updateSurfaceCombo(){
		
		cmbSurface.removeAllItems();
		
		ShapeSet3DInt shape_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
		if (shape_set == null) return;
		
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		
		for (int j = 0; j < models.size(); j++){
//			ShapeSet3DInt meshes = models.get(j).getModelSet().getShapeType(new Mesh3DInt());
			List<Shape3DInt> meshes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
			for (Shape3DInt mesh : meshes) {
				cmbSurface.addItem(mesh);
				}
			
//			for (int i = 0; i < meshes.members.size(); i++)
//				cmbSurface.addItem(meshes.members.get(i));
			}
	
		if (current_surface == null){
			chkSurface.setSelected(false);
			cmbSurface.setEnabled(false);
		}else{
			cmbSurface.setEnabled(true);
			chkSurface.setSelected(true);
			cmbSurface.setSelectedItem(current_surface);
			}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Set Surface")){
			
			if (chkSurface.isSelected()){
				cmbSurface.setEnabled(true);
				current_surface = (Mesh3DInt)cmbSurface.getSelectedItem();
			}else{
				cmbSurface.setEnabled(false);
				}
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			GiftiInputOptions _options = (GiftiInputOptions)options;
			if (chkSurface.isSelected()){
				_options.current_mesh = current_surface;
			}else{
				_options.current_mesh = null;
				}
			
			}
		
		super.actionPerformed(e);
	}
	
}