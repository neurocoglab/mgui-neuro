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

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.pipelines.PipelineProcess;
import mgui.pipelines.TaskParameter;


public class CaminoProcessDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblName = new JLabel("Name:");
	JTextField txtName = new JTextField();
	JLabel lblMainClass = new JLabel("Main class:");
	JTextField txtMainClass = new JTextField();
	JCheckBox chkLogger = new JCheckBox(" Logger:");
	JTextField txtLogger = new JTextField();
	JLabel lblParameters = new JLabel("Parameters:");
	
	JScrollPane scrParameters;
	JTable tblParameters;
	ParameterTableModel parameter_model;
	
	JButton cmdAddParameter = new JButton("Add");
	JButton cmdRemoveParameter = new JButton("Remove");
	
	CaminoProcess current_process;
	
	public CaminoProcessDialogBox(){
		super();
	}

	public CaminoProcessDialogBox(InterfaceOptions options){
		super(InterfaceSession.getSessionFrame(), options);
		init();
	}
	
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		this.setTitle("Camino process settings");
		
		CaminoProcessOptions _options = (CaminoProcessOptions)options;
		current_process = _options.process;
		
		if (current_process == null)
			current_process = new CaminoProcess("", "");
		
		//set up lists
		updateDialog();
		
		LineLayout layout = new LineLayout(20, 5, 0);
		mainPanel.setLayout(layout);
		setDialogSize(600, 450);
		
		cmdAddParameter.addActionListener(this);
		cmdAddParameter.setActionCommand("Add Parameter");
		cmdRemoveParameter.addActionListener(this);
		cmdRemoveParameter.setActionCommand("Remove Parameter");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(1, 1, 0.35, 0.6, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		mainPanel.add(lblMainClass, c);
		c = new LineLayoutConstraints(2, 2, 0.35, 0.6, 1);
		mainPanel.add(txtMainClass, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		mainPanel.add(chkLogger, c);
		c = new LineLayoutConstraints(3, 3, 0.35, 0.6, 1);
		mainPanel.add(txtLogger, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.9, 1);
		mainPanel.add(lblParameters, c);
		c = new LineLayoutConstraints(5, 10, 0.05, 0.9, 1);
		mainPanel.add(scrParameters, c);
		c = new LineLayoutConstraints(11, 11, 0.05, 0.44, 1);
		mainPanel.add(cmdAddParameter, c);
		c = new LineLayoutConstraints(11, 11, 0.51, 0.44, 1);
		mainPanel.add(cmdRemoveParameter, c);
		
	}
	
	public static CaminoProcess showDialog(CaminoProcessOptions options){
		CaminoProcessDialogBox dialog = new CaminoProcessDialogBox(options);
		
		dialog.setVisible(true);
		return options.process;
	}
	
	public static CaminoProcess showDialog(){
		return showDialog(new CaminoProcessOptions());
	}
	
	public boolean updateDialog(){
		
		if (current_process == null) return false;
		
		txtName.setText(current_process.getName());
		txtMainClass.setText(current_process.getMainClass());
		if (current_process.getLogger() == null){
			chkLogger.setSelected(false);
		}else{
			chkLogger.setSelected(true);
			txtLogger.setText(current_process.getLogger());
			}
		
		if (tblParameters == null){
			parameter_model = new ParameterTableModel(current_process);
			tblParameters = new JTable(parameter_model);
			scrParameters = new JScrollPane(tblParameters);
		}else{
			parameter_model.setCurrentProcess(current_process);
			}
		
		return true;
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Add Parameter")){
			
			parameter_model.process.addParameter(new TaskParameter());
			parameter_model.fireTableDataChanged();
			
			return;
			}
		
		if (e.getActionCommand().equals("Remove Parameter")){
			
			int row = tblParameters.getSelectedRow();
			if (row < 0) return;
			
			String name = (String)parameter_model.getValueAt(row, 0);
			parameter_model.process.removeParameter(name);
			parameter_model.fireTableDataChanged();
			
			return;
			}
		
		if (e.getActionCommand().equals(this.DLG_CMD_OK)){
			current_process.setName(txtName.getText());
			current_process.setMainClass(txtMainClass.getText());
			if (chkLogger.isSelected())
				current_process.setLogger(txtLogger.getText());
			else
				current_process.setLogger(null);
			CaminoProcessOptions _options = (CaminoProcessOptions)options;
			_options.process = current_process;
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_CANCEL)){
			CaminoProcessOptions _options = (CaminoProcessOptions)options;
			_options.process = null;
			this.setVisible(false);
			}

	}
	
	
	static class ParameterTableModel extends DefaultTableModel {
		
		PipelineProcess process;
		
		public ParameterTableModel(PipelineProcess process){
			this.process = process;
		}
		
		public void setCurrentProcess(PipelineProcess instance){
			process = instance;
			this.fireTableDataChanged();
		}
		
		public int getRowCount() {
			if (process == null) return 0;
			return process.getParameters().size();
		}
		
		public int getColumnCount() {
			return 5;
		}
		 
		public Object getValueAt(int row, int column) {
			if (process == null) return 0;
			ArrayList<TaskParameter> parameters = process.getParameters();
			
			switch (column){
				case 0:
					return parameters.get(row).name;
				case 1:
					return parameters.get(row).optional;
				case 2:
					return parameters.get(row).use_name;
				case 3:
					return parameters.get(row).has_value;
				case 4:
					return parameters.get(row).default_value;
				}
			 
			return null;
		}
		
		 public boolean isCellEditable(int row, int column) {
			 return true;
		 }
		
		public void setValueAt(Object value, int row, int column) {
			if (process == null) return;
			ArrayList<TaskParameter> parameters = process.getParameters();
			switch (column){
				case 0:
					parameters.get(row).name = (String)value;
					break;
				case 1:
					parameters.get(row).optional = (Boolean)value;
					break;
				case 2:
					parameters.get(row).use_name = (Boolean)value;
					break;
				case 3:
					parameters.get(row).has_value = (Boolean)value;
					break;
				case 4:
					parameters.get(row).default_value = (String)value;
					break;
				}
			
		}
		
		public Class<?> getColumnClass(int column){
			
			switch (column){
				case 0:
					return String.class;
				case 1:
					return Boolean.class;
				case 2:
					return Boolean.class;
				case 3:
					return Boolean.class;
				case 4:
					return String.class;
				}
			
			return Object.class;
		}
		
		public String getColumnName(int column) {
			switch (column){
				case 0:
					return "Name";
				case 1:
					return "Optional";
				case 2:
					return "Use name";
				case 3:
					return "Has value";
				case 4:
					return "Default value";
				}
			return "?";
		}
	}
	
}