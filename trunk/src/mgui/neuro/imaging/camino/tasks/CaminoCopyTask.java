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


package mgui.neuro.imaging.camino.tasks;

import mgui.interfaces.unix.UnixFunctions;
import misc.LoggedException;

public class CaminoCopyTask {

	public static void main(String[] args){
		
		String in_file = null, out_file = null;
		
		for (int i = 0; i < args.length; i++){
			if (args[i].equals("-inputfile"))
				in_file = args[i + 1];
			if (args[i].equals("-outputfile"))
				out_file = args[i + 1];
			}
		
		if (in_file == null || out_file == null){
			System.out.println("CaminoCopyTask: must specify inputfile and outputfile..");
			return;
			}
		
		try{
			UnixFunctions.cp.main(new String[]{in_file, out_file});
		}catch (Exception e){
			LoggedException.logExceptionWarning(e, Thread.currentThread().getName());
			}
		
	}
	
}