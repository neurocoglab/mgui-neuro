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


package mgui.io.foreign.wunil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.datasources.DataType;
import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.standard.nifti.Nifti1Dataset;
import mgui.io.standard.nifti.util.NiftiFunctions;
import mgui.io.util.EndianCorrectInputStream;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;

/****************************************************
 * Extension of {@link Nifti1Dataset} to allow data to be read from the WUNIL *.ifh format (used with
 * Caret). Also handles Caret metadata for region names; i.e., uses them to fill a {@link NameMap}. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class WunilDataset extends Nifti1Dataset implements VolumeMetadata {


	//This is why private fields suck
	//String 				ds_hdrname;	// file name for header
	//String 				ds_datname;	// file name for data
	//public short		dim[];		// data array dimensions (8 shorts)
	//short				datatype;	// datatype of image blob
	//short				bitpix;		// #bits per voxel
	public float[] 		origin;
	public NameMap		name_map;
	protected Vector3f axis_s, axis_t, axis_r;
	
	public WunilDataset(){
		super();		//sets defaults
		
		setDefaults();
	}
	
	public WunilDataset(String filename){
		super();		//sets defaults
		
		setDefaults();
		
		String lc_name = filename.toLowerCase();
		
		if (filename.endsWith(".ifh")){
			setHeaderFilename(lc_name);
			lc_name = lc_name.substring(0, lc_name.lastIndexOf(".")) + ".img";
			setDataFilename(lc_name);
		}else if (filename.endsWith(".img")){
			setDataFilename(lc_name);
			lc_name = lc_name.substring(0, lc_name.lastIndexOf(".")) + ".ifh";
			setHeaderFilename(lc_name);
		}else{
			System.out.println("Invalid file name for WUNIL volume: '" + filename + "'.");
			}
		
		
	}
	
	@Override
	public void setFromMetadata(VolumeMetadata metadata){
		
		this.setOrigin(metadata.getOrigin());
		this.setDataType(metadata.getDataType());
		this.setDataDims(metadata.getDataDims());
		this.setGeomDims(metadata.getGeomDims());
		this.setAxes(metadata.getAxes());
		
	}
	
	@Override
	public Vector3f[] getAxes() {
		if (axis_s == null){
			float[] dims = getGeomDims();
			axis_s = new Vector3f(dims[0],0,0);
			axis_t = new Vector3f(0,dims[1],0);
			axis_r = new Vector3f(0,0,dims[2]);
			}
		return new Vector3f[]{axis_s, axis_t, axis_r};
	}

	@Override
	public void setAxes(Vector3f[] axes) {
		axis_s = axes[0];
		axis_t = axes[1];
		axis_r = axes[2];
		setGeomDims(new float[]{axis_s.length(), axis_t.length(), axis_r.length()});
	}
	
	@Override
	public int getVoxelDim() {
		return dim[4];
	}

	@Override
	public void setVoxelDim(int t) {
		dim[4] = (short)t;
	}
	
	@Override
	public void setFromVolume(Volume3DInt volume){
		String column = volume.getCurrentColumn();
		if (column == null) return;
		setFromVolume(volume, column);
	}
	
	@Override
	public void setFromVolume(Volume3DInt volume, String column){
		
		Grid3D grid = volume.getGrid();
		this.setDataDims(new int[]{grid.getSizeS(), grid.getSizeT(), grid.getSizeR(), grid.getSizeV()});
		this.setGeomDims(new float[]{grid.getGeomS(), grid.getGeomT(), grid.getGeomR()});
		this.setOrigin(grid.getBasePt());
		this.setDataType(volume.getDataType(column));
		
	}
	
	private void setDefaults(){
		
		dim = new short[8];
		origin = new float[3];
		
	}
	
	public void setHeaderFilename(String s) {
		ds_hdrname = s;
	}
	
	public void setDataFilename(String s) {
		ds_datname = s;
	}
	
	@Override
	public ArrayList<MguiNumber> readVolume(int t, DataType data_type, ProgressUpdater progress) throws IOException {
		
		// TODO: read directly from file and avoid this memory-intensive step
		double[][][] d_vals = readDoubleVol((short)t);
		
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>(dim[1] * dim[2] * dim[3]);
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(dim[1] * dim[2] * dim[3]);
			progress.update(0);
			}
		
		int p = 0;
		for (int i = 0; i < dim[1]; i++)
			for (int j = 0; j < dim[2]; j++)
				for (int k = 0; k < dim[3]; k++){
					if (progress != null)
						progress.update(p++);
					values.add(NumberFunctions.getInstance(data_type, d_vals[i][j][k]));
				}
		
		return null;
	}
	
	/**
	* Read one 3D volume from disk and return it as 3D double array.
	* 
	* <p>NB. this is copied verbatim from <code>Nifti1Dataset</code>, since it makes its fields private...
	* 
	* @param ttt T dimension of vol to read (0 based index)
	* @return 3D double array, scale and offset have been applied.
	* Array indices are [Z][Y][X], assuming an xyzt ordering of dimensions.
	* ie  indices are data[dim[3]][dim[2]][dim[1]]
	* 
	* @exception IOException
	* 
	*/
	public double[][][] readDoubleVol(short ttt) throws IOException {
		
		double data[][][];
		byte b[];
		EndianCorrectInputStream ecs;
		short ZZZ;
		int i,j,k;
		
		XDIM = dim[1];
		YDIM = dim[2];
		ZDIM = dim[3];
		
		// for 2D volumes, zdim may be 0
		ZZZ = ZDIM;
		if (dim[0] == 2)
			ZZZ = 1;


		// allocate 3D array
		data = new double[ZZZ][YDIM][XDIM];

		// read bytes from disk
		b = readVolBlob(ttt);

		// read the correct datatypes from the byte array
		// undo signs if necessary, add scaling
		ecs = new EndianCorrectInputStream(new ByteArrayInputStream(b),big_endian);
		switch (datatype) {

			case NIFTI_TYPE_INT8:
			case NIFTI_TYPE_UINT8:
				for (k=0; k<ZZZ; k++)
				for (j=0; j<YDIM; j++)
				for (i=0; i<XDIM; i++) {
					data[k][j][i] = (double) (ecs.readByte());
					if ((datatype == NIFTI_TYPE_UINT8) && (data[k][j][i] < 0) )
						data[k][j][i] = Math.abs(data[k][j][i]) + (double)(1<<7);
					if (scl_slope != 0)
						data[k][j][i] = data[k][j][i] * scl_slope + scl_inter;
				}
				break;

			case NIFTI_TYPE_INT16:
			case NIFTI_TYPE_UINT16:
				for (k=0; k<ZZZ; k++)
				for (j=0; j<YDIM; j++)
				for (i=0; i<XDIM; i++) {
					data[k][j][i] = (double) (ecs.readShortCorrect());
					if ((datatype == NIFTI_TYPE_UINT16) && (data[k][j][i] < 0))
						data[k][j][i] = Math.abs(data[k][j][i]) + (double)(1<<15);
					if (scl_slope != 0)
						data[k][j][i] = data[k][j][i] * scl_slope + scl_inter;
				}
				break;

			case NIFTI_TYPE_INT32:
			case NIFTI_TYPE_UINT32:
				for (k=0; k<ZZZ; k++)
				for (j=0; j<YDIM; j++)
				for (i=0; i<XDIM; i++) {
					data[k][j][i] = (double) (ecs.readIntCorrect());
					if ( (datatype == NIFTI_TYPE_UINT32) && (data[k][j][i] < 0) )
						data[k][j][i] = Math.abs(data[k][j][i]) + (double)(1<<31);
					if (scl_slope != 0)
						data[k][j][i] = data[k][j][i] * scl_slope + scl_inter;
				}
				break;


			case NIFTI_TYPE_INT64:
			case NIFTI_TYPE_UINT64:
				for (k=0; k<ZZZ; k++)
				for (j=0; j<YDIM; j++)
				for (i=0; i<XDIM; i++) {
					data[k][j][i] = (double) (ecs.readLongCorrect());
					if ( (datatype == NIFTI_TYPE_UINT64) && (data[k][j][i] < 0) )
						data[k][j][i] = Math.abs(data[k][j][i]) + (double)(1<<63);
					if (scl_slope != 0)
						data[k][j][i] = data[k][j][i] * scl_slope + scl_inter;
				}
				break;


			case NIFTI_TYPE_FLOAT32:
				for (k=0; k<ZZZ; k++)
				for (j=0; j<YDIM; j++)
				for (i=0; i<XDIM; i++) {
					data[k][j][i] = (double) (ecs.readFloatCorrect());
					if (scl_slope != 0)
						data[k][j][i] = data[k][j][i] * scl_slope + scl_inter;
				}
				break;


			case NIFTI_TYPE_FLOAT64:
				for (k=0; k<ZZZ; k++)
				for (j=0; j<YDIM; j++)
				for (i=0; i<XDIM; i++) {
					data[k][j][i] = (double) (ecs.readDoubleCorrect());
					if (scl_slope != 0)
						data[k][j][i] = data[k][j][i] * scl_slope + scl_inter;
				}
				break;


			case DT_NONE:
			case DT_BINARY:
			case NIFTI_TYPE_COMPLEX64:
			case NIFTI_TYPE_FLOAT128:
			case NIFTI_TYPE_RGB24:
			case NIFTI_TYPE_COMPLEX128:
			case NIFTI_TYPE_COMPLEX256:
			case DT_ALL:
			default:
				throw new IOException("Sorry, cannot yet read nifti-1 datatype "+decodeDatatype(datatype));
			}

		ecs.close();
		b = null;


		return(data);
	}
	
	//NB. copied verbatim from Nifti1Dataset since it is private
	private byte[] readVolBlob(short ttt) throws IOException {

		byte b[];
		RandomAccessFile raf;
		BufferedInputStream bis;
		short ZZZ;
		long skip_head, skip_data;
		int blob_size;
		
		// for 2D volumes, zdim may be 0
		ZZZ = dim[3];
		if (dim[0] == 2)
			ZZZ = 1;

		blob_size = dim[1]*dim[2]*ZZZ*bytesPerVoxel(datatype);
		b = new byte[blob_size];

		skip_head= 0; //(long)vox_offset;  //no header
		skip_data = (long) (ttt*blob_size);

		// read the volume from disk into a byte array
		// read compressed data with BufferedInputStream
		if (ds_datname.endsWith(".gz")) {
		bis = new BufferedInputStream(new GZIPInputStream(new FileInputStream(ds_datname)));
		bis.skip(skip_head+skip_data);
		bis.read(b,0,blob_size);
		bis.close();
		}
		// read uncompressed data with RandomAccessFile
		else {
		raf = new RandomAccessFile(ds_datname, "r");
		raf.seek(skip_head+skip_data);
		raf.readFully(b,0,blob_size);
		raf.close();
		}

		return b;
		}
	
	/**
	* Read header information into memory.
	* 
	* @exception IOException 
	* @exception FileNotFoundException
	*/
	public void readHeader() throws IOException, FileNotFoundException {
		
		//read successive lines and assign header values
		BufferedReader reader = new BufferedReader(new FileReader(this.ds_hdrname));
		String line = reader.readLine();
		float[] center = null, center_sign = null;
		
		boolean is_metadata = false;
		
		while (line != null){
			
			if (line.contains(":=")){
				
				int div = line.indexOf(":=");
				String element = line.substring(0, div);
				String value = line.substring(div + 3);
				
				if (!is_metadata){
					if (element.startsWith("number format"))
						this.datatype = getFormat(value);
					
					if (element.startsWith("number of bytes per pixel"))
						bitpix = (short)(Integer.valueOf(value).shortValue() * 8);	//weird
					
					if (element.startsWith("number of dimensions"))
						dim[0] = Integer.valueOf(value).shortValue();
					
					if (element.startsWith("imagedata byte order"))
						big_endian = (value.equals("bigendian")); 
						
					
					for (int a = 0; a < 3; a++){
						if (element.startsWith("scaling factor (mm/pixel) [" + (a + 1) + "]"))
							pixdim[a + 1] = Float.valueOf(value);
						}
					
					for (int a = 0; a < dim[0]; a++){
						if (element.startsWith("matrix size [" + (a + 1) + "]"))
							dim[a + 1] = Integer.valueOf(value).shortValue();
						}
					
					if (element.startsWith("center"))
						center = getFloatArray(value);
					
					if (element.startsWith("mmppix"))		//not sure what this is all about
						center_sign = getFloatArray(value);
					
					if (element.startsWith("caret_metadata"))
						is_metadata = true;
				
				}else{
					
					if (element.startsWith("region names"))
						addNameMapElement(value);
					
					}
				}
			
			line = reader.readLine();
			}
		
		//set origin
		if (center != null && center_sign != null){
			origin = new float[3];
			for (int a = 0; a < 3; a++)
				origin[a] = -(((float)dim[a + 1] - (center[a] * center_sign[a]) - 0.5f) * pixdim[a + 1]);
			}
		
	}
	
	private void addNameMapElement(String s){
		StringTokenizer tokens = new StringTokenizer(s);
		
		//add 2 because... yeah.
		if (name_map == null) name_map = new NameMap();
		name_map.add(Integer.valueOf(tokens.nextToken()) + 2, tokens.nextToken());
		
	}
	
	private float[] getFloatArray(String s){
		StringTokenizer tokens = new StringTokenizer(s);
		return new float[]{Float.valueOf(tokens.nextToken()), 
						   Float.valueOf(tokens.nextToken()),
						   Float.valueOf(tokens.nextToken())};
	}
	
	private short getFormat(String s){
		
		s = s.toLowerCase();
		
		if (s.equals("float"))
			return NIFTI_TYPE_FLOAT32;
		if (s.equals("int"))
			return NIFTI_TYPE_INT32;
		
		return NIFTI_TYPE_FLOAT32;
		//others?
		
	}
	
	@Override
	public int[] getDataDims() {
		int[] dims = new int[4];
		for (int i = 0; i < 4; i++)
			dims[i] = dim[i + 1];
		return dims;
	}

	@Override
	public int getDataType() {
		return NiftiFunctions.getDataType(datatype);
	}

	@Override
	public float[] getGeomDims() {
		float xDim = dim[1];
		float yDim = dim[2];
		float zDim = dim[3];
		float xSpace = pixdim[1] * xDim;
		float ySpace = pixdim[2] * yDim;
		float zSpace = pixdim[3] * zDim;
		
		return new float[]{xSpace, ySpace, zSpace};
	}

	@Override
	public Point3f getOrigin() {
		return new Point3f(origin[0], origin[1], origin[2]);
	}
	
	@Override
	public Box3D getBounds(){
		
		//no rotation support..?
		Point3f origin = getOrigin();
		float[] geom = getGeomDims();
		
		Box3D box = new Box3D(origin,
							  new Vector3f(geom[0], 0f, 0f),
							  new Vector3f(0f, geom[1], 0f),
							  new Vector3f(0f, 0f, geom[2]));
		
		return box;
	}
	
	@Override
	public void setDataDims(int[] dims) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDataType(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGeomDims(float[] dims) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOrigin(Point3f origin) {
		// TODO Auto-generated method stub
		
	}
	
}