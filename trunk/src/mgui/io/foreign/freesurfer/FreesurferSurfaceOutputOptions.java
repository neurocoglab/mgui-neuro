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


package mgui.io.foreign.freesurfer;

import java.nio.ByteOrder;

import mgui.interfaces.InterfaceEnvironment;
import mgui.io.domestic.shapes.SurfaceOutputOptions;

/******************************************************
 * Options for a {@linkplain FreesurferSurfaceWriter}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FreesurferSurfaceOutputOptions extends SurfaceOutputOptions {

	public static final int FORMAT_BINARY = 0;
	public static final int FORMAT_ASCII = 1;
	
	public int format = FORMAT_BINARY;
	public ByteOrder byte_order = ByteOrder.BIG_ENDIAN;
	public String create_string = "Freesurfer surface file created by ModelGUI v" + InterfaceEnvironment.getVersion();
	
	public FreesurferSurfaceOutputOptions(){
		
	}
	
}