package ch.eawag.chemeql;

import java.awt.Font;


class CustomConcentrationsRenderer extends CustomRenderer
{
	private ChemEqlGuiController main;

	CustomConcentrationsRenderer(Font font, int alignment, ChemEqlGuiController m)
	{
		super(font,alignment);
		main = m;
	}

	protected void setValue(Object value)
	{
		String s;
		if (value instanceof String)
			s = (String)value;	// for rendering components info in output window
		else
		{
			setFont(myFont);
			if (((Double)value).doubleValue() == 0.0)
				s = "0.0";
			else if (((Double)value).doubleValue() == 1.0)
				s = "1.0";
			else
				s = main.settingsDialog.concentrationFormat.format(value);
		}
		this.setText(s);
	}
}