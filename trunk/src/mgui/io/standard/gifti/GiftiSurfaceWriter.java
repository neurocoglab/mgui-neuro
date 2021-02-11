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

import org.jogamp.vecmath.Color4f;
import org.jogamp.vecmath.Point3f;

import java.util.Base64;

import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileWriter;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

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

	String GIFTI_HEADER = "<!DOCTYPE GIFTI SYSTEM 'http://www.nitrc.org/frs/download.php/115/gifti.dtd'>" +
						  "\n<GIFTI Version='1.0'  NumberOfDataArrays='2'>";
	
	public GiftiSurfaceWriter(){
		
	}
	
	public GiftiSurfaceWriter(File file){
		this.setFile(file);
	}
	
	@Override
	public boolean writeSurface(Mesh3DInt mesh_int, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		// Write the XML
		try{
			String tab1 = "\t";
			String tab2 = "\t\t";
			
			Mesh3D mesh = mesh_int.getMesh();
			
			GiftiOutputOptions _options = (GiftiOutputOptions)options;
			
			// Encoding string
			String encoding = null;
			switch(_options.encoding){
				case Ascii:
					encoding = "ASCII";
					break;
				case Base64Binary:
					encoding = "Base64Binary";
					break;
				case GzipBase64Binary:
					encoding = "GZipBase64Binary";
					break;
				case ExternalFileBinary:
					encoding = "ExternalFileBinary";
					InterfaceSession.log("GiftiSurfaceWriter: Type 'ExternalFileBinary' not implemented.",
										 LoggingType.Errors);
					return false;
				}
			
			// Byte order
			String byte_order = null;
			ByteOrder b_order = ByteOrder.BIG_ENDIAN;
				
			switch (_options.byte_order){
				case LittleEndian:
					byte_order = "LittleEndian";
					b_order = ByteOrder.LITTLE_ENDIAN;
					break;
				case BigEndian:
					byte_order = "BigEndian";
					b_order = ByteOrder.BIG_ENDIAN;
					break;
				}
			
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
			
			// Header
			writer.write(XMLFunctions.getXMLHeader());
			writer.write("\n" + GIFTI_HEADER);
			writer.write("\n" + tab1 + "<MetaData/>");
			writer.write("\n" + tab1 + "<LabelTable/>");
			
			// Coordinates
			writer.write("\n" + tab1 + "<DataArray Intent='NIFTI_INTENT_POINTSET'" +
						"\n" + tab2 + "DataType='NIFTI_TYPE_FLOAT32'" +
						"\n" + tab2 + "ArrayIndexingOrder='RowMajorOrder'" +
						"\n" + tab2 + "Dimensionality='2'" + 
						"\n" + tab2 + "Dim0='" + mesh.getSize() + "'" +
						"\n" + tab2 + "Dim1='3'" + 
						"\n" + tab2 + "Encoding='" + encoding + "'" +
						"\n" + tab2 + "Endian='" + byte_order + "'" +
						"\n" + tab2 + "ExternalFileName=''" +
						"\n" + tab2 + "ExternalFileOffset='' >");
			writer.write("\n" + tab2 + "<MetaData/>");
			writer.write("\n" + tab2 + "<CoordinateSystemTransformMatrix/>");
			
			writer.write("\n" + tab2 + "<Data>");
			
			// Write data in specified format
			switch (_options.encoding){
				case GzipBase64Binary:
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
					"\n" + tab2 + "Encoding='" + encoding + "'" +
					"\n" + tab2 + "Endian='" + byte_order + "'" +
					"\n" + tab2 + "ExternalFileName=''" +
					"\n" + tab2 + "ExternalFileOffset='' >");
			writer.write("\n" + tab2 + "<MetaData/>");
			writer.write("\n" + tab2 + "<CoordinateSystemTransformMatrix/>");
			
			writer.write("\n" + tab2 + "<Data>");
			
			// Write data in specified format
			switch (_options.encoding){
				case GzipBase64Binary:
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
			
			// Write vertex data, if specified
			if (_options.write_columns.size() > 0){
				for (int i = 0; i < _options.write_columns.size(); i++){
					VertexDataColumn this_column = mesh_int.getVertexDataColumn(_options.write_columns.get(i));
					
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
						writeVertexData(writer, this_column, _options);
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
	
	protected void writeVertexData(BufferedWriter writer, VertexDataColumn this_column, GiftiOutputOptions _options) throws IOException{
		
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
		
		ArrayList<MguiNumber> data = this_column.getData();
		
		String dt = getGiftiDatatype(this_column.getDataTransferType());
		writer.write("\n" + tab1 + "<DataArray Intent='NIFTI_INTENT_VERTEXDATA'" +
				"\n" + tab2 + "DataType='" + dt + "'" +
				"\n" + tab2 + "ArrayIndexingOrder='RowMajorOrder'" +
				"\n" + tab2 + "Dimensionality='1'" + 
				"\n" + tab2 + "Dim0='" + data.size() + "'" +
				"\n" + tab2 + "Encoding='" + _options.encoding + "'" +
				"\n" + tab2 + "Endian='" + _options.byte_order + "'" +
				"\n" + tab2 + "ExternalFileName=''" +
				"\n" + tab2 + "ExternalFileOffset='' >");
		writer.write("\n" + tab2 + "<MetaData/>");
		writer.write("\n" + tab2 + "<CoordinateSystemTransformMatrix/>");
		writer.write("\n" + tab2 + "<Data>");
		
		// Write data in specified format
		switch (_options.encoding){
			case GzipBase64Binary:
				writeBinaryArray(writer, data, true, b_order);
				break;
			case Base64Binary:
				writeBinaryArray(writer, data, false, b_order);
				break;
			case Ascii:
				writeAsciiArray(writer, data, this_column.getDataTransferType() == DataBuffer.TYPE_INT ? 0 : _options.decimal_places);
				break;
			case ExternalFileBinary:
				// TODO: Placeholder for future implementation
				break;
			}
		
		writer.write("</Data>");
		writer.write("\n" + tab1 + "</DataArray>");
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
		
		ArrayList<MguiNumber> data = this_column.getData();
		
		String dt = getGiftiDatatype(this_column.getDataTransferType());
		writer.write("\n" + tab1 + "<DataArray Intent='NIFTI_INTENT_'" + intent +
				"\n" + tab2 + "DataType='" + dt + "'" +
				"\n" + tab2 + "ArrayIndexingOrder='RowMajorOrder'" +
				"\n" + tab2 + "Dimensionality='1'" + 
				"\n" + tab2 + "Dim0='" + data.size() + "'" +
				"\n" + tab2 + "Encoding='" + _options.encoding + "'" +
				"\n" + tab2 + "Endian='" + _options.byte_order + "'" +
				"\n" + tab2 + "ExternalFileName=''" +
				"\n" + tab2 + "ExternalFileOffset='' >");
		writer.write("\n" + tab2 + "<MetaData/>");
		writer.write("\n" + tab2 + "<CoordinateSystemTransformMatrix/>");
		writer.write("\n" + tab2 + "<Data>");
		
		// Write data in specified format
		switch (_options.encoding){
			case GzipBase64Binary:
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