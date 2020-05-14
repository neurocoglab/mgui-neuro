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


package mgui.neuro.components.cortical;

import java.util.HashMap;

import mgui.interfaces.shapes.Volume3DInt;
import mgui.neuro.components.cortical.simple.SimpleCorticalRegion;


/*********************************
 * A Volume3D-based representation of a cortical region. Contains a pointer to a
 * Volume3DInt object and an array of integers holding the indices of the voxels
 * comprising this cortical area.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class CorticalVolume extends SimpleCorticalRegion {

	
	
	@Override
	public void connectTo(AbstractCorticalRegion region, double weight) {
		// TODO Auto-generated method stub
		
	}


	public Volume3DInt volume;
	public int[] voxels; 
	
	public CorticalVolume(Volume3DInt vol){
		setVolume(vol);
	}
	
	public void setVolume(Volume3DInt vol){
		volume = vol;
	}
	
	public Volume3DInt getVolume(){
		return volume;
	}
	
	public void setVoxels(int[] v){
		voxels = v;
	}

	public void connectTo(AbstractCorticalRegion region) {
		// TODO Auto-generated method stub
		
	}

	
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	
}