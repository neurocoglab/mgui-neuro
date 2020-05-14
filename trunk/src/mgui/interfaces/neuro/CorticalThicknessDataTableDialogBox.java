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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataTable;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;


public class CorticalThicknessDataTableDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblDataTable = new JLabel("Select data table and subject ID field:");
	JTree treeSources;
	JPanel treeSourcePanel = new JPanel();
	JScrollPane treeSourceScrollPane = new JScrollPane();
	DefaultMutableTreeNode rootNode;
	
	String selected_field;
	String selected_table;
	String selected_source;
	
	public CorticalThicknessDataTableDialogBox(){
		super();
	}
	
	public CorticalThicknessDataTableDialogBox(JFrame f, CorticalThicknessDataTableOptions opts){
		super(f, opts);
		init();
	}
	
	public void setOptions(CorticalThicknessDataTableOptions opts){
		this.options = opts;
	}
	
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		cmdOK.setEnabled(false);
		
		this.setTitle("Define subject data table");
		
		//sources tree
		initTree();
		
		setMainLayout(new LineLayout(20, 5, 0));
		setDialogSize(500, 400);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		mainPanel.add(lblDataTable, c);
		c = new LineLayoutConstraints(2, 12, 0.05, 0.9, 1);
		mainPanel.add(treeSourceScrollPane, c);
	
		this.setLocationRelativeTo(getParent());
	}
	
	void initTree(){
		
		rootNode = getDataTablesNode();
		treeSources = new JTree(new DefaultTreeModel(rootNode));
		treeSourceScrollPane = new JScrollPane(treeSources);
		
		treeSources.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		                           treeSources.getLastSelectedPathComponent();

			    /* if nothing is selected */ 
			        if (node == null) return;
	
			    /* retrieve the node that was selected */ 
			        Object nodeInfo = node.getUserObject();
				
			    /* React to the node selection. */
			        updateSelectedNode(node);
			        	
			    }
			});
	}
	
	void updateSelectedNode(DefaultMutableTreeNode node){
		if (!(node.getUserObject() instanceof DataField)){
			cmdOK.setEnabled(false);
			return;
			}
		
		cmdOK.setEnabled(true);
		selected_field = ((DataField)node.getUserObject()).getName();
		node = (DefaultMutableTreeNode)node.getParent();
		selected_table = ((DataTable)node.getUserObject()).getName();
		node = (DefaultMutableTreeNode)node.getParent();
		selected_source = ((DataSource)node.getUserObject()).getName();
		
	}
	
	public boolean updateDialog(InterfaceOptions opts){
		this.options = opts;
		
		rootNode = getDataTablesNode();
		treeSources.setModel(new DefaultTreeModel(rootNode));
		
		return true;
	}
	
//	return a node of all displayable objects
	private DefaultMutableTreeNode getDataTablesNode(){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Data Sources");
		DefaultMutableTreeNode thisNode;
		CorticalThicknessDataTableOptions options = (CorticalThicknessDataTableOptions)this.options;
		//InterfaceDisplayPanel p = options.getDisplayPanel();
		//return all DataTable and DataQuery objects
		ArrayList<DataSource> sources = InterfaceSession.getWorkspace().getDataSources();
		for (int i = 0; i < sources.size(); i++){
			//add datasource node
			thisNode = new DefaultMutableTreeNode(sources.get(i));
			//add all tables and queries
			if (sources.get(i).isConnected()){
				try{
					ArrayList<DataTable> tables = sources.get(i).getTableSet().getTables();
					for (int j = 0; j < tables.size(); j++){
						DataTable table = tables.get(j);
						DefaultMutableTreeNode table_node = new DefaultMutableTreeNode(table);
						thisNode.add(table_node);
						Iterator<String> itr = table.getFields().keySet().iterator();
						while (itr.hasNext())
							table_node.add(new DefaultMutableTreeNode(table.getField(itr.next())));
						}
				}catch (DataSourceException ex){
					InterfaceSession.log("CorticalThicknessDataTableDialogBox: Could not retrieve table set from data source '" + 
							 sources.get(i).getName(), 
							 LoggingType.Errors);
					}
				for (int j = 0; j < sources.get(i).getDataQueries().size(); j++)
					thisNode.add(new DefaultMutableTreeNode(sources.get(i).getDataQueries().get(j)));
				root.add(thisNode);
				}
			}
		return root;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//set options
			CorticalThicknessDataTableOptions options = (CorticalThicknessDataTableOptions)getOptions();
			options.data_source = selected_source;
			options.data_table = selected_table;
			options.id_field = selected_field;
			
			((InterfaceCorticalThicknessPanel)options.panel).addDataTable();
			setVisible(false);
		}
		
		super.actionPerformed(e);
		
	}
	
}