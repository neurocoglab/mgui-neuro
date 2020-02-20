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


package mgui.interfaces.neuro.imaging.camino;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.media.j3d.Node;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.foreign.camino.CaminoPipelineLoader;
import mgui.io.foreign.camino.CaminoPipelineWriter;
import mgui.io.foreign.camino.CaminoProjectLoader;
import mgui.io.foreign.camino.CaminoProjectWriter;
import mgui.io.util.IoFunctions;
import mgui.neuro.imaging.camino.CaminoEnvironment;
import mgui.neuro.imaging.camino.CaminoException;
import mgui.neuro.imaging.camino.CaminoFunctions;
import mgui.neuro.imaging.camino.CaminoProcesses;
import mgui.neuro.imaging.camino.CaminoProject;
import mgui.neuro.imaging.camino.CaminoTaskTreeNode;
import mgui.pipelines.InterfacePipeline;
import mgui.pipelines.PipelineException;
import mgui.pipelines.PipelineProcess;
import mgui.pipelines.PipelineProcessInstance;
import mgui.pipelines.PipelineTask;
import mgui.pipelines.TaskParameter;
import mgui.pipelines.PipelineTask.Status;
import mgui.util.PowerfulTokenizer;

//import sun.swing.DefaultLookup;

import data.OutputManager;

import foxtrot.Job;
import foxtrot.Worker;


public class InterfaceCaminoPanel extends InterfacePanel implements ActionListener{

	CategoryTitle lblProject = new CategoryTitle("PROJECT");
	JList lstProjects;
	JScrollPane scrProjects = new JScrollPane(lstProjects);
	DefaultListModel project_model;
	JButton cmdProjectNew = new JButton("New");
	JButton cmdProjectRem = new JButton("Remove");
	JButton cmdProjectSelect = new JButton("Select");
	JButton cmdProjectEdit = new JButton("Edit..");
	JButton cmdProjectSave = new JButton("Save");
	JButton cmdProjectOpen = new JButton("Open");
	
	CategoryTitle lblSubjects = new CategoryTitle("SUBJECTS");
	JList lstSubjects;
	JScrollPane scrSubjects = new JScrollPane(lstSubjects);
	DefaultListModel subject_model;
	JButton cmdSubjectsAdd = new JButton("Add..");
	JButton cmdSubjectsRem = new JButton("Remove");
	
	CategoryTitle lblPipelines = new CategoryTitle("PIPELINES");
	CaminoPipelineTree treePipelines;
	JScrollPane scrPipelines;
	JLabel lblPipelineSubjects = new JLabel("Subjects");
	JCheckBox chkPipelineAllSubjects = new JCheckBox("Process all subjects");
	JCheckBox chkPipelineSelSubjects = new JCheckBox("Process selected subjects");
	JCheckBox chkPipelineNoSubjects = new JCheckBox("Do not process subjects");
	JLabel lblPipelinePipelines = new JLabel("Pipelines");
	JCheckBox chkPipelineSerial = new JCheckBox("Process serial");
	JCheckBox chkPipelineParallel = new JCheckBox("Process parallel");
	JCheckBox chkPipelineFailOnException = new JCheckBox("Fail on exception");
	JButton cmdPipelineProcessUp = new JButton("Up");
	JButton cmdPipelineProcessDown = new JButton("Down");
	JButton cmdPipelineLoad = new JButton("Load");
	JButton cmdPipelineSave = new JButton("Save");
	JLabel lblPipelineAddRem = new JLabel("Pipeline:");
	JButton cmdPipelineNew = new JButton("New");
	JButton cmdPipelineRem = new JButton("Remove");
	JLabel lblPipelineProcessAddRem = new JLabel("Process:");
	JButton cmdPipelineProcessAdd = new JButton("Add");
	JButton cmdPipelineProcessRem = new JButton("Remove");
	JButton cmdPipelineProcessEdit = new JButton("Edit");
	JButton cmdPipelineLaunch = new JButton("Launch");
	JButton cmdPipelineReset = new JButton("Reset");
	
	CategoryTitle lblEnvironment = new CategoryTitle("ENVIRONMENT");
	JLabel lblEnvironmentInitFile = new JLabel("Init file:");
	JTextField txtEnvironmentInitFile = new JTextField("resources/init/camino/environment.xml");
	JButton cmdEnvironmentInitFile = new JButton("Browse..");
	JLabel lblEnvironmentUpdate = new JLabel("Update file:");
	JButton cmdEnvironmentUpdate = new JButton("Go");
	JLabel lblEnvironmentProcesses = new JLabel("Processes:");
	JList lstEnvironmentProcesses;
	JScrollPane scrEnvironmentProcesses;
	DefaultListModel process_model;
	JButton cmdEnvironmentAddProcess = new JButton("Add");
	JButton cmdEnvironmentEditProcess = new JButton("Edit");
	
	CategoryTitle lblCommandLine = new CategoryTitle("COMMAND LINE");
	JLabel lblCommand = new JLabel("Command:");
	JComboBox cmbCommand = new JComboBox();
	JLabel lblCommandArgs = new JLabel("Arguments:");
	JTextArea txtCommandArgs = new JTextArea(0,0);
	JScrollPane scrCommandArgs = new JScrollPane(txtCommandArgs);
	JButton cmdCommandExec = new JButton("Execute");
	
	ArrayList<CaminoProject> projects = new ArrayList<CaminoProject>();
	CaminoProject currentProject;
	
	public InterfaceCaminoPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	/*
	public InterfaceCaminoPanel(InterfaceDisplayPanel p){
		//setDisplayPanel(p);
		init();
	}
	*/

	String getInitFile(){
		String sep = File.separator;
		//return "resources" + sep +
		//       "init" + sep +
		//       "camino" + sep +
		//       "camino.init";
		return "/mgui/resources/init/camino/camino.init";
	}	

	protected void init() {
		_init();
		
		CaminoEnvironment.initFromFile(getInitFile());
		
		setLayout(new CategoryLayout(20, 5, 200, 10));
		setCommands();
		
		cmdCommandExec.addActionListener(this);
		cmdCommandExec.setActionCommand("Command Exec");
		cmdPipelineLaunch.addActionListener(this);
		cmdPipelineLaunch.setActionCommand("Pipeline Exec");
		cmdPipelineReset.addActionListener(this);
		cmdPipelineReset.setActionCommand("Pipeline Reset");
		cmdProjectEdit.addActionListener(this);
		cmdProjectEdit.setActionCommand("Project Edit");
		cmdProjectNew.addActionListener(this);
		cmdProjectNew.setActionCommand("Project New");
		cmdProjectOpen.addActionListener(this);
		cmdProjectOpen.setActionCommand("Project Open");
		cmdProjectSave.addActionListener(this);
		cmdProjectSave.setActionCommand("Project Save");
		cmdProjectRem.addActionListener(this);
		cmdProjectRem.setActionCommand("Project Remove");
		cmdProjectSelect.addActionListener(this);
		cmdProjectSelect.setActionCommand("Project Select");
		cmdPipelineNew.addActionListener(this);
		cmdPipelineNew.setActionCommand("Pipeline New");
		cmdPipelineSave.addActionListener(this);
		cmdPipelineSave.setActionCommand("Pipeline Save");
		cmdPipelineLoad.addActionListener(this);
		cmdPipelineLoad.setActionCommand("Pipeline Load");
		cmdPipelineRem.addActionListener(this);
		cmdPipelineRem.setActionCommand("Pipeline Remove");
		cmdPipelineProcessAdd.addActionListener(this);
		cmdPipelineProcessAdd.setActionCommand("Pipeline Process Add");
		cmdPipelineProcessRem.addActionListener(this);
		cmdPipelineProcessRem.setActionCommand("Pipeline Process Remove");
		cmdPipelineProcessEdit.addActionListener(this);
		cmdPipelineProcessEdit.setActionCommand("Pipeline Process Edit");
		cmdPipelineProcessUp.addActionListener(this);
		cmdPipelineProcessUp.setActionCommand("Pipeline Process Up");
		cmdPipelineProcessDown.addActionListener(this);
		cmdPipelineProcessDown.setActionCommand("Pipeline Process Down");
		cmdEnvironmentUpdate.addActionListener(this);
		cmdEnvironmentUpdate.setActionCommand("Environment Update");
		cmdEnvironmentAddProcess.addActionListener(this);
		cmdEnvironmentAddProcess.setActionCommand("Environment Add Process");
		cmdEnvironmentEditProcess.addActionListener(this);
		cmdEnvironmentEditProcess.setActionCommand("Environment Edit Process");
		
		chkPipelineNoSubjects.addActionListener(this);
		chkPipelineNoSubjects.setActionCommand("Pipeline Subjects None");
		chkPipelineAllSubjects.addActionListener(this);
		chkPipelineAllSubjects.setActionCommand("Pipeline Subjects All");
		chkPipelineSelSubjects.addActionListener(this);
		chkPipelineSelSubjects.setActionCommand("Pipeline Subjects Sel");
		chkPipelineSerial.setSelected(true);
		chkPipelineSerial.addActionListener(this);
		chkPipelineSerial.setActionCommand("Pipeline Serial");
		chkPipelineParallel.addActionListener(this);
		chkPipelineParallel.setActionCommand("Pipeline Parallel");
		chkPipelineNoSubjects.setSelected(true);
		
		
		txtCommandArgs.setLineWrap(true);
		
		updateProjectList();
		updateProcessList();
		
		updateProjectList();
		setCurrentProject(null);
		
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblProject, c);
		lblProject.setParentObj(this);
		c = new CategoryLayoutConstraints("PROJECT", 1, 7, 0.05, .9, 1);
		add(scrProjects, c);
		c = new CategoryLayoutConstraints("PROJECT", 8, 8, 0.05, .43, 1);
		add(cmdProjectSelect, c);
		c = new CategoryLayoutConstraints("PROJECT", 8, 8, 0.52, .43, 1);
		add(cmdProjectEdit, c);
		c = new CategoryLayoutConstraints("PROJECT", 9, 9, 0.05, .43, 1);
		add(cmdProjectRem, c);
		c = new CategoryLayoutConstraints("PROJECT", 9, 9, 0.52, .43, 1);
		add(cmdProjectNew, c);
		c = new CategoryLayoutConstraints("PROJECT", 10, 10, 0.05, .43, 1);
		add(cmdProjectOpen, c);
		c = new CategoryLayoutConstraints("PROJECT", 10, 10, 0.52, .43, 1);
		add(cmdProjectSave, c);
		
		c = new CategoryLayoutConstraints();
		add(lblSubjects, c);
		lblSubjects.setParentObj(this);
		c = new CategoryLayoutConstraints("SUBJECTS", 1, 7, 0.05, .9, 1);
		add(scrSubjects, c);
		c = new CategoryLayoutConstraints("SUBJECTS", 8, 8, 0.05, .43, 1);
		add(cmdSubjectsAdd, c);
		c = new CategoryLayoutConstraints("SUBJECTS", 8, 8, 0.52, .43, 1);
		add(cmdSubjectsRem, c);
		
		c = new CategoryLayoutConstraints();
		add(lblPipelines, c);
		lblPipelines.setParentObj(this);
		c = new CategoryLayoutConstraints("PIPELINES", 1, 1, 0.05, .9, 1);
		add(lblPipelineSubjects, c);
		c = new CategoryLayoutConstraints("PIPELINES", 2, 2, 0.1, .85, 1);
		add(chkPipelineAllSubjects, c);
		c = new CategoryLayoutConstraints("PIPELINES", 3, 3, 0.1, .85, 1);
		add(chkPipelineSelSubjects, c);
		c = new CategoryLayoutConstraints("PIPELINES", 4, 4, 0.1, .85, 1);
		add(chkPipelineNoSubjects, c);
		c = new CategoryLayoutConstraints("PIPELINES", 5, 5, 0.05, .9, 1);
		add(lblPipelinePipelines, c);
		c = new CategoryLayoutConstraints("PIPELINES", 6, 6, 0.1, .85, 1);
		add(chkPipelineSerial, c);
		c = new CategoryLayoutConstraints("PIPELINES", 7, 7, 0.1, .85, 1);
		add(chkPipelineParallel, c);
		c = new CategoryLayoutConstraints("PIPELINES", 8, 8, 0.1, .85, 1);
		add(chkPipelineFailOnException, c);
		c = new CategoryLayoutConstraints("PIPELINES", 9, 17, 0.05, .9, 1);
		add(scrPipelines, c);
		c = new CategoryLayoutConstraints("PIPELINES", 18, 18, 0.05, .9, 1);	
		add(lblPipelineProcessAddRem, c);
		lblPipelineProcessAddRem.setHorizontalAlignment(JLabel.CENTER);
		c = new CategoryLayoutConstraints("PIPELINES", 19, 19, 0.05, .43, 1);
		add(cmdPipelineProcessDown, c);
		c = new CategoryLayoutConstraints("PIPELINES", 19, 19, 0.52, .43, 1);
		add(cmdPipelineProcessUp, c);
		c = new CategoryLayoutConstraints("PIPELINES", 20, 20, 0.05, .43, 1);
		add(cmdPipelineProcessRem, c);
		c = new CategoryLayoutConstraints("PIPELINES", 20, 20, 0.52, .43, 1);
		add(cmdPipelineProcessAdd, c);
		c = new CategoryLayoutConstraints("PIPELINES", 21, 21, 0.05, .43, 1);
		add(cmdPipelineProcessEdit, c);
		c = new CategoryLayoutConstraints("PIPELINES", 22, 22, 0.05, .9, 1);
		add(lblPipelineAddRem, c);
		lblPipelineAddRem.setHorizontalAlignment(JLabel.CENTER);
		c = new CategoryLayoutConstraints("PIPELINES", 23, 23, 0.05, .43, 1);
		add(cmdPipelineRem, c);
		c = new CategoryLayoutConstraints("PIPELINES", 23, 23, 0.52, .43, 1);
		add(cmdPipelineNew, c);
		c = new CategoryLayoutConstraints("PIPELINES", 24, 24, 0.05, .43, 1);
		add(cmdPipelineLoad, c);
		c = new CategoryLayoutConstraints("PIPELINES", 24, 24, 0.52, .43, 1);
		add(cmdPipelineSave, c);
		c = new CategoryLayoutConstraints("PIPELINES", 25, 26, 0.52, .43, 1);
		add(cmdPipelineLaunch, c);
		c = new CategoryLayoutConstraints("PIPELINES", 25, 26, 0.05, .43, 1);
		add(cmdPipelineReset, c);
		
		c = new CategoryLayoutConstraints();
		add(lblEnvironment, c);
		lblEnvironment.setParentObj(this);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 1, 1, 0.05, .3, 1);
		add(lblEnvironmentInitFile, c);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 1, 1, 0.35, .6, 1);
		add(txtEnvironmentInitFile, c);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 2, 2, 0.51, .44, 1);
		add(cmdEnvironmentInitFile, c);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 3, 3, 0.05, .44, 1);
		add(lblEnvironmentUpdate, c);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 3, 3, 0.51, .44, 1);
		add(cmdEnvironmentUpdate, c);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 4, 4, 0.05, .9, 1);
		add(lblEnvironmentProcesses, c);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 5, 10, 0.05, .9, 1);
		add(scrEnvironmentProcesses, c);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 11, 11, 0.05, .44, 1);
		add(cmdEnvironmentAddProcess, c);
		c = new CategoryLayoutConstraints("ENVIRONMENT", 11, 11, 0.51, .44, 1);
		add(cmdEnvironmentEditProcess, c);
		
		
		/*
		c = new CategoryLayoutConstraints();
		add(lblCommandLine, c);
		lblCommandLine.setParentObj(this);
		c = new CategoryLayoutConstraints("COMMAND LINE", 1, 1, 0.05, .3, 1);
		add(lblCommand, c);
		c = new CategoryLayoutConstraints("COMMAND LINE", 1, 1, 0.35, .6, 1);
		add(cmbCommand, c);
		c = new CategoryLayoutConstraints("COMMAND LINE", 2, 2, 0.05, .9, 1);
		add(lblCommandArgs, c);
		c = new CategoryLayoutConstraints("COMMAND LINE", 3, 6, 0.05, .9, 1);
		add(scrCommandArgs, c);
		c = new CategoryLayoutConstraints("COMMAND LINE", 7, 7, 0.05, .9, 1);
		add(cmdCommandExec, c);
		*/
		
	}
	
	void updateEnvironment(){
		
		String file = txtEnvironmentInitFile.getText();
		File init_file = null;
		if (file.startsWith(File.separator)){
			File current = new File(".");
			String path = current.getAbsolutePath();
			path = path.substring(0, path.lastIndexOf(File.separator));
			init_file = new File(path + file);
		}else{
			init_file = new File(file);
			}
		
		if (!init_file.exists()){
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Cannot find init file!", 
										  "Update Camino init file", 
										  JOptionPane.ERROR_MESSAGE);
			return;
			}
		
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(init_file));
			if (!CaminoEnvironment.writeToXML(writer))
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Error writing to environment file!", 
											  "Update Camino environment file", 
											  JOptionPane.ERROR_MESSAGE);
			else
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Updated environment file.", 
											  "Update Camino init file", 
											  JOptionPane.INFORMATION_MESSAGE);
			writer.close();
		}catch (Exception e){
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Error opening init file!", 
										  "Update Camino init file", 
										  JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			}
		
	}
	
	void initProcesses(){
		
		CaminoProcess CaminoProcess = new CaminoProcess("image-to-voxel", 
		  										  "apps.ImageToVoxel");
		CaminoProcess.addParameter(new TaskParameter("test", "default", false));
		CaminoEnvironment.registerProcess(CaminoProcess);
	}
	
	CaminoProject getDemoProject(){
		CaminoProject project = new CaminoProject("Test", IoFunctions.getCurrentDir());
		
		InterfacePipeline pipeline = new InterfacePipeline("Pipeline 1");
		CaminoProcess CaminoProcess = new CaminoProcess("some-task", "apps.sometask");
		CaminoProcess.addParameter(new TaskParameter("parameter1"));
		PipelineProcessInstance process_instance = new PipelineProcessInstance(CaminoProcess, 0);
		process_instance.setParameter("parameter1", "some-value");
		pipeline.append(process_instance);
		process_instance = new PipelineProcessInstance(CaminoProcess, 1);
		pipeline.append(process_instance);
		CaminoProcess = new CaminoProcess("some-other-task", "apps.someothertask");
		process_instance = new PipelineProcessInstance(CaminoProcess, 0);
		pipeline.append(new PipelineProcessInstance(CaminoProcess, 0));
		
		project.addPipeline(pipeline);
		
		pipeline = new InterfacePipeline("Pipeline 2");
		CaminoProcess = new CaminoProcess("some-task", "apps.sometask");
		CaminoProcess.addParameter(new TaskParameter("parameter1"));
		process_instance = new PipelineProcessInstance(CaminoProcess, 2);
		process_instance.setParameter("parameter1", "some-value");
		pipeline.append(process_instance);
		
		project.addPipeline(pipeline);
		
		return project;
	}
	
	CaminoProject getDemoProject2(){
		CaminoProject project = new CaminoProject("Test", IoFunctions.getCurrentDir());
		
		InterfacePipeline pipeline = new InterfacePipeline("Pipeline 1");
		PipelineProcessInstance process = getTimedTestProcess(2000, 0);
		pipeline.append(process);
		process = getTimedTestProcess(5000, 1);
		pipeline.append(process);
		process = getTimedTestProcess(1000, 2);
		pipeline.append(process);
		
		project.addPipeline(pipeline);
		
		return project;
	}
	
	PipelineProcessInstance getImageToVoxelProcess(int t, int i){
		
		PipelineProcess process = CaminoEnvironment.getProcess("image-to-voxel");
		PipelineProcessInstance instance = new PipelineProcessInstance(process, i);
		
		return instance;
	}
	
	PipelineProcessInstance getTimedTestProcess(int t, int i){
		PipelineProcess process = CaminoEnvironment.getProcess("timed-test");
		PipelineProcessInstance instance = new PipelineProcessInstance(process, i);
		instance.setParameter("Delay", "" + t);
		
		return instance;
	}
	
	void setCommands(){
		cmbCommand.removeAllItems();
		
		Iterator<PipelineProcess> itr = CaminoProcesses.getProcesses().values().iterator();
		
		while (itr.hasNext())
			cmbCommand.addItem(itr.next());
		
	}
	
	PipelineProcessInstance getSelectedTask(){
		if (treePipelines == null) return null;
		return treePipelines.getSelectedTask();
	}
	
	InterfacePipeline getSelectedPipeline(){
		//get pipeline from tree selection
		if (treePipelines == null) return null;
		return treePipelines.getSelectedPipeline();
	}
	
	ArrayList<InterfacePipeline> getSelectedPipelines(){
		if (treePipelines == null) return null;
		return treePipelines.getSelectedPipelines();
	}
	
	boolean setSelectedProcess(InterfacePipeline pipeline, PipelineTask process){
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)treePipelines.getModel().getRoot();
		int index = pipeline.getTaskIndex(process);
		if (index < 0) return false;
		Enumeration children = root.children();
		
		while (children.hasMoreElements()){
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
			if (child.getUserObject().equals(pipeline)){
				TreePath path_to_pipeline = new TreePath(child.getPath());
				treePipelines.setExpandsSelectedPaths(true);
				treePipelines.addSelectionPath(path_to_pipeline.pathByAddingChild(child.getChildAt(index)));
				return true;
				}
			}
		
		return false;
		
	}
	
	void setPipelineTree(){
		
		InterfacePipeline pipeline = getSelectedPipeline();
		DefaultTreeModel model;
		
		if (treePipelines == null){
			
			model = new DefaultTreeModel(null);
			treePipelines = new CaminoPipelineTree(model, this);
			if (currentProject != null)
				model.setRoot(currentProject.getTreeNode(treePipelines));
			treePipelines.setRootVisible(false);
			treePipelines.setCellRenderer(new PipelineRenderer());
			treePipelines.setFont(new Font("Courier", Font.PLAIN, 13));
			treePipelines.setRowHeight(22);
			treePipelines.setDragEnabled(true);
			scrPipelines = new JScrollPane(treePipelines);
			}
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)treePipelines.getModel().getRoot();
		//if (root.getUserObject() == currentProject) return;
		
		if (root != null){
			//otherwise detach all children
			Enumeration children = root.children();
			while (children.hasMoreElements()){
				Object obj = children.nextElement();
				if (obj instanceof CaminoTaskTreeNode)
					((CaminoTaskTreeNode)obj).detach();
				}
			}
		
		if (currentProject == null){
			treePipelines.updateUI();
			return;
			}
		model = (DefaultTreeModel)treePipelines.getModel(); 
		model.setRoot(currentProject.getTreeNode(treePipelines));
		root = (DefaultMutableTreeNode)treePipelines.getModel().getRoot();
		
		if (pipeline != null){
			Enumeration children = root.children();
			int i = 0;
			while (children.hasMoreElements()){
				if (pipeline.equals(((DefaultMutableTreeNode)children.nextElement()).getUserObject()))
					treePipelines.expandRow(i);
				i++;
				}
			}
		treePipelines.updateUI();
		
	}
	
	public void showPanel(){
		
		updateProjectList();
		
	}
	
	public void addProject(CaminoProject project){
		projects.add(project);
		updateProjectList();
	}
	
	public void removeProject(CaminoProject project){
		if (projects.remove(project)){
			updateProjectList();
			if (currentProject == project)
				setCurrentProject(null);
			setPipelineTree();
			setSubjectList();
			}
	}
	
	void updateProjectList(){
		if (project_model == null){
			project_model = new DefaultListModel();
			lstProjects = new JList(project_model);
			lstProjects.setCellRenderer(new CaminoProjectListRenderer(this));
			scrProjects = new JScrollPane(lstProjects);
		}else{	
			project_model.removeAllElements();
			}
		
		for (int i = 0; i < projects.size(); i++)
			project_model.addElement(projects.get(i));
		
		if (currentProject != null)
			lstProjects.setSelectedValue(currentProject, true);
		
		lstProjects.updateUI();
	}
	
	void updateProcessList(){
		if (process_model == null){
			process_model = new DefaultListModel();
			CaminoProcessListRenderer renderer = new CaminoProcessListRenderer();
			lstEnvironmentProcesses = new JList(process_model);
			lstEnvironmentProcesses.setCellRenderer(renderer);
			scrEnvironmentProcesses = new JScrollPane(lstEnvironmentProcesses);
			
		}else{
			process_model.removeAllElements();
			}
		
		ArrayList<PipelineProcess> processes = new ArrayList<PipelineProcess>(CaminoEnvironment.getProcesses());
		Collections.sort(processes, new Comparator<PipelineProcess>(){
			public int compare(PipelineProcess p1, PipelineProcess p2){
				return p1.getName().compareTo(p2.getName());
				}
			});
		
		for (int i = 0; i < processes.size(); i++)
			process_model.addElement(processes.get(i));
		
		lstEnvironmentProcesses.updateUI();
	}
	
	protected void setCurrentProject(CaminoProject project){
		currentProject = project;
		//select in list
		
		if (currentProject != null)
			lstProjects.setSelectedValue(currentProject, true);
		
		setPipelineTree();
		setSubjectList();
		updateControls();
	}
	
	protected void setSubjectList(){
		
		if (subject_model == null){
			subject_model = new DefaultListModel();
			lstSubjects = new JList(subject_model);
			scrSubjects = new JScrollPane(lstSubjects);
			lstSubjects.setCellRenderer(new CaminoSubjectListRenderer(this));
		}else{	
			subject_model.removeAllElements();
			}
		
		if (currentProject == null) return;
		ArrayList<String> subjects = currentProject.subjects;
		
		for (int i = 0; i < subjects.size(); i++)
			subject_model.addElement(subjects.get(i));
		
		lstSubjects.updateUI();
	}
	
	void launchPipelineSubjects(){
		
		System.out.println("Launch pipeline subjects");
		ArrayList<String> subjects = null;
		ArrayList<InterfacePipeline> pipes = getSelectedPipelines();
		
		if (chkPipelineAllSubjects.isSelected())
			subjects = currentProject.subjects;
		else
			subjects = getSelectedSubjects();
		
		for (int i = 0; i < subjects.size(); i++){
			cmdPipelineReset.setText("Stop");
			String subject = subjects.get(i);
			
			System.out.println("Subject: " + subject);
			File root_dir = new File(currentProject.root_directory + File.separator + subject);
			
			if (chkPipelineParallel.isSelected()){
				execPipelinesParallel(pipes, subject, root_dir);
			}else{
				execPipelinesSerial(pipes, subject, root_dir);	
				}
			}
		
		cmdPipelineReset.setText("Reset");
		return;
	}
	
	ArrayList<String> getSelectedSubjects(){
		
		Object[] selected = lstSubjects.getSelectedValues();
		ArrayList<String> subjects = new ArrayList<String> ();
		
		for (int i = 0; i < selected.length; i++)
			subjects.add((String)selected[i]);
		
		return subjects;
		
	}
	
	void updateControls(){
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().startsWith("Project")){
			
			if (e.getActionCommand().endsWith("Edit")){
				if (currentProject == null){
					
					return;
					}
				
				CaminoProjectOptions options = new CaminoProjectOptions(currentProject);
				CaminoProjectDialogBox dialog = new CaminoProjectDialogBox(InterfaceSession.getSessionFrame(), options);
				dialog.setVisible(true);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("New")){
				CaminoProjectOptions options = new CaminoProjectOptions(null);
				CaminoProjectDialogBox dialog = new CaminoProjectDialogBox(InterfaceSession.getSessionFrame(), options);
				dialog.setVisible(true);
				
				if (options.project == null) return;
				
				addProject(options.project);
				//TODO: update list
				setCurrentProject(options.project);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				CaminoProject project = (CaminoProject)lstProjects.getSelectedValue();
				if (project == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No project selected!", "Camino Panel", JOptionPane.ERROR_MESSAGE);
					return;
					}
				if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
											  "Really remove project '" + project.name + "'", 
											  "Remove Camino Project", 
											  JOptionPane.YES_NO_OPTION, 
											  JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
					removeProject(project);
				return;
			}
			
			if (e.getActionCommand().endsWith("Select")){
				
				CaminoProject project = (CaminoProject)lstProjects.getSelectedValue();
				
				if (project == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No project selected!", "Camino Panel", JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				setCurrentProject(project);
				return;
				}
			
			if (e.getActionCommand().endsWith("Save")){
				if (currentProject == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No project selected!");
					return;
					}
				
				JFileChooser jc = new JFileChooser();
				jc.setFileFilter(new FileNameExtensionFilter("Camino projects (*.cproj)", "cproj"));
				jc.showSaveDialog(InterfaceSession.getSessionFrame());
				File file = jc.getSelectedFile();
				if (file == null) return;
				
				CaminoProjectWriter writer = new CaminoProjectWriter(file);
				if (!writer.writeProjectXML(currentProject))
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error writing '" + file.getAbsolutePath() + "'", 
												  "Save Camino Project", 
												  JOptionPane.ERROR_MESSAGE);
				else
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Wrote project to '" + file.getAbsolutePath() + "'", 
												  "Save Camino Project", 
												  JOptionPane.INFORMATION_MESSAGE);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Open")){
				
				JFileChooser jc = new JFileChooser();
				jc.setFileFilter(new FileNameExtensionFilter("Camino projects (*.cproj)", "cproj"));
				jc.showOpenDialog(InterfaceSession.getSessionFrame());
				File file = jc.getSelectedFile();
				if (file == null) return;
				
				CaminoProjectLoader loader = new CaminoProjectLoader(file);
				try{
					CaminoProject project = loader.loadCaminoProjectXML();
					if (project == null){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
								  "Error loading project '" + file.getAbsolutePath() + "'", 
								  "Load Camino Project", 
								  JOptionPane.ERROR_MESSAGE);
						return;
						}
					
					addProject(project);
					setCurrentProject(project);
					
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Loaded project '" + file.getAbsolutePath() + "'", 
							  "Load Camino Project", 
							  JOptionPane.INFORMATION_MESSAGE);
					
				}catch (IOException ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Error loading project '" + file.getAbsolutePath() + "'", 
							  "Load Camino Project", 
							  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				
				return;
				}
			
			}
		
		if (e.getActionCommand().equals("Pipeline New")){
			
			if (currentProject == null) return;
			
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
													  "Enter name for new pipeline", 
													  "New Camino Pipeline", 
													  JOptionPane.QUESTION_MESSAGE);
			
			if (name == null) return;
			currentProject.addPipeline(new InterfacePipeline(name));
			setPipelineTree();
			
			return;
			}
		
		if (e.getActionCommand().startsWith("Pipeline Process")){
			
			if (e.getActionCommand().endsWith("Add")){
				InterfacePipeline pipeline = getSelectedPipeline();
				if (pipeline == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No pipeline selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				PipelineProcessInstance instance = (PipelineProcessInstance)CaminoTaskDialogBox.showDialog();
				if (instance == null) return;
				
				pipeline.append(instance);
				setPipelineTree();
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				InterfacePipeline pipeline = getSelectedPipeline();
				
				if (pipeline == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No pipeline selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				PipelineTask task = getSelectedTask();
				if (task == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No task selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				pipeline.remove(task);
				setPipelineTree();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Edit")){
				InterfacePipeline pipeline = getSelectedPipeline();
				
				if (pipeline == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No pipeline selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				PipelineTask task = getSelectedTask();
				if (task == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No task selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				CaminoTaskDialogBox.showDialog(task);
				setPipelineTree();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Up")){
				InterfacePipeline pipeline = getSelectedPipeline();
				
				if (pipeline == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No pipeline selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				PipelineTask task = getSelectedTask();
				if (task == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No task selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				
				int index = pipeline.getTaskIndex(task);
				if (index <= 0) return;
				
				pipeline.remove(task);
				pipeline.insert(index - 1, task);
				
				setPipelineTree();
				setSelectedProcess(pipeline, task);
				//treePipelines.setSelectionRow(index);
				treePipelines.updateUI();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Down")){
				InterfacePipeline pipeline = getSelectedPipeline();
				
				if (pipeline == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No pipeline selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				PipelineTask task = getSelectedTask();
				if (task == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No task selected!", 
												  "Add Camino Process", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				
				int index = pipeline.getTaskIndex(task);
				if (index < 0 || index >= pipeline.getTasks().size() - 1) return;
				
				pipeline.remove(task);
				pipeline.insert(index + 1, task);
				
				setPipelineTree();
				setSelectedProcess(pipeline, task);
				//treePipelines.setSelectionRow(index + 2);
				treePipelines.updateUI();
				
				return;
				}
			
			}
		
		if (e.getActionCommand().startsWith("Pipeline Subjects")){
			
			if (e.getActionCommand().endsWith("All")){
				if (chkPipelineAllSubjects.isSelected()){
					chkPipelineNoSubjects.setSelected(false);
					chkPipelineSelSubjects.setSelected(false);
				}else{
					chkPipelineNoSubjects.setSelected(true);
					chkPipelineSelSubjects.setSelected(false);
					}
				return;
				}
			
			if (e.getActionCommand().endsWith("Sel")){
				if (chkPipelineSelSubjects.isSelected()){
					chkPipelineNoSubjects.setSelected(false);
					chkPipelineAllSubjects.setSelected(false);
				}else{
					chkPipelineNoSubjects.setSelected(true);
					chkPipelineAllSubjects.setSelected(false);
					}
				return;
				}
			
			if (e.getActionCommand().endsWith("None")){
				if (chkPipelineNoSubjects.isSelected()){
					chkPipelineAllSubjects.setSelected(false);
					chkPipelineSelSubjects.setSelected(false);
				}else{
					chkPipelineAllSubjects.setSelected(true);
					chkPipelineSelSubjects.setSelected(false);
					}
				return;
				}
			
			}
		
		if (e.getActionCommand().startsWith("Pipeline")){
		
			if (e.getActionCommand().endsWith("Serial")){
				chkPipelineParallel.setSelected(!chkPipelineSerial.isSelected());
				return;
				}
			
			if (e.getActionCommand().endsWith("Parallel")){
				chkPipelineSerial.setSelected(!chkPipelineParallel.isSelected());
				return;
				}
			
			
			if (e.getActionCommand().endsWith("Exec")){
				
				CaminoFunctions.fail_on_exception = chkPipelineFailOnException.isSelected();
				
				//execute pipeline, process by process
				if (chkPipelineAllSubjects.isSelected() ||
					chkPipelineSelSubjects.isSelected()){
						launchPipelineSubjects();
						return;
						}
				
				ArrayList<InterfacePipeline> pipes = getSelectedPipelines();
				File root_dir = currentProject.getRootDirectory();
				
				cmdPipelineReset.setText("Stop");
				
				if (chkPipelineParallel.isSelected()){
					execPipelinesParallel(pipes, null, root_dir);
				}else{
					execPipelinesSerial(pipes, null, root_dir);	
					}
				
				cmdPipelineReset.setText("Reset");
				return;
				}
			
			if (e.getActionCommand().endsWith("Reset")){
				
				if (cmdPipelineReset.getText().equals("Stop")){
					
					//stop process
					//CaminoPipeline pipeline = getSelectedPipeline();
					CaminoFunctions.stopExecution();
					cmdPipelineReset.setText("Stopping..");
					
					return;
					}
				
				if (cmdPipelineReset.getText().equals("Reset")){
				
					//CaminoPipeline pipeline = getSelectedPipeline();
					ArrayList<InterfacePipeline> pipes = getSelectedPipelines();
					
					if (pipes.size() == 0){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No pipelines selected!");
						return;
						}
					
					for (int i = 0; i < pipes.size(); i++)
						pipes.get(i).reset();
					
					return;
					}
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				InterfacePipeline pipeline = getSelectedPipeline();
				if (pipeline == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No pipeline selected!");
					return;
					}
				
				currentProject.removePipeline(pipeline);
				setPipelineTree();
				return;
				}
			
			if (e.getActionCommand().endsWith("Save")){
				InterfacePipeline pipeline = getSelectedPipeline();
				if (pipeline == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No pipeline selected!");
					return;
					}
				
				JFileChooser jc = new JFileChooser();
				jc.setFileFilter(new FileNameExtensionFilter("Camino pipelines (*.cpipe)", "cpipe"));
				jc.showSaveDialog(InterfaceSession.getSessionFrame());
				File file = jc.getSelectedFile();
				if (file == null) return;
				
				CaminoPipelineWriter writer = new CaminoPipelineWriter(file);
				if (!writer.writePipelineXML(pipeline))
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error writing '" + file.getAbsolutePath() + "'", 
												  "Save Camino Pipeline", 
												  JOptionPane.ERROR_MESSAGE);
				else
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Wrote pipeline to '" + file.getAbsolutePath() + "'", 
												  "Save Camino Pipeline", 
												  JOptionPane.INFORMATION_MESSAGE);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Load")){
				if (currentProject == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "No project selected!", 
							  "Load Camino Pipeline", 
							  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				JFileChooser jc = new JFileChooser();
				jc.setFileFilter(new FileNameExtensionFilter("Camino pipelines (*.cpipe)", "cpipe"));
				jc.showOpenDialog(InterfaceSession.getSessionFrame());
				File file = jc.getSelectedFile();
				if (file == null) return;
				
				CaminoPipelineLoader loader = new CaminoPipelineLoader(file);
				try{
					InterfacePipeline pipeline = loader.loadCaminoPipelineXML();
					if (pipeline == null){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
								  "Error loading pipeline '" + file.getAbsolutePath() + "'", 
								  "Load Camino Pipeline", 
								  JOptionPane.ERROR_MESSAGE);
						return;
						}
					
					currentProject.addPipeline(pipeline);
					setPipelineTree();
					
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Loaded pipeline '" + file.getAbsolutePath() + "'", 
							  "Load Camino Pipeline", 
							  JOptionPane.INFORMATION_MESSAGE);
					
				}catch (IOException ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Error loading pipeline '" + file.getAbsolutePath() + "'", 
							  "Load Camino Pipeline", 
							  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				}
			
			}
		
		if (e.getActionCommand().equals("Command Exec")){
			
			
			
			//launch process in worker thread
			CaminoProcess process = (CaminoProcess)cmbCommand.getSelectedItem();
			
			if (CaminoFunctions.launchCaminoProcess(process, txtCommandArgs.getText()))
				System.out.println(process.getSuccessMessage());
			else
				System.out.println(process.getFailureMessage());
			
			}
		
		if (e.getActionCommand().startsWith("Environment")){
			
			if (e.getActionCommand().endsWith("Update")){
				updateEnvironment();
				return;
				}
			
			if (e.getActionCommand().endsWith("Add Process")){
				
				CaminoProcess process = CaminoProcessDialogBox.showDialog();
				if (process == null) return;
				
				CaminoEnvironment.registerProcess(process);
				updateProcessList();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Edit Process")){
				
				CaminoProcessOptions options = new CaminoProcessOptions();
				CaminoProcess process = (CaminoProcess) lstEnvironmentProcesses.getSelectedValue();
				if (process == null) return;
				options.process = (CaminoProcess)process.clone();
				
				CaminoProcess new_process = CaminoProcessDialogBox.showDialog(options);
				if (new_process == null) return;
				
				CaminoEnvironment.deregisterProcess(process);
				CaminoEnvironment.registerProcess(new_process);
				updateProcessList();
				
				return;
				}
			
			
			}
		
	}
	
	boolean execPipelinesParallel(ArrayList<InterfacePipeline> pipes, String subject, File root_dir){
		
		//launches a new thread for each pipeline
		//and blocks until all pipelines are finished
		
		PipelinesThread plt = new PipelinesThread(pipes, subject, root_dir.getAbsolutePath());
		plt.start();
		
		try{
			plt.join();
		}catch (InterruptedException e){
			e.printStackTrace();
			}
		
		return plt.getSuccess();
	}
	
	boolean launchPipelineTask(final InterfacePipeline pipeline){
		
		try{
			return (Boolean)Worker.post(new foxtrot.Task(){
				public Boolean run(){
					try{
						boolean success = pipeline.launch();
						if (success)
							InterfaceSession.log(pipeline.getSuccessMessage());
						else
							InterfaceSession.log(pipeline.getFailureMessage());
						return success;
					}catch (PipelineException ex){
						System.out.println("Pipeline '" + pipeline.getName() + "' failed with exception:");
						ex.printStackTrace();
						return false;
						}
					}
				});
		}catch (Exception ex){
			System.out.println("Pipeline '" + pipeline.getName() + "' failed with exception:");
			ex.printStackTrace();
			return false;
			}
		
	}
	
	boolean execPipelinesSerial(ArrayList<InterfacePipeline> pipes, String subject, File root_dir){
		//exec serial
		for (int i = 0; i < pipes.size(); i++){
			InterfacePipeline pipeline = pipes.get(i);
			
			if (pipeline == null){
				//shouldn't happen
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Null pipeline encountered..!");
				return false;
				}
			
			//pipeline.setRootDirectory(currentProject.getRootDirectory());
			pipeline.setRootDirectory(root_dir);
			pipeline.reset();
			
			try{
				if (pipeline.launch(subject, root_dir.getAbsolutePath()))
					InterfaceSession.log(pipeline.getSuccessMessage());
				else
					InterfaceSession.log(pipeline.getFailureMessage());
				if (pipeline.getStatus() == Status.Failure) return false;
			}catch (PipelineException ex){
				//shouldn't fail here because we reset it
				System.out.println("Pipeline '" + pipeline.getName() + "' failed with exception:");
				ex.printStackTrace();
				return false;
				}
			}
		return true;
	}
	
	public String toString(){
		return "Camino Panel";
	}
	
	class PipelineRenderer extends DefaultTreeCellRenderer{
		
		public PipelineRenderer(){
			super();
			this.setBackgroundSelectionColor(new Color(200, 200, 255));
			this.setOpaque(true);
		}
		
		public Component getTreeCellRendererComponent(JTree tree,
												      Object value,
												      boolean sel,
												      boolean expanded,
												      boolean leaf,
												      int row,
												      boolean hasFocus){
		
			String suffix = "";
			String text = value.toString();
			
			if (value instanceof CaminoTaskTreeNode){
				PipelineTask task = ((CaminoTaskTreeNode)value).getTask();
				suffix = " [" + task.getStatusStr() + "]";
				this.setForeground(getTextColour(task.getStatus()));
				ImageIcon icon = (ImageIcon)task.getObjectIcon();
				icon.setImageObserver((CaminoTaskTreeNode)value);
				this.setIcon(icon);
			}else{
				this.setIcon(null);
				this.setForeground(tree.getForeground());
				}
			
			if (sel)
				this.setBackground(this.getBackgroundSelectionColor());
			else
				this.setBackground(this.getBackgroundNonSelectionColor());
			this.setFont(tree.getFont());
			
			this.setText(text + suffix);
			
			return this;
		}
		
		Color getTextColour(PipelineTask.Status status){
			switch (status){
				case NotStarted:
					return Color.black;
				case Processing:
					return Color.blue;
				case Failure:
					return Color.red;
				case Success:
					return new Color(0, 102, 0);
				}
			return Color.black;
		}
		
	}
	
	static class CaminoProcessListRenderer extends DefaultListCellRenderer {
		
		Icon icon;
		
		public CaminoProcessListRenderer(){
			super();
			icon = PipelineProcess.getIcon();
		}
		
		public Component getListCellRendererComponent(JList list,
											          Object value,
											          int index,
											          boolean isSelected,
											          boolean cellHasFocus){
			
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setIcon(icon);
			return this;
			
		}
		
	}
	
	static class CaminoProjectListRenderer extends DefaultListCellRenderer {

		//seriously why are these fields private?
		private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
		private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

		InterfaceCaminoPanel parent;
		
		public CaminoProjectListRenderer(InterfaceCaminoPanel parent){
			super();
			this.parent = parent;
		}
		
		public Component getListCellRendererComponent(JList list, Object value,
													  int index, boolean isSelected, boolean cellHasFocus) {
			
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			/*
			setComponentOrientation(list.getComponentOrientation());

	        Color bg = null;
	        Color fg = null;

	        JList.DropLocation dropLocation = list.getDropLocation();
	        if (dropLocation != null
	                && !dropLocation.isInsert()
	                && dropLocation.getIndex() == index) {

	            bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
	            fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");

	            isSelected = true;
	        }

		if (isSelected) {
	            setBackground(bg == null ? list.getSelectionBackground() : bg);
		    setForeground(fg == null ? list.getSelectionForeground() : fg);
		}
		else {
		    setBackground(list.getBackground());
		    setForeground(list.getForeground());
		}
	    
		setEnabled(list.isEnabled());
		setFont(list.getFont());
	        
	        Border border = null;
	        if (cellHasFocus) {
	            if (isSelected) {
	                border = DefaultLookup.getBorder(this, ui, "List.focusSelectedCellHighlightBorder");
	            }
	            if (border == null) {
	                border = DefaultLookup.getBorder(this, ui, "List.focusCellHighlightBorder");
	            }
	        } else {
	            border = getNoFocusBorder();
	        	}
	        
	        setBorder(border);
	        */
			
			CaminoProject project = (CaminoProject)value;
			
			setText(project.name);
			Icon icon = null;
			
			if (project != parent.currentProject)
				icon = project.getObjectIcon();
			else
				icon = project.getSelectedIcon();
			if (icon != null)
				setIcon(icon);
			
			return this;
		}
		
		/*
		private Border getNoFocusBorder() {
	        Border border = DefaultLookup.getBorder(this, ui, "List.cellNoFocusBorder");
	        if (System.getSecurityManager() != null) {
	            if (border != null) return border;
	            return SAFE_NO_FOCUS_BORDER;
	        } else {
	            if (border != null &&
	                    (noFocusBorder == null ||
	                    noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
	                return border;
	            }
	            return noFocusBorder;
	        }
	    }
	    */
		
	}
	
	static class CaminoSubjectListRenderer extends DefaultListCellRenderer {

		//seriously why are these fields private?
		private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
		private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

		InterfaceCaminoPanel parent;
		
		public CaminoSubjectListRenderer(InterfaceCaminoPanel parent){
			super();
			this.parent = parent;
			setIcon(getObjectIcon());
		}
		
		public Component getListCellRendererComponent(JList list, Object value,
													  int index, boolean isSelected, boolean cellHasFocus) {
			
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setIcon(getObjectIcon());
			String subject = (String)value;
			
			setText(subject);
			Icon icon = null;
			
			
			if (icon != null)
				setIcon(icon);
			
			return this;
		}
		
		ImageIcon getObjectIcon(){
			java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/camino/subject_17.png");
			if (imgURL != null)
				return new ImageIcon(imgURL);
			else
				System.out.println("Cannot find resource: /mgui/resources/icons/camino/subject_17.png");
			return null;
		}
		
		/*
		private Border getNoFocusBorder() {
	        Border border = DefaultLookup.getBorder(this, ui, "List.cellNoFocusBorder");
	        if (System.getSecurityManager() != null) {
	            if (border != null) return border;
	            return SAFE_NO_FOCUS_BORDER;
	        } else {
	            if (border != null &&
	                    (noFocusBorder == null ||
	                    noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
	                return border;
	            }
	            return noFocusBorder;
	        }
	    }
		*/
		
	}
	
	static class PipelinesThread extends Thread{
		
		ArrayList<PipelineThread> threads;
		boolean success = true;
		
		public PipelinesThread(ArrayList<InterfacePipeline> pipelines, String subject, String root){
			threads = new ArrayList<PipelineThread>(pipelines.size());
			for (int i = 0; i < pipelines.size(); i++)
				threads.add(new PipelineThread(pipelines.get(i), subject, root));
		}
		
		public void run() { 
			success = true;
			for (int i = 0; i < threads.size(); i++)
				threads.get(i).start();
			try{
				for (int i = 0; i < threads.size(); i++)
					threads.get(i).join();
			}catch (InterruptedException e){
				e.printStackTrace();
				success = false;
				}
		}
		
		public boolean getSuccess(){
			if (!success) return false;
			for (int i = 0; i < threads.size(); i++)
				if (!threads.get(i).success) return false;
			return true;
		}
		
		
	}
	
	static class PipelineThread extends Thread{
		
		InterfacePipeline pipeline;
		String subject, root;
		public boolean success = false;
		
		public PipelineThread(InterfacePipeline pipeline, String subject, String root){
			this.pipeline = pipeline;
			this.subject = subject;
			this.root = root;
		}
		
		public void run(){
			try{
				success = pipeline.launch(subject, root, true);
				if (success)
					System.out.println(pipeline.getSuccessMessage());
				else
					System.out.println(pipeline.getFailureMessage());
			}catch (PipelineException e){
				e.printStackTrace();
				}
		}
		
	}
	
}