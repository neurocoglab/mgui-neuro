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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.graphics.video.xml.VideoXMLHandler;
import mgui.interfaces.neuro.imaging.camino.CaminoProcess;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.domestic.pipelines.PipelineProcessLibraryXMLHandler;
import mgui.pipelines.PipelineProcess;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class CaminoEnvironment {

	protected static ArrayList<PipelineProcess> processes = new ArrayList<PipelineProcess>();
	
	static String parent = "";
	public static String camino_root = "";
	
	public static boolean initFromFile(String file){
		/*
		File init_file = null;
		
		if (file.startsWith(File.separator)){
			File current = new File(".");
			String path = current.getAbsolutePath();
			path = path.substring(0, path.lastIndexOf(File.separator));
			init_file = new File(path + file);
		}else{
			init_file = new File(file);
			}
		*/
		
		java.net.URL url = InterfaceEnvironment.class.getResource(file);
		File init_file = null;
		try{
			init_file =  new File(url.toURI());
		}catch(Exception e){
			System.out.println("CaminoEnvironment: could not open '" + url.toString() + "'..");
			return false;
			}
		
		//if (!init_file.exists()){
		//	System.out.println("CaminoEnvironment: init file '" + init_file.getAbsolutePath() + "' does not exist..");
		//	return false;
		//	}
		
		parent = init_file.getParent();
		String debug = parent;
		String sep = File.separator;
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(init_file));
			String line = reader.readLine();
			
			while (line != null){
				StringTokenizer tokens = new StringTokenizer(line);
				if (line.indexOf(" ") > 0){
					String command = line.substring(0, line.indexOf(" "));
					
					if (command.equals("setCaminoRoot")){
						if (line.contains("\""))
							camino_root = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
						else
							camino_root = line.substring(line.indexOf(" ") + 1);
						}
					
					if (command.equals("loadEnvironment")){
						String file_str = line.substring(line.indexOf(" ") + 1);
						if (file_str.startsWith(sep)) file_str = file_str.substring(1);
						if (file_str.endsWith(sep)) file_str = file_str.substring(0, file_str.lastIndexOf(sep));
						
						loadEnvironment(file_str);
						}
					}
				
				
				line = reader.readLine();
				}
		
		
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
		
		return true;
	}
	
	public static void loadEnvironment(String file_name){
		
		File file = new File(parent + File.separator + file_name);
		
		if (!file.exists()){
			System.out.println("CaminoEnvironment: processes file '" + file_name + "' does not exist..");
			return;
			}
		
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			PipelineProcessLibraryXMLHandler handler = new PipelineProcessLibraryXMLHandler();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(file)));
			
			for (int i = 0; i < handler.processes.size(); i++)
				if (handler.processes.get(i) instanceof CaminoProcess)
					registerProcess((CaminoProcess)handler.processes.get(i));
			
		}catch (Exception e){
			e.printStackTrace();
			}
		
		
	}
	
	public static boolean writeToXML(BufferedWriter writer){
		
		try{
			writer.write(XMLFunctions.getXMLHeader() + "\n");
			
			writer.write("<CaminoEnvironment>\n");
			writer.write("\t<Processes>\n");
			
			for (int i = 0; i < processes.size(); i++)
				processes.get(i).writeXML(2, writer);
			
			writer.write("\t</Processes>\n");
			writer.write("</CaminoEnvironment>");
			
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
			}
		
	}
	
	public static ArrayList<PipelineProcess> getProcesses(){
		return processes;
	}
	
	public static PipelineProcess getProcess(String name){
		for (int i = 0; i < processes.size(); i++)
			if (processes.get(i).getName().equals(name))
				return processes.get(i);
		return null;
	}
	
	public static void registerProcess(CaminoProcess process){
		processes.add(process);
	}
	
	public static boolean deregisterProcess(CaminoProcess process){
		return processes.remove(process);
	}
	
}