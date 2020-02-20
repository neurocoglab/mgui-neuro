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


package mgui.interfaces.shapes.neuro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.geometry.Mesh3D;
import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.neuro.mesh.NeuroMeshFunctions;
import mgui.geometry.neuro.mesh.ScalpAndSkullModelOptions;
import mgui.geometry.volume.VolumeFunctions;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import foxtrot.Job;
import foxtrot.Worker;


public class InterfaceNeuroMeshPanel extends InterfacePanel implements ActionListener {

	CategoryTitle lblScalpSkull = new CategoryTitle("SCALP/SKULL MODEL"); 
	JLabel lblScalpT1Volume = new JLabel("T1 Volume:");
	//JButton cmdScalpT1Volume = new JButton("Browse..");
	JComboBox cmbScalpT1Volume = new JComboBox();
	JLabel lblScalpMask = new JLabel("Brain mask:");
	JComboBox cmbScalpMask = new JComboBox();
	//JButton cmdScalpMaskVolume = new JButton("Browse..");
	JLabel lblScalpBrainSurface = new JLabel("Brain surface:");
	//JButton cmdScalpBrainSurface = new JButton("Browse..");
	JComboBox cmbScalpBrainSurface = new JComboBox();
	//JLabel lblScalpOutputDir = new JLabel("Output directory:");
	//JButton cmdScalpOutputDir = new JButton("Browse..");
	JLabel lblScalpSphereN = new JLabel("Sphere node N:");
	JTextField txtScalpSphereN = new JTextField("1000");
	JLabel lblScalpSample = new JLabel("Sample density (/mm):");
	JTextField txtScalpSample = new JTextField("5");
	JLabel lblScalpMinDistB_IS = new JLabel("Min dist B->ISk (mm):");
	JTextField txtScalpMinDistB_IS = new JTextField("1");
	JLabel lblScalpMinDistOS_S = new JLabel("Min dist OSk->Sc (mm):");
	JTextField txtScalpMinDistOS_S = new JTextField("5");
	JLabel lblScalpThreshold = new JLabel("Intensity threshold:");
	JTextField txtScalpThreshold = new JTextField("1000");
	JLabel lblScalpThresholdDist = new JLabel("Threshold distance (mm):");
	JTextField txtScalpThresholdDist = new JTextField("5");
	JLabel lblScalpMinSlope = new JLabel("Min slope:");
	JTextField txtScalpMinSlope = new JTextField("0.05");
	JLabel lblScalpPlane = new JLabel("Define ears & nasium plane:");
	JLabel lblScalpSectionSet = new JLabel(" Section set:");
	JComboBox cmbScalpSectionSet = new JComboBox();
	JLabel lblScalpSectionPlane = new JLabel(" Section:");
	JTextField txtScalpSectionPlane = new JTextField("0");
	JCheckBox chkScalpGaussian = new JCheckBox(" Gaussian smoothing");
	JLabel lblScalpSigmaNormal = new JLabel(" Sigma normal:");
	JTextField txtScalpSigmaNormal = new JTextField("1");
	JLabel lblScalpSigmaTangent = new JLabel(" Sigma tangent:");
	JTextField txtScalpSigmaTangent = new JTextField("4");
	JLabel lblScalpGaussianCutoff = new JLabel(" Cutoff (sigmas):");
	JTextField txtScalpGaussianCutoff = new JTextField("2");
	
	JLabel lblScalpShapeSet = new JLabel("Add results to:");
	JComboBox cmbScalpShapeSet = new JComboBox();
	JButton cmdScalpExecute = new JButton("Execute");
	
	
	CategoryTitle lblTests = new CategoryTitle("TESTS");
	JLabel lblSphere = new JLabel("Create sphere");
	JLabel lblSphereN = new JLabel("No. nodes:");
	JTextField txtSphereN = new JTextField("100");
	JLabel lblSphereCenter = new JLabel("Center:");
	JTextField txtSphereCenter_x = new JTextField("0");
	JTextField txtSphereCenter_y = new JTextField("0");
	JTextField txtSphereCenter_z = new JTextField("0");
	JLabel lblSphereRadius = new JLabel("Radius:");
	JTextField txtSphereRadius = new JTextField("1.0");
	JLabel lblSphereName = new JLabel("Name:");
	JTextField txtSphereName = new JTextField("no-name");
	JButton cmdSphere = new JButton("Create");
	
	JLabel lblGaussian = new JLabel("Gaussian smoothing");
	JLabel lblGaussianSigmaNormal = new JLabel("Sigma normal:");
	JTextField txtGaussianSigmaNormal = new JTextField("1");
	JLabel lblGaussianSigmaTangent = new JLabel("Sigma tangent:");
	JTextField txtGaussianSigmaTangent = new JTextField("4");
	JLabel lblGaussianGaussianCutoff = new JLabel("Cutoff (sigmas):");
	JTextField txtGaussianGaussianCutoff = new JTextField("2");
	JButton cmdGaussian = new JButton("Execute");
	
	boolean update_combos = true;
	
	public InterfaceNeuroMeshPanel(){
		super();
	}
	
	protected void init() {
		_init();
		
		//set this sucker up
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		//set listeners
		cmbScalpT1Volume.addActionListener(this);
		cmbScalpT1Volume.setActionCommand("Scalp Volume Changed");
		cmdScalpExecute.addActionListener(this);
		cmdScalpExecute.setActionCommand("Scalp Execute");
		
		cmdSphere.addActionListener(this);
		cmdSphere.setActionCommand("Tests Create Sphere");
		cmdGaussian.addActionListener(this);
		cmdGaussian.setActionCommand("Tests Smooth Gaussian");
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblScalpSkull, c);
		lblScalpSkull.setParentObj(this);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 1, 1, 0.05, .3, 1);
		add(lblScalpT1Volume, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 1, 1, 0.35, .6, 1);
		add(cmbScalpT1Volume, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 2, 2, 0.05, .3, 1);
		add(lblScalpMask, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 2, 2, 0.35, .6, 1);
		add(cmbScalpMask, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 3, 3, 0.05, .3, 1);
		add(lblScalpBrainSurface, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 3, 3, 0.35, .6, 1);
		add(cmbScalpBrainSurface, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 4, 4, 0.05, .44, 1);
		add(lblScalpSphereN, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 4, 4, 0.51, .44, 1);
		add(txtScalpSphereN, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 5, 5, 0.05, .44, 1);
		add(lblScalpSample, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 5, 5, 0.51, .44, 1);
		add(txtScalpSample, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 6, 6, 0.05, .44, 1);
		add(lblScalpMinDistB_IS, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 6, 6, 0.51, .44, 1);
		add(txtScalpMinDistB_IS, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 7, 7, 0.05, .44, 1);
		add(lblScalpMinDistOS_S, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 7, 7, 0.51, .44, 1);
		add(txtScalpMinDistOS_S, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 8, 8, 0.05, .44, 1);
		add(lblScalpThreshold, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 8, 8, 0.51, .44, 1);
		add(txtScalpThreshold, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 9, 9, 0.05, .44, 1);
		add(lblScalpThresholdDist, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 9, 9, 0.51, .44, 1);
		add(txtScalpThresholdDist, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 10, 10, 0.05, .44, 1);
		add(lblScalpMinSlope, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 10, 10, 0.51, .44, 1);
		add(txtScalpMinSlope, c);
		
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 11, 11, 0.05, .9, 1);
		add(lblScalpPlane, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 12, 12, 0.05, .3, 1);
		add(lblScalpSectionSet, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 12, 12, 0.35, .6, 1);
		add(cmbScalpSectionSet, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 13, 13, 0.05, .44, 1);
		add(lblScalpSectionPlane, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 13, 13, 0.51, .44, 1);
		add(txtScalpSectionPlane, c);
		
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 14, 14, 0.05, .9, 1);
		add(chkScalpGaussian, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 15, 15, 0.05, .44, 1);
		add(lblScalpSigmaNormal, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 15, 15, 0.51, .44, 1);
		add(txtScalpSigmaNormal, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 16, 16, 0.05, .44, 1);
		add(lblScalpSigmaTangent, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 16, 16, 0.51, .44, 1);
		add(txtScalpSigmaTangent, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 17, 17, 0.05, .44, 1);
		add(lblScalpGaussianCutoff, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 17, 17, 0.51, .44, 1);
		add(txtScalpGaussianCutoff, c);
		
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 18, 18, 0.05, .3, 1);
		add(lblScalpShapeSet, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 18, 18, 0.35, .6, 1);
		add(cmbScalpShapeSet, c);
		c = new CategoryLayoutConstraints("SCALP/SKULL MODEL", 19, 20, 0.2, .6, 1);
		add(cmdScalpExecute, c);
	
		
		c = new CategoryLayoutConstraints();
		add(lblTests, c);
		lblTests.setParentObj(this);
		c = new CategoryLayoutConstraints("TESTS", 1, 1, 0.05, .9, 1);
		add(lblSphere, c);
		lblSphere.setHorizontalAlignment(JLabel.CENTER);
		c = new CategoryLayoutConstraints("TESTS", 2, 2, 0.05, .44, 1);
		add(lblSphereN, c);
		c = new CategoryLayoutConstraints("TESTS", 2, 2, 0.51, .44, 1);
		add(txtSphereN, c);
		c = new CategoryLayoutConstraints("TESTS", 3, 3, 0.05, .9, 1);
		add(lblSphereCenter, c);
		c = new CategoryLayoutConstraints("TESTS", 4, 4, 0.05, .3, 1);
		add(txtSphereCenter_x, c);
		c = new CategoryLayoutConstraints("TESTS", 4, 4, 0.35, .3, 1);
		add(txtSphereCenter_y, c);
		c = new CategoryLayoutConstraints("TESTS", 4, 4, 0.65, .3, 1);
		add(txtSphereCenter_z, c);
		c = new CategoryLayoutConstraints("TESTS", 5, 5, 0.05, .44, 1);
		add(lblSphereRadius, c);
		c = new CategoryLayoutConstraints("TESTS", 5, 5, 0.51, .44, 1);
		add(txtSphereRadius, c);
		c = new CategoryLayoutConstraints("TESTS", 6, 6, 0.05, .44, 1);
		add(lblSphereName, c);
		c = new CategoryLayoutConstraints("TESTS", 6, 6, 0.51, .44, 1);
		add(txtSphereName, c);
		c = new CategoryLayoutConstraints("TESTS", 7, 8, 0.2, .6, 1);
		add(cmdSphere, c);
		
		c = new CategoryLayoutConstraints("TESTS", 9, 9, 0.05, .9, 1);
		add(lblGaussian, c);
		lblGaussian.setHorizontalAlignment(JLabel.CENTER);
		c = new CategoryLayoutConstraints("TESTS", 10, 10, 0.05, .44, 1);
		add(lblGaussianSigmaNormal, c);
		c = new CategoryLayoutConstraints("TESTS", 10, 10, 0.51, .44, 1);
		add(txtGaussianSigmaNormal, c);
		c = new CategoryLayoutConstraints("TESTS", 11, 11, 0.05, .44, 1);
		add(lblGaussianSigmaTangent, c);
		c = new CategoryLayoutConstraints("TESTS", 11, 11, 0.51, .44, 1);
		add(txtGaussianSigmaTangent, c);
		c = new CategoryLayoutConstraints("TESTS", 12, 12, 0.05, .44, 1);
		add(lblGaussianGaussianCutoff, c);
		c = new CategoryLayoutConstraints("TESTS", 12, 12, 0.51, .44, 1);
		add(txtGaussianGaussianCutoff, c);
		c = new CategoryLayoutConstraints("TESTS", 13, 14, 0.2, .6, 1);
		add(cmdGaussian, c);
		
	}
	
	void updateCombos(){
		if (!update_combos) return;
		update_combos = false;
		Volume3DInt current_volume = (Volume3DInt)cmbScalpT1Volume.getSelectedItem();
		cmbScalpT1Volume.removeAllItems();
		Volume3DInt volume = new Volume3DInt();
		ShapeSet3DInt current_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		ShapeSet3DInt shape_set = current_set.getShapeType(volume, true);
		
		for (int i = 0; i < shape_set.members.size(); i++)
			cmbScalpT1Volume.addItem(shape_set.members.get(i));
		
		if (current_volume != null)
			cmbScalpT1Volume.setSelectedItem(current_volume);
		
		updateMasks();
		
		SectionSet3DInt current_section_set = (SectionSet3DInt)cmbScalpSectionSet.getSelectedItem();
		cmbScalpSectionSet.removeAllItems();
		SectionSet3DInt section_set = new SectionSet3DInt();
		shape_set = current_set.getShapeType(section_set, true);
		
		for (int i = 0; i < shape_set.members.size(); i++)
			cmbScalpSectionSet.addItem(shape_set.members.get(i));
		
		if (current_section_set != null)
			cmbScalpSectionSet.setSelectedItem(current_section_set);
		
		
		Mesh3DInt current_mesh = (Mesh3DInt)cmbScalpBrainSurface.getSelectedItem();
		cmbScalpBrainSurface.removeAllItems();
		
		Mesh3DInt mesh = new Mesh3DInt();
		shape_set = current_set.getShapeType(mesh, true);
		
		for (int i = 0; i < shape_set.members.size(); i++)
			cmbScalpBrainSurface.addItem(shape_set.members.get(i));
		
		if (current_mesh != null)
			cmbScalpBrainSurface.setSelectedItem(current_mesh);
		
		cmbScalpShapeSet.removeAllItems();
		
		ShapeSet3DInt set = new ShapeSet3DInt();
		cmbScalpShapeSet.addItem(current_set);
		shape_set = current_set.getShapeType(set, true);
		
		for (int i = 0; i < shape_set.members.size(); i++)
			cmbScalpShapeSet.addItem(shape_set.members.get(i));
		
		update_combos = true;
	}
	
	public void showPanel(){
		updateCombos();
	}

	void updateMasks(){
		
		Volume3DInt volume = (Volume3DInt)cmbScalpT1Volume.getSelectedItem();
		String current_mask = (String)cmbScalpMask.getSelectedItem();
		
		cmbScalpMask.removeAllItems();
		
		if (volume == null) return;
		
		HashMap<String, boolean[][][]> masks = volume.getMasks();
		Iterator<String> itr = masks.keySet().iterator();
		
		while (itr.hasNext())
			cmbScalpMask.addItem(itr.next());
		
		if (current_mask != null)
			cmbScalpMask.setSelectedItem(current_mask);
		
	}
	
	public void actionPerformed(ActionEvent e) {
	
		if (e.getActionCommand().startsWith("Scalp")){
			
			if (e.getActionCommand().endsWith("Volume Changed")){
				if (!update_combos) return;
				updateMasks();
				return;
				}
			
			if (e.getActionCommand().endsWith("Execute")){
				
				//TODO checks to ensure all required fields are set
				//	   (currently will throw a NullPointerException)
				
				ScalpAndSkullModelOptions options = new ScalpAndSkullModelOptions();
				options.t1_volume = (Volume3DInt)cmbScalpT1Volume.getSelectedItem();
				options.brain_mask = (String)cmbScalpMask.getSelectedItem();
				options.ear_nasium_plane = ((SectionSet3DInt)cmbScalpSectionSet.getSelectedItem()).
													getPlaneAt(Integer.valueOf(txtScalpSectionPlane.getText()));
				options.center_of_mass = ((Mesh3DInt)cmbScalpBrainSurface.getSelectedItem()).getCenterOfGravity();
				options.n_nodes = Integer.valueOf(txtScalpSphereN.getText());
				options.sample_rate = Float.valueOf(txtScalpSample.getText());
				options.min_dist_B_IS = Float.valueOf(txtScalpMinDistB_IS.getText());
				options.min_dist_OS_S = Float.valueOf(txtScalpMinDistOS_S.getText());
				options.min_intensity = Float.valueOf(txtScalpThreshold.getText());
				options.max_intensity_dist = Float.valueOf(txtScalpThresholdDist.getText());
				options.min_slope = Float.valueOf(txtScalpMinSlope.getText());
				options.gaussian_cutoff = Float.valueOf(txtScalpGaussianCutoff.getText());
				options.sigma_normal = Float.valueOf(txtScalpSigmaNormal.getText());
				options.sigma_tangent = Float.valueOf(txtScalpSigmaTangent.getText());
				options.apply_gaussian = chkScalpGaussian.isSelected();
				
				options.shape_set = (ShapeSet3DInt)cmbScalpShapeSet.getSelectedItem();
				
				InterfaceProgressBar progress_bar = new InterfaceProgressBar("Modelling scalp/skull surfaces: ");
				
				if (NeuroMeshFunctions.modelScalpAndSkull(options, progress_bar))
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Surfaces modelled successfully.", 
												  "Model scalp/skull surfaces", 
												  JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Problem modelling surfaces.", 
												  "Model scalp/skull surfaces", 
												  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			}
		
		if (e.getActionCommand().startsWith("Tests")){
			
			if (e.getActionCommand().endsWith("Create Sphere")){
				
				/*
				Mesh3D mesh = MeshFunctions.getGlobeSphereMesh(
											new Point3f(Float.valueOf(txtSphereCenter_x.getText()),
														Float.valueOf(txtSphereCenter_y.getText()),
														Float.valueOf(txtSphereCenter_z.getText())),
											Float.valueOf(txtSphereRadius.getText()),
											Integer.valueOf(txtSphereN.getText()));
				*/
				
				/*
				Mesh3D mesh = MeshFunctions.getFractalSphereMesh(
											new Point3f(Float.valueOf(txtSphereCenter_x.getText()),
														Float.valueOf(txtSphereCenter_y.getText()),
														Float.valueOf(txtSphereCenter_z.getText())),
											Float.valueOf(txtSphereRadius.getText()),
											Integer.valueOf(txtSphereN.getText()));
				*/
				
				Mesh3D mesh = MeshFunctions.getMeanSphereMesh(
						new Point3f(Float.valueOf(txtSphereCenter_x.getText()),
								Float.valueOf(txtSphereCenter_y.getText()),
								Float.valueOf(txtSphereCenter_z.getText())),
						Float.valueOf(txtSphereRadius.getText()),
						Integer.valueOf(txtSphereN.getText()),
						0,
						1000);
					
				
				InterfaceSession.getDisplayPanel().getCurrentShapeSet().addShape(new Mesh3DInt(mesh, txtSphereName.getText()));
				
				return;
				}
				
			if (e.getActionCommand().endsWith("Smooth Gaussian")){
				
				final Volume3DInt t1_volume = (Volume3DInt)cmbScalpT1Volume.getSelectedItem();
				final Grid3D t1_grid = t1_volume.getGrid();
				
				final int x_size = t1_grid.getSizeS();
				final int y_size = t1_grid.getSizeT();
				final int z_size = t1_grid.getSizeR();
				final int t_size = t1_grid.getSizeV();
				
				final Volume3DInt smoothed = new Volume3DInt(new Grid3D(t1_grid));
				
				final float sigma_normal = Float.valueOf(txtGaussianSigmaNormal.getText());
				final float sigma_tangent = Float.valueOf(txtGaussianSigmaTangent.getText());
				final float cutoff = Float.valueOf(txtGaussianGaussianCutoff.getText());
				final Point3f center = ((Mesh3DInt)cmbScalpBrainSurface.getSelectedItem()).getCenterOfGravity();
				final Vector3f normal = new Vector3f();
				
				final InterfaceProgressBar progress_bar = new InterfaceProgressBar("Smoothing volume '" + t1_volume.getName() + "': ");
				progress_bar.register();
				progress_bar.progressBar.setMinimum(0);
				progress_bar.progressBar.setMaximum(x_size * y_size);
				
				Worker.post(new Job(){
					public Object run(){
						for (int i = 0; i < x_size; i++)
							for (int j = 0; j < y_size; j++){
								for (int k = 0; k < z_size; k++){
									normal.set(t1_grid.getVoxelMidPoint(i, j, k));
									normal.sub(center);
									normal.normalize();
									smoothed.setDatumAtVoxel(i, j, k, 
											VolumeFunctions.getGaussianSmoothedValue(t1_volume, new int[]{i, j, k}, 0, 
																					 normal, sigma_normal, sigma_tangent, cutoff));
									
									}
								progress_bar.update(i * y_size + j);
								}
					return null;
					}
				});
				
				progress_bar.deregister();
				smoothed.setName("smoothed_" + t1_volume.getName());
				InterfaceSession.getDisplayPanel().getCurrentShapeSet().addShape(smoothed);
				
				return;
				}
			
			}
		
	}
	
	public String toString(){
		return "Neuro Mesh Panel";
	}
	
}