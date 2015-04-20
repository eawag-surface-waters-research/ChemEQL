package ch.eawag.chemeql;

import javax.swing.JToggleButton;


class NumFormatEnum extends JToggleButton.ToggleButtonModel
{
	static final NumFormatEnum LINEAR = new NumFormatEnum("linear");
	static final NumFormatEnum LOGARITHMIC = new NumFormatEnum("logarithmic");

	private String value;

	private NumFormatEnum(String v) {
		value = v;
	}

	String getValue() {
		return value;
	}
}
