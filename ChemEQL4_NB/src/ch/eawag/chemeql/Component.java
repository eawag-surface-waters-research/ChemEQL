package ch.eawag.chemeql;


class Component extends Object
{
	String name;
	double conc;
	Mode mode;

	Component()
	{
		initialize();
	}

	Component(String n, double c, Mode m)
	{
		// assert m == Mode.TOTAL || m == Mode.FREE;
		name = n;
		conc = c;
		mode = m;
	}

	void initialize()
	{
		name = null;
		conc = 0;
		mode = null;
	}

	void copyFrom (Component other)
	{
		name = other.name;
		conc = other.conc;
		mode = other.mode;
	}

	boolean isModeSolidPhaseOrCheckPrecip()
	{
		return mode == Mode.SOLID_PHASE || mode == Mode.CHECKPRECIP;
	}

	boolean isHPlusOrEMinus()
	{
		return name.equals("H+") || name.equals("e-");
	}

	public String toString()
	{
		return name;
	}
}