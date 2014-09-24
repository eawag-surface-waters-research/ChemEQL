package ch.eawag.chemeql;


/** This class enumerates possible "Activities" selected by the user via the
 * options menu. Since it is a subclass of the button model used by radio menu
 * items, the selected activity (and its string value) can directly accessed
 * via the button group that holds the radio menu items.
 */
enum Activity
{
	NO("no"),
	DEBYEHUECKEL("DebyeHueckel"),
	GUENTELBERG("Guentelberg"),
	DAVIES("Davies");

	private final String value;

	private Activity(String v)
	{
		value = v;
	}

	String getValue()
	{
		return value;
	}
}
