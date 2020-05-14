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

import java.util.ArrayList;

import data.OutputManager;
import tools.CL_Initializer;

public class DTFit {

	public static void main (String[] args){
		
		int inversion = 1;
		String scheme_file = null;
		String input_file = null;
		
		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-nonlinear"))
				inversion = 2;
			else if (args[i].equals("-schemefile"))
				scheme_file = args[i + 1];
			else if (args[i].equals("-inputfile"))
				input_file = args[i + 1];
		
		if (scheme_file == null){
			System.out.println("DTFit: scheme file not optional..");
			System.exit(0);
			}
		
		ArrayList<String> args2 = new ArrayList<String>();
		if (input_file != null){
			args2.add("-inputfile");
			args2.add(input_file);
			}
		
		args2.add("-schemefile");
		args2.add(scheme_file);
		args2.add("inversion");
		args2.add("" + inversion);
		args2.add("-inputfile");
		args2.add(CL_Initializer.inputFile);
		args2.add("-outputfile");
		args2.add(OutputManager.outputFile);
		
		args = new String[args2.size()];
		for (int i = 0; i < args2.size(); i++)
			args[i] = args2.get(i);
		
//		apps.ModelFit.main(args);
		
	}
	
	
}