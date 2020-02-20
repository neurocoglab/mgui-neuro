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


package mgui.io.domestic.network;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.vecmath.Point3f;

import mgui.neuro.components.cortical.AbstractCorticalConnection;
import mgui.neuro.components.cortical.AbstractCorticalRegion;
import mgui.neuro.components.cortical.simple.SimpleCorticalRegion;
import mgui.neuro.networks.CorticalNetwork;
import mgui.util.MathFunctions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/********************************************
 * Handler for XML encoding of cortical networks.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CorticalNetworkXMLHandler extends DefaultHandler {

	private CorticalNetwork current_network, last_network;
	private ArrayList<AbstractCorticalRegion> regions;
	private AbstractCorticalRegion current_region;
	private boolean in_connections = false;
	
	private CorticalNetworkMatrixInOptions options = new CorticalNetworkMatrixInOptions();
	private int conn_i;
	
	public CorticalNetworkXMLHandler(){
		super();
	}
	
	public CorticalNetworkXMLHandler(CorticalNetworkMatrixInOptions options){
		super();
		this.options = options;
	}
	
	/***********************************
	 * Returns the last network loaded by a call to <code>parse</code>.
	 * 
	 * @return
	 */
	public CorticalNetwork getCorticalNetwork(){
		return this.last_network;
	}
	
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) 
																			throws SAXException {
		
		//System.out.println("Debug, Start element: " + name);
		
		if (localName.equals("CorticalNetwork")){
			if (this.current_network != null)
				throw new SAXException("CorticalNetworkXMLHandler: Network already started.");
			
			String n_name = attributes.getValue("name");
			if (n_name == null) n_name = "No-name";
			this.current_network = new CorticalNetwork(name);
			return;
			}
		
		//TODO: metadata
		
		if (localName.equals("Regions")){
			if (this.regions != null)
				throw new SAXException("CorticalNetworkXMLHandler: Regions already started.");
			
			if (this.current_network == null)
				throw new SAXException("CorticalNetworkXMLHandler: No network started...");
			
			this.regions = new ArrayList<AbstractCorticalRegion>();
			
			return;
			}
		
		if (localName.equals("CorticalRegion")){
			if (this.regions == null)
				throw new SAXException("CorticalNetworkXMLHandler: Regions not started.");
			
			String attr = attributes.getValue("name");
			if (attr == null) attr = "No-name";
			this.current_region = new SimpleCorticalRegion(attr);
			float x=0, y=0, z=0;
			attr = attributes.getValue("x");
			if (attr != null) x = Float.valueOf(attr);
			attr = attributes.getValue("y");
			if (attr != null) y = Float.valueOf(attr);
			attr = attributes.getValue("z");
			if (attr != null) z = Float.valueOf(attr);
			
			current_region.setLocation(new Point3f(x, y, z));
			
			return;
			}
		
		if (localName.equals("Connections")){
			if (this.in_connections == true)
				throw new SAXException("CorticalNetworkXMLHandler: Connections already started.");
			
			in_connections = true;
			conn_i = 0;
			return;
			}
		
		
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		//System.out.println("Debug, End element: " + name);
		
		if (localName.equals("CorticalNetwork")){
			if (this.current_network == null)
				throw new SAXException("CorticalNetworkXMLHandler: Network termination but " +
									   "no network started...");
			
			//add regions and connections to current network
			if (this.regions != null){
				current_network.addRegions(regions, false);
				}
			
			this.last_network = this.current_network;
			this.current_network = null;
			return;
			}
		
		if (this.current_network == null)
			throw new SAXException("CorticalNetworkXMLHandler: No network started...");
		
		if (localName.equals("Regions")){
			if (this.regions == null)
				throw new SAXException("CorticalNetworkXMLHandler: Regions terminated but not started.");
			
			if (this.current_network == null)
				throw new SAXException("CorticalNetworkXMLHandler: No network started...");
			
			return;
			}
		
		if (localName.equals("CorticalRegion")){
			if (this.current_network == null)
				throw new SAXException("CorticalNetworkXMLHandler: No network started...");
			if (this.regions == null)
				throw new SAXException("CorticalNetworkXMLHandler: Regions not started.");
			if (this.current_region == null)
				throw new SAXException("CorticalNetworkXMLHandler: No region started."); 
			
			this.regions.add(current_region);
			current_region = null;
			return;
			}
		
		if (localName.equals("Connections")){
			if (this.current_network == null)
				throw new SAXException("CorticalNetworkXMLHandler: No network started...");
			if (in_connections == false)
				throw new SAXException("CorticalNetworkXMLHandler: Connections not started.");
			
			in_connections = false;
			return;
			}
		
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		//System.out.println("Debug, Characters: " + String.valueOf(ch));
		
		if (in_connections){
			
			//add connection
			String line = new String(ch, start, length);
			line = line.trim();
			if (line.length() == 0) return;
			
			if (regions.size() <= conn_i)
				throw new SAXException("CorticalNetworkXMLHandler: Connection matrix too large.");
			
			int j = 0;
			StringTokenizer tokens = new StringTokenizer(line, "\t");
			
			while (tokens.hasMoreElements()){
				if (regions.size() <= j)
					throw new SAXException("CorticalNetworkXMLHandler: Connection matrix too large.");
				String element = tokens.nextToken();
				if (conn_i != j){
					double weight = 0;
					if (options.setWeights)
						if (options.normalizeWeights)
							weight = MathFunctions.normalize(options.min, options.max, Double.valueOf(element).doubleValue());
						else
							weight = Double.valueOf(element).doubleValue();
					else
						if (Double.valueOf(element).doubleValue() > 0) weight = 1;
					
					if (weight > 0 || options.addAllConnections)
						regions.get(conn_i).connectTo(regions.get(j), weight);
					}
				j++;
				}
			conn_i++;
			
			return;
			}
		
	}
	
}