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


package mgui.io.foreign.fsl;

import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.GridVertexDataColumn;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.domestic.shapes.ShapeIOException;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.domestic.shapes.VolumeInputOptions;
import mgui.numbers.MguiNumber;

/************************************************************
 * Loader for a FSL-style volume file (mgh/mgz).
 * 
 * <p>See <a href="http://ftp.nmr.mgh.harvard.edu/fswiki/FsTutorial/MghFormat">
 * http://ftp.nmr.mgh.harvard.edu/fswiki/FsTutorial/MghFormat</a>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FSLVolumeLoader extends VolumeFileLoader {

	public FSLVolumeLoader(){
		
	}
	
	public FSLVolumeLoader(File file){
		this.setFile(file);
	}
	
	@Override
	public VolumeMetadata getVolumeMetadata() throws IOException, FileNotFoundException {
		
		FSLVolumeMetadata metadata = new FSLVolumeMetadata();
		metadata.readHeader(dataFile);
		
		return metadata;
	}

	@Override
	protected boolean setVolume3DBlocking(Volume3DInt volume, String column,
										  int v, VolumeInputOptions options, ProgressUpdater progress)
												  throws ShapeIOException {
		
		FSLVolumeMetadata metadata = new FSLVolumeMetadata();
		
		try{
			metadata.readHeader(dataFile);
			DataType data_type = null;
			
			switch(options.transfer_type){
				case DataBuffer.TYPE_USHORT:
					data_type = DataTypes.getType(DataTypes.USHORT);
					break;
				case DataBuffer.TYPE_SHORT:
					data_type = DataTypes.getType(DataTypes.SHORT);
					break;
				case DataBuffer.TYPE_INT:
					data_type = DataTypes.getType(DataTypes.INTEGER);
					break;
				case DataBuffer.TYPE_FLOAT:
					data_type = DataTypes.getType(DataTypes.FLOAT);
					break;
				case DataBuffer.TYPE_DOUBLE:
				default:
					data_type = DataTypes.getType(DataTypes.DOUBLE);
					break;
				}
			
			ArrayList<MguiNumber> values = metadata.readVolume(dataFile, v, data_type, progress);
			double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
			for (int i = 0; i < values.size(); i++){
				double d = values.get(i).getValue();
				min = Math.min(min, d);
				max = Math.max(max, d);
				}
			volume.addVertexData(column, values, options.colour_map);
			volume.hasAlpha(options.has_alpha);
			volume.setCurrentColumn(column, false);
			
			GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(column);
			WindowedColourModel cm = v_column.getColourModel();
			
			cm.setIntercept(min);
			  if (max > min)
				  cm.setScale(1.0 / (max - min));
			  else
				  cm.setScale(1.0);
			cm.setWindowMid(0.5);
			cm.setWindowWidth(1.0);
			v_column.setDataMin(min, false);
			v_column.setDataMax(max, false);
			v_column.setColourMin(min, false);
			v_column.setColourMax(max, false);
			
			InterfaceSession.log("Scale: " + cm.getScale() + " Intercept: " + cm.getIntercept(), LoggingType.Debug);
			InterfaceSession.log("Min: " + min + " Max: " + max, LoggingType.Debug);
			  
			return true;
		
		}catch (IOException ex){
			throw new ShapeIOException(ex);
			}
		
	}


}