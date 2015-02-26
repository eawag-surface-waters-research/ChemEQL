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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType.DoubleType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * @author kaibrassel
 */
public class MatrixView extends BorderPane
{
	private static SpreadsheetCell createEmptyCell(int row, int col)
	{
		SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, col, 1, 1, "");
		cell.setEditable(false);
		return cell;
	}

//	static MatrixView create()
//	{
//		// create minimal presentation model for an empty matrix (i.e. without any components and species
//		Grid grid = new GridBase(2, 2);
//		ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
//		for (int row = 0; row < grid.getRowCount(); ++row) {
//			final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
//			for (int col = 0; col < grid.getColumnCount(); ++col) {
//				list.add(createEmptyCell(row, col));
//			}
//			rows.add(list);
//		}
//		grid.setRows(rows);
//		return new MatrixView(grid);
//	}
//
	private SpreadsheetView matrixTable;
	private Matrix matrix;

//	private MatrixView(Grid grid)
//	{
//	}
	void setMatrix(Matrix newMatrix)
	{
		matrix = newMatrix;
		Grid grid = new GridBase(matrix.totSpec + 3, matrix.totComp + 4);
		ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
		List<String> modes = Arrays.asList("total", "free");

		// row with component names
		int rowIdx = 0;
		SpreadsheetCell cell;
		ObservableList<SpreadsheetCell> newRow = FXCollections.observableArrayList();
		// empty cells for running number and species columns
		newRow.add(createEmptyCell(rowIdx, 0));
		newRow.add(createEmptyCell(rowIdx, 1));
		// components
		for (int compIdx = 0; compIdx < matrix.totComp; ++compIdx) {
			cell = SpreadsheetCellType.STRING.createCell(rowIdx, compIdx + 2, 1, 1, matrix.components[compIdx].getName());
			cell.setEditable(false);
			cell.getStyleClass().add("matrix-component");
			newRow.add(cell);
		}
		// empty cells for formation constants column and sources column
		newRow.add(createEmptyCell(rowIdx, matrix.totComp + 2));
		newRow.add(createEmptyCell(rowIdx, matrix.totComp + 3));
		rows.add(newRow);
		rowIdx++;

		// row with modes
		newRow = FXCollections.observableArrayList();
		// empty cell for species column
		newRow.add(createEmptyCell(rowIdx, 0));
		newRow.add(createEmptyCell(rowIdx, 1));
		// modes
		for (int compIdx = 0; compIdx < matrix.totComp; ++compIdx) {
			cell = SpreadsheetCellType.LIST(modes).createCell(
					rowIdx, compIdx + 2, 1, 1, matrix.components[compIdx].getName());
			cell.getStyleClass().add("matrix-mode");
			newRow.add(cell);
		}
		// empty cell for formation constants column and sources column
		newRow.add(createEmptyCell(rowIdx, matrix.totComp + 2));
		newRow.add(createEmptyCell(rowIdx, matrix.totComp + 3));
		rows.add(newRow);
		rowIdx++;

		// row with concentrations
		DoubleType cellType = new SpreadsheetCellType.DoubleType(
				new CustomConcentrationsConverter(matrix.main.settingsDialog.concentrationFormat));
		newRow = FXCollections.observableArrayList();
		newRow.add(createEmptyCell(rowIdx, 0));
		newRow.add(createEmptyCell(rowIdx, 1));
		// concentrations
		for (int compIdx = 0; compIdx < matrix.totComp; ++compIdx) {
			cell = cellType.createCell(matrix.totSpec + 2, compIdx + 1, 1, 1, matrix.multiConcMatrix[0][compIdx]);
			cell.getStyleClass().add("matrix-concentration");
			newRow.add(cell);
		}
		// empty cell for formation constants column and comment column
		newRow.add(createEmptyCell(rowIdx, matrix.totComp + 2));
		newRow.add(createEmptyCell(rowIdx, matrix.totComp + 3));
		rows.add(newRow);
		rowIdx++;

		// --- species rows
		for (int speciesIdx = 0; speciesIdx < matrix.totSpec; ++speciesIdx) {
			newRow = FXCollections.observableArrayList();
			// running number
			cell = SpreadsheetCellType.STRING.createCell(rowIdx, 0, 1, 1, String.valueOf(speciesIdx + 1) + ".");
			cell.getStyleClass().add("matrix-species-number");
			cell.setEditable(false);
			newRow.add(cell);
			// species name
			cell = SpreadsheetCellType.STRING.createCell(rowIdx, 1, 1, 1, matrix.species[speciesIdx].name);
			cell.setEditable(false);
			cell.getStyleClass().add("matrix-species");
			newRow.add(cell);
			// stoichiometric coefficients
			for (int compIdx = 0; compIdx < matrix.totComp; ++compIdx) {
				cell = SpreadsheetCellType.INTEGER.createCell(rowIdx, compIdx + 2, 1, 1,
						(int)Math.round(matrix.speciesMat[speciesIdx][compIdx]));
				cell.getStyleClass().add("matrix-coeff");
				newRow.add(cell);
			}
			// formation constants (logK)
			cell = SpreadsheetCellType.DOUBLE.createCell(rowIdx, matrix.totComp + 1, 1, 1,
					matrix.species[speciesIdx].constant);
			cell.getStyleClass().add("matrix-logk");
			newRow.add(cell);
			// source
			cell = SpreadsheetCellType.STRING.createCell(rowIdx, matrix.totComp + 2, 1, 1,
					matrix.species[speciesIdx].source);
			cell.getStyleClass().add("matrix-source");
			newRow.add(cell);
			rows.add(newRow);
			rowIdx++;
		};

		// add view to GUI
		grid.setRows(rows);
		grid.spanColumn(2, 0, 0);
		grid.spanRow(3, 0, 0);
//		grid.getRows().get(0).get(0).getStyleClass().add("matrix-concentration"); // style for thicker border to species part
		grid.spanColumn(2, 0, matrix.totComp + 2);
		grid.spanRow(3, 0, matrix.totComp + 2);
//		grid.getRows().get(0).get(matrix.totComp + 2).getStyleClass().add("matrix-concentration");

		matrixTable = new SpreadsheetView(grid);
		matrixTable.getStyleClass().add("matrix");
		matrixTable.setShowColumnHeader(false);
		matrixTable.setFixingColumnsAllowed(false);
		matrixTable.setShowRowHeader(false);
		matrixTable.setFixingRowsAllowed(false);
		this.setCenter(matrixTable);

	}

	static private class CustomConcentrationsConverter extends StringConverter<Double>
	{
		private final DecimalFormat format;

		CustomConcentrationsConverter(DecimalFormat format)
		{
			this.format = format;
		}

		@Override
		public String toString(Double val)
		{
			return val == 0.0 ? "0.0" : (val == 1.0 ? "1.0" : format.format(val));
		}

		@Override
		public Double fromString(String s)
		{
			return Double.parseDouble(s);
		}
	}
}
