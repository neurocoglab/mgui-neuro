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


package mgui.neuro.imaging.camino;

import inverters.ModelIndex;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.Utility;
import mgui.interfaces.neuro.imaging.camino.CaminoProcess;
import mgui.io.util.IoFunctions;
import mgui.io.util.ParallelOutputStream;
import mgui.pipelines.PipelineProcessInstance;
import simulation.SimulationParams;
import sphfunc.BasisSumFactory;
import tools.CL_Initializer;
import data.OutputManager;
import foxtrot.Worker;

public class CaminoFunctions extends Utility {

	public static String output_file;
	public static String root_dir;
	public static String input_file;
	public static String temp_input = "temp.0", temp_output = "temp.1";
	
	public static int temp_count = 0;
	public static boolean fail_on_exception = true;
	
	static boolean taskInterrupted;
	//public static foxtrot.Task current_task;
	
	private static boolean init_once = false;
	private static boolean task_failed = false;
	
	public static synchronized boolean launchCaminoProcess(final CaminoProcess command, 
			  									 		   final String args){

		setTaskFailed(false);
		setTaskInterrupted (false);
		resetDefaults();
		foxtrot.Task current_task = new foxtrot.Task() {
	
		public Boolean run() throws Exception{
		
			Class[] argTypes = new Class[1];
			argTypes[0] = String[].class;
			String rem = args.trim();
			
			//get ouput if specified
			int o = rem.indexOf(">");
			if (o > 0){
				String file = rem.substring(o + 1);
				file = file.trim();
				//trim quotes
				if (file.startsWith("\"")){
					file = file.substring(1, file.length() - 1).trim();
					}
				OutputManager.outputFile = file;
				rem = rem.substring(0, o).trim();
				}
			
			Vector<String> v = new Vector<String>();
			while (rem != null && rem.length() > 0){
				int i = rem.indexOf(" ");
				int j = rem.indexOf("\"");
				if (j >= 0 && j < i){
					int k = rem.indexOf("\"", j + 1);
					if (k <= j){
						System.out.println("Error parsing Camino arguments: '" + args + "'");
						return new Boolean(false);
						}
					v.add(rem.substring(j + 1, k).trim());
					if (k >= rem.length() - 2) break;
					rem = rem.substring(k + 2).trim();
					i = rem.indexOf(" ");
					}
				if (i <= 0 || i == rem.length() - 1){
					v.add(rem);
					break;
					}
				v.add(rem.substring(0, i).trim());
				rem = rem.substring(i + 1).trim();
				}
			
			String[] _args = new String[v.size()];
			for (int i = 0; i < v.size(); i++)
				_args[i] = v.get(i);
			
			try{
				Method main_method = Class.forName(command.getMainClass()).
									getDeclaredMethod("main", argTypes);
				
				if (main_method == null){
					System.out.println("Exception running Camino process '" + command.toString() + " " +
						args + "'");
					return new Boolean(false);
					}
				
				main_method.invoke(null, new Object[]{_args});
			
			}catch (Exception e){
				System.out.println("Exception running Camino process '" + command + " " +
				args);
				e.printStackTrace();
				return new Boolean(false);
				}
			
			return new Boolean(true);
			}
		};
		
		resetDefaults();
		
		try{
			return (Boolean)Worker.post(current_task);
		}catch (Exception e){
			e.printStackTrace();
			return false;
			}

	}
	
	public static void init(){
		if (init_once) return;
		Logger log = Logger.getLogger("camino.apps.OutputManager");
		log.addHandler(new ConsoleHandler());
		log = Logger.getLogger("camino.tools.CL_Initializer");
		log.addHandler(new ConsoleHandler());
		init_once = true;
	}
	
	public static void reset(){
		input_file = null;
		output_file = null;
		temp_count = 0;
		taskInterrupted = false;
	}
	
	public static boolean launchCaminoProcess(final PipelineProcessInstance process){
		return launchCaminoProcess(process, "", "", null, false);
	}
	
	public static boolean launchCaminoProcess(final PipelineProcessInstance process, boolean blocking){
		return launchCaminoProcess(process, "", "", null, blocking);
	}
	
	public static boolean stopExecution(){
		//if (current_task == null) return false;
		setTaskInterrupted(true);
		return true;
	}
	
	public static boolean launchCaminoProcess(final PipelineProcessInstance process, 
															 final String subject, 
															 final String root){
		return launchCaminoProcess(process, subject, root, null, false);
	}
	
	public static boolean launchCaminoProcess(final PipelineProcessInstance process, 
															 final String subject, 
															 final String root, 
															 final String logger){
		return launchCaminoProcess(process, subject, root, logger, false);
	}
	
	public static boolean launchCaminoProcess(final PipelineProcessInstance process, 
															 final String subject, 
															 final String root, 
															 final String logger,
															 final boolean blocking){
		
		if (blocking)
			return launchBlockingCaminoProcess(process, subject, root, logger);
		
		foxtrot.Task current_task = new foxtrot.Task(){
			
			public Boolean run() throws Exception{
				return launchBlockingCaminoProcess(process, subject, root, logger);
				}
			
			};
		
		boolean success = false;
		try{
			success = (Boolean)Worker.post(current_task);
		}catch (Exception e){
			e.printStackTrace();
			success = false;
			}
		
		
		
		return success;
	}
	
	public static boolean launchBlockingCaminoProcess(final PipelineProcessInstance process, 
													  final String subject, 
													  final String root, 
													  final String logger){
		
		//TODO: add System.err listener to catch any errors in running processes
		ErrorStreamListener error_stream = null;
		
		if (fail_on_exception){
			error_stream = new ErrorStreamListener();
			error_stream.start();
			}
		
		setTaskFailed(false);
		setTaskInterrupted(false);
		root_dir = root;
		
		if (root_dir == null){
			System.out.println("No root directory specified");
			return false;
			}
		
		resetDefaults();
		boolean copy_output = true;
		
		final String sep = File.separator;
		
		if (process.hasInput()){
			if (input_file == null)
				input_file = temp_input;
		}else{
			input_file = null;
			}
		//else
		//	copy_input = true;
		
		if (output_file == null)
			output_file = temp_output;
		else
			copy_output = true;
		
		updateLogger();
		
		Class[] argTypes = new Class[1];
		argTypes[0] = String[].class;
		
		try{
			String[] args = process.getArguments(subject, null);
			
			System.out.print("Processing " + process.toString() + ": ");
			
			if (process.hasInput())
				args = setInput(args, root_dir + File.separator + input_file);
			//if (output_file != null)
				args = setOutput(args, root_dir + File.separator + output_file);
			
			process.getProcess().updateLogger();
			String main_class = ((CaminoProcess)process.getProcess()).getMainClass();
			
			Method main_method = Class.forName(main_class).getDeclaredMethod("main", argTypes);
			
			if (main_method == null){
				System.out.println("Exception running process (no main method).");
				resetDefaults();
				if (fail_on_exception)
					error_stream.close();
				return false;
				}
			
			System.out.println("Command line arguments for '" + main_class + "." + main_method.getName() + "': " + getArgString(args));
			main_method.invoke(null, new Object[]{args});
			
		}catch (Exception e){
			System.out.println("Exception running process."); // '" + process.toString() + "'.");
			resetDefaults();
			e.printStackTrace();
			if (fail_on_exception)
				error_stream.close();
			return false;
			}
		
		resetDefaults();
		if (fail_on_exception)
			error_stream.close();
		if (isTaskInterrupted()){
			System.out.println("User interrupted process."); // '" + process.toString() + "'.");
			resetDefaults();
			return false;
			}
		if (isTaskFailed()){
			System.out.println("Task failed: '" + process.toString() + "'.");
			resetDefaults();
			return false;
			}
		
		System.out.println("Success.");
		
		//copy output file if necessary (so it is available for next process)
		if (copy_output){
			File file = new File (root_dir + sep + output_file);
			File temp = new File (root_dir + sep + temp_input);
			try{
				if (!temp.exists() && !temp.createNewFile()){
					System.out.println("Cannot create temp output file '" + root_dir + sep + temp_input +"'");
					return false;
					}
				if (file.exists() && temp.exists()){
					IoFunctions.copyFile(file, temp);
					//System.out.println("Copied '" + file.getAbsolutePath() + "' to '" + temp.getAbsolutePath() + "'..");
					}
			}catch (IOException ex){
				System.out.println("Cannot copy to temp input file '" + root_dir + sep + temp_input +"'");
				ex.printStackTrace();
				return false;
				}
		}else{
			//otherwise rename temp output to input
			File temp_in = new File(root_dir + sep + temp_input);
			if (temp_in.exists() && !temp_in.delete()){
				System.out.println("Cannot delete temp file '" + temp_in.getAbsolutePath() +"'");
				return false;
				}
			File temp_out = new File(root_dir + sep + temp_output);
			if (temp_out.exists() && !temp_out.renameTo(temp_in)){
				System.out.println("Cannot rename temp file '" + temp_out.getAbsolutePath() +"'");
				return false;
				}
			}

		return true;
	}
	
	private static String[] setInput(String args[], String input_file){
		
		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-inputfile")){
				args[i + 1] = input_file;
				return args;
				}
		
		String[] args2 = new String[args.length + 2];
		args2[0] = "-inputfile";
		args2[1] = input_file;
		System.arraycopy(args, 0, args2, 2, args.length);
		return args2;
	}
	
	private static String[] setOutput(String args[], String output_file){
		
		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-outputfile")){
				args[i + 1] = output_file;
				return args;
				}
		
		String[] args2 = new String[args.length + 2];
		args2[0] = "-outputfile";
		args2[1] = output_file;
		System.arraycopy(args, 0, args2, 2, args.length);
		return args2;
	}
	
	private static void updateLogger(){
		Logger logger = Logger.getLogger("camino");
		Handler[] handlers = logger.getHandlers();
		
		for (int i = 0; i < handlers.length; i++)
			if (handlers[i] instanceof ConsoleHandler) return;
		
		logger.addHandler(new ConsoleHandler());
	}
	
	static boolean isTaskInterrupted(){
		return taskInterrupted;
	}
	
	static boolean isTaskFailed(){
		return task_failed;
	}

	static private void setTaskInterrupted(boolean value){
	      taskInterrupted = value;
	}
	
	static private void setTaskFailed(boolean value){
	      task_failed = value;
	}
	
	public static String getArgString(String[] args){
		String s = "";
		for (int i = 0; i < args.length; i++)
			if (i > 0)
				s = s + ",'" + args[i] + "'";
			else
				s = "'" + args[i] + "'";
		return s;
	}
	
	public static ArrayList<String> parseArg(String arg){
		return parseArg(arg, "", "");
	}
	
	/************************************************
	 * 
	 * Since Camino was built to be run from the command line, its static CL_Initializer and OutputManager classes
	 * are meant to be reinstantiated upon each function call. Since we want to call Camino functions directly, from 
	 * within the same JVM instance, we need to initialize the variables manually to their defaults, which many 
	 * processes assume.
	 * 
	 *****************/
	public static void resetDefaults(){
		//sphfunc.ISCodes.directory = "/" + (CaminoEnvironment.camino_root + "/ISCodes/").replace("/", "\\");
		
		CL_Initializer.testFunction = -1;
		CL_Initializer.nonStandardTestFunction = false;
		CL_Initializer.simParams= null;
		CL_Initializer.DIFF_CONST=2.02E-9; // m^2 s^{-1}
		CL_Initializer.volFrac = 1;
		CL_Initializer.dataDims = new int[]{0,0,0};
		CL_Initializer.voxelDims = new double[]{0,0,0};
		CL_Initializer.centreDist=0;
		CL_Initializer.testFunctionData = new double[]{};
		CL_Initializer.inversionIndices = new ModelIndex[] {ModelIndex.LDT};
		CL_Initializer.rotationIndex = 0;
		CL_Initializer.numVoxels = 0;
//		CL_Initializer.sequenceIndex = 0;
//		CL_Initializer.sequenceIndexSet = false;
//		CL_Initializer.diffusionTime = -1;
		CL_Initializer.lambda1 = -1;
		CL_Initializer.dt2rotangle = 0;
		CL_Initializer.dt2mix = -1;
		CL_Initializer.scale = -1;
		CL_Initializer.numElements=-1;
		CL_Initializer.M = -1;
		CL_Initializer.N = -1;
//		CL_Initializer.modQ = -1;
//		CL_Initializer.qScale = 1.0;
//		CL_Initializer.tauScale = 1.0;
		CL_Initializer.SNR = -1;
		CL_Initializer.bootstrap = -1;
		CL_Initializer.wildBootstrapModel = null;
		CL_Initializer.bsDataFiles = null;
		CL_Initializer.seed = 36558013;
		CL_Initializer.voxelClassMap = null;
		CL_Initializer.maxTensorComponents = 2;
		CL_Initializer.gamma_k=1.84037;
		CL_Initializer.gamma_beta= 7.8E-7;
		CL_Initializer.classifiedModelIndices = new ModelIndex[][]
	    {{ModelIndex.LDT}, {ModelIndex.LDT}, {ModelIndex.LDT}, 
	     {ModelIndex.POSPOS, ModelIndex.LDT}};
		CL_Initializer.inputFile = null;
		CL_Initializer.bgMaskFile = null;
		CL_Initializer.schemeFile = null;
		CL_Initializer.sigma = -1;
		CL_Initializer.noiseType = "rician";
		CL_Initializer.outlierMapFile = null;
		CL_Initializer.noiseVarianceMapFile = null;
		CL_Initializer.residualVectorMapFile = null;
		CL_Initializer.matrixFile = null;
		CL_Initializer.lrNormalize = false;
		CL_Initializer.lrLog = false;
		CL_Initializer.inputDataType = "float";
		CL_Initializer.bgMaskType = "short";
		CL_Initializer.inputModel = null;
		CL_Initializer.basisType = BasisSumFactory.TUCH_RBF;
		CL_Initializer.pointSetInd = 3;
		CL_Initializer.pointSetIndSet = false;
		CL_Initializer.numPDsIO = 3;
		CL_Initializer.maxOrder = 4;
		CL_Initializer.f1 = -1;
		CL_Initializer.f2 = -1;
		CL_Initializer.f3 = -1;
		CL_Initializer.kernelParams = new double[]{ 0.0, 1.0 };
		CL_Initializer.mePointSet = -1;
//		CL_Initializer.skipEvery = -1;
		CL_Initializer.BACKGROUNDTHRESHOLD = 0.0;
		CL_Initializer.CSFTHRESHOLD = -1.0;
		CL_Initializer.imPars = null;
		CL_Initializer.p = null;
		CL_Initializer.data = null;
		CL_Initializer.bgMask = null;
		CL_Initializer.transformFile = null;
		CL_Initializer.transformFileX = null;
		CL_Initializer.transformFileY = null;
		CL_Initializer.transformFileZ = null;
		CL_Initializer.reorientation = "ppd";
		
		
		OutputManager.outputDataType = "double";
		OutputManager.outputFile = null;
		OutputManager.gzipOut = false;
		
		
		SimulationParams.sim_separate_runs = false;
		SimulationParams.sim_inflamm_increments = 10;
		SimulationParams.sim_N_walkers= 10000;
		SimulationParams.sim_tmax= 100000;
		SimulationParams.sim_p= 0.0;
		SimulationParams.sim_initial= SimulationParams.UNIFORM;
//		SimulationParams.sim_geomType= SubstrateFactory.CYL_1_FIXED;
		SimulationParams.sim_L=20.0;
		SimulationParams.sim_l=1.0;
		SimulationParams.sim_stripethickness=3;
		SimulationParams.sim_p_perc=0.5;
//		SimulationParams.sim_fixedFrac=0.75;
//		SimulationParams.sim_modFill=4;
//		SimulationParams.sim_modFree=1;
		SimulationParams.sim_voxelSize= 10.0;
//		SimulationParams.sim_stepType= StepGeneratorFactory.FIXEDLENGTH;
		SimulationParams.sim_delta_set=false;
		SimulationParams.sim_DELTA_set=false;
		SimulationParams.sim_G_set=false;
//		SimulationParams.sim_cyl_pack = ParallelCylinderSubstrate.HEX;
		SimulationParams.sim_cyl_dist_size=20;
		SimulationParams.sim_cyl_min_r=0.0;
		SimulationParams.sim_cyl_max_r=2E-5;
		SimulationParams.sim_cyl_r = 1E-5;
		SimulationParams.sim_cyl_R = 3E-5;
		SimulationParams.sim_cyl_r1= SimulationParams.sim_cyl_r/2.0;
		SimulationParams.sim_cyl_D1= CL_Initializer.DIFF_CONST;
		SimulationParams.sim_cyl_D2= CL_Initializer.DIFF_CONST;
		SimulationParams.sim_cAngle= Math.PI/2;
		SimulationParams.sim_num_cylinders=20;
		SimulationParams.sim_num_facets=0;
		SimulationParams.sim_plyfile=null;
		SimulationParams.sim_spatial_grid_size= 10;
//		SimulationParams.sim_amender_type=StepAmenderFactory.ELESTIC_REFLECTOR;
		
		
	}
	
	
	public static ArrayList<String> parseArg(String arg, String subject, String root){
		
		ArrayList<String> tokens = new ArrayList<String>();
		
		char[] chars = arg.toCharArray();
		String thisToken = "";
		boolean in_quotes = false;
		boolean in_brackets = false;
		String replace = null;
		
		//TODO: allow calculations/expressions...?
		for (int i = 0; i < chars.length; i++){
			switch (chars[i]){
				case '{':
					replace = "";
					in_brackets = true;
					break;
				case '}':
					if (in_brackets){
						if (replace.toLowerCase().equals("subject"))
							thisToken = thisToken + subject;
						if (replace.toLowerCase().equals("root"))
							thisToken = thisToken + root;
						}
					replace = null;
					in_brackets = false;
					break;
				case '\"':
					if (in_quotes){
						if (thisToken.length() > 0)
							tokens.add(thisToken);
						thisToken = "";
						in_quotes = false;
					}else{
						in_quotes = true;
						thisToken = "";
						}
					break;
				case ' ':
					if (!in_quotes){
						if (thisToken.length() > 0)
							tokens.add(thisToken);
						thisToken = "";
					}else{
						thisToken = thisToken + " ";
						}
					break;
				default:
					if (in_brackets)
						replace = replace + String.valueOf(chars[i]);
					else
						thisToken = thisToken + String.valueOf(chars[i]);
				}
			}
		if (thisToken.length() > 0) tokens.add(thisToken);
		return tokens;
	}
	
	 public static boolean deleteTempFiles(){
		
		File temp_in = new File(root_dir + File.separator + temp_input);
		if (temp_in.exists() && !temp_in.delete()){
			System.out.println("Cannot delete temp file '" + temp_in.getAbsolutePath() +"'");
			return false;
			}
		File temp_out = new File(root_dir + File.separator + temp_output);
		if (temp_out.exists() && !temp_out.delete()){
			System.out.println("Cannot delete temp file '" + temp_out.getAbsolutePath() +"'");
			return false;
			}
		
		return true;
	 }
	 
	 public static void exceptionEncountered() {
		 if (!fail_on_exception) return;
		 System.out.println("CaminoFunctions: Exception encountered...");
		 System.exit(0);
		 setTaskFailed(true);
	 }

	 static class ErrorStreamListener implements Runnable {
		 
		PipedInputStream error_input_stream;
		PipedOutputStream error_output_stream;
		Thread error_reader;
		boolean quit = false;
		
		public ErrorStreamListener(){
			error_input_stream = new PipedInputStream();
			try{
				error_output_stream = new PipedOutputStream(error_input_stream);
				ParallelOutputStream out_stream = InterfaceEnvironment.getSystemErrorStream();
				out_stream.addStream(error_output_stream);
			}catch (IOException e){
				e.printStackTrace();
				}
		}
		
		public void start(){
			error_reader = new Thread(this);
			error_reader.setDaemon(true);	
			error_reader.start();
		}
		
		public synchronized void run(){
			try {
				while (Thread.currentThread() == error_reader){
					try {
						this.wait(100);
					}catch(InterruptedException ie) {}
					
					if (error_input_stream.available() != 0){
						CaminoFunctions.exceptionEncountered();
						}
					if (quit) return;
					}
				
			}catch (Exception e){
				CaminoFunctions.exceptionEncountered();
				return;
				}
		}
		
		public void close(){
			quit = true;
			if (error_output_stream != null){
				try{
					error_output_stream.close();
					InterfaceEnvironment.getSystemErrorStream().removeStream(error_output_stream);
				}catch (IOException e){
					e.printStackTrace();
					}
				}
		}
		 
	 }
	
	
}