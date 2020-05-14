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


package mgui.io.domestic.network;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceDataInputOptions;
import mgui.numbers.MguiDouble;


public class CorticalNetworkMatrixInDialogBox extends InterfaceIODialogBox {

	JLabel lblName = new JLabel("Name:"); 
	JTextField txtName = new JTextField(); 
	JLabel lblNonNumeric = new JLabel("Non-numeric value:");
	JTextField txtNonNumeric = new JTextField("0");
	JCheckBox chkSetWeights = new JCheckBox("Set weights");
	JCheckBox chkNormWeights = new JCheckBox("Normalize weights");
	JLabel lblNormMin = new JLabel("Min:");
	JTextField txtNormMin = new JTextField("0");
	JLabel lblNormMax = new JLabel("Max:");
	JTextField txtNormMax = new JTextField("0");
	JCheckBox chkAddAll = new JCheckBox("Add zero-weight connections");
	JCheckBox chkCreateGraph = new JCheckBox("Create graph");
	
	protected LineLayout lineLayout;
	
	//InterfaceDisplayPanel displayPanel;
	//File[] files;
	
	public CorticalNetworkMatrixInDialogBox(){
		
	}
	
	public CorticalNetworkMatrixInDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		//setLocationRelativeTo(displayPanel);
		//displayPanel = panel.getDisplayPanel();
	}
	
	
	protected void init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(350,300);
		this.setTitle("Network Matrix File Options");
		
		chkSetWeights.setActionCommand("Weights");
		chkSetWeights.addActionListener(this);
		chkNormWeights.setActionCommand("Weights");
		chkNormWeights.addActionListener(this);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.45, 1);
		mainPanel.add(lblNonNumeric, c);
		c = new LineLayoutConstraints(1, 1, 0.5, 0.45, 1);
		mainPanel.add(txtNonNumeric, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.6, 1);
		mainPanel.add(chkSetWeights, c);
		c = new LineLayoutConstraints(3, 3, 0.1, 0.6, 1);
		mainPanel.add(chkNormWeights, c);
		c = new LineLayoutConstraints(4, 4, 0.15, 0.2, 1);
		mainPanel.add(lblNormMin, c);
		c = new LineLayoutConstraints(4, 4, 0.25, 0.23, 1);
		mainPanel.add(txtNormMin, c);
		c = new LineLayoutConstraints(4, 4, 0.5, 0.2, 1);
		mainPanel.add(lblNormMax, c);
		c = new LineLayoutConstraints(4, 4, 0.72, 0.23, 1);
		mainPanel.add(txtNormMax, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.6, 1);
		mainPanel.add(chkAddAll, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.6, 1);
		mainPanel.add(chkCreateGraph, c);
		
		initControls();
		
	}
	
	public boolean updateDialog(){
		super.updateDialog();
		initControls();
		return true;
	}
	
	protected void initControls(){
		if (options == null) return;
		CorticalNetworkMatrixInOptions opts = (CorticalNetworkMatrixInOptions)options;
		//opts.displayPanel = displayPanel;
		txtName.setText(opts.name);
		chkSetWeights.setSelected(opts.setWeights);
		chkNormWeights.setSelected(opts.normalizeWeights);
		txtNormMin.setText(MguiDouble.getString(opts.min, "#0.000###"));
		txtNormMax.setText(MguiDouble.getString(opts.max, "#0.000###"));
		chkCreateGraph.setSelected(opts.createGraph);
		chkAddAll.setSelected(opts.addAllConnections);
		
		updateControls();
	}
	
	public void showDialog(){
		if (options == null) options = new CorticalNetworkMatrixInOptions();
		initControls();
		setVisible(true);
	}
	
	protected void updateControls(){
	
		boolean setWeights = chkSetWeights.isSelected();
		boolean normWeights = chkNormWeights.isSelected();
		
		chkNormWeights.setEnabled(setWeights);
		lblNormMin.setEnabled(setWeights && normWeights);
		txtNormMin.setEnabled(setWeights && normWeights);
		lblNormMax.setEnabled(setWeights && normWeights);
		txtNormMax.setEnabled(setWeights && normWeights);
		
		mainPanel.updateUI();
		
	}
	
	public void actionPerformed(ActionEvent e){
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals("Weights")){
			updateControls();
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			CorticalNetworkMatrixInOptions opts = (CorticalNetworkMatrixInOptions)options;
			
			//opts.dataFile = dataFile;
			//opts.displayPanel = displayPanel;
			opts.setWeights = chkSetWeights.isSelected();
			opts.normalizeWeights = chkNormWeights.isSelected();
			opts.min = Double.valueOf(txtNormMin.getText());
			opts.max = Double.valueOf(txtNormMax.getText());
			opts.createGraph = chkCreateGraph.isSelected();
			opts.addAllConnections = chkAddAll.isSelected();
			
			this.setVisible(false);
			}
		
	}
	
	
}