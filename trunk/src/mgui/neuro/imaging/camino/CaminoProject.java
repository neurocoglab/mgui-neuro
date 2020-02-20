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


package mgui.neuro.imaging.camino;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.pipelines.InterfacePipeline;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;


public class CaminoProject implements IconObject, 
									  XMLObject,
									  Serializable{

	public String name;
	public String subject_prefix = "";
	public File root_directory;
	public ArrayList<InterfacePipeline> pipelines = new ArrayList<InterfacePipeline>();
	
	public ArrayList<String> subjects = new ArrayList<String>();
	public ArrayList<String> subdirs = new ArrayList<String>();
	
	public CaminoProject(String name, File root_directory){
		this.name = name;
		this.root_directory = root_directory;
		setSubjects();
		setSubdirs();
	}
	
	public CaminoProject(String name, String root_directory){
		this(name, new File(root_directory));
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/ar/resources/icons/camino/project_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			System.out.println("Cannot find resource: /ar/resources/icons/camino/project_17.png");
		return null;
	}
	
	public Icon getSelectedIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/ar/resources/icons/camino/selected_project_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			System.out.println("Cannot find resource: /ar/resources/icons/camino/selected_project_17.png");
		return null;
	}
	
	public boolean applySubjects(boolean remove){
		if (root_directory == null) return false;
		File dir = root_directory;
		String[] list = dir.list(IoFunctions.getDirFilter());
		
		TreeSet<String> set = new TreeSet<String>(Arrays.asList(list));
		ArrayList<String> to_add = new ArrayList<String>();
		ArrayList<String> to_remove = new ArrayList<String>();
		
		for (int i = 0; i < subjects.size(); i++)
			if (!set.contains(subjects.get(i)))
				to_add.add(subjects.get(i));
		
		if (remove){
			set = new TreeSet<String>(subjects);
			for (int i = 0; i < list.length; i++)
				if (!set.contains(list[i]))
					to_remove.add(list[i]);
			}
		
		boolean success = true;
		for (int i = 0; i < to_add.size(); i++){
			File subdir = new File(dir.getAbsolutePath() + File.separator + to_add.get(i));
			if (!subdir.exists()) 
				success &= subdir.mkdir();
			}
		
		for (int i = 0; i < to_remove.size(); i++){
			File subdir = new File(dir.getAbsolutePath() + File.separator + to_remove.get(i));
			if (subdir.exists()) 
				success &= IoFunctions.deleteDir(subdir);
			}
		
		return success;
	}
	
	public boolean applySubdirs(boolean remove){
		if (root_directory == null) return false;
		File dir = root_directory;
		
		boolean success = true;
		TreeSet<String> project_subdirs = new TreeSet<String>(subdirs);
		
		//since TreeSet is sorted
		for (int i = 0; i < subjects.size(); i++){
			File subject_dir = new File(dir.getAbsolutePath() + File.separator + subjects.get(i));
			if (subject_dir.exists()){
				TreeSet<String> existing_subdirs = new TreeSet<String>(IoFunctions.getSubdirs(subject_dir, true));
				for (int j = 0; j < subdirs.size(); j++)
					if (!existing_subdirs.contains(subdirs.get(j))){
						File f = new File(subject_dir.getAbsolutePath() + File.separator + subdirs.get(j));
						success &= f.mkdir();
						}
				if (remove){
					for (String this_dir : existing_subdirs)
						if (!project_subdirs.contains(this_dir)){
							File f = new File(dir.getAbsoluteFile() + File.separator + subjects.get(i) + File.separator + this_dir);
							if (f.exists() && f.isDirectory()) 
								success &= IoFunctions.deleteDir(f);
							}
					}
				}
			}
		
		return success;
		
	}
	
	public boolean updateFileSystem(boolean remove){
		return applySubjects(remove) && applySubdirs(remove);
	}
	
	public void setSubjects(){
		if (root_directory == null) return;
		File dir = root_directory;
		String[] list = dir.list(IoFunctions.getDirFilter());
		subjects = new ArrayList<String>();
		
		for (int i = 0; i < list.length; i++)
			subjects.add(list[i]);
	}
	
	public void setSubdirs(){
		if (root_directory == null) return;
		if (subjects == null) setSubjects();
		File dir = root_directory;
		String sep = System.getProperty("file.separator");
		TreeSet<String> set = new TreeSet<String>();
		
		for (int i = 0; i < subjects.size(); i++){
			File subdir = new File(dir.getAbsolutePath() + sep + subjects.get(i));
			ArrayList<String> subdirs = IoFunctions.getSubdirs(subdir, true);
			set.addAll(subdirs);
			//String[] subsubs = subdir.list(IOFunctions.getDirFilter());
			//for (int j = 0; j < subsubs.length; j++)
			//	set.add(subsubs[j]);
			}
		
		subdirs = new ArrayList<String>(set);
	}
	
	public void addPipeline(InterfacePipeline pipeline){
		pipelines.add(pipeline);
		pipeline.setRootDirectory(root_directory);
	}
	
	public void removePipeline(InterfacePipeline pipeline){
		pipelines.remove(pipeline);
	}
	
	public File getRootDirectory(){
		return root_directory;
	}
	
	public DefaultMutableTreeNode getTreeNode(JTree tree){
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(this);
		
		for (int i = 0; i < pipelines.size(); i++)
			root.add(pipelines.get(i).issueTreeNode());
		
		return root;
	}
	
	public String toString(){
		return name;
	}
	
	public String getDTD() {
		return "";
	}

	public String getLocalName() {
		return "CaminoProject";
	}

	public String getShortXML(int tab) {
		return null;
	}

	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = _tab + "<CaminoProject " +
		"name = '" + name + 
		"' root_dir = '" + root_directory +
		"' prefix = '" + subject_prefix + "'>\n";
		
		xml = xml + _tab2 + "<Pipelines>\n";
		
		for (int i = 0; i < pipelines.size(); i++)
			xml = xml + pipelines.get(i).getXML(tab + 2);
		
		xml = xml + _tab2 + "</Pipelines>\n";
		xml = xml + _tab + "</CaminoProject>\n";
		
		return xml;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}

	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		writer.write(getXML(tab));
	}

	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null);
	}
	
}