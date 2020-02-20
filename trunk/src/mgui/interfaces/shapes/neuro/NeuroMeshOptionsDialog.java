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

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.geometry.neuro.mesh.ScalpAndSkullModelOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

public class NeuroMeshOptionsDialog extends InterfaceOptionsDialogBox {

	JLabel lblNoNodes = new JLabel("Number of nodes:");
	JTextField txtNoNodes = new JTextField("1000");
	JLabel lblSampleRate = new JLabel("Sample rate:");
	JTextField txtSampleRate = new JTextField("5");
	JLabel lblScalpThreshold = new JLabel("Scalp threshold:");
	JTextField txtScalpThreshold = new JTextField("0.2");
	JLabel lblMinBrainToSkull = new JLabel("Min B>IS distance (mm):");
	JTextField txtMinBrainToSkull = new JTextField("1.5");
	JLabel lblPlateauSlope = new JLabel("Plateau slope:");
	JTextField txtPlateauSlope = new JTextField("0.2");
	JLabel lblPlateauLength = new JLabel("Min plateau length:");
	JTextField txtPlateauLength = new JTextField("10");
	JLabel lblPlateauMaxSlope = new JLabel("Plateau max slope:");
	JTextField txtPlateauMaxSlope = new JTextField("0.012");
	JLabel lblNeighbourWeight = new JLabel("Neighbour weights:");
	JTextField txtNeighbourWeight = new JTextField("1.0");
	JCheckBox chkApplyGaussian = new JCheckBox(" Apply Gaussian");
	JLabel lblSigmaNormal = new JLabel("Sigma normal (mm):");
	JTextField txtSigmaNormal = new JTextField("1.0");
	JLabel lblSigmaTangent = new JLabel("Sigma tangent (mm):");
	JTextField txtSigmaTangent = new JTextField("4.0");
	JLabel lblSigmaCutoff = new JLabel("Cutoff (\u03C3):");
	JTextField txtSigmaCutoff = new JTextField("2.0");
	
	public NeuroMeshOptionsDialog(){
		
	}
	
	public NeuroMeshOptionsDialog(JFrame frame, ScalpAndSkullModelOptions options){
		super(frame, options);
		_init();
	}
	
	private void _init(){
		setButtonType(BT_OK_CANCEL);
		init();
		
		this.setDialogSize(400,450);
		this.setTitle("Set Neuro Mesh Options");
		
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		chkApplyGaussian.addActionListener(this);
		chkApplyGaussian.setActionCommand("Gaussian Changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(lblNoNodes, c);
		lblNoNodes.setToolTipText("Approximate number of nodes in the resulting mesh surfaces");
		c = new LineLayoutConstraints(1, 1, 0.35, 0.6, 1);
		mainPanel.add(txtNoNodes, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		mainPanel.add(lblSampleRate, c);
		lblSampleRate.setToolTipText("The number of samples to take per mm");
		c = new LineLayoutConstraints(2, 2, 0.35, 0.6, 1);
		mainPanel.add(txtSampleRate, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		mainPanel.add(lblScalpThreshold, c);
		lblScalpThreshold.setToolTipText("The threshold value at which to define the scalp, " + 
										 "[0-1] as a proportion of maximum intensity.");
		c = new LineLayoutConstraints(3, 3, 0.35, 0.6, 1);
		mainPanel.add(txtScalpThreshold, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.3, 1);
		mainPanel.add(lblMinBrainToSkull, c);
		lblMinBrainToSkull.setToolTipText("The minimum distance from the brain surface to the inner" + 
										  " skull, in mm.");
		c = new LineLayoutConstraints(4, 4, 0.35, 0.6, 1);
		mainPanel.add(txtMinBrainToSkull, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.3, 1);
		mainPanel.add(lblPlateauSlope, c);
		lblPlateauSlope.setToolTipText("The threshold slope, as a proportion of the slope from N2 to N1, at" +
									   " which to define a 'plateau'.");
		c = new LineLayoutConstraints(5, 5, 0.35, 0.6, 1);
		mainPanel.add(txtPlateauSlope, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.3, 1);
		mainPanel.add(lblPlateauMaxSlope, c);
		lblPlateauSlope.setToolTipText("The maximum slope from N2 to N1, acceptable to define a 'plateau'.");
		c = new LineLayoutConstraints(6, 6, 0.35, 0.6, 1);
		mainPanel.add(txtPlateauMaxSlope, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.3, 1);
		mainPanel.add(lblPlateauLength, c);
		lblPlateauLength.setToolTipText("The minimum length at which to define a 'plateau', the start of which " +
										"becomes the outer skull surface. Make this very large to prevent plateau detection");
		c = new LineLayoutConstraints(7, 7, 0.35, 0.6, 1);
		mainPanel.add(txtPlateauLength, c);
		c = new LineLayoutConstraints(8, 8, 0.05, 0.3, 1);
		mainPanel.add(lblNeighbourWeight, c);
		c = new LineLayoutConstraints(8, 8, 0.35, 0.6, 1);
		lblNeighbourWeight.setToolTipText("The weight given to a ray's immediate neighbours, which is used to determine " +
										  "a weighted average. Make this zero to prevent averaging.");
		mainPanel.add(txtNeighbourWeight, c);
		c = new LineLayoutConstraints(9, 9, 0.05, 0.9, 1);
		mainPanel.add(chkApplyGaussian, c);
		chkApplyGaussian.setToolTipText("Whether to apply biased Gaussian smoothing to the image before sampling. This " +
		"may help achieve smooth surfaces, at the cost of processing time.");
		c = new LineLayoutConstraints(10, 10, 0.1, 0.3, 1);
		mainPanel.add(lblSigmaNormal, c);
		lblSigmaNormal.setToolTipText("The value of sigma (\u03C3) in the direction of the sample ray, for biased Gaussian " +
									  "smoothing. This should generally be smaller than sigma_tangent.");
		c = new LineLayoutConstraints(10, 10, 0.4, 0.55, 1);
		mainPanel.add(txtSigmaNormal, c);
		c = new LineLayoutConstraints(11, 11, 0.1, 0.3, 1);
		mainPanel.add(lblSigmaTangent, c);
		lblSigmaTangent.setToolTipText("The value of sigma (\u03C3) in the direction tangential to the sample ray, for biased" +
									  " Gaussian smoothing. This should generally be larger than sigma_normal.");
		c = new LineLayoutConstraints(11, 11, 0.4, 0.55, 1);
		mainPanel.add(txtSigmaTangent, c);
		c = new LineLayoutConstraints(12, 12, 0.1, 0.3, 1);
		mainPanel.add(lblSigmaCutoff, c);
		lblSigmaCutoff.setToolTipText("The distance, in sigmas, at which to stop sampling for biased Gaussian smoothing. " +
									  "This value greatly influences processing time.");
		c = new LineLayoutConstraints(12, 12, 0.4, 0.55, 1);
		mainPanel.add(txtSigmaCutoff, c);
		
		updateDialog();
		updateControls();
		
	}
	
	public boolean updateDialog(){
		if (options == null) options = new ScalpAndSkullModelOptions();
		ScalpAndSkullModelOptions _options = (ScalpAndSkullModelOptions)options;
		
		txtNoNodes.setText("" + _options.n_nodes);
		txtSampleRate.setText("" + _options.sample_rate);
		txtScalpThreshold.setText("" + _options.min_intensity);
		txtMinBrainToSkull.setText("" + _options.min_dist_B_IS);
		txtPlateauSlope.setText("" + _options.plateau_slope);
		txtPlateauMaxSlope.setText("" + _options.max_plateau_slope);
		txtPlateauLength.setText("" + _options.min_plateau_length);
		txtNeighbourWeight.setText("" + _options.average_neighbour_weight);
		chkApplyGaussian.setSelected(_options.apply_gaussian);
		txtSigmaNormal.setText("" + _options.sigma_normal);
		txtSigmaTangent.setText("" + _options.sigma_tangent);
		txtSigmaCutoff.setText("" + _options.gaussian_cutoff);
		
		return true;
	}
	
	void updateControls(){
		boolean enable = chkApplyGaussian.isSelected();
		txtSigmaNormal.setEnabled(enable);
		txtSigmaTangent.setEnabled(enable);
		txtSigmaCutoff.setEnabled(enable);
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Gaussian Changed")){
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			ScalpAndSkullModelOptions _options = (ScalpAndSkullModelOptions)options;
			_options.n_nodes = Integer.valueOf(txtNoNodes.getText());
			_options.sample_rate = Float.valueOf(txtSampleRate.getText());
			_options.min_intensity = Double.valueOf(txtScalpThreshold.getText());
			_options.min_dist_B_IS = Float.valueOf(txtMinBrainToSkull.getText());
			_options.plateau_slope = Double.valueOf(txtPlateauSlope.getText());
			_options.max_plateau_slope = Double.valueOf(txtPlateauMaxSlope.getText());
			_options.min_plateau_length = Integer.valueOf(txtPlateauLength.getText());
			_options.average_neighbour_weight = Double.valueOf(txtNeighbourWeight.getText());
			_options.apply_gaussian = chkApplyGaussian.isSelected();
			_options.sigma_normal = Float.valueOf(txtSigmaNormal.getText());
			_options.sigma_tangent = Float.valueOf(txtSigmaTangent.getText());
			_options.gaussian_cutoff = Float.valueOf(txtSigmaCutoff.getText());
			
			this.setVisible(false);
			}
		
		super.actionPerformed(e);
	}
	
	public static void showDialog(JFrame frame, ScalpAndSkullModelOptions options){
		NeuroMeshOptionsDialog dialog = new NeuroMeshOptionsDialog(frame, options);
		dialog.setVisible(true);
	}
	
}