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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import Jama.Matrix;
import mgui.geometry.Mesh3D;
import mgui.geometry.mesh.MeshFunctionException;
import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import mgui.io.standard.gifti.GiftiOutputOptions.NiftiIntent;
import mgui.io.standard.gifti.xml.GiftiXMLHandler;

/***********************************************************
 * Loads an XML-format GIFTI file. See <a href="http://www.nitrc.org/projects/gifti/">
 * http://www.nitrc.org/projects/gifti/</a> for details and specification.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class GiftiSurfaceLoader extends SurfaceFileLoader {

	public GiftiSurfaceLoader(){
		
	}
	
	public GiftiSurfaceLoader(File file){
		setFile(file);
	}
	
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		if (!(options instanceof GiftiInputOptions)) return false;
		GiftiInputOptions s_options = (GiftiInputOptions)options;
		if (s_options.shapeSet == null || s_options.files == null) return false;
		ArrayList<Mesh3D> merge_shapes = null;
		if (s_options.merge_shapes)
			merge_shapes = new ArrayList<Mesh3D>();
		for (int i = 0; i < s_options.files.length; i++){
			setFile(s_options.files[i]);
			setObjectName(s_options.names[i]);
			try{
				Mesh3DInt mesh = loadSurface(progress_bar, s_options);
				if (mesh == null) return false;
				
				if (!s_options.merge_shapes){
//					if (s_options.current_mesh == null)
//						mesh.setName(s_options.names[i]);
					s_options.shapeSet.addShape(mesh);
				}else{
					merge_shapes.add(mesh.getMesh());
					}
			}catch (IOException ex){
				return false;
				}
			}
		if (s_options.merge_shapes && merge_shapes.size() > 1){
			try{
				Mesh3DInt mesh = new Mesh3DInt(MeshFunctions.mergeMeshes(merge_shapes));
				mesh.setName(s_options.merge_name);
				s_options.shapeSet.addShape(mesh);
			}catch (MeshFunctionException ex){
				InterfaceSession.log("SurfaceFileLoader: Could not merge meshes.\nDetails:" +
									 ex.getMessage(), 
									 LoggingType.Errors);
				return false;
				}
			}
		return true;
	}
	
	
	@Override
	public Mesh3DInt loadSurface(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		
		GiftiInputOptions _options = (GiftiInputOptions)options;
		
		if (_options == null){
			InterfaceSession.log("GiftiSurfaceLoader: Options not set, using defaults.", LoggingType.Warnings);
			_options = (GiftiInputOptions)this.getIOType().getOptionsInstance();
			}
		
		if (dataFile == null){
			InterfaceSession.log("GiftiSurfaceLoader: Input file not set..");
			return null;
			}
		
		if (!dataFile.exists()){
			InterfaceSession.log("GiftiSurfaceLoader: Input file '" + dataFile.getAbsolutePath() + "' not found.");
			return null;
			}
		
		if (progress_bar != null){
			progress_bar.setIndeterminate(true);
			}
		
		try{
			//XMLReader reader = XMLReaderFactory.createXMLReader();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader reader = sp.getXMLReader();
			reader.setEntityResolver(new DtdResolver());
			//reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			GiftiXMLHandler handler = new GiftiXMLHandler(_options.current_mesh);
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
			Mesh3DInt mesh_int = handler.getMesh();
			mesh_int.setName(getObjectName());
			ArrayList<Matrix> transforms = handler.getTransforms();
			if (_options.apply_transforms && transforms.size() > 0){
				Mesh3D mesh = mesh_int.getMesh();
				for (int i = 0; i < transforms.size(); i++)
					GeometryFunctions.transform(mesh, transforms.get(i));
				mesh_int.setMesh(mesh);
				}
			
			ColourMap cmap = handler.label_cmap;
			if (cmap != null) {
				
				cmap.setName(getObjectName() + "_labels");
				
				// Add to environment
				if (InterfaceEnvironment.getColourMap(cmap.getName()) == null) {
					InterfaceEnvironment.addColourMap(cmap);
					}

				// Set for all label columns
				for (VertexDataColumn column : mesh_int.getVertexDataColumns()) {
					if (handler.nifti_intents.get(column.getName()) == NiftiIntent.NIFTI_INTENT_LABEL) {
						mesh_int.setColourMap(column.getName(), cmap);
						//column.setColourMap(cmap);
						}
					}
				
				
				
				}
			
			return mesh_int;
			
		}catch (Exception e){
//			InterfaceSession.log("GiftiSurfaceLoader: error loading file '" + dataFile.getAbsolutePath() + 
//								 "';\nDetails: " + e.getMessage(), 
//								 LoggingType.Errors);
			InterfaceSession.handleException(e);
			return null;
			}
	
	}
	
	class DtdResolver implements EntityResolver {
		   public InputSource resolveEntity (String publicId, String systemId) {
			   try {
				   if (systemId.equals("http://www.nitrc.org/frs/download.php/115/gifti.dtd")) {
			           	// return local DTD
				       	URL url = getClass().getClassLoader().getResource("mgui/resources/init/gifti/gifti.dtd");
				       	
				       	return new InputSource(url.toURI().toASCIIString());
				   } else {
			           	// use the default behaviour
				       	return null;
						}
			   } catch(URISyntaxException ex) {
				    InterfaceSession.handleException(ex);
			   		}
			   
			   return null;
		   }
	}
	
	@Override
	public InterfaceIOType getWriterComplement(){
		return (new GiftiSurfaceWriter()).getIOType();
	}

}