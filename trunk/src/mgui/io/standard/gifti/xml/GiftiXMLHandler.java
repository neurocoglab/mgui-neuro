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


package mgui.io.standard.gifti.xml;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.numbers.MguiShort;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import Jama.Matrix;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**********************************************************
 * XML handler for GIFTI format surface files. See <a href="http://www.nitrc.org/projects/gifti/">
 * http://www.nitrc.org/projects/gifti/</a> for details and specification. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GiftiXMLHandler extends DefaultHandler {

	// Current surface object
	Mesh3DInt mesh_int = null;
	protected String topo_file = null;
	boolean is_new_mesh = true;
	
	// Metadata
	HashMap<String,String> metadata = new HashMap<String,String>();
	protected String md_name, md_value;
	
	// Version
	enum GiftiVersion{
		V_1_0;
	}
	GiftiVersion version = GiftiVersion.V_1_0;
	
	// Data array count
	int n_arrays; 
	
	// Keep track of XML tags
	String tag_tracker;
	
	// DataArray fields
	String da_intent;
	String da_type;
	String da_index_order;
	int da_dimensions;
	int[] da_dims;
	String da_encode;
	String da_endian;
	String da_file;
	String da_file_offset;
	
	// Transform matrix
	ArrayList<Matrix> transforms = new ArrayList<Matrix>();
	String s_transform;
	
	// Data buffer
	StringBuffer data_buffer;
	
	public GiftiXMLHandler(){
		super();
		is_new_mesh = true;
	}
	
	/***************************************
	 * Instantiate this handler with a pre-defined mesh. If the 
	 * 
	 * @param mesh
	 */
	public GiftiXMLHandler(Mesh3DInt mesh){
		super();
		this.mesh_int = mesh;
		is_new_mesh = mesh == null;
	}
	
	boolean is_finalized = false;
	
	/***************************************
	 * Returns the loaded surface. 
	 * 
	 * @return
	 */
	public Mesh3DInt getMesh(){
		if (!is_finalized)
			this.mesh_int.getMesh().finalize();
		is_finalized = true;
		return this.mesh_int;
	}
	
	/***************************************
	 * Returns the associated topo file, if one was set.
	 * 
	 * @return
	 */
	public String getTopoFile(){
		return topo_file;
	}
	
	/***************************************
	 * Returns the transform(s), if any were set
	 * 
	 * @return
	 */
	public ArrayList<Matrix> getTransforms(){
		return this.transforms;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (localName.equals("GIFTI")){
			if (tag_tracker != null){
				throw new SAXException ("GiftiXMLHandler: GIFTI tag encountered, but surface already started..");
				}
			
			// Instantiate mesh
			if (mesh_int == null)
				mesh_int = new Mesh3DInt();
			
			// Set version here (currently only 1.0 possible)
			version = GiftiVersion.V_1_0;
			
			// Number of arrays
			n_arrays = Integer.valueOf(attributes.getValue("NumberOfDataArrays"));
			
			// Track tags
			tag_tracker = localName;
			return;
			}
		
		if (localName.equals("MetaData")){
			
			if (tag_tracker.equals("GIFTI")){
				// Set surface metadata here
				
				tag_tracker = tag_tracker + "." + localName;
				return;
				}
			
			if (tag_tracker.equals("GIFTI.DataArray")){
				// Set data array metadata here
				
				tag_tracker = tag_tracker + "." + localName;
				return;
				}
			
			}
		
		if (localName.equals("DataArray")){
			
			if (tag_tracker.equals("GIFTI")){
				// Set up the loading parameters
				da_intent = attributes.getValue("Intent");
				da_type = attributes.getValue("DataType");
				da_index_order = attributes.getValue("ArrayIndexingOrder");
				da_dimensions = Integer.valueOf(attributes.getValue("Dimensionality"));
				da_dims = new int[da_dimensions];
				for (int i = 0; i < da_dimensions; i++){
					da_dims[i] = Integer.valueOf(attributes.getValue("Dim" + i));
					}
				da_encode = attributes.getValue("Encoding");
				da_endian = attributes.getValue("Endian");
				da_file = attributes.getValue("ExternalFileName");
				da_file_offset = attributes.getValue("ExternalFileOffset");
				
				}
			
			tag_tracker = tag_tracker + "." + localName;
			return;
			}
		
		if (localName.equals("CoordinateSystemTransformMatrix")){
			tag_tracker = tag_tracker + "." + localName;
			return;
			}
		
		if (localName.equals("MatrixData")){
			
			if (tag_tracker.endsWith(".CoordinateSystemTransformMatrix")){
				s_transform = "";
				}
			
			tag_tracker = tag_tracker + "." + localName;
			return;
			}
		
		if (localName.equals("Data")){
			
			if (tag_tracker.endsWith(".DataArray")){
				
				// We want to start loading data into a String buffer
				// Allocate according to dimensions?
				int size = 1;
				for (int i = 0; i < da_dims.length; i++)
					size *= da_dims[i];
				data_buffer = new StringBuffer(size);
				}
			
			tag_tracker = tag_tracker + "." + localName;
			return;
			}
		
		if (localName.equals("MD")){
			if (!tag_tracker.endsWith("MetaData"))
				throw new SAXException ("GiftiXMLHandler: MD tag encountered, but not currently in MetaData block..");
			
			tag_tracker = tag_tracker + "." + localName;
			return;
			}
		
		if (localName.equals("Name") && tag_tracker.endsWith("MD")){
			md_name = "";
			tag_tracker = tag_tracker + "." + localName;
			}
		
		if (localName.equals("Value") && tag_tracker.endsWith("MD")){
			md_value = "";
			tag_tracker = tag_tracker + "." + localName;
			}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (localName.equals("GIFTI")){
			// We're done here
			if (mesh_int == null)
				throw new SAXException ("GiftiXMLHandler: File finished but no surface was created..");
			
			tag_tracker = null;
			return;
			}
		
		if (localName.equals("MetaData")){
			
			if (tag_tracker.endsWith("NIFTI.MetaData")){
				
				topo_file = metadata.get("topo_file");
				
				}
			
			tag_tracker = tag_tracker.substring(0, tag_tracker.lastIndexOf("."));
			return;
			}
		
		if (localName.equals("MD")){
			metadata.put(md_name, md_value);
			md_name = null;
			md_value = null;
			tag_tracker = tag_tracker.substring(0, tag_tracker.lastIndexOf("."));
			return;
			}
		
		if (localName.equals("Name") && tag_tracker.endsWith(".MD.Name")){
			tag_tracker = tag_tracker.substring(0, tag_tracker.lastIndexOf("."));
			return;
			}
		
		if (localName.equals("Value") && tag_tracker.endsWith(".MD.Value")){
			tag_tracker = tag_tracker.substring(0, tag_tracker.lastIndexOf("."));
			return;
			}
		
		if (localName.equals("DataArray")){
			
			tag_tracker = tag_tracker.substring(0, tag_tracker.lastIndexOf("."));
			return;
			}
		
		if (localName.equals("MatrixData")){
			
			if (tag_tracker.endsWith("CoordinateSystemTransformMatrix.MatrixData")){
				if (s_transform == null)
					throw new SAXException ("GiftiXMLHandler: Transform matrix ended without being started..");
				transforms.add(parseMatrix(s_transform));
				s_transform = null;
				}
			
			tag_tracker = tag_tracker.substring(0, tag_tracker.lastIndexOf("."));
			return;
			}
		
		if (localName.equals("Data")){
			
			// Set the data
			if (tag_tracker.endsWith(".DataArray.Data")){
				// Convert the data buffer
				
				if (data_buffer == null)
					throw new SAXException ("GiftiXMLHandler: Data ended without being started..");
				
				String name = metadata.get("Name");
				if (name == null) name = "unnamed";
				
				try{
					if (da_encode.endsWith("Base64Binary")){
						// First, decode
						Charset charset = Charset.forName("UTF-8");
						byte[] utf8_bytes = data_buffer.toString().getBytes(charset);
						byte[] b_data = Base64.decode(utf8_bytes);
						
						if (da_encode.startsWith("GZip")){
							// If necessary, decompress
							// Apparently gzipped isn't really gzipped...
							b_data = IoFunctions.decompressZipped(b_data);
							}
						
						// Finally, convert
						switch(da_intent){
						
							case "NIFTI_INTENT_POINTSET":
								setSurfaceCoords(b_data);
								break;
								
							case "NIFTI_INTENT_TRIANGLE":
								setSurfaceFaces(b_data);
								break;
						
							default:
								InterfaceSession.log("Warning: GiftiXMLHandler: Unknown GIFTI intent: " 
															+ da_intent + "; I'm assuming it's vertex data.", 
													  LoggingType.Warnings);
							case "NIFTI_INTENT_LABEL":
							case "NIFTI_INTENT_SHAPE":
							case "NIFTI_INTENT_NONE":
								addVertexData(name, b_data);
								break;
								
							case "NIFTI_INTENT_TIMESERIES":
								InterfaceSession.log("GiftiXMLHandler: Column '" + name + "': Handling of time series data not yet implemented.", 
													 LoggingType.Errors);
								break;
							}
						
						
					}else if (da_encode.equals("ASCII")){
						
						switch(da_intent){
						
							case "NIFTI_INTENT_POINTSET":
								setSurfaceCoords(data_buffer.toString());
								break;
								
							case "NIFTI_INTENT_TRIANGLE":
								setSurfaceFaces(data_buffer.toString());
								break;
						
							default:
								InterfaceSession.log("Warning: GiftiXMLHandler: Unknown GIFTI intent: " 
															+ da_intent + "; I'm assuming it's vertex data.", 
													  LoggingType.Warnings);
							case "NIFTI_INTENT_LABEL":
							case "NIFTI_INTENT_SHAPE":
							case "NIFTI_INTENT_NONE":
								addVertexData(name, data_buffer.toString());
								break;
								
							case "NIFTI_INTENT_TIMESERIES":
								InterfaceSession.log("GiftiXMLHandler: Column '" + name + "': Handling of time series data not yet implemented.", 
													 LoggingType.Errors);
								break;
							}
						
						}
					
				}catch (Base64DecodingException ex){
					//ex.printStackTrace();
					throw new SAXException ("GiftiXMLHandler: Error decoding data; encoding is: " + da_encode);
				}catch (IOException ex){
					//ex.printStackTrace();
					throw new SAXException ("GiftiXMLHandler: I/O Error decoding data; encoding is: " + da_encode);
					}
				
				data_buffer = null;
				}
			tag_tracker = tag_tracker.substring(0, tag_tracker.lastIndexOf("."));
			}
		
		if (localName.equals("CoordinateSystemTransformMatrix")){
			tag_tracker = tag_tracker.substring(0, tag_tracker.lastIndexOf("."));
			return;
			}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		if (tag_tracker.endsWith(".MatrixData")){
			// Append matrix data
			for (int i = start; i < start + length; i++)
				s_transform = s_transform + ch[i];
			return;
			}
		
		if (tag_tracker.endsWith(".Data")){
			// Append data to buffer
			if (data_buffer == null)
				throw new SAXException ("GiftiXMLHandler: Data added but no buffer set..");
			data_buffer.append(Arrays.copyOfRange(ch, start, start + length));
			return;
			}
		
		if (tag_tracker.endsWith(".MD.Name")){
			md_name = md_name + new String(Arrays.copyOfRange(ch, start, start + length));
			return;
			}
		
		if (tag_tracker.endsWith(".MD.Value")){
			md_value = md_value + new String(Arrays.copyOfRange(ch, start, start + length));
			return;
			}
		
	}
	
	//******************** HELPER STUFF ******************************
	
	protected Matrix parseMatrix(String s_data) throws SAXException{
		
		// Assume space delimited
		String[] temp = s_data.split(" ");
		String[] pieces = new String[16];
		
		int j = 0;
		for (int i = 0; i < temp.length; i++){
			if (temp[i].trim().length() > 0){
				pieces[j++] = temp[i];
				}
			}
		
		if (j != 16)
			throw new SAXException ("GiftiXMLHandler: Incorrect number of elements in transform matrix: " + j);
		
		Matrix T = new Matrix(4,4);
		
		for (int i = 0; i < 4; i++)
			for (j = 0; j < 4; j++)
				T.set(i, j, Double.valueOf(pieces[i*4 + j]));
		
		return T;
	}
	
	/*************************************************************
	 * Sets the coordinates from {@code b_data}, with the given "da" parameters. If the mesh
	 * already has coordinates and faces, they will be deleted.
	 * 
	 * @param b_data
	 * @throws SAXException
	 */
	protected void setSurfaceCoords(byte[] b_data) throws SAXException{
		
		if (mesh_int == null)
			throw new SAXException ("GiftiXMLHandler: Coordinates set, but no mesh is..");
		
		int n = da_dims[0];
		int m = 3;
		if (da_dimensions != 2 || da_dims[1] != 3)
			throw new SAXException ("GiftiXMLHandler: Unexpected dimensions for mesh coordinates..");
		
		ByteOrder order = ByteOrder.BIG_ENDIAN;
		if (da_endian.equals("LittleEndian"))
			order = ByteOrder.LITTLE_ENDIAN;
		
		Mesh3D mesh = mesh_int.getMesh();
		mesh.removeAllVertices(false);
		
		if (da_type.equals("NIFTI_TYPE_FLOAT32")){
			// Convert bytes to floats

			for (int i = 0; i < n; i++){
				float[] coords = new float[m];
				for (int j = 0; j < m; j++){
					if (da_index_order.equals("RowMajorOrder")){
						coords[j] = arr2float(b_data, (i * m + j) * 4, order);
					}else{
						coords[j] = arr2float(b_data, (j * n + i) * 4, order);
						}
					}
				mesh.addVertex(new Point3f(coords[0], coords[1], coords[2]));
				}
			
		}else if (da_type.equals("NIFTI_TYPE_INT32")){
			// Convert bytes to floats

			for (int i = 0; i < n; i++){
				int[] coords = new int[m];
				for (int j = 0; j < m; j++){
					if (da_index_order.equals("RowMajorOrder")){
						coords[j] = arr2int(b_data, (i * m + j) * 4, order);
					}else{
						coords[j] = arr2int(b_data, (j * n + i) * 4, order);
						}
					}
				mesh.addVertex(new Point3f(coords[0], coords[1], coords[2]));
				}
			
		}
		
	}
	
	protected void setSurfaceCoords(String ascii_data) throws SAXException{
		
		if (mesh_int == null)
			throw new SAXException ("GiftiXMLHandler: Coordinates set, but no mesh is..");
		
		int n = da_dims[0];
		int m = 3;
		if (da_dimensions != 2 || da_dims[1] != 3)
			throw new SAXException ("GiftiXMLHandler: Unexpected dimensions for mesh coordinates..");
		
		Mesh3D mesh = mesh_int.getMesh();
		
		if (da_type.equals("NIFTI_TYPE_FLOAT32")){
			float[] f_data = this.parseAsciiFloat(ascii_data);
			for (int i = 0; i < n; i++){
				float[] coords = new float[m];
				for (int j = 0; j < m; j++){
					if (da_index_order.equals("RowMajorOrder")){
						coords[j] = f_data[i * m + j];
					}else{
						coords[j] = f_data[j * n + i];
						}
					}
				mesh.addVertex(new Point3f(coords[0], coords[1], coords[2]));
				}
		}else if (da_type.equals("NIFTI_TYPE_INT32")){
			int[] i_data = this.parseAsciiInt(ascii_data);
			for (int i = 0; i < n; i++){
				float[] coords = new float[m];
				for (int j = 0; j < m; j++){
					if (da_index_order.equals("RowMajorOrder")){
						coords[j] = i_data[i * m + j];
					}else{
						coords[j] = i_data[j * n + i];
						}
					}
				mesh.addVertex(new Point3f(coords[0], coords[1], coords[2]));
				}
			}
		
	}
	
	/************************************************************
	 * Sets this mesh's faces from {@code b_data}. If faces already exist, they will be
	 * deleted.
	 * 
	 * @param b_data
	 * @throws SAXException
	 */
	protected void setSurfaceFaces(byte[] b_data) throws SAXException{
		
		if (mesh_int == null)
			throw new SAXException ("GiftiXMLHandler: Coordinates set, but no mesh is..");
		
		int n = da_dims[0];
		int m = 3;
		if (da_dimensions != 2 || da_dims[1] != 3)
			throw new SAXException ("GiftiXMLHandler: Unexpected dimensions for mesh faces..");
		
		ByteOrder order = ByteOrder.BIG_ENDIAN;
		if (da_endian.equals("LittleEndian"))
			order = ByteOrder.LITTLE_ENDIAN;
		
		Mesh3D mesh = mesh_int.getMesh();
		mesh.removeAllFaces();
		
		if (da_type.equals("NIFTI_TYPE_INT32")){
			// Convert bytes to ints
			for (int i = 0; i < n; i++){
				int[] indexes = new int[m];
				for (int j = 0; j < m; j++){
					if (da_index_order.equals("RowMajorOrder")){
						indexes[j] = arr2int(b_data, (i * m + j) * 4, order);
					}else{
						indexes[j] = arr2int(b_data, (j * n + i) * 4, order);
						}
					}
				if (!mesh.addFace(indexes[0], indexes[1], indexes[2])){
					throw new SAXException("GiftiXMLHandler: Bad indices [" + indexes[0] + ", " 
																			+ indexes[1] + ", "
																			+ indexes[2] + "].");
					}
				}
			}
		
	}
	
	protected void setSurfaceFaces(String ascii_data) throws SAXException{
		
		if (mesh_int == null)
			throw new SAXException ("GiftiXMLHandler: Coordinates set, but no mesh is..");
		
		int n = da_dims[0];
		int m = 3;
		if (da_dimensions != 2 || da_dims[1] != 3)
			throw new SAXException ("GiftiXMLHandler: Unexpected dimensions for mesh faces..");
		
		Mesh3D mesh = mesh_int.getMesh();
		
		if (da_type.equals("NIFTI_TYPE_INT32")){
			int[] i_data = parseAsciiInt(ascii_data);
			for (int i = 0; i < n; i++){
				int[] indexes = new int[m];
				for (int j = 0; j < m; j++){
					if (da_index_order.equals("RowMajorOrder")){
						indexes[j] = i_data[i * m + j];
					}else{
						indexes[j] = i_data[j * n + i];
						}
					}
				mesh.addFace(indexes[0], indexes[1], indexes[2]);
				}
			}
		
		
	}
	
	protected void addVertexData(String column_name, byte[] b_data) throws SAXException{
		
		if (mesh_int == null)
			throw new SAXException ("GiftiXMLHandler: Adding vertex data, but no mesh is set.");
		if (mesh_int.getVertexCount() == 0)
			throw new SAXException ("GiftiXMLHandler: Adding vertex data, but no mesh coordinates are set.");
		
		int n = da_dims[0];
		if (n != mesh_int.getVertexCount())
			throw new SAXException ("GiftiXMLHandler: Unexpected data size when adding vertex data (" + n + "!= " + mesh_int.getVertexCount() +  ".");
		
		if (da_dimensions != 1)
			throw new SAXException ("GiftiXMLHandler: Unexpected dimensions for vertex data (" + da_dimensions + " != 1)..");
		
		ByteOrder order = ByteOrder.BIG_ENDIAN;
		if (da_endian.equals("LittleEndian"))
			order = ByteOrder.LITTLE_ENDIAN;
		
		// Instantiate array of appropriate type
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>(n);
		for (int i = 0; i < n; i++){
			switch (da_type){
				case "NIFTI_TYPE_UINT8":
					values.add(new MguiShort());
					break;
				case "NIFTI_TYPE_INT32":
					values.add(new MguiInteger());
					break;
				case "NIFTI_TYPE_FLOAT32":
				default:
					values.add(new MguiFloat());
					break;
				
				}
			}
			
		for (int i = 0; i < n; i++){
			double value = Double.NaN;
			switch (da_type){
				case "NIFTI_TYPE_UINT8":
					value = uint2short(b_data[i]);
					break;
				case "NIFTI_TYPE_INT32":
					value = arr2int(b_data, i * 4, order);
					break;
				case "NIFTI_TYPE_FLOAT32":
				default:
					value = arr2float(b_data, i * 4, order);
					break;
				}
			
			values.get(i).setValue(value);
			}
		
		mesh_int.addVertexData(column_name, values);
		
	}
	
	protected void addVertexData(String column_name, String ascii_data) throws SAXException{
		
		
		
	}
	
	// Parse using white space
	private float[] parseAsciiFloat(String ascii_data) {
		
		StringTokenizer tokens = new StringTokenizer(ascii_data);
		float[] values = new float[tokens.countTokens()];
		
		int i = 0;
		while (tokens.hasMoreTokens())
			values[i++] = Float.valueOf(tokens.nextToken());
		
		return values;
	}
	
	// Parse using white space
	private int[] parseAsciiInt(String ascii_data) {
		
		StringTokenizer tokens = new StringTokenizer(ascii_data);
		int[] values = new int[tokens.countTokens()];
		
		int i = 0;
		while (tokens.hasMoreTokens())
			values[i++] = Integer.valueOf(tokens.nextToken());
		
		return values;
	}
	
	private short uint2short(byte b){
		return (short)(b & 0xff);
	}
	
	private float arr2float (byte[] buf, int pos, ByteOrder order){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(order);
		bb.put(buf, pos, 4);
		return bb.getFloat(0);
	}
	
	private double arr2double (byte[] buf, int pos, ByteOrder order){
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.order(order);
		bb.put(buf, pos, 8);
		return bb.getDouble(0);
	}
	
	private int arr2int (byte[] buf, int pos, ByteOrder order){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(order);
		bb.put(buf, pos, 4);
		return bb.getInt(0);
	}
	
	
}