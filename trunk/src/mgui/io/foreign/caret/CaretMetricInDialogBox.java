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
import java.io.File;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceDataInputDialogBox;


public class CaretMetricInDialogBox extends SurfaceDataInputDialogBox {

	public CaretMetricInDialogBox(){
		
	}
	
	public CaretMetricInDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		init2();
		//((CaretMetricInOptions)opts).displayPanel = panel.getDisplayPanel();
	}
	
	protected void init2(){
		
		table_pos = 3;
		//super.init();
		this.setDialogSize(450,400);
		this.setTitle("Input Metric Paint File Options");
		
	}
	
	protected void updateTable(){
		
		if (options == null) return;
		CaretMetricInOptions opts = (CaretMetricInOptions)options;
		
		//header and new table model
		if (scrColumns != null) mainPanel.remove(scrColumns);
		
		if (opts.getFiles() == null) return;
		
		Vector<Boolean> v_include = new Vector<Boolean>();
		Vector<String> v_files = new Vector<String>();
		Vector<String> v_names = new Vector<String>();
		
		File[] files = options.getFiles();
		String[] names = opts.names;
		boolean[] include = opts.include;
		
		if (files == null || names == null) return;
		
		int p = 0;
		for (int i = 0; i < files.length; i++){
			for (int j = 0; j < opts.columns[i]; j++){
				v_files.add(files[i].getName());
				v_names.add(names[p + j]);
				v_include.add(new Boolean(include[p + j]));
				}
			p += opts.columns[i];
			}
		
		Vector<Vector> values = new Vector<Vector>();
		for (int i = 0; i < v_files.size(); i++){
			Vector v = new Vector(3);
			v.add(v_include.get(i));
			v.add(v_names.get(i));
			v.add(v_files.get(i));
			values.add(v);
			}
		
		Vector<String> header = new Vector<String>(3);
		header.add("Include");
		header.add("Name");
		header.add("Filename");
		
		TableModel model = new TableModel(values, header, 2);
		table = new JTable(model);
		scrColumns = new JScrollPane(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		LineLayoutConstraints c = new LineLayoutConstraints(table_pos, table_pos + 5, 0.05, 0.9, 1);
		mainPanel.add(scrColumns, c);
		mainPanel.updateUI();
		
	}

	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			CaretMetricInOptions opts = (CaretMetricInOptions)options;
			if (cmbMesh.getItemCount() > 0)
				opts.mesh = (Mesh3DInt)cmbMesh.getSelectedItem();
			if (table == null){	
				setVisible(false);
				return;
				}
			for (int i = 0; i < table.getModel().getRowCount(); i++){
				opts.names[i] = (String)table.getValueAt(i, 1);
				opts.include[i] = ((Boolean)table.getValueAt(i, 0)).booleanValue();
				}
			
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
	
	}
	
}