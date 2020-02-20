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


package mgui.neuro.imaging.camino.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import mgui.interfaces.unix.UnixFunctions;
import mgui.neuro.imaging.camino.CaminoFunctions;

import tools.CL_Initializer;
import data.OutputManager;

/*******************************************
 * Wraps class UnixFunctions.cat, in order to set correct directories from the
 * Camino environment. 
 * 
 * @author Andrew Reid
 *
 */

public class CaminoCatTask {

	public static void main(String[] args){
		
		String output_file = null;
		String input_dir = null;
		
		int skip = -1;
		
		//CL_Initializer.CL_init(args);
		for (int i = 0; i < args.length; i++){
			if (args[i].equals("-outputfile"))
				output_file = args[i + 1];
			if (args[i].equals("-inputfile")){
				input_dir = args[i + 1];
				skip = i;
				}
			}
		
		if (output_file == null){
			System.out.println("CaminoCatTask: no output file specified..");
			return;
			}
		//String output_file = OutputManager.outputFile;
		//remove subject suffix
		
		//String input_dir = CL_Initializer.inputFile;
		if (input_dir == null) input_dir = System.getProperty("user.dir");
		if (input_dir.lastIndexOf(File.separator) > 0)
			input_dir = input_dir.substring(0, input_dir.lastIndexOf(File.separator));
		
		//insert 
		//ArrayList<String> args_list = new ArrayList<String>();
		//args_list.addAll(Arrays.asList(args));
		
		//add directory to args
		//int add = 0;
		//if (output_file != null) add = 2;
		
		String[] new_args = new String [args.length - 2];
		new_args[0] = "-outputfile";
		new_args[1] = output_file;
		
		int j = 2;
		for (int i = 2; i < args.length; i++)
			if (i < skip || i > skip + 1)
				new_args[j++] = input_dir + File.separator + args[i];
		
		UnixFunctions.cat.main(new_args);
		
	}
	
	
	
}