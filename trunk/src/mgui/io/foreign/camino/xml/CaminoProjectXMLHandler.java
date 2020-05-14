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


package mgui.io.foreign.camino.xml;

import mgui.io.domestic.pipelines.PipelineXMLHandler;
import mgui.neuro.imaging.camino.CaminoProject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class CaminoProjectXMLHandler extends DefaultHandler {

	CaminoProject project;
	
	PipelineXMLHandler pipeline_handler;
	
	public CaminoProjectXMLHandler(){
		super();
	}
	
	public CaminoProject getProject(){
		return project;
	}
	
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		
		if (pipeline_handler != null){
			pipeline_handler.startElement(uri, localName, name, attributes);
			return;
			}
		
		if (localName.equals("CaminoProject")){
			project = new CaminoProject(attributes.getValue("name"),
										attributes.getValue("root_dir"));
			
			return;
			}
		
		if (localName.equals("Pipeline")){
			if (project == null)
				throw new SAXException("CaminoProjectXMLHandler: attempt to add pipeline before creating project..");
			pipeline_handler = new PipelineXMLHandler();
			pipeline_handler.startElement(uri, localName, name, attributes);
			return;
			}
		
		
	}
	
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		if (localName.equals("Pipeline")){
			if (pipeline_handler != null){
				pipeline_handler.endElement(uri, localName, name);
				project.addPipeline(pipeline_handler.getLastPipeline());
				pipeline_handler = null;
				return;
				}
			return;
		}
		
		if (pipeline_handler != null){
			pipeline_handler.endElement(uri, localName, name);
			return;
			}
		
	}
	
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		
		
	}
	
	
}