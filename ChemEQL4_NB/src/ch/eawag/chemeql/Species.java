package ch.eawag.chemeql;

import java.io.Serializable;


class Species extends Object implements Serializable
{
	String name;
	double constant;
	String source;

	Species()
	{
		this.initialize();
	}

	Species(String n, double c, String s)
	{
		name = n;
		constant = c;
		source = s;
	}

	void initialize()
	{
		name = null;
		constant = 0;
		source = null;
	}

	void copyFrom (Species other)
	{
		name = other.name;
		constant = other.constant;
		source = other.source;
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