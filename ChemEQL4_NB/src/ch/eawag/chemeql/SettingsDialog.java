package ch.eawag.chemeql;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;


class SettingsDialog extends ProceedCancelDialog implements ItemListener
{
	private static SettingsDialog INSTANCE;

	static SettingsDialog getInstance(ChemEqlGuiController parent) {
		if (INSTANCE == null) {
			INSTANCE = new SettingsDialog(parent);
		}
		return INSTANCE;
	}

	private static final String[] CONCENTRATION_FORMATS =
			new String[]{"0.0E0", "0.00E0", "0.000E0", "0.0000E0", "0.00000E0"};
	private static final String[] NONEXPO_CONCENTRATION_FORMATS =
			new String[]{"0.0", "0.00", "0.000", "0.0000", "0.00000"};
	private static final String[] CONSTANT_FORMATS =
			new String[]{"0.0", "0.00", "0.000", "0.0000"};

	private int concentrationFormatIndex;
	DecimalFormat concentrationFormat;
	DecimalFormat nonExpoConcentrationFormat;
	private int constantFormatIndex;
	DecimalFormat constantFormat;

	// Constructor for creating a bean
	public SettingsDialog() {
		initComponents();
	}

	private SettingsDialog(ChemEqlGuiController main) {
		super();
		initComponents();
		setLocation(300, 250);

		Preferences pref = Preferences.userRoot();
		concentrationFormatIndex = pref.getInt("concentrationDigits", 2);
		constantFormatIndex = pref.getInt("constantDigits", 0);

		concentrationFormat = new DecimalFormat(
				CONCENTRATION_FORMATS[concentrationFormatIndex]);
		nonExpoConcentrationFormat = new DecimalFormat(
				NONEXPO_CONCENTRATION_FORMATS[concentrationFormatIndex]);
		concentrationsChoice.addItemListener(this);
		constantFormat = new DecimalFormat(
				CONSTANT_FORMATS[constantFormatIndex]);
		constantsChoice.addItemListener(this);

		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e) {
				concentrationsChoice.setSelectedIndex(concentrationFormatIndex);
				constantsChoice.setSelectedIndex(constantFormatIndex);
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
      javax.swing.JLabel label1;
      javax.swing.JLabel label2;
      javax.swing.JLabel label3;

      label1 = new javax.swing.JLabel();
      label2 = new javax.swing.JLabel();
      label3 = new javax.swing.JLabel();
      concentrationsChoice = new JComboBox(CONCENTRATION_FORMATS);
      constantsChoice = new JComboBox(CONSTANT_FORMATS);

      setTitle("Settings");
      label1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      label1.setText("Define number of decimal places");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.ipadx = 20;
      gridBagConstraints.insets = new java.awt.Insets(6, 0, 10, 0);
      getControls().add(label1, gridBagConstraints);

      label2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      label2.setText("Concentrations:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.ipadx = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      getControls().add(label2, gridBagConstraints);

      label3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      label3.setText("Constants:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.ipadx = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      getControls().add(label3, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(3, 4, 3, 0);
      getControls().add(concentrationsChoice, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(3, 4, 6, 0);
      getControls().add(constantsChoice, gridBagConstraints);

      pack();
   }//GEN-END:initComponents

	public void itemStateChanged(ItemEvent e) {
		proceedButton.setEnabled(true);
	}

	protected void doProceed() {
		concentrationFormatIndex = concentrationsChoice.getSelectedIndex();
		concentrationFormat.applyPattern(
				CONCENTRATION_FORMATS[concentrationFormatIndex]);
		nonExpoConcentrationFormat.applyPattern(
				NONEXPO_CONCENTRATION_FORMATS[concentrationFormatIndex]);
		constantFormatIndex = constantsChoice.getSelectedIndex();
		constantFormat.applyPattern(
				CONSTANT_FORMATS[constantFormatIndex]);
		Preferences pref = Preferences.userRoot();
		pref.putInt("concentrationDigits", concentrationFormatIndex);
		pref.putInt("constantDigits", constantFormatIndex);
		main.repaintDataWindows();
		super.doProceed();
	}

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox concentrationsChoice;
   private javax.swing.JComboBox constantsChoice;
   // End of variables declaration//GEN-END:variables

}
