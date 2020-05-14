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


package mgui.io.foreign.analyze;

import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.geometry.volume.VolumeFunctions;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.standard.nifti.NiftiMetadata;
import mgui.io.standard.nifti.NiftiVolumeLoader;


/**********************************
 * <P>Rotate an Analyze volume about a specified axis, by some multiple of 90 degrees.</P>
 * <P>Output is a new img file with the prefix 'trans'. New header files with this name
 * will also be generated; these are simply copies of the originals.</P>
 * <P>Syntax: java AnalyzeRotateVolume [AXIS] [ROT] [FILE]</P>
 * <P>Arguments:</>
 * <P>AXIS: one of X, Y, Z indicating the axis to rotate about</P>
 * <P>ROT: the rotation, which must be 90, 180, or 270</P>
 * <P>FILE: the input Analyze volume (*.img)</P>
 *  
 * @author Andrew Reid
 *
 */
public class AnalyzeRotateVolume {

	static final int X = 0, Y = 1, Z = 2;
	static final int ROT90 = 0, ROT180 = 1, ROT270 = 2; 
	
	
	public static void main(String[] args){
		FileDialog dialog = new FileDialog();
		dialog.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main_old(String[] args) {
		int axis = getAxis(args[0]);
		if (axis < 0){
			System.out.print("First argument must be either X, Y, or Z: " + args[0]);
			return;
			}
		int angle = Integer.valueOf(args[1]).intValue();
		int rotation = -1;
		if (angle == 90) rotation = ROT90;
		if (angle == 180) rotation = ROT180;
		if (angle == 270) rotation = ROT270;
		if (rotation < 0){
			System.out.print("Second argument must a multiple of 90 and <360: " + args[1]);
			return;
			}
		File file = new File(args[2]);
		if (!file.exists()){
			System.out.print("Third argument must be an existing file: " + args[2]);
			return;
			}
		execute(axis, rotation, file);
	}
	
	static int getAxis(String a){
		if (a.equals("X") || a.equals("x")) return X;
		if (a.equals("Y") || a.equals("y")) return Y;
		if (a.equals("Z") || a.equals("z")) return Z;
		return -1;
	}
	
	static void execute(int axis, int rotation, File file){
	
		/*
			AnalyzeVolumeLoader loader = new AnalyzeVolumeLoader(file);
			AnalyzeHeader header = loader.readHeader();
			int[] dimTrans = VolumeFunctions.rotateDims(new int[]{0, 1, 2}, axis, rotation);
			
			//alter image dims
			short s1 = header.image.dim[dimTrans[0] + 1];
			short s2 = header.image.dim[dimTrans[1] + 1];
			short s3 = header.image.dim[dimTrans[2] + 1];
			header.image.dim[1] = s1;
			header.image.dim[2] = s2;
			header.image.dim[3] = s3;
			
			//alter geometric dims
			float f1 = header.image.pixdim[dimTrans[0] + 1];
			float f2 = header.image.pixdim[dimTrans[1] + 1];
			float f3 = header.image.pixdim[dimTrans[2] + 1];
			header.image.pixdim[1] = f1;
			header.image.pixdim[2] = f2;
			header.image.pixdim[3] = f3;
			
			//alter origin
			s1 = header.image.origin[dimTrans[0]];
			s2 = header.image.origin[dimTrans[1]];
			s3 = header.image.origin[dimTrans[2]];
			header.image.origin[0] = s1;
			header.image.origin[1] = s2;
			header.image.origin[2] = s3;
			
			//rotate image
			loader.setAlpha(false);
			Grid3D gridIn = loader.getGrid3D();
			Grid3D gridOut = VolumeFunctions.applyRotation(axis, rotation, gridIn);
			
			File trans = new File(file.getParent() + "\\trans_" + file.getName());
			AnalyzeVolumeWriter writer = new AnalyzeVolumeWriter(trans);
			writer.writeHeader(header);
			writer.setDataTypeFromHeader();
			writer.writeImage(gridOut);
			
			*/
		
	}
	
	static void execute2(int axis, int rotation, File file){
		
		/*
			AnalyzeVolumeLoader loader = new AnalyzeVolumeLoader(file);
			Grid3D gridIn = loader.getGrid3D();
			int[] dimIn = {gridIn.xSize, gridIn.ySize, gridIn.zSize};
			int[] dimOut = transformDims(dimIn, axis, rotation);
			System.out.println("Rotate image:");
			System.out.println("IN: " + dimIn[0] + ", " + dimIn[1] + ", " +	dimIn[2] 
			               + "; OUT: " + dimOut[0] + ", " + dimOut[1] + ", " + dimOut[2]);
			int[] dimTrans = transformDims(new int[]{0, 1, 2}, axis, rotation);
			Grid3D gridOut = new Grid3D(dimOut[0], dimOut[1], dimOut[2]);
			AnalyzeHeader header = loader.readHeader();
			
			//alter image dims
			header.image.dim[1] = header.image.dim[dimTrans[0] + 1];
			header.image.dim[2] = header.image.dim[dimTrans[1] + 1];
			header.image.dim[3] = header.image.dim[dimTrans[2] + 1];
			
			//alter geometric dims
			header.image.pixdim[1] = header.image.pixdim[dimTrans[0] + 1];
			header.image.pixdim[2] = header.image.pixdim[dimTrans[1] + 1];
			header.image.pixdim[3] = header.image.pixdim[dimTrans[2] + 1];
			
			//alter origin
			header.image.origin[0] = header.image.origin[dimTrans[0]];
			header.image.origin[1] = header.image.origin[dimTrans[1]];
			header.image.origin[2] = header.image.origin[dimTrans[2]];
			
			//copy gridIn into gridOut after transforming indices
			int[] indices;
			byte[] b;
			for (int i = 0; i < gridIn.xSize; i++)
				for (int j = 0; j < gridIn.ySize; j++)
					for (int k = 0; k < gridIn.zSize; k++){
						indices = VolumeFunctions.rotate(new int[]{i,j,k}, dimIn, axis, rotation);
						System.out.println("IN: " + i + ", " + j + ", " + k + "; OUT: "
								+ indices[0] + ", " + indices[1] + ", " + indices[2]);
						b = new byte[gridIn.dataSize];
						//b = (byte[])gridIn.getValue(i, j, k);
						gridIn.getValue(i, j, k, b);
						gridOut.setValue(indices[0], indices[1], indices[2], b);
						}
			
			//write output to trans+Filename
			File trans = new File(file.getAbsolutePath() + "trans_" + file.getName());
			AnalyzeVolumeWriter writer = new AnalyzeVolumeWriter(trans);
			writer.writeHeader(header);
			//set geometric dimensions
			gridOut.bounds = setGridBox(dimOut, gridIn.bounds);
			writer.setFromGrid(gridOut);
			
	*/	
		
	}
	
	static Box3D setGridBox(int[] dims, Box3D bounds){
		int x = dims[0], y = dims[1], z = dims[2];
		Box3D box = new Box3D();
		switch (x){
			case X: 
				box.setSDim(bounds.getSAxis().length());
				break;
			case Y:
				box.setSDim(bounds.getTAxis().length());
				break;
			case Z:
				box.setSDim(bounds.getRAxis().length());
				break;
			}
		switch (y){
			case X: 
				box.setTDim(bounds.getSAxis().length());
				break;
			case Y:
				box.setTDim(bounds.getTAxis().length());
				break;
			case Z:
				box.setTDim(bounds.getRAxis().length());
				break;
			}
		switch (z){
			case X: 
				box.setRDim(bounds.getSAxis().length());
				break;
			case Y:
				box.setRDim(bounds.getTAxis().length());
				break;
			case Z:
				box.setRDim(bounds.getRAxis().length());
				break;
			}
		
		return box;
	}
	
	static int[] transformDims(int[] dims, int axis, int rotation){
		int x = dims[0], y = dims[1], z = dims[2];
		//180 rotation doesn't change dimensions
		if (rotation == ROT180) return new int[]{x, y, z};
		switch (axis){
			case X:
				//x' = x, y' = z, z' = y
				return new int[]{x, z, y};
			case Y:
				//x' = z, y' = y, z' = x
				return new int[]{z, y, x};
			case Z:
				//x' = y, y' = x, z' = z
				return new int[]{y, x, z};
			default:
				return null;
			}
	}
	
	static int[] transform(int[] coords, int[] dims, int axis, int rotation){
		int x = coords[0], y = coords[1], z = coords[2];
		switch (axis){
			case X:
				switch (rotation){
					case ROT90:
						//rotate 90 about X
						//y' = max - z; z' = y
						return new int[]{x, dims[2] - z, y};
					case ROT180:
						//rotate 180 about X
						//y' = max - y; z' = max - z
						return new int[]{x, dims[1] - y, dims[2] - z};
					case ROT270:
						//rotate 270 about X
						//y' = max - z; z' = max - y
						return new int[]{x, dims[2] - z, dims[1] - y};
					default:
						return null;
					}
				
			case Y:
				switch (rotation){
					case ROT90:
						//rotate 90 about Y
						//z' = max - x; x' = z
						return new int[]{z, y, dims[0] - x};
					case ROT180:
						//rotate 180 about Y
						//z' = max - z; x' = max - x
						return new int[]{dims[0] - x, y, dims[2] - z};
					case ROT270:
						//rotate 270 about Y
						//z' = max - x; x' = max - z
						return new int[]{dims[2] - z, y, dims[0] - x};
					default:
						return null;
					}
				
			case Z:
				switch (rotation){
					case ROT90:
						//rotate 90 about Z
						//y' = max - x; x' = y
						return new int[]{y, dims[1] - y, z};
					case ROT180:
						//rotate 180 about Z
						//y' = max - y; x' = max - y
						return new int[]{dims[0] - x, dims[1] - y, z};
					case ROT270:
						//rotate 270 about Z
						//y' = max - x; x' = max - y
						return new int[]{dims[1] - y, dims[0] - x, z};
					default:
						return null;
					}
				
			default:
				return null;
		
		}
		
	}
	
	static class FileDialog extends InterfaceDialogBox {

		//file
		JLabel lblFileName = new JLabel("File name:");
		JTextField txtFileName = new JTextField("");
		JButton cmdBrowse = new JButton("Browse..");
		JCheckBox chkRotFile = new JCheckBox("Single file");
		JCheckBox chkRotFolder = new JCheckBox("Folder");
		JLabel lblRotAngle = new JLabel("Rotation:");
		JComboBox cmbRotAngle = new JComboBox();
		JLabel lblRotAxis = new JLabel("Axis:");
		JComboBox cmbRotAxis = new JComboBox();
		
		LineLayout lineLayout;
		File volFile;
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Analyze file (*.hdr)", "hdr");
		
		//constructor
		public FileDialog(){
			super();
			//super(aFrame, parent);
			setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
			init();
			//setLocationRelativeTo(aFrame);
			this.setLocation(300, 300);
		}
		
		//init
		protected void init(){
			super.init();
			lineLayout = new LineLayout(20, 5, 0);
			this.setMainLayout(lineLayout);
			this.setDialogSize(500,550);
			this.setTitle("Write grid data to volume file");
			
			LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.01, 0.18, 1);
			
			chkRotFile.addActionListener(this);
			chkRotFolder.addActionListener(this);
			chkRotFile.setActionCommand("File");
			chkRotFolder.setActionCommand("Folder");
			
			cmdBrowse.addActionListener(this);
			cmdBrowse.setActionCommand("Browse");
			
			chkRotFile.setSelected(true);
			chkRotFolder.setSelected(false);
			chkRotFolder.setEnabled(false);
			
			//combos
			cmbRotAngle.addItem("90CW");
			cmbRotAngle.addItem("180");
			cmbRotAngle.addItem("270CW");
			cmbRotAxis.addItem("X");
			cmbRotAxis.addItem("Y");
			cmbRotAxis.addItem("Z");
			
			c = new LineLayoutConstraints(1, 1, 0.71, 0.28, 1);
			mainPanel.add(cmdBrowse, c);
			c = new LineLayoutConstraints(2, 2, 0.01, 0.18, 1);
			mainPanel.add(lblFileName, c);
			c = new LineLayoutConstraints(2, 2, 0.2, 0.5, 1);
			mainPanel.add(txtFileName, c);
			c = new LineLayoutConstraints(2, 2, 0.71, 0.28, 1);
			mainPanel.add(cmdBrowse, c);
			c = new LineLayoutConstraints(3, 3, 0.01, 0.48, 1);
			mainPanel.add(chkRotFile, c);
			c = new LineLayoutConstraints(3, 3, 0.01, 0.48, 1);
			mainPanel.add(chkRotFolder, c);
			c = new LineLayoutConstraints(4, 4, 0.01, 0.18, 1);
			mainPanel.add(lblRotAngle, c);
			c = new LineLayoutConstraints(4, 4, 0.2, 0.5, 1);
			mainPanel.add(cmbRotAngle, c);
			c = new LineLayoutConstraints(5, 5, 0.01, 0.18, 1);
			mainPanel.add(lblRotAxis, c);
			c = new LineLayoutConstraints(5, 5, 0.2, 0.5, 1);
			mainPanel.add(cmbRotAxis, c);
			
			}
		
		int getAxis(){
			if (cmbRotAxis.getSelectedItem().equals("X")) return X;
			if (cmbRotAxis.getSelectedItem().equals("Y")) return Y;
			if (cmbRotAxis.getSelectedItem().equals("Z")) return Z;
			return -1;
		}
		
		int getAngle(){
			if (cmbRotAngle.getSelectedItem().equals("90CW")) return ROT90;
			if (cmbRotAngle.getSelectedItem().equals("180")) return ROT180;
			if (cmbRotAngle.getSelectedItem().equals("270CW")) return ROT270;
			return -1;
		}
		
		public void actionPerformed(ActionEvent e){
			
			if (e.getActionCommand().equals("Browse")){
				JFileChooser fc;
				if (volFile != null)
					fc = new JFileChooser(volFile);
				else
					fc = new JFileChooser();
				fc.setFileFilter(filter);
						
				fc.setDialogTitle("Select Volume File");
				fc.setMultiSelectionEnabled(false);
				if (fc.showDialog(this, "Accept") == JFileChooser.APPROVE_OPTION){
					volFile = fc.getSelectedFile();
					String fileStr = volFile.getAbsolutePath();
					if (fileStr.length() < 4 || 
						fileStr.lastIndexOf(".") != fileStr.length() - 4){
						fileStr += ".hdr";
						volFile = new File(fileStr);
						}
					txtFileName.setText(volFile.getPath());
					}
				return;
				}
			
			//write to specified file
			if (e.getActionCommand().equals(DLG_CMD_OK)){
				if (volFile == null) return;
				execute(getAxis(), getAngle(), volFile);
				JOptionPane.showMessageDialog(this, "File rotated successfully");
				}
			
			if (e.getActionCommand().equals("File")){
				chkRotFolder.setSelected(!chkRotFile.isSelected());
				this.repaint();
				return;
				}
			
			if (e.getActionCommand().equals("Folder")){
				chkRotFile.setSelected(!chkRotFolder.isSelected());
				this.repaint();
				return;
				}
			
			super.actionPerformed(e);
			
		}
	}

}