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


package mgui.interfaces.shapes.neuro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import mgui.geometry.Grid3D;
import mgui.geometry.Mesh3D;
import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.neuro.mesh.NeuroMeshEngine;
import mgui.geometry.neuro.mesh.NeuroMeshFunctions;
import mgui.geometry.neuro.mesh.NeuroMeshFunctionsException;
import mgui.geometry.neuro.mesh.SampleRay;
import mgui.geometry.neuro.mesh.ScalpAndSkullModelOptions;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.VectorSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiNumber;

public class InterfaceNeuroMeshPanel2 extends InterfacePanel implements ActionListener {

	CategoryTitle lblGeneral = new CategoryTitle("GENERAL");
	JLabel lblT1Image = new JLabel("T1 image:");
	JComboBox cmbT1Image = new JComboBox();
	JLabel lblMaskHull = new JLabel("Brain mask hull:");
	JComboBox cmbMaskHull = new JComboBox();
	JLabel lblShapeSet = new JLabel("Target shape set:");
	JComboBox cmbShapeSet = new JComboBox();
	JButton cmdOptions = new JButton("Options");
	JButton cmdResetAll = new JButton("Reset All");
	
	CategoryTitle lblGenerateRays = new CategoryTitle("GENERATE RAYS", "1. GENERATE RAYS [X]");
	JLabel lblRaysPrefix = new JLabel("Prefix:");
	JTextField txtRaysPrefix = new JTextField("rays");
	JCheckBox chkRaysShapes = new JCheckBox(" Generate ray shapes");
	JButton cmdRays = new JButton("Execute");
	
	CategoryTitle lblControlPts = new CategoryTitle("GENERATE CONTROL PTS", "2. GENERATE CONTROL PTS [X]");
	JCheckBox chkControlAverage = new JCheckBox(" Load average control points");
	JTextField txtControlAverage = new JTextField();
	JButton cmdControlAverage = new JButton("Browse..");
	JCheckBox chkControlSubject = new JCheckBox(" Use subject rays");
	JCheckBox chkControlShapes = new JCheckBox(" Generate surfaces");
	JLabel lblControlPrefix = new JLabel("Prefix:");
	JTextField txtControlPrefix = new JTextField("control_pts");
	JButton cmdControlPts = new JButton("Execute");
	
	CategoryTitle lblMeshes = new CategoryTitle("GENERATE MESHES", "3. GENERATE MESHES");
	JCheckBox chkMeshesGenerateControls = new JCheckBox(" Generate control meshes");
	JLabel lblMeshesPrefix = new JLabel("Prefix:");
	JTextField txtMeshesPrefix = new JTextField("neuro");
	JButton cmdMeshes = new JButton("Execute");
	
	
	CategoryTitle lblFixEdges = new CategoryTitle("FIX JAGGED EDGES");
	JCheckBox chkEdgesIslands = new JCheckBox(" Include islands");
	JLabel lblEdgesMaxNbr = new JLabel("Max neighbours:");
	JTextField txtEdgesMaxNbr = new JTextField("1");
	JLabel lblEdgesMesh = new JLabel("Mesh:");
	JComboBox cmbEdgesMesh = new JComboBox();
	JLabel lblEdgesColumn = new JLabel("Column:");
	JComboBox cmbEdgesColumn = new JComboBox();
	JButton cmdEdgesShow = new JButton("Show Nodes");
	JButton cmdEdgesFix = new JButton("Fix Edges");
	
	
	
	CategoryTitle lblVolumeToCortex = new CategoryTitle("VOLUME TO CORTEX");
	JLabel lblVolumeToCortexGrid = new JLabel("Volume");
	InterfaceComboBox cmbVolumeToCortexGrid = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblVolumeToCortexSurface = new JLabel("Surface");
	InterfaceComboBox cmbVolumeToCortexSurface = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JCheckBox chkVolumeToCortexThickness = new JCheckBox(" Thickness column:");
	InterfaceComboBox cmbVolumeToCortexThickness = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JCheckBox chkVolumeToCortexFixedThickness = new JCheckBox(" Fixed thickness:");
	JTextField txtVolumeToCortexFixedThickness = new JTextField("5");
	JLabel lblVolumeToCortexName = new JLabel("Data column name:");
	JTextField txtVolumeToCortexName = new JTextField("no-name");
	JLabel lblVolumeToCortexParameters = new JLabel("Parameters");
	InterfaceAttributePanel volumeToCortexAttr;
	JButton cmdVolumeToCortex = new JButton("Apply");
	
	CategoryTitle lblCortexToVolume = new CategoryTitle("CORTEX TO VOLUME");
	JLabel lblCortexToVolumeGrid = new JLabel("Volume");
	InterfaceComboBox cmbCortexToVolumeGrid = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblCortexToVolumeSurface = new JLabel("Surface");
	InterfaceComboBox cmbCortexToVolumeSurface = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JCheckBox chkCortexToVolumeThickness = new JCheckBox(" Thickness column:");
	InterfaceComboBox cmbCortexToVolumeThickness = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JCheckBox chkCortexToVolumeFixedThickness = new JCheckBox(" Fixed thickness:");
	JTextField txtCortexToVolumeFixedThickness = new JTextField("5");
	JLabel lblCortexToVolumeName = new JLabel("Data column:");
	JComboBox cmbCortexToVolumeName = new JComboBox();
	JLabel lblCortexToVolumeParameters = new JLabel("Parameters");
	InterfaceAttributePanel cortexToVolumeAttr;
	JButton cmdCortexToVolume = new JButton("Apply");
	
	ScalpAndSkullModelOptions options = new ScalpAndSkullModelOptions();
	ArrayList<SampleRay> rays_1, rays_2;
	int[][] control_points;
	
	File average_points_file;
	boolean update_controls = true;
	
	NeuroMeshEngine engine = new NeuroMeshEngine();
	
	public InterfaceNeuroMeshPanel2(){
		if (InterfaceSession.isInit())
			init();
	}
	
	protected void init() {
		_init();
		
		cmdRays.addActionListener(this);
		cmdRays.setActionCommand("Generate Rays");
		cmdOptions.addActionListener(this);
		cmdOptions.setActionCommand("Options");
		cmdResetAll.addActionListener(this);
		cmdResetAll.setActionCommand("Reset All");
		cmdControlPts.addActionListener(this);
		cmdControlPts.setActionCommand("Generate Control Pts");
		cmdControlAverage.addActionListener(this);
		cmdControlAverage.setActionCommand("Load Average Control Pts");
		cmdMeshes.addActionListener(this);
		cmdMeshes.setActionCommand("Generate Meshes");
		cmdEdgesShow.addActionListener(this);
		cmdEdgesShow.setActionCommand("Show Edge Nodes");
		cmdEdgesFix.addActionListener(this);
		cmdEdgesFix.setActionCommand("Fix Jagged Edges");
		cmbEdgesMesh.addActionListener(this);
		cmbEdgesMesh.setActionCommand("Edge Mesh Changed");
		
		chkControlAverage.setSelected(true);
		chkControlAverage.addActionListener(this);
		chkControlAverage.setActionCommand("Control Points Changed Average");
		chkControlSubject.setSelected(false);
		chkControlSubject.addActionListener(this);
		chkControlSubject.setActionCommand("Control Points Changed Subject");
		
		cmdVolumeToCortex.setActionCommand("Volume To Cortex");
		cmdVolumeToCortex.addActionListener(this);
		cmdCortexToVolume.setActionCommand("Cortex To Volume");
		cmdCortexToVolume.addActionListener(this);
		
		cmbCortexToVolumeSurface.setActionCommand("Cortex Surface Changed");
		cmbCortexToVolumeSurface.addActionListener(this);
		cmbVolumeToCortexSurface.setActionCommand("Cortex Surface Changed");
		cmbVolumeToCortexSurface.addActionListener(this);
		
		volumeToCortexAttr = new InterfaceAttributePanel(engine.getAttributes("Volume -> Cortex"));
		cortexToVolumeAttr = new InterfaceAttributePanel(engine.getAttributes("Cortex -> Volume"));
		
		//set this sucker up
		setLayout(new CategoryLayout(20, 5, 200, 10));

		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblGeneral, c);
		lblGeneral.setParentObj(this);
		c = new CategoryLayoutConstraints("GENERAL", 1, 1, 0.05, .3, 1);
		add(lblT1Image, c);
		c = new CategoryLayoutConstraints("GENERAL", 1, 1, 0.35, .6, 1);
		add(cmbT1Image, c);
		c = new CategoryLayoutConstraints("GENERAL", 2, 2, 0.05, .3, 1);
		add(lblMaskHull, c);
		c = new CategoryLayoutConstraints("GENERAL", 2, 2, 0.35, .6, 1);
		add(cmbMaskHull, c);
		c = new CategoryLayoutConstraints("GENERAL", 3, 3, 0.05, .3, 1);
		add(lblShapeSet, c);
		c = new CategoryLayoutConstraints("GENERAL", 3, 3, 0.35, .6, 1);
		add(cmbShapeSet, c);
		c = new CategoryLayoutConstraints("GENERAL", 4, 4, 0.1, .8, 1);
		add(cmdOptions, c);
		c = new CategoryLayoutConstraints("GENERAL", 4, 4, 0.1, .8, 1);
		add(cmdResetAll, c);
		
		c = new CategoryLayoutConstraints();
		add(lblGenerateRays, c);
		lblGenerateRays.setParentObj(this);
		
		c = new CategoryLayoutConstraints("GENERATE RAYS", 1, 1, 0.05, .9, 1);
		add(chkRaysShapes, c);
		c = new CategoryLayoutConstraints("GENERATE RAYS", 2, 2, 0.15, .2, 1);
		add(lblRaysPrefix, c);
		c = new CategoryLayoutConstraints("GENERATE RAYS", 2, 2, 0.35, .5, 1);
		add(txtRaysPrefix, c);
		c = new CategoryLayoutConstraints("GENERATE RAYS", 3, 3, 0.1, .8, 1);
		add(cmdRays, c);
		
		c = new CategoryLayoutConstraints();
		add(lblControlPts, c);
		lblControlPts.setParentObj(this);
		
		c = new CategoryLayoutConstraints("GENERATE CONTROL PTS", 1, 1, 0.05, .9, 1);
		add(chkControlAverage, c);
		c = new CategoryLayoutConstraints("GENERATE CONTROL PTS", 2, 2, 0.1, .8, 1);
		add(cmdControlAverage, c);
		c = new CategoryLayoutConstraints("GENERATE CONTROL PTS", 3, 3, 0.05, .9, 1);
		add(chkControlSubject, c);
		c = new CategoryLayoutConstraints("GENERATE CONTROL PTS", 4, 4, 0.05, .9, 1);
		add(chkControlShapes, c);
		c = new CategoryLayoutConstraints("GENERATE CONTROL PTS", 5, 5, 0.15, .2, 1);
		add(lblControlPrefix, c);
		c = new CategoryLayoutConstraints("GENERATE CONTROL PTS", 5, 5, 0.35, .5, 1);
		add(txtControlPrefix, c);
		c = new CategoryLayoutConstraints("GENERATE CONTROL PTS", 6, 6, 0.1, .8, 1);
		add(cmdControlPts, c);
		
		c = new CategoryLayoutConstraints();
		add(lblMeshes, c);
		lblMeshes.setParentObj(this);
		c = new CategoryLayoutConstraints("GENERATE MESHES", 1, 1, 0.05, .9, 1);
		add(chkMeshesGenerateControls, c);
		c = new CategoryLayoutConstraints("GENERATE MESHES", 2, 2, 0.15, .2, 1);
		add(lblMeshesPrefix, c);
		c = new CategoryLayoutConstraints("GENERATE MESHES", 2, 2, 0.35, .5, 1);
		add(txtMeshesPrefix, c);
		c = new CategoryLayoutConstraints("GENERATE MESHES", 3, 3, 0.1, .8, 1);
		add(cmdMeshes, c);
		
		
		c = new CategoryLayoutConstraints();
		add(lblFixEdges, c);
		lblFixEdges.setParentObj(this);
		
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 1, 1, 0.05, .9, 1);
		add(chkEdgesIslands, c);
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 2, 2, 0.05, .3, 1);
		add(lblEdgesMaxNbr, c);
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 2, 2, 0.35, .6, 1);
		add(txtEdgesMaxNbr, c);
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 3, 3, 0.05, .3, 1);
		add(lblEdgesMesh, c);
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 3, 3, 0.35, .6, 1);
		add(cmbEdgesMesh, c);
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 4, 4, 0.05, .3, 1);
		add(lblEdgesColumn, c);
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 4, 4, 0.35, .6, 1);
		add(cmbEdgesColumn, c);
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 5, 5, 0.05, .9, 1);
		add(cmdEdgesShow, c);
		c = new CategoryLayoutConstraints("FIX JAGGED EDGES", 6, 6, 0.05, .9, 1);
		add(cmdEdgesFix, c);
		
		
		c = new CategoryLayoutConstraints();
		lblVolumeToCortex.isExpanded = false;
		add(lblVolumeToCortex, c);
		lblVolumeToCortex.setParentObj(this);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 1, 1, 0.05, 0.3, 1);
		add(lblVolumeToCortexGrid, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 1, 1, 0.35, 0.6, 1);
		add(cmbVolumeToCortexGrid, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 2, 2, 0.05, 0.3, 1);
		add(lblVolumeToCortexSurface, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 2, 2, 0.35, 0.6, 1);
		add(cmbVolumeToCortexSurface, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 3, 3, 0.05, 0.4, 1);
		add(chkVolumeToCortexThickness, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 3, 3, 0.45, 0.5, 1);
		add(cmbVolumeToCortexThickness, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 4, 4, 0.05, 0.4, 1);
		add(chkVolumeToCortexFixedThickness, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 4, 4, 0.45, 0.5, 1);
		add(txtVolumeToCortexFixedThickness, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 5, 5, 0.05, 0.9, 1);
		add(lblVolumeToCortexParameters, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 6, 10, 0.05, 0.9, 1);
		add(volumeToCortexAttr, c);
		c = new CategoryLayoutConstraints("VOLUME TO CORTEX", 11, 11, 0.05, 0.9, 1);
		add(cmdVolumeToCortex, c);
		
		c = new CategoryLayoutConstraints();
		lblCortexToVolume.isExpanded = false;
		add(lblCortexToVolume, c);
		lblCortexToVolume.setParentObj(this);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 1, 1, 0.05, 0.3, 1);
		add(lblCortexToVolumeGrid, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 1, 1, 0.35, 0.6, 1);
		add(cmbCortexToVolumeGrid, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 2, 2, 0.05, 0.3, 1);
		add(lblCortexToVolumeSurface, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 2, 2, 0.35, 0.6, 1);
		add(cmbCortexToVolumeSurface, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 3, 3, 0.05, 0.4, 1);
		add(chkCortexToVolumeThickness, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 3, 3, 0.45, 0.5, 1);
		add(cmbCortexToVolumeThickness, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 4, 4, 0.05, 0.4, 1);
		add(chkCortexToVolumeFixedThickness, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 4, 4, 0.45, 0.5, 1);
		add(txtCortexToVolumeFixedThickness, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 5, 5, 0.05, 0.9, 1);
		add(lblCortexToVolumeParameters, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 6, 10, 0.05, 0.9, 1);
		add(cortexToVolumeAttr, c);
		c = new CategoryLayoutConstraints("CORTEX TO VOLUME", 11, 11, 0.05, 0.9, 1);
		add(cmdCortexToVolume, c);
		
		updateCombos();
		updateControls();
		
	}
	
	void updateCombos(){
		
		Object current_edge_mesh = cmbEdgesMesh.getSelectedItem();
		Object current = cmbT1Image.getSelectedItem();
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbT1Image, new Volume3DInt());
		if (current != null) cmbT1Image.setSelectedItem(current);
		current = cmbMaskHull.getSelectedItem();
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbMaskHull, new Mesh3DInt());
		if (current != null) cmbMaskHull.setSelectedItem(current);
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbEdgesMesh, new Mesh3DInt());
		if (current_edge_mesh != null)
			cmbEdgesMesh.setSelectedItem(current_edge_mesh);
		current = cmbShapeSet.getSelectedItem();
		InterfaceSession.getWorkspace().populateShapeSetCombo(cmbShapeSet);
		if (current != null) cmbShapeSet.setSelectedItem(current);
		
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbVolumeToCortexGrid, new Volume3DInt());
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbVolumeToCortexSurface, new Mesh3DInt());
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbCortexToVolumeGrid, new Volume3DInt());
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbCortexToVolumeSurface, new Mesh3DInt());
		
	}
	
	void updateMeshColumns(){
		
		Object current = cmbEdgesColumn.getSelectedItem();
		cmbEdgesColumn.removeAllItems();
		
		Mesh3DInt mesh = (Mesh3DInt)cmbEdgesMesh.getSelectedItem();
		if (mesh == null) return;
		
		ArrayList<String> columns = mesh.getVertexDataColumnNames();
		for (int i = 0; i < columns.size(); i++)
			cmbEdgesColumn.addItem(columns.get(i));
		
		if (current != null)
			cmbEdgesColumn.setSelectedItem(current);
		
		mesh = (Mesh3DInt)cmbCortexToVolumeSurface.getSelectedItem();
		this.cmbCortexToVolumeThickness.removeAllItems();
		if (mesh != null){
			columns = mesh.getVertexDataColumnNames();
			for (int i = 0; i < columns.size(); i++)
				cmbCortexToVolumeThickness.addItem(columns.get(i));
			}
		
		mesh = (Mesh3DInt)cmbVolumeToCortexSurface.getSelectedItem();
		this.cmbVolumeToCortexThickness.removeAllItems();
		if (mesh != null){
			columns = mesh.getVertexDataColumnNames();
			for (int i = 0; i < columns.size(); i++)
				cmbVolumeToCortexThickness.addItem(columns.get(i));
			}
		
	}
	
	void updateControls(){
		
		if (!update_controls) return;
		update_controls = false;
		
		boolean completed_0 = true;
				
		if (cmbT1Image.getSelectedItem() == null ||
				cmbMaskHull.getSelectedItem() == null)
			completed_0 = false;
		
		cmdRays.setEnabled(completed_0);
		
		boolean completed_1 = false;
		
		if (rays_1 != null && rays_2 != null){
			lblGenerateRays.setText("1. GENERATE RAYS [\u2713]");
			completed_1 = true;
		}else{
			lblGenerateRays.setText("1. GENERATE RAYS [X]");
			completed_1 = false;
			}
		
		boolean control_ready = (chkControlAverage.isSelected() && average_points_file != null) ||
								(chkControlSubject.isSelected());
		
		cmdControlAverage.setEnabled(chkControlAverage.isSelected());
		cmdControlPts.setEnabled(completed_1 && control_ready);
		
		boolean completed_2 = false;
		
		if (control_points != null){
			lblControlPts.setText("2. GENERATE CONTROL PTS [\u2713]");
			completed_2 = true;
		}else{
			lblControlPts.setText("2. GENERATE CONTROL PTS [X]");
			completed_2 = false;
			}
		
		cmdMeshes.setEnabled(completed_2);
		
		update_controls = true;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Cortex Surface Changed")){
			updateMeshColumns();
			return;
			}
		
		if (e.getActionCommand().equals("Reset All")){
			this.rays_1 = null;
			this.rays_2 = null;
			this.control_points = null;
			this.average_points_file = null;
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Generate Rays")){
			
			//set options
			Mesh3DInt hull_mesh = (Mesh3DInt)cmbMaskHull.getSelectedItem();
			options.brain_surface = hull_mesh.getMesh();
			options.center_of_mass = hull_mesh.getCenterOfGravity();
			options.t1_volume = (Volume3DInt)cmbT1Image.getSelectedItem();
			
			//get rays
			rays_1 = new ArrayList<SampleRay>();
			rays_2 = new ArrayList<SampleRay>();
			InterfaceProgressBar progress = new InterfaceProgressBar();
			progress.register();
			
			try{
				
				NeuroMeshFunctions.getRays(options, rays_1, rays_2, progress);
				
				if (chkRaysShapes.isSelected()){
					//add shapes to shape set
					ShapeSet3DInt shape_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
					VectorSet3DInt v_set = NeuroMeshFunctions.generateRayShapes(rays_1, txtRaysPrefix.getText() + "_rays1");
					if (v_set != null){
						v_set.showData(true);
						shape_set.addShape(v_set);
						}
					v_set = NeuroMeshFunctions.generateRayShapes(rays_2, txtRaysPrefix.getText() + "_rays2");
					if (v_set != null){
						v_set.showData(true);
						shape_set.addShape(v_set);
						}
					}
				
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Rays generated", 
											  "Neuro Mesh Functions", 
											  JOptionPane.INFORMATION_MESSAGE);
			}catch (NeuroMeshFunctionsException ex){
				ex.printStackTrace();
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Error generating rays", 
											  "Neuro Mesh Functions", 
											  JOptionPane.ERROR_MESSAGE);
				}
			
			updateControls();
			progress.deregister();
			return;
			}
		
		if (e.getActionCommand().startsWith("Control Points Changed")){
			update_controls = false;
			if (e.getActionCommand().endsWith("Average"))
				chkControlSubject.setSelected(!chkControlAverage.isSelected());
			else
				chkControlAverage.setSelected(!chkControlSubject.isSelected());
			update_controls = true;
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Load Average Control Pts")){
			JFileChooser jc = null;
			
			if (this.average_points_file != null)
				jc = new JFileChooser(average_points_file);
			else
				jc = new JFileChooser();
			jc.setFileFilter(getTxtFilter());
			jc.showOpenDialog(InterfaceSession.getSessionFrame());
			if (jc.getSelectedFile() != null) this.average_points_file = jc.getSelectedFile();
			
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Generate Control Pts")){
			
			if (rays_2 == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "No rays set!", 
											  "Neuro Mesh Functions", 
											  JOptionPane.ERROR_MESSAGE);
				updateControls();
				return;
				}
			
			String action = "loaded";
			if (chkControlAverage.isSelected()){
				if (this.average_points_file == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No average points file set!", 
												  "Neuro Mesh Functions", 
												  JOptionPane.ERROR_MESSAGE);
					updateControls();
					return;
					}
				
				this.control_points = new int[rays_2.size()][4];
				try{
					BufferedReader reader = new BufferedReader(new FileReader(average_points_file));
					String line = reader.readLine();
					int i = 0; 
					
					while (i < rays_2.size() && line != null){
						StringTokenizer tokens = new StringTokenizer(line);
						for (int j = 0; j < 4; j++)
							control_points[i][j] = Integer.valueOf(tokens.nextToken());
						i++;
						line = reader.readLine();
						}
					reader.close();
					
				}catch (Exception ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error reading average control points.", 
												  "Neuro Mesh Functions", 
												  JOptionPane.ERROR_MESSAGE);
					control_points = null;
					ex.printStackTrace();
					return;
				}
				
			}else{
				control_points = NeuroMeshFunctions.getRayControlPoints(rays_2);
				action = "generated";
				}
				
			if (chkControlShapes.isSelected()){
				Mesh3D[] meshes = NeuroMeshFunctions.getControlPointMeshes(control_points,
																		   rays_2, 
																		   options);
				ShapeSet3DInt shape_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
				for (int i = 0; i < 4; i++)
					shape_set.addShape(new Mesh3DInt(meshes[i], txtControlPrefix.getText() + "_A" + (i + 1)));
				
				}
			
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Control points " + action + ".", 
										  "Neuro Mesh Functions", 
										  JOptionPane.INFORMATION_MESSAGE);
				
			
			updateControls();
			return;
		}
		
		if (e.getActionCommand().equals("Generate Meshes")){
			if (control_points == null || rays_2 == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "No control points or rays set!", 
											  "Neuro Mesh Functions", 
											  JOptionPane.ERROR_MESSAGE);
				updateControls();
				return;
				}
			
			InterfaceProgressBar progress = new InterfaceProgressBar("Generating skull and scalp meshes: ");
			
			Mesh3D[] meshes = NeuroMeshFunctions.getSkullAndScalpMeshes(control_points, rays_2, rays_1, options, progress);
			ShapeSet3DInt shape_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
			
			shape_set.addShape(new Mesh3DInt(meshes[0], txtMeshesPrefix.getText() + "_inner_skull"));
			shape_set.addShape(new Mesh3DInt(meshes[1], txtMeshesPrefix.getText() + "_outer_skull"));
			shape_set.addShape(new Mesh3DInt(meshes[2], txtMeshesPrefix.getText() + "_scalp"));
			if (chkMeshesGenerateControls.isSelected()){
				for (int j = 0; j < 4; j++)
					shape_set.addShape(new Mesh3DInt(meshes[j + 3], txtMeshesPrefix.getText() + "_N" + (j + 1)));
				}
			
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Neuro meshes generated.", 
										  "Neuro Mesh Functions", 
										  JOptionPane.INFORMATION_MESSAGE);
			
			return;
			}
		
		if (e.getActionCommand().equals("Options")){
			NeuroMeshOptionsDialog.showDialog(InterfaceSession.getSessionFrame(), options);
			return;
			}
		
		if (e.getActionCommand().equals("Show Edge Nodes")){
			
			Mesh3DInt mesh_int = (Mesh3DInt)cmbEdgesMesh.getSelectedItem();
			String column = (String)cmbEdgesColumn.getSelectedItem();
			
			if (mesh_int == null || !mesh_int.hasColumn(column)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						  "No mesh specified!", 
						  "Fix Jagged Edges", 
						  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			int max_nbr = Integer.valueOf(txtEdgesMaxNbr.getText()); 
			ArrayList<MguiNumber> values = mesh_int.getVertexData(column);
			ArrayList<Integer> indices = new ArrayList<Integer>(MeshFunctions.getJaggedEdgeNodes(mesh_int.getMesh(), values, 
																								 3, max_nbr, 
																								 chkEdgesIslands.isSelected()).keySet());
			
			if (indices == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						  "No jagged edges found..", 
						  "Fix Jagged Edges", 
						  JOptionPane.INFORMATION_MESSAGE);
			}else{
				mesh_int.setSelectedVertices(indices);
				mesh_int.getAttribute("3D.ShowSelectedVertices").setValue(new MguiBoolean(true));
				}
			
			return;
			}
		
		if (e.getActionCommand().equals("Fix Jagged Edges")){
			
			Mesh3DInt mesh_int = (Mesh3DInt)cmbEdgesMesh.getSelectedItem();
			String column = (String)cmbEdgesColumn.getSelectedItem();
			
			if (mesh_int == null || !mesh_int.hasColumn(column)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "No mesh specified!", 
											  "Fix Jagged Edges", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			int max_nbr = Integer.valueOf(txtEdgesMaxNbr.getText()); 
			ArrayList<MguiNumber> values = mesh_int.getVertexData(column);
			HashMap<Integer, Integer> indices = MeshFunctions.getJaggedEdgeNodes(mesh_int.getMesh(), values, 
																				 3, max_nbr, 
																				 chkEdgesIslands.isSelected());
			
			Iterator<Integer> itr = indices.keySet().iterator();
			
			while (itr.hasNext()){
				int index = itr.next();
				values.get(index).setValue(indices.get(index));
				}
			
			mesh_int.fireShapeModified();
			return;
		}
		
		if (e.getActionCommand().equals("Edge Mesh Changed")){
			updateMeshColumns();
			return;
			}
		
		if (e.getActionCommand().equals("Volume To Cortex")){
			
			if (cmbVolumeToCortexGrid.getSelectedItem() == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No volume specified!");
				return;
				}
			
			if (cmbVolumeToCortexSurface.getSelectedItem() == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No surface specified!");
				return;
				}
			
			Volume3DInt volume = (Volume3DInt)cmbVolumeToCortexGrid.getSelectedItem();
			Mesh3DInt mesh = (Mesh3DInt)cmbVolumeToCortexSurface.getSelectedItem();
			Object thickness = null;
			
			if (chkVolumeToCortexThickness.isSelected()){
				if (cmbVolumeToCortexThickness.getSelectedItem() == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No thickness column specified!");
					return;
					}
				thickness = mesh.getVertexData((String)cmbVolumeToCortexThickness.getSelectedItem());
			}else{
				thickness = Double.valueOf(txtVolumeToCortexFixedThickness.getText());
				}
			
			InterfaceProgressBar progress_bar = new InterfaceProgressBar("Mapping '" + 
																		 mesh.getName() +"': ");
		
			progress_bar.register();
			
			ArrayList<MguiNumber> mapped_values = 
				engine.mapVolumeToCortex(mesh.getMesh(),
										 volume,
										 thickness,
										 progress_bar);
			
			progress_bar.deregister();
			
			if (mapped_values == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Problem mapping volume to surface..");
				return;
				}
			
			String column = (String)volumeToCortexAttr.getAttribute("mesh_column").getValue();
			mesh.addVertexData(column, mapped_values);
			mesh.setCurrentColumn(column);
			
//			mesh.updateShape();
//			mesh.setScene3DObject();
//			mesh.fireShapeModified();
			
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Successfully mapped surface to volume.",
										  "Map Volume to Cortex",
										  JOptionPane.INFORMATION_MESSAGE);
			
			return;
			
			}
		
		if (e.getActionCommand().equals("Cortex To Volume")){
			
			if (cmbCortexToVolumeGrid.getSelectedItem() == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No volume specified!");
				return;
				}
			
			if (cmbCortexToVolumeSurface.getSelectedItem() == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No surface specified!");
				return;
				}
			
			Volume3DInt volume = (Volume3DInt)cmbCortexToVolumeGrid.getSelectedItem();
			Mesh3DInt mesh = (Mesh3DInt)cmbCortexToVolumeSurface.getSelectedItem();
			Object thickness = null;
			
			if (chkCortexToVolumeThickness.isSelected()){
				if (cmbCortexToVolumeThickness.getSelectedItem() == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No thickness column specified!");
					return;
					}
				thickness = mesh.getVertexData((String)cmbCortexToVolumeThickness.getSelectedItem());
			}else{
				thickness = Double.valueOf(txtCortexToVolumeFixedThickness.getText());
				}
			
			InterfaceProgressBar progress_bar = new InterfaceProgressBar("Mapping '" + mesh.getName() +"': ");
		
			progress_bar.register();
			
			Volume3DInt mapped_volume = 
				engine.mapCortexToVolume(mesh,
										 volume,
										 thickness,
										 progress_bar);
			
			progress_bar.deregister();
			
			if (mapped_volume == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Mapping operation failed..");
				return;
				}
			
			String channel = (String)cortexToVolumeAttr.getAttribute("grid_channel").getValue();
			volume.addVertexData(channel, mapped_volume.getVertexData(channel));
			volume.setCurrentColumn(channel);
			
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Successfully mapped surface to volume.",
										  "Map Cortex to Volume",
										  JOptionPane.INFORMATION_MESSAGE);
			
			return;
			}
		
		
	}
	
	FileFilter getTxtFilter(){
		return new FileFilter(){

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getAbsolutePath().endsWith(".txt");
			}

			@Override
			public String getDescription() {
				return "Text files (*.txt)";
			}
			
			
		};
	}
	
	public void showPanel(){
		updateCombos();
		updateControls();
	}
	
	public String toString(){
		return "Neuro Mesh Panel";
	}
		
}