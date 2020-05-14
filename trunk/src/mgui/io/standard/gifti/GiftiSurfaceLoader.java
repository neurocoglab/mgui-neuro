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
import java.util.ArrayList;

import mgui.geometry.Mesh3D;
import mgui.geometry.mesh.MeshFunctionException;
import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import mgui.io.domestic.shapes.SurfaceInputOptions;
import mgui.io.standard.gifti.xml.GiftiXMLHandler;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import Jama.Matrix;

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
			try{
				Mesh3DInt mesh = loadSurface(progress_bar, s_options);
				if (mesh == null) return false;
				
				if (!s_options.merge_shapes){
					if (s_options.current_mesh == null)
						mesh.setName(s_options.names[i]);
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
			XMLReader reader = XMLReaderFactory.createXMLReader();
			GiftiXMLHandler handler = new GiftiXMLHandler(_options.current_mesh);
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
			Mesh3DInt mesh_int = handler.getMesh();
			ArrayList<Matrix> transforms = handler.getTransforms();
			if (_options.apply_transforms && transforms.size() > 0){
				Mesh3D mesh = mesh_int.getMesh();
				for (int i = 0; i < transforms.size(); i++)
					GeometryFunctions.transform(mesh, transforms.get(i));
				mesh_int.setMesh(mesh);
				}
			
			return mesh_int;
			
		}catch (Exception e){
			InterfaceSession.log("GiftiSurfaceLoader: error loading file '" + dataFile.getAbsolutePath() + 
								 "';\nDetails: " + e.getMessage(), 
								 LoggingType.Errors);
			return null;
			}
	
	}
	
	@Override
	public InterfaceIOType getWriterComplement(){
		return (new GiftiSurfaceWriter()).getIOType();
	}

}