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

import java.util.ArrayList;

import mgui.io.domestic.shapes.SurfaceOutputOptions;

/***********************************************************
 * Options for writing a surface to a Gifti format file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GiftiOutputOptions extends SurfaceOutputOptions {

	public enum GiftiEncoding{
		Ascii,
		Base64Binary,
		GzipBase64Binary,
		ExternalFileBinary;
	}
	
	public enum ByteOrder{
		LittleEndian,
		BigEndian;
	}
	
	public enum ColumnFormat{
		Data,
		RGB,
		RGBA,
		DataAndRGB,
		DataAndRGBA;
	}
	
	public GiftiEncoding encoding = GiftiEncoding.GzipBase64Binary;
	public ByteOrder byte_order = ByteOrder.LittleEndian;
	public int decimal_places = 5; 		// Number of decimal places, for ASCII-encoded data
	
	public ArrayList<String> write_columns = new ArrayList<String>();
	public ColumnFormat column_format = ColumnFormat.Data;
	
	/********************************
	 * Sets the columns for which data will be written to the GIFTI file; an empty array, or {@code null},
	 * indicates that no columns will be written. The data will be written either as values or as their
	 * associated RGB or RGBA colours, using the current colour mapping (or both), as specified by the 
	 * {@code column_format} parameter.
	 * 
	 * @param columns
	 */
	public void setWriteColumns(ArrayList<String> columns){
		if (columns == null) columns = new ArrayList<String>();
		this.write_columns = new ArrayList<String>(columns);
	}
	
	
	
	
}