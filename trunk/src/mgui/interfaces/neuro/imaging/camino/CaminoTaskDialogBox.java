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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.neuro.imaging.camino.CaminoEnvironment;
import mgui.pipelines.PipelineProcess;
import mgui.pipelines.PipelineProcessInstance;
import mgui.pipelines.PipelineTask;
import mgui.pipelines.TaskParameter;
import mgui.pipelines.TaskParameterInstance;


public class CaminoTaskDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblProcess = new JLabel("Process:");
	JComboBox cmbProcess = new JComboBox();
	JButton cmdDefineProcesses = new JButton("Define processes..");
	JCheckBox chkInputNone = new JCheckBox(" No input");
	JCheckBox chkInputPrevious = new JCheckBox(" Input from previous task");
	JCheckBox chkInputFile = new JCheckBox(" Input from file");
	JTextField txtInputFile = new JTextField();
	JButton cmdInputFile = new JButton("Browse..");
	JCheckBox chkInputPrepend = new JCheckBox(" Prepend prefix and subject name");
	
	JCheckBox chkOutputFile = new JCheckBox(" Output to file");
	JTextField txtOutputFile = new JTextField();
	JButton cmdOutputFile = new JButton("Browse..");
	JCheckBox chkOutputPrepend = new JCheckBox(" Prepend prefix and subject name");
	
	JLabel lblParameters = new JLabel("Parameters:");
	JTable tblParameters;
	JScrollPane scrParameters;
	ParameterTableModel parameter_model;
	
	PipelineProcessInstance current_instance;
	
	boolean update_combo = true;
	
	public CaminoTaskDialogBox(){
		super();
	}

	public CaminoTaskDialogBox(JFrame aFrame, InterfaceOptions options){
		super(aFrame, options);
		init();
	}
	
	public static PipelineTask showDialog(){
		return showDialog(null);
	}
	
	public static PipelineTask showDialog(PipelineTask task){
		CaminoTaskOptions options = new CaminoTaskOptions();
		options.task = task;
		//options.displayPanel = panel;
		CaminoTaskDialogBox dialog = new CaminoTaskDialogBox(InterfaceSession.getSessionFrame(), options);
		
		dialog.setVisible(true);
		return options.task;
	}
	
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		this.setTitle("Camino Task Settings");
		
		LineLayout layout = new LineLayout(20, 5, 0);
		mainPanel.setLayout(layout);
				
		setDialogSize(700, 540);
		
		CaminoTaskOptions _options = (CaminoTaskOptions)options;
		if (_options.task instanceof PipelineProcessInstance)
			current_instance = (PipelineProcessInstance) _options.task;
		
		//set up lists
		updateDialog();
		
		chkInputPrevious.addActionListener(this);
		chkInputPrevious.setActionCommand("Input previous check");
		chkInputNone.addActionListener(this);
		chkInputNone.setActionCommand("Input none");
		chkInputFile.addActionListener(this);
		chkInputFile.setActionCommand("Input file check");
		cmdInputFile.addActionListener(this);
		cmdInputFile.setActionCommand("Input file browse");
		cmdDefineProcesses.addActionListener(this);
		cmdDefineProcesses.setActionCommand("Define processes");
		chkOutputFile.addActionListener(this);
		chkOutputFile.setActionCommand("Output file check");
		cmdOutputFile.addActionListener(this);
		cmdOutputFile.setActionCommand("Output file browse");
		cmbProcess.addActionListener(this);
		cmbProcess.setActionCommand("Process changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.15, 1);
		mainPanel.add(lblProcess, c);
		c = new LineLayoutConstraints(1, 1, 0.2, 0.55, 1);
		mainPanel.add(cmbProcess, c);
		c = new LineLayoutConstraints(1, 1, 0.75, 0.2, 1);
		mainPanel.add(cmdDefineProcesses, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(chkInputNone, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.9, 1);
		mainPanel.add(chkInputPrevious, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.9, 1);
		mainPanel.add(chkInputFile, c);
		c = new LineLayoutConstraints(5, 5, 0.1, 0.65, 1);
		mainPanel.add(txtInputFile, c);
		c = new LineLayoutConstraints(5, 5, 0.75, 0.2, 1);
		mainPanel.add(cmdInputFile, c);
		c = new LineLayoutConstraints(6, 6, 0.1, 0.8, 1);
		mainPanel.add(chkInputPrepend, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.9, 1);
		mainPanel.add(chkOutputFile, c);
		c = new LineLayoutConstraints(8, 8, 0.1, 0.65, 1);
		mainPanel.add(txtOutputFile, c);
		c = new LineLayoutConstraints(8, 8, 0.75, 0.2, 1);
		mainPanel.add(cmdOutputFile, c);
		c = new LineLayoutConstraints(9, 9, 0.1, 0.8, 1);
		mainPanel.add(chkOutputPrepend, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 0.9, 1);
		mainPanel.add(lblParameters, c);
		c = new LineLayoutConstraints(11, 16, 0.05, 0.9, 1);
		mainPanel.add(scrParameters, c);
		
	}
	
	protected void updateProcessCombo(){
		update_combo = false;
		cmbProcess.removeAllItems();
		ArrayList<PipelineProcess> processes = new ArrayList<PipelineProcess>(CaminoEnvironment.getProcesses());
		Collections.sort(processes, new Comparator<PipelineProcess>(){
				public int compare(PipelineProcess p1, PipelineProcess p2){
					return p1.getName().compareTo(p2.getName());
				}
			});
		
		for (int i = 0; i < processes.size(); i++)
			cmbProcess.addItem(processes.get(i));
		
		if (current_instance != null)
			cmbProcess.setSelectedItem(current_instance.getProcess());
		if (cmbProcess.getSelectedItem() != null)
			updateCurrentInstance();
		update_combo = true;
	}
	
	public boolean updateDialog(){
		
		updateProcessCombo();
		
		if (current_instance == null) return false;
		
		if (tblParameters == null){
			parameter_model = new ParameterTableModel(current_instance);
			tblParameters = new JTable(parameter_model);
			scrParameters = new JScrollPane(tblParameters);
		}else{
			parameter_model.setCurrentInstance(current_instance);
			}
		
		updateControls();
		
		return true;
	}
	
	void updateControls(){
		
		txtInputFile.setEnabled(chkInputFile.isSelected());
		cmdInputFile.setEnabled(chkInputFile.isSelected());
		txtOutputFile.setEnabled(chkOutputFile.isSelected());
		cmdOutputFile.setEnabled(chkOutputFile.isSelected());
		
		
	}
	
	void updateCurrentInstance(){
		
		if (current_instance == null){
		
			PipelineProcess process = getProcess();
			if (process == null) return;
			
			current_instance = process.getInstance(0);
			}
		
		if (!current_instance.hasInput()){
			chkInputNone.setSelected(true);
			chkInputPrevious.setSelected(false);
			chkInputFile.setSelected(false);
		}else{
			chkInputNone.setSelected(false);
			chkInputPrevious.setSelected(current_instance.getInputFile() == null);
			chkInputFile.setSelected(!chkInputPrevious.isSelected());
			}
		if (chkInputFile.isSelected())
			txtInputFile.setText(current_instance.getInputFile());
		chkOutputFile.setSelected(current_instance.getOutputFile() != null);
		if (chkOutputFile.isSelected())
			txtOutputFile.setText(current_instance.getOutputFile());
		chkInputPrepend.setSelected(current_instance.isPrependSubjectInput());
		chkOutputPrepend.setSelected(current_instance.isPrependSubjectOutput());
		
	}
	
	protected PipelineProcess getProcess(){
		
		return (PipelineProcess)cmbProcess.getSelectedItem();
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(this.DLG_CMD_OK)){
			
			if (chkInputNone.isSelected()){
				current_instance.setHasInput(false);
				current_instance.setInputFile(null);
			}else{
				current_instance.setHasInput(true);
				if (chkInputFile.isSelected())
					current_instance.setInputFile(txtInputFile.getText());
				else
					current_instance.setInputFile(null);
				}
			if (chkOutputFile.isSelected())
				current_instance.setOutputFile(txtOutputFile.getText());
			else
				current_instance.setOutputFile(null);
			current_instance.setPrependSubjectInput(chkInputPrepend.isSelected());
			current_instance.setPrependSubjectOutput(chkOutputPrepend.isSelected());
			CaminoTaskOptions _options = (CaminoTaskOptions)options;
			_options.task = current_instance;
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Input previous check")){
			chkInputFile.setSelected(!chkInputPrevious.isSelected());
			chkInputNone.setSelected(false);
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Input file check")){
			chkInputPrevious.setSelected(!chkInputFile.isSelected());
			chkInputNone.setSelected(false);
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Input none")){
			chkInputPrevious.setSelected(!chkInputNone.isSelected());
			chkInputFile.setSelected(false);
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Output file check")){
			
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Input file browse")){
			
			JFileChooser jc = null;
			
			if (current_instance.getInputFile() != null)
				jc = new JFileChooser(current_instance.getInputFile());
			else
				jc = new JFileChooser();
			
			jc.setMultiSelectionEnabled(false);
			CaminoTaskOptions _options = (CaminoTaskOptions)options;
			jc.showDialog(InterfaceSession.getSessionFrame(), "Select");
			
			if (jc.getSelectedFile() == null) return;
			
			//current_instance.input_file = jc.getSelectedFile().getAbsolutePath();
			txtInputFile.setText(jc.getSelectedFile().getAbsolutePath());
			
			return;
			}
		
		if (e.getActionCommand().equals("Output file browse")){
			
			JFileChooser jc = null;
			
			if (current_instance.getOutputFile() != null)
				jc = new JFileChooser(current_instance.getOutputFile());
			else
				jc = new JFileChooser();
			
			jc.setMultiSelectionEnabled(false);
			CaminoTaskOptions _options = (CaminoTaskOptions)options;
			jc.showSaveDialog(InterfaceSession.getSessionFrame());
			
			if (jc.getSelectedFile() == null) return;
			
			//current_instance.output_file = jc.getSelectedFile().getAbsolutePath();
			txtOutputFile.setText(jc.getSelectedFile().getAbsolutePath());
			
			return;
			}
		
		if (e.getActionCommand().equals("Process changed")){
			if (!update_combo) return;
			
			current_instance = new PipelineProcessInstance(getProcess(), 0);
			
			updateCurrentInstance();
			updateDialog();
			return;
			}
		
		super.actionPerformed(e);
	}
	
	static class ParameterTableModel extends DefaultTableModel {
		
		PipelineProcessInstance process;
		
		public ParameterTableModel(PipelineProcessInstance process){
			this.process = process;
		}
		
		public void setCurrentInstance(PipelineProcessInstance instance){
			process = instance;
			this.fireTableDataChanged();
		}
		
		public int getRowCount() {
			if (process == null) return 0;
			return process.getParameters().size();
		}
		
		public int getColumnCount() {
			return 3;
		}
		 
		public Object getValueAt(int row, int column) {
			if (process == null) return 0;
			ArrayList<TaskParameterInstance> parameters = new ArrayList<TaskParameterInstance>(process.getParameters().values());
			switch (column){
				case 0:
					return parameters.get(row).apply;
				case 1:
					return parameters.get(row).name;
				case 2:
					return parameters.get(row).value;
				}
			 
			return null;
		}
		
		 public boolean isCellEditable(int row, int column) {
			 if (column == 1) return false;
			 if (column == 0){
				TaskParameter p = process.getProcess().getParameter((String)getValueAt(row, 1));
				if (p == null) return false;
				if (!p.optional) return false;
			 	}
			 if (column == 2){
				TaskParameter p = process.getProcess().getParameter((String)getValueAt(row, 1));
				return p.has_value; 
			 	}
			 return true;
		 }
		
		public void setValueAt(Object value, int row, int column) {
			if (process == null) return;
			ArrayList<TaskParameterInstance> parameters = new ArrayList<TaskParameterInstance>(process.getParameters().values());
			switch (column){
				case 0:
					parameters.get(row).apply = (Boolean)value;
					break;
				case 2:
					parameters.get(row).value = (String)value;
					break;
				}
			
		}
		
		public Class<?> getColumnClass(int column){
			
			switch (column){
				case 0:
					return Boolean.class;
				case 1:
					return String.class;
				case 2:
					return String.class;
				}
			
			return Object.class;
		}
		
		public String getColumnName(int column) {
			switch (column){
				case 0:
					return "Apply";
				case 1:
					return "Name";
				case 2:
					return "Value";
				}
			return "?";
		}
		
		
	}
}