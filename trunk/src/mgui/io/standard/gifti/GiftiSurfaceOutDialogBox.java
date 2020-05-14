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

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.shapes.SurfaceOutputDialogBox;
import mgui.io.standard.gifti.GiftiOutputOptions.ByteOrder;
import mgui.io.standard.gifti.GiftiOutputOptions.GiftiEncoding;

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
	
	public GiftiSurfaceOutDialogBox(){
		super();
	}
	
	public GiftiSurfaceOutDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		init();
	}
	
	protected void init(){
		
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
		
		return true;
	}
	
	private void updateControls(){
		boolean is_ascii = cmbEncoding.getSelectedItem().equals("Ascii");
		txtDecimals.setEnabled(is_ascii);
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
				_options.encoding = GiftiEncoding.GzipBase64Binary;
			
			if (cmbByteOrder.getSelectedItem().equals("BigEndian"))
				_options.byte_order = ByteOrder.BigEndian;
			else
				_options.byte_order = ByteOrder.LittleEndian;
			
			_options.decimal_places = Integer.valueOf(txtDecimals.getText());
		
			setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Encoding changed")){
			updateControls();
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
}