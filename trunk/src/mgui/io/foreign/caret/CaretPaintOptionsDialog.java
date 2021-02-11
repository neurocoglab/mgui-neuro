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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceFilePanel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;


/********************
 * @deprecated use CaretPaintInDialogBox or CaretPaintOutDialogBox
 * 
 * @author AndrewR
 *
 */

public class CaretPaintOptionsDialog extends InterfaceDialogBox implements ActionListener {

	static final String CMD_BROWSE_TOPO = "Browse topo file";
	static final String CMD_BROWSE_COORD = "Browse coord file";
	
	//controls
	JLabel lblMesh = new JLabel("Apply to mesh:");
	JComboBox cmbMesh = new JComboBox();
		
	LineLayout lineLayout;
	
	public CaretPaintOptionsDialog(JFrame aFrame, InterfaceDialogUpdater updater){
		super(aFrame, updater);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		
		init();
		setLocationRelativeTo(aFrame);
		this.setLocation(300, 300);
		if (updater instanceof InterfaceFilePanel)
			parentPanel = (InterfaceFilePanel)updater;
	}
	
	protected void init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,300);
		this.setTitle("Input Caret Paint File Options");
		
		fillMeshCombo();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblMesh, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbMesh, c);
		
	}
	
	public boolean updateDialog(){
		super.updateDialog();
		fillMeshCombo();
		return true;
	}
	
	protected void fillMeshCombo(){
		if (parentPanel == null) return;
		
		cmbMesh.removeAllItems();
		
		List<Shape3DInt> meshes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
		for (Shape3DInt mesh : meshes) {
			cmbMesh.addItem(mesh);
			}
		
//		ShapeSet3DInt meshes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
//		for (int i = 0; i < meshes.members.size(); i++)
//			cmbMesh.addItem(meshes.members.get(i));
	}
	
	public Mesh3DInt getMesh(){
		if (cmbMesh.getSelectedItem() != null)
			return (Mesh3DInt)cmbMesh.getSelectedItem();
		return null;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (parentPanel == null) return;
			
			InterfaceFilePanel panel = (InterfaceFilePanel)parentPanel;
			panel.actionPerformed(new ActionEvent(this, 
												  ActionEvent.ACTION_PERFORMED, 
												  InterfaceFilePanel.Command.Apply_Input_Opt.type));
			
			}
		
	}

}