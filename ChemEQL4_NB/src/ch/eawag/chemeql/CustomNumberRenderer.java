package ch.eawag.chemeql;

import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;


class CustomNumberRenderer extends CustomRenderer
{
	private NumberFormat formatter;

	CustomNumberRenderer(Font font, int alignment, int digits)
	{
		super(font,alignment);
		formatter = NumberFormat.getInstance();
		formatter.setMinimumFractionDigits(digits);
		formatter.setMaximumFractionDigits(digits);
	}

	CustomNumberRenderer(Font font, int alignment, String pattern)
	{
		super(font,alignment);
		formatter = new DecimalFormat(pattern);
	}

	protected void setValue(Object value)
	{
		this.setFont(myFont);
		this.setText(formatter.format(value));
	}
	
	

}