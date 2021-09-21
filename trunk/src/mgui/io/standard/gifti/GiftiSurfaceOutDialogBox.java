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


package mgui.io.standard.gifti;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceOutputDialogBox;
import mgui.io.standard.gifti.GiftiOutputOptions.AnatomicalStructurePrimary;
import mgui.io.standard.gifti.GiftiOutputOptions.AnatomicalStructureSecondary;
import mgui.io.standard.gifti.GiftiOutputOptions.ByteOrder;
import mgui.io.standard.gifti.GiftiOutputOptions.ColumnType;
import mgui.io.standard.gifti.GiftiOutputOptions.DataSpace;
import mgui.io.standard.gifti.GiftiOutputOptions.GeometricType;
import mgui.io.standard.gifti.GiftiOutputOptions.GiftiEncoding;
import mgui.io.standard.gifti.GiftiOutputOptions.OutputType;
import mgui.io.standard.gifti.GiftiOutputOptions.TopologicalType;

/***************************************************************
 * Opotions dialog box for a Gifti surface write operation.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GiftiSurfaceOutDialogBox extends SurfaceOutputDialogBox {

	JPanel tabOutput = new JPanel(), tabOptions = new JPanel();
	
	JLabel lblEncoding = new JLabel("Encode as:");
	JComboBox cmbEncoding = new JComboBox();
	JLabel lblDecimals = new JLabel("Decimals:");
	JTextField txtDecimals = new JTextField("6");
	JLabel lblByteOrder = new JLabel("Byte order:");
	JComboBox cmbByteOrder = new JComboBox();
	JLabel lblWriteColumns = new JLabel("Write columns as:");
	JComboBox cmbWriteColumns = new JComboBox();
	JLabel lblOutputColumns = new JLabel("Output columns as:");
	JComboBox cmbOutputColumns = new JComboBox();
	
	JTabbedPane tabPane = new JTabbedPane();
	
	protected JTable tblColumns, tblOptions;
	//OptionsTableModel options_table_model;
	protected JScrollPane scrColumns;
	
	Mesh3DInt current_mesh;
	
	public GiftiSurfaceOutDialogBox(){
		super();
	}
	
	public GiftiSurfaceOutDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		init();
	}
	
	protected void init(){
		
		this.setDialogSize(450,480);
		
		setTitle("Output Gifti Surface File Options");
		
		cmbEncoding.addActionListener(this);
		cmbEncoding.setActionCommand("Encoding changed");
		
		tabOutput.setLayout(new LineLayout(20, 5, 0));
		tabOptions.setLayout(new LineLayout(20, 5, 0));
		
		tabPane.addTab("Output", tabOutput);
		tabPane.addTab("Options", tabOptions);
		mainPanel.removeAll();
		
		mainPanel.setLayout(new GridLayout(1, 1));
		mainPanel.add(tabPane);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		tabOutput.add(lblMesh, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		tabOutput.add(cmbMesh, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		tabOutput.add(lblEncoding, c);
		c = new LineLayoutConstraints(2, 2, 0.35, 0.6, 1);
		tabOutput.add(cmbEncoding, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		tabOutput.add(lblDecimals, c);
		c = new LineLayoutConstraints(3, 3, 0.35, 0.3, 1);
		tabOutput.add(txtDecimals, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.3, 1);
		tabOutput.add(lblByteOrder, c);
		c = new LineLayoutConstraints(4, 4, 0.35, 0.6, 1);
		tabOutput.add(cmbByteOrder, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.3, 1);
		tabOutput.add(lblWriteColumns, c);
		c = new LineLayoutConstraints(5, 5, 0.35, 0.6, 1);
		tabOutput.add(cmbWriteColumns, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.3, 1);
		tabOutput.add(lblOutputColumns, c);
		c = new LineLayoutConstraints(6, 6, 0.35, 0.6, 1);
		tabOutput.add(cmbOutputColumns, c);
		
	
		
		
		fillCombos();
		updateDialog();
		
	}
	
	private void fillCombos(){
		cmbEncoding.removeAllItems();
		cmbEncoding.addItem("Ascii");
		cmbEncoding.addItem("Base64Binary");
		cmbEncoding.addItem("GzipBase64Binary");
		
		cmbByteOrder.removeAllItems();
		cmbByteOrder.addItem("BigEndian");
		cmbByteOrder.addItem("LittleEndian");
		
		
		
		cmbWriteColumns.removeAllItems();
		cmbWriteColumns.addItem("Values");
		cmbWriteColumns.addItem("RGB");
		cmbWriteColumns.addItem("RGBA");
		cmbWriteColumns.addItem("Values + RGB");
		cmbWriteColumns.addItem("Values + RGBA");
		
		cmbOutputColumns.removeAllItems();
		cmbOutputColumns.addItem("Single file");
		cmbOutputColumns.addItem("Surface + Column files");
		
	}
	
	protected void fillMeshCombo(){
		super.fillMeshCombo();
		
		current_mesh = (Mesh3DInt)cmbMesh.getSelectedItem();
		
	}
	
	public void showDialog(){
		//updateDialog();
		this.setVisible(true);
	}
	
	public boolean updateDialog(){
		if (options == null) options = new GiftiOutputOptions();
		return updateDialog(options);
	}
	
	@Override
	public boolean updateDialog(InterfaceOptions options){
		this.options = (InterfaceIOOptions)options;
		
		GiftiOutputOptions _options = (GiftiOutputOptions)options;
		
		switch (_options.encoding){
			case Ascii:
				cmbEncoding.setSelectedItem("Ascii");
				break;
			case Base64Binary:
				cmbEncoding.setSelectedItem("Base64Binary");
				break;
			case GZipBase64Binary:
				cmbEncoding.setSelectedItem("GzipBase64Binary");
				break;
			case ExternalFileBinary:
				// TODO: Placeholder for implementation
				break;
			}
	
		switch (_options.byte_order){
			case BigEndian:
				cmbByteOrder.setSelectedItem("BigEndian");
				break;
			case LittleEndian:
				cmbByteOrder.setSelectedItem("LittleEndian");
				break;
			}
		
		switch (_options.output_type) {
		case AllInOne:
			cmbOutputColumns.setSelectedItem("Single file");
			break;
		case SeparateFiles:
			cmbOutputColumns.setSelectedItem("Surface + Column files");
			break;
		}
		
		txtDecimals.setText("" + _options.decimal_places);
		updateControls();
		updateColumnTable();
		updateOptionsTable();
		
		return true;
	}
	
	private void updateControls(){
		boolean is_ascii = cmbEncoding.getSelectedItem().equals("Ascii");
		txtDecimals.setEnabled(is_ascii);
	}
	
	protected void updateOptionsTable() {
		
		GiftiOutputOptions _options = (GiftiOutputOptions)options;
		
		// Table
		HashMap<String,Object> values = new HashMap<String,Object>();
		
		if (_options.metadata == null) { 
			values.put("DataSpace", DataSpace.NIFTI_XFORM_UNKNOWN);
			values.put("AnatomicalStructurePrimary", AnatomicalStructurePrimary.Other);
			values.put("AnatomicalStructureSecondary", AnatomicalStructureSecondary.GrayWhite);
			values.put("GeometricType", GeometricType.Reconstruction);
			values.put("TopologicalType", TopologicalType.Closed);
		} else {
			values = _options.metadata;
			values.put("DataSpace", _options.data_space);
			}
		
		OptionsTableModel options_table_model = new OptionsTableModel(values);
		tblOptions = new JTable(options_table_model);
		tblOptions.setRowHeight(25);
		TableColumn col = tblOptions.getColumnModel().getColumn(1);
		col.setCellEditor(new OptionsTableCellEditor(options_table_model));
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 12, 0.05, 0.9, 1);
		tabOptions.add(tblOptions, c);
	}
	
	protected void updateColumnTable(){
		if (current_mesh == null) return;
		
		//header and new table model
		if (scrColumns != null) tabOutput.remove(scrColumns);
		
		//table for all data into mesh
		ArrayList<String> cols = current_mesh.getVertexDataColumnNames();
		if (cols == null) return;
		
		Vector<String> v_cols = new Vector<String>(cols);
		Vector<Boolean> v_out = new Vector<Boolean>();
		Vector<String> v_formats = new Vector<String>();
		Vector<ColumnType> v_types = new Vector<ColumnType>();
		
		GiftiOutputOptions _options = (GiftiOutputOptions)options;
		
		for (int i = 0; i < v_cols.size(); i++){
			String column = v_cols.get(i);
			if (_options.write_columns != null && _options.write_columns.contains(column)){
				v_out.add(true);
				int idx = _options.write_columns.indexOf(column);
				v_types.add(idx, _options.column_types.get(idx) );
				v_formats.add(idx, _options.write_formats.get(idx));
			}else{
				v_out.add(false);
				int dtype = current_mesh.getVertexDataColumn(v_cols.get(i)).getDataTransferType();
				if (dtype == DataBuffer.TYPE_INT || dtype == DataBuffer.TYPE_SHORT) {
					v_formats.add("0");
					v_types.add(ColumnType.Labels);
				} else {
					v_formats.add("0.000#####");
					v_types.add(ColumnType.Values);
					}
				}
			}
		
		Vector<Vector<Object>> values = new Vector<Vector<Object>>(cols.size());
		for (int i = 0; i < cols.size(); i++){
			Vector<Object> v = new Vector<Object>(4);
			v.add(v_out.get(i));
			v.add(v_cols.get(i));
			v.add(v_formats.get(i));
			v.add(v_types.get(i));
			values.add(v);
			}
		
		Vector<String> header = new Vector<String>(4);
		header.add("Write");
		header.add("Column");
		header.add("Number");
		header.add("Type");
		
		ColumnTableModel model = new ColumnTableModel(values, header);
		tblColumns = new JTable(model);
		TableColumn col = tblColumns.getColumnModel().getColumn(3);
		JComboBox<ColumnType> cmbTypes = new JComboBox<ColumnType>();
		cmbTypes.addItem(ColumnType.Labels);
		cmbTypes.addItem(ColumnType.Values);
		col.setCellEditor(new DefaultCellEditor(cmbTypes));
		
		scrColumns = new JScrollPane(tblColumns);
		tblColumns.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		LineLayoutConstraints c = new LineLayoutConstraints(8, 13, 0.05, 0.9, 1);
		tabOutput.add(scrColumns, c);
		tabOutput.updateUI();
		
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (options == null) return;
	
			GiftiOutputOptions _options = (GiftiOutputOptions)options;
			
			_options.mesh = getMesh();
		
			if (cmbEncoding.getSelectedItem().equals("Ascii"))
				_options.encoding = GiftiEncoding.Ascii;
			else if (cmbEncoding.getSelectedItem().equals("Base64Binary"))
				_options.encoding = GiftiEncoding.Base64Binary;
			else if (cmbEncoding.getSelectedItem().equals("GzipBase64Binary"))
				_options.encoding = GiftiEncoding.GZipBase64Binary;
			
			if (cmbByteOrder.getSelectedItem().equals("BigEndian"))
				_options.byte_order = ByteOrder.BigEndian;
			else
				_options.byte_order = ByteOrder.LittleEndian;
			
			if (cmbOutputColumns.getSelectedItem().equals("Single file"))
				_options.output_type = OutputType.AllInOne;
			else
				_options.output_type = OutputType.SeparateFiles;
			
			_options.decimal_places = Integer.valueOf(txtDecimals.getText());
			
			// Data columns
			_options.write_columns = new ArrayList<String>();
			_options.write_formats = new ArrayList<String>();
			_options.column_types = new ArrayList<ColumnType>();
			
			if (tblColumns != null && tblColumns.getModel() != null) {
				ColumnTableModel model = (ColumnTableModel)tblColumns.getModel();
				for (int i = 0; i < model.getRowCount(); i++)
					if (model.getValueAt(i, 0).equals(true)){
						_options.write_columns.add((String)model.getValueAt(i, 1));
						_options.write_formats.add((String)model.getValueAt(i, 2));
						_options.column_types.add((ColumnType)model.getValueAt(i, 3));
						}
				}
			
			// Options
			OptionsTableModel options_table_model = (OptionsTableModel)tblOptions.getModel();
			if (options_table_model != null) {
				_options.data_space = (DataSpace)options_table_model.values.get("DataSpace");
				_options.metadata = options_table_model.values;	
				}
			
			setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Encoding changed")){
			updateControls();
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	
	protected class ColumnTableModel extends AbstractTableModel {
	      
		Vector<Vector<Object>> data;
		Vector<String> columns;
		
		public ColumnTableModel(Vector<Vector<Object>> data, Vector<String> columns){
			this.data = data;
			this.columns = columns;

		}

        public int getColumnCount() {
            return columns.size();
        }

        public int getRowCount() {
            return data.size();
        }

        @Override
		public String getColumnName(int col) {
            return columns.get(col);
        }

        public Object getValueAt(int row, int col) {
            return data.get(row).get(col);
        }

        @Override
		public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Column names not editable
         */
        @Override
		public boolean isCellEditable(int row, int col) {
        	if (col == 1) return false;
        	return true;
        }

        @Override
		public void setValueAt(Object value, int row, int col) {
            data.get(row).set(col, value);
            fireTableCellUpdated(row, col);
        }
        	

    }
	
	protected class OptionsTableModel extends AbstractTableModel {
		
		Vector<String> keys = new Vector<String>();
		public HashMap<String,Object> values = new HashMap<String,Object>();
		
		public OptionsTableModel(HashMap<String,Object> values) {
			this.values = values;
			keys = new Vector<String>(values.keySet());
			Collections.sort(keys);
		}

		@Override
		public int getRowCount() {
			return values.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}
		
		public boolean isCellEditable(int row, int column) {
        	return column == 1;
        }
		
		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Name";
			if (column == 1)
				return "Value";
			return null;
        }

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0)
				return keys.get(row);
			if (column == 1)
				return values.get(keys.get(row));
			return null;
		}
		
		@Override
		public void setValueAt(Object value, int row, int column) {
			values.put(keys.get(row), value);
            fireTableCellUpdated(row, column);
        }
		
		
	}
	
	protected class OptionsTableCellEditor extends AbstractCellEditor implements TableCellEditor {
        private TableCellEditor editor;
        OptionsTableModel table_model;

        public OptionsTableCellEditor(OptionsTableModel table_model) {
        	this.table_model = table_model;
        }
        
        @Override
        public Object getCellEditorValue() {
        	if (editor != null) {
                return editor.getCellEditorValue();
            }

            return null;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            switch(table_model.keys.get(row)) {
            
	            case "DataSpace":
	            	editor = new DefaultCellEditor(new JComboBox<DataSpace>(DataSpace.values()));
	            	break;
	            	
	            case "AnatomicalStructurePrimary":
	            	editor = new DefaultCellEditor(new JComboBox<AnatomicalStructurePrimary>(AnatomicalStructurePrimary.values()));
	            	break;
	            	
	            case "AnatomicalStructureSecondary":
	            	editor = new DefaultCellEditor(new JComboBox<AnatomicalStructureSecondary>(AnatomicalStructureSecondary.values()));
	            	break;
	            	
	            case "GeometricType":
	            	editor = new DefaultCellEditor(new JComboBox<GeometricType>(GeometricType.values()));
	            	break;
	            	
	            case "TopologicalType":
	            	editor = new DefaultCellEditor(new JComboBox<TopologicalType>(TopologicalType.values()));
	            	break;
	            	
	            default:
	            	editor = new DefaultCellEditor(new JTextField(value.toString()));
            }

            return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }
	
}