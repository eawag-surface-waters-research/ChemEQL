package ch.eawag.chemeql;


enum NumFormatEnum
{
	LINEAR("linear"),
	LOGARITHMIC("logarithmic");

	private final String value;

	private NumFormatEnum(String v) {
		value = v;
	}

	String getValue() {
		return value;
	}
}
