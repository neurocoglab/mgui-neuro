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


package mgui.neuro.components.cortical;

import java.util.Comparator;

import javax.vecmath.Point3f;

import mgui.neuro.components.AbstractNeuroComponent;


/****************************
 * Represents a component at the resolution of a cortical region. This superclass is a
 * generic representation which defines the geometry of the region. Subclasses should
 * provide specific dynamic model implementations of the region.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public abstract class AbstractCorticalRegion extends AbstractNeuroComponent {
	
	protected Point3f location = new Point3f();
	
	protected void init(){
		super.init();
		//attributes.add(new Attribute("Name", "no-name"));
	}
	
	public void setLocation(Point3f pt){
		this.location.set(pt);
	}
	
	public Point3f getLocation(){
		return new Point3f(location);
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public static Comparator<AbstractCorticalRegion> getComparator(){
		return new Comparator<AbstractCorticalRegion>(){
				public int compare(AbstractCorticalRegion r1, AbstractCorticalRegion r2){
					return r1.getName().compareTo(r2.getName());
				}
			};
	}
	
	public static Comparator getStrComparator(){
		return new Comparator(){
				public int compare(Object r1, Object r2){
					String s1 = null, s2 = null;
					if (r1 instanceof AbstractCorticalRegion)
						s1 = ((AbstractCorticalRegion)r1).getName();
					if (r1 instanceof String)
						s1 = (String)r1;
					if (r2 instanceof AbstractCorticalRegion)
						s2 = ((AbstractCorticalRegion)r2).getName();
					if (r2 instanceof String)
						s2 = (String)r2;
					if (s1 == null || s2 == null) return 0; //unpredicable if type is wrong
					return s1.compareTo(s2);
				}
			};
	}
	
	public abstract void connectTo(AbstractCorticalRegion region, double weight);
	public abstract AbstractCorticalOutput getOutput();
}