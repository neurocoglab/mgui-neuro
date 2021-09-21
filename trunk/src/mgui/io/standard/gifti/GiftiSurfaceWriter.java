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

import java.awt.image.DataBuffer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Vector;

import org.jogamp.vecmath.Color4f;
import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileWriter;
import mgui.io.standard.gifti.GiftiOutputOptions.ColumnType;
import mgui.io.standard.gifti.GiftiOutputOptions.NiftiIntent;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.util.Colour;

/****************************************************************
 * Writes a surface to Gifti format. See <a href="http://www.nitrc.org/projects/gifti/">
 * http://www.nitrc.org/projects/gifti/</a> for details and specification.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GiftiSurfaceWriter extends SurfaceFileWriter {

//	String GIFTI_HEADER = "<!DOCTYPE GIFTI SYSTEM 'http://www.nitrc.org/frs/download.php/115/gifti.dtd'>" +
//						  "\n<GIFTI Version='1.0'  NumberOfDataArrays='2'>";
//	
	String tab1 = "\t";
	String tab2 = "\t\t";
	String tab3 = "\t\t\t";
	
	public GiftiSurfaceWriter(){
		
	}
	
	public GiftiSurfaceWriter(File file){
		this.setFile(file);
	}
	
	@Override
	public boolean writeSurface(Mesh3DInt mesh_int, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		GiftiOutputOptions _options = (GiftiOutputOptions)options;
		switch (_options.output_type) { 
			case SeparateFiles:
				// Write surface + columns as separate files
				return writeSurfaceSeparate(mesh_int, options, progress_bar);
			case AllInOne:
			default:
				return writeSurfaceAll(mesh_int, options, progress_bar);
			}
		
		
	}
	
	protected String getGiftiHeader(int num_arrays) {
		
		return "<!DOCTYPE GIFTI SYSTEM 'http://www.nitrc.org/frs/download.php/115/gifti.dtd'>" +
				        "\n<GIFTI Version='1.0'  NumberOfDataArrays='" + num_arrays + "'>";
		
	}
	
	/****************************
	 * Writes mesh to separate output files:
	 * 
	 * <ol>
	 * <li>pointset/triangle data sets to a "surf.gii" file
	 * <li>vertex-wise labels to separate "label.gii" files
	 * <li> vertex-wise values to separate "shape.gii" files
	 * </ol>
	 * 
	 * @param mesh_int
	 * @param options
	 * @param progress_bar
	 * @return
	 */
	protected boolean writeSurfaceSeparate(Mesh3DInt mesh_int, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		try{
			
			GiftiOutputOptions _options = (GiftiOutputOptions)options;
			
			// Encoding string
			switch(_options.encoding){
				case ExternalFileBinary:
					InterfaceSession.log("GiftiSurfaceWriter: Type 'ExternalFileBinary' not implemented.",
										 LoggingType.Errors);
					return false;
				default:
				}
			
			// Write mesh to surf.nii file
			String filename = dataFile.getName();
			if (filename.endsWith(".gii")) {
				filename = filename.substring(0, filename.length()-4) + ".surf.gii";
			} else if (!filename.endsWith(".surf.gii")) {
				filename = filename + ".surf.gii";
				}
			File output_file = new File(dataFile.getParentFile().getAbsolutePath() + File.separator + filename);
			
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(output_file));
			
			// Header
			writer.write(XMLFunctions.getXMLHeader());
			writer.write("\n" + getGiftiHeader(2));
			writer.write("\n" + tab1 + "<MetaData/>");
			
			// Write coordinates + faces
			writeMesh(writer, mesh_int, _options);
			
			writer.write("\n" + "</GIFTI>");
			writer.close();
			
			// Write vertex-wise labels/values to label.gii/shape.gii file(s)
			for (int i = 0; i < _options.write_columns.size(); i++) {
					
				String ext = _options.column_types.get(i) == ColumnType.Labels ? ".label.gii" : ".shape.gii";
				String number_format = _options.column_types.get(i) == ColumnType.Labels ? "0" : _options.number_format;
				
				String column = _options.write_columns.get(i);
				VertexDataColumn vcolumn = mesh_int.getVertexDataColumn(column);
				
				filename = dataFile.getName();
	
				if (filename.endsWith(ext)) {
					// All good
				} else if (filename.endsWith(".gii")) {
					filename = filename.substring(0, filename.length()-4) + ext;
				} else {
					filename = filename + ext;
					}
				output_file = new File(dataFile.getParentFile().getAbsolutePath() + File.separator + filename);
				
				writer = new BufferedWriter(new java.io.FileWriter(output_file));
					
				// Header
				writer.write(XMLFunctions.getXMLHeader());
				writer.write("\n" + getGiftiHeader(1));
				
				writer.write("\n" + tab1 + "<MetaData>");
				writeMetadataItem(writer, "Name", column, 2);
				
				if (_options.metadata != null) {
					
					for (int j = 0; j < metadata_mesh.length; j++) {
						writeMetadataItem(writer, metadata_mesh[j], _options.metadata.get(metadata_mesh[j]).toString(), 2);
						}
					
					}
				
				writer.write("\n" + tab1 + "</MetaData>");
					
				// Write label table, if necessary
				if (_options.column_types.get(i) == ColumnType.Labels && 
						vcolumn.getColourMap() instanceof DiscreteColourMap) {
					writeLabelTable(writer, vcolumn, _options);
					}
					
				// Write vertex values
				writeVertexData(writer, vcolumn, _options, number_format, NiftiIntent.NIFTI_INTENT_LABEL);

				writer.write("\n" + "</GIFTI>");
				writer.close();
				
				}

			return true;
			
		} catch (IOException ex) {
			InterfaceSession.log("GiftiSurfaceWriter: I/O error while writing to '" + 
					dataFile.getAbsolutePath() + "'.\nDetails: " + ex.getMessage(), 
					LoggingType.Errors);
			}
		
		
		return false;
	}
	
	
	/****************************
	 * Write all data sets, including vertex-wise data, to one GIFTI file. This will not be compatible
	 * with some viewers.
	 * 
	 * @param mesh_int
	 * @param options
	 * @param progress_bar
	 * @return
	 */
	protected boolean writeSurfaceAll(Mesh3DInt mesh_int, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		// Write the XML
		try{
			
			GiftiOutputOptions _options = (GiftiOutputOptions)options;
			
			// Encoding string
			switch(_options.encoding){
				case ExternalFileBinary:
					InterfaceSession.log("GiftiSurfaceWriter: Type 'ExternalFileBinary' not implemented.",
										 LoggingType.Errors);
					return false;
				default:
				}
			
			// LabelTable?
			VertexDataColumn label_column = null;
			if (_options.write_columns.size() > 0){
				for (int i = 0; i < _options.column_types.size(); i++){
					if (_options.column_types.get(i) == ColumnType.Labels) {
						
						VertexDataColumn vcolumn = mesh_int.getVertexDataColumn(_options.write_columns.get(i));

						ColourMap cmap = vcolumn.getColourMap();
						if (cmap instanceof DiscreteColourMap) {
							label_column = vcolumn;
							continue;
							}
						
						}
					}
				
				}
			
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
			
			// Header
			writer.write(XMLFunctions.getXMLHeader());
			writer.write("\n" + getGiftiHeader(2 + _options.write_columns.size()));
			writer.write("\n" + tab1 + "<MetaData/>");
			
			if (label_column != null) {
				this.writeLabelTable(writer, label_column, _options);
				}
			
			// Write coordinates + faces
			writeMesh(writer, mesh_int, _options);

			
			// Write vertex data, if specified
			if (_options.write_columns.size() > 0){
				for (int i = 0; i < _options.write_columns.size(); i++){
					VertexDataColumn this_column = mesh_int.getVertexDataColumn(_options.write_columns.get(i));
					String number_format = _options.write_formats.get(i);
					NiftiIntent nifti_intent = _options.column_types.get(i) == ColumnType.Values ? 
																					NiftiIntent.NIFTI_INTENT_SHAPE :
																					NiftiIntent.NIFTI_INTENT_LABEL;
					
					boolean write_data = false;
					boolean write_rgb = false;
					boolean write_rgba = false;
					
					switch (_options.column_format){
						case Data:
							write_data = true;
							break;
						case RGB:
							write_rgb = true;
							break;
						case RGBA:
							write_rgba = true;
							break;
						case DataAndRGB:
							write_data = true;
							write_rgb = true;
							break;
						case DataAndRGBA:
							write_data = true;
							write_rgba = true;
							break;
						}
					
					if (write_data){
						// Write data column
						writeVertexData(writer, this_column, _options, number_format, nifti_intent);
						}
					
					if (write_rgb){
						writeVertexColours(writer, this_column, _options, false);
						}
					
					if (write_rgba){
						writeVertexColours(writer, this_column, _options, true);
						}
					
					}
				
				}
			
			
			writer.write("\n" + "</GIFTI>");
			writer.close();
			
			return true;
		}catch (IOException ex){
			InterfaceSession.log("GiftiSurfaceWriter: I/O error while writing to '" + 
			dataFile.getAbsolutePath() + "'.\nDetails: " + ex.getMessage(), 
			LoggingType.Errors);
			}
		
		return false;
	}
	
	
	String[] metadata_mesh = new String[] {"AnatomicalStructurePrimary",
										   "AnatomicalStructureSecondary",
										   "GeometricType",
										   "TopologicalType"};
	
	protected void writeMesh(BufferedWriter writer, Mesh3DInt mesh_int, GiftiOutputOptions _options) throws IOException {
		
		Mesh3D mesh = mesh_int.getMesh();
		
		// Byte order
		ByteOrder b_order = _options.byte_order == GiftiOutputOptions.ByteOrder.LittleEndian ?
													ByteOrder.LITTLE_ENDIAN : 
													ByteOrder.BIG_ENDIAN;

		writer.write("\n" + tab1 + "<DataArray Intent='NIFTI_INTENT_POINTSET'" +
				"\n" + tab2 + "DataType='NIFTI_TYPE_FLOAT32'" +
				"\n" + tab2 + "ArrayIndexingOrder='RowMajorOrder'" +
				"\n" + tab2 + "Dimensionality='2'" + 
				"\n" + tab2 + "Dim0='" + mesh.getSize() + "'" +
				"\n" + tab2 + "Dim1='3'" + 
				"\n" + tab2 + "Encoding='" + _options.encoding + "'" +
				"\n" + tab2 + "Endian='" + _options.byte_order + "'" +
				"\n" + tab2 + "ExternalFileName=''" +
				"\n" + tab2 + "ExternalFileOffset='' >");

		
		writer.write("\n" + tab2 + "<MetaData>");
		writeMetadataItem(writer, "Name", mesh_int.getName(), 3);
		
		if (_options.metadata != null) {
			
			for (int i = 0; i < metadata_mesh.length; i++) {
				writeMetadataItem(writer, metadata_mesh[i], _options.metadata.get(metadata_mesh[i]).toString(), 3);
				}
			
			}
		
		writer.write("\n" + tab2 + "</MetaData>");
		
		writer.write("\n" + tab2 + "<CoordinateSystemTransformMatrix>");
		writer.write("\n" + tab3 + "<DataSpace>" + _options.data_space + "</DataSpace>");
		writer.write("\n" + tab3 + "<TransformedSpace>" + _options.data_space + "</TransformedSpace>");
		writer.write("\n" + tab3 + "<MatrixData>");
		for (int i = 0; i < 4; i++) {
			writer.write("\n" + tab3);
			for (int j = 0; j < 4; j++) {
				writer.write(_options.transform.getElement(i, j) + " ");
				}
			}
		writer.write("\n" + tab3 + "</MatrixData>");
		writer.write("\n" + tab2 + "</CoordinateSystemTransformMatrix>");
		
		
		writer.write("\n" + tab2 + "<Data>");
		
		// Write data in specified format
		switch (_options.encoding){
			case GZipBase64Binary:
				writeBinaryCoords(writer, mesh, true, b_order);
				break;
			case Base64Binary:
				writeBinaryCoords(writer, mesh, false, b_order);
				break;
			case Ascii:
				writeAsciiCoords(writer, mesh, _options.decimal_places);
				break;
			case ExternalFileBinary:
				// TODO: Placeholder for future implementation
				break;
			}
		
		writer.write("</Data>");
		writer.write("\n" + tab1 + "</DataArray>");
		
		// Faces
		writer.write("\n" + tab1 + "<DataArray Intent='NIFTI_INTENT_TRIANGLE'" +
				"\n" + tab2 + "DataType='NIFTI_TYPE_INT32'" +
				"\n" + tab2 + "ArrayIndexingOrder='RowMajorOrder'" +
				"\n" + tab2 + "Dimensionality='2'" + 
				"\n" + tab2 + "Dim0='" + mesh.getFaceCount() + "'" +
				"\n" + tab2 + "Dim1='3'" + 
				"\n" + tab2 + "Encoding='" + _options.encoding + "'" +
				"\n" + tab2 + "Endian='" + _options.byte_order + "'" +
				"\n" + tab2 + "ExternalFileName=''" +
				"\n" + tab2 + "ExternalFileOffset='' >");
		
		writer.write("\n" + tab2 + "<MetaData />");
		
		writer.write("\n" + tab2 + "<Data>");
		
		// Write data in specified format
		switch (_options.encoding){
			case GZipBase64Binary:
				writeBinaryFaces(writer, mesh, true, b_order);
				break;
			case Base64Binary:
				writeBinaryFaces(writer, mesh, false, b_order);
				break;
			case Ascii:
				writeAsciiFaces(writer, mesh);
				break;
			case ExternalFileBinary:
				// TODO: Placeholder for future implementation
				break;
			}
		
		writer.write("</Data>");
		writer.write("\n" + tab1 + "</DataArray>");
		
	}
	
	protected void writeVertexData(BufferedWriter writer, VertexDataColumn this_column, GiftiOutputOptions _options,
								   String number_format, NiftiIntent nifti_intent) throws IOException{
		
		String tab1 = "\t";
		String tab2 = "\t\t";
		
		ByteOrder b_order = ByteOrder.BIG_ENDIAN;
		
		switch (_options.byte_order){
			case LittleEndian:
				b_order = ByteOrder.LITTLE_ENDIAN;
				break;
			case BigEndian:
				b_order = ByteOrder.BIG_ENDIAN;
				break;
			}
		
		// Encoding string
		String encoding = null;
		switch(_options.encoding){
			case Ascii:
				encoding = "ASCII";
				break;
			case Base64Binary:
				encoding = "Base64Binary";
				break;
			case GZipBase64Binary:
				encoding = "GZipBase64Binary";
				break;
			case ExternalFileBinary:
				encoding = "ExternalFileBinary";
				InterfaceSession.log("GiftiSurfaceWriter: Type 'ExternalFileBinary' not implemented.",
									 LoggingType.Errors);
				return;
			}
		
		String dt = getGiftiDatatype(this_column.getDataTransferType());
		
		ArrayList<MguiNumber> data = this_column.getData();
		if (nifti_intent == NiftiIntent.NIFTI_INTENT_LABEL) {
			data = getDataAsInteger(data);
			dt = getGiftiDatatype(DataBuffer.TYPE_INT);
			}
		
		writer.write("\n" + tab1 + "<DataArray Intent='" + nifti_intent + "'" +
				"\n" + tab2 + "DataType='" + dt + "'" +
				"\n" + tab2 + "ArrayIndexingOrder='RowMajorOrder'" +
				"\n" + tab2 + "Dimensionality='1'" + 
				"\n" + tab2 + "Dim0='" + data.size() + "'" +
				"\n" + tab2 + "Encoding='" + encoding + "'" +
				"\n" + tab2 + "Endian='" + _options.byte_order + "'" +
				"\n" + tab2 + "ExternalFileName=''" +
				"\n" + tab2 + "ExternalFileOffset='' >");
		
		
		writer.write("\n" + tab2 + "<MetaData>");
		writeMetadataItem(writer, "Name", this_column.getName(), 3);
		
//		if (_options.metadata != null) {
//			
//			for (int i = 0; i < metadata_mesh.length; i++) {
//				writeMetadataItem(writer, metadata_mesh[i], _options.metadata.get(metadata_mesh[i]).toString(), 3);
//				}
//			
//			}
		
		writer.write("\n" + tab2 + "</MetaData>");
		
		writer.write("\n" + tab2 + "<CoordinateSystemTransformMatrix/>");
		writer.write("\n" + tab2 + "<Data>");
		
		// Write data in specified format
		switch (_options.encoding){
			case GZipBase64Binary:
				writeBinaryArray(writer, data, true, b_order);
				break;
			case Base64Binary:
				writeBinaryArray(writer, data, false, b_order);
				break;
			case Ascii:
				//writeAsciiArray(writer, data, this_column.getDataTransferType() == DataBuffer.TYPE_INT ? 0 : _options.decimal_places);
				writeAsciiArray(writer, data, number_format);

				break;
			case ExternalFileBinary:
				// TODO: Placeholder for future implementation
				break;
			}
		
		writer.write("</Data>");
		writer.write("\n" + tab1 + "</DataArray>");
	}
	
	private ArrayList<MguiNumber> getDataAsInteger(ArrayList<MguiNumber> data) {
		
		ArrayList<MguiNumber> new_data = new ArrayList<MguiNumber>(data.size());
		
		for (MguiNumber v : data) {
			new_data.add(new MguiInteger(v.getValue()));
			}
		
		return new_data;
		
	}
	
	protected boolean writeLabelTable(BufferedWriter writer, VertexDataColumn this_column, GiftiOutputOptions _options)
			throws IOException{
		
		// LabelTable
		NameMap label_nmap = this_column.getNameMap();
		DiscreteColourMap label_cmap = null;
		if (this_column.getColourMap() instanceof DiscreteColourMap) {
			label_cmap = (DiscreteColourMap)this_column.getColourMap();
			}
		
		if (label_cmap == null) {
			InterfaceSession.log("GiftiSurfaceWriter: LabelTable must correspond to a colours map; column " + 
									this_column.getName() + " has none.", LoggingType.Errors);
			return false;
			}

		writer.write("\n" + tab1 + "<LabelTable>");
		String idx_name;
		
		for (int idx : label_cmap.getIndices()) {
			idx_name = null;
			if (label_nmap != null) {
				idx_name = label_nmap.get(idx);
				}
			if (idx_name == null) {
				idx_name = "" + idx;
				}
			writer.write("\n" + tab2 + "<Label Key='" + idx + "'");
			Colour clr = label_cmap.getColour(idx);
			writer.write(" Red='" + clr.getRed() + "'" +
						 " Green='" + clr.getGreen() + "'" +
						 " Blue='" + clr.getBlue() + "'" +
						 " Alpha='" + clr.getAlpha() + "'>" +
						 idx_name + "</Label>");
			}
		
		writer.write("\n" + tab1 + "</LabelTable>");
		
		return true;
	}
	
	protected void writeVertexLabels(BufferedWriter writer, VertexDataColumn this_column, GiftiOutputOptions _options) 
			throws IOException{
		
		
		
		
		
	}
	
	
	protected void writeMetadataItem(BufferedWriter writer, String name, String value, int tab) throws IOException {
		
		String tab1 = "";
		for (int i = 0; i < tab; i++)
			tab1 += "\t";
		String tab2 = tab1 + "\t";
		
		String line = "\n" + tab1 + "<MD>" +
					  "\n" + tab2 + "<Name>" + name + "</Name>" +
					  "\n" + tab2 + "<Value>" + value + "</Value>" +
					  "\n" + tab1 + "</MD>";
		
		writer.write(line);
		
	}
	
	protected void writeVertexColours(BufferedWriter writer, VertexDataColumn this_column, GiftiOutputOptions _options, boolean has_alpha) throws IOException{
		
		String tab1 = "\t";
		String tab2 = "\t\t";
		
		ByteOrder b_order = ByteOrder.BIG_ENDIAN;
		String intent = "RGB";
		if (has_alpha) intent = "RGBA";
		
		switch (_options.byte_order){
			case LittleEndian:
				b_order = ByteOrder.LITTLE_ENDIAN;
				break;
			case BigEndian:
				b_order = ByteOrder.BIG_ENDIAN;
				break;
			}
		
		// Encoding string
		String encoding = null;
		switch(_options.encoding){
			case Ascii:
				encoding = "ASCII";
				break;
			case Base64Binary:
				encoding = "Base64Binary";
				break;
			case GZipBase64Binary:
				encoding = "GZipBase64Binary";
				break;
			case ExternalFileBinary:
				encoding = "ExternalFileBinary";
				InterfaceSession.log("GiftiSurfaceWriter: Type 'ExternalFileBinary' not implemented.",
									 LoggingType.Errors);
				return;
			}
		
		
		ArrayList<MguiNumber> data = this_column.getData();
		
		String dt = getGiftiDatatype(this_column.getDataTransferType());
		writer.write("\n" + tab1 + "<DataArray Intent='NIFTI_INTENT_'" + intent +
				"\n" + tab2 + "DataType='" + dt + "'" +
				"\n" + tab2 + "ArrayIndexingOrder='RowMajorOrder'" +
				"\n" + tab2 + "Dimensionality='1'" + 
				"\n" + tab2 + "Dim0='" + data.size() + "'" +
				"\n" + tab2 + "Encoding='" + encoding + "'" +
				"\n" + tab2 + "Endian='" + _options.byte_order + "'" +
				"\n" + tab2 + "ExternalFileName=''" +
				"\n" + tab2 + "ExternalFileOffset='' >");
		writer.write("\n" + tab2 + "<MetaData Name='" + this_column.getName() + "'/>");
		writer.write("\n" + tab2 + "<CoordinateSystemTransformMatrix/>");
		writer.write("\n" + tab2 + "<Data>");
		
		// Write data in specified format
		switch (_options.encoding){
			case GZipBase64Binary:
				writeBinaryColours(writer, this_column, true, b_order, has_alpha);
				break;
			case Base64Binary:
				writeBinaryColours(writer, this_column, false, b_order, has_alpha);
				break;
			case Ascii:
				writeAsciiColours(writer, this_column, this_column.getDataTransferType() == DataBuffer.TYPE_INT ? 0 : _options.decimal_places, has_alpha);
				break;
			case ExternalFileBinary:
				// TODO: Placeholder for future implementation
				break;
			}
		
		writer.write("</Data>");
		writer.write("\n" + tab1 + "</DataArray>");
	}
	
	private String getGiftiDatatype(int transfer_type){
		
		switch (transfer_type){
		
			case DataBuffer.TYPE_DOUBLE:
			case DataBuffer.TYPE_FLOAT:
				return "NIFTI_TYPE_FLOAT32";
				
			case DataBuffer.TYPE_INT:
			case DataBuffer.TYPE_SHORT:
			case DataBuffer.TYPE_USHORT:
				return "NIFTI_TYPE_INT32";
				
			default:
				return "NIFTI_TYPE_FLOAT32";
			
			}
		
	}

	
	/*******************************************
	 * Write mesh coordinates as Base64 encoded binary data to an XML writer, in row major order.
	 * 
	 * @param mesh
	 * @param encoding
	 */
	protected void writeBinaryCoords(BufferedWriter writer, Mesh3D mesh, boolean compress, ByteOrder b_order) throws IOException{
		
		ByteBuffer data_out = ByteBuffer.allocate(mesh.getSize() * 3 * 4);
		data_out.order(b_order);
		
		// First encode as raw bytes
		for (int i = 0; i < mesh.getSize(); i++){
			Point3f p = mesh.getVertex(i);
			data_out.putFloat(p.x);
			data_out.putFloat(p.y);
			data_out.putFloat(p.z);
			}
		
		// Now compress if necessary
		if (compress){
			byte[] b = IoFunctions.compressZipped(data_out.array());
			data_out = ByteBuffer.wrap(b);
			}
		
		// Now encode as Base64
		// Finally, write string to file
		writer.write(Base64.getEncoder().encodeToString(data_out.array()));
		
	}
	
	protected void writeBinaryArray(BufferedWriter writer, ArrayList<MguiNumber> array, boolean compress, ByteOrder b_order) throws IOException{
		
		if (array.size() == 0) return;
		
		boolean is_int = array.get(0).getClass().equals(MguiInteger.class);
		
		ByteBuffer data_out = ByteBuffer.allocate(array.size() * 4);
		data_out.order(b_order);
		
		// First encode as raw bytes
		for (int i = 0; i < array.size(); i++){
			double val = array.get(i).getValue();
			if (is_int)
				data_out.putInt((int)val);
			else
				data_out.putFloat((float)val);
			}
		
		// Now compress if necessary
		if (compress){
			byte[] b = IoFunctions.compressZipped(data_out.array());
			data_out = ByteBuffer.wrap(b);
			}
		
		// Now encode as Base64
		// Finally, write string to file
		writer.write(Base64.getEncoder().encodeToString(data_out.array()));
		
	}
	
	protected void writeBinaryColours(BufferedWriter writer, VertexDataColumn column, boolean compress, ByteOrder b_order, boolean has_alpha) throws IOException{
		
		ArrayList<MguiNumber> array = column.getData();
		if (array.size() == 0) return;
		
		ByteBuffer data_out = ByteBuffer.allocate(array.size() * 4);
		data_out.order(b_order);
		
		ColourMap colourMap = column.getColourMap();
    	Color4f[] colours = colourMap.getColor4fArray(array, 
    												  column.getColourMin(), 
    												  column.getColourMax());
		
		// First encode as raw bytes
		for (int i = 0; i < colours.length; i++){
			data_out.putFloat(colours[i].getX());
			data_out.putFloat(colours[i].getY());
			data_out.putFloat(colours[i].getZ());
			if (has_alpha)
				data_out.putFloat(colours[i].getW());
			}
		
		// Now compress if necessary
		if (compress){
			byte[] b = IoFunctions.compressZipped(data_out.array());
			data_out = ByteBuffer.wrap(b);
			}
		
		// Now encode as Base64
		// Finally, write string to file
		writer.write(Base64.getEncoder().encodeToString(data_out.array()));
		
	}
	
	/*******************************************
	 * Write vertex data as ASCII data to an XML writer; formats to {@code decimals} decimal places. Wraps
	 * line when it exceeds 76 characters.
	 * 
	 * @param mesh
	 * @param encoding
	 */
	protected void writeAsciiArray(BufferedWriter writer, ArrayList<MguiNumber> data, int decimals) throws IOException{
		
		String line;
		int length = 0;
		
		for (int i = 0; i < data.size(); i++){
			line = MguiDouble.getString(data.get(i).getValue(), decimals);
			length += line.length();
			if (length > 76){
				line = line + "\n";
				length = 0;
			}else{
				line = line + " ";
				}
			writer.write(line);
			}
		
	}
	
	/*******************************************
	 * Write vertex data as ASCII data to an XML writer; formats to {@code decimals} decimal places. Wraps
	 * line when it exceeds 76 characters.
	 * 
	 * @param mesh
	 * @param number_format
	 */
	protected void writeAsciiArray(BufferedWriter writer, ArrayList<MguiNumber> data, String number_format) throws IOException{
		
		String line;
		int length = 0;
		
		for (int i = 0; i < data.size(); i++){
			line = MguiDouble.getString(data.get(i).getValue(), number_format);
			length += line.length();
			if (length > 76){
				line = line + "\n";
				length = 0;
			}else{
				line = line + " ";
				}
			writer.write(line);
			}
		
	}
	
	
	/*******************************************
	 * Write mesh faces as Base64 encoded binary data to an XML writer, in row major order.
	 * 
	 * @param mesh
	 * @param encoding
	 */
	protected void writeBinaryFaces(BufferedWriter writer, Mesh3D mesh, boolean compress, ByteOrder b_order) throws IOException{
	
		ByteBuffer data_out = ByteBuffer.allocate(mesh.getFaceCount() * 3 * 4);
		data_out.order(b_order);
		
		// First encode as raw bytes
		for (int i = 0; i < mesh.getFaceCount(); i++){
			MeshFace3D face = mesh.getFace(i);
			data_out.putInt(face.A);
			data_out.putInt(face.B);
			data_out.putInt(face.C);
			}
		
		// Now compress if necessary
		if (compress){
			byte[] b = IoFunctions.compressZipped(data_out.array());
			data_out = ByteBuffer.wrap(b);
			}
		
		// Now encode as Base64
		// Finally, write string to file
		writer.write(Base64.getEncoder().encodeToString(data_out.array()));
		
	}
	
	/*******************************************
	 * Write coordinates as ASCII data to an XML writer; formats to {@code decimals} decimal places. Wraps
	 * line when it exceeds 76 characters.
	 * 
	 * @param mesh
	 * @param encoding
	 */
	protected void writeAsciiCoords(BufferedWriter writer, Mesh3D mesh, int decimals) throws IOException{
		
		String line;
		int length = 0;
		
		for (int i = 0; i < mesh.getSize(); i++){
			Point3f p = mesh.getVertex(i);
			line = MguiDouble.getString(p.x, decimals) + " " + 
				   MguiDouble.getString(p.y, decimals) + " " + 
				   MguiDouble.getString(p.z, decimals);
			length += line.length();
			if (length > 76){
				line = line + "\n";
				length = 0;
			}else{
				line = line + " ";
				}
			writer.write(line);
			}
		
	}
	
	/*******************************************
	 * Write coordinates as ASCII data to an XML writer; formats to {@code decimals} decimal places. Wraps
	 * line when it exceeds 76 characters.
	 * 
	 * @param mesh
	 * @param encoding
	 */
	protected void writeAsciiColours(BufferedWriter writer, VertexDataColumn column, int decimals, boolean has_alpha) throws IOException{
		
		String line;
		int length = 0;
		
		ArrayList<MguiNumber> array = column.getData();
		ColourMap colourMap = column.getColourMap();
    	Color4f[] colours = colourMap.getColor4fArray(array, 
    												  column.getColourMin(), 
    												  column.getColourMax());
		
		for (int i = 0; i < colours.length; i++){
			line = MguiDouble.getString(colours[i].getX(), decimals) + " " + 
				   MguiDouble.getString(colours[i].getY(), decimals) + " " + 
				   MguiDouble.getString(colours[i].getZ(), decimals);
			if (has_alpha)
				line = line + " " + MguiDouble.getString(colours[i].getW(), decimals);
			length += line.length();
			if (length > 76){
				line = line + "\n";
				length = 0;
			}else{
				line = line + " ";
				}
			writer.write(line);
			}
		
	}
	
	
	/*******************************************
	 * Write faces as ASCII data to an XML writer; formats to {@code decimals} decimal places. Wraps
	 * line when it exceeds 76 characters.
	 * 
	 * @param mesh
	 * @param encoding
	 */
	protected void writeAsciiFaces(BufferedWriter writer, Mesh3D mesh) throws IOException{
		
		String line;
		int length = 0;
		
		for (int i = 0; i < mesh.getFaceCount(); i++){
			MeshFace3D face = mesh.getFace(i);
			line = face.A + " " + face.B + " " + face.C;
			length += line.length();
			if (length > 76){
				line = line + "\n";
				length = 0;
			}else{
				line = line + " ";
				}
			writer.write(line);
			}
		
	}
	
	@Override
	public InterfaceIOType getLoaderComplement(){
		return (new GiftiSurfaceLoader()).getIOType();
	}
	
}