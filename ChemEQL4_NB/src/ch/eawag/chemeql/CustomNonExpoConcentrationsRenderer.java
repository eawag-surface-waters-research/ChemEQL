package ch.eawag.chemeql;

import java.awt.Font;


class CustomNonExpoConcentrationsRenderer extends CustomRenderer
{
	private ChemEqlGuiController main;

	CustomNonExpoConcentrationsRenderer(Font font, int alignment, ChemEqlGuiController m) {
		super(font, alignment);
		main = m;
	}

	protected void setValue(Object value) {
		setFont(myFont);
		setText(main.settingsDialog.nonExpoConcentrationFormat.format(value));
	}
}
