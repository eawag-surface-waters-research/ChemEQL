package ch.eawag.chemeql;

import java.awt.Font;


class CustomNonExpoConcentrationsRenderer extends CustomRenderer
{
	private ChemEQL3 main;

	CustomNonExpoConcentrationsRenderer(Font font, int alignment, ChemEQL3 m)
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