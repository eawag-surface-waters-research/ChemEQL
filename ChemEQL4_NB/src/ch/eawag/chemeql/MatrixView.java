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

import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * @author kaibrassel
 */
public class MatrixView extends SpreadsheetView
{
	private Matrix matrix;

	static MatrixView create()
	{
		// create minimal presentation model for an empty matrix (i.e. without any components and species
		Grid grid = new GridBase(3, 3);
		ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
		List<String> modes = Arrays.asList("total", "free");
		for (int row = 0; row < grid.getRowCount(); ++row) {
			final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
			for (int col = 0; col < grid.getColumnCount(); ++col) {
				SpreadsheetCell cell;
				if (row == 1) {
					cell = SpreadsheetCellType.LIST(modes).createCell(row, col, 1, 1, modes.get(0));
				}
				else {
					cell = SpreadsheetCellType.STRING.createCell(row, col, 1, 1, "");
				}
				list.add(cell);
			}
			rows.add(list);
		}
		grid.setRows(rows);
		grid.setCellValue(0, 0, "Species");
		grid.setCellValue(0, 1, "logK");
		grid.setCellValue(0, 2, "Comment");
		return new MatrixView(grid);
	}

	private MatrixView(Grid grid)
	{
		super(grid);
		setFixingColumnsAllowed(false);
		setFixingRowsAllowed(false);
		setShowColumnHeader(false);
		setShowRowHeader(false);
	}

	void setMatrix(Matrix newMatrix)
	{
		matrix = newMatrix;
		Grid grid = new GridBase(matrix.totSpec + 3, matrix.totComp + 3);
		ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
		List<String> modes = Arrays.asList("total", "free");

		// --- first row
		SpreadsheetCell cell;
		ObservableList<SpreadsheetCell> newRow = FXCollections.observableArrayList();
		// empty cell for species column
		newRow.add(createEmptyCell(0, 0));
		// component names
		for (int compIdx = 0; compIdx < matrix.totComp; ++compIdx) {
			cell = SpreadsheetCellType.STRING.createCell(0, compIdx + 1, 1, 1, matrix.components[compIdx].getName());
			cell.setEditable(false);
			newRow.add(cell);
		}
		// empty cell for formation constants column and sources column
		newRow.add(createEmptyCell(0, matrix.totComp + 1));
		newRow.add(createEmptyCell(0, matrix.totComp + 2));
		rows.add(newRow);

		// --- second row
		newRow = FXCollections.observableArrayList();
		// empty cell for species column
		newRow.add(createEmptyCell(1, 0));
		// modes
		for (int compIdx = 0; compIdx < matrix.totComp; ++compIdx) {
			cell = SpreadsheetCellType.LIST(modes).createCell(1, compIdx + 1, 1, 1, matrix.components[compIdx].getName());
			newRow.add(cell);
		}
		// empty cell for formation constants column and sources column
		newRow.add(createEmptyCell(1, matrix.totComp + 1));
		newRow.add(createEmptyCell(1, matrix.totComp + 2));
		rows.add(newRow);

		// --- species rows
		for (int speciesIdx = 0; speciesIdx < matrix.totSpec; ++speciesIdx) {
			newRow = FXCollections.observableArrayList();
			// species name
			cell = SpreadsheetCellType.STRING.createCell(speciesIdx + 2, 0, 1, 1, matrix.species[speciesIdx].name);
			cell.setEditable(false);
			newRow.add(cell);
			// stoichiometric coefficients
			for (int compIdx = 0; compIdx < matrix.totComp; ++compIdx) {
				cell = SpreadsheetCellType.INTEGER.createCell(speciesIdx + 2, compIdx + 1, 1, 1,
						(int)Math.round(matrix.speciesMat[speciesIdx][compIdx]));
				newRow.add(cell);
			}
			// formation constants (logK)
			cell = SpreadsheetCellType.DOUBLE.createCell(speciesIdx + 2, matrix.totComp, 1, 1,
					matrix.species[speciesIdx].constant);
			newRow.add(cell);
			// source
			cell = SpreadsheetCellType.STRING.createCell(speciesIdx + 2, matrix.totComp + 1, 1, 1,
					matrix.species[speciesIdx].source);
			newRow.add(cell);
			rows.add(newRow);
		};

		// --- footer row with concentrations
		newRow = FXCollections.observableArrayList();
		newRow.add(createEmptyCell(matrix.totSpec + 2, 0));

		// concentrations
		for (int compIdx = 0; compIdx < matrix.totComp; ++compIdx) {
			cell = SpreadsheetCellType.DOUBLE.createCell(matrix.totSpec + 2, compIdx + 1, 1, 1, 8.8); //TODO
			newRow.add(cell);
		}
		// empty cell for formation constants column and comment column
		newRow.add(createEmptyCell(matrix.totSpec + 2, matrix.totComp + 1));
		newRow.add(createEmptyCell(matrix.totSpec + 2, matrix.totComp + 2));
		rows.add(newRow);
		grid.setRows(rows);
		setGrid(grid);
	}

	private SpreadsheetCell createEmptyCell(int row, int col)
	{
		SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, col, 1, 1, "");
		cell.setEditable(false);
		return cell;
	}
}
