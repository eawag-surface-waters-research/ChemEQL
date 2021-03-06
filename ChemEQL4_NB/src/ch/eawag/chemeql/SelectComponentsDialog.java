package ch.eawag.chemeql;

import java.awt.Frame;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


class SelectComponentsDialog extends JDialog
{
	private static SelectComponentsDialog INSTANCE;

	private Library library;
	private DefaultListModel libraryListModel;
	private DefaultListModel selectionListModel;
	private double currentConcentrationInput;
	private ChemEqlGuiController main;

	static SelectComponentsDialog getInstance(ChemEqlGuiController parent) {
		if (INSTANCE == null) {
			INSTANCE = new SelectComponentsDialog(parent);
		}
		return INSTANCE;
	}

	/** Creates new form SelectComponentsDialog */
	private SelectComponentsDialog(ChemEqlGuiController parent) {
		super((Frame)null, true);
		main = parent;
		libraryListModel = new DefaultListModel();
		selectionListModel = new DefaultListModel();
		initComponents();
	}

	void show(Library lib) {
		library = lib;
		libraryListModel.removeAllElements();
		for (int i = 0; i < lib.libTotComp; i++) {
			libraryListModel.addElement(library.libCompNames[i]);
		}
		selectionListModel.removeAllElements();

		concentrationTF.setText("");
		concentrationTF.setEnabled(false);
		totalModeButton.setSelected(true);
		totalModeButton.setEnabled(false);
		freeModeButton.setEnabled(false);
		addButton.setEnabled(false);
		removeButton.setEnabled(false);
		doneButton.setEnabled(false);

		super.setVisible(true);
	}

	private void concentrationChanged() {
		try {
			currentConcentrationInput =
					Double.parseDouble(concentrationTF.getText());
			addButton.setEnabled(true);
		} catch (NumberFormatException ex) {
			currentConcentrationInput = Double.NaN;
			addButton.setEnabled(false);
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
   private void initComponents()//GEN-BEGIN:initComponents
   {
      java.awt.GridBagConstraints gridBagConstraints;

      modeChoice = new javax.swing.ButtonGroup();
      libraryScrollPane = new javax.swing.JScrollPane();
      libraryList = new javax.swing.JList();
      controlsPanel = new javax.swing.JPanel();
      concentrationLabel = new javax.swing.JLabel();
      concentrationTF = new KeyListeningTextField()
      {
         void keyPressedActivity()
         {
            concentrationChanged();
         }
      };

      modeLabel = new javax.swing.JLabel();
      totalModeButton = new javax.swing.JRadioButton();
      freeModeButton = new javax.swing.JRadioButton();
      addButton = new javax.swing.JButton();
      removeButton = new javax.swing.JButton();
      separator = new javax.swing.JSeparator();
      cancelButton = new javax.swing.JButton();
      doneButton = new javax.swing.JButton();
      selectionScrollPane = new javax.swing.JScrollPane();
      selectionList = new javax.swing.JList();

      getContentPane().setLayout(new java.awt.GridBagLayout());

      setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
      setTitle("Select Components");
      setLocationRelativeTo(cancelButton);
      setModal(true);
      addWindowListener(new java.awt.event.WindowAdapter()
      {
         public void windowClosing(java.awt.event.WindowEvent evt)
         {
            dialogClosing(evt);
         }
      });

      libraryScrollPane.setBorder(new javax.swing.border.TitledBorder(null, "ChemEQL Library", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
      libraryList.setModel(libraryListModel);
      libraryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      libraryList.setPrototypeCellValue("abcdefgh");
      libraryList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
      {
         public void valueChanged(javax.swing.event.ListSelectionEvent evt)
         {
            libraryListValueChanged(evt);
         }
      });

      libraryScrollPane.setViewportView(libraryList);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      getContentPane().add(libraryScrollPane, gridBagConstraints);

      controlsPanel.setLayout(new java.awt.GridBagLayout());

      concentrationLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      concentrationLabel.setText("Concentration:");
      concentrationLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
      concentrationLabel.setAlignmentX(0.5F);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(0, 8, 4, 8);
      controlsPanel.add(concentrationLabel, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
      controlsPanel.add(concentrationTF, gridBagConstraints);

      modeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
      modeLabel.setText("Mode:");
      modeLabel.setAlignmentX(0.5F);
      modeLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridheight = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 6);
      controlsPanel.add(modeLabel, gridBagConstraints);

      totalModeButton.setSelected(true);
      totalModeButton.setText("total");
      modeChoice.add(totalModeButton);
      totalModeButton.setAlignmentX(0.5F);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 18);
      controlsPanel.add(totalModeButton, gridBagConstraints);

      freeModeButton.setText("free");
      modeChoice.add(freeModeButton);
      freeModeButton.setAlignmentX(0.5F);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 18);
      controlsPanel.add(freeModeButton, gridBagConstraints);

      addButton.setText(">> Add >>");
      addButton.setAlignmentX(0.5F);
      SwingUtilities.getRootPane(this).setDefaultButton(addButton);
      addButton.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            addButtonActionPerformed(evt);
         }
      });

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
      controlsPanel.add(addButton, gridBagConstraints);

      removeButton.setText("<< Remove <<");
      removeButton.setAlignmentX(0.5F);
      removeButton.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            removeButtonActionPerformed(evt);
         }
      });

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
      controlsPanel.add(removeButton, gridBagConstraints);

      separator.setForeground(new java.awt.Color(0, 0, 0));
      separator.setPreferredSize(new java.awt.Dimension(4, 4));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(20, 8, 8, 8);
      controlsPanel.add(separator, gridBagConstraints);

      cancelButton.setText("Cancel");
      cancelButton.setAlignmentX(0.5F);
      cancelButton.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            cancelButtonActionPerformed(evt);
         }
      });

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
      controlsPanel.add(cancelButton, gridBagConstraints);

      doneButton.setText("Compile Matrix");
      doneButton.setAlignmentX(0.5F);
      doneButton.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            doneButtonActionPerformed(evt);
         }
      });

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
      controlsPanel.add(doneButton, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
      gridBagConstraints.weighty = 1.0;
      getContentPane().add(controlsPanel, gridBagConstraints);

      selectionScrollPane.setBorder(new javax.swing.border.TitledBorder(null, "Matrix Selection", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
      selectionList.setModel(selectionListModel);
      selectionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      selectionList.setPrototypeCellValue("abcdefgh");
      selectionList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
      {
         public void valueChanged(javax.swing.event.ListSelectionEvent evt)
         {
            selectionListValueChanged(evt);
         }
      });

      selectionScrollPane.setViewportView(selectionList);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      getContentPane().add(selectionScrollPane, gridBagConstraints);

      java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
      setBounds((screenSize.width-400)/2, (screenSize.height-400)/2, 400, 400);
   }//GEN-END:initComponents

	private void dialogClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_dialogClosing
	{//GEN-HEADEREND:event_dialogClosing
		// Add your handling code here:
		doCancel();
	}//GEN-LAST:event_dialogClosing

	private void doneButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_doneButtonActionPerformed
	{//GEN-HEADEREND:event_doneButtonActionPerformed
		// construct Matrix from selected components data
		main.matrix.buildMyChoiceMatrixAndTransfer(
				library, selectionListModel.toArray());		// sets main.matrixIsLoaded to true
		main.matrix.defaultsDatInput();	/*setzt Defaultwerte, Comp.Reihenfolge etc.*/

		main.matrix.adjustConcEstim();	/*neu zählen von noOfSolidPhases, noOfCheckPrecip und totLimComp und einsetzen der Konz.*/

		doClose();
	}//GEN-LAST:event_doneButtonActionPerformed

	private void selectionListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_selectionListValueChanged
	{//GEN-HEADEREND:event_selectionListValueChanged
		removeButton.setEnabled(true);
	}//GEN-LAST:event_selectionListValueChanged

	private void libraryListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_libraryListValueChanged
	{//GEN-HEADEREND:event_libraryListValueChanged
		// Add your handling code here:
		concentrationTF.setEnabled(true);
		totalModeButton.setEnabled(true);
		freeModeButton.setEnabled(true);
		concentrationTF.requestFocusInWindow();
	}//GEN-LAST:event_libraryListValueChanged

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
	{//GEN-HEADEREND:event_cancelButtonActionPerformed
		// Add your handling code here:
		doCancel();
	}//GEN-LAST:event_cancelButtonActionPerformed

	private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
	{//GEN-HEADEREND:event_removeButtonActionPerformed
		// Add your handling code here:
		DefaultListModel selList = (DefaultListModel)selectionList.getModel();
		int selI = selectionList.getSelectedIndex();
		((DefaultListModel)libraryList.getModel()).addElement(
				((Component)selList.remove(selI)).toString());
		removeButton.setEnabled(false);
		if (selList.size() == 1) {
			doneButton.setEnabled(false);
		}
	}//GEN-LAST:event_removeButtonActionPerformed

	private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
	{//GEN-HEADEREND:event_addButtonActionPerformed
		// Add your handling code here:
		if (freeModeButton.isSelected() && currentConcentrationInput == 0) {
			MyTools.showError(
					"Input not sensible:\nFree concentration should never be zero!");
			return;
		}
		DefaultListModel libList = (DefaultListModel)libraryList.getModel();
		DefaultListModel selList = (DefaultListModel)selectionList.getModel();
		int selI = libraryList.getSelectedIndex();
		selList.addElement(new Component((String)libList.remove(selI),
				currentConcentrationInput,
				freeModeButton.isSelected() ? Mode.FREE : Mode.TOTAL));
		concentrationTF.setText("");
		concentrationTF.setEnabled(false);
		addButton.setEnabled(false);
		totalModeButton.setEnabled(false);
		freeModeButton.setEnabled(false);
		if (selList.size() == 2) {
			doneButton.setEnabled(true);
		}
	}//GEN-LAST:event_addButtonActionPerformed

	private void doCancel() {
		int dialogResult = JOptionPane.showConfirmDialog(
				this, "Do you want to dismiss dialog Select Components\nand all changes made therein?",
				ChemEql.APP_TITLE, JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			// assert dialogResult == JOptionPane.NO_OPTION || dialogResult == JOptionPane.CLOSED_OPTION;
			main.matrixIsLoaded = false;	// absence of this line causes errors in ChemEQL V203
			try {
				Thread.sleep(200);
			} // solves indeterministic error on Mac OS
			catch (InterruptedException ex) {
			}
			doClose();
		}
	}

	private void doClose() {
		setVisible(false);
	}

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton addButton;
   private javax.swing.JButton cancelButton;
   private javax.swing.JLabel concentrationLabel;
   private javax.swing.JTextField concentrationTF;
   private javax.swing.JPanel controlsPanel;
   private javax.swing.JButton doneButton;
   private javax.swing.JRadioButton freeModeButton;
   private javax.swing.JList libraryList;
   private javax.swing.JScrollPane libraryScrollPane;
   private javax.swing.ButtonGroup modeChoice;
   private javax.swing.JLabel modeLabel;
   private javax.swing.JButton removeButton;
   private javax.swing.JList selectionList;
   private javax.swing.JScrollPane selectionScrollPane;
   private javax.swing.JSeparator separator;
   private javax.swing.JRadioButton totalModeButton;
   // End of variables declaration//GEN-END:variables

}
