package ch.eawag.chemeql;

import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.JTableHeader;


class MyTable extends JTable
{
	TableCellRenderer stringRenderer;
	TableCellRenderer rightAlignRenderer;

	MyTable(TableModel model, boolean resizingAllowed)
	{
		super(model);
		setFont(getFont().deriveFont(12));
		setShowGrid(false);
		setOpaque(false);
		setAutoResizeMode(AUTO_RESIZE_OFF);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(false);
		
		JTableHeader th = getTableHeader();
		th.setReorderingAllowed(false);
		th.setResizingAllowed(resizingAllowed);

		stringRenderer = new CustomRenderer();
		rightAlignRenderer = new CustomRenderer(getFont(),SwingConstants.RIGHT);
	}
	
	void appendTableData(FileWriter out, Format numFormat) throws IOException
	{
		TableModel tm = getModel();
		int columns = tm.getColumnCount();
		int rows = tm.getRowCount();
		
		// store column titles
		for (int c=0; c < columns; c++)
		{
			out.write(tm.getColumnName(c));
			out.write(Tokenizer.TAB);
		}
		out.write(Tokenizer.CR);
		out.write(Tokenizer.CR);
		for (int r=0; r < rows; r++)
		{
			for (int c=0; c < columns; c++)
			{
				Object v = tm.getValueAt(r,c);
				if (v instanceof Double || v instanceof Float)
					out.write(numFormat.format(v));
				else
					out.write(v.toString());
				out.write(Tokenizer.TAB);
			}
			out.write(Tokenizer.CR);
		}
	}
}