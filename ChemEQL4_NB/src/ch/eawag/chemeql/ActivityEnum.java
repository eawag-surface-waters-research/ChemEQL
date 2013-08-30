          package ch.eawag.chemeql;

import javax.swing.JToggleButton.ToggleButtonModel;


/** This class enumerates possible "Activities" selected by the user via the
 * options menu. Since it is a subclass of the button model used by radio menu
 * items, the selected activity (and its string value) can directly accessed
 * via the button group that holds the radio menu items.
 */
class ActivityEnum extends ToggleButtonModel
{
	static final ActivityEnum NO = new ActivityEnum("no");
	static final ActivityEnum DEBYEHUECKEL = new ActivityEnum("DebyeHueckel");
	static final ActivityEnum GUENTELBERG = new ActivityEnum("Guentelberg");
	static final ActivityEnum DAVIES = new ActivityEnum("Davies");

	private String value;

	private ActivityEnum(String v)
	{
		value = v;
	}

	String getValue()
	{
		return value;
	}
}