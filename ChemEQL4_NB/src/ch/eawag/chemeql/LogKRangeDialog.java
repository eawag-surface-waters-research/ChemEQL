package ch.eawag.chemeql;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


class LogKRangeDialog extends ProceedCancelDialog implements ItemListener
{
	private static LogKRangeDialog INSTANCE;

	static LogKRangeDialog getInstance(ChemEqlGuiController parent) {
		if (INSTANCE == null) {
			INSTANCE = new LogKRangeDialog(parent);
		}
		return INSTANCE;
	}

	private double currentStart;
	private double currentEnd;
	private double currentStep;

	// Constructor for creating a bean
	public LogKRangeDialog() {
		initComponents();
	}

	private LogKRangeDialog(ChemEqlGuiController parent) {
		super(parent);
		initComponents();
		setLocation(300, 250);
		speciesCB.addItemListener(this);
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent ev) {
				speciesCB.setModel(main.matrix.createSpeciesNamesModel());
				speciesCB.setSelectedIndex(0);
				logKTF.setText(MyTools.EXACT_2_DIGITS.format(
						((Species)speciesCB.getItemAt(0)).constant));
				startTF.setText("");
				endTF.setText("");
				stepTF.setText("");
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
      java.awt.GridBagConstraints gridBagConstraints;
      javax.swing.JLabel label2;
      javax.swing.JLabel label3;
      javax.swing.JLabel label4;
      javax.swing.JLabel logKLabel;
      javax.swing.JLabel rangeLabel;
      javax.swing.JLabel selectLabel;

      selectLabel = new javax.swing.JLabel();
      speciesCB = new javax.swing.JComboBox();
      logKLabel = new javax.swing.JLabel();
      logKTF = new javax.swing.JTextField();
      rangeLabel = new javax.swing.JLabel();
      label2 = new javax.swing.JLabel();
      startTF = new KeyListeningTextField()
      {
         void keyPressedActivity()
         {
            checkInputs();
         }
      };

      label3 = new javax.swing.JLabel();
      endTF = new KeyListeningTextField()
      {
         void keyPressedActivity()
         {
            checkInputs();
         }
      };

      label4 = new javax.swing.JLabel();
      stepTF = new KeyListeningTextField()
      {
         void keyPressedActivity()
         {
            checkInputs();
         }
      };

      setTitle("log K range");
      selectLabel.setText("select:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
      getControls().add(selectLabel, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      getControls().add(speciesCB, gridBagConstraints);

      logKLabel.setText("log K:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 4;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
      getControls().add(logKLabel, gridBagConstraints);

      logKTF.setColumns(5);
      logKTF.setEditable(false);
      logKTF.setBorder(null);
      logKTF.setOpaque(false);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 5;
      gridBagConstraints.gridy = 0;
      getControls().add(logKTF, gridBagConstraints);

      rangeLabel.setText("log K range:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 6;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.insets = new java.awt.Insets(16, 0, 6, 0);
      getControls().add(rangeLabel, gridBagConstraints);

      label2.setText("from:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.insets = new java.awt.Insets(0, 13, 0, 4);
      getControls().add(label2, gridBagConstraints);

      startTF.setColumns(5);
      startTF.setMinimumSize(new java.awt.Dimension(59, 20));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      getControls().add(startTF, gridBagConstraints);

      label3.setText("to:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 4);
      getControls().add(label3, gridBagConstraints);

      endTF.setColumns(5);
      endTF.setMinimumSize(new java.awt.Dimension(59, 20));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      getControls().add(endTF, gridBagConstraints);

      label4.setText("step:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 4);
      getControls().add(label4, gridBagConstraints);

      stepTF.setColumns(5);
      stepTF.setMinimumSize(new java.awt.Dimension(59, 20));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      getControls().add(stepTF, gridBagConstraints);

      pack();
   }//GEN-END:initComponents

	public void itemStateChanged(ItemEvent e) {
		logKTF.setText(MyTools.EXACT_2_DIGITS.format(
				((Species)speciesCB.getSelectedItem()).constant));
	}

	private void checkInputs() {
		try {
			currentStart = Double.parseDouble(startTF.getText());
			currentEnd = Double.parseDouble(endTF.getText());
			currentStep = Double.parseDouble(stepTF.getText());
			proceedButton.setEnabled(true);
		} catch (NumberFormatException ex) {
			currentStart = Double.NaN;
			currentEnd = Double.NaN;
			currentStep = Double.NaN;
			proceedButton.setEnabled(false);
		}
	}

	protected void doCancel() {
		/*cancel, restore default*/
		main.logKrange = false;
		if (main.matrix.isHorHplusAndFree()) {
			main.pHrange = true;
		}
		if (main.matrix.adsorption()) {
			main.adsRange = true;
		}
		main.outputFormat = OutputFormat.REGULAR;
		main.setNumFormatToLinear();
		main.formatMenu.setDisable(true);
		main.graphMI.setDisable(true);
		main.pHrangeMI.setDisable(false);
		main.compRangeMI.setDisable(false);
		if (main.matrix.adsorption()) {
			main.adsRangeMI.setDisable(false);
		}
		super.doCancel();
	}

	protected void doProceed() {
		if (currentStart >= currentEnd) {
			MyTools.showError("Start logK must be smaller than end logK,"
					+ " step must be > 0!");
		}
		else if (currentStep <= 0) {
			MyTools.showError("Step must be > 0!");
		}
		else if (currentEnd - currentStart < currentStep) {
			MyTools.showError("Range too small or step to large!");
		}
		else if (main.matrix.oldConstantMemory[speciesCB.getSelectedIndex()] == 0) {
			MyTools.showError("This species is a component with log K=0. Do not change it!");
		}
		else {
			main.matrix.specNo = speciesCB.getSelectedIndex();
			main.matrix.logKrangeStart = currentStart;
			main.matrix.logKrangeEnd = currentEnd;
			main.matrix.logKrangeStep = currentStep;
			main.logKrange = true;
			main.compRange = false;
			main.pHrange = false;
			main.adsRange = false;
			main.outputFormat = OutputFormat.INTERVAL;
			main.graphMI.setDisable(false);
			main.formatMenu.setDisable(false);
			main.pHrangeMI.setDisable(true);
			main.compRangeMI.setDisable(true);
			main.adsRangeMI.setDisable(true);
			super.doProceed();
		}
	}
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JTextField endTF;
   private javax.swing.JTextField logKTF;
   private javax.swing.JComboBox speciesCB;
   private javax.swing.JTextField startTF;
   private javax.swing.JTextField stepTF;
   // End of variables declaration//GEN-END:variables
}
