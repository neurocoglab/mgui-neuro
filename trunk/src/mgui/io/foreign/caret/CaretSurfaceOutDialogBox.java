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

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceDataInputOptions;
import mgui.io.domestic.shapes.SurfaceOutputDialogBox;

/***************************************************************
 * Opotions dialog box for a Caret surface write operation.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CaretSurfaceOutDialogBox extends SurfaceOutputDialogBox {

	JLabel lblTopo = new JLabel("Topo file information");
	JLabel lblTopoFile = new JLabel("Topo file name:");
	JTextField txtTopoFile = new JTextField("");
	JLabel lblTopoPerim = new JLabel("Perimeter ID:");
	JTextField txtTopoPerim = new JTextField("CLOSED");
	JLabel lblTopoRes = new JLabel("Resolution:");
	JTextField txtTopoRes = new JTextField("FULL");
	JLabel lblTopoSample = new JLabel("Sampling:");
	JTextField txtTopoSample = new JTextField("ORIGINAL");
	JLabel lblTopoComment = new JLabel("Comment:");
	JTextArea txtTopoComment = new JTextArea("generated by ar.model.interface.03.2008");
	JScrollPane scrTopoComment = new JScrollPane(txtTopoComment);
	
	JLabel lblCoord = new JLabel("Coord file information");
	JLabel lblCoordFile = new JLabel("Coord file name:");
	JTextField txtCoordFile = new JTextField("");
	JLabel lblCoordConfig = new JLabel("Configuration ID:");
	JTextField txtCoordConfig = new JTextField("FIDUCIAL");
	JLabel lblCoordFrame = new JLabel("Coord frame ID:");
	JTextField txtCoordFrame = new JTextField("SPM5");
	JLabel lblCoordOrient = new JLabel("Orientation:");
	JTextField txtCoordOrient = new JTextField("LPI");
	JLabel lblCoordStruct = new JLabel("Structure:");
	JTextField txtCoordStruct = new JTextField("right");
	JLabel lblCoordComment = new JLabel("Comment:");
	//JTextField txtCoordComment = new JTextField("generated by ar.model.interface.03.2008");
	JTextArea txtCoordComment = new JTextArea("generated by ar.model.interface.03.2008");
	JScrollPane scrCoordComment = new JScrollPane(txtCoordComment);
	
	public CaretSurfaceOutDialogBox(){
		super();
	}
	
	public CaretSurfaceOutDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		init();
	}
	
	protected void init(){
		//super.init();
		txtTopoComment.setColumns(20);
		txtTopoComment.setLineWrap(true);
		txtTopoComment.setRows(5);
		txtTopoComment.setWrapStyleWord(true);
		
		txtCoordComment.setColumns(20);
		txtCoordComment.setLineWrap(true);
		txtCoordComment.setRows(5);
		txtCoordComment.setWrapStyleWord(true);
		
		LineLayoutConstraints c = new LineLayoutConstraints(2, 2, 0.05, 0.5, 1);
		mainPanel.add(lblTopo, c);
		c = new LineLayoutConstraints(3, 3, 0.15, 0.35, 1);
		mainPanel.add(lblTopoFile, c);
		c = new LineLayoutConstraints(3, 3, 0.45, 0.5, 1);
		mainPanel.add(txtTopoFile, c);
		c = new LineLayoutConstraints(4, 4, 0.15, 0.35, 1);
		mainPanel.add(lblTopoPerim, c);
		c = new LineLayoutConstraints(4, 4, 0.45, 0.5, 1);
		mainPanel.add(txtTopoPerim, c);
		c = new LineLayoutConstraints(5, 5, 0.15, 0.35, 1);
		mainPanel.add(lblTopoRes, c);
		c = new LineLayoutConstraints(5, 5, 0.45, 0.5, 1);
		mainPanel.add(txtTopoRes, c);
		c = new LineLayoutConstraints(6, 6, 0.15, 0.35, 1);
		mainPanel.add(lblTopoSample, c);
		c = new LineLayoutConstraints(6, 6, 0.45, 0.5, 1);
		mainPanel.add(txtTopoSample, c);
		c = new LineLayoutConstraints(7, 7, 0.15, 0.35, 1);
		mainPanel.add(lblTopoComment, c);
		c = new LineLayoutConstraints(7, 8, 0.45, 0.5, 1);
		mainPanel.add(scrTopoComment, c);
		
		c = new LineLayoutConstraints(9, 9, 0.05, 0.5, 1);
		mainPanel.add(lblCoord, c);
		c = new LineLayoutConstraints(10, 10, 0.15, 0.35, 1);
		mainPanel.add(lblCoordFile, c);
		c = new LineLayoutConstraints(10, 10, 0.45, 0.5, 1);
		mainPanel.add(txtCoordFile, c);
		c = new LineLayoutConstraints(11, 11, 0.15, 0.35, 1);
		mainPanel.add(lblCoordConfig, c);
		c = new LineLayoutConstraints(11, 11, 0.45, 0.5, 1);
		mainPanel.add(txtCoordConfig, c);
		c = new LineLayoutConstraints(12, 12, 0.15, 0.35, 1);
		mainPanel.add(lblCoordFrame, c);
		c = new LineLayoutConstraints(12, 12, 0.45, 0.5, 1);
		mainPanel.add(txtCoordFrame, c);
		c = new LineLayoutConstraints(13, 13, 0.15, 0.35, 1);
		mainPanel.add(lblCoordOrient, c);
		c = new LineLayoutConstraints(13, 13, 0.45, 0.5, 1);
		mainPanel.add(txtCoordOrient, c);
		c = new LineLayoutConstraints(14, 14, 0.15, 0.35, 1);
		mainPanel.add(lblCoordStruct, c);
		c = new LineLayoutConstraints(14, 14, 0.45, 0.5, 1);
		mainPanel.add(txtCoordStruct, c);
		c = new LineLayoutConstraints(15, 15, 0.15, 0.35, 1);
		mainPanel.add(lblCoordComment, c);
		c = new LineLayoutConstraints(15, 16, 0.45, 0.5, 1);
		mainPanel.add(scrCoordComment, c);
		
		
		this.setDialogSize(450,550);
		this.setTitle("Output Caret Surface File Options");
	}
	
	public void showDialog(){
		updateDialog();
	}
	
	public boolean updateDialog(){
		if (options == null) options = new CaretSurfaceOutOptions();
		return updateDialog(options);
	}
	
	public boolean updateDialog(InterfaceOptions options){
		this.options = (InterfaceIOOptions)options;
		
		CaretSurfaceOutOptions opts = (CaretSurfaceOutOptions)options;
		
		txtTopoFile.setText(opts.topoFile);
		txtTopoPerim.setText(opts.info.topo_perimeter_id);
		txtTopoRes.setText(opts.info.topo_resolution);
		txtTopoSample.setText(opts.info.topo_sampling);
		txtTopoComment.setText(opts.info.topo_comment);

		txtCoordComment.setText(opts.info.coord_comment);
		txtCoordConfig.setText(opts.info.coord_configuration_id);
		txtCoordConfig.setText(opts.info.coord_coordframe_id);
		txtCoordOrient.setText(opts.info.coord_orientation);
		txtCoordStruct.setText(opts.info.coord_structure);
		
		//System.out.println("Fill mesh combo..");
		fillMeshCombo();
		
		setVisible(true);
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (options == null) return;
			
			CaretSurfaceOutOptions opts = (CaretSurfaceOutOptions)options;
			opts.mesh = getMesh();
			opts.coordFile = getCoordFile();
			opts.info.coord_comment = txtCoordComment.getText();
			opts.info.coord_configuration_id = txtCoordConfig.getText();
			opts.info.coord_coordframe_id = txtCoordConfig.getText();
			opts.info.coord_orientation = txtCoordOrient.getText();
			opts.info.coord_structure = txtCoordStruct.getText();
			
			opts.topoFile = getTopoFile();
			opts.info.topo_comment = txtTopoComment.getText();
			opts.info.topo_perimeter_id = txtTopoPerim.getText();
			opts.info.topo_resolution = txtTopoRes.getText();
			opts.info.topo_sampling = txtTopoSample.getText();
			
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	public String getTopoFile(){
		return txtTopoFile.getText();
	}
	
	public String getCoordFile(){
		return txtCoordFile.getText();
	}
	
}