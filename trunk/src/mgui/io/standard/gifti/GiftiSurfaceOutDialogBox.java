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

import java.awt.event.ActionEvent;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceOutputDialogBox;
import mgui.io.standard.gifti.GiftiOutputOptions.ByteOrder;
import mgui.io.standard.gifti.GiftiOutputOptions.GiftiEncoding;
import mgui.io.standard.gifti.GiftiOutputOptions.NiftiIntent;

/***************************************************************
 * Opotions dialog box for a Gifti surface write operation.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GiftiSurfaceOutDialogBox extends SurfaceOutputDialogBox {

	JLabel lblEncoding = new JLabel("Encode as:");
	JComboBox cmbEncoding = new JComboBox();
	JLabel lblDecimals = new JLabel("Decimals:");
	JTextField txtDecimals = new JTextField("6");
	JLabel lblByteOrder = new JLabel("Byte order:");
	JComboBox cmbByteOrder = new JComboBox();
	JLabel lblWriteColumns = new JLabel("Write columns as:");
	JComboBox cmbWriteColumns = new JComboBox();
	
	protected JTable table;
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
		
		this.setDialogSize(450,440);
		
		setTitle("Output Gifti Surface File Options");
		
		cmbEncoding.addActionListener(this);
		cmbEncoding.setActionCommand("Encoding changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		mainPanel.add(lblEncoding, c);
		c = new LineLayoutConstraints(2, 2, 0.35, 0.6, 1);
		mainPanel.add(cmbEncoding, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		mainPanel.add(lblDecimals, c);
		c = new LineLayoutConstraints(3, 3, 0.35, 0.3, 1);
		mainPanel.add(txtDecimals, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.3, 1);
		mainPanel.add(lblByteOrder, c);
		c = new LineLayoutConstraints(4, 4, 0.35, 0.6, 1);
		mainPanel.add(cmbByteOrder, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.3, 1);
		mainPanel.add(lblWriteColumns, c);
		c = new LineLayoutConstraints(5, 5, 0.35, 0.6, 1);
		mainPanel.add(cmbWriteColumns, c);
		
		// Table
		
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
		
	}
	
	protected void fillMeshCombo(){
		super.fillMeshCombo();
		
		current_mesh = (Mesh3DInt)cmbMesh.getSelectedItem();
		
	}
	
	public void showDialog(){
		updateDialog();
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
			case GzipBase64Binary:
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
		
		txtDecimals.setText("" + _options.decimal_places);
		updateControls();
		updateTable();
		
		return true;
	}
	
	private void updateControls(){
		boolean is_ascii = cmbEncoding.getSelectedItem().equals("Ascii");
		txtDecimals.setEnabled(is_ascii);
	}
	
	protected void updateTable(){
		if (current_mesh == null) return;
		
		//header and new table model
		if (scrColumns != null) mainPanel.remove(scrColumns);
		
		//table for all data into mesh
		ArrayList<String> cols = current_mesh.getVertexDataColumnNames();
		if (cols == null) return;
		
		Vector<String> v_cols = new Vector<String>(cols);
		Vector<Boolean> v_out = new Vector<Boolean>();
		Vector<String> v_formats = new Vector<String>();
		Vector<Boolean> v_labels = new Vector<Boolean>();
		
		GiftiOutputOptions _options = (GiftiOutputOptions)options;
		
		for (int i = 0; i < v_cols.size(); i++){
			String column = v_cols.get(i);
			if (_options.write_columns != null && _options.write_columns.contains(column)){
				v_out.add(true);
				int idx = _options.write_columns.indexOf(column);
				v_labels.add(idx, _options.nifti_intents.get(idx) == NiftiIntent.NIFTI_INTENT_LABEL);
				v_formats.add(idx, _options.write_formats.get(idx));
			}else{
				v_out.add(false);
				int dtype = current_mesh.getVertexDataColumn(v_cols.get(i)).getDataTransferType();
				if (dtype == DataBuffer.TYPE_INT || dtype == DataBuffer.TYPE_SHORT) {
					v_formats.add("0");
				} else {
					v_formats.add("0.000#####");
					}
				v_labels.add(false);
				}
			}
		
		Vector<Vector<Object>> values = new Vector<Vector<Object>>(cols.size());
		for (int i = 0; i < cols.size(); i++){
			Vector<Object> v = new Vector<Object>(4);
			v.add(v_out.get(i));
			v.add(v_cols.get(i));
			v.add(v_formats.get(i));
			v.add(v_labels.get(i));
			values.add(v);
			}
		
		Vector<String> header = new Vector<String>(4);
		header.add("Write");
		header.add("Column");
		header.add("Number");
		header.add("Labels");
		
		TableModel model = new TableModel(values, header);
		table = new JTable(model);
		scrColumns = new JScrollPane(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		LineLayoutConstraints c = new LineLayoutConstraints(7, 12, 0.05, 0.9, 1);
		mainPanel.add(scrColumns, c);
		mainPanel.updateUI();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (options == null) return;
	
			GiftiOutputOptions _options = (GiftiOutputOptions)options;
			TableModel model = (TableModel)table.getModel();
			_options.mesh = getMesh();
		
			if (cmbEncoding.getSelectedItem().equals("Ascii"))
				_options.encoding = GiftiEncoding.Ascii;
			else if (cmbEncoding.getSelectedItem().equals("Base64Binary"))
				_options.encoding = GiftiEncoding.Base64Binary;
			else if (cmbEncoding.getSelectedItem().equals("GzipBase64Binary"))
				_options.encoding = GiftiEncoding.GzipBase64Binary;
			
			if (cmbByteOrder.getSelectedItem().equals("BigEndian"))
				_options.byte_order = ByteOrder.BigEndian;
			else
				_options.byte_order = ByteOrder.LittleEndian;
			
			_options.decimal_places = Integer.valueOf(txtDecimals.getText());
			
			// Data columns
			_options.write_columns = new ArrayList<String>();
			_options.write_formats = new ArrayList<String>();
			_options.nifti_intents = new ArrayList<NiftiIntent>();
			for (int i = 0; i < model.getRowCount(); i++)
				if (model.getValueAt(i, 0).equals(true)){
					_options.write_columns.add((String)model.getValueAt(i, 1));
					_options.write_formats.add((String)model.getValueAt(i, 2));
					_options.nifti_intents.add((Boolean)model.getValueAt(i, 3) ? NiftiIntent.NIFTI_INTENT_LABEL : NiftiIntent.NIFTI_INTENT_VECTOR);
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
	
	
	protected class TableModel extends AbstractTableModel {
	      
		Vector<Vector<Object>> data;
		Vector<String> columns;
		
		public TableModel(Vector<Vector<Object>> data, Vector<String> columns){
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
	
}