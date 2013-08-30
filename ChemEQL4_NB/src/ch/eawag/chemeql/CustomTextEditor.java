package ch.eawag.chemeql;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;


class CustomTextEditor extends AbstractCellEditor
	implements TableCellEditor
{
	JTextField editorComponent;
	
	CustomTextEditor(int alignment)
	{
		editorComponent = new JTextField();
		editorComponent.setHorizontalAlignment(alignment);
		editorComponent.setBorder(
			BorderFactory.createLineBorder(Color.blue.brighter()));		
	}

	public boolean isCellEditable(EventObject evt)
	{
		if (evt instanceof MouseEvent)
			return ((MouseEvent)evt).getClickCount() >= 1;
		return true;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int rowIndex, int vColIndex)
	{
		editorComponent.setText((String)value);
		return editorComponent;
	}
	
	public Object getCellEditorValue()
	{
		return editorComponent.getText();
	}
}