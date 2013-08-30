package ch.eawag.chemeql;

import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


class NewSpeciesDialog extends ProceedCancelDialog
{
	private static NewSpeciesDialog INSTANCE;
	static NewSpeciesDialog getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new NewSpeciesDialog();
		return INSTANCE;
	}


	private Library library;
	private String newSpec;
	private int currentCoeff;
	private Species newSpecies;
	private int[] myStoichCoeffs;

	// Constructor for creating a bean
	public NewSpeciesDialog()
	{
		super();
		initComponents();

		nameTF.addFocusListener(new FocusAdapter()
		{
			public void focusLost(FocusEvent e)
			{
				if (nameTF.getText().length() == 0)
					nameTF.setText(newSpec);
				else
				{
					newSpec = nameTF.getText();
					if (library.nameAlreadyInUse(newSpec))
						MyTools.showWarning("A species with this name already exists!");
					plusEductButton.setEnabled(true);
					plusProductButton.setEnabled(true);
					equationTF.setText(library.equationFor(myStoichCoeffs,newSpec));
				}
			}
		});

		nameTF.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				nameTF.transferFocus();	// let focusLost above do the work
			}
		});

		coeffTF.addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				coeffTF.selectAll();
			}
		});
		
		plusEductButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateEquation(false);
			}
		});

		plusProductButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateEquation(true);
			}
		});

		setLocation(300,150);		
	}

	private void updateEquation(boolean isProduct)
	{
		// assert newSpec.length() > 0;
		try
		{
			currentCoeff = Integer.parseInt(coeffTF.getText());
			if (currentCoeff < 0)
				coeffTF.setText(Integer.toString(Math.abs(currentCoeff)));
			if (currentCoeff != 0)
				if ((currentCoeff > 0) == isProduct)
					currentCoeff = -currentCoeff;
			myStoichCoeffs[componentsCB.getSelectedIndex()] = currentCoeff;
			equationTF.setText(library.equationFor(myStoichCoeffs,newSpec));
		}
		catch (NumberFormatException ex)
		{
			MyTools.showError("Stoichometric coefficient must be an integer!");
		}
	}

	void show(Library lib)
	{
		if (library != lib)
		{
			library = lib;
			componentsCB.setModel(library.getComponentsCBModel());
		}
		componentsCB.setSelectedIndex(0);
		myStoichCoeffs = new int[library.libColumns];
			// automatically initialized with 0

		newSpec = "";

		nameTF.requestFocus();
		nameTF.setText("");
		logKTF.setText("");
		litTF.setText("");
		coeffTF.setText("1");
		equationTF.setText("");
		plusEductButton.setEnabled(false);
		plusProductButton.setEnabled(false);
		super.setVisible(true);
	}

	int[] stoichCoeffs()
	{
		return myStoichCoeffs;
	}

	Species newSpecies()
	{
		return newSpecies;
	}

	protected void doCancel()
	{
		newSpecies = null;
		super.doCancel();
	}

	protected void doProceed()
	{
		// assert newSpec.length() > 0;

		if (nameTF.getText().length() == 0)
			MyTools.showError("Please provide a name for the new Species!");
		else if (equationTF.getText().startsWith(Library.EQUALS_STRING))
			MyTools.showError("Equation still incomplete!");
		else
			try
			{
				double logK = Double.parseDouble(logKTF.getText());
				newSpecies = new Species(newSpec,logK,litTF.getText());
				super.doProceed();
			}
			catch (NumberFormatException ex)
			{
				MyTools.showError("Log K input is missing or in wrong format!");
			}
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
   private void initComponents()//GEN-BEGIN:initComponents
   {
      javax.swing.JLabel coeffLabel;
      java.awt.GridBagConstraints gridBagConstraints;
      javax.swing.JPanel kPanel;
      javax.swing.JLabel litLabel;
      javax.swing.JLabel logKLabel;
      javax.swing.JLabel nameLabel;

      nameLabel = new javax.swing.JLabel();
      nameTF = new javax.swing.JTextField();
      kPanel = new javax.swing.JPanel();
      logKLabel = new javax.swing.JLabel();
      logKTF = new javax.swing.JTextField();
      litLabel = new javax.swing.JLabel();
      litTF = new javax.swing.JTextField();
      coeffLabel = new javax.swing.JLabel();
      coeffTF = new javax.swing.JTextField();
      componentsCB = new javax.swing.JComboBox();
      plusEductButton = new javax.swing.JButton();
      plusProductButton = new javax.swing.JButton();
      equationTF = new javax.swing.JTextField();

      setTitle("Create New Species");
      nameLabel.setText("New Species:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
      getControls().add(nameLabel, gridBagConstraints);

      nameTF.setColumns(24);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      getControls().add(nameTF, gridBagConstraints);

      kPanel.setLayout(new java.awt.GridBagLayout());

      logKLabel.setFont(new java.awt.Font("Dialog", 0, 12));
      logKLabel.setText("Log K:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
      kPanel.add(logKLabel, gridBagConstraints);

      logKTF.setColumns(4);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
      kPanel.add(logKTF, gridBagConstraints);

      litLabel.setFont(new java.awt.Font("Dialog", 0, 12));
      litLabel.setText("Literatur:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
      kPanel.add(litLabel, gridBagConstraints);

      litTF.setColumns(4);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      kPanel.add(litTF, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
      getControls().add(kPanel, gridBagConstraints);

      coeffLabel.setText("Coeff.:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 4);
      getControls().add(coeffLabel, gridBagConstraints);

      coeffTF.setColumns(2);
      coeffTF.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 2);
      getControls().add(coeffTF, gridBagConstraints);

      componentsCB.setMaximumRowCount(16);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 0);
      getControls().add(componentsCB, gridBagConstraints);

      plusEductButton.setText("+ educt");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 4);
      getControls().add(plusEductButton, gridBagConstraints);

      plusProductButton.setText("+ product");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 3;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 0);
      getControls().add(plusProductButton, gridBagConstraints);

      equationTF.setEditable(false);
      equationTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
      equationTF.setBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED));
      equationTF.setMinimumSize(new java.awt.Dimension(100, 20));
      equationTF.setPreferredSize(new java.awt.Dimension(100, 20));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.gridwidth = 4;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.ipady = 6;
      gridBagConstraints.weightx = 3.0;
      gridBagConstraints.insets = new java.awt.Insets(8, 0, 4, 0);
      getControls().add(equationTF, gridBagConstraints);

      pack();
   }//GEN-END:initComponents

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JTextField coeffTF;
   private javax.swing.JComboBox componentsCB;
   private javax.swing.JTextField equationTF;
   private javax.swing.JTextField litTF;
   private javax.swing.JTextField logKTF;
   private javax.swing.JTextField nameTF;
   private javax.swing.JButton plusEductButton;
   private javax.swing.JButton plusProductButton;
   // End of variables declaration//GEN-END:variables
}
