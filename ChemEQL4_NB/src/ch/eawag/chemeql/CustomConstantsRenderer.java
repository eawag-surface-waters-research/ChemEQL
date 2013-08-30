package ch.eawag.chemeql;

import java.awt.Font;


class CustomConstantsRenderer extends CustomRenderer
{
	private Main main;

	CustomConstantsRenderer(Font font, int alignment, Main m)
	{
		super(font,alignment);
		main = m;
	}

	protected void setValue(Object value)
	{
		setFont(myFont);
		setText(main.settingsDialog.constantFormat.format(value));
	}
}