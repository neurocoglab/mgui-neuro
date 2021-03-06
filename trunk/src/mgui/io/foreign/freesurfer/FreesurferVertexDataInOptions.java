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


package mgui.io.foreign.freesurfer;

import mgui.interfaces.maps.ColourMap;
import mgui.io.domestic.shapes.SurfaceDataInputOptions;

/**************************************************
 * Options for FreesurferVertexDataLoader. Format specifies which type to expect when loading.
 * Default is dense1 (old curvature format). Dense2 = new curv format.
 * 
 *  <p>See: <a href="http://www.grahamwideman.com/gw/brain/fs/surfacefileformats.htm">
 * http://www.grahamwideman.com/gw/brain/fs/surfacefileformats.htm</a>.
 * 
 * <p>See: <a href="http://surfer.nmr.mgh.harvard.edu/fswiki/LabelsClutsAnnotationFiles">
 * http://surfer.nmr.mgh.harvard.edu/fswiki/LabelsClutsAnnotationFiles</a>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FreesurferVertexDataInOptions extends SurfaceDataInputOptions {

	public enum Format{
		Annotation,
		Label,
		Dense1,
		Dense2,
		Sparse,
		Ascii;
	}
	
	protected Format format = null;
	
	public ColourMap colour_map = null;
	public boolean load_colour_map = true;
	public double missing_value = 0;
	public boolean fail_on_bad_rgb = false;
	
	public FreesurferVertexDataInOptions(){
		
	}
	
	public FreesurferVertexDataInOptions(Format format){
		this.format = format;
	}
	
	public Format getFormat(){
		return format;
	}
	
	public void setFormat(Format format){
		this.format = format;
	}

}