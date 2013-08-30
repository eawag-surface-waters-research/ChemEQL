package ch.eawag.chemeql;

import java.awt.Font;
import javax.swing.table.DefaultTableCellRenderer;


class CustomRenderer extends DefaultTableCellRenderer
{
	Font myFont;

	CustomRenderer(Font font, int alignment)
	{
		super();
		myFont = font;
		super.setHorizontalAlignment(alignment);
	}

	CustomRenderer(Font font)
	{
		super();
		myFont = font;
//		this(font,SwingConstants.LEFT); is default
	}

	CustomRenderer()
	{
		super();
	}

	public boolean isOpaque()
	{
		return false;
	}

	protected void setValue(Object value)
	{
		setFont(myFont);
		super.setValue(value);
	}         
}