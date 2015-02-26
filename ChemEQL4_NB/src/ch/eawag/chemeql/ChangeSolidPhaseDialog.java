package ch.eawag.chemeql;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;


class ChangeSolidPhaseDialog extends ProceedCancelDialog
{
	private static ChangeSolidPhaseDialog INSTANCE;
	static ChangeSolidPhaseDialog getInstance(ChemEQL3 parent)
	{
		if (INSTANCE == null)
			INSTANCE = new ChangeSolidPhaseDialog(parent);
		return INSTANCE;
	}

	private double currentLogInput;

	private ChangeSolidPhaseDialog(ChemEQL3 parent)
	{
		super(parent);
		initComponents();
		replacementCB.setMaximumRowCount(20);
		componentCB.setPrototypeDisplayValue("CaH2Si=4[s]");
		replacementCB.setPrototypeDisplayValue("CaH2Si=4[s]");
		setLocation(600,150);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent arg0)
			{
				if (main.spLibrary == null) // falls Library noch nicht offen
					main.spLibrary = Library.readBinLibrary(main,false);
				if (main.spLibrary == null) return;	// Error while reading library: Exit!

				proceedButton.setEnabled(false);
				componentCB.setModel(main.matrix.createSpecialComponentsCBModel());
				int i=0;
				while(((Component)componentCB.getItemAt(i)).isModeSolidPhaseOrCheckPrecip())
					i++;
				componentCB.setSelectedIndex(i);
				componentCB.setRenderer(new DefaultListCellRenderer()
				{
					public java.awt.Component getListCellRendererComponent(JList list, Object value,
						int index, boolean isSelected, boolean cellHasFocus)
					{
						java.awt.Component comp = super.getListCellRendererComponent(
							list,value,index,isSelected,cellHasFocus);
						if (((Component)value).isModeSolidPhaseOrCheckPrecip())
							comp.setEnabled(false);
						return comp;
					}
				});
				replacementCB.setModel(main.spLibrary.getSpeciesCBModel());
				replacementCB.setSelectedIndex(0);
			}
		});
	}

	private void logChanged()
	{
		try
		{
			currentLogInput = Double.parseDouble(logTF.getText());
			proceedButton.setEnabled(true);
			litTF.setText("");
		}
		catch (NumberFormatException ex)
		{
			currentLogInput = Double.NaN;
			proceedButton.setEnabled(false);
		}
	}


   private void initComponents()//GEN-BEGIN:initComponents
   {
      java.awt.GridBagConstraints gridBagConstraints;

      componentLabel = new javax.swing.JLabel();
      componentCB = new javax.swing.JComboBox();
      replacementLabel = new javax.swing.JLabel();
      replacementCB = new javax.swing.JComboBox();
      logKsoPanel = new javax.swing.JPanel();
      logTF = new KeyListeningTextField()
      {
         void keyPressedActivity()
         {
            logChanged();
         }
      };
      litTF = new javax.swing.JTextField();
      equationTF = new javax.swing.JTextField();

      setTitle("Insert Solid Phase");
      componentLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      componentLabel.setText("Replace");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
      getControls().add(componentLabel, gridBagConstraints);

      componentCB.setMinimumSize(new java.awt.Dimension(250, 25));
      componentCB.setPreferredSize(new java.awt.Dimension(250, 25));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 4.0;
      getControls().add(componentCB, gridBagConstraints);

      replacementLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
      replacementLabel.setText("with");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
      getControls().add(replacementLabel, gridBagConstraints);

      replacementCB.setMinimumSize(new java.awt.Dimension(250, 25));
      replacementCB.setPreferredSize(new java.awt.Dimension(250, 25));
      replacementCB.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            replacementCBActionPerformed(evt);
         }
      });

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 4.0;
      getControls().add(replacementCB, gridBagConstraints);

      logKsoPanel.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

      logKsoPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)), " logKso ", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
      logKsoPanel.setFont(new java.awt.Font("Dialog", 1, 12));
      logTF.setColumns(7);
      logTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
      logTF.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            logTFActionPerformed(evt);
         }
      });

      logKsoPanel.add(logTF);

      litTF.setColumns(7);
      litTF.setEditable(false);
      litTF.setFont(new java.awt.Font("Dialog", 0, 10));
      litTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
      litTF.setBorder(null);
      litTF.setOpaque(false);
      logKsoPanel.add(litTF);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridheight = 2;
      gridBagConstraints.insets = new java.awt.Insets(0, 10, 16, 0);
      getControls().add(logKsoPanel, gridBagConstraints);

      equationTF.setEditable(false);
      equationTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
      equationTF.setBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED));
      equationTF.setMinimumSize(new java.awt.Dimension(100, 20));
      equationTF.setPreferredSize(new java.awt.Dimension(100, 20));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.ipady = 6;
      gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
      getControls().add(equationTF, gridBagConstraints);

      pack();
   }//GEN-END:initComponents

	private void logTFActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_logTFActionPerformed
	{//GEN-HEADEREND:event_logTFActionPerformed
		// Add your handling code here:
	}//GEN-LAST:event_logTFActionPerformed

	private void replacementCBActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_replacementCBActionPerformed
	{//GEN-HEADEREND:event_replacementCBActionPerformed
		// Add your handling code here:
		Species selSpec = (Species)replacementCB.getSelectedItem();
		logTF.setText(Double.toString(selSpec.constant));
		litTF.setText(selSpec.source.replace(Tokenizer.TAB,' '));
		equationTF.setText(
			main.spLibrary.equationFor(replacementCB.getSelectedIndex()));
	}//GEN-LAST:event_replacementCBActionPerformed

	protected void doProceed()
	{
		((Species)replacementCB.getSelectedItem()).constant = currentLogInput;
		if (main.matrix.recalculate(
			componentCB.getSelectedIndex(),replacementCB.getSelectedIndex()))
		{
			/*Komponenten neu ordnen, da ein 'total' zu einem 'solidPhase' geworden ist!*/
			main.matrix.reorderComponents();
			super.doProceed();
		}
	}

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox componentCB;
   private javax.swing.JLabel componentLabel;
   private javax.swing.JTextField equationTF;
   private javax.swing.JTextField litTF;
   private javax.swing.JPanel logKsoPanel;
   private javax.swing.JTextField logTF;
   private javax.swing.JComboBox replacementCB;
   private javax.swing.JLabel replacementLabel;
   // End of variables declaration//GEN-END:variables
}
