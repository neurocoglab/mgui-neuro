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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataTable;
import mgui.geometry.volume.VolumeFunctions;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.datasources.DataTableModel;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.tables.InterfaceDataTable;
import mgui.io.domestic.shapes.SurfaceDataFileLoader;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.domestic.shapes.VolumeInputOptions;
import mgui.numbers.MguiNumber;
import mgui.stats.Histogram;
import mgui.util.StringFunctions;


/*************************************************
 * Panel providing an interface to cortical thickness databases and subject databases.
 * Originally for the MINC system, but intended extensible for any surface-based data
 * with a specific directory structure and file formats for which loaders and writers
 * are available. 
 * 
 * @author Andrew Reid
 *
 */
public class InterfaceCorticalThicknessPanel extends InterfacePanel implements ActionListener {

	CategoryTitle lblProject = new CategoryTitle("PROJECT");
	JLabel lblProfile = new JLabel("Project:");
	JComboBox cmbProfile = new JComboBox();
	JButton cmdProfileEdit = new JButton("Edit");
	JButton cmdProfileNew = new JButton("New");
	JButton cmdProfileLoad = new JButton("Load");
	JButton cmdProfileApply = new JButton("Apply");
	
	CategoryTitle lblSubjects = new CategoryTitle("SUBJECTS");
	//list of subjects
	SubjectList lstSubjects;
	JLabel lblSubjectList = new JLabel("Subjects:");
	JButton cmdSubjectPreview = new JButton("Preview selected");
	
	CategoryTitle lblShapes = new CategoryTitle("SHAPES");
	//list of surfaces
	JLabel lblSubjectSurfaces = new JLabel("Surfaces");
	SubjectList lstSurfaces = new SubjectList("File name", 0);
	JLabel lblSubjectVolumes = new JLabel("Volume");
	//list of volumes
	SubjectList lstVolumes = new SubjectList("File name", 0);
	JLabel lblSubjectData = new JLabel("Vertex data");
	//list of text data
	SubjectList lstSurfData = new SubjectList("File name", 0);
	JLabel lblAtlas = new JLabel("Vertex data");
	//list of text data
	SubjectList lstAtlas = new SubjectList("File name", 0);
	JCheckBox chkSubjectLeft = new JCheckBox("Left");
	JCheckBox chkSubjectRight = new JCheckBox("Right");
	JLabel lblSubjectShapeSet = new JLabel("Shape Set:");
	JComboBox cmbSubjectShapeSet = new JComboBox();
	JButton cmdSubjectLoad = new JButton("Load selected subject");
	JButton cmdSubjectNext = new JButton("Next>");
	JButton cmdSubjectPrev = new JButton("<Prev");
	
	//list of available atlases
	CategoryTitle lblAtlases = new CategoryTitle("ATLASES");
	JLabel lblAtlasesLeft = new JLabel("Left:");
	JLabel lblAtlasesRight = new JLabel("Right:");
	JScrollPane lstAtlasesLeft = new JScrollPane(), lstAtlasesRight = new JScrollPane();
	AtlasTableModel atlas_table_left, atlas_table_right;
	
	CategoryTitle lblAttributes = new CategoryTitle("ATTRIBUTES");
	InterfaceAttributePanel tblAttributes = new InterfaceAttributePanel();
	
	CategoryTitle lblDataSource = new CategoryTitle("DATA SOURCES");
	JLabel lblDataTables = new JLabel("Tables");
	//list of data tables
	DataTableList lstDataTables;
	JLabel lblDataWindow = new JLabel("Datasource window:");
	JComboBox cmbDataWindow = new JComboBox();
	JCheckBox chkDataFilter = new JCheckBox("Show only selected subjects");
	JButton cmdDataAddTable = new JButton("Add");
	JButton cmdDataRemTable = new JButton("Remove");
	JButton cmdDataShowTable = new JButton("Show");
	
	CategoryTitle lblPlots = new CategoryTitle("PLOTS");
	
	CategoryTitle lblOperations = new CategoryTitle("OPERATIONS");
	
	CategoryTitle lblQueries = new CategoryTitle("QUERIES");
	
	CategoryTitle lblStatistics = new CategoryTitle("STATISTICS");
	
	//other stuff
	CorticalThicknessOptions directory_options = new CorticalThicknessOptions(this);
	CorticalThicknessDialogBox directory_dialog; // = new InterfaceCorticalThicknessDialogBox()
	CorticalThicknessDataTableOptions datatable_options = new CorticalThicknessDataTableOptions(this);
	CorticalThicknessDataTableDialogBox datatable_dialog;
	
	ArrayList<SubjectDataTable> data_tables = new ArrayList<SubjectDataTable>();
	
	
	ShapeSet3DInt shape_set, parent_set;
	VolumeInputOptions current_volume_input_options = new VolumeInputOptions();
	
	public InterfaceCorticalThicknessPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	protected void init(){
		_init();
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		directory_dialog = new CorticalThicknessDialogBox(InterfaceSession.getSessionFrame(), directory_options);
		datatable_dialog = new CorticalThicknessDataTableDialogBox(InterfaceSession.getSessionFrame(), datatable_options);
		
		cmdProfileEdit.addActionListener(this);
		cmdProfileEdit.setActionCommand("Profile Edit");
		cmdProfileApply.addActionListener(this);
		cmdProfileApply.setActionCommand("Profile Apply");
		
		cmdSubjectPreview.addActionListener(this);
		cmdSubjectPreview.setActionCommand("Subject Preview");
		cmdSubjectLoad.addActionListener(this);
		cmdSubjectLoad.setActionCommand("Subject Load");
		cmdSubjectNext.addActionListener(this);
		cmdSubjectNext.setActionCommand("Subject Load Next");
		cmdSubjectPrev.addActionListener(this);
		cmdSubjectPrev.setActionCommand("Subject Load Prev");
		
		chkSubjectLeft.setSelected(true);
		chkSubjectRight.setSelected(true);
		
		cmdDataAddTable.addActionListener(this);
		cmdDataAddTable.setActionCommand("DataTable Add");
		cmdDataRemTable.addActionListener(this);
		cmdDataRemTable.setActionCommand("DataTable Remove");
		cmdDataShowTable.addActionListener(this);
		cmdDataShowTable.setActionCommand("DataTable Show");
		
		cmbDataWindow.addActionListener(this);
		cmbDataWindow.setActionCommand("DataTable Combo");
		
		updateSubjectList();
		updateDataTableList();
		updateShapeSetList();
		updateDataWindowList();
		updateAtlases();
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		lblProject.isExpanded = true;
		add(lblProject, c);
		lblProject.setParentObj(this);
		
		c = new CategoryLayoutConstraints("PROJECT", 1, 1, 0.05, 0.2, 1);
		add(lblProfile, c);
		c = new CategoryLayoutConstraints("PROJECT", 1, 1, 0.25, 0.7, 1);
		add(cmbProfile, c);
		c = new CategoryLayoutConstraints("PROJECT", 2, 2, 0.05, 0.43, 1);
		add(cmdProfileEdit, c);
		c = new CategoryLayoutConstraints("PROJECT", 2, 2, 0.52, 0.43, 1);
		add(cmdProfileNew, c);
		c = new CategoryLayoutConstraints("PROJECT", 3, 3, 0.05, 0.9, 1);
		add(cmdProfileApply, c);
		
		c = new CategoryLayoutConstraints();
		lblSubjects.isExpanded = false;
		add(lblSubjects, c);
		lblSubjects.setParentObj(this);
		
		c = new CategoryLayoutConstraints("SUBJECTS", 1, 1, 0.05, 0.9, 1);
		add(lblSubjectList, c);
		c = new CategoryLayoutConstraints("SUBJECTS", 2, 9, 0.05, 0.9, 1);
		//list here
		add(lstSubjects, c);
		c = new CategoryLayoutConstraints("SUBJECTS", 10, 10, 0.05, 0.9, 1);
		add(cmdSubjectPreview, c);
		
		c = new CategoryLayoutConstraints();
		lblShapes.isExpanded = false;
		add(lblShapes, c);
		lblShapes.setParentObj(this);
		
		c = new CategoryLayoutConstraints("SHAPES", 1, 1, 0.05, 0.9, 1);
		add(lblSubjectSurfaces, c);
		c = new CategoryLayoutConstraints("SHAPES", 2, 5, 0.05, 0.9, 1);
		add(lstSurfaces, c);
		c = new CategoryLayoutConstraints("SHAPES", 6, 6, 0.05, 0.9, 1);
		add(lblSubjectVolumes, c);
		c = new CategoryLayoutConstraints("SHAPES", 7, 10, 0.05, 0.9, 1);
		add(lstVolumes, c);
		c = new CategoryLayoutConstraints("SHAPES", 11, 11, 0.05, 0.9, 1);
		add(lblSubjectData, c);
		c = new CategoryLayoutConstraints("SHAPES", 12, 15, 0.05, 0.9, 1);
		add(lstSurfData, c);
				
		c = new CategoryLayoutConstraints("SHAPES", 16, 16, 0.05, 0.43, 1);
		add(chkSubjectLeft, c);
		c = new CategoryLayoutConstraints("SHAPES", 16, 16, 0.52, 0.43, 1);
		add(chkSubjectRight, c);
		c = new CategoryLayoutConstraints("SHAPES", 17, 17, 0.05, 0.3, 1);
		add(lblSubjectShapeSet, c);
		c = new CategoryLayoutConstraints("SHAPES", 18, 18, 0.05, 0.9, 1);
		add(cmbSubjectShapeSet, c);
		c = new CategoryLayoutConstraints("SHAPES", 19, 19, 0.05, 0.9, 1);
		add(cmdSubjectLoad, c);
		c = new CategoryLayoutConstraints("SHAPES", 20, 20, 0.05, 0.44, 1);
		add(cmdSubjectPrev, c);
		c = new CategoryLayoutConstraints("SHAPES", 20, 20, 0.51, 0.44, 1);
		add(cmdSubjectNext, c);
		
		c = new CategoryLayoutConstraints();
		lblAtlases.isExpanded = false;
		add(lblAtlases, c);
		lblAtlases.setParentObj(this);
		
		c = new CategoryLayoutConstraints("ATLASES", 1, 1, 0.05, 0.9, 1);
		add(lblAtlasesLeft, c);
		c = new CategoryLayoutConstraints("ATLASES", 2, 5, 0.05, 0.9, 1);
		add(lstAtlasesLeft, c);
		c = new CategoryLayoutConstraints("ATLASES", 6, 6, 0.05, 0.9, 1);
		add(lblAtlasesRight, c);
		c = new CategoryLayoutConstraints("ATLASES", 7, 11, 0.05, 0.9, 1);
		add(lstAtlasesRight, c);
	
		c = new CategoryLayoutConstraints();
		lblAttributes.isExpanded = false;
		add(lblAttributes, c);
		lblAttributes.setParentObj(this);
		
		c = new CategoryLayoutConstraints("ATTRIBUTES", 1, 15, 0.05, 0.9, 1);
		add(tblAttributes, c);
		
		c = new CategoryLayoutConstraints();
		lblDataSource.isExpanded = false;
		add(lblDataSource, c);
		lblDataSource.setParentObj(this);
		
		c = new CategoryLayoutConstraints("DATA SOURCES", 1, 1, 0.05, 0.9, 1);
		add(lblDataTables, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 2, 7, 0.05, 0.9, 1);
		add(lstDataTables, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 8, 8, 0.05, 0.43, 1);
		add(cmdDataAddTable, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 8, 8, 0.52, 0.43, 1);
		add(cmdDataRemTable, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 9, 9, 0.05, 0.9, 1);
		add(lblDataWindow, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 10, 10, 0.05, 0.9, 1);
		add(cmbDataWindow, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 11, 11, 0.05, 0.9, 1);
		add(chkDataFilter, c);
		c = new CategoryLayoutConstraints("DATA SOURCES", 12, 12, 0.05, 0.9, 1);
		add(cmdDataShowTable, c);
		
		c = new CategoryLayoutConstraints();
		lblOperations.isExpanded = false;
		add(lblOperations, c);
		lblOperations.setParentObj(this);
		
		c = new CategoryLayoutConstraints();
		lblQueries.isExpanded = false;
		add(lblQueries, c);
		lblQueries.setParentObj(this);
		
		c = new CategoryLayoutConstraints();
		lblStatistics.isExpanded = false;
		add(lblStatistics, c);
		lblStatistics.setParentObj(this);
		
		
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceCorticalThicknessPanel.class.getResource("/mgui/resources/icons/neuro/cortical_surf_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			System.out.println("Cannot find resource: /mgui/resources/icons/neuro/cortical_surf_20.png");
		return null;
	}
	
	public void updateDisplay(){
		//stuff
		
		updateUI();
	}
	
	public void cleanUpPanel(){
		//System.out.println("Cleaning up panel..");
		//release shapes if they have been destroyed
		cmbSubjectShapeSet.removeAllItems();
		if (shape_set != null && shape_set.isDestroyed()){
			shape_set = null;
			//surface_set = null;
			//System.out.println("Shape set destroyed..");
			}
//		if (surface_set != null && surface_set.isDestroyed()){
//			surface_set = null;
//			}
		if (parent_set != null && parent_set.isDestroyed())
			parent_set = null;
		
	}
	
	void updateAtlases(){
		
		atlas_table_left = new AtlasTableModel(directory_options.atlases);
		JTable table = new JTable(atlas_table_left);
		TableColumn column = table.getColumnModel().getColumn(0);
		column.setMinWidth(40);
		column.setMaxWidth(40);
		lstAtlasesLeft.setViewportView(table);
		//lstAtlasesLeft = new JScrollPane(table);
		
		atlas_table_right = new AtlasTableModel(directory_options.atlases);
		table = new JTable(atlas_table_right);
		column = table.getColumnModel().getColumn(0);
		column.setMinWidth(40);
		column.setMaxWidth(40);
		lstAtlasesRight.setViewportView(table);
		//lstAtlasesRight = new JScrollPane(table);
	}
	
	void updateShapeSetList(){
		InterfaceDisplayPanel displayPanel = InterfaceSession.getDisplayPanel();
		//ShapeSet3DInt sets = displayPanel.getCurrentShapeSet().getShapeType(new ShapeSet3DInt());
		
		List<Shape3DInt> sets = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new ShapeSet3DInt());
		
		boolean set_found = false;
		cmbSubjectShapeSet.removeAllItems();
		cmbSubjectShapeSet.addItem(displayPanel.getCurrentShapeSet());
		if (parent_set == displayPanel.getCurrentShapeSet())
			set_found = true;
		for (int i = 0; i < sets.size(); i++){
			cmbSubjectShapeSet.addItem(sets.get(i));
			if (parent_set != null && shape_set == sets.get(i))
				set_found = true;
			}
		if (set_found)
			cmbSubjectShapeSet.setSelectedItem(parent_set);
		else
			parent_set = null;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().startsWith("Profile")){
			if (e.getActionCommand().endsWith("Edit")){
				directory_dialog.updateDialog(directory_options);
				directory_dialog.setVisible(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				updateSubjectList();
				updateAtlases();
				updateDisplay();
				return;
				}
			}
		
		if (e.getActionCommand().startsWith("Subject")){
			if (e.getActionCommand().endsWith("Preview")){
				//compile list of surfaces from first subject
				updateSubjectData();
				updateDisplay();
				return;
				}
			
			if (e.getActionCommand().endsWith("Prev")){
				int r = lstSubjects.table.getSelectedRow();
				lstSubjects.table.getSelectionModel().setSelectionInterval(r - 1, r - 1);
				lstSubjects.updateUI();
				}
			
			if (e.getActionCommand().endsWith("Next")){
				int r = lstSubjects.table.getSelectedRow();
				lstSubjects.table.getSelectionModel().setSelectionInterval(r + 1, r + 1);
				lstSubjects.updateUI();
				}
			
			if (e.getActionCommand().contains("Load")){
				
				if (!setShapeSet()){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No shape set specified!");
					return;
					}
				
				//get subjects
				if (directory_options.mesh_loader == null && lstSurfaces.getSelected().size() > 0){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No mesh loader specified!");
					return;
					}
				if (directory_options.mesh_data_loader == null && lstSurfData.getSelected().size() > 0){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No mesh data loader specified!");
					return;
					}
				if (directory_options.volume_loader == null && lstVolumes.getSelected().size() > 0){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No volume loader specified!");
					return;
					}
				//if single, load single surface and vertex data and volumes
				ArrayList<String> subjects = lstSubjects.getSelected();
				if (subjects.size() == 0){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No subjects selected!");
					return;
					}
				if (subjects.size() == 1){
					
					InterfaceProgressBar progress_bar = new InterfaceProgressBar("Loading volume(s) for subject '" + subjects.get(0) + "'");
					
					progress_bar.setMinimum(0);
					ArrayList<String> surfaces = lstSurfaces.getSelected();
					ArrayList<String> data = lstSurfData.getSelected();
					ArrayList<String> volumes = lstVolumes.getSelected();
					
					progress_bar.setMaximum(surfaces.size() + volumes.size() + data.size());
					progress_bar.register();
					
					TreeSet<String> shapes_to_keep = new TreeSet<String>();
					
					//load surfaces
					loadSurfaces(subjects.get(0), surfaces, progress_bar, shapes_to_keep);
					
					//load surface data
					loadSurfaceData(subjects.get(0), data, progress_bar, shapes_to_keep);
					
					//load volumes 
					loadVolumes(subjects.get(0), volumes, progress_bar, shapes_to_keep);
				
					//remove unwanted shapes
					removeShapes(shapes_to_keep);
					
					InterfaceSession.getDisplayPanel().updateDisplay();
					
					progress_bar.deregister();
					return;
				}else{
					//TODO: if multi, get average surface and vertex data and volumes...
					
					
					
					}
				}
			
			}
		
		if (e.getActionCommand().startsWith("DataTable")){
			
			if (e.getActionCommand().endsWith("Combo")){
				if (cmbDataWindow.getSelectedItem() != null)
					cmdDataShowTable.setEnabled(true);
				else
					cmdDataShowTable.setEnabled(false);
				return;
				}
			
			if (e.getActionCommand().endsWith("Add")){
				datatable_dialog.updateDialog(datatable_options);
				datatable_dialog.setVisible(true);
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				if (lstDataTables.selected_row > -1){
					lstDataTables.model.removeRow(lstDataTables.selected_row);
					lstDataTables.selected_row = -1;
					}
				return;
				}
			
			if (e.getActionCommand().endsWith("Show")){
				if (cmbDataWindow.getSelectedItem() == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No data table window selected!");
					return;
					}
				
				String[] selected = lstDataTables.getSelected();
				if (selected == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No data table selected!");
					return;
					}
				
				try{
					InterfaceDataTable window = (InterfaceDataTable)cmbDataWindow.getSelectedItem();
					DataSource ds = InterfaceSession.getWorkspace().getDataSource(selected[0]);
					DataTable dt = ds.getTableSet().getTable(selected[1]);
					if (dt != null){
						
						window.setSource(dt);
					
						//filter?
						if (chkDataFilter.isSelected()){
							ArrayList<String> subjects = new ArrayList<String>(lstSubjects.getSelected());
							if (subjects.size() == 0){
								JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No subjects selected!");
								return;
								}
							try{
								((DataTableModel)window.getModel()).applyFilter(subjects, selected[2]);
							}catch (DataSourceException ex){
								ex.printStackTrace();
								return;
								}
							}
						}
				}catch (DataSourceException ex){
					ex.printStackTrace();
					DataSource ds = InterfaceSession.getWorkspace().getDataSource(selected[0]);
					JOptionPane.showMessageDialog(getParent(), 
							  "Problem reading from data source '" + ds.getName() + "'.", 
							  "Data Source Error", 
							  JOptionPane.ERROR_MESSAGE);
					return;
					}
				}
			
			
			}
		
	}
	
	public void addDataTable(){
		if (lstDataTables.model.containsTable(datatable_options.data_source, datatable_options.data_table)){
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Table already in list!");
			return;
			}
		
		lstDataTables.model.addRow(datatable_options.data_source, 
								   datatable_options.data_table, 
								   datatable_options.id_field);
		
	}
	
	boolean setShapeSet(){
		ShapeSet3DInt set = (ShapeSet3DInt)cmbSubjectShapeSet.getSelectedItem();
		if (set == null) return false;
		
		if (shape_set != null && parent_set != null){
			if (set.equals(parent_set)) return true;
			if (parent_set.hasShape(shape_set))
				parent_set.removeShape(shape_set);
			parent_set = set;
			parent_set.addShape(shape_set);
			return true;
			}
		
		shape_set = new ShapeSet3DInt();
		parent_set = set;
		parent_set.addShape(shape_set);
		shape_set.setName("Cortical Thickness Set");
		
		return true;
	}
	
	// Remove all shapes that aren't in this list
	void removeShapes(TreeSet<String> shapes_to_keep){
		
		if (shape_set == null) return;
		
		ArrayList<InterfaceShape> shapes = shape_set.getMembers();
		
		for (int i = 0; i < shapes.size(); i++){
			InterfaceShape shape = shapes.get(i);
			if (!(shape instanceof ShapeSet) && !shapes_to_keep.contains(shape.getName())){
				shape_set.removeShape((Shape3DInt)shape, true, true);
				}
			}
		
	}
	
	void loadSurfaces(String subject, ArrayList<String> surfaces, InterfaceProgressBar progress_bar, TreeSet<String> shapes_to_keep){
		//we want to determine which surfaces are which, and which need loading
		if (shape_set == null) return;
		
//		InterfaceProgressBar progress_bar = new InterfaceProgressBar("Loading surface(s) for subject '" + subject + "'",
//																		InterfaceSession.getDisplayPanel());
//		
//		progress_bar.register();
		progress_bar.setMessage("Loading surface(s) for subject '" + subject + "'");
		
		// For each volume:
		// Load
		// Add to parent set
		for (int i = 0; i < surfaces.size(); i++){
			String surface_name = surfaces.get(i);
			Mesh3DInt mesh = getMesh(subject, surface_name, progress_bar);
			
			if (mesh == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Error loading surfaces; see log.", 
											  "Load cortical surfaces", 
											  JOptionPane.ERROR_MESSAGE);
			
				return;
				}
			
			shapes_to_keep.add(mesh.getName());
			
			if (!shape_set.hasShape(mesh.getName())){
				// Create new volume and add to parent set
				shape_set.addShape(mesh);
			}else{
				// Set data for an existing volume
				Mesh3DInt mesh2 = (Mesh3DInt)shape_set.getShape(mesh.getName());
				mesh2.setMesh(mesh.getMesh());
				}
			progress_bar.iterate();
			}
	
		//progress_bar.deregister();

	}
	
	
	void loadSurfaceData(String subject, ArrayList<String> data, InterfaceProgressBar progress_bar, TreeSet<String> shapes_to_keep){
		if (parent_set == null){
			System.out.println("InterfaceCorticalThicknessPanel: No cortical surface set in which to load data..");
			return;
			}
		if (directory_options.mesh_data_loader == null){
			System.out.println("InterfaceCorticalThicknessPanel: No surface data loader specified..");
			return;
			}

	}
	
	void loadVolumes(String subject, ArrayList<String> volumes, InterfaceProgressBar progress_bar, TreeSet<String> shapes_to_keep){
		
		if (shape_set == null) return;
		
		if (directory_options.volumes_as_composite){
			loadVolumesAsComposite(subject, volumes, progress_bar, shapes_to_keep);
			return;
			}
		
		progress_bar.setMessage("Loading volume(s) for subject '" + subject + "'");
		
		
		// For each volume:
		// Load
		// Add to parent set
		for (int i = 0; i < volumes.size(); i++){
			String volume_name = volumes.get(i);
			String shape_name = getNameForVolume(volume_name);
			Volume3DInt volume_existing = (Volume3DInt)shape_set.getShape(shape_name);
			VertexDataColumn column_existing = null;
			if (volume_existing != null && volume_existing.getCurrentColumn() != null){
				column_existing = volume_existing.getCurrentDataColumn();
				ArrayList<MguiNumber> data = column_existing.getData();
				// Deallocate the memory before loading new data simultaneously into memory;
				// this is necessary for very large volumes
				data.clear();
				System.gc();
				//data.trimToSize();
				}
			Volume3DInt volume = getVolume(subject, volume_name, progress_bar);
			
			if (volume == null){
				InterfaceSession.log("InterfaceCorticalThicknessPanel: Could not load volume '" +
									 volume_name +"'.", LoggingType.Errors);
			}else{
				shapes_to_keep.add(volume.getName());
				if (!shape_set.hasShape(volume.getName())){
					// Create new volume and add to parent set
					shape_set.addShape(volume);
				}else{
					// Set data for an existing volume
					Volume3DInt vol = (Volume3DInt)shape_set.getShape(volume.getName());
					if (column_existing != null){
						VertexDataColumn new_column = volume.getCurrentDataColumn();
						//column_existing.setFromVertexDataColumn(volume.getCurrentDataColumn(),true,false);
						column_existing.setValues(new_column.getData(), true, false);
						column_existing.setDataLimits(new_column.getDataMin(), new_column.getDataMax(), false);
						column_existing.setColourMap(new_column.getColourMap(), false);
						column_existing.setColourLimits(new_column.getColourMin(), new_column.getColourMax(), true);
					}else{
						vol.addVertexData(volume.getCurrentDataColumn());
						}
//					VertexDataColumn column = vol.getCurrentDataColumn();
//					column.setFromVertexDataColumn(volume.getCurrentDataColumn());
					}
				}
			progress_bar.iterate();
			}
		
	}
	
	void loadVolumesAsComposite(String subject, ArrayList<String> volumes, InterfaceProgressBar progress_bar, TreeSet<String> shapes_to_keep){
			
			if (shape_set == null) return;
		
//			InterfaceProgressBar progress_bar = new InterfaceProgressBar("Loading volume(s) for subject '" + subject + "'",
//																			InterfaceSession.getDisplayPanel());
//			progress_bar.register();
			progress_bar.setMessage("Loading volume(s) for subject '" + subject + "'");
			
			String comp_name = "ct_composite_volume";
			Volume3DInt composite_volume = null;
			if (shape_set.hasShape(comp_name)){
				composite_volume = (Volume3DInt)shape_set.getShape(comp_name);
				}
			
			// For each volume:
			// Load
			// Add to parent set
			boolean add_me = false;
			boolean update_me = false;
			for (int i = 0; i < volumes.size(); i++){
				String volume_name = volumes.get(i);
				if (volume_name.contains(File.separator))
					volume_name = volume_name.substring(volume_name.lastIndexOf(File.separator) + 1);
				Volume3DInt volume = getVolume(subject, volumes.get(i), progress_bar, volume_name);
				shapes_to_keep.add(volume.getName());
				if (composite_volume == null){
					// Set this volume as the new composite volume
					composite_volume = volume;
					volume.setName(comp_name);
					composite_volume.isComposite(true);
					add_me = true;
				}else{
					// Set data for an existing volume
					if (composite_volume.hasColumn(volume_name)){
						composite_volume.setVertexData(volume_name, volume.getCurrentVertexData(), false);
						update_me = true;
					}else{
						composite_volume.addVertexData(volume.getCurrentDataColumn());
						}
					}
				progress_bar.iterate();
				}
			
			if (add_me){
				shape_set.addShape(composite_volume);
				}
			if (update_me){
				composite_volume.updateTexture();
				}
			
			shapes_to_keep.add(composite_volume.getName());
			//progress_bar.deregister();
	}
	
	Volume3DInt getVolume(String subject, String volume, InterfaceProgressBar progress_bar){
		return getVolume(subject, volume, progress_bar, "Default");
	}
	
	private String getNameForVolume(String volume){
		if (volume.contains(File.separator)){
			volume = volume.substring(volume.lastIndexOf(File.separator) + 1);
			}
		return volume.replace(".", "_");
	}
	
	Volume3DInt getVolume(String subject, String volume, InterfaceProgressBar progress_bar, String column_name){
		
		//set from options or header?
		String sep = File.separator;
		String p = File.separator;
		
		if (volume.contains(sep)){
			p += volume.substring(0, volume.lastIndexOf(sep) + 1);
			volume = volume.substring(volume.lastIndexOf(sep) + 1);
			}
		
		String path = directory_options.subject_dir + sep + subject + p + 
				  directory_options.subject_prefix;
		if (directory_options.prepend_subject_name)
			path = path + subject + "_";
		path = path + volume;
		
		progress_bar.setMessage("Loading volume '" + volume + "':");
		
		VolumeInputOptions options = directory_options.volume_options;
		options.input_column = column_name;
		if (options.colour_map == null)
			options.colour_map = InterfaceEnvironment.getColourMaps().get(0);
		if (directory_options.set_data_type)
			options.transfer_type = directory_options.data_type;
		VolumeFileLoader loader = getVolumeFileLoader(path);
		Volume3DInt vol = loader.loadVolume(options, progress_bar);
		if (vol == null) return null;
		if (directory_options.set_intensity_from_histogram){
			Histogram hist = VolumeFunctions.getHistogram(vol, 80);
			double data_min = vol.getDataMin();
			double data_max = vol.getDataMax();
			double min = hist.getPercentileValue(directory_options.min_pct);
			//min = (min - data_min) / (data_max - data_min);
			double max = hist.getPercentileValue(directory_options.max_pct);
			//max = (max - data_min) / (data_max - data_min);
			if (min < max){
				vol.getCurrentDataColumn().setColourLimits(min, max);
				//vol.getColourModel().setLimits(min, max);
				}
			}
		vol.setName(volume.replace(".", "_"));
		vol.show3D(directory_options.show_volumes_3d);
		
		return vol;
	}
		
	Mesh3DInt getMesh(String subject, String surface, InterfaceProgressBar progress_bar) {
		
		//set from options or header?
		String sep = File.separator;
		String p = File.separator;
		
		if (surface.contains(sep)){
			p += surface.substring(0, surface.lastIndexOf(sep) + 1);
			surface = surface.substring(surface.lastIndexOf(sep) + 1);
			}
		
		String path = directory_options.subject_dir + sep + subject + p + 
				  directory_options.subject_prefix;
		if (directory_options.prepend_subject_name)
			path = path + subject + "_";
		path = path + surface;
		
		path = StringFunctions.replaceAll(path, "{subject}", subject);
		
		progress_bar.setMessage("Loading surface '" + surface + "':");
		
		SurfaceFileLoader loader = getSurfaceFileLoader(path);
		if (loader == null){
			InterfaceSession.log("Error loading surface at '" + path + "'",
								 LoggingType.Errors);
			return null;
			}
		try{
			Mesh3DInt mesh = loader.loadSurface(progress_bar);
			mesh.setName(surface.replace(".", "_"));
			return mesh;
		}catch (Exception ex){
			InterfaceSession.log("Error loading surface at '" + path + "'", LoggingType.Errors);
			return null;
			}
		
	}
	
	ArrayList<MguiNumber> getSurfData(String subject, String data, boolean left){
		String hemi = directory_options.right_contains;
		if (left) hemi = directory_options.left_contains;
		
		if (data.contains(hemi)){
			//load this surface
			String p = File.separator;
			String sep = File.separator;
			if (data.contains(sep)){
				p += data.substring(0, data.lastIndexOf(sep) + 1);
				data = data.substring(data.lastIndexOf(sep) + 1);
				}
			String path = directory_options.subject_dir + sep + subject + p + 
					  directory_options.subject_prefix;
			if (directory_options.prepend_subject_name)
				path = path + subject + "_";
			path = path + data;
			SurfaceDataFileLoader loader = getSurfaceDataFileLoader(path);
			return loader.loadValues();
			}
		
		return null;
		
	}
	
	
	
	SurfaceFileLoader getSurfaceFileLoader(String path){
		SurfaceFileLoader loader = (SurfaceFileLoader)directory_options.mesh_loader.getIOInstance();
		loader.setFile(new File(path));
		return loader;
	}
	
	SurfaceDataFileLoader getSurfaceDataFileLoader(String path){
		SurfaceDataFileLoader loader = (SurfaceDataFileLoader)directory_options.mesh_data_loader.getIOInstance();
		loader.setFile(new File(path));
		return loader;
	}
	
	VolumeFileLoader getVolumeFileLoader(String path){
		VolumeFileLoader loader = (VolumeFileLoader)directory_options.volume_loader.getIOInstance();
		loader.setFile(new File(path));
		return loader;
	}
	
	void updateSubjectData(){
		//get selection
		ArrayList<String> subjects = lstSubjects.getSelected();
		
		if (subjects.size() == 0) return;
		
		File dir = new File(directory_options.subject_dir + File.separator + subjects.get(0));
		if (!dir.exists() || !dir.isDirectory()){
			InterfaceSession.log("InterfaceCorticalThicknessPanel: bad subject dir: '" + dir.getAbsolutePath() + "'",
								 LoggingType.Errors);
			return;
			}
		
		lstSurfaces.model.empty();
		lstSurfData.model.empty();
		lstVolumes.model.empty();
		//lstAtlas.model.empty();
		
		//recursive search subdirectories for types
		fillSubjectDataObjects(dir, subjects.get(0), "");
		//if (atlas) fillAtlasDataObjects(atlas_dir, "");
		lblSubjectSurfaces.setText("Surfaces (" + lstSurfaces.model.rows + "):");
		lblSubjectData.setText("Surface data (" + lstSurfData.model.rows + "):");
		lblSubjectVolumes.setText("Volumes (" + lstVolumes.model.rows + "):");
		//lblAtlas.setText("Atlases (" + lstAtlas.model.rows + "):");
	}
	
	public void showPanel(){
		
		this.updateShapeSetList();
		this.updateDataWindowList();
		
		updateDisplay();
	}
	
	void fillAtlasDataObjects(File dir, String prefix){
		String[] subdirs = dir.list();
		for (int i = 0; i < subdirs.length; i++){
			File d = new File(dir.getAbsolutePath() + File.separator + subdirs[i]);
			if (d.isDirectory()){
				fillAtlasDataObjects(d, prefix + d.getName() + File.separator);
			}else{
				if (directory_options.mesh_data_loader != null && directory_options.mesh_data_loader.getFilter().accept(d))
					lstAtlas.model.addRow(prefix + d.getName());
				}
			}
	}
	
	void fillSubjectDataObjects(File dir, String subject, String prefix){
		String[] subdirs = dir.list();
		for (int i = 0; i < subdirs.length; i++){
			File d = new File(dir.getAbsolutePath() + File.separator + subdirs[i]);
			if (d.isDirectory()){
				fillSubjectDataObjects(d, subject, prefix + d.getName() + File.separator);
			}else{
				if (directory_options.mesh_loader != null && directory_options.mesh_loader.getFilter().accept(d))
					lstSurfaces.model.addRow(stripPrefix(prefix + d.getName(), subject));
				if (directory_options.mesh_data_loader != null && directory_options.mesh_data_loader.getFilter().accept(d))
					lstSurfData.model.addRow(stripPrefix(prefix + d.getName(), subject));
				if (directory_options.volume_loader != null && directory_options.volume_loader.getFilter().accept(d))
					lstVolumes.model.addRow(stripPrefix(prefix + d.getName(), subject));
				}
			}
	}
	
	String stripPrefix(String file, String subject){
		String stripped = new String(file);
		String prefix = directory_options.subject_prefix;
		prefix = StringFunctions.replaceAll(prefix, "{subject}", subject);
		if (file.startsWith(prefix))
			stripped = stripped.substring(prefix.length());
		if (stripped.startsWith(subject))
			stripped = stripped.substring(subject.length());
		if (stripped.startsWith("_"))
			stripped = stripped.substring(1);
		return stripped;
	}
	
	void updateDataTableList(){
		DataTableList s = new DataTableList(0);
		if (lstDataTables == null){
			lstDataTables = s;
		}else{
			lstDataTables.setFromList(s);
		}
		
		lstDataTables.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstDataTables.setListener();
	}
	
	void updateDataWindowList(){
		
		cmbDataWindow.removeAllItems();
		InterfaceDisplayPanel displayPanel = InterfaceSession.getDisplayPanel();
		ArrayList<InterfaceGraphicWindow> panels = displayPanel.getPanels();
		
		for (int i = 0; i < panels.size(); i++){
			if (panels.get(i).getPanel() instanceof InterfaceDataTable)
				cmbDataWindow.addItem(panels.get(i).getPanel());
			}
		
		if (cmbDataWindow.getSelectedItem() != null)
			cmdDataShowTable.setEnabled(true);
		else
			cmdDataShowTable.setEnabled(false);
	}
	
	class SubjectDataTable{
		public DataSource source;
		public DataTable table;
		public DataField id_field;
		
		public SubjectDataTable(DataSource source, DataTable table, DataField id_field){
			this.source = source;
			this.table = table;
			this.id_field = id_field;
		}
	}
	
	void updateSubjectList(){
		SubjectList s = new SubjectList("Subject ID", 0);
		if (lstSubjects == null){
			lstSubjects = s;
			s.setListener();
		}else{
			lstSubjects.setFromList(s);
			s.setListener();
		}
		
		lstSubjects.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		ArrayList<String> subjects = getSubjectList();
		
		for (int i = 0; i < subjects.size(); i++)
			lstSubjects.model.addRow(subjects.get(i));
		
		lblSubjectList.setText("Subjects (" + subjects.size() + "):");
		
		lstSubjects.updateUI();
	}
	
	ArrayList<String> getSubjectList(){
		ArrayList<String> subjects = new ArrayList<String>();
		if (directory_options.subject_dir == null) return subjects;
		File dir = new File(directory_options.subject_dir);
		if (!dir.exists() || !dir.isDirectory()) return subjects;
		
		File[] s_files = dir.listFiles();
		
		for (int i = 0; i < s_files.length; i++)
			if (s_files[i].isDirectory())
				subjects.add(s_files[i].getName());
		
		Collections.sort(subjects);
		return subjects;
	}
	
	public String toString(){
		return "Cortical Thickness Panel";
	}
	
	void updateSubjectSelection(boolean multi, boolean first, boolean last){
		if (multi){
			cmdSubjectLoad.setText("Load selection means");
			cmdSubjectPrev.setEnabled(false);
			cmdSubjectNext.setEnabled(false);
		}else{
			cmdSubjectLoad.setText("Load selected subject");
			cmdSubjectPrev.setEnabled(!first);
			cmdSubjectNext.setEnabled(!last);
			}
	}
	
	class SubjectList extends JPanel {
		public JTable table;
		public SubjectListModel model;
		public JScrollPane scrollPane;
		public SubjectSelectionListener listener;
		
		public SubjectList(String label, int rows){
			//if (rows == 0) rows = 1;
			model = new SubjectListModel();
			model.setRowCount(rows);
			model.setColumnName(0, label);
			
			table = new JTable(model);
			scrollPane = new JScrollPane(table);
			setLayout(new GridLayout(1, 1));
			add(scrollPane);
			
		}
		
		public void setFromList(SubjectList s){
			scrollPane = s.scrollPane;
			table = s.table;
			
			model = s.model;
			removeAll();
			setLayout(new GridLayout(1, 1));
			add(scrollPane);
		}
		
		public void setListener(){
			listener = new SubjectSelectionListener(this);
			table.getSelectionModel().addListSelectionListener(listener);
		}
		
		public void updateDisplay(){
			table.getTableHeader().updateUI();
			table.updateUI();
		}
		
		public void addSubject(String id){
			model.addRow(id);
		}
		
		public ArrayList<String> getSelected(){
			ListSelectionModel m = table.getSelectionModel();
			ArrayList<String> subjects = new ArrayList<String>();
			
			for (int i = 0; i < model.rows; i++)
				if (m.isSelectedIndex(i))
					subjects.add(model.getSubject(i));
			
			return subjects;
		}
		
		public boolean isFirstSelected(){
			return ((ListSelectionModel)table.getSelectionModel()).isSelectedIndex(0);
		}
		
		public boolean isLastSelected(){
			ListSelectionModel m = table.getSelectionModel();
			return m.getMaxSelectionIndex() == model.rows;
		}
		
		public void removeWindow(int row){
			model.removeRow(row);
		}
		
		public void moveUp(int row){
			
		}
		
		public void moveDown(int row){
			
		}
		
	}
	
	class SubjectListModel extends DefaultTableModel{
		
		DefaultTableModel m = new DefaultTableModel();
		ArrayList<String> subjects = new ArrayList<String>();
		int rows;
		//int cols;
		String label = "Subject ID";
		
		public SubjectListModel(){
			
		}
		
		public void setValueAt(Object val, int row, int col){
			if (col != 0) return;
			subjects.set(row, (String)val);
		}
		
		public Object getValueAt(int row, int col){
			if (col != 0) return null;
			return subjects.get(row);
		}
		
		public int getRowCount(){
			return rows;
		}
		
		public int getColumnCount(){
			return 1;
		}
		
		public void setRowCount(int r){
			rows = r;
			while (r > subjects.size())
				subjects.add("");
			while (r < subjects.size())
				subjects.remove(subjects.size() - 1);
			fireTableChanged(new TableModelEvent(this));
		}
		
		public void empty(){
			subjects = new ArrayList<String>();
			rows = 0;
			fireTableChanged(new TableModelEvent(this));
		}
		
		public void setColCount(int c){
			return;
		}
		
		public String getSubject(int r){
			return (String)getValueAt(r, 0);
		}
		
		public void addRow(String id){
			subjects.add(id);
			rows++;
			this.fireTableRowsInserted(rows - 1, rows - 1);
		}
		
		public void removeRow(int row){
			subjects.remove(row);
			rows--;
			this.fireTableRowsDeleted(row, row);
		}
		
		public void setColumnName(int c, String name){
			label = name;
			this.fireTableStructureChanged();
		}
		
		public String getColumnName(int c){
			return label;
		}
		
		public Class getColumnClass(int c){
			return String.class;
		}
		
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
	}
	
	class SubjectSelectionListener implements ListSelectionListener {
		SubjectList list;
    
        SubjectSelectionListener(SubjectList list) {
            this.list = list;
        }
        
        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column change events are fired
        	ListSelectionModel m = list.table.getSelectionModel();
            if (e.getSource() == m){
            	ArrayList<String> subjects = list.getSelected();
            	updateSubjectSelection(subjects.size() > 1, 
            						   list.isFirstSelected(),
            						   list.isLastSelected());
            	lblSubjectList.setText("Subjects (" + subjects.size() + " of " + list.model.rows + "):");
            	}
        }
    }
	
	class DataTableList extends JPanel {
		public JTable table;
		public DataTableListModel model;
		public JScrollPane scrollPane;
		public ListSelectionListener listener;
		public int selected_row = -1;
		
		public DataTableList(int rows){
			//if (rows == 0) rows = 1;
			model = new DataTableListModel();
			model.setRowCount(rows);
			
			table = new JTable(model);
			scrollPane = new JScrollPane(table);
			setLayout(new GridLayout(1, 1));
			add(scrollPane);
			
		}
		
		public String[] getSelected(){
			if (selected_row < 0) return null;
			
			return new String[]{(String)model.getValueAt(selected_row, 0),
								(String)model.getValueAt(selected_row, 1),
								(String)model.getValueAt(selected_row, 2)};
			
		}
		
		public void setListener(){
			listener = new ListSelectionListener(){

				public void valueChanged(ListSelectionEvent e) {
					ListSelectionModel m = table.getSelectionModel();
					selected_row = m.getMinSelectionIndex();
				}
			};
			table.getSelectionModel().addListSelectionListener(listener);
		}
		
		public void setFromList(DataTableList s){
			scrollPane = s.scrollPane;
			table = s.table;
			
			model = s.model;
			removeAll();
			setLayout(new GridLayout(1, 1));
			add(scrollPane);
		}
		
		public void updateDisplay(){
			table.getTableHeader().updateUI();
			table.updateUI();
		}
		
		public void addDataTable(String data_source, String data_table, String id_field){
			model.addRow(data_source, data_table, id_field);
		}
		
		
		public void removeDataTable(int row){
			model.removeRow(row);
		}
		
		public void moveUp(int row){
			
		}
		
		public void moveDown(int row){
			
		}
		
	}
	
	class AtlasTableModel extends AbstractTableModel{
		
		
		ArrayList<Boolean> use_atlas = new ArrayList<Boolean>();
		ArrayList<InterfaceNeuroAtlas> atlases = new ArrayList<InterfaceNeuroAtlas>();
		
		public AtlasTableModel(ArrayList<InterfaceNeuroAtlas> atlases){
			if (atlases == null){
				this.atlases = new ArrayList<InterfaceNeuroAtlas>();
				use_atlas = new ArrayList<Boolean>();
				return;
				}
			this.atlases = new ArrayList<InterfaceNeuroAtlas>(atlases);
			use_atlas = new ArrayList<Boolean>(atlases.size());
			for (int i = 0; i < atlases.size(); i++)
				use_atlas.add(new Boolean(false));
		}
		
		public String getColumnName(int col) {
			if (col == 0) return "Use";
			return "Atlas";
		}
		
		public boolean isCellEditable(int row, int col){
			if (col != 0) return false;
			return true;
		}
		
		public void setValueAt(Object aValue, int row, int column) {
			if (column != 0) return;
			Boolean b = (Boolean)aValue;
			Collections.fill(use_atlas, false);
			if (b != false) use_atlas.set(row, true);
			this.fireTableDataChanged();
	    }
		
		public Object getValueAt(int row, int column){
			if (column == 0) return use_atlas.get(row);
			return atlases.get(row);
		}
		
		 public int getRowCount() {
			 return atlases.size();
		 }
		
		public Class getColumnClass(int col){
			if (col == 0) return Boolean.class;
			return InterfaceNeuroAtlas.class;
		}
		 
		public boolean useAtlas(String name){
			int i = getAtlasIndex(name);
			if (i < 0) return false;
			return use_atlas.get(i);
		}
		
		public int getAtlasIndex(String name){
			for (int i = 0; i < atlases.size(); i++)
				if (atlases.get(i).getName().equals(name)) return i;
			return -1;
		}
		
		public int getColumnCount() {
			return 2;
		}

	   
	}
	
	class DataTableListModel extends DefaultTableModel{
		
		DefaultTableModel m = new DefaultTableModel();
		ArrayList<String> sources = new ArrayList<String>();
		ArrayList<String> tables = new ArrayList<String>();
		ArrayList<String> fields = new ArrayList<String>();
		int rows;
		
		
		public DataTableListModel(){
			
		}
		
		public void setValueAt(Object val, int row, int col){
			switch (col){
				case 0:
					sources.set(row, (String)val);
				case 1:
					tables.set(row, (String)val);
				case 2:
					fields.set(row, (String)val);
				}
		}
		
		public Object getValueAt(int row, int col){
			switch (col){
				case 0:
					return sources.get(row);
				case 1:
					return tables.get(row);
				case 2:
					return fields.get(row);
				}
			return "";
		}
		
		public int getRowCount(){
			return rows;
		}
		
		public int getColumnCount(){
			return 3;
		}
		
		public void setRowCount(int r){
			rows = r;
			while (r > sources.size()){
				sources.add("");
				tables.add("");
				fields.add("");
				}
			while (r < sources.size()){
				sources.remove(sources.size() - 1);
				tables.remove(tables.size() - 1);
				fields.remove(fields.size() - 1);
				}
		}
		
		public void setColCount(int c){
			return;
		}
		
		public String getSubject(int r){
			return (String)getValueAt(r, 0);
		}
		
		public void addRow(String data_source, String data_table, String id_field){
			sources.add(data_source);
			tables.add(data_table);
			fields.add(id_field);
			rows++;
			this.fireTableRowsInserted(rows - 1, rows - 1);
		}
		
		public void removeRow(int row){
			sources.remove(row);
			tables.remove(row);
			fields.remove(row);
			rows--;
			this.fireTableRowsDeleted(row, row);
		}
		
		public boolean containsTable(String source, String table){
			for (int i = 0; i < sources.size(); i++)
				if (sources.get(i).equals(source) && tables.get(i).equals(table)) return true;
			return false;
		}
		
		public void setColumnName(int c, String name){
			
		}
		
		public String getColumnName(int c){
			switch (c){
				case 0:
					return "Data Source";
				case 1:
					return "Data Table";
				case 2:
					return "ID Field";
				}
			return "";
		}
		
		public Class getColumnClass(int c){
			/*
			switch (c){
				case 0:
					return DataSource.class;
				case 1:
					return DataTable.class;
				case 2:
					return DataField.class;
			}
			*/
			return String.class;
		}
		
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
	}
}