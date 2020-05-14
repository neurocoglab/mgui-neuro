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


package mgui.io.foreign.caret;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

public class CaretAreaColourOutDialogBox extends InterfaceIODialogBox {

	public JLabel lblColourMap = new JLabel("Colour Map:");
	public JComboBox cmbColourMap = new JComboBox();
	
	public CaretAreaColourOutDialogBox(){
		
	}
	
	public CaretAreaColourOutDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super (frame, panel, options);
		_init();
	}
	
	private void _init(){
		super.init();
		
		fillCombo();
		setMainLayout(new LineLayout(20, 5, 0));
		setTitle("Write Caret Area Colour File - Options");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.2, 1);
		mainPanel.add(lblColourMap, c);
		c = new LineLayoutConstraints(1, 1, 0.25, 0.7, 1);
		mainPanel.add(cmbColourMap, c);
		
	}
	
	protected void fillCombo(){
		
		ArrayList<DiscreteColourMap> maps = InterfaceEnvironment.getDiscreteColourMaps();
		CaretAreaColourOutputOptions _options =  (CaretAreaColourOutputOptions)options;
		
		cmbColourMap.removeAllItems();
		
		for (int i = 0; i < maps.size(); i++)
			cmbColourMap.addItem(maps.get(i));
		
		if (_options.colour_map != null)
			cmbColourMap.setSelectedItem(_options.colour_map);
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			CaretAreaColourOutputOptions _options =  (CaretAreaColourOutputOptions)options;
			_options.colour_map = (DiscreteColourMap)cmbColourMap.getSelectedItem();
			
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
	}
	
}