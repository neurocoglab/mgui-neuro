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


package mgui.io.domestic.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.network.CorticalNetworkMatrixInOptions.Format;
import mgui.neuro.components.cortical.simple.SimpleCorticalRegion;
import mgui.neuro.networks.CorticalNetwork;
import mgui.util.MathFunctions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/********************************
 * <p>Reads an ASCII matrix representing a connected network into a cortical network, assigning weights
 * based upon a specified range (mapped to the continuous range [0.0:1.0]). The expected format, where 
 * R_i is region i, w_ij is the weight for the directed connection from i to j, the matrix
 * elements are tab-delimited, and the diagonal is ignored and can therefore be any value, is:</p>  
 * 
 * R_1   R_2   ...  R_n<br>
 * 0     w_12  ...  w_1n<br>
 * w_21  0     ...  w_2n<br>
 * ...<br>
 * w_n1  w_n2  ...  0<br>
 * 
 * <p>Note this type of file does not contain any information about the regions themselves, only their
 * connectivity. TODO: implement or find more detailed file format for this (preferable XML)...</p>
 * 
 * @author Andrew Reid
 *
 */
public class CorticalNetworkMatrixLoader extends FileLoader {
	
	CorticalNetworkMatrixInOptions options = new CorticalNetworkMatrixInOptions();
	
	public CorticalNetworkMatrixLoader(){
		
	}
	
	public CorticalNetworkMatrixLoader(File input){
		setFile(input);
	}
	
	public CorticalNetworkMatrixLoader(File input, CorticalNetworkMatrixInOptions options){
		setFile(input);
		setOptions(options);
	}
	
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		CorticalNetwork network = getCorticalNetwork();
		return network;
	}
	
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		if (!(options instanceof CorticalNetworkMatrixInOptions)) return false;
		CorticalNetworkMatrixInOptions opts = (CorticalNetworkMatrixInOptions)options;
		setOptions(opts);
		//InterfaceDisplayPanel panel = InterfaceSession.getDisplayPanel(); // opts.displayPanel;
		boolean success = true;
		
		for (int i = 0; i < opts.files.length; i++){
			setFile(opts.files[i]);
			CorticalNetwork network = getCorticalNetwork();
			success &= (network != null);
			if (network != null){
				InterfaceSession.getWorkspace().addModel(network);
				if (opts.createGraph)
					InterfaceSession.getWorkspace().addGraph(network.getGraph());
				}
			}
		
		return success;
	}
	
	public void setOptions(CorticalNetworkMatrixInOptions options){
		if (options == null) return;
		this.options = options;
	}
	
	public CorticalNetwork getCorticalNetwork(){
		
		boolean is_xml = (options.format == Format.XML);
		if (options.format == Format.Detect){
			//Try detecting file type
			try{
				BufferedReader reader = new BufferedReader(new FileReader(dataFile));
				String line = reader.readLine();
				is_xml = line.startsWith("<?xml");
				reader.close();
			}catch (IOException ex){
				InterfaceSession.log("CorticalNetworkMatrixLoader: could not open source file '" +
									 dataFile.getAbsolutePath() + "'.", LoggingType.Errors);
				return null;
				}
			}
		
		if (is_xml){
			try{
				XMLReader reader = XMLReaderFactory.createXMLReader();
				CorticalNetworkXMLHandler handler = new CorticalNetworkXMLHandler(options);
				reader.setContentHandler(handler);
				reader.setErrorHandler(handler);
				reader.parse(new InputSource(new FileReader(dataFile)));
				return handler.getCorticalNetwork();
			}catch (SAXException ex){
				InterfaceSession.log(ex.getLocalizedMessage(), LoggingType.Errors);
				return null;
			}catch (IOException ex){
				InterfaceSession.log(ex.getLocalizedMessage(), LoggingType.Errors);
				return null;
				}
			}
		
		try{
			CorticalNetwork network = new CorticalNetwork();
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			//load and create cortical regions
			String line = reader.readLine();
			
			if (line == null){
				reader.close();
				System.out.println("File '" + dataFile.getAbsolutePath() + "' has no data...");
				return null;
				}
			
			ArrayList<SimpleCorticalRegion> regions = new ArrayList<SimpleCorticalRegion>();
			StringTokenizer tokens = new StringTokenizer(line, "\t");
		
			while (tokens.hasMoreElements()){
				String name = tokens.nextToken();
				//SimpleCorticalRegion region = new SimpleCorticalRegion(name);
				SimpleCorticalRegion region = options.getRegion(name);
				network.addRegion(region, false);
				regions.add(region);
				}
			
			//for each line of input, add connections to region
			line = reader.readLine();
			
			int i = 0, j;
			String element;
			while (line != null){
				j = 0;
				tokens = new StringTokenizer(line, "\t");
				
				while (tokens.hasMoreElements()){
					element = tokens.nextToken();
					if (i != j){
						double weight = 0;
						if (options.setWeights)
							if (options.normalizeWeights)
								weight = MathFunctions.normalize(options.min, options.max, Double.valueOf(element).doubleValue());
							else
								weight = Double.valueOf(element).doubleValue();
						else
							if (Double.valueOf(element).doubleValue() > 0) weight = 1;
						if (weight > 0 || options.addAllConnections)
							regions.get(i).connectTo(regions.get(j), weight);
						}
					j++;
					}
				i++;
				line = reader.readLine();
				}
			reader.close();
			return network;
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}
		
	}
	

}