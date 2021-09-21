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
import java.util.HashMap;

import org.jogamp.vecmath.Matrix4f;

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

	public enum GiftiEncoding {
		Ascii,
		Base64Binary,
		GZipBase64Binary,
		ExternalFileBinary;
	}
	
	public enum ByteOrder {
		LittleEndian,
		BigEndian;
	}
	
	public enum ColumnFormat {
		Data,
		RGB,
		RGBA,
		DataAndRGB,
		DataAndRGBA;
	}
	
	public enum NiftiIntent {
		NIFTI_INTENT_LABEL,
		NIFTI_INTENT_VECTOR,
		NIFTI_INTENT_SHAPE,
		NIFTI_INTENT_POINTSET,
		NIFTI_INTENT_NODE_INDEX,
		NIFTI_INTENT_TIME_SERIES,
		NIFTI_INTENT_TRIANGLE,
		NIFTI_INTENT_NONE;
	}
	
	public enum DataSpace {
		NIFTI_XFORM_UNKNOWN,
		NIFTI_XFORM_SCANNER_ANAT,
		NIFTI_XFORM_ALIGNED_ANAT,
		NIFTI_XFORM_TALAIRACH,
		NIFTI_XFORM_MNI_152;
	}
	
	public enum AnatomicalStructurePrimary {
		CortexLeft,
		CortexRight,
		CirtexRightAndLeft,
		Cerebellum,
		Head,
		HippocampusLeft,
		HippocampusRight,
		Other
	}
	
	public enum AnatomicalStructureSecondary {
		GrayWhite,
		Pial,
		MidThickness;
		
	}
	
	public enum GeometricType {
		Reconstruction,
		Anatomical,
		Inflated,
		VeryInflated,
		Spherical,
		SemiSpherical,
		Ellipsoid,
		Flat,
		Hull;
	}
	
	public enum TopologicalType {
		Open,
		Closed,
		Cut;
	}
	
	public enum ColumnType {
		Labels,
		Values;
	}
	
	public enum OutputType {
		AllInOne,
		SeparateFiles;
	}
	
	public GiftiEncoding encoding = GiftiEncoding.GZipBase64Binary;
	public ByteOrder byte_order = ByteOrder.LittleEndian;
	public int decimal_places = 5; 		// Number of decimal places, for ASCII-encoded data
	
	public DataSpace data_space = DataSpace.NIFTI_XFORM_UNKNOWN;
	public Matrix4f transform = new Matrix4f(1, 0, 0, 0, 
											 0, 1, 0, 0,
											 0, 0, 1, 0,
											 0, 0, 0, 1);
	
	public ArrayList<String> write_columns = new ArrayList<String>();
	public ArrayList<String> write_formats = new ArrayList<String>();
	public ColumnFormat column_format = ColumnFormat.Data;
	
	public ArrayList<ColumnType> column_types = new ArrayList<ColumnType>();
	//public ArrayList<NiftiIntent> nifti_intents = new ArrayList<NiftiIntent>();
	
	public OutputType output_type = OutputType.AllInOne;
	
	public HashMap<String,Object> metadata = null;
	
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