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


package mgui.interfaces.neuro;

import java.util.ArrayList;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.maps.NameMap;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

/*******************************
 * Defines an atlas mapping a set of data points to integers which map in turn
 * to atlas names (via a name map).
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceNeuroAtlas extends AbstractInterfaceObject {

	protected ArrayList<MguiInteger> mappings;
	protected NameMap name_map;
	protected String name;
	
	public InterfaceNeuroAtlas(String name){
		setName(name);
	}
	
	public InterfaceNeuroAtlas(String name, ArrayList<MguiNumber> mappings){
		setName(name);
		setMappings(mappings);
	}
	
	public InterfaceNeuroAtlas(String name, ArrayList<MguiNumber> mappings, NameMap name_map){
		setName(name);
		setMappings(mappings);
		setNameMap(name_map);
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public ArrayList<MguiInteger> getMappings(){
		return mappings;
	}
	
	public void setMappings(ArrayList<MguiNumber> mappings){
		//copy to integer new array
		this.mappings = new ArrayList<MguiInteger>(mappings.size());
		for (int i = 0; i < mappings.size(); i++)
			this.mappings.add(new MguiInteger(mappings.get(i).getValue()));
	}
	
	public NameMap getNameMap(){
		return name_map;
	}
	
	public void setNameMap(NameMap name_map){
		this.name_map = name_map;
	}
	
	public String getNameAt(int index){
		if (index < 0 || index > mappings.size() || name_map == null) return null;
		return name_map.get(mappings.get(index).getInt());
	}
	
	public int getMappingAt(int index){
		return mappings.get(index).getInt();
	}
	
	public String toString(){
		return getName();
	}
	
}