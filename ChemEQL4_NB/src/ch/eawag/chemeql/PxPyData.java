package ch.eawag.chemeql;


public class PxPyData extends Object
{
	public double pX;
	public double pY;
	String pXpYborderOldSpecies;
	String pXpYborderNewSpecies;

	public PxPyData(double x, double y, String oldSpec, String newSpec) {
		pX = x;
		pY = y;
		pXpYborderOldSpecies = oldSpec;
		pXpYborderNewSpecies = newSpec;
	}
}
