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


package mgui.interfaces.neuro.imaging.camino;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsTabbedDialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.neuro.imaging.camino.CaminoProject;


public class CaminoProjectDialogBox extends InterfaceOptionsTabbedDialogBox {

	//Main tab
	JLabel lblMainName = new JLabel("Name:");
	JTextField txtMainName = new JTextField("Project 1");
	JLabel lblMainRootFolder = new JLabel("Root folder:");
	JTextField txtMainRootFolder = new JTextField();
	JButton cmdMainRootFolder = new JButton("Browse..");
	JLabel lblSubjectPrefix = new JLabel("Prefix:");
	JTextField txtSubjectPrefix = new JTextField("");
	JButton cmdMainApply = new JButton("Apply");
	JButton cmdMainRevert = new JButton("Revert");
	
	//Subjects tab
	JLabel lblSubSubjects = new JLabel("Subjects in root folder:");
	JList lstSubSubjects;
	JScrollPane scrSubSubjects;
	JLabel lblSubSubfolders = new JLabel("Subfolders:");
	JList lstSubSubfolders;
	JScrollPane scrSubSubfolders;
	JButton cmdSubAddSubfolder = new JButton("Add");
	JButton cmdSubRemSubfolder = new JButton("Remove");
	JButton cmdSubAddSubject = new JButton("Add subject");
	JButton cmdSubAddSubjectList = new JButton("Add list");
	JCheckBox chkSubRemFileSystem = new JCheckBox(" Apply removals to file system");
	JButton cmdSubRemSubject = new JButton("Remove selected");
	JButton cmdSubApply = new JButton("Apply");
	JButton cmdSubRevert = new JButton("Revert");
	
	//Processes tab
	
	DefaultListModel subject_model;
	DefaultListModel subfolder_model;
	
	public CaminoProjectDialogBox(){
		super();
	}

	public CaminoProjectDialogBox(JFrame aFrame, InterfaceOptions options){
		super(aFrame, options);
		init();
	}
	
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		this.setTitle("Camino project settings");
		
		cmdMainRootFolder.addActionListener(this);
		cmdMainRootFolder.setActionCommand("Main Set Root");
		cmdMainApply.addActionListener(this);
		cmdMainApply.setActionCommand("Main Apply");
		cmdSubAddSubject.addActionListener(this);
		cmdSubAddSubject.setActionCommand("Subjects Add Subject");
		cmdSubRemSubject.addActionListener(this);
		cmdSubRemSubject.setActionCommand("Subjects Remove Subject");
		cmdSubAddSubfolder.addActionListener(this);
		cmdSubAddSubfolder.setActionCommand("Subjects Add Subfolder");
		cmdSubRemSubfolder.addActionListener(this);
		cmdSubRemSubfolder.setActionCommand("Subjects Remove Subfolder");
		
		cmdSubRevert.addActionListener(this);
		cmdSubRevert.setActionCommand("Subjects Revert");
		cmdSubApply.addActionListener(this);
		cmdSubApply.setActionCommand("Subjects Apply");
		
		//set up lists
		updateDialog();
		
		LineLayout layout = new LineLayout(20, 5, 0);
		setDialogSize(700, 600);
		
		JPanel panel = new JPanel();
		panel.setLayout(layout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.15, 1);
		panel.add(lblMainName, c);
		c = new LineLayoutConstraints(1, 1, 0.2, 0.75, 1);
		panel.add(txtMainName, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.15, 1);
		panel.add(lblMainRootFolder, c);
		c = new LineLayoutConstraints(2, 2, 0.2, 0.55, 1);
		panel.add(txtMainRootFolder, c);
		c = new LineLayoutConstraints(2, 2, 0.75, 0.2, 1);
		panel.add(cmdMainRootFolder, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.15, 1);
		panel.add(lblSubjectPrefix, c);
		c = new LineLayoutConstraints(3, 3, 0.2, 0.75, 1);
		panel.add(txtSubjectPrefix, c);
		
		c = new LineLayoutConstraints(5, 6, 0.45, 0.24, 1);
		panel.add(cmdMainApply, c);
		c = new LineLayoutConstraints(5, 6, 0.71, 0.24, 1);
		panel.add(cmdMainRevert, c);
		
		addTab("Main", panel);
		
		panel = new JPanel();
		panel.setLayout(layout);
		
		c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		panel.add(lblSubSubjects, c);
		c = new LineLayoutConstraints(2, 6, 0.05, 0.7, 1);
		panel.add(scrSubSubjects, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.7, 1);
		panel.add(lblSubSubfolders, c);
		c = new LineLayoutConstraints(8, 12, 0.05, 0.7, 1);
		panel.add(scrSubSubfolders, c);
		c = new LineLayoutConstraints(8, 8, 0.75, 0.2, 1);
		panel.add(cmdSubAddSubfolder, c);
		c = new LineLayoutConstraints(9, 9, 0.75, 0.2, 1);
		panel.add(cmdSubRemSubfolder, c);
		c = new LineLayoutConstraints(2, 2, 0.75, 0.2, 1);
		panel.add(cmdSubAddSubject, c);
		c = new LineLayoutConstraints(3, 3, 0.75, 0.2, 1);
		panel.add(cmdSubAddSubjectList, c);
		c = new LineLayoutConstraints(4, 4, 0.75, 0.2, 1);
		panel.add(cmdSubRemSubject, c);
		c = new LineLayoutConstraints(13, 13, 0.05, 0.9, 1);
		panel.add(chkSubRemFileSystem, c);
		c = new LineLayoutConstraints(15, 16, 0.45, 0.24, 1);
		panel.add(cmdSubApply, c);
		c = new LineLayoutConstraints(15, 16, 0.71, 0.24, 1);
		panel.add(cmdSubRevert, c);
		
		addTab("Subjects", panel);
		
	}
	
	public boolean updateDialog(){
		
		CaminoProjectOptions _options = (CaminoProjectOptions)options;
		
		if (_options.project != null){
			txtMainName.setText(_options.project.name);
			txtMainRootFolder.setText(_options.project.root_directory.getAbsolutePath());
			txtSubjectPrefix.setText(_options.project.subject_prefix);
			_options.project.setSubjects();
			_options.project.setSubdirs();
			}
		
		updateSubjects();
		updateSubfolders();
		
		return true;
	}
	
	void updateSubjects(){
		
		if (subject_model == null){
			subject_model = new DefaultListModel();
			lstSubSubjects = new JList(subject_model);
			scrSubSubjects = new JScrollPane(lstSubSubjects);
		}else{	
			subject_model.removeAllElements();
			}
		
		CaminoProjectOptions _options = (CaminoProjectOptions)options;
		
		if (_options.project != null){
			
			for (int i = 0; i < _options.project.subjects.size(); i++)
				subject_model.addElement(_options.project.subjects.get(i));
			
			/*
			File dir = _options.project.root_directory;
			String[] subjects = dir.list(getDirFilter());
			ArrayList<String> list = new ArrayList<String>();
			
			for (int i = 0; i < subjects.length; i++){
				subject_model.addElement(subjects[i]);
				list.add(subjects[i]);
				}
			
			_options.subjects = list;
			*/
			
			}
		
		lstSubSubjects.updateUI();
		
	}
	
	void updateSubfolders(){
		
		if (subfolder_model == null){
			subfolder_model = new DefaultListModel();
			lstSubSubfolders = new JList(subfolder_model);
			scrSubSubfolders = new JScrollPane(lstSubSubfolders);
		}else{	
			subfolder_model.removeAllElements();
			}
		
		CaminoProjectOptions _options = (CaminoProjectOptions)options;
		
		if (_options.project != null){
			
			for (int i = 0; i < _options.project.subdirs.size(); i++)
				subfolder_model.addElement(_options.project.subdirs.get(i));
			
			/*
			TreeSet<String> set = new TreeSet<String>();
			
			File dir = _options.project.root_directory;
			String[] subjects = dir.list(getDirFilter());
			String sep = System.getProperty("file.separator");
			
			for (int i = 0; i < subjects.length; i++){
				File subdir = new File(dir.getAbsolutePath() + sep + subjects[i]);
				String[] subsubs = subdir.list(getDirFilter());
				for (int j = 0; j < subsubs.length; j++)
					set.add(subsubs[j]);
				}
			
			ArrayList<String> dirs = new ArrayList<String>(set);
			for (int i = 0; i < dirs.size(); i++)
				subfolder_model.addElement(dirs.get(i));
			
			_options.subfolders = dirs;
			*/
			
			}
		
		lstSubSubfolders.updateUI();
		
	}
	
	public void actionPerformed(ActionEvent e){
	
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().startsWith("Main")){
			
			if (e.getActionCommand().endsWith("Set Root")){
			
				JFileChooser fc = null;
				if (txtMainRootFolder.getText().length() > 0)
					fc = new JFileChooser(txtMainRootFolder.getText());
				else
					fc = new JFileChooser();
				
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setFileFilter(getDirFilter2());
				fc.setMultiSelectionEnabled(false);
				CaminoProjectOptions _options = (CaminoProjectOptions)options;
				if (fc.showDialog(_options.display_panel, "Accept") != JFileChooser.APPROVE_OPTION) return;
				
				txtMainRootFolder.setText(fc.getSelectedFile().getAbsolutePath());
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				
				CaminoProjectOptions _options = (CaminoProjectOptions)options;
				
				if (txtMainName.getText().length() == 0){
					JOptionPane.showMessageDialog(_options.display_panel, 
												  "No name set!", 
												  "Camino dialog error", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				if (txtMainRootFolder.getText().length() == 0){
					JOptionPane.showMessageDialog(_options.display_panel, 
												  "No root directory set!", 
												  "Camino dialog error", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				File root = new File(txtMainRootFolder.getText());
				
				if (!root.exists() && !root.mkdir()){
					JOptionPane.showMessageDialog(_options.display_panel, 
												  "Invalid root directory!", 
												  "Camino dialog error", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				if (_options.project == null)
					_options.project = new CaminoProject(txtMainName.getText(), root);
				else{
					_options.project.root_directory = root;
					_options.project.name = txtMainName.getText();
					_options.project.setSubdirs();
					}
				
				_options.project.subject_prefix = txtSubjectPrefix.getText();
				
				updateDialog();
				
				return;
				}
				
			}
		
		if (e.getActionCommand().startsWith("Subjects")){
			
			if (e.getActionCommand().endsWith("Add Subject")){
				String subject = JOptionPane.showInputDialog("Enter name for new subject");
				if (subject == null) return;
				
				subject_model.addElement(subject);
				scrSubSubjects.updateUI();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove Subject")){
				
				int[] selected = lstSubSubjects.getSelectedIndices();
				
				for (int i = selected.length - 1; i >= 0; i--)
					//because removeElement with non-generic Vector is useless
					subject_model.remove(selected[i]);
					
				lstSubSubjects.updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Add Subfolder")){
				
				String name = JOptionPane.showInputDialog("Enter name for new subfolder");
				if (name == null) return;
				
				subfolder_model.addElement(name);
				lstSubSubfolders.updateUI();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove Subfolder")){
				
				int[] selected = lstSubSubfolders.getSelectedIndices();
				
				for (int i = selected.length - 1; i >= 0; i--)
					subfolder_model.remove(selected[i]);
					
				lstSubSubfolders.updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Revert")){
				updateDialog();
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				//apply changes to file system
				CaminoProjectOptions _options = (CaminoProjectOptions)options;
				_options.subjects = new ArrayList<String>();
				_options.subfolders = new ArrayList<String>();
				
				for (int i = 0; i < subject_model.size(); i++)
					_options.subjects.add((String)subject_model.get(i));
				for (int i = 0; i < subfolder_model.size(); i++)
					_options.subfolders.add((String)subfolder_model.get(i));
				
				_options.project.subjects = _options.subjects;
				_options.project.subdirs = _options.subfolders;
				
				if (_options.project.updateFileSystem(chkSubRemFileSystem.isSelected()))
					JOptionPane.showMessageDialog(_options.display_panel, 
												  "File system updated", 
												  "Update Camino Project File System", 
												  JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane.showMessageDialog(_options.display_panel, 
												  "Problems encountered in file system update..", 
												  "Update Camino Project File System", 
												  JOptionPane.ERROR_MESSAGE);
				
				updateDialog();
				
				return;
				}
			
			
			}
		
		
		
		super.actionPerformed(e);
	}
	
	static javax.swing.filechooser.FileFilter getDirFilter2(){
		return new javax.swing.filechooser.FileFilter() {
	        public boolean accept(File dir) {
	            return dir.isDirectory();
	        }
	        public String getDescription(){
	        	return "Directory";
	        }
		};
	}
	
	static FilenameFilter getDirFilter(){
		return new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return new File(dir, name).isDirectory();
	        }
		};
	}
	
}