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


package mgui.interfaces.neuro.imaging.camino;

import mgui.pipelines.JavaProcess;

/********************************************
 * Extends {@link JavaProcess} specifically for Camino processes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CaminoProcess extends JavaProcess {

	public CaminoProcess(String name, String main_class){
		this(name, main_class, null);
	}
	
	public CaminoProcess(String name, String main_class, String logger){
		super(name, logger);
		this.name = name;
		this.main_class = main_class;
		this.logger = logger;
	}
	
	
}