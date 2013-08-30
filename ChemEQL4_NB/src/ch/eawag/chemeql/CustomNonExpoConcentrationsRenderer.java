package ch.eawag.chemeql;

import java.awt.Font;


class CustomNonExpoConcentrationsRenderer extends CustomRenderer
{
	private Main main;

	CustomNonExpoConcentrationsRenderer(Font font, int alignment, Main m)
	{
		super(font,alignment);
		main = m;
	}

	protected void setValue(Object value)
	{
		setFont(myFont);
		setText(main.settingsDialog.nonExpoConcentrationFormat.format(value));
	}
}