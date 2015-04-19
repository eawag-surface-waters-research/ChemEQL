package ch.eawag.chemeql;

import java.awt.Component;
import javax.swing.JTable;


class CustomDoubleEditor extends CustomTextEditor
{
	CustomDoubleEditor(int alignment) {
		super(alignment);
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int rowIndex, int vColIndex) {
		editorComponent.setText(value.toString());
		return editorComponent;
	}

	public Object getCellEditorValue() {
		double v;
		try {
			v = Double.parseDouble(editorComponent.getText());
		} catch (NumberFormatException ex) {
			MyTools.showError("Input is not a number!");
			return null;
		}
		return new Double(v);
	}
}
