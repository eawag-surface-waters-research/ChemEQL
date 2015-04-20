package ch.eawag.chemeql;


public class SpeciesData extends Object
{

	public double x;
	public final double[] concentration = new double[10];

	public SpeciesData() {
	}

	public SpeciesData(double x, int[] speciesToDraw, double[] conc) {
		this.x = x;
		for (int i = 0; i < speciesToDraw.length; i++) {
			concentration[i] = conc[speciesToDraw[i]];
		}
	}
}
