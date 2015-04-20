package ch.eawag.chemeql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Format;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.PlainDocument;


class OutputWindow extends JInternalFrame
{
	private ChemEQL3 main;
	private Matrix matrix;

	private JScrollPane scroller;

	private DataTable intervalTable;
	private PxPyTable pxpyTable;
	private JPanel regularPanel;
	private JTextArea notesTA;
	private SpeciesTable speciesTable;
	private ComponentsTable componentsTable;
	private JTextField activityTF;

	public OutputWindow(ChemEQL3 main, Matrix matrix) {
		super("Data", true, true, true, true);
		this.main = main;
		this.matrix = matrix;
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

//		bufferCount = 0;		/*prepare for output*/
//		readCount = 0;
//		outputTitleFlag = false;
		// create components
		regularPanel = createRegularPanel();
		intervalTable = new DataTable(main);
		pxpyTable = new PxPyTable(main, matrix.getPxPyOutputTableModel());

		// add components
		scroller = new JScrollPane();
		scroller.setOpaque(false);
		scroller.getViewport().setOpaque(false);
		getContentPane().add(scroller, BorderLayout.CENTER);

		// textfield for (optionally) displaying notes while calculating
		PlainDocument doc = matrix.getNotesDoc();
		notesTA = new JTextArea(doc);
		doc.addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e) {
				notesTA.setVisible(true);
			}

			public void removeUpdate(DocumentEvent e) {
				if (notesTA.getText().length() == 0) {
					notesTA.setVisible(false);
				}
			}

			public void changedUpdate(DocumentEvent e) {
			}
		});
		notesTA.setEditable(false);
		notesTA.setFont(speciesTable.getFont());
		notesTA.setOpaque(false);
		notesTA.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 0));
		getContentPane().add(notesTA, BorderLayout.NORTH);
		notesTA.setVisible(false);
	}

	private JPanel createRegularPanel() {
		JPanel panel;

		Dimension gap = new Dimension(6, 0);
		speciesTable = new SpeciesTable(
				main, matrix.getSpeciesOutputTableModel());
		speciesTable.setIntercellSpacing(gap);
		speciesTable.setRowSelectionAllowed(true);
		componentsTable = new ComponentsTable(
				main, matrix.getComponentsOutputTableModel());
		componentsTable.setIntercellSpacing(gap);
		componentsTable.setRowSelectionAllowed(true);
		activityTF = new JTextField();
		activityTF.setBorder(BorderFactory.createEmptyBorder());
		activityTF.setEditable(false);

		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[]{1.0f};
		panel = new JPanel(gbl);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, gap.width / 2, 0, 0);
		panel.add(speciesTable.getTableHeader(), gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(speciesTable, gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(20, 20 + gap.width / 2, 0, 0);
		panel.add(componentsTable.getTableHeader(), gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 20, 0, 0);
		panel.add(componentsTable, gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(20, 6, 0, 0);
		panel.add(activityTF, gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 1.0f;
		panel.add(new JPanel(), gbc);

		return panel;
	}

	void update() {
		try {
			SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run() {
					if (!isVisible()) {
						setVisible(true);
						toBack();
					}
					switch (main.outputFormat) {
					case REGULAR:
						updateRegular();
						break;
					case INTERVAL:
						updateInterval();
						break;
					// Aufzeichnen(DrawIntervalLinear, outputWindow);
					// Aufzeichnen(DrawLogarithmic, outputWindow);
					case PXPY_DIAGRAM:
						updatePxPy();
						break; // Aufzeichnen(DrawpXpYdata, outputWindow);
					}
				}
			});
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}

	private void updateRegular() {
		scroller.setViewportView(regularPanel);

		boolean printInteger = matrix.speciesMatrixAllInt();

		((AbstractTableModel)speciesTable.getModel()).fireTableStructureChanged();
		TableColumnModel cm = speciesTable.getColumnModel();
		int r = 0;
		setColumnWidths(cm.getColumn(r++), 60, 150);
		int width = matrix.totComp * (printInteger ? 18 : 32);
		setColumnWidths(cm.getColumn(r++), width, width + 30);
		setColumnWidths(cm.getColumn(r++), 50, 90);
		setColumnWidths(cm.getColumn(r++), 70, 110);
		if (matrix.calculatedWithActivity) {
			setColumnWidths(cm.getColumn(r++), 70, 110);
		}
		setColumnWidths(cm.getColumn(r++), 50, 80);

		((AbstractTableModel)componentsTable.getModel()).fireTableStructureChanged();
		cm = componentsTable.getColumnModel();
		setColumnWidths(cm.getColumn(0), 80, 160);
		setColumnWidths(cm.getColumn(1), 60, 90);
		setColumnWidths(cm.getColumn(2), 60, 90);
		setColumnWidths(cm.getColumn(3), 100, 130);

		if (matrix.calculatedWithActivity) {
			activityTF.setText("Ionic strength:  "
					+ main.settingsDialog.concentrationFormat.format(matrix.ionicStr)
					+ "       Approximation:  " + main.getCalcActivCoeff());
		}
		activityTF.setVisible(matrix.calculatedWithActivity);
	}

	private void updateInterval() {
		intervalTable.startItalic = matrix.startItalics;
		intervalTable.stopItalic = matrix.stopItalics;
		intervalTable.setModel(matrix.getDataTableModel());
		scroller.setViewportView(intervalTable);
		TableColumnModel cm = intervalTable.getColumnModel();
		for (int i = 0; i < cm.getColumnCount(); i++) {
			setColumnWidths(cm.getColumn(i), 80, 150);
		}
	}

	private void updatePxPy() {
		((AbstractTableModel)pxpyTable.getModel()).fireTableStructureChanged();
		scroller.setViewportView(pxpyTable);
	}

	private void setColumnWidths(TableColumn col, int min, int max) {
		col.setPreferredWidth(min);
		col.setMaxWidth(max);
	}

	void saveData(File outFile) throws IOException {
		java.awt.Component dataView = scroller.getViewport().getView();
		Format f = main.settingsDialog.concentrationFormat;
		FileWriter out = new FileWriter(outFile);
		if (dataView == regularPanel) {
			speciesTable.appendTableData(out, f);
			out.write(Tokenizer.CR);
			out.write(Tokenizer.CR);
			componentsTable.appendTableData(out, f);
		}
		else if (dataView == intervalTable) {
			intervalTable.appendTableData(out, f);
		}
		else if (dataView == pxpyTable) {
			pxpyTable.appendTableData(out, f);
		}
		out.close();
	} // end of saveData


	// --- custom table for species data
	static class SpeciesTable extends MyTable
	{
		private ChemEQL3 main;
		private TableCellRenderer speciesRenderer;
		private TableCellRenderer matrixRenderer;
		private TableCellRenderer logKRenderer;
		private TableCellRenderer concRenderer;

		SpeciesTable(ChemEQL3 main, TableModel model) {
			super(model, false);
			this.main = main;
			speciesRenderer = new CustomRenderer(getFont().deriveFont(Font.BOLD));
			matrixRenderer = new CustomRenderer(getFont(), SwingConstants.CENTER);
			logKRenderer = new CustomConstantsRenderer(
					getFont(), SwingConstants.RIGHT, main);
			concRenderer = new CustomConcentrationsRenderer(
					getFont().deriveFont(Font.BOLD), SwingConstants.RIGHT, main);
		}

		public TableCellRenderer getCellRenderer(int row, int col) {
			int c = 0;
			if (col == c) {
				return speciesRenderer;
			}
			if (col == ++c) {
				return matrixRenderer;
			}
			if (col == ++c) {
				return logKRenderer;
			}
			if (col == ++c) {
				return concRenderer;
			}
			if (main.matrix.calculatedWithActivity) {
				if (col == ++c) {
					return concRenderer;
				}
			}
			// assert col == ++c
			return logKRenderer;
		}
	}


	// --- custom table for component data
	static class ComponentsTable extends MyTable
	{
		private TableCellRenderer componentRenderer;
		private TableCellRenderer modeRenderer;
		private TableCellRenderer concRenderer;

		ComponentsTable(ChemEQL3 main, TableModel model) {
			super(model, false);

			componentRenderer = new CustomRenderer(getFont().deriveFont(Font.BOLD));
			modeRenderer = new CustomRenderer(getFont(), SwingConstants.CENTER);
			concRenderer = new CustomConcentrationsRenderer(
					getFont(), SwingConstants.CENTER, main);
		}

		public TableCellRenderer getCellRenderer(int row, int col) {
			switch (col) {
			case 0:
				return componentRenderer;
			case 1:
				return modeRenderer;
			case 2:
				return concRenderer;
			case 3:
				return concRenderer;
			default:
				return null;
			}
		}
	}

	// --- custom table for output data

	static class DataTable extends MyTable
	{
		private TableCellRenderer linRenderer;
		private TableCellRenderer logRenderer;
		private TableCellRenderer italicLinRenderer;
		private TableCellRenderer italicLogRenderer;
		int startItalic;
		int stopItalic;

		DataTable(ChemEQL3 main) {
			super(new DefaultTableModel(), true);
			linRenderer = new CustomConcentrationsRenderer(
					getFont(), SwingConstants.RIGHT, main);
			logRenderer = new CustomNonExpoConcentrationsRenderer(
					getFont(), SwingConstants.RIGHT, main);
			italicLinRenderer = new CustomConcentrationsRenderer(
					getFont().deriveFont(Font.ITALIC), SwingConstants.RIGHT, main);
			italicLogRenderer = new CustomNonExpoConcentrationsRenderer(
					getFont().deriveFont(Font.ITALIC), SwingConstants.RIGHT, main);
		}

		public TableCellRenderer getCellRenderer(int row, int col) {
			Object val = getValueAt(row, col);
			if (val instanceof String) {
				return stringRenderer;
			}
			if (col >= startItalic && col <= stopItalic) {
				if (val instanceof Float) {
					return italicLogRenderer;
				}
				return italicLinRenderer;
			}
			if (val instanceof Float) {
				return logRenderer;
			}
			return linRenderer;
		}
	}

	// --- custom table for pXpY data

	static class PxPyTable extends MyTable
	{
		private TableCellRenderer numRenderer;
		private TableCellRenderer strRenderer;

		PxPyTable(ChemEQL3 main, TableModel model) {
			super(model, true);
			numRenderer = new CustomNonExpoConcentrationsRenderer(
					getFont(), SwingConstants.RIGHT, main);
			strRenderer = new CustomRenderer(getFont(), SwingConstants.CENTER);
		}

		public TableCellRenderer getCellRenderer(int row, int col) {
			return col < 2 ? numRenderer : strRenderer;
		}
	}

}
