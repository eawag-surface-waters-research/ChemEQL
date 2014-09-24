package ch.eawag.chemeql;


class Component extends Object
{
	private String name;
	private double conc;
	private Mode mode;


	public String getName()
	{
		return name;
	}

	public void setName(String value)
	{
		name = value;
	}


	public double getConc()
	{
		return conc;
	}

	public void setConc(double value)
	{
		conc = value;
	}

	public Mode getMode()
	{
		return mode;
	}

	public void setMode(Mode value)
	{
		mode = value;
	}

	Component()
	{
		initialize();
	}

	Component(final String n, final double c, final Mode m)
	{
		// assert m == Mode.TOTAL || m == Mode.FREE;
		setName(n);
		setConc(c);
		setMode(m);
	}

	void initialize()
	{
		setName(null);
		setConc(0);
		setMode(null);
	}

	void copyFrom (Component other)
	{
		setName(other.getName());
		setConc(other.getConc());
		setMode(other.getMode());
	}

	boolean isModeSolidPhaseOrCheckPrecip()
	{
		return Mode.SOLID_PHASE == getMode() || Mode.CHECKPRECIP == getMode();
	}

	boolean isHPlusOrEMinus()
	{
		return "H+".equals(getName()) || "e-".equals(getName());
	}

	public String toString()
	{
		return getName();
	}
}