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


package mgui.io.foreign.wunil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import mgui.geometry.Grid3D;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.domestic.shapes.ShapeIOException;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.domestic.shapes.VolumeInputOptions;

/********************************************************
 * Loader to load WUNIL IDF format volumes (University of Washington in St. Louis format used by Caret).
 * 
 * <p>This format consists of an ASCII header file and a separate raw binary image file
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class WunilVolumeLoader extends VolumeFileLoader {

	protected WunilDataset dataset;
	
	public WunilVolumeLoader(){
		
	}
	
	public WunilVolumeLoader(File file){
		setFile(file);
	}

	@Override
	public boolean setVolume3DBlocking(Volume3DInt volume, String column, int v, VolumeInputOptions options, ProgressUpdater progress) throws ShapeIOException {
		
		if (!hasDataset() && !loadDataset()) return false;
		
		try{
			dataset.readHeader();
			
			double[][][] data = dataset.readDoubleVol((short)v);
			if (!volume.hasColumn(options.input_column)){
				volume.addVertexData(options.input_column, options.transfer_type);
				}
			
			Grid3D grid = volume.getGrid();
			int x_size = grid.getSizeS();
			int y_size = grid.getSizeT();
			int z_size = grid.getSizeR();
			
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			int x = Math.min(x_size, data[0][0].length);
			int y = Math.min(y_size, data[0].length);
			int z = Math.min(z_size, data.length);
			
			if (progress != null){
				progress.setMinimum(0);
				progress.setMaximum(z);
				progress.update(0);
				}
			
			for (int k = 0; k < z; k++){
				int k0 = k;
				if (flipZ) k0 = z_size - k - 1;
				
				if (progress != null)
					progress.update(k);
			
			    for (int i = 0; i < x; i++)
			    	for (int j = 0; j < y; j++){
			    		int i0 = i;
			    		if (flipX) i0 = x - i - 1;
			    		int j0 = j;
			    		if (flipY) j0 = y - j - 1;
			    		
			    		double d = data[k][j][i];
			    		
			    		volume.setDatumAtVoxel(options.input_column, i0, j0, k0, d);
			    		
			    		min = Math.min(min, d);
					  	max = Math.max(max, d);
			    		}
				}
			
			
			WindowedColourModel cm = volume.getColourModel(options.input_column);
			 
			cm.setIntercept(min);
			cm.setScale(cm.data_size / max);
			VertexDataColumn v_column = volume.getVertexDataColumn(options.input_column);
			v_column.setDataMin(min);
			v_column.setDataMax(max);
			v_column.setColourMin(min, false);
			v_column.setColourMax(max, false);
		
			return true;
			
		}catch (IOException e){
			throw new ShapeIOException(e); 
			}
		
	}
	
	public WunilDataset getDataset(){
		try{
			return (WunilDataset)getVolumeMetadata();
		}catch (Exception e){
			e.printStackTrace();
			}
		return null;
	}
	
	public VolumeMetadata getVolumeMetadata() throws IOException, FileNotFoundException{
		if (dataFile == null) throw new IOException("WunilVolumeLoader: no file specified!");
		if (!hasDataset() && !loadDataset()) return null;
		dataset.readHeader();
		return dataset;
	}

	protected boolean hasDataset(){
		return dataset != null;
	}
	
	protected boolean loadDataset(){
		if (dataFile == null) return false;
		
		dataset = new WunilDataset(dataFile.getAbsolutePath());
		return true;
	}
	
}