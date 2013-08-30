package ch.eawag.chemeql;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;


class EditComponentsDialog extends ProceedCancelDialog
{
	private static EditComponentsDialog INSTANCE;
	static EditComponentsDialog getInstance(Main parent)
	{
		if (INSTANCE == null)
			INSTANCE = new EditComponentsDialog(parent);
		return INSTANCE;
	}
	
	private Library library;

	// Constructor for creating a bean
	public EditComponentsDialog()
	{
		initComponents();
	}

	private EditComponentsDialog(Main parent)
	{
		super(parent);
		getRootPane().setDefaultButton(null);
		initComponents();

		componentsScroller.setMinimumSize(componentsScroller.getPreferredSize());
		componentsScroller.setMaximumSize(componentsScroller.getPreferredSize());
		componentsList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent evt)
			{
				updateData();
			}
		});

		acceptButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				String newName = nameTF.getText();
				int l = newName.length();
				if (l == 0)
					MyTools.showError("Please provide a name!");
				else if (l > 50)
					MyTools.showError("Name too long!");
				else
				{
					library.changedComponentName(
						newName,componentsList.getSelectedIndex());
					resetButton.setEnabled(false);
					acceptButton.setEnabled(false);
					proceedButton.setEnabled(true);
				}
			}
		});

		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				updateData();
			}
		});

		newButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				String newComp = JOptionPane.showInputDialog(
					EditComponentsDialog.this,
					"Name of new component?",Main.AN,JOptionPane.QUESTION_MESSAGE);
				if (newComp == null || newComp.length() == 0)
					; // user canceled dialog
				else
				{
					int i = componentsList.getSelectedIndex();
					library.insertComponent(newComp,i);
					componentsList.ensureIndexIsVisible(i);
					componentsList.setSelectedIndex(i);
					proceedButton.setEnabled(true);
				}
			}
		});

		deleteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				int i = componentsList.getSelectedIndex();
				componentsList.clearSelection();
				library.deleteComponentAtIndex(i);
				proceedButton.setEnabled(true);
			}
		});
		setLocation(100,100);
	}

	void show(Library lib)
	{
		if (library != lib)
		{
			library = lib;
			setTitle("Edit Components (" + library.libraryType() + " Library)");
			componentsList.setModel(library.getComponentsListModel());
			componentsList.ensureIndexIsVisible(0);
		}
		componentsList.clearSelection();
		setEditPanelEnabled(false);
		resetButton.setEnabled(false);
		acceptButton.setEnabled(false);
		newButton.setEnabled(false);
		deleteButton.setEnabled(false);
		proceedButton.setEnabled(false);
		super.setVisible(true);
	}

	private void updateData()
	{
		int i = componentsList.getSelectedIndex();
		componentsList.ensureIndexIsVisible(i);
		boolean compSelected = i >= 0;
		setEditPanelEnabled(compSelected);
		nameTF.setText(compSelected ? library.libCompNames[i] : "");
		resetButton.setEnabled(false);
		acceptButton.setEnabled(false);
		newButton.setEnabled(compSelected);
		deleteButton.setEnabled(compSelected);
	}

	private void setEditPanelEnabled(boolean enabled)
	{
		nameTF.setEnabled(enabled);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
   private void initComponents()//GEN-BEGIN:initComponents
   {
      javax.swing.JPanel editPanel;
      java.awt.GridBagConstraints gridBagConstraints;

      componentsScroller = new javax.swing.JScrollPane();
      componentsList = new javax.swing.JList();
      editPanel = new javax.swing.JPanel();
      nameTF = new KeyListeningTextField()
      {
         void keyPressedActivity()
         {
            didEdit();
         }
      };

      buttonPanel = new javax.swing.JPanel();
      resetButton = new javax.swing.JButton();
      acceptButton = new javax.swing.JButton();
      newButton = new javax.swing.JButton();
      deleteButton = new javax.swing.JButton();
      filler = new javax.swing.JPanel();

      componentsScroller.setBorder(new javax.swing.border.TitledBorder(null, "Component Selection", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
      componentsScroller.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      componentsScroller.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      componentsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      componentsList.setVisibleRowCount(12);
      componentsScroller.setViewportView(componentsList);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridheight = 4;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
      getControls().add(componentsScroller, gridBagConstraints);

      editPanel.setLayout(new java.awt.GridBagLayout());

      editPanel.setBorder(new javax.swing.border.TitledBorder(null, "Edit Name", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
      nameTF.setColumns(24);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
      editPanel.add(nameTF, gridBagConstraints);

      buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 6));

      resetButton.setText("Reset");
      buttonPanel.add(resetButton);

      acceptButton.setText("Accept");
      buttonPanel.add(acceptButton);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
      editPanel.add(buttonPanel, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
      getControls().add(editPanel, gridBagConstraints);

      newButton.setText("Insert new Component before");
      newButton.setMaximumSize(new java.awt.Dimension(120, 26));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 12);
      getControls().add(newButton, gridBagConstraints);

      deleteButton.setText("Delete Component");
      deleteButton.setMaximumSize(new java.awt.Dimension(120, 26));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(18, 12, 30, 12);
      getControls().add(deleteButton, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
      getControls().add(filler, gridBagConstraints);

      pack();
   }//GEN-END:initComponents
	
	private void didEdit()
	{
		resetButton.setEnabled(true);
		acceptButton.setEnabled(true);
	}

	protected void doCancel()
	{
		if (proceedButton.isEnabled())
		{
			if (JOptionPane.showConfirmDialog(this,
				"Do you really want to dismiss all changes made in this dialog?",
				Main.AN,JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION)
			{
				main.reloadLibrary(library);
				super.doCancel();
			}
		}
		else
			super.doCancel();
	}
	
	protected void doProceed()
	{
		library.writeBinary();
		super.doProceed();
	}

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton acceptButton;
   private javax.swing.JPanel buttonPanel;
   private javax.swing.JList componentsList;
   private javax.swing.JScrollPane componentsScroller;
   private javax.swing.JButton deleteButton;
   private javax.swing.JPanel filler;
   private javax.swing.JTextField nameTF;
   private javax.swing.JButton newButton;
   private javax.swing.JButton resetButton;
   // End of variables declaration//GEN-END:variables
}
