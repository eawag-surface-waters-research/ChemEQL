package ch.eawag.chemeql;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


class ActivityInputDialog extends ProceedCancelDialog
{
	private static ActivityInputDialog INSTANCE;

	static ActivityInputDialog getInstance(ChemEqlGuiController parent) {
		if (INSTANCE == null) {
			INSTANCE = new ActivityInputDialog(parent);
		}
		return INSTANCE;
	}

	private double currentIonicStr;
	private double currentActivA;

	private ActivityInputDialog(ChemEqlGuiController parent) {
		super(parent);
		initComponents();
		setLocation(300, 250);
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e) {
				currentActivA = 0.5;
				ionicStrTF.setEnabled(true);
				ionicStrTF.setText("");
				ionicStrTF.requestFocusInWindow();
				activATF.setText("0.5");
				fixIonicStrRB.setSelected(true);
				concentrationsOutputRB.setSelected(true);
				proceedButton.setEnabled(false);
			}
		});
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
   private void initComponents()//GEN-BEGIN:initComponents
   {
      javax.swing.JLabel activALabel;
      java.awt.GridBagConstraints gridBagConstraints;
      javax.swing.JLabel outputLabel;

      ionicBG = new javax.swing.ButtonGroup();
      outputBG = new javax.swing.ButtonGroup();
      fixIonicStrRB = new javax.swing.JRadioButton();
      ionicStrTF = new KeyListeningTextField()
      {
         void keyPressedActivity()
         {
            checkInputs();
         }
      };

      varIonicStrRB = new javax.swing.JRadioButton();
      activALabel = new javax.swing.JLabel();
      activATF = new KeyListeningTextField()
      {
         void keyPressedActivity()
         {
            checkInputs();
         }
      };

      outputLabel = new javax.swing.JLabel();
      concentrationsOutputRB = new javax.swing.JRadioButton();
      activityOutputRB = new javax.swing.JRadioButton();

      setTitle("Activity");
      ionicBG.add(fixIonicStrRB);
      fixIonicStrRB.setText("Ionic strength     I =");
      fixIonicStrRB.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            fixIonicStrRBActionPerformed(evt);
         }
      });

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      getControls().add(fixIonicStrRB, gridBagConstraints);

      ionicStrTF.setColumns(6);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      getControls().add(ionicStrTF, gridBagConstraints);

      ionicBG.add(varIonicStrRB);
      varIonicStrRB.setText("Calculate ionic strength according to matrix");
      varIonicStrRB.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            varIonicStrRBActionPerformed(evt);
         }
      });

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      getControls().add(varIonicStrRB, gridBagConstraints);

      activALabel.setText("A = 1.82E6 (eT)-3/2 = ");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 50, 0, 4);
      getControls().add(activALabel, gridBagConstraints);

      activATF.setColumns(4);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.insets = new java.awt.Insets(16, 0, 16, 0);
      getControls().add(activATF, gridBagConstraints);

      outputLabel.setText("Give output in:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 3;
      gridBagConstraints.gridheight = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
      getControls().add(outputLabel, gridBagConstraints);

      outputBG.add(concentrationsOutputRB);
      concentrationsOutputRB.setText("concentrations");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 3;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      getControls().add(concentrationsOutputRB, gridBagConstraints);

      outputBG.add(activityOutputRB);
      activityOutputRB.setText("activities");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      getControls().add(activityOutputRB, gridBagConstraints);

      pack();
   }//GEN-END:initComponents

	private void fixIonicStrRBActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fixIonicStrRBActionPerformed
	{//GEN-HEADEREND:event_fixIonicStrRBActionPerformed
		ionicStrTF.setEnabled(true);
		ionicStrTF.requestFocusInWindow();
		ionicStrTF.selectAll();
		checkInputs();
	}//GEN-LAST:event_fixIonicStrRBActionPerformed

	private void varIonicStrRBActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_varIonicStrRBActionPerformed
	{//GEN-HEADEREND:event_varIonicStrRBActionPerformed
		activATF.requestFocusInWindow();
		activATF.selectAll();
		ionicStrTF.setEnabled(false);
		checkInputs();
	}//GEN-LAST:event_varIonicStrRBActionPerformed

	private void checkInputs() {
		try {
			currentActivA = Double.parseDouble(activATF.getText());
			if (fixIonicStrRB.isSelected()) {
				currentIonicStr = Double.parseDouble(ionicStrTF.getText());
			}
			proceedButton.setEnabled(true);
		} catch (NumberFormatException ex) {
			proceedButton.setEnabled(false);
		}
	}

	protected void doCancel() {
		main.setActivity(Activity.NO); // was main.calcActivCoeff.selectToggle(Activity.NO);
		main.activityOutput = false;
		super.doCancel();
	}

	protected void doProceed() {
		if (currentActivA <= 0) {
			MyTools.showError("Constant A must be > 0 !");
		}
		else if (fixIonicStrRB.isSelected() && currentIonicStr <= 0) {
			MyTools.showError("Ionic strength must be > 0 !");
		}
		else {
			main.matrix.activA = currentActivA;
			main.matrix.ionicStr = currentIonicStr;
			main.matrix.fixionicStr = fixIonicStrRB.isSelected();
			main.activityOutput = activityOutputRB.isSelected();
			super.doProceed();
		}
	}
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JTextField activATF;
   private javax.swing.JRadioButton activityOutputRB;
   private javax.swing.JRadioButton concentrationsOutputRB;
   private javax.swing.JRadioButton fixIonicStrRB;
   private javax.swing.ButtonGroup ionicBG;
   private javax.swing.JTextField ionicStrTF;
   private javax.swing.ButtonGroup outputBG;
   private javax.swing.JRadioButton varIonicStrRB;
   // End of variables declaration//GEN-END:variables
}
