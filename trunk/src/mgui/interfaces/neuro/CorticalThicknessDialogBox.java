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


package mgui.interfaces.neuro;

import java.awt.event.ActionEvent;
import java.awt.image.DataBuffer;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsTabbedDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.io.domestic.maps.NameMapLoader;
import mgui.io.domestic.shapes.SurfaceDataFileLoader;
import mgui.io.domestic.shapes.SurfaceDataFileWriter;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import mgui.io.domestic.shapes.SurfaceFileWriter;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.domestic.shapes.VolumeFileWriter;
import mgui.io.domestic.shapes.VolumeInputOptions;
import mgui.io.foreign.minc.MincSurfaceDataLoader;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiNumber;

/**************************************************************
 * Defines a cortical thickness project
 * 
 * 
 * @version 1.0
 * @since 1.0
 * @author Andrew Reid
 *
 */
public class CorticalThicknessDialogBox extends InterfaceOptionsTabbedDialogBox {

	// --- GENERAL ---
	JLabel lblProfileName = new JLabel("Profile name:");
	JTextField txtProfileName = new JTextField("profile 1");
	JButton cmdLoadProfile = new JButton("Load from file");
	JButton cmdSaveProfile = new JButton("Save to file");
	
	// --- I/O ---
	JLabel lblSubjectDir = new JLabel("Subject dir:");
	JTextField txtSubjectDir = new JTextField();
	JButton cmdBrowseDir = new JButton("Browse..");
	JLabel lblAtlasDir = new JLabel("Atlas dir:");
	JTextField txtAtlasDir = new JTextField();
	JButton cmdBrowseAtlas = new JButton("Browse..");
	JLabel lblSubjectPrefix = new JLabel("Subject prefix:");
	JTextField txtSubjectPrefix = new JTextField();
	JLabel lblLeftContains = new JLabel("Left contains:");
	JTextField txtLeftContains = new JTextField();
	JLabel lblRightContains = new JLabel("Right contains:");
	JTextField txtRightContains = new JTextField();
	JLabel lblGMContains = new JLabel("GM contains:");
	JTextField txtGMContains = new JTextField();
	JLabel lblWMContains = new JLabel("WM contains:");
	JTextField txtWMContains = new JTextField();
	JLabel lblMidContains = new JLabel("Mid contains:");
	JTextField txtMidContains = new JTextField();
	JCheckBox chkPrependSubject = new JCheckBox(" Prepend subject name");
	
	
	JLabel lblMeshLoader = new JLabel("Mesh loader:");
	JComboBox cmbMeshLoader = new JComboBox();
	JLabel lblMeshDataLoader = new JLabel("Mesh data loader:");
	JComboBox cmbMeshDataLoader = new JComboBox();
	JLabel lblVolumeLoader = new JLabel("Volume loader:");
	JComboBox cmbVolumeLoader = new JComboBox();
	
	JLabel lblMeshWriter = new JLabel("Mesh writer:");
	JComboBox cmbMeshWriter = new JComboBox();
	JLabel lblMeshDataWriter = new JLabel("Mesh data writer:");
	JComboBox cmbMeshDataWriter = new JComboBox();
	JLabel lblVolumeWriter = new JLabel("Volume writer:");
	JComboBox cmbVolumeWriter = new JComboBox();
	
	// --- VOLUME ---
	JLabel lblVolDims = new JLabel("DIMENSIONS");
	JLabel lblVolFromHeader = new JLabel("<html><center>Set from<p>header</center></html>", JLabel.CENTER);
	JCheckBox chkVolOrigin = new JCheckBox();
	JLabel lblVolOrigin = new JLabel("Origin");
	JTextField txtVolOriginX = new JTextField("0");
	JTextField txtVolOriginY = new JTextField("0");
	JTextField txtVolOriginZ = new JTextField("0");
	
	JCheckBox chkVolDim = new JCheckBox();
	JLabel lblVolDim = new JLabel("Image Dim");
	JTextField txtVolDimX = new JTextField("0");
	JTextField txtVolDimY = new JTextField("0");
	JTextField txtVolDimZ = new JTextField("0");
	
	JCheckBox chkVolGeom = new JCheckBox();
	JLabel lblVolGeom = new JLabel("Geometry");
	JTextField txtVolGeomX = new JTextField("0");
	JTextField txtVolGeomY = new JTextField("0");
	JTextField txtVolGeomZ = new JTextField("0");
	
	JLabel lblVolFlipX = new JLabel("Flip X");
	JCheckBox chkVolFlipX = new JCheckBox();
	JLabel lblVolFlipY = new JLabel("Flip Y");
	JCheckBox chkVolFlipY = new JCheckBox();
	JLabel lblVolFlipZ = new JLabel("Flip Z");
	JCheckBox chkVolFlipZ = new JCheckBox();
	
	JLabel lblVolIntensity = new JLabel("INTENSITY");
	JCheckBox chkVolPreserveExisting = new JCheckBox("Preserve existing parameters");
	JCheckBox chkVolDataType = new JCheckBox(" Data type:");
	JComboBox cmbVolDataType = new JComboBox();
	JLabel lblVolColourMap = new JLabel("Colour map");
	JComboBox cmbVolColourMap = new JComboBox();
	//JLabel lblVolAlpha = new JLabel("Set alpha");
	JCheckBox chkVolAlpha = new JCheckBox(" Set alpha");
	JCheckBox chkVolFixedIntensity = new JCheckBox(" Fixed intensities:");
	JLabel lblVolBrightness = new JLabel("Brightness");
	JTextField txtVolBrightness = new JTextField("0.5");
	JLabel lblVolContrast = new JLabel("Contrast");
	JTextField txtVolContrast = new JTextField("1.0");
	JCheckBox chkVolRelIntensity = new JCheckBox(" Relative intensities:");
	JLabel lblVolMinPct = new JLabel("Low %ile");
	JTextField txtVolMinPct = new JTextField("0");
	JLabel lblVolMaxPct = new JLabel("High %ile");
	JTextField txtVolMaxPct = new JTextField("99");
	JLabel lblVolAlphaMin = new JLabel("Min Alpha");
	JTextField txtVolAlphaMin = new JTextField("0.0");
	JLabel lblVolAlphaMax = new JLabel("Max Alpha");
	JTextField txtVolAlphaMax = new JTextField("1.0");
	JCheckBox chkVolAsComposite = new JCheckBox("Volumes as composite");
	JCheckBox chkVolShow3D = new JCheckBox("Show 3D");
	
	// --- ATLASES ---
	JLabel lblAtlases = new JLabel("ATLASES");
	JLabel lblCurrentAtlases = new JLabel("Current atlases:");
	JScrollPane lstAtlases = new JScrollPane();
	AtlasTableModel atlas_table_model;
	JButton cmdAddAtlas = new JButton("Add new");
	JButton cmdRemoveAtlas = new JButton("Remove");
	
	public CorticalThicknessDialogBox(){
		super();
	}
	
	public CorticalThicknessDialogBox(JFrame f, CorticalThicknessOptions opts){
		super(f, opts);
		init();
	}
	
	public void setOptions(CorticalThicknessOptions opts){
		this.options = opts;
	}
	
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		this.setTitle("Define cortical thickness project");
		
		cmdBrowseDir.addActionListener(this);
		cmdBrowseDir.setActionCommand("Browse Dir");
		cmdBrowseAtlas.addActionListener(this);
		cmdBrowseAtlas.setActionCommand("Browse Atlas");
		
		chkPrependSubject.setSelected(true);
		chkVolFixedIntensity.setSelected(true);
		
		fillCombos();
		
		chkVolOrigin.addActionListener(this);
		chkVolOrigin.setActionCommand("Vol Changed");
		chkVolDim.addActionListener(this);
		chkVolDim.setActionCommand("Vol Changed");
		chkVolGeom.addActionListener(this);
		chkVolGeom.setActionCommand("Vol Changed");
		chkVolAsComposite.setSelected(true);
		chkVolShow3D.setSelected(false);
		chkVolFixedIntensity.setActionCommand("Vol Intensity Fixed");
		chkVolFixedIntensity.addActionListener(this);
		chkVolRelIntensity.setActionCommand("Vol Intensity Rel");
		chkVolRelIntensity.addActionListener(this);
		chkVolDataType.addActionListener(this);
		chkVolDataType.setActionCommand("Vol Set Data Type");
		
		cmdAddAtlas.setActionCommand("Atlas Add New");
		cmdAddAtlas.addActionListener(this);
		cmdRemoveAtlas.setActionCommand("Atlas Remove");
		cmdRemoveAtlas.addActionListener(this);
		
		LineLayout layout = new LineLayout(20, 5, 0);
		setDialogSize(500, 670);
		
		JPanel panel_general = new JPanel(false);
		panel_general.setLayout(layout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.45, 1);
		panel_general.add(lblProfileName, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.45, 1);
		panel_general.add(txtProfileName, c);
		c = new LineLayoutConstraints(1, 1, 0.6, 0.35, 1);
		panel_general.add(cmdLoadProfile, c);
		c = new LineLayoutConstraints(2, 2, 0.6, 0.35, 1);
		panel_general.add(cmdSaveProfile, c);
		
		addTab("General", panel_general);
		
		JPanel panel_io = new JPanel(false);
		panel_io.setLayout(layout);
		
		c = new LineLayoutConstraints(1, 1, 0.05, 0.55, 1);
		panel_io.add(lblSubjectDir, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.55, 1);
		panel_io.add(txtSubjectDir, c);
		c = new LineLayoutConstraints(2, 2, 0.65, 0.3, 1);
		panel_io.add(cmdBrowseDir, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.55, 1);
		panel_io.add(lblAtlasDir, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.55, 1);
		panel_io.add(txtAtlasDir, c);
		c = new LineLayoutConstraints(4, 4, 0.65, 0.3, 1);
		panel_io.add(cmdBrowseAtlas, c);
		
		c = new LineLayoutConstraints(5, 5, 0.05, 0.3, 1);
		panel_io.add(lblSubjectPrefix, c);
		c = new LineLayoutConstraints(5, 5, 0.35, 0.6, 1);
		panel_io.add(txtSubjectPrefix, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.3, 1);
		panel_io.add(lblMeshLoader, c);
		c = new LineLayoutConstraints(6, 6, 0.35, 0.6, 1);
		panel_io.add(cmbMeshLoader, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.3, 1);
		panel_io.add(lblMeshDataLoader, c);
		c = new LineLayoutConstraints(7, 7, 0.35, 0.6, 1);
		panel_io.add(cmbMeshDataLoader, c);
		c = new LineLayoutConstraints(8, 8, 0.05, 0.3, 1);
		panel_io.add(lblVolumeLoader, c);
		c = new LineLayoutConstraints(8, 8, 0.35, 0.6, 1);
		panel_io.add(cmbVolumeLoader, c);
		
		c = new LineLayoutConstraints(9, 9, 0.05, 0.3, 1);
		panel_io.add(lblMeshWriter, c);
		c = new LineLayoutConstraints(9, 9, 0.35, 0.6, 1);
		panel_io.add(cmbMeshWriter, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 0.3, 1);
		panel_io.add(lblMeshDataWriter, c);
		c = new LineLayoutConstraints(10, 10, 0.35, 0.6, 1);
		panel_io.add(cmbMeshDataWriter, c);
		c = new LineLayoutConstraints(11, 11, 0.05, 0.3, 1);
		panel_io.add(lblVolumeWriter, c);
		c = new LineLayoutConstraints(11, 11, 0.35, 0.6, 1);
		panel_io.add(cmbVolumeWriter, c);
		
		c = new LineLayoutConstraints(12, 12, 0.05, 0.3, 1);
		panel_io.add(lblLeftContains, c);
		c = new LineLayoutConstraints(12, 12, 0.35, 0.6, 1);
		panel_io.add(txtLeftContains, c);
		c = new LineLayoutConstraints(13, 13, 0.05, 0.3, 1);
		panel_io.add(lblRightContains, c);
		c = new LineLayoutConstraints(13, 13, 0.35, 0.6, 1);
		panel_io.add(txtRightContains, c);
		c = new LineLayoutConstraints(14, 14, 0.05, 0.3, 1);
		panel_io.add(lblGMContains, c);
		c = new LineLayoutConstraints(14, 14, 0.35, 0.6, 1);
		panel_io.add(txtGMContains, c);
		c = new LineLayoutConstraints(15, 15, 0.05, 0.3, 1);
		panel_io.add(lblWMContains, c);
		c = new LineLayoutConstraints(15, 15, 0.35, 0.6, 1);
		panel_io.add(txtWMContains, c);
		c = new LineLayoutConstraints(16, 16, 0.05, 0.3, 1);
		panel_io.add(lblMidContains, c);
		c = new LineLayoutConstraints(16, 16, 0.35, 0.6, 1);
		panel_io.add(txtMidContains, c);
		c = new LineLayoutConstraints(17, 17, 0.05, 0.6, 1);
		panel_io.add(chkPrependSubject, c);
	
		addTab("I/O", panel_io);
		
		JPanel panel_volume = new JPanel(false);
		panel_volume.setLayout(layout);
		
		c = new LineLayoutConstraints(1, 1, 0.02, 0.9, 1);
		panel_volume.add(lblVolDims, c);
		c = new LineLayoutConstraints(0, 1, 0.85, 0.15, 1);
		lblVolFromHeader.setHorizontalAlignment(SwingConstants.CENTER);
		panel_volume.add(lblVolFromHeader, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.2, 1);
		panel_volume.add(lblVolOrigin, c);
		c = new LineLayoutConstraints(2, 2, 0.25, 0.2, 1);
		panel_volume.add(txtVolOriginX, c);
		c = new LineLayoutConstraints(2, 2, 0.45, 0.2, 1);
		panel_volume.add(txtVolOriginY, c);
		c = new LineLayoutConstraints(2, 2, 0.65, 0.2, 1);
		panel_volume.add(txtVolOriginZ, c);
		c = new LineLayoutConstraints(2, 2, 0.9, 0.1, 1);
		panel_volume.add(chkVolOrigin, c);
		
		c = new LineLayoutConstraints(3, 3, 0.05, 0.2, 1);
		panel_volume.add(lblVolDim, c);
		c = new LineLayoutConstraints(3, 3, 0.25, 0.2, 1);
		panel_volume.add(txtVolDimX, c);
		c = new LineLayoutConstraints(3, 3, 0.45, 0.2, 1);
		panel_volume.add(txtVolDimY, c);
		c = new LineLayoutConstraints(3, 3, 0.65, 0.2, 1);
		panel_volume.add(txtVolDimZ, c);
		c = new LineLayoutConstraints(3, 3, 0.9, 0.1, 1);
		panel_volume.add(chkVolDim, c);
		
		c = new LineLayoutConstraints(4, 4, 0.05, 0.2, 1);
		panel_volume.add(lblVolGeom, c);
		c = new LineLayoutConstraints(4, 4, 0.25, 0.2, 1);
		panel_volume.add(txtVolGeomX, c);
		c = new LineLayoutConstraints(4, 4, 0.45, 0.2, 1);
		panel_volume.add(txtVolGeomY, c);
		c = new LineLayoutConstraints(4, 4, 0.65, 0.2, 1);
		panel_volume.add(txtVolGeomZ, c);
		c = new LineLayoutConstraints(4, 4, 0.9, 0.1, 1);
		panel_volume.add(chkVolGeom, c);
		
		c = new LineLayoutConstraints(5, 5, 0.05, 0.05, 1);
		panel_volume.add(chkVolFlipX, c);
		c = new LineLayoutConstraints(5, 5, 0.1, 0.25, 1);
		panel_volume.add(lblVolFlipX, c);
		c = new LineLayoutConstraints(5, 5, 0.35, 0.05, 1);
		panel_volume.add(chkVolFlipY, c);
		c = new LineLayoutConstraints(5, 5, 0.4, 0.25, 1);
		panel_volume.add(lblVolFlipY, c);
		c = new LineLayoutConstraints(5, 5, 0.65, 0.05, 1);
		panel_volume.add(chkVolFlipZ, c);
		c = new LineLayoutConstraints(5, 5, 0.7, 0.25, 1);
		panel_volume.add(lblVolFlipZ, c);
		
		c = new LineLayoutConstraints(7, 7, 0.02, 0.9, 1);
		panel_volume.add(lblVolIntensity, c);
		c = new LineLayoutConstraints(8, 8, 0.05, 0.2, 1);
		panel_volume.add(lblVolColourMap, c);
		c = new LineLayoutConstraints(8, 8, 0.25, 0.4, 1);
		panel_volume.add(cmbVolColourMap, c);
		//c = new LineLayoutConstraints(9, 9, 0.05, 0.2, 1);
		//panel_volume.add(lblVolAlpha, c);
		c = new LineLayoutConstraints(8, 8, 0.7, 0.25, 1);
		panel_volume.add(chkVolAlpha, c);
		c = new LineLayoutConstraints(9, 9, 0.05, 0.2, 1);
		panel_volume.add(chkVolDataType, c);
		c = new LineLayoutConstraints(9, 9, 0.25, 0.4, 1);
		panel_volume.add(cmbVolDataType, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 0.3, 1);
		panel_volume.add(chkVolFixedIntensity, c);
		c = new LineLayoutConstraints(11, 11, 0.05, 0.2, 1);
		panel_volume.add(lblVolBrightness, c);
		c = new LineLayoutConstraints(11, 11, 0.25, 0.4, 1);
		panel_volume.add(txtVolBrightness, c);
		c = new LineLayoutConstraints(12, 12, 0.05, 0.2, 1);
		panel_volume.add(lblVolContrast, c);
		c = new LineLayoutConstraints(12, 12, 0.25, 0.4, 1);
		panel_volume.add(txtVolContrast, c);
		
		c = new LineLayoutConstraints(13, 13, 0.05, 0.3, 1);
		panel_volume.add(chkVolRelIntensity, c);
		c = new LineLayoutConstraints(14, 14, 0.05, 0.2, 1);
		panel_volume.add(lblVolMinPct, c);
		c = new LineLayoutConstraints(14, 14, 0.25, 0.4, 1);
		panel_volume.add(txtVolMinPct, c);
		c = new LineLayoutConstraints(15, 15, 0.05, 0.2, 1);
		panel_volume.add(lblVolMaxPct, c);
		c = new LineLayoutConstraints(15, 15, 0.25, 0.4, 1);
		panel_volume.add(txtVolMaxPct, c);
		
		c = new LineLayoutConstraints(16, 16, 0.05, 0.2, 1);
		panel_volume.add(lblVolAlphaMin, c);
		c = new LineLayoutConstraints(16, 16, 0.25, 0.4, 1);
		panel_volume.add(txtVolAlphaMin, c);
		c = new LineLayoutConstraints(17, 17, 0.05, 0.2, 1);
		panel_volume.add(lblVolAlphaMax, c);
		c = new LineLayoutConstraints(17, 17, 0.25, 0.4, 1);
		panel_volume.add(txtVolAlphaMax, c);
		
		c = new LineLayoutConstraints(19, 19, 0.02, 0.9, 1);
		panel_volume.add(chkVolPreserveExisting, c);
		c = new LineLayoutConstraints(20, 20, 0.02, 0.48, 1);
		panel_volume.add(chkVolAsComposite, c);
		c = new LineLayoutConstraints(20, 20, 0.52, 0.48, 1);
		panel_volume.add(chkVolShow3D, c);
		
		addTab("Volume", panel_volume);
		
		setAtlasModel();
		JPanel panel_atlas = new JPanel(false);
		panel_atlas.setLayout(layout);
		c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		panel_atlas.add(lblAtlases, c);
		c = new LineLayoutConstraints(2, 9, 0.05, 0.9, 1);
		panel_atlas.add(lstAtlases, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 0.43, 1);
		panel_atlas.add(cmdAddAtlas, c);
		c = new LineLayoutConstraints(10, 10, 0.52, 0.43, 1);
		panel_atlas.add(cmdRemoveAtlas, c);
		
		addTab("Atlases", panel_atlas);
		
		this.setLocationRelativeTo(getParent());
	}
	
	void setAtlasModel(){
		atlas_table_model = new AtlasTableModel(new String[]{"Name","Mappings","Name Map"}, 0);
		JTable table = new JTable(atlas_table_model);
		lstAtlases = new JScrollPane(table);
	}
	
	void fillCombos(){
		//fill combo boxes with IO types
		HashMap<String, InterfaceIOType> ioTypes = InterfaceEnvironment.getIOTypes();
		Iterator<String> itr = ioTypes.keySet().iterator();
		
		cmbMeshLoader.removeAllItems();
		cmbMeshDataLoader.removeAllItems();
		cmbVolumeLoader.removeAllItems();
		cmbMeshWriter.removeAllItems();
		cmbMeshDataWriter.removeAllItems();
		cmbVolumeWriter.removeAllItems();
		cmbVolColourMap.removeAllItems();
		cmbVolDataType.removeAllItems();
		CorticalThicknessOptions options = (CorticalThicknessOptions)getOptions();
		
		try{
			while (itr.hasNext()){
				InterfaceIOType type = ioTypes.get(itr.next());
				if (SurfaceFileLoader.class.isInstance(type.getIO().newInstance())){
					cmbMeshLoader.addItem(type);
					if (options.mesh_loader != null && type.equals(options.mesh_loader))
						cmbMeshLoader.setSelectedItem(type);
					}
				if (SurfaceDataFileLoader.class.isInstance(type.getIO().newInstance())){
					cmbMeshDataLoader.addItem(type);
					if (options.mesh_data_loader != null && type.equals(options.mesh_data_loader))
						cmbMeshDataLoader.setSelectedItem(type);
					}
				if (VolumeFileLoader.class.isInstance(type.getIO().newInstance())){
					cmbVolumeLoader.addItem(type);
					if (options.volume_loader != null && type.equals(options.volume_loader))
						cmbVolumeLoader.setSelectedItem(type);
					}
				if (SurfaceFileWriter.class.isInstance(type.getIO().newInstance())){
					cmbMeshWriter.addItem(type);
					if (options.mesh_writer != null && type.equals(options.mesh_writer))
						cmbMeshWriter.setSelectedItem(type);
					}
				if (SurfaceDataFileWriter.class.isInstance(type.getIO().newInstance())){
					cmbMeshDataWriter.addItem(type);
					if (options.mesh_data_writer != null && type.equals(options.mesh_data_writer))
						cmbMeshDataWriter.setSelectedItem(type);
					}
				if (VolumeFileWriter.class.isInstance(type.getIO().newInstance())){
					cmbVolumeWriter.addItem(type);
					if (options.volume_writer != null && type.equals(options.volume_writer))
						cmbVolumeWriter.setSelectedItem(type);
					}
				}
		}catch (Exception e){
			InterfaceSession.handleException(e);
			}
		
		//colour maps
		ArrayList<ColourMap> maps = InterfaceEnvironment.getColourMaps();
		for (int i = 0; i < maps.size(); i++){
			cmbVolColourMap.addItem(maps.get(i));
			if (options.volume_options.colour_map != null && maps.get(i).equals(options.volume_options.colour_map))
				cmbVolColourMap.setSelectedItem(maps.get(i));
			}
		
		// Data types
		cmbVolDataType.addItem("Short");
		cmbVolDataType.addItem("Float");
		cmbVolDataType.addItem("Integer");
		cmbVolDataType.addItem("Double");
		cmbVolDataType.setSelectedItem("Float");
		
	}
	
	public boolean updateDialog(InterfaceOptions opts){
		this.options = opts;
		
		CorticalThicknessOptions options = (CorticalThicknessOptions)getOptions();
		
		txtSubjectDir.setText(options.subject_dir);
		txtAtlasDir.setText(options.atlas_dir);
		txtSubjectPrefix.setText(options.subject_prefix);
		txtLeftContains.setText(options.left_contains);
		txtRightContains.setText(options.right_contains);
		txtGMContains.setText(options.gm_contains);
		txtWMContains.setText(options.wm_contains);
		txtMidContains.setText(options.mid_contains);
		
		chkPrependSubject.setSelected(options.prepend_subject_name);
		
		chkVolOrigin.setSelected(!options.volume_options.set_origin);
		txtVolOriginX.setText(MguiFloat.getString(options.volume_options.origin_x, "0.000###"));
		txtVolOriginY.setText(MguiFloat.getString(options.volume_options.origin_y, "0.000###"));
		txtVolOriginZ.setText(MguiFloat.getString(options.volume_options.origin_z, "0.000###"));
		
		chkVolGeom.setSelected(!options.volume_options.set_geom);
		txtVolGeomX.setText(MguiFloat.getString(options.volume_options.geom_x, "0.000###"));
		txtVolGeomY.setText(MguiFloat.getString(options.volume_options.geom_y, "0.000###"));
		txtVolGeomZ.setText(MguiFloat.getString(options.volume_options.geom_z, "0.000###"));
		
		chkVolDim.setSelected(!options.volume_options.set_dims);
		txtVolDimX.setText("" + options.volume_options.dim_x);
		txtVolDimY.setText("" + options.volume_options.dim_y);
		txtVolDimZ.setText("" + options.volume_options.dim_z);
		
		chkVolFlipX.setSelected(options.volume_options.flip_x);
		chkVolFlipY.setSelected(options.volume_options.flip_y);
		chkVolFlipZ.setSelected(options.volume_options.flip_z);
		
		txtVolBrightness.setText(MguiDouble.getString(options.volume_options.window_mid, "0.000###"));
		txtVolContrast.setText(MguiDouble.getString(options.volume_options.window_width, "0.000###"));
		txtVolAlphaMin.setText(MguiDouble.getString(options.volume_options.alpha_min, "0.000###"));
		txtVolAlphaMax.setText(MguiDouble.getString(options.volume_options.alpha_max, "0.000###"));
		chkVolAlpha.setSelected(options.volume_options.has_alpha);
		
		chkVolPreserveExisting.setSelected(options.preserveVolExisting);
		chkVolAsComposite.setSelected(options.volumes_as_composite);
		chkVolShow3D.setSelected(options.show_volumes_3d);
		
		chkVolFixedIntensity.setSelected(!options.set_intensity_from_histogram);
		chkVolRelIntensity.setSelected(options.set_intensity_from_histogram);
		this.txtVolMinPct.setText(MguiDouble.getString(options.min_pct, "0.000###"));
		this.txtVolMaxPct.setText(MguiDouble.getString(options.max_pct, "0.000###"));
		
		chkVolDataType.setSelected(options.set_data_type);
		
		fillCombos();
		cmbVolDataType.setSelectedItem(getDataTypeStr(options.data_type));
		updateControls();
		
		return true;
	}
	
	private String getDataTypeStr(int type){
		switch (type){
			case DataBuffer.TYPE_SHORT:
				return "Short";
			case DataBuffer.TYPE_INT:
				return "Integer";
			case DataBuffer.TYPE_FLOAT:
				return "Float";
			case DataBuffer.TYPE_DOUBLE:
				return "Double";
			default:
				return "Float";
			}
	}
	
	private int getDataType(String type){
		if (type.equals("Short"))
			return DataBuffer.TYPE_SHORT;
		if (type.equals("Integer"))
			return DataBuffer.TYPE_INT;
		if (type.equals("Float"))
			return DataBuffer.TYPE_FLOAT;
		if (type.equals("Double"))
			return DataBuffer.TYPE_DOUBLE;
		return DataBuffer.TYPE_FLOAT;
	}
	
	void updateControls(){
		txtVolOriginX.setEnabled(!chkVolOrigin.isSelected());
		txtVolOriginY.setEnabled(!chkVolOrigin.isSelected());
		txtVolOriginZ.setEnabled(!chkVolOrigin.isSelected());
		
		txtVolDimX.setEnabled(!chkVolDim.isSelected());
		txtVolDimY.setEnabled(!chkVolDim.isSelected());
		txtVolDimZ.setEnabled(!chkVolDim.isSelected());
		
		txtVolGeomX.setEnabled(!chkVolGeom.isSelected());
		txtVolGeomY.setEnabled(!chkVolGeom.isSelected());
		txtVolGeomZ.setEnabled(!chkVolGeom.isSelected());
		
		boolean is_fixed = this.chkVolFixedIntensity.isSelected();
		this.txtVolBrightness.setEnabled(is_fixed);
		this.txtVolContrast.setEnabled(is_fixed);
		this.txtVolMinPct.setEnabled(!is_fixed);
		this.txtVolMaxPct.setEnabled(!is_fixed);
		
		cmbVolDataType.setEnabled(chkVolDataType.isSelected());
		
	}
	
	protected VolumeInputOptions getVolumeOptions(){
		
		InterfaceIOType io_type = (InterfaceIOType)cmbVolumeLoader.getSelectedItem();
		if (io_type == null) return null;
		
		return (VolumeInputOptions)io_type.getOptionsInstance();
		
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Vol Changed")){
			updateControls();
			return;
		}
		
		if (e.getActionCommand().startsWith("Vol Intensity")){
			boolean is_fixed = false;
			if (e.getActionCommand().endsWith("Fixed")){
				is_fixed = chkVolFixedIntensity.isSelected();
				chkVolRelIntensity.setSelected(!is_fixed);
			}else{
				is_fixed = !chkVolRelIntensity.isSelected();
				chkVolFixedIntensity.setSelected(is_fixed);
				}
			
			updateControls();
			
		}
		
		if (e.getActionCommand().equals("Vol Set Data Type")){
			updateControls();
			return;
		}
		
		if (e.getActionCommand().equals("Browse Dir")){
			boolean setDir = false;
			if (txtSubjectDir.getText().length() > 0){
				File dir = new File(txtSubjectDir.getText());
				setDir = (dir.exists() && dir.isDirectory());
				}
			JFileChooser fc = null;
			if (setDir) 
				fc = new JFileChooser(txtSubjectDir.getText());
			else
				fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setDialogTitle("Select subjects directory");
			fc.showDialog(this, "Select");
			if (fc.getSelectedFile() != null)
				txtSubjectDir.setText(fc.getSelectedFile().getAbsolutePath());
			return;
			}
		
		if (e.getActionCommand().equals("Browse Atlas")){
			boolean setDir = false;
			if (txtAtlasDir.getText().length() > 0){
				File dir = new File(txtAtlasDir.getText());
				setDir = (dir.exists() && dir.isDirectory());
				}
			JFileChooser fc = null;
			if (setDir) 
				fc = new JFileChooser(txtAtlasDir.getText());
			else
				fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setDialogTitle("Select atlas directory");
			fc.showDialog(this, "Select");
			if (fc.getSelectedFile() != null)
				txtAtlasDir.setText(fc.getSelectedFile().getAbsolutePath());
			return;
			}
		
		//handle cancel event
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//set options
			CorticalThicknessOptions options = (CorticalThicknessOptions)getOptions();
			options.subject_dir = txtSubjectDir.getText();
			options.atlas_dir = txtAtlasDir.getText();
			options.subject_prefix = txtSubjectPrefix.getText();
			options.mesh_loader = null;
			if (cmbMeshLoader.getSelectedItem() != null) options.mesh_loader = (InterfaceIOType)cmbMeshLoader.getSelectedItem();
			options.mesh_data_loader = null;
			if (cmbMeshDataLoader.getSelectedItem() != null) options.mesh_data_loader = (InterfaceIOType)cmbMeshDataLoader.getSelectedItem();
			options.volume_loader = null;
			if (cmbVolumeLoader.getSelectedItem() != null) options.volume_loader = (InterfaceIOType)cmbVolumeLoader.getSelectedItem();
			options.mesh_writer = null;
			if (cmbMeshWriter.getSelectedItem() != null) options.mesh_writer = (InterfaceIOType)cmbMeshWriter.getSelectedItem();
			options.mesh_data_writer = null;
			if (cmbMeshDataWriter.getSelectedItem() != null) options.mesh_data_writer = (InterfaceIOType)cmbMeshDataWriter.getSelectedItem();
			options.volume_writer = null;
			if (cmbVolumeWriter.getSelectedItem() != null) options.volume_writer = (InterfaceIOType)cmbVolumeWriter.getSelectedItem();
			
			options.prepend_subject_name = chkPrependSubject.isSelected();
			options.volumes_as_composite = chkVolAsComposite.isSelected();
			options.show_volumes_3d = chkVolShow3D.isSelected();
			
			options.left_contains = txtLeftContains.getText();
			options.right_contains = txtRightContains.getText();
			options.gm_contains = txtGMContains.getText();
			options.wm_contains = txtWMContains.getText();
			options.mid_contains = txtMidContains.getText();
			
			options.volume_options = getVolumeOptions();
			options.volume_options.has_alpha = chkVolAlpha.isSelected();
			options.volume_options.colour_map = (ColourMap)cmbVolColourMap.getSelectedItem();
			options.volume_options.set_dims = !chkVolDim.isSelected();
			options.volume_options.set_geom = !chkVolGeom.isSelected();
			options.volume_options.set_origin = !chkVolOrigin.isSelected();
			
			options.atlases = this.atlas_table_model.getAtlases();
			
			options.preserveVolExisting = chkVolPreserveExisting.isSelected();
			
			options.set_data_type = chkVolDataType.isSelected();
			options.data_type = getDataType((String)cmbVolDataType.getSelectedItem());
			
			try{
				//options.volume_options.set_origin = chkVolOrigin.isSelected();
				options.volume_options.origin_x = Float.valueOf(txtVolOriginX.getText()).floatValue();
				options.volume_options.origin_y = Float.valueOf(txtVolOriginY.getText()).floatValue();
				options.volume_options.origin_z = Float.valueOf(txtVolOriginZ.getText()).floatValue();
				
				//options.volume_options.set_dims = chkVolDim.isSelected();
				options.volume_options.dim_x = Integer.valueOf(txtVolDimX.getText()).intValue();
				options.volume_options.dim_y = Integer.valueOf(txtVolDimY.getText()).intValue();
				options.volume_options.dim_z = Integer.valueOf(txtVolDimZ.getText()).intValue();
				
				//options.volume_options.set_geom = chkVolGeom.isSelected();
				options.volume_options.geom_x = Float.valueOf(txtVolGeomX.getText()).floatValue();
				options.volume_options.geom_y = Float.valueOf(txtVolGeomY.getText()).floatValue();
				options.volume_options.geom_z = Float.valueOf(txtVolGeomZ.getText()).floatValue();
				
				options.volume_options.window_mid = Double.valueOf(txtVolBrightness.getText()).doubleValue();
				options.volume_options.window_width = Double.valueOf(txtVolContrast.getText()).doubleValue();
				options.volume_options.alpha_min = Double.valueOf(txtVolAlphaMin.getText()).doubleValue();
				options.volume_options.alpha_max = Double.valueOf(txtVolAlphaMax.getText()).doubleValue();
				
				options.set_intensity_from_histogram = !chkVolFixedIntensity.isSelected();
				options.min_pct = Double.valueOf(txtVolMinPct.getText()).doubleValue();
				options.max_pct = Double.valueOf(txtVolMaxPct.getText()).doubleValue();
				
			}catch (NumberFormatException ex){
				JOptionPane.showMessageDialog(this.parentPanel, "Number format problem (note dims must be integers)");
				}
			
			options.volume_options.flip_x = chkVolFlipX.isSelected();
			options.volume_options.flip_y = chkVolFlipY.isSelected();
			options.volume_options.flip_z = chkVolFlipZ.isSelected();
			
			options.panel.updateDisplay();
			
			this.setVisible(false);
			}
		
		if (e.getActionCommand().startsWith("Atlas")){
			
			if (e.getActionCommand().endsWith("Add New")){
				InterfaceNeuroAtlas atlas = NewAtlasDialog.showDialog((JFrame)getParent());
				if (atlas == null) return;
				String n_map = "none";
				NameMap map = atlas.getNameMap();
				if (map != null) n_map = "{" + map.getSize() + "}";
				atlas_table_model.addRow(new Object[]{atlas, "{" + atlas.mappings.size() + "}", n_map});
				lstAtlases.updateUI();
				return;
				}
			
			
			}
		
		super.actionPerformed(e);

	}
	
	class AtlasTableModel extends DefaultTableModel{
		
		public AtlasTableModel(Object[] obj, int rc){
			super(obj, rc);
		}
		
		public boolean isCellEditable(int row, int col){
			if (col != 0) return false;
			return true;
		}
		
		public void setValueAt(Object aValue, int row, int column) {
	        Vector rowVector = (Vector)dataVector.elementAt(row);
	        if (column == 0){
	        	InterfaceNeuroAtlas atlas = (InterfaceNeuroAtlas)getValueAt(row, column);
	        	atlas.setName((String)aValue);
	        }else{
		        rowVector.setElementAt(aValue, column);
		        }
	        fireTableCellUpdated(row, column);
	    }
		
		public ArrayList<InterfaceNeuroAtlas> getAtlases(){
			ArrayList<InterfaceNeuroAtlas> atlases = new ArrayList<InterfaceNeuroAtlas>();
			int n = getRowCount();
			for (int i = 0; i < n; i++)
				atlases.add((InterfaceNeuroAtlas)getValueAt(i, 0));
			return atlases;
		}
		
	}
	
	static class NewAtlasDialog extends InterfaceDialogBox {
		
		JLabel lblName = new JLabel("Atlas name:");
		JTextField txtName = new JTextField("no-name");
		JLabel lblMappingsFile = new JLabel("Mappings file:");
		JTextField txtMappingsFile = new JTextField();
		JButton cmdMappingsFile = new JButton("Browse..");
		JCheckBox chkNameMapFile = new JCheckBox(" Name map file:");
		JTextField txtNameMapFile = new JTextField();
		JButton cmdNameMapFile = new JButton("Browse..");
		
		File mappings_file, name_map_file;
		
		public InterfaceNeuroAtlas new_atlas;
		
		public NewAtlasDialog(JFrame frame){
			super(frame);
			_init();
		}
		
		void _init(){
			setButtonType(BT_OK_CANCEL);
			super.init();
			
			this.setTitle("Add new atlas");
			LineLayout layout = new LineLayout(20, 5, 0);
			this.setMainLayout(layout);
			setDialogSize(500, 230);
			
			cmdMappingsFile.setActionCommand("Browse Mappings File");
			cmdMappingsFile.addActionListener(this);
			cmdNameMapFile.setActionCommand("Browse Name Map File");
			cmdNameMapFile.addActionListener(this);
			
			LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
			mainPanel.add(lblName, c);
			c = new LineLayoutConstraints(1, 1, 0.35, 0.6, 1);
			mainPanel.add(txtName, c);
			c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
			mainPanel.add(lblMappingsFile, c);
			c = new LineLayoutConstraints(2, 2, 0.35, 0.29, 1);
			mainPanel.add(txtMappingsFile, c);
			c = new LineLayoutConstraints(2, 2, 0.66, 0.29, 1);
			mainPanel.add(cmdMappingsFile, c);
			c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
			mainPanel.add(chkNameMapFile, c);
			c = new LineLayoutConstraints(3, 3, 0.35, 0.29, 1);
			mainPanel.add(txtNameMapFile, c);
			c = new LineLayoutConstraints(3, 3, 0.66, 0.29, 1);
			mainPanel.add(cmdNameMapFile, c);
			
			this.setLocationRelativeTo(getParent());
		}
		
		public void actionPerformed(ActionEvent e){
			
			if (e.getActionCommand().equals(DLG_CMD_OK)){
				//load mappings
				if (mappings_file == null){
					JOptionPane.showMessageDialog(getParent(), 
												  "No mappings file selected!", 
												  "New Atlas", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				//load mappings
				MincSurfaceDataLoader loader = new MincSurfaceDataLoader(mappings_file, 
																		 MincSurfaceDataLoader.FORMAT_INT);
				ArrayList<MguiNumber> mappings = loader.loadValues();
				if (mappings == null){
					JOptionPane.showMessageDialog(getParent(), 
												  "Error loading from mappings file..", 
												  "New Atlas", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				//load name map if necessary
				NameMap name_map = null;
				if (chkNameMapFile.isSelected()){
					NameMapLoader nm_loader = new NameMapLoader(name_map_file);
					name_map = nm_loader.loadNameMap();
					if (name_map == null){
						JOptionPane.showMessageDialog(getParent(), 
													  "Error loading from name map file..", 
													  "New Atlas", 
													  JOptionPane.ERROR_MESSAGE);
						return;
						}
					name_map.setName(txtName.getText());
					}
					
				//set atlas
				if (name_map != null)
					new_atlas = new InterfaceNeuroAtlas(txtName.getText(), mappings, name_map);
				else
					new_atlas = new InterfaceNeuroAtlas(txtName.getText(), mappings);
				
				this.setVisible(false);
				return;
				}
			
			if (e.getActionCommand().equals("Browse Mappings File")){
				ArrayList<String> filter = new ArrayList<String>();
				filter.add("txt");
				JFileChooser chooser = new JFileChooser("Choose mappings file");
				chooser.setFileFilter(IoFunctions.getFileChooserFilter(filter, "Surface Data Files"));
				chooser.showOpenDialog(getParent());
				if (chooser.getSelectedFile() != null){
					mappings_file = chooser.getSelectedFile();
					txtMappingsFile.setText(mappings_file.getName());
				}else{
					txtMappingsFile.setText("");
					}
				return;
				}
			
			if (e.getActionCommand().equals("Browse Name Map File")){
				ArrayList<String> filter = new ArrayList<String>();
				filter.add("nmap");
				JFileChooser chooser = new JFileChooser("Choose name map file");
				chooser.setFileFilter(IoFunctions.getFileChooserFilter(filter, "Name Map Files"));
				chooser.showOpenDialog(getParent());
				if (chooser.getSelectedFile() != null){
					name_map_file = chooser.getSelectedFile();
					txtNameMapFile.setText(name_map_file.getName());
				}else{
					txtNameMapFile.setText("");
					}
				return;
				}
			
			super.actionPerformed(e);
		}
		
		public static InterfaceNeuroAtlas showDialog(JFrame frame){
			NewAtlasDialog dialog = new NewAtlasDialog(frame);
			dialog.setVisible(true);
			return dialog.new_atlas;
		}
		
	}
	
}