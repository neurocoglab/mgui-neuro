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
import mgui.interfaces.maps.NameMap;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


public class CaretAreaColourInDialogBox extends InterfaceIODialogBox {

	JLabel lblNameMap = new JLabel("Use name map:");
	JComboBox cmbNameMap = new JComboBox();
	
	protected LineLayout lineLayout;
	
	public CaretAreaColourInDialogBox(){
		
	}
	
	public CaretAreaColourInDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		init();
	}
	
	protected void init(){
		
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,250);
		this.setTitle("Input Surface Data Options");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(lblNameMap, c);
		c = new LineLayoutConstraints(1, 1, 0.35, 0.6, 1);
		mainPanel.add(cmbNameMap, c);
		
		updateCombo();
		
	}
	
	void updateCombo(){
		
		NameMap current = (NameMap)cmbNameMap.getSelectedItem();
		cmbNameMap.removeAllItems();
		
		ArrayList<NameMap> maps = InterfaceEnvironment.getNameMaps();
		
		for (int i = 0; i < maps.size(); i++)
			cmbNameMap.addItem(maps.get(i));
		
		cmbNameMap.setSelectedItem(current);
		
	}
	
	public void showDialog(){
		if (options == null) options = new CaretAreaColourInOptions();
		CaretAreaColourInOptions opts = (CaretAreaColourInOptions)options;
		
		updateCombo();
		
		cmbNameMap.setSelectedItem(opts.nameMap);
		
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			((CaretAreaColourInOptions)options).nameMap = (NameMap)cmbNameMap.getSelectedItem();
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
	}
}