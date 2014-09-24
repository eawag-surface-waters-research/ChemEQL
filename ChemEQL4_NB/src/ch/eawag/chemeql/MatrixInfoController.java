/*
 * The MIT License
 *
 *  Copyright (c) 2013 Beat Müller, www.eawag.ch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ch.eawag.chemeql;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


/**
 * FXML Controller class for Matrices
 *
 * @author kaibrassel
 */
public class MatrixInfoController
{
	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TableView<?> componentsTable;

	@FXML
	private TableView<?> speciesTable;

	@FXML
	private TextField multiCalculationTF;

	@FXML
	private TableView<?> adsorptionParametersTable;

	@FXML
	private TextField pHTF;
	private Matrix matrix;

	@FXML
	void initialize()
	{
		assert componentsTable != null :
				"fx:id=\"componentsTable\" was not injected: check your FXML file 'MatrixInfo.fxml'.";
		assert speciesTable != null : "fx:id=\"speciesTable\" was not injected: check your FXML file 'MatrixInfo.fxml'.";
		assert multiCalculationTF != null :
				"fx:id=\"multiCalculationTF\" was not injected: check your FXML file 'MatrixInfo.fxml'.";
		assert adsorptionParametersTable != null :
				"fx:id=\"adsorptionParametersTable\" was not injected: check your FXML file 'MatrixInfo.fxml'.";
		assert pHTF != null : "fx:id=\"pHTF\" was not injected: check your FXML file 'MatrixInfo.fxml'.";
	}

	void setMatrix(Matrix matrix)
	{
		this.matrix = matrix;
		update();
		// create components
//		componentsTable = new ComponentTable(
//			main,matrix.getComponentsTableModel());
//		adsorptionParametersTable = new AdsorptionParamsTable(
//			matrix.getAdsorptionTableModel());
//		multiCalculationTF.setOpaque(false);
//		Dimension prefTextboxSize =
//			new Dimension(Integer.MAX_VALUE, multiCalculationTF.getMinimumSize().height);
//		multiCalculationTF.setMaximumSize(prefTextboxSize);
//		multiCalculationTF.setBorder(BorderFactory.createEmptyBorder(6,0,6,0));
//		pHTF.setOpaque(false);
//		pHTF.setMaximumSize(prefTextboxSize);
//		pHTF.setBorder(BorderFactory.createEmptyBorder(6,0,6,0));
//		speciesTable = new SpeciesTable(main,matrix.getSpeciesTableModel());
	}

	void update()
	{
//		((AbstractTableModel)componentsTable.getModel()).fireTableStructureChanged();
//		TableColumnModel cm = componentsTable.getColumnModel();
//		for (int i=0; i<cm.getColumnCount(); i++)
//			setColumnWidths(cm.getColumn(i),60,150);
//
//		if (matrix.adsorption())
//		{
//			((AbstractTableModel)adsorptionParametersTable.getModel()).fireTableStructureChanged();
//			cm = adsorptionParametersTable.getColumnModel();
//			setColumnWidths(cm.getColumn(0),180,180);
//			setColumnWidths(cm.getColumn(1),90,90);
//			for (int i=2; i<cm.getColumnCount(); i++)
//				setColumnWidths(cm.getColumn(i),60,60);
//		}
		adsorptionParametersTable.setVisible(matrix.adsorption());

		multiCalculationTF.setVisible(matrix.multiConc > 1);

		if (matrix.isHorHplusAndFree()) {
			double conc = matrix.getMultiConcForLast();
			if (conc == 0.0) {
				pHTF.setText("ENTER A CONCENTRATION!");
			}
			else {
				pHTF.setText("-> pH is constant: "
						+ MyTools.EXACT_3_DIGITS.format(-MyTools.myLog(conc)));
			}
			pHTF.setVisible(true);
		}
		else {
			pHTF.setVisible(false);
		}

//		((AbstractTableModel)speciesTable.getModel()).fireTableStructureChanged();
//		cm = speciesTable.getColumnModel();
//		setColumnWidths(cm.getColumn(0),35,35);
//		setColumnWidths(cm.getColumn(1),40,120);
//		setColumnWidths(cm.getColumn(2),40,70);
//		setColumnWidths(cm.getColumn(3),40,100);
	}

//	private void setColumnWidths(TableColumn col, int min, int max)
//	{
//		col.setPreferredWidth(min);
//		col.setMaxWidth(max);
//	}
//
//	// --- custom table for component data
//
//	static class ComponentTable extends MyTable
//	{
//		private TableCellRenderer componentRenderer;
//		private TableCellEditor componentEditor;
//		private TableCellRenderer modeRenderer;
//		private TableCellEditor modeEditorTotalFree;
//		private TableCellEditor modeEditorPhaseCheckPrecip;
//		private TableCellRenderer concentrationRenderer;
//		private TableCellEditor concentrationEditor;
//
//		ComponentTable(ChemEqlGuiController main, TableModel model)
//		{
//			super(model,false);
//			componentRenderer = new CustomRenderer(
//				getFont().deriveFont(Font.BOLD),SwingConstants.CENTER);
//			componentEditor = new CustomTextEditor(SwingConstants.CENTER);
//			modeRenderer = new CustomRenderer(
//				getFont().deriveFont(Font.ITALIC),SwingConstants.CENTER);
//			modeEditorTotalFree = new DefaultCellEditor(
//				new JComboBox(new Mode[]{Mode.TOTAL,Mode.FREE}));
//			modeEditorPhaseCheckPrecip = new DefaultCellEditor(
//				new JComboBox(new Mode[]{Mode.SOLID_PHASE,Mode.CHECKPRECIP}));
//			concentrationRenderer = new CustomConcentrationsRenderer(
//				getFont(),SwingConstants.CENTER, main);
//			concentrationEditor = new CustomDoubleEditor(SwingConstants.CENTER);
//		}
//
//		public TableCellRenderer getCellRenderer(int row, int col)
//		{
//			switch (row)
//			{
//				case 0: return componentRenderer;
//				case 1: return modeRenderer;
//				case 2: return concentrationRenderer;
//				default: return null;
//			}
//		}
//
//		public boolean isCellEditable(int row, int col)
//		{
//			return true;
//		}
//
//		public TableCellEditor getCellEditor(int row, int col)
//		{
//			switch (row)
//			{
//				case 0: return componentEditor;
//				case 1:
//					if (this.getValueAt(row,col) == Mode.TOTAL
//						|| this.getValueAt(row,col) == Mode.FREE)
//						return modeEditorTotalFree;
//					if (this.getValueAt(row,col) == Mode.SOLID_PHASE
//						|| this.getValueAt(row,col) == Mode.CHECKPRECIP)
//						return modeEditorPhaseCheckPrecip;
//					return null;	// component mode is not editable
//				case 2: return concentrationEditor;
//				default: return null;
//			}
//		}
//	}
//
//
//	// --- custom table for adsorption parameters
//
//	static class AdsorptionParamsTable extends MyTable
//	{
//		private TableCellRenderer nameRenderer;
//		private TableCellRenderer digits2Renderer;
//		private TableCellRenderer digits3Renderer;
//
//		AdsorptionParamsTable(TableModel model)
//		{
//			super(model,false);
//			nameRenderer = new CustomRenderer(getFont().deriveFont(Font.BOLD));
//			digits2Renderer = new CustomNumberRenderer(getFont(),SwingConstants.RIGHT,2);
//			digits3Renderer = new CustomNumberRenderer(getFont(),SwingConstants.RIGHT,3);
//		}
//
//		public TableCellRenderer getCellRenderer(int row, int col)
//		{
//			if (col == 0)
//				return nameRenderer;
//			switch (row)
//			{
//				case 0: return stringRenderer;
//				case 1: return digits2Renderer;
//				case 2: return rightAlignRenderer;
//				case 3: return digits3Renderer;
//				case 4: return col == 1 ? digits3Renderer : stringRenderer;
//				case 5: return digits2Renderer;
//				case 6: return digits2Renderer;
//				default: return null;
//			}
//		}
//
//		public boolean isCellEditable(int row, int col)
//		{
//			return row > 0 && col > 0 && !(row == 4 && col > 1);
//		}
//
//		public TableCellEditor getCellEditor(int row, int col)
//		{
//			return new CustomDoubleEditor(SwingConstants.RIGHT);
//		}
//	}
//
//
//	// --- custom table for species data
//
//	static class SpeciesTable extends MyTable
//	{
//		private TableCellRenderer logKRenderer;
//		private TableCellEditor logKEditor;
//		private TableCellRenderer logKSourceRenderer;
//
//		SpeciesTable(ChemEqlGuiController main, TableModel model)
//		{
//			super(model,false);
//			logKRenderer = new CustomConstantsRenderer(getFont(),SwingConstants.RIGHT,main);
//			logKEditor = new CustomDoubleEditor(SwingConstants.RIGHT);
//			logKSourceRenderer = new CustomRenderer(getFont().deriveFont(10.0f));
//		}
//
//		public TableCellRenderer getCellRenderer(int row, int col)
//		{
//			switch (col)
//			{
//				case 0: return rightAlignRenderer;
//				case 1: return stringRenderer;
//				case 2: return logKRenderer;
//				case 3: return logKSourceRenderer;
//				default: return null;
//			}
//		}
//
//		public boolean isCellEditable(int row, int col)
//		{
//			return col == 2;
//		}
//
//		public TableCellEditor getCellEditor(int row, int col)
//		{
//			if (col == 2)
//				return logKEditor;
//			return null;
//		}
//	}
}
