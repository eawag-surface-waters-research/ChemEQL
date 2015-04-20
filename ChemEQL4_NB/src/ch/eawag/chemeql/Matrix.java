package ch.eawag.chemeql;

import de.vseit.showit.ColorEnum;
import de.vseit.showit.LineEnum;
import de.vseit.showit.MarkerEnum;
import de.vseit.showit.PlotView;
import de.vseit.showit.SizeEnum;
import java.awt.Color;
import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


class Matrix extends Object
{
	private static final double B1 = 6.022E23;	/* Avogadro's number  -->  sites /mol */

	// B1 is never used!!

	private static final double B2 = 0.1174;		/* (8EEoRT *1000)^1/2  -->  Cm-2(mol l-1)1/2 */

	private static final double B3 = 19.46;		/* F/2RT   -->  V-1 */

	private static final double B4 = 0.0256;		/* RT/F  -->  V */

	private static final double B5 = 0.05916;		/* log10 RT/F  -->  V */

	private static final double B6 = 9.6487E4;	/* F  -->  C/mol */

	private static final double ROUNDING_ERROR_RELAXATION = 1000000.0;

	private static final char TAB = Tokenizer.TAB;
	private static final char CR = Tokenizer.CR;

	private static final int columns = 40;
	private static final int lines = 400;
	private static final int multiConcLines = 200;
	private static final int maxParticles = 5;

	ChemEQL3 main;
	private String name;

	int totComp;
	int totSpec;
	int multiConc;
	int compNo;	/*to calculate a component range*/

	int specNo;	/*to calculate a range of log K*/

	private int totLimComp;
	private int noOfSolidPhases;	/*solid phases with activity=1*/

	private int noOfCheckPrecip;	/*components that are to be checked for precipitation*/

	private boolean severalAdsorbents;
	private boolean severalSiteTypes;
	private int noOfAdsorbents;
	ModelEnum model;

	private double[][] speciesMat = new double[lines][columns];
	double[][] multiConcMatrix = new double[multiConcLines][columns];

	Component[] components = new Component[columns];
//	private Mode[] mode = new Mode[columns];
//	private double[] conc = new double[columns];

	private Species[] species = new Species[lines];
//	private double[] logK = new double[lines];
//	private String[] logKSource = new String[lines];

	private double[] speConc = new double[lines];
	double[] oldConstantMemory = new double[lines];
	double[] concEstim = new double[columns];
	private int[] adsNo = new int[maxParticles];
	private int[] chargeNo1 = new int[maxParticles];
	private int[] chargeNo2 = new int[maxParticles];
	private int[] chargeNo3 = new int[maxParticles];
	private double[] area = new double[maxParticles];
	private double[] surfaceSites = new double[maxParticles];
	private double[] solidConc = new double[maxParticles];
	double ionicStr;
	private double[] innerCap = new double[maxParticles];
	private double[] outerCap = new double[maxParticles];

	private int[] ii = new int[columns];

	// temporaries
	boolean printInteger;
	boolean calculatedWithActivity;

	// for matrix computation
	private double[] compConc;
	private double[] concMemory;
	private double[] errorEstim;
	private double[] logfSpec = new double[lines];
	private double[] amountPrecipitated;
	private double[][] jacob;
	double compRangeStart;
	double compRangeEnd;
	double compRangeStep;
	double adsRangeStart;
	double adsRangeEnd;
	double adsRangeStep;
	double logKrangeStart;
	double logKrangeEnd;
	double logKrangeStep;
	double reactionOrder;
	// KineticProc
	int kinCompC, kinCompD, kinCompE, kinCompP;
	int coeffC, coeffD, coeffP;
	double kinTimeEnd, kinTimeInc;
	double rateConst, kBack;
	// ActivityInputProc
	boolean fixionicStr;
	double activA;
	// pXpYProc
	int xComp;
	int yComp;
	int mainpXpYcomp;
	double pXrangeStart, pXrangeEnd;
	double pYrangeStart, pYrangeEnd;
	double pXpYstep;
	int pXpYspecs;
	private boolean isAGas;	// global temporary variable

	// models for interface elements in output window
	private PlainDocument notesDoc = new PlainDocument();
	private DefaultTableModel dataTableModel;
	private ArrayList<Object> dataTableRow; // temporary for filling a data table row
	int startItalics;
	int stopItalics;

	Matrix(ChemEQL3 m) {
		main = m;
		name = "New Matrix";
		for (int a = 0; a < lines; a++) {
			species[a] = new Species();
		}
		for (int b = 0; b < columns; b++) {
			components[b] = new Component();
		}
	}

	public String toString() {
		return name;
	}

	PlainDocument getNotesDoc() {
		return notesDoc;
	}

	private void writeNote(final String note) {
		try {
			SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run() {
					try {
						notesDoc.insertString(notesDoc.getLength(), note, null);
					} catch (BadLocationException ex) {
					}
				}
			});
		} catch (InterruptedException ex) {
		} catch (InvocationTargetException ex) {
		}
	}

	AbstractTableModel getComponentsTableModel() {
		return new ComponentsTableModel();
	}

	AbstractTableModel getAdsorptionTableModel() {
		return new AdsorptionTableModel();
	}

	AbstractTableModel getSpeciesTableModel() {
		return new SpeciesTableModel();
	}

	AbstractTableModel getSpeciesOutputTableModel() {
		return new SpeciesOutputTableModel();
	}

	AbstractTableModel getComponentsOutputTableModel() {
		return new ComponentsOutputTableModel();
	}

	AbstractTableModel getPxPyOutputTableModel() {
		return new PxPyOutputTableModel();
	}

	DefaultTableModel getDataTableModel() {
		return dataTableModel;
	}

	SpeciesNamesModel createSpeciesNamesModel() {
		return new SpeciesNamesModel();
	}

	ComboBoxModel createComponentsCBModel() {
		return new ComponentsCBModel();
	}

	ComboBoxModel createSpecialComponentsCBModel() {
		return new SpecialComponentsCBModel();
	}

	// replaces DrawTitles in module DataHandling
	private void initIntervalOutput() {
		dataTableModel = new DefaultTableModel();
		dataTableRow = new ArrayList<Object>(20);
		startItalics = Integer.MAX_VALUE;	// assume: no italic output
		int i = 0;
		if (main.compRange && !main.adsRange && noOfCheckPrecip == 0) {
			dataTableModel.addColumn("tot." + components[compNo].name);
			/*writeDraw('tot. ', compNames[compNo]);*/
			i++;
		}
		if (main.adsRange) {
			dataTableModel.addColumn("tot. " + components[adsNo[0]].name + " [g/l]");
			/*writeDraw('tot. ', compNames[adsNo[1]], ' [g/l]');*/
			i++;
		}
		if (noOfCheckPrecip > 0) {
			for (int b = 0; b < totComp; b++) {
				if (components[b].mode == Mode.CHECKPRECIP) {
					dataTableModel.addColumn("tot. " + components[b].name);
					/*writeDraw('tot. ', compNames[b]);*/
					i++;
					dataTableModel.addColumn("diss. " + components[b].name);
					/*writeDraw('diss. ', compNames[b]);*/
					i++;
					dataTableModel.addColumn("precip. " + components[b].name);
					/*writeDraw('diss. ', compNames[b]);*/
					i++;
				}
			}
		}
		if (main.activityOutput) {
			startItalics = i;						// italic output starting at column i
			stopItalics = i + totSpec - 1;	// last italic output column
		}
		for (int a = 0; a < totSpec - 1; a++) {
			dataTableModel.addColumn(species[a].name);
		}
		/*writeDraw(specNames[a] : 8);*/
		dataTableModel.addColumn("-log " + components[totComp - 1].name);
		if (calculatedWithActivity) {
			dataTableModel.addColumn("Ionic strength");
		}
		if (main.logKrange) {
			dataTableModel.addColumn("log K (" + species[specNo].name + ")");
		}
		/*writeDraw('log K (', specNames[specNo], ')');*/
	}

	private void addOutputData(boolean log) {
		dataTableRow.clear();
		if (log) {
			addOutputDataLog();
		}
		else {
			addOutputDataLin();
		}
		dataTableModel.addRow(dataTableRow.toArray());
	}

	private void addOutputDataLin() {
		if (main.compRange && !main.adsRange && (noOfCheckPrecip == 0)) {
			dataTableRow.add(new Double(compRangeStart));
		}
		if (main.adsRange) {
			dataTableRow.add(new Double(solidConc[0]));
		}
		if (noOfCheckPrecip > 0) {
			for (int b = 0; b < totComp; b++) {
				if (components[b].mode == Mode.CHECKPRECIP) {
					double totConc = concMemory[b];
					double dissConc;
					if (main.pHrange || main.pHfix) {
						dissConc = components[b].conc;
					}
					else if (totConc > components[b].conc) {
						dissConc = errorEstim[b];
					}
					else {
						dissConc = components[b].conc;
					}
					dataTableRow.add(new Double(totConc));
					dataTableRow.add(new Double(dissConc));
					if (totConc - dissConc > 0) {
						dataTableRow.add(new Double(totConc - dissConc));
					}
					else {
						dataTableRow.add("");
					}
				}	/*if ((mode[b] = 'checkPrecip')*/

			}
		}

		for (int a = 0; a < totSpec - 1; a++) {
			dataTableRow.add(new Double(main.activityOutput
					? MyTools.expo(10, logfSpec[a]) * speConc[a] : speConc[a]));
		}
		dataTableRow.add(new Float(-MyTools.myLog(speConc[totSpec - 1])));
		if (calculatedWithActivity) {
			dataTableRow.add(new Float(ionicStr));
		}
		if (main.logKrange) {
			dataTableRow.add(new Float(species[specNo].constant));
		}
	}

	private void addOutputDataLog() {
		if (main.compRange && !main.adsRange && noOfCheckPrecip == 0) {
			dataTableRow.add(new Float(main.compRangeIsLog
					? compRangeStart : MyTools.myLog(compRangeStart)));
		}
		if (main.adsRange) {
			dataTableRow.add(new Float(MyTools.myLog(solidConc[0])));
		}
		if (noOfCheckPrecip > 0) {
			for (int b = 0; b < totComp; b++) {
				if (components[b].mode == Mode.CHECKPRECIP) {
					double totConc = concMemory[b];
					double dissConc;
					if (main.pHrange || main.pHfix) {
						dissConc = components[b].conc;
					}
					else if (totConc > components[b].conc) {
						dissConc = errorEstim[b];
					}
					else {
						dissConc = components[b].conc;
					}

					if (totConc != 0) {
						dataTableRow.add(new Float(MyTools.myLog(totConc)));
						dataTableRow.add(new Float(MyTools.myLog(dissConc)));
						dataTableRow.add(new Float(MyTools.myLog(totConc - dissConc)));
					}
					else {
						dataTableRow.add(" --- ");
						dataTableRow.add(new Float(MyTools.myLog(dissConc)));
						dataTableRow.add("");
					}
				}	/*if ((mode[b] = 'checkPrecip')*/

			}
		}

		for (int a = 0; a < totSpec - 1; a++) {
			dataTableRow.add(new Float(main.activityOutput
					? logfSpec[a] + MyTools.myLog(speConc[a]) : MyTools.myLog(speConc[a])));
		}
		dataTableRow.add(new Float(-MyTools.myLog(speConc[totSpec - 1])));
		if (calculatedWithActivity) {
			dataTableRow.add(new Float(ionicStr));
		}
		if (main.logKrange) {
			dataTableRow.add(new Float(species[specNo].constant));
		}
	}

	void initialize() {
		compNo = 0;
		specNo = 0;
		for (int a = 0; a < lines; a++) {
			for (int b = 0; b < columns; b++) {
				speciesMat[a][b] = 0;
			}
		}
		for (int a = 0; a < multiConcLines; a++) {
			for (int b = 0; b < columns; b++) {
				multiConcMatrix[a][b] = 0;
			}
		}
		for (int b = 0; b < columns; b++) {
			components[b].initialize();
			concEstim[b] = 0;
		}
		for (int a = 0; a < lines; a++) {
			species[a].initialize();
			oldConstantMemory[a] = 0;
			speConc[a] = 0;
		}
		for (int b = 0; b < maxParticles; b++) {
			area[b] = 0;
			surfaceSites[b] = 0;
			solidConc[b] = 0;
			innerCap[b] = 0;
			outerCap[b] = 0;
		}
	}

// -----------------------------------------------------------------------------
// --- matrix commands  --------------------------------------------------------
// -----------------------------------------------------------------------------
	void replaceHbyOHProc() {
		double logKw = 0;

		String itemString = main.replaceHbyOHCmd.getText();
		if (itemString.equals("Replace H+ by OH-")) /*wenn H+ durch OH- ersetzen*/ {
			if (!isLastCompName("H+") || !species[totSpec - 1].name.equals("H+")
					|| !species[totSpec - 2].name.equals("OH-")) {
				MyTools.showError("Component or species not found: "
						+ "H+ must be last component and last species, "
						+ "OH- must be second last species!");
				return;
			}
			else {
				components[totComp - 1].name = "OH-";
				species[totSpec - 2].name = "H+";
				species[totSpec - 1].name = "OH-";
				logKw = species[totSpec - 2].constant;	/*ion product of water from matrix*/

			}
		}			/*if (itemString = 'Replace H+ by OH-')*/

		else // itemString must be "Replace OH- by H+"	/*wenn H+ durch OH- ersetzen*/
		{
			if (!isLastCompName("OH-") || !species[totSpec - 1].name.equals("OH-")
					|| !species[totSpec - 2].name.equals("H+")) {
				MyTools.showError("Component or species not found: "
						+ "OH- must be last component and last species, "
						+ "rH+ must be second last species!");
				return;
			}
			else {
				components[totComp - 1].name = "H+";
				species[totSpec - 2].name = "OH-";
				species[totSpec - 1].name = "H+";
				logKw = species[totSpec - 2].constant;		/*ion product of water from matrix*/

			}
		}		/*if (itemString = 'Replace OH- by H+')*/

		for (int a = 0; a < totSpec - 2; a++) {
			species[a].constant += logKw * speciesMat[a][totComp - 1];
			oldConstantMemory[a] = species[a].constant;	/*keeps original logK in 'oldConstantMemory'*/

			speciesMat[a][totComp - 1] = -speciesMat[a][totComp - 1];
		}
	}

// -----------------------------------------------------------------------------
// --- file commands  ----------------------------------------------------------
// -----------------------------------------------------------------------------
	/* reads input matrix from an EXCEL-text file */
	void datInput(File inputFile) throws IOException {
		String s;
		String z;

		Reader reader = new BufferedReader(new FileReader(inputFile));
		Tokenizer myRead = new Tokenizer(reader);
		//		gmatrixName = myReply.sfFile.Name;				/*zum Drucken*/

		myRead.nextItem();
		if (!myRead.isItemEmpty()) /*checks if first field in matrix is empty or remark*/ {
			throw new DataFormatException(
					"Error reading matrixfile: This file is not a legal " + ChemEQL3.AN + " matrix or has wrong formats");
		}

		myRead.skipLine();	/* eine Zeile ¸berspringen */

		myRead.nextItem();
		if (!myRead.isItemEmpty()) /*checks if first field in the second line is empty or remark*/ {
			throw new DataFormatException(
					"Error reading matrixfile: This file is not a legal " + ChemEQL3.AN + " matrix or has wrong formats");
		}

		totComp = 0;	/*Zeile mit 'modes': Counts the number of Components*/

		do {
			s = myRead.nextItem();
			if (!myRead.isItemEmpty() && myRead.delimiter() != CR) {
				Mode m = Mode.get(s);
				if (m == null) {
					throw new DataFormatException(
							"Error reading matrixfile: Mode " + s + " is an unknown code");
				}
				components[totComp++].mode = m;
			}
		}
		while (myRead.notEOL());

		if (totComp < 2) {
			throw new DataFormatException("Error reading matrixfile: This file is not a legal "
					+ ChemEQL3.AN + " matrix or has wrong formats");
		}

		totSpec = 0;	/* we count the speciesMat along checking the logK-column */

		do {
			z = myRead.nextItem();
			for (int b = 0; b < totComp + 1; b++) {
				s = myRead.nextItem();
			}
			try {
				myRead.itemToDouble();
				species[totSpec++].name = z;	/* liest Namen der Spezies */

			} catch (NumberFormatException ex) {
			} // just skip species[totSpec++] = z;
			while (myRead.notEOL()) /* springe bis Zeilenende */ {
				myRead.nextItem();
			}
		}
		while (s.length() > 0 && myRead.delimiter() != '{' && myRead.notEOF());
//		while (s.length() > 0 && myRead.delimiter() != '{'
//			&& Tokenizer.stringRealOK(s) && myRead.notEOF());

		// reset file for second pass
		reader.close();
		reader = new FileReader(inputFile);
		myRead = new Tokenizer(reader);

		/* reads names of components */
		myRead.nextItem();	// skip empty field at the beginning
		for (int b = 0; b < totComp; b++) {
			components[b].name = myRead.nextItem();
		}
		myRead.skipToEOL();	/* springe bis Zeilenende */

		defaultsDatInput();
		myRead.skipLine();	/* eine Zeile ¸berspringen */

		for (int a = 0; a < totSpec; a++) /* reads names and coefficients of speciesMat*/ {
			myRead.nextItem();	// skip name of speciesMat
			for (int b = 0; b < totComp; b++) {
				try {
					speciesMat[a][b] = Double.parseDouble(myRead.nextItem());
				} catch (NumberFormatException ex) {
					throw new DataFormatException(
							"Error reading stoichiometric coefficient of: "
							+ components[b].name + "/" + species[a].name);
				}
			}
			try {
				species[a].constant = Double.parseDouble(myRead.nextItem());
			} catch (NumberFormatException ex) {
				throw new DataFormatException(
						"Error reading log K of: " + species[a].name);
			}

			StringBuffer str = new StringBuffer();
			if (myRead.delimiter() != CR) {
				str.append(myRead.nextItem());		/*Temp und Ionenst.*/

			}
			if (myRead.delimiter() != CR) {
				str.append(TAB);
				str.append(myRead.nextItem());		/*Lit.angabe*/

			}
			species[a].source = str.toString();
			myRead.skipToEOL();	/*falls dies noch nicht das Zeilenende ist, dann gehe bis zum Zeilenende (->readln)*/

		}

		/*reads total concentrations of components and recognizes if several conc*/
		multiConc = 0;
		// at least one line of concentrations expected
		myRead.nextItem();
		while (myRead.isItemEmpty() && myRead.delimiter() == TAB) {
			// expecting (annother) line of concentrations
			multiConc++;
			if (multiConc > multiConcLines) {
				throw new DataFormatException(
						"Max. " + Integer.toString(multiConcLines)
						+ " concentrations possible for concentration array");
			}
			for (int b = 0; b < totComp; b++) {
				try {
					s = myRead.nextItem();
					multiConcMatrix[multiConc - 1][b] = Double.parseDouble(s);
				} catch (NumberFormatException ex) {
					throw new DataFormatException(
							"Error reading concentrations: '" + s + "' is not a number");
				}
			}
			myRead.skipToEOL();
			s = myRead.nextItem();
		}
		if (adsorption()) /*pr¸fe, ob auf dieser Zeile Eingaben f¸r Adsorption*/ {
			// expect name of model in s
			model = ModelEnum.getInstance(s);
			if (model == null) {
				throw new DataFormatException(
						"Error reading adsorption input: the double layer-model '"
						+ s + "' is not an allowed code. Consult 'info'!");
			}

			myRead.skipToEOL();

			for (int a = 0; a < noOfAdsorbents; a++) /*surface area*/ {
				try {
					area[a] = Double.parseDouble(myRead.nextItem());
				} catch (NumberFormatException ex) {
					throw new DataFormatException(
							"Error reading adsorption input: The particle surface is not a number");
				}
			}
			myRead.skipToEOL();

			for (int a = 0; a < noOfAdsorbents; a++) /*surfaceSites*/ {
				try {
					surfaceSites[a] = Double.parseDouble(myRead.nextItem());
				} catch (NumberFormatException ex) {
					throw new DataFormatException(
							"Error reading adsorption input: The number of surface sites is not a number");
				}
			}
			myRead.skipToEOL();

			for (int a = 0; a < noOfAdsorbents; a++) /*solidConc*/ {
				try {
					solidConc[a] = Double.parseDouble(myRead.nextItem());
				} catch (NumberFormatException ex) {
					throw new DataFormatException(
							"Error reading adsorption input: The particle concentration is not a number");
				}
			}
			myRead.skipToEOL();

			try {
				ionicStr = Double.parseDouble(myRead.nextItem());
			} /*ionicStr*/ catch (NumberFormatException ex) {
				throw new DataFormatException(
						"Error reading adsorption input: The ionic strength is not a number");
			}
			myRead.skipToEOL();

			if (model != ModelEnum.DIFFUSE_GTL) {
				for (int a = 0; a < noOfAdsorbents; a++) /*innerCap*/ {
					try {
						innerCap[a] = Double.parseDouble(myRead.nextItem());
					} catch (NumberFormatException ex) {
						throw new DataFormatException(
								"Error reading adsorption input: The number of surface sites is not a number");
					}
					if (innerCap[a] <= 0) {
						throw new DataFormatException(
								"Error reading adsorption input: Value for inner capacitance must be >0");
					}
				}
				myRead.skipToEOL();
			}	/*if (model != 'Diffuse/GTL')*/

			if (model == ModelEnum.TRIPLE_L) {
				for (int a = 0; a < noOfAdsorbents; a++) /*outerCap*/ {
					try {
						outerCap[a] = Double.parseDouble(myRead.nextItem());
					} catch (NumberFormatException ex) {
						throw new DataFormatException(
								"Error reading adsorption input: The outer capacitance is missing or is not a number");
					}
				}
				myRead.skipToEOL();
			}	/*if (model = 'TripleL'*/

		} // end of reading adsorptions

		for (int b = 0; b < totComp; b++) {
			components[b].conc = multiConcMatrix[0][b];
		}

		adjustConcEstim();

		for (int a = 0; a < totSpec; a++) {
			oldConstantMemory[a] = species[a].constant;
		}
		/*keeps original logK in 'oldConstantMemory'*/

		name = inputFile.getName();

	} // end of datInput

	void save(File outputFile) throws IOException {
		FileWriter fileWriter = new FileWriter(outputFile);

		fileWriter.write(TAB);											/*erstes Feld leer*/

		for (int b = 0; b < totComp; b++) {
			fileWriter.write(components[b].name);		/*Namen der Komponenten schreiben*/

			fileWriter.write(TAB);
		}
		fileWriter.write(CR);													/*neue Zeile*/


		fileWriter.write(TAB);											/*erstes Feld leer*/

		for (int b = 0; b < totComp; b++) /*Namen der Komponenten schreiben*/ {
			fileWriter.write(components[b].mode.toString());
			fileWriter.write(TAB);
		}
		fileWriter.write(CR);													/*neue Zeile*/

		for (int a = 0; a < totSpec; a++) {
			fileWriter.write(species[a].name);			/*Namen der Species schreiben*/

			fileWriter.write(TAB);
			for (int b = 0; b < totComp; b++) {
				fileWriter.write(MyTools.UPTO_6_DIGITS.format(speciesMat[a][b]));
				/*falls gebrochener Koeff: gen¸gend Kommastellen!*/
				fileWriter.write(TAB);
			}
			fileWriter.write(MyTools.UPTO_3_DIGITS.format(species[a].constant));			/*Konstanten*/

			fileWriter.write(TAB);
			fileWriter.write(species[a].source.trim());	/*Lit.angabe, CR ist schon drin beim Powerbook, nicht beim Quadra...*/

			fileWriter.write(CR);	/*neue Zeile. Ein CR einsetzen funktioniert einfach nicht!!!*/

		}

		fileWriter.write(TAB);													/*erstes Feld leer*/

		for (int b = 0; b < totComp; b++) {
			fileWriter.write(Double.toString(multiConcMatrix[0][b])); /*Konzentrationen der Komponenten schreiben*/

			fileWriter.write(TAB);
		}
		fileWriter.write(TAB);

		if (adsorption()) {
			fileWriter.write(CR);
			fileWriter.write(model.getName());							/*neue Zeile, DS-Modell*/

			fileWriter.write(TAB);
			fileWriter.write(CR);												/*neue Zeile*/

			for (int a = 0; a < noOfAdsorbents; a++) {
				fileWriter.write(Double.toString(area[a]));		/*particle surface*/

				fileWriter.write(TAB);
			}
			fileWriter.write(CR);												/*neue Zeile*/

			for (int a = 0; a < noOfAdsorbents; a++) {
				fileWriter.write(Double.toString(surfaceSites[a]));/*surfaceSites*/

				fileWriter.write(TAB);
			}

			fileWriter.write(CR);											/*neue Zeile*/

			for (int a = 0; a < noOfAdsorbents; a++) {
				fileWriter.write(Double.toString(solidConc[a]));		/*solidConc*/

				fileWriter.write(TAB);
			}

			fileWriter.write(CR);												/*neue Zeile*/

			fileWriter.write(Double.toString(ionicStr));						/*ionicStr*/

			fileWriter.write(TAB);

			if (model != ModelEnum.DIFFUSE_GTL) {
				fileWriter.write(CR);											/*neue Zeile*/

				for (int a = 0; a < noOfAdsorbents; a++) {
					fileWriter.write(MyTools.UPTO_3_DIGITS.format(innerCap[a]));	/*innerCap*/

					fileWriter.write(TAB);
				}
			}
			if (model == ModelEnum.TRIPLE_L) {
				fileWriter.write(CR);											/*neue Zeile*/

				for (int a = 0; a < noOfAdsorbents; a++) {
					fileWriter.write(MyTools.UPTO_3_DIGITS.format(outerCap[a]));/*outerCap*/

					fileWriter.write(TAB);
				}
			}
		} /*if (adsorption)*/

		fileWriter.flush();

		name = outputFile.getName();

	} // end of save

// -----------------------------------------------------------------------------
// --- matrix operations -------------------------------------------------------
// -----------------------------------------------------------------------------
	/*sucht die zu den ausgew‰hlten Componenten gehˆrigen Spezies aus der Library*/
	void buildMyChoiceMatrixAndTransfer(Library lib, Object[] selectedComponents) {
		boolean chooseSpecies;
		boolean isAComp;
		int[][] myBufMat = new int[lines][Library.libColumns];

		main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		totSpec = 0;
		for (int a = 0; a < lib.libTotSpec; a++) /*suche alle Spezies ab*/ {
			chooseSpecies = true;
			for (int b = 0; b < lib.libTotComp; b++) /*gehe alle Komponenten durch*/ {
				if (lib.libSpecMat[a][b] != 0) /*wenn ein Koeffizient gefunden*/ {
					isAComp = false;
					for (int c = 0; c < selectedComponents.length; c++) {
						if (lib.libCompNames[b].equals(
								((Component)selectedComponents[c]).name)) /*wenn die dazugeh‰rige Komponente ausgew‰hlt ist*/ {
							isAComp = true;
							break;
						}
					}
					if (!isAComp) {
						chooseSpecies = false;
						break;
					}
				}
			}

			if (chooseSpecies) {
				species[totSpec].copyFrom(lib.libSpecies[a]);
				if (species[totSpec].source == null) {
					species[totSpec].source = "-";
				}
				for (int b = 0; b < lib.libTotComp; b++) {
					myBufMat[totSpec][b] = lib.libSpecMat[a][b];
				}
				totSpec++;
			}
		} // durchlaufe Species

		totComp = 0;
		for (int b = 0; b < lib.libTotComp; b++) /*nicht ausgew‰hlte Komponenten rauswerfen*/ {
			for (int c = 0; c < selectedComponents.length; c++) {
				Component selComp = (Component)selectedComponents[c];
				if (lib.libCompNames[b].equals(selComp.name)) {
					components[totComp].copyFrom(selComp);
					if (components[totComp].conc == 0) {
						concEstim[totComp] = -38;
					}
					else {
						concEstim[totComp] = MyTools.myLog(components[totComp].conc);
					}
					for (int a = 0; a < totSpec; a++) {
						speciesMat[a][totComp] = myBufMat[a][b];
					}
					totComp++;
				}
			}
		}

		multiConc = 1;
		for (int b = 0; b < totComp; b++) {
			multiConcMatrix[0][b] = components[b].conc;
		}

		if (isHorHplusAndFree()) {
			main.pHfix = true;
			if (multiConc > 1) {
				main.pHrange = true;
			}
		}

		for (int a = 0; a < totSpec; a++) {
			oldConstantMemory[a] = species[a].constant;	/*keeps original logK in 'oldConstantMemory'*/

		}

		main.matrixIsLoaded = true;
		main.setCursor(Cursor.getDefaultCursor());
	}

	/*setzt Defaultwerte, Comp.Reihenfolge etc.*/
	void defaultsDatInput() {
		severalAdsorbents = false;
		severalSiteTypes = false;
		noOfAdsorbents = 0;			/*counter for various adsorbents*/

		int k = 0;
		int l = 0;
		int m = 0;
		for (int b = 0; b < totComp; b++) /*reads modes of components*/ {
			if (components[b].mode.isAdsorbent()) /*verschiedene Partikel*/ {
				adsNo[noOfAdsorbents++] = b;
				if (noOfAdsorbents > 1) {
					if (components[b].mode.isAdsorbent1_X()) {
						severalSiteTypes = true;
					}
					else {
						severalAdsorbents = true;
					}
				}
			}
			else if (components[b].mode.isChargeX_1()) {
				chargeNo1[k++] = b;		/*charge of 1st layer is in column b*/

			}
			else if (components[b].mode.isChargeX_2()) {
				chargeNo2[l++] = b;
			}
			else if (components[b].mode.isChargeX_3()) {
				chargeNo3[m++] = b;
			}
		}

		reorderComponents();
	}

	void reorderComponents() {
		int j = 0;			/*ii: sequence in which the components have to be arranged for the calculation*/

		for (int c = 1; c <= 8; c++) {
			for (int b = 0; b < totComp; b++) {
				if (components[b].mode.modeNo == c) {
					ii[j++] = b;
				}
			}
		}
	}

	// returns false when aborting with error, otherwise true
	boolean recalculate(int replaceCompNo, int spLibSpecNo) {
		int[] jj = new int[20];
		int[] kk = new int[20];
		Library spLib = main.spLibrary;

		for (int a = 0; a < spLib.libTotComp; a++) {
			if (spLib.libSpecMat[spLibSpecNo][a] != 0 && a != replaceCompNo) /*untersuche, ob die ausgew‰hlten Komponenten der Bibliothek*/ {																	/*in der Matrix auch vorkommen*/

				boolean ok = false;
				for (int b = 0; b < totComp; b++) {
					if (components[b].name.equals(spLib.libCompNames[a])) {
						ok = true;
					}
				}
				if (!ok) {
					MyTools.showError("Input matrix and solid phases-library are not compatible:\n"
							+ "Names of components do not match!");
					return false;
				}
			}		/*if ((spLibSpecMat[libCompNo]^^[a] != 0))*/

		}

		/*compress die Zeile aus der Bibliothek:*/
		int counter = 0;
		for (int a = 0; a < spLib.libTotComp; a++) {
			if (spLib.libSpecMat[spLibSpecNo][a] != 0) {
				jj[counter++] = a;
			}
		}

		/*Positionen der zu ver‰ndernden Komponenten in der Matrix:*/
		counter = 0;
		for (int a = 0; a < spLib.libTotComp; a++) {
			if (spLib.libSpecMat[spLibSpecNo][a] != 0) {
				for (int b = 0; b < totComp; b++) {
					if (components[b].name.equals(spLib.libCompNames[a])) {
						kk[counter++] = b;
					}
				}
			}
		}

		components[replaceCompNo].name = spLib.libSpecies[spLibSpecNo].name;	/*Bisheriger Komponentenname wird ersetzt mit dem neuen Namen des Festk‰rpers*/

		components[replaceCompNo].mode = Mode.SOLID_PHASE;	/*Modus wird ersetzt = solidPhase*/

		multiConcMatrix[0][replaceCompNo] = 1;					/*Konzentration wird angepasst = 1*/

		for (int a = 0; a < totSpec; a++) /*‹ber alle Zeilen a,b -> lines,columns*/ {
			if (speciesMat[a][replaceCompNo] != 0) /*falls in der auszutauschenden Kolonne ein Koeffizient != 0, dann muss gerechnet werden*/ {
				int c = 0;
				for (int b = 0; b < totComp; b++) /*zuerst muss die Koeffiziente der Hauptspezies ausgetauscht werden*/ {
					for (int d = 0; d < counter; d++) {
						if (b == kk[d]) /*falls eine Spezies gefunden, dann*/ {
							if (b == replaceCompNo) /*f¸r die Hauptspezies*/ {
								speciesMat[a][b] =
										speciesMat[a][b] / spLib.libSpecMat[spLibSpecNo][jj[c]];
							}
							c++;
						} // iterate over components
					}
				}
				c = 0;											/*dann erst alle anderen! Diese werden nach dem neuen Koeffizienten berechnet!*/

				for (int b = 0; b < totComp; b++) {
					for (int d = 0; d < counter; d++) {
						if (b == kk[d]) /*falls eine Spezies gefunden, dann*/ {
							if (b != replaceCompNo) /*f¸r die anderen Spezies*/ {
								speciesMat[a][b] = speciesMat[a][b]
										- speciesMat[a][replaceCompNo]
										* spLib.libSpecMat[spLibSpecNo][jj[c]];
							}
							c++;
						} // iterate over components
					}
				}
				species[a].constant = species[a].constant
						- speciesMat[a][replaceCompNo] * spLib.libSpecies[spLibSpecNo].constant;
				oldConstantMemory[a] = species[a].constant;
			} // iterate over species
		}
		return true;
	}


	/*neu z‰hlen von noOfSolidPhases, noOfCheckPrecip und totLimComp und einsetzen der Konz.*/
	void adjustConcEstim() {
		totLimComp = totComp;
		noOfSolidPhases = 0;
		noOfCheckPrecip = 0;

		for (int b = 0; b < totComp; b++) {
			if (components[b].mode == Mode.TOTAL) {
				concEstim[b] = components[b].conc > 0 ? MyTools.myLog(components[b].conc) - 3 : -3;
			}
			/*so konvergieren auch die Titrationen*/
			else if (components[b].mode == Mode.FREE) {
				totLimComp--;
				concEstim[b] = components[b].conc > 0 ? MyTools.myLog(components[b].conc) : 0;
				components[b].conc = 0;
			}
			else if (components[b].mode == Mode.SOLID_PHASE) {
				noOfSolidPhases++;
				totLimComp--;
				concEstim[b] = 0;
				components[b].conc = 0;
			}
			else if (components[b].mode == Mode.CHECKPRECIP) {
				noOfCheckPrecip++;
				concEstim[b] = components[b].conc > 0 ? MyTools.myLog(components[b].conc) - 3 : -3;
				/*so konvergieren auch die Titrationen*/
			}
			else if (adsorption()) {
				if (components[b].mode.isAdsorbent()) {
					concEstim[b] = -3;	/*ein  ungefährer Schätzwert, sollte angepasst werden können**/

				}
				if (model == ModelEnum.CONSTANT_CAP || model == ModelEnum.DIFFUSE_GTL) {
					if (components[b].mode.isChargeX_1()) {
						concEstim[b] = 0;
					}
				}
				else if (model == ModelEnum.STERN_L) {
					if (components[b].mode.isChargeX_1()) {
						concEstim[b] = -1;
					}
					else if (components[b].mode.isChargeX_2()) {
						concEstim[b] = -0.5;
					}
				}
				else if (model == ModelEnum.TRIPLE_L) {
					if (components[b].mode.isChargeX_1()) {
						concEstim[b] = -1;
					}
					else if (components[b].mode.isChargeX_2()) {
						concEstim[b] = -0.5;
					}
					else if (components[b].mode.isChargeX_2()) {
						concEstim[b] = 0;
					}
				}
			}		/*if (adsorption*/

		}		/*for (int b=0; b < totComp; b++)*/

	}

	boolean adsorption() {
		return noOfAdsorbents > 0;
	}

	boolean isHorHplusAndFree() {
		return (isLastCompName("H") || isLastCompName("H+"))
				&& components[totComp - 1].mode == Mode.FREE;
	}

	boolean isTotal() {
		for (int b = 0; b < totComp; b++) {
			if (components[b].mode == Mode.TOTAL) {
				return true;
			}
		}
		return false;
	}

	boolean isLastCompName(String s) {
		return components[totComp - 1].name.equals(s);
	}

	double getMultiConcForLast() {
		return multiConcMatrix[0][totComp - 1];
	}

	boolean speciesMatrixAllInt() {
		printInteger = true;
		for (int a = 0; a < totSpec; a++) {
			for (int b = 0; b < totComp; b++) {
				if (speciesMat[a][b] != Math.floor(speciesMat[a][b])) {
					printInteger = false;
					return printInteger;
				}
			}
		}
		return printInteger;
	}

// -----------------------------------------------------------------------------
// --- matrix computations -----------------------------------------------------
// -----------------------------------------------------------------------------
	void runProc() {
		main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// must use an instance variable since calcActivCoeff will be resetted
		// to "no" right after calculation but display of tables in output window
		// still depends on the former value of calcActivCoeff
		calculatedWithActivity = !main.isCalcActivCoeff(ActivityEnum.NO);

		// clear notes that are displayed in output window
		try {
			notesDoc.remove(0, notesDoc.getLength());
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}

		// all elements of new arrays are initialized with 0 automatically
		compConc = new double[columns];
		concMemory = new double[columns];
		errorEstim = new double[columns];
		amountPrecipitated = new double[columns];
		for (int a = 0; a < lines; a++) {
			logfSpec[a] = 0;
		}

		// The Pascal version of this program had the following three lines of
		// at the end of this method. Alas, resetting the components concentration
		// right after calculating them results in displaying wrong numbers in
		// the output window. However, the original comment (see below) suggests
		// that executing the statements before calculation may work, too.
		// And, yes, it seems to ...
		for (int b = 0; b < totComp; b++) /*restore old conc.values for evt. new calculation*/ {
			components[b].conc = multiConcMatrix[0][b];
		}
		adjustConcEstim();

		if (main.doDrawGraph) {
			main.graphicsWindow.setVisible(true);
			main.graphicsWindow.moveToFront();
		}
		if (!main.pHrange && !main.compRange && !main.adsRange && !main.logKrange
				&& !main.doKinetik && !main.dopXpYdiagram) {
			if (multiConc > 1) {
				main.outputFormat = OutputFormat.INTERVAL;
				initIntervalOutput();
			}
			for (int c = 0; c < multiConc; c++) {
				for (int b = 0; b < totComp; b++) {
					components[b].conc = multiConcMatrix[c][b];
				}

				if (noOfCheckPrecip > 0) /*in case precipitating species*/ {
					for (int b = 0; b < totComp; b++) /*total comp.conc. memory for solubility*/ {
						concMemory[b] = components[b].mode == Mode.CHECKPRECIP
								? components[b].conc : 0;
					}
				}

				adjustConcEstim();
				calcSimpleEquilibrium();
			}
		}

		if (main.pHrange && !main.adsRange && !main.doKinetik && !main.dopXpYdiagram) {	/*pH=constant or pH-range*/

			// Die Tabelle f¸r die mehrzeilige Datenausgabe muss hier in jedem Fall
			// initialisiert werden, da entweder "multiConc > 1 und "pH=constant"
			// gilt, oder: "multiConc = 1 und "pH-range"

			initIntervalOutput();
			if (multiConc > 1) {
				main.outputFormat = OutputFormat.INTERVAL;
			}
			for (int c = 0; c < multiConc; c++) {
				for (int b = 0; b < totComp; b++) {
					components[b].conc = multiConcMatrix[c][b];
				}

				if (noOfCheckPrecip > 0) /*in case precipitating species*/ {
					for (int b = 0; b < totComp; b++) /*total comp.conc. memory for solubility*/ {
						concMemory[b] = components[b].mode == Mode.CHECKPRECIP
								? components[b].conc : 0;
					}
				}

				adjustConcEstim();
				if (multiConc > 1) /*pH is set according to matrix*/ {
					main.pHrangeStart = -(concEstim[totComp - 1]);
					main.pHrangeEnd = main.pHrangeStart;
					main.pHrangeStep = main.pHrangeEnd;
				}
				calcPHInterval();
			}
		}

		if (main.compRange && !main.adsRange) {
			calcCompRangePH(main.pHfix);
		}

		if (main.adsRange) {
			calcAdsRange();
		}

		if (main.logKrange) {
			calcLogKrange();
		}

		if (main.doKinetik) {
			calcKinetik();
		}

		if (main.dopXpYdiagram) {
			try {
				calcpXpY();
			} catch (Exception ex) {
			} finally {
				for (int a = 0; a < totSpec; a++) {
					species[a].constant = oldConstantMemory[a];	/*setzt wieder die alten logK ein*/

				}
			}
		}

// ---------- see comment above --------------
//		for (int b=0; b < totComp; b++)		/*restore old conc.values for evt. new calculation*/
//			components[b].conc = multiConcMatrix[0][b];
//		adjustConcEstim();
// ---------- see comment above --------------
		main.setCursor(Cursor.getDefaultCursor());
	}

	private void calcPHInterval() /*pH=interval or pH=constant*/ {
		boolean log = main.isLogNumFormat();
		if (main.doDrawGraph) {
			initializeGraphicsSettings();
			main.plotView.configureAxisTitle(PlotView.BOTTOM_AXIS, "pH range", null);
			main.plotView.configureAxisTicks(PlotView.BOTTOM_AXIS,
					new DecimalFormat("#0.00"), 0, 0, 5, 3);
			main.plotView.configureAxisLinearScale(PlotView.BOTTOM_AXIS, 1.0, 0, false);
		}

		double pHrangeEndRelaxed =
				main.pHrangeEnd + Math.abs(main.pHrangeEnd / ROUNDING_ERROR_RELAXATION);
		while (main.pHrangeStart <= pHrangeEndRelaxed) {
			concEstim[totComp - 1] = -main.pHrangeStart;
			if (!checkSolubility()) {
				calculation();
				if (calculatedWithActivity) {
					calcActivityCoefficients();
				}
			}

			// data output and graphics
			addOutputData(log);
			main.dataOutput();
			if (main.doDrawGraph) {
				main.graphicsData.addData(this,
						new SpeciesData(main.pHrangeStart, main.drawSpecs, speConc));
			}
			if (calculatedWithActivity) {
				for (int a = 0; a < totSpec; a++) {
					species[a].constant = oldConstantMemory[a];			/*puts the original constants back in*/

				}
			}
			main.pHrangeStart += main.pHrangeStep;
		}
	}

	private void calcCompRangePH(boolean pHfix) {
		initIntervalOutput();
		if (main.doDrawGraph) {
			initializeGraphicsSettings();
			if (main.compRangeIsLog) {
				main.plotView.configureAxisLogarithmicScale(
						PlotView.BOTTOM_AXIS, 10, 0, false);
			}
			main.plotView.configureAxisTitle(PlotView.BOTTOM_AXIS, "conc. range of "
					+ components[compNo].mode + " " + components[compNo].name, null);
		}

		if (main.compRangeIsLog) {
			compRangeStart = MyTools.expo(10, compRangeStart);
			compRangeEnd = MyTools.expo(10, compRangeEnd);
		}

		double compRangeEndRelaxed =
				compRangeEnd + Math.abs(compRangeEnd / ROUNDING_ERROR_RELAXATION);
		while (compRangeStart <= compRangeEndRelaxed) {
			components[compNo].conc = compRangeStart;
			if (pHfix || components[compNo].mode != Mode.TOTAL) {
				concEstim[compNo] = MyTools.myLog(compRangeStart);	/*free conc.*/

			}
			else {
				concEstim[compNo] = MyTools.myLog(compRangeStart) - 3;
			}

			for (int b = 0; b < totComp; b++) {
				if (b == compNo && components[b].mode == Mode.CHECKPRECIP) /*total metal-memory for solubility*/ {
					concMemory[b] = components[b].conc;
				}
			}

			if (!checkSolubility()) {
				calculation();
				if (calculatedWithActivity) {
					calcActivityCoefficients();
				}
			}

			// data output and graphics
			addOutputData(main.isLogNumFormat());
			main.dataOutput();
			if (main.doDrawGraph) {
				main.graphicsData.addData(this,
						new SpeciesData(compRangeStart, main.drawSpecs, speConc));
			}

			if (calculatedWithActivity) {
				for (int a = 0; a < totSpec; a++) {
					species[a].constant = oldConstantMemory[a];	/*puts the original constants back in*/

				}
			}
			if (main.compRangeIsLog) {
				compRangeStart = MyTools.myLog(compRangeStart) + compRangeStep;
				compRangeStart = MyTools.expo(10, compRangeStart);
			}
			else {
				compRangeStart += compRangeStep;
			}
		}
	}

	private void calcSimpleEquilibrium() {
		if (!checkSolubility()) /*falls etwas ausf‰llt, ist die Speziierung schon in 'checkSolubility' gerechnet*/ {
			calculation();
			if (calculatedWithActivity) {
				calcActivityCoefficients();
			}
		}
		if (multiConc > 1) {
			addOutputData(main.isLogNumFormat());
		}
		main.dataOutput();
		if (calculatedWithActivity) {
			for (int a = 0; a < totSpec; a++) {
				species[a].constant = oldConstantMemory[a];		/*puts the original constants back in*/

			}
		}
	}

	private void calcAdsRange() {
		boolean log = main.isLogNumFormat();
		initIntervalOutput();
		if (main.doDrawGraph) {
			initializeGraphicsSettings();
			main.plotView.configureAxisTitle(PlotView.BOTTOM_AXIS,
					"range of adsorbent", null);
			main.plotView.configureAxisFixedBounds(PlotView.BOTTOM_AXIS,
					main.matrix.adsRangeStart, main.matrix.adsRangeEnd);
			main.plotView.configureAxisLinearScale(PlotView.BOTTOM_AXIS,
					main.matrix.adsRangeStep, 0, false);
		}

		if (main.pHfix) {
			concEstim[totComp - 1] = -main.pHrangeStart;
		}

		double adsRangeEndRelaxed =
				adsRangeEnd + Math.abs(adsRangeEnd / ROUNDING_ERROR_RELAXATION);
		while (adsRangeStart <= adsRangeEndRelaxed) {
			solidConc[0] = adsRangeStart;
			calculation();
			if (calculatedWithActivity) {
				calcActivityCoefficients();
			}

			// data output and graphics
			addOutputData(log);
			main.dataOutput();
			if (main.doDrawGraph) {
				main.graphicsData.addData(this,
						new SpeciesData(adsRangeStart, main.drawSpecs, speConc));
			}
			if (calculatedWithActivity) {
				for (int a = 0; a < totSpec; a++) {
					species[a].constant = oldConstantMemory[a];				/*puts the original constants back in*/

				}
			}
			adsRangeStart += adsRangeStep;
		}
	}

	private void calcLogKrange() {
		boolean log = main.isLogNumFormat();
		initIntervalOutput();
		if (main.doDrawGraph) {
			initializeGraphicsSettings();
			main.plotView.configureAxisTitle(PlotView.BOTTOM_AXIS,
					"range of log K for " + species[specNo].name, null);
			main.plotView.configureAxisFixedBounds(PlotView.BOTTOM_AXIS,
					main.matrix.logKrangeStart, main.matrix.logKrangeEnd);
			main.plotView.configureAxisLinearScale(PlotView.BOTTOM_AXIS,
					main.matrix.logKrangeStep, 0, false);
		}

		if (multiConc > 1) {
			MyTools.showWarning("Your matrix contains an array of concentrations."
					+ "Log K range is done with the first concentrations only.");
		}
		double logKrangeEndRelaxed =
				logKrangeEnd + Math.abs(logKrangeEnd / ROUNDING_ERROR_RELAXATION);
		while (logKrangeStart <= logKrangeEndRelaxed) {
			species[specNo].constant = logKrangeStart;
			if (!checkSolubility()) {
				calculation();
				if (calculatedWithActivity) {
					calcActivityCoefficients();
				}
			}

			// data output and graphics
			addOutputData(log);
			main.dataOutput();
			if (main.doDrawGraph) {
				main.graphicsData.addData(this,
						new SpeciesData(logKrangeStart, main.drawSpecs, speConc));
			}

			if (calculatedWithActivity) {
				for (int a = 0; a < totSpec; a++) {
					species[a].constant = oldConstantMemory[a];			/*puts the original constants back in*/

				}
			}

			logKrangeStart += logKrangeStep;
		}
	}

	private void calcKinetik() /*reactionOrder, reactionRate, kinComp, timeInc, rateConst */ {
		boolean log = main.isLogNumFormat();
		initIntervalOutput();
		if (main.doDrawGraph) {
			initializeGraphicsSettings();
			main.plotView.configureAxisTitle(PlotView.BOTTOM_AXIS, "KINETIC: time", "sec");
			main.plotView.configureAxisTicks(PlotView.BOTTOM_AXIS,
					new DecimalFormat("#0.0"), 0, 0, 5, 2);
			main.plotView.configureAxisFixedBounds(PlotView.BOTTOM_AXIS,
					0.0, kinTimeEnd);
			main.plotView.configureAxisLinearScale(PlotView.BOTTOM_AXIS,
					kinTimeInc, 0, false);
		}

		double C_equil;
		if (multiConc > 1) {
			MyTools.showWarning("Your matrix contains an array of concentrations."
					+ "Kinetik is performed with the first concentrations only.");
		}

		double t = 0;
		if (components[kinCompP].conc <= 0) {
			components[kinCompP].conc = 1e-38;
		}
		calculation();			/*erste Rechnung, als Ausgangslage, wenn t(kin)=0*/

		// data output and graphics

		addOutputData(log);
		main.dataOutput();
		if (main.doDrawGraph) {
			main.graphicsData.addData(
					this, new SpeciesData(t, main.drawSpecs, speConc));
		}

		t = kinTimeInc;						/*erster Zeitschritt*/

		while (t <= kinTimeEnd) {
			double Co;
			double Ct = 0;	/*Ct: Konz des Edukts nach der Zeit t*/

			double Db = 0;
			double Po;
			double Pt;
			Co = compConc[kinCompC];	/*freie Konzentrationen einsetzen f¸r kinetische Rechnungen:*/

			Po = compConc[kinCompP];	/*f¸r jeden Schritt ist vorher das Co, Db, Po als Spezies neu berechnet worden*/

			if (reactionOrder == 21) {
				Db = compConc[kinCompD];
			}

			/** ** gepr¸ft *** */
			if (reactionOrder == 0) /*ZeroOrder:		-dC/dt = k*/ {
				Ct = Co - coeffC * rateConst * kinTimeInc;
				if (Co < (coeffC * rateConst * kinTimeInc)) {
					Ct = 0;
				}
			}

			/** ** gepr¸ft *** */
			if (reactionOrder == 10) /*1st Order:		cC -> P + ...		-dC/dt = k*C*/ {
				Ct = Co * Math.exp(-coeffC * rateConst * kinTimeInc);
			}

			/** ** nicht m‰glich !!!!!!!! *** */
			if (reactionOrder == 11) /*1st Order:		C <=> P,  with Backreaction*/ {
				/*Ct = (Co / (rateConst + kBack)) * (rateConst / (Math.exp((rateConst + kBack) * kinTimeInc)) + kBack);     geht nur, wenn P nicht durch Komplexbildung verschwindet!*/
				Ct = Co * Math.exp(-coeffC * rateConst * kinTimeInc);
				Pt = Po * Math.exp(-coeffP * kBack * kinTimeInc);

				components[kinCompC].conc =
						components[kinCompC].conc - (Co - Ct) + (Po - Pt);
				if (components[kinCompC].conc <= 0) {
					components[kinCompC].conc = 1e-38;
				}
				concEstim[kinCompC] = MyTools.myLog(components[kinCompC].conc);
				components[kinCompP].conc =
						components[kinCompP].conc + coeffP / coeffC * (Co - Ct) - (Po - Pt);
				if (components[kinCompP].conc <= 0) {
					components[kinCompP].conc = 1e-38;
				}
				concEstim[kinCompP] = MyTools.myLog(components[kinCompP].conc);
			}

			/** ** gepr¸ft *** */
			if (reactionOrder == 20) /*2ndOrder:		cC -> P + ...		-dC/dt = k*C*C*/ {
				Ct = Co / (Co * coeffC * rateConst * kinTimeInc + 1);
			}

			/** ** ? *** */
			if (reactionOrder == 21) /*2ndOrder:		C+D -> P + ...		-dC/dt = k*C*D*/ {
				if (Co == Db) {
					Db = Db + Db * 1e-10;
				}
				Ct = Co - coeffC * (Co * Db * (Math.exp((Co - Db) * rateConst * kinTimeInc) - 1) / (coeffD * Co * Math.exp(
						(Co - Db) * rateConst * kinTimeInc) - coeffC * Db));
				components[kinCompD].conc =
						components[kinCompD].conc - coeffD / coeffC * (Co - Ct);			/*neue Konz des Edukts D*/

				if (components[kinCompD].conc <= 0) {
					components[kinCompD].conc = 1e-20;
				}
				concEstim[kinCompD] = MyTools.myLog(components[kinCompD].conc);
			}

			/** ** gepr¸ft *** Frost&Pearson, p18 */
			if (reactionOrder == 22) /*1st Order in C:		cC -> pP,  autocatalysis   -dC/dt = k*C*P,*/ {
				if ((Po <= 0)) {
					Po = 1e-20;
				}
				Ct =
						1 / coeffC * (Co - (Co * Po * (coeffP * Math.exp(rateConst * kinTimeInc * (Co + Po)) - coeffC)) / (coeffC * Co + coeffP * Po * Math
						.exp(rateConst * kinTimeInc * (Co + Po))));
			}

			/** ** sollte OK sein *** */
			if (reactionOrder == 30) /*3rdOrder:		cC -> P+...   		-1/c*dC/dt = k*C*C*C*/ {
				Ct = Co / Math.sqrt(2 * coeffC * rateConst * kinTimeInc * Co * Co + 1);
			}

			if (reactionOrder != 11) {
				components[kinCompC].conc = components[kinCompC].conc - (Co - Ct);			/*neue TOTALKONZ des Edukts*/

				if (components[kinCompC].conc <= 0) {
					components[kinCompC].conc = 1e-38;
				}
				concEstim[kinCompC] = MyTools.myLog(components[kinCompC].conc);

				components[kinCompP].conc =
						components[kinCompP].conc + coeffP / coeffC * (Co - Ct);		/*neue TOTALKONZ des Produkts*/

				if (components[kinCompP].conc <= 0) {
					components[kinCompP].conc = 1e-38;
				}
				concEstim[kinCompP] = MyTools.myLog(components[kinCompP].conc);
			}

			calculation();

			// data output and graphics
			addOutputData(log);
			main.dataOutput();
			if (main.doDrawGraph) {
				main.graphicsData.addData(
						this, new SpeciesData(t, main.drawSpecs, speConc));
			}

			t += kinTimeInc;
		} /*while t <= kinTimeEnd*/

//		if (reactionOrder == 11 && main.doDrawGraph)
//		{
//			C_equil = multiConcMatrix[0][kinCompC] * kBack / (rateConst + kBack);
//			moveto(250, 100);
//			drawString("Equilibrium conc. of comp '");
//			drawString(compNames[kinCompC]);
//			drawString("'");
//			moveto(300, 120);
//			drawString("without speciation would be: ");
//			drawString(stringof(C_equil : 12));
//		}
	}

	// "subroutine" for calcpXpY()
	private void changeSolidSpecLogK(boolean[] gases) {
		String myName;
		for (int a = 0; a < pXpYspecs; a++) {
			myName = species[a].name;				/*untersuche den Namen nach '(s)'*/

			if (!myName.equals("")) {
				if (myName.indexOf("(s)") > 0) /*ein solid phase*/ {
					if ((mainpXpYcomp == xComp || mainpXpYcomp == yComp)
							&& components[mainpXpYcomp].mode == Mode.FREE) {
						species[a].constant += speciesMat[a][mainpXpYcomp]
								* concEstim[mainpXpYcomp];
					}
					else {
						species[a].constant += speciesMat[a][mainpXpYcomp]
								* MyTools.myLog(components[mainpXpYcomp].conc);
					}
				}
				else {
					gases[a] = myName.indexOf("(g)") > 0;	/*ein Gas if true*/

				}
			}
		}
	}		/*am Ende werden aus "oldConstantMemory" wieder die alten logK eingesetzt!*/


	// "subroutine" of calcpXpY(). Returns dominating species as well as gas info
	// encoded as sign of the return value
	private int searchDominatingSpecies(boolean[] gases) {
		double dominatingSpecConc = 0;	/*diejenige Spezies mit der hˆchsten Konz suchen*/

		int dominatingSpec = 0;
		isAGas = false;
		for (int a = 0; a < pXpYspecs; a++) /*suche ob ein Gas*/ {
			if (gases[a] && speConc[a] >= 1 && speConc[a] > dominatingSpecConc) {	/*falls ein Gas und Partialdruck >=1 atm und conc > als beim letzten*/

				dominatingSpecConc = speConc[a];
				dominatingSpec = a;
				isAGas = true;
			}
		}
		if (!isAGas) /*wenn es kein Gas >1atm gibt*/ {
			for (int a = 0; a < pXpYspecs; a++) {
				if (speConc[a] > dominatingSpecConc) {
					dominatingSpecConc = speConc[a];
					dominatingSpec = a;
				}
			}
		}
		return dominatingSpec;
	}

	private void calcpXpY() {
		int x;
		int oldSpec;
		int newSpec;
		int newVSpec;
		double memoryLogConcX;
		double memoryLogConcY;
		double myLogConcX;
		double myLogConcY;
		boolean[] pXpYgases = new boolean[lines];
		boolean result;

		// init draw settings
		main.graphicsData.initializeDataList(this, PxPyData.class);
		main.graphicsData.configureVariableDrawing(PxPyData.class, 1,
				null, ColorEnum.BLACK, null, MarkerEnum.FILLED_CIRCLE,
				LineEnum.NONE, SizeEnum.TINY);
		main.graphicsData.configureDataSourceDrawing(this, null,
				null, null, MarkerEnum.AS_IS, null, null);
		main.plotView.configureSelectedData(
				PxPyData.class, new int[]{1}, 0, new int[]{0}, false);
		// init view
		main.plotView.configureView(
				null, 12, null, Color.white, false, false, true, false, false);
		final int ba = PlotView.BOTTOM_AXIS;
		final int la = PlotView.LEFT_AXIS;
		main.plotView.configureAxisTitle(ba, "-log " + components[xComp].name, null);
		main.plotView.configureAxisTitle(la, "-log " + components[yComp].name, null);
		main.plotView.configureAxisFixedBounds(ba, pXrangeStart, pXrangeEnd);
		main.plotView.configureAxisFixedBounds(la, pYrangeStart, pYrangeEnd);
		main.plotView.configureAxisLinearScale(ba, 0, 0, false);
		main.plotView.configureAxisLinearScale(la, 0, 0, false);

		/*----------------------------------*/
		if (noOfCheckPrecip > 0) /*in case precipitating species*/ {
			for (int b = 0; b < totComp; b++) {
				if (components[b].mode == Mode.CHECKPRECIP) {
					concMemory[b] = components[b].conc;  /*total comp.conc. memory for solubility*/

				}
				else {
					concMemory[b] = 0;
				}
			}
		}
		/*----------------------------------*/

		concEstim[yComp] = -pYrangeStart;		/*Ausgangswerte f¸r Y einsetzen*/

		if (components[yComp].mode == Mode.FREE) {
			components[yComp].conc = 0;			/*falls mode='free'*/

			myLogConcY = concEstim[yComp];
		}
		else {													/*falls mode='total'*/

			components[yComp].conc = MyTools.expo(10, concEstim[yComp]);
			myLogConcY = MyTools.myLog(components[yComp].conc);
		}

		changeSolidSpecLogK(pXpYgases);	// sets pXpYgases

		while (-myLogConcY < pYrangeEnd) {
			concEstim[xComp] = -pXrangeStart;	/*Ausgangswerte f¸r X einsetzen*/

			if (components[xComp].mode == Mode.FREE) {
				components[xComp].conc = 0;
				myLogConcX = concEstim[xComp];
			}
			else {
				components[xComp].conc = MyTools.expo(10, concEstim[xComp]);
				myLogConcX = MyTools.myLog(components[xComp].conc);
			}
			if (!checkSolubility()) /*falls etwas ausf‰llt, ist die Speziierung schon in 'checkSolubility' gerechnet*/ {
				calculation();								/*ersteRechnung auf der X-Achse*/

			}

			oldSpec = searchDominatingSpecies(pXpYgases);	// side effect: determines isAGas

			while (-myLogConcX < pXrangeEnd - pXpYstep) {									/*der n‰chste Punkt in HORIZONTALER Richtung*/

				memoryLogConcX = myLogConcX;						/*diesen alten Wert behalten um nachher vertikal zu berechnen*/

				concEstim[xComp] = myLogConcX - pXpYstep;		/*Intervall, n‰chster Schritt*/

				if (components[xComp].mode == Mode.FREE) {
					components[xComp].conc = 0;
					myLogConcX = concEstim[xComp];
				}
				else {
					components[xComp].conc = MyTools.expo(10, concEstim[xComp]);
					myLogConcX = MyTools.myLog(components[xComp].conc);
				}
				if (!checkSolubility()) /*falls etwas ausf‰llt, ist die Speziierung schon in 'checkSolubility' gerechnet*/ {
					calculation();
				}

				newSpec = searchDominatingSpecies(pXpYgases);

				if (oldSpec != newSpec) /*falls nicht mehr dieselbe Spezies dominiert...*/ {
					result = false;
					x = 1;
					do {
						if ((!isAGas && (Math.abs(speConc[oldSpec] - speConc[newSpec]) < (speConc[newSpec] / 1000)))
								|| (isAGas && speConc[newSpec] > 0.999 && speConc[newSpec] < 1.001)
								|| (!isAGas && pXpYgases[oldSpec] && speConc[oldSpec] > 0.999)) {						/*wenn die beiden Konz nur 0.1% auseinander..*/

							result = true;
							/** * store data and GRAPHICS ** */
							main.graphicsData.addData(
									this, new PxPyData(-myLogConcX, -myLogConcY,
											species[oldSpec].name, species[newSpec].name));
						}
						else {		/*versuchen, einen besseren Wert f¸r concEstim[xComp] zu erhalten*/

							if (!isAGas) {
								if (speConc[oldSpec] > speConc[newSpec]) {
									concEstim[xComp] = myLogConcX - pXpYstep / MyTools.expo(2, x);
								}
								else {
									concEstim[xComp] = myLogConcX + pXpYstep / MyTools.expo(2, x);
								}
							}
							else if (isAGas) {
								if (speConc[newSpec] > 1) {
									concEstim[xComp] = myLogConcX + pXpYstep / MyTools.expo(2, x);
								}
								else {
									concEstim[xComp] = myLogConcX - pXpYstep / MyTools.expo(2, x);
								}
							}

							if (components[xComp].mode == Mode.FREE) {
								components[xComp].conc = 0;
								myLogConcX = concEstim[xComp];
							}
							else {
								components[xComp].conc = MyTools.expo(10, concEstim[xComp]);
								myLogConcX = MyTools.myLog(components[xComp].conc);
							}
							if (!checkSolubility()) /*falls etwas ausf‰llt, ist die Speziierung schon in 'checkSolubility' gerechnet*/ {
								calculation();
							}
							x += 1;
						}
					}
					while (!result);
				}	/*if (oldSpec != newSpec*/

				concEstim[xComp] = memoryLogConcX;				/*vorheriger pX-Wert wieder einsetzen um damit Vertikalwert zu rechnen*/

				if (components[xComp].mode == Mode.FREE) {
					components[xComp].conc = 0;
					myLogConcX = concEstim[xComp];
				}
				else {
					components[xComp].conc = MyTools.expo(10, concEstim[xComp]);
					myLogConcX = MyTools.myLog(components[xComp].conc);
				}

//	/**** jetzt in VERTIKALER Richtung, nach oben, falls eine horizontale Trennungslinie besteht ****/
				memoryLogConcY = myLogConcY;						/*am Ende wird der alte pY-Wert wieder eingesetzt um horizontal weiterzurechnen*/

				concEstim[yComp] = myLogConcY - pXpYstep;		/*n‰chster Punkt in VERTIKALER Richtung*/

				if (components[yComp].mode == Mode.FREE) {
					components[yComp].conc = 0;
					myLogConcY = concEstim[yComp];
				}
				else {
					components[yComp].conc = MyTools.expo(10, concEstim[yComp]);
					myLogConcY = MyTools.myLog(components[yComp].conc);
				}
				if (!checkSolubility()) /*falls etwas ausf‰llt, ist die Speziierung schon in 'checkSolubility' gerechnet*/ {
					calculation();
				}
				newVSpec = searchDominatingSpecies(pXpYgases);

				if (oldSpec != newVSpec) /*falls nicht mehr dasselbe Spezies dominiert...*/ {
					result = false;
					x = 1;
					do {
						if ((!isAGas && (Math.abs(speConc[oldSpec] - speConc[newVSpec]) < (speConc[newVSpec] / 1000)))
								|| (isAGas && speConc[newVSpec] > 0.999 && speConc[newVSpec] < 1.001)
								|| (!isAGas && pXpYgases[oldSpec] && speConc[oldSpec] > 0.999)) {						/*wenn die beiden Konz nur 0.1% auseinander..*/

							result = true;
							/** * store data and GRAPHICS ** */
							main.graphicsData.addData(
									this, new PxPyData(-myLogConcX, -myLogConcY,
											species[oldSpec].name, species[newSpec].name));
						}
						else {		/*versuchen, einen besseren Wert f¸r concEstim[yComp] zu erhalten*/

							if (!isAGas) {
								if (speConc[oldSpec] > speConc[newVSpec]) {
									concEstim[yComp] = myLogConcY - pXpYstep / MyTools.expo(2, x);
								}
								else {
									concEstim[yComp] = myLogConcY + pXpYstep / MyTools.expo(2, x);
								}
							}
							else if (isAGas) {
								if (speConc[newVSpec] > 1) {
									concEstim[yComp] = myLogConcY + pXpYstep / MyTools.expo(2, x);
								}
								else {
									concEstim[yComp] = myLogConcY - pXpYstep / MyTools.expo(2, x);
								}
							}

							if (components[yComp].mode == Mode.FREE) {
								components[yComp].conc = 0;
								myLogConcY = concEstim[yComp];
							}
							else {
								components[yComp].conc = MyTools.expo(10, concEstim[yComp]);
								myLogConcY = MyTools.myLog(components[yComp].conc);
							}
							if (!checkSolubility()) /*falls etwas ausf‰llt, ist die Speziierung schon in 'checkSolubility' gerechnet*/ {
								calculation();
							}
							x += 1;
						}
					}
					while (!result);
				}	/*if (oldSpec != newSpec*/

				concEstim[yComp] = memoryLogConcY;
				if (components[yComp].mode == Mode.FREE) {
					components[yComp].conc = 0;
					myLogConcY = concEstim[yComp];
				}
				else {
					components[yComp].conc = MyTools.expo(10, concEstim[yComp]);
					myLogConcY = MyTools.myLog(components[yComp].conc);
				}

				concEstim[xComp] = myLogConcX - pXpYstep;		/*urspr¸nglicher neuer X-Wert wieder einsetzen, der nur f¸r verikale zur¸ckgesetzt wurde*/

				if (components[xComp].mode == Mode.FREE) {
					components[xComp].conc = 0;
					myLogConcX = concEstim[xComp];
				}
				else {
					components[xComp].conc = MyTools.expo(10, concEstim[xComp]);
					myLogConcX = MyTools.myLog(components[xComp].conc);
				}
				/** ** vertikal abgeschlossen, alter Wert wieder eingesetzt *** */

				oldSpec = newSpec;
			} /* while -myLogConcX < pXrangeEnd - pXpYstep*/

			concEstim[yComp] = myLogConcY - pXpYstep;		/*Intervall, n‰chster Schritt*/

			if (components[yComp].mode == Mode.FREE) {
				components[yComp].conc = 0;
				myLogConcY = concEstim[yComp];
			}
			else {
				components[yComp].conc = MyTools.expo(10, concEstim[yComp]);
				myLogConcY = MyTools.myLog(components[yComp].conc);
			}
			for (int a = 0; a < totSpec; a++) {
				species[a].constant = oldConstantMemory[a];
			}
			changeSolidSpecLogK(pXpYgases);
		}	/*while -myLogConcY < pYrangeEnd*/

		main.dataOutput();
	} // calcpXpY()

	/*treats a dissolved component which is to check for precipitation as a solid phase, */
	/*calculates the maximum soluble concentration and compares to the original total concentration.*/
	/*'errorEstim' is the max soluble conc., 'amountPrecipitated' gives the amount of eventually*/
	/*precipitated concentration of the component in moles/liter. It returns a boolean true if (precipitation occured */
	private boolean checkSolubility() {
		if (noOfCheckPrecip == 0) {
			return false; // in case precipitation is not possible
		}
		// precipitation is possible, but suppose it is not
		boolean precipitation = false;
		int oldTotLimComp = totLimComp;

		for (int b = 0; b < oldTotLimComp; b++) {
			if (components[ii[b]].mode == Mode.CHECKPRECIP) /*if possible precipitation*/ {
				components[ii[b]].conc = 0;
				concEstim[ii[b]] = 0;
				totLimComp--;
			}
		}

		calculation();
		if (calculatedWithActivity) {
			calcActivityCoefficients();
		}

		totLimComp = oldTotLimComp;								/*restore old value*/

		for (int b = 0; b < totLimComp; b++) {
			if (components[ii[b]].mode == Mode.CHECKPRECIP) {
				if (concMemory[ii[b]] > errorEstim[ii[b]]) /*if there is precipitation (errorEstim=max soluble conc.)*/ {
					precipitation = true;
					amountPrecipitated[ii[b]] = concMemory[ii[b]] - errorEstim[ii[b]];
					if (main.outputFormat != OutputFormat.INTERVAL) {
						writeNote("\n" + components[ii[b]].name + "-precipitation: "
								+ MyTools.UPTO_6_DIGITS.format(amountPrecipitated[ii[b]]));
					}
					components[ii[b]].conc = errorEstim[ii[b]];			/*new tot.solub. component*/

				}
				else {
					components[ii[b]].conc = concMemory[ii[b]];
				}
				concEstim[ii[b]] = MyTools.myLog(components[ii[b]].conc);
			}
		}
		return precipitation;
	}	// checkSolubility

	/*reads charges from the names on the coefficient file*/
	private int readChargesFromName(String n) {
		int result = 0;
		for (int i = 0; i < n.length(); i++) {
			if (n.charAt(i) == '+') /*jedes '+' im Namen z‰hlt als 1 positive Ladung*/ {
				result++;
			}
			if (n.charAt(i) == '-') /*jedes '-' im Namen z‰hlt als 1 negative Ladung*/ {
				result--;
			}
		}
		return result;
	}

	/** * Kcorr=[A1][A2]/[A3], K(I=0)=(A1)(A2)/(A3) , Kcorr=K(I=0)*f3/(f1f2) ** */
	private void calcActivityCoefficients() {
		int counter;
		boolean ok;
		boolean allDissolved;
		int[] zSpec = new int[lines];
		double[] logKCorr = new double[lines];
		int[] zComp = new int[columns];
		double[] logfComp = new double[columns];

		counter = 0;
		ok = false;

		/*reads charges from the names on the coefficient file*/
		for (int a = 0; a < totSpec; a++) {
			zSpec[a] = readChargesFromName(species[a].name);	/*charge of species a*/

		}
		for (int b = 0; b < totComp; b++) {
			zComp[b] = readChargesFromName(components[b].name);	/*charge of component b*/

		}

		do {
			if (++counter > 10) {
				throw new RuntimeException(
						"Adjusting activity coefficients failed to converge after 10 runs."
						+ " Ionic strength may be unreasonably high!");
			}

			if (!fixionicStr) {	// CalcionicStrength
				boolean clear;
				ionicStr = 0;
				for (int a = 0; a < totSpec; a++) {
					clear = true;

					if (adsorption()) {
						for (int c = 0; c < noOfAdsorbents; c++) {
							if (speciesMat[a][adsNo[c]] != 0) {
								clear = false;		 /*partikul‰re Spezies z‰hlen nicht f¸r die Ionenst‰rke!*/

							}
						}
					}

					if (species[a].name.equals("e-")) /*Elektronen z‰hlen nicht zur Ionenst‰rke*/ {
						clear = false;
					}

					if (clear) {
						ionicStr += 0.5 * (speConc[a] * (zSpec[a] * zSpec[a]));
					}
				}
			}

			if (main.isCalcActivCoeff(ActivityEnum.DEBYEHUECKEL)) {
				for (int a = 0; a < totSpec; a++) {
					if (species[a].isHPlusOrEMinus()) {
						logfSpec[a] = 0;
					}
					else {
						logfSpec[a] = -activA * zSpec[a] * zSpec[a] * Math.sqrt(ionicStr);			/*activity coeff for all species except H+ and OH-*/

					}
				}
				for (int b = 0; b < totComp; b++) {
					if (components[b].isModeSolidPhaseOrCheckPrecip()
							|| components[b].mode.isAdsorbentX()
							|| components[b].isHPlusOrEMinus()) {
						logfComp[b] = 0;
					}
					else {
						logfComp[b] = -activA * zComp[b] * zComp[b] * Math.sqrt(ionicStr);			/*activity coeff for all components*/

					}
				}
			}
			if (main.isCalcActivCoeff(ActivityEnum.GUENTELBERG)) {
				for (int a = 0; a < totSpec; a++) {
					if (species[a].isHPlusOrEMinus()) {
						logfSpec[a] = 0;
					}
					else {
						logfSpec[a] = -activA * zSpec[a] * zSpec[a]
								* Math.sqrt(ionicStr) / (1 + Math.sqrt(ionicStr));
					}
				}
				for (int b = 0; b < totComp; b++) {
					if (components[b].isModeSolidPhaseOrCheckPrecip()
							|| components[b].mode.isAdsorbentX()
							|| components[b].isHPlusOrEMinus()) {
						logfComp[b] = 0;
					}
					else {
						logfComp[b] = -activA * zComp[b] * zComp[b] * Math.sqrt(ionicStr) / (1 + Math.sqrt(ionicStr));
					}
				}
			}
			if (main.isCalcActivCoeff(ActivityEnum.DAVIES)) {
				for (int a = 0; a < totSpec; a++) {
					if (species[a].name.equals("H+") || species[a].name.equals("e-")) {
						logfSpec[a] = 0;	/*activity coeff=1*/

					}
					else {
						logfSpec[a] =
								-activA * zSpec[a] * zSpec[a] * (Math.sqrt(ionicStr) / (1 + Math.sqrt(ionicStr)) - 0.2 * ionicStr);
					}
				}
				for (int b = 0; b < totComp; b++) {
					if (components[b].isModeSolidPhaseOrCheckPrecip()
							|| components[b].mode.isAdsorbentX()
							|| components[b].isHPlusOrEMinus()) {
						logfComp[b] = 0;
					}
					else {
						logfComp[b] =
								-activA * zComp[b] * zComp[b] * (Math.sqrt(ionicStr) / (1 + Math.sqrt(ionicStr)) - 0.2 * ionicStr);
					}
				}
			}

			for (int a = 0; a < totSpec; a++) /*calculates activity-corrected new log K*/ {
				double x = 0;
				for (int b = 0; b < totComp; b++) {
					allDissolved = true;
					if (adsorption()) /*exclude components that have to do with surfaces*/ {
						for (int c = 0; c < noOfAdsorbents; c++) {
							if (adsNo[c] == b || chargeNo1[c] == b || chargeNo2[c] == b || chargeNo3[c] == b) {
								allDissolved = false;
							}
						}
					}
					if (allDissolved) /*only in case NO particulate species or surface charges are involved*/ {
						x += speciesMat[a][b] * logfComp[b];
					}
				}
				logKCorr[a] = oldConstantMemory[a] + x - logfSpec[a];
			}

			ok = true;
			for (int a = 0; a < totSpec && ok; a++) {
				if (Math.abs(species[a].constant - logKCorr[a]) > 0.001) {
					ok = false;
				}
			}

			if (!ok) {
				for (int a = 0; a < totSpec; a++) {
					species[a].constant = logKCorr[a];
				}
				calculation();
			}
		}
		while (!ok);

		for (int a = 0; a < totSpec; a++) {
			species[a].constant = logKCorr[a];
		}
	}	/*CalcActivityCoefficients*/


	private double[] calculatePsiAndSigma(int c) {
		double psi0 = 0;
		double psi1 = 0;
		double psi2 = 0;
		double psi3 = 0;
		double factor;

		if (!severalSiteTypes) {
			psi0 = -B5 * concEstim[chargeNo1[c]];	/*Potential an der Oberfl‰che in Volt*/
			/*Jetzt gehts nur noch darum, die Konz. der Ladung an den versch. Oberfl.stellen zu berechnen*/

			factor = area[c] * solidConc[c] / B6;	/*ein Umrechnungsfaktor, um die Oberfl‰chenladung sigma von C/m2 in mol/l umzurechnen*/

			components[adsNo[c]].conc = surfaceSites[c] * solidConc[c];		/*totale Konz. der Oberfl.PLAETZEN in mol/l*/

			if (model == ModelEnum.CONSTANT_CAP) /*jetzt Anw}ung des constant cap. models: sigma=kappa*psi*/ {
				components[chargeNo1[c]].conc = innerCap[c] * psi0 * factor;	/*totale Konz. an Oberfl.LADUNGEN in mol/l*/

			}

			if (model == ModelEnum.DIFFUSE_GTL) {
				components[chargeNo1[c]].conc = B2 * Math.sqrt(ionicStr) * MyTools.mySinh(B3 * psi0) * factor;
			}

			if (model == ModelEnum.STERN_L) {
				psi1 = -B5 * concEstim[chargeNo2[c]];			/*Potential der 1. Schicht in Volt*/

				psi3 = -B2 * Math.sqrt(ionicStr) * MyTools.mySinh(B3 * psi1);
				components[chargeNo1[c]].conc = (psi0 - psi1) * innerCap[c] * factor;
				components[chargeNo2[c]].conc =
						-components[chargeNo1[c]].conc - psi3 * factor;
			}

			if (model == ModelEnum.TRIPLE_L) {
				psi1 = -B5 * concEstim[chargeNo2[c]];
				psi2 = -B5 * concEstim[chargeNo3[c]];
				psi3 = -B2 * Math.sqrt(ionicStr) * MyTools.mySinh(B3 * psi2);
				components[chargeNo1[c]].conc = (psi0 - psi1) * innerCap[c] * factor;
				components[chargeNo3[c]].conc = (psi2 - psi1) * outerCap[c] * factor;
				components[chargeNo2[c]].conc =
						-components[chargeNo1[c]].conc - components[chargeNo3[c]].conc;
			}
		}
		else {
			// assert severalSiteTypes;
			psi0 = -B5 * concEstim[chargeNo1[0]];					/*Potential an der Oberfl‰che in Volt*/
			/*Jetzt gehts nur noch darum, die Konz. der Ladung an den versch. Oberfl.stellen zu berechnen*/

			factor = area[0] * solidConc[0] / B6;					/*ein Umrechnungsfaktor, um die Oberfl‰chenladung sigma von C/m2 in mol/l umzurechnen*/

			components[adsNo[c]].conc = surfaceSites[c] * solidConc[0];		/*totale Konz. der Oberfl.PLAETZE in mol/l*/

			if (model == ModelEnum.CONSTANT_CAP) /*jetzt Anw}ung des constant cap. models: sigma=kappa*psi*/ {
				components[chargeNo1[0]].conc = innerCap[0] * psi0 * factor;	/*totale Konz. an Oberfl.LADUNGEN in mol/l*/

			}

			else if (model == ModelEnum.DIFFUSE_GTL) {
				components[chargeNo1[0]].conc = B2 * Math.sqrt(ionicStr) * MyTools.mySinh(B3 * psi0) * factor;
			}

			else if (model == ModelEnum.STERN_L) {
				psi1 = -B5 * concEstim[chargeNo2[0]];			/*Potential der 1. Schicht in Volt*/

				psi3 = -B2 * Math.sqrt(ionicStr) * MyTools.mySinh(B3 * psi1);
				components[chargeNo1[0]].conc = (psi0 - psi1) * innerCap[0] * factor;
				components[chargeNo2[0]].conc = -components[chargeNo1[0]].conc - psi3 * factor;
			}
			else if (model == ModelEnum.TRIPLE_L) {
				psi1 = -B5 * concEstim[chargeNo2[0]];
				psi2 = -B5 * concEstim[chargeNo3[0]];
				psi3 = -B2 * Math.sqrt(ionicStr) * MyTools.mySinh(B3 * psi2);
				components[chargeNo1[0]].conc = (psi0 - psi1) * innerCap[0] * factor;
				components[chargeNo3[0]].conc = (psi2 - psi1) * outerCap[0] * factor;
				components[chargeNo2[0]].conc = -components[chargeNo1[0]].conc - components[chargeNo3[0]].conc;
			}
		}

		return new double[]{psi0, psi1, psi2, psi3, factor};
	}

	private void modifyDerivatives(int c, double[] psi) {
		double factor = psi[4];
		if (model == ModelEnum.CONSTANT_CAP) {
			jacob[chargeNo1[c]][chargeNo1[c]] += +innerCap[c] * B4 / compConc[chargeNo1[c]] * factor;
		}

		else if (model == ModelEnum.DIFFUSE_GTL) {
			jacob[chargeNo1[c]][chargeNo1[c]] +=
					+B3 * B2 * Math.sqrt(ionicStr) * MyTools.myCosh(B3 * psi[0]) * B4 / compConc[chargeNo1[c]] * factor;
		}

		else if (model == ModelEnum.STERN_L) {
			jacob[chargeNo1[c]][chargeNo1[c]] += +innerCap[c] * B4 / compConc[chargeNo1[c]] * factor;
			jacob[chargeNo1[c]][chargeNo2[c]] += -innerCap[c] * B4 / compConc[chargeNo1[c]] * factor;
			jacob[chargeNo2[c]][chargeNo1[c]] += -innerCap[c] * B4 / compConc[chargeNo2[c]] * factor;
			jacob[chargeNo2[c]][chargeNo2[c]] += +(innerCap[c] + B3 * B2 * Math.sqrt(ionicStr) * MyTools
					.myCosh(B3 * psi[1])) * B4 / compConc[chargeNo2[c]] * factor;
		}

		else if (model == ModelEnum.TRIPLE_L) {
			jacob[chargeNo2[c]][chargeNo3[c]] = -outerCap[c] * B4 / compConc[chargeNo3[c]] * factor;
			jacob[chargeNo3[c]][chargeNo2[c]] = -outerCap[c] * B4 / compConc[chargeNo2[c]] * factor;
			jacob[chargeNo3[c]][chargeNo3[c]] =
					(outerCap[c] + B3 * B2 * Math.sqrt(ionicStr) * MyTools.myCosh(B3 * psi[2])) * B4 / compConc[chargeNo3[c]] * factor;
			jacob[chargeNo1[c]][chargeNo2[c]] += -innerCap[c] * B4 / compConc[chargeNo2[c]] * factor;
			jacob[chargeNo2[c]][chargeNo1[c]] += -innerCap[c] * B4 / compConc[chargeNo1[c]] * factor;
			jacob[chargeNo2[c]][chargeNo2[c]] += +(innerCap[c] + outerCap[c]) * B4 / compConc[chargeNo2[c]] * factor;
			jacob[chargeNo1[c]][chargeNo1[c]] += +innerCap[c] * B4 / compConc[chargeNo1[c]] * factor;
		}
	}

	private void invertMatrix(double[][] d, int n) {
		int[] l1 = new int[columns];
		int[] k1 = new int[columns];
		int i2;
		int j2;
		double p;
		double t;

		int n1 = 0;

		for (int i = 0; i < n; i++) {
			p = d[i][i];
			j2 = i;
			i2 = i;
			for (int i1 = i; i1 < n; i1++) {
				for (int j = i; j < n; j++) {
					if (Math.abs(p) < Math.abs(d[i1][j])) {
						p = d[i1][j];
						i2 = i1;
						j2 = j;
					}
				}
			}
			if (i2 != i) {
				for (int j = 0; j < n; j++) {
					t = d[i][j];
					d[i][j] = d[i2][j];
					d[i2][j] = t;
				}
			}
			n1++;
			l1[i] = i2;
			if (j2 != i) {
				for (int i1 = 0; i1 < n; i1++) {
					t = d[i1][i];
					d[i1][i] = d[i1][j2];
					d[i1][j2] = t;
				}
			}
			n1++;
			k1[i] = j2;
			for (int i1 = 0; i1 < n; i1++) {
				if (p == 0) {
					/*					MyTools.showWarning("Message from procedure InvertMatrix:\n"
					 + "P turned = 0, I`ll set it to 1E-11!");	*/
					p = 1e-11;      /*error protection*/

				}

				double a = d[i1][i] / p;
				for (int j = 0; j < n; j++) {
					if (i1 != i && j != i) {
						d[i1][j] = d[i1][j] - a * d[i][j];
					}
				}
			}
			for (int i1 = 0; i1 < n; i1++) {
				d[i1][i] = d[i1][i] / p;
			}
			for (int j = 0; j < n; j++) {
				d[i][j] = d[i][j] / (-p);
			}
			d[i][i] = 1 / p;
		}

		for (int k = n - 1; k >= 0; k--) {
			if (l1[k] != k) {
				i2 = l1[k];
				for (int i = 0; i < n; i++) {
					t = d[i][k];
					d[i][k] = d[i][i2];
					d[i][i2] = t;
				}
			}
		}
		for (int k = n - 1; k >= 0; k--) {
			if (k1[k] != k) {
				i2 = k1[k];
				for (int j = 0; j < n; j++) {
					t = d[k][j];
					d[k][j] = d[i2][j];
					d[i2][j] = t;
				}
			}
		}
	}

	/** **** Mass law equations: calc. of free conc. in mol/l according to estimation: ****** */
	private void calculation() {
		double[][] jacobRed;
		double[] psi = new double[4];
		double factor = Double.NaN;
		boolean convergent;
		int iterationCounter;
		double convergenceCriteria;
		double errorEstX;

		// array elements are initialized to 0 automatically
		jacob = new double[columns][columns];
		jacobRed = new double[columns][columns];
		double[] D0 = new double[lines];
		double[] Y0 = new double[lines];

		iterationCounter = 0;
		convergenceCriteria = IterationParametersDialog.INITIAL_CONV_CRIT;  	/*default is 1E-6*/

		do {
			if (!main.stopCmd.isEnabled()) {
				throw new CalculationStoppedException();
			}
			for (int a = 0; a < totSpec; a++) {
				speConc[a] = 0;
				for (int b = 0; b < totComp; b++) {
					speConc[a] = speConc[a] + (speciesMat[a][b] * concEstim[b]);
				}
				speConc[a] = speConc[a] + species[a].constant;
				speConc[a] = MyTools.expo(10, speConc[a]);
			}
			for (int b = 0; b < totComp; b++) {
				compConc[b] = MyTools.expo(10, concEstim[b]);
			}

			if (adsorption()) {
				for (int c = 0; c < noOfAdsorbents; c++) /*entweder severalAdsorbents oder severalSiteTypes*/ {
					psi = calculatePsiAndSigma(c);
					factor = psi[4];
				}
			}
			convergent = true;

			for (int b = 0; b < totComp; b++) {
				errorEstim[ii[b]] = -(components[ii[b]].conc);   /*error of estimation*/

				errorEstX = Math.abs(components[ii[b]].conc);

				if (adsorption() && !severalSiteTypes) {
					for (int c = 0; c < noOfAdsorbents; c++) {
						if (model == ModelEnum.STERN_L) {
							if (ii[b] == chargeNo2[c]) {
								errorEstX = Math.abs(components[chargeNo1[c]].conc)
										+ Math.abs(psi[3] * factor);
							}
						}
						if (model == ModelEnum.TRIPLE_L) {
							if (ii[b] == chargeNo3[c]) {
								errorEstim[ii[b]] = errorEstim[ii[b]] + psi[3] * factor;
							}
						}
					}		/*if adsorption*/

				}
				if (adsorption() && severalSiteTypes) {
					if (model == ModelEnum.STERN_L) {
						if (ii[b] == chargeNo2[0]) {
							errorEstX = Math.abs(components[chargeNo1[0]].conc) + Math.abs(psi[3] * factor);
						}
					}
					if (model == ModelEnum.TRIPLE_L) {
						if (ii[b] == chargeNo3[0]) {
							errorEstim[ii[b]] = errorEstim[ii[b]] + psi[3] * factor;
						}
					}
				}		/*if adsorption*/

				for (int a = 0; a < totSpec; a++) {																					/* Total conc. of one component (all species added)*/

					errorEstim[ii[b]] = errorEstim[ii[b]] + speciesMat[a][ii[b]] * speConc[a];	/* minus the totalconc. (Conc) is in the ideal case = 0 */

					errorEstX = errorEstX + Math.abs(speciesMat[a][ii[b]]) * speConc[a];					/* (if (the speciesconc., calculated from the estimations, fit best)*/

				}																					/*Goes through a maxima if (the calculated speciesconc. fit best*/

				if (b < totLimComp && errorEstX != 0) {
					if ((Math.abs(errorEstim[ii[b]]) / errorEstX) > convergenceCriteria) {
						convergent = false;
					}
				}
			}

			/*Iterative technique to improve values of concEstim such that the error errorEstim becomes smaller*/
			/*compute jacobian matrix: */
			for (int b = 0; b < totComp; b++) {
				for (int c = 0; c < totComp; c++) {
					jacob[b][c] = 0;
					for (int a = 0; a < totSpec; a++) {
						jacob[b][c] += speciesMat[a][b] * speciesMat[a][c] * speConc[a] / compConc[c];
					}
				}
			}

			if (adsorption() && !severalSiteTypes) {
				if (adsorption()) {
					if (severalSiteTypes) {
						modifyDerivatives(0, psi);
					}
					else {
						for (int c = 0; c < noOfAdsorbents; c++) {
							modifyDerivatives(c, psi);
						}
					}
				}
			}

			if (!convergent) {
				if (main.iterationParametersDialog.markIter) {
					writeNote("*");	/*schreibe '*' f¸r jeden Iterationsschritt*/

				}
				iterationCounter++;
				if (iterationCounter >= main.iterationParametersDialog.maxIterations) {
					if (main.iterationParametersDialog.autoConvCrit) {
						convergenceCriteria = convergenceCriteria * 10;
						iterationCounter = 0;
						writeNote("  !  "); /*print '  !  ' if (convergence criteria was altered automatically*/

					}
					else {
						int choice = JOptionPane.showOptionDialog(main,
								new String[]{
									"Check concentrations and constants for plausible values",
									"and make sure all components appear as species.",
									" ",
									"The criteria of "
									+ MyTools.SCI_NOTATION.format(convergenceCriteria)
									+ " can be changed to"
									+ MyTools.SCI_NOTATION.format(10 * convergenceCriteria),
									"(Data are not reliable if the criteria is > 1e-5)"},
								ChemEQL3.AN + ": Iterations do not converge!",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE, null, new String[]{"Do it", "Abort"}, null);
						if (choice == 0) {	// "Do it"
							convergenceCriteria = convergenceCriteria * 10;
							iterationCounter = 1;
						}
						else // "Abort" or dialog was closed
						{
							// assert choice == 1 || choice == -1
							writeNote("  ~~~ calculation aborted ~~~");
							throw new CalculationStoppedException();
						}
					}
				} // if (iterationCounter >= main.maxIterations)

				for (int b = 0; b < totLimComp; b++) /*Solution: components with constant free conc. remain unchanged*/ {
					for (int c = 0; c < totLimComp; c++) {
						jacobRed[b][c] = jacob[ii[b]][ii[c]];	/*Reduced Jacobian*/

					}
					Y0[b] = errorEstim[ii[b]];       			/*Reduced ErrorEstim*/

				}

				invertMatrix(jacobRed, totLimComp);

				for (int b = 0; b < totLimComp; b++) {
					D0[b] = 0;
					for (int c = 0; c < totLimComp; c++) {
						D0[b] = D0[b] + jacobRed[b][c] * Y0[c];  	 	/*matD0=matRedJacob*matRedErrorEstim*/

					}
					compConc[ii[b]] = compConc[ii[b]] - D0[b];    		/*Correction for estimation: matE = matE - matD0*/

					if (compConc[ii[b]] <= 0) {
						/*MyTools.showWarning(
						 "Message from proc. Calculation: compConc became <= 0! I`ll handle the problem...");*/
						compConc[ii[b]] = (compConc[ii[b]] + D0[b]) / 10;
					}
					if (compConc[ii[b]] == 0) {
						/*MyTools.showWarning(
						 "Message from proc. Calculation: compConc became = 0! I`ll set it to 1E-11.");*/
						compConc[ii[b]] = 1e-11;                		/*error protection*/

					}

					concEstim[ii[b]] = MyTools.myLog(compConc[ii[b]]);		 	/*New estim. for the free componentconc.*/

				}		/*for b = 1 to totLimComp*/

			}		/*if (not convergent*/

		}
		while (!convergent);
	}

	private static final MarkerEnum[] MARKERS = new MarkerEnum[]{
		MarkerEnum.FILLED_CIRCLE, MarkerEnum.CIRCLE, MarkerEnum.CROSS, MarkerEnum.PLUS,
		MarkerEnum.FILLED_SQUARE, MarkerEnum.SQUARE, MarkerEnum.TRIANGLE,
		MarkerEnum.FILLED_TRIANGLE, MarkerEnum.DIAMOND, MarkerEnum.FILLED_DIAMOND};
	private static final DecimalFormat DEF_FORMAT = new DecimalFormat("0.0E0");

	// initialize data samples and settings for axis and data
	private void initializeGraphicsSettings() {
		main.graphicsData.initializeDataList(this, SpeciesData.class);

		// init draw settings
		int[] yIndices = new int[main.drawSpecs.length];
		for (int i = 0; i < main.drawSpecs.length; i++) {
			main.graphicsData.configureVariableDrawing(SpeciesData.class, i + 1,
					species[main.drawSpecs[i]].name, ColorEnum.BLACK, null, MARKERS[i],
					LineEnum.NONE, SizeEnum.TINY);
			yIndices[i] = i + 1;
		}
		main.graphicsData.configureDataSourceDrawing(this, null,
				null, null, MarkerEnum.AS_IS, null, null);
		main.plotView.configureSelectedData(
				SpeciesData.class, yIndices, 0, new int[]{0}, false);

		// init view
		main.plotView.configureView(
				null, 12, null, Color.white, true, false, true, false, false);
		final int ba = PlotView.BOTTOM_AXIS;
		final int la = PlotView.LEFT_AXIS;
		String act = main.activityOutput ? "Activity       " : "";
		main.plotView.configureAxisTitle(la, act + "concentration", "mol/l");
		main.plotView.configureAxisTicks(ba, DEF_FORMAT, 0, 0, 5, 2);
		main.plotView.configureAxisTicks(la, DEF_FORMAT, 0, 0, 5, 2);
		main.plotView.configureAxisAdaptingBounds(ba, true);
		main.plotView.configureAxisAdaptingBounds(la, true);
		main.plotView.configureAxisLinearScale(ba, 0, 0, false);
		if (main.isLogNumFormat()) {
			main.plotView.configureAxisLogarithmicScale(la, 10, 0, false);
		}
		else {
			main.plotView.configureAxisLinearScale(la, 0, 0, false);
		}
	}

// -----------------------------------------------------------------------------
// --- model implementations ---------------------------------------------------
// -----------------------------------------------------------------------------

// --- TableModel for components in file info window ---------------------------
	class ComponentsTableModel extends AbstractTableModel
	{
		public int getColumnCount() {
			return totComp;
		}

		public int getRowCount() {
			return 3;
		}

		public Object getValueAt(int row, int col) {
			switch (row) {
			case 0:
				return components[col].name;
			case 1:
				return components[col].mode;
			case 2:
				return new Double(multiConcMatrix[0][col]);
			default:
				return null;
			}
		}

		public void setValueAt(Object val, int row, int col) {
			switch (row) {
			case 0:	// change component name
				if (components[col].name.equals((String)val)) {
					return;
				}
				components[col].name = (String)val;
				break;
			case 1:	// change component mode
				Mode m = (Mode)val;
				if (components[col].mode == m) {
					return;
				}
				components[col].mode = m;
				defaultsDatInput();	/*organisiert Reihenfolge der Komponenten*/

				adjustConcEstim();	/*neu z‰hlen von noOfSolidPhases, noOfCheckPrecip und totLimComp und einsetzen der Konz.*/

				if (isHorHplusAndFree()) {
					main.pHfix = true;
					if (multiConc > 1) {
						main.pHrange = true;
					}
					main.pHconstCmd.setEnabled(true);
					main.pHrangeCmd.setEnabled(true);
					if (multiConcMatrix[0][totComp - 1] == 0) {
						multiConcMatrix[0][totComp - 1] = 1e-7;
					}
					main.pHrangeStart = -MyTools.myLog(multiConcMatrix[0][totComp - 1]);
					main.pHrangeEnd = main.pHrangeStart;
					main.pHrangeStep = main.pHrangeEnd;
				}
				if ((isLastCompName("H") || isLastCompName("H+"))
						&& components[totComp - 1].mode == Mode.TOTAL) {
					main.pHfix = false;
					main.pHrange = false;
					main.pHconstCmd.setEnabled(false);
					main.pHrangeCmd.setEnabled(false);
					multiConcMatrix[0][totComp - 1] = 0;
				}
				for (int a = 0; a < totComp; a++) {
					if (components[a].mode == Mode.SOLID_PHASE) {
						multiConcMatrix[0][a] = 1;
					}
				}
				break;
			case 2:	// change component concentration
				if (val == null) {
					return;		// number format error
				}
				double v = ((Double)val).doubleValue();
				if (multiConcMatrix[0][col] == v) {
					return;
				}
				if (isHorHplusAndFree() && col == totComp - 1 && v == 0) {
					MyTools.showError("Free concentration of H+ cannot be zero!");
					return;
				}

				multiConcMatrix[0][col] = v;

				for (int b = 0; b < totComp; b++) /*neue Konzentrationen einsetzen*/ {
					components[b].conc = multiConcMatrix[0][b];
				}
				adjustConcEstim();
				if (isHorHplusAndFree()) {
					main.pHfix = true;
					if (multiConc > 1) {
						main.pHrange = true;
					}
					main.pHrangeStart = -concEstim[totComp - 1];
					main.pHrangeEnd = main.pHrangeStart;
					main.pHrangeStep = main.pHrangeEnd;
				}
				break;
			}
			main.drawFileInfoWindow(true);
		}
	}


	// --- TableModel for adsorbant parameters in file info window --------------
	class AdsorptionTableModel extends AbstractTableModel
	{
		public int getColumnCount() {
			return noOfAdsorbents + 1;
		}

		public int getRowCount() {
			if (!adsorption()) {
				return 1;
			}

			int result = 5;
			if (model != ModelEnum.DIFFUSE_GTL) {
				result++;
			}
			if (model == ModelEnum.TRIPLE_L) {
				result++;
			}
			return result;
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) {
				switch (row) {
				case 0:
					return "double layer model:";
				case 1:
					return "surface area [m2/g]:";
				case 2:
					return "surface sites [mol/m2]:";
				case 3:
					return "particle conc. [g/l]:";
				case 4:
					return "ionic strength [mol/l]:";
				case 5:
					return "inner capacitance [F/m2]:";
				case 6:
					return "outer capacitance [F/m2]:";
				default:
					return null;
				}
			}
			else {
				switch (row) {
				case 0:
					return col == 1 ? model.getName() : "";
				case 1:
					return new Double(area[col - 1]);
				case 2:
					return new Double(surfaceSites[col - 1]);
				case 3:
					return new Double(solidConc[col - 1]);
				case 4:
					return col == 1 ? (Object)new Double(ionicStr) : "";
				case 5:
					return new Double(innerCap[col - 1]);
				case 6:
					return new Double(outerCap[col - 1]);
				default:
					return null;
				}
			}
		}

		public void setValueAt(Object val, int row, int col) {
			double oldV = 0;
			// assert: col > 0 && row > 0 && row <= 6
			double v = ((Double)val).doubleValue();
			switch (row) {
			case 1:
				oldV = area[col - 1];
				area[col - 1] = v;
				break;
			case 2:
				oldV = surfaceSites[col - 1];
				surfaceSites[col - 1] = v;
				break;
			case 3:
				oldV = solidConc[col - 1];
				solidConc[col - 1] = v;
				break;
			case 4:
				oldV = ionicStr;
				ionicStr = v;
				break;	// assert: col==1
			case 5:
				oldV = innerCap[col - 1];
				innerCap[col - 1] = v;
				break;
			case 6:
				oldV = outerCap[col - 1];
				outerCap[col - 1] = v;
				break;
			}
			if (v != oldV) {
				main.drawFileInfoWindow(true);
			}
		}
	}

	// --- TableModel for speciesMat in file info window ------------------------

	class SpeciesTableModel extends AbstractTableModel
	{
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return totSpec;
		}

		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0:
				return Integer.toString(row + 1) + ".";
			case 1:
				return species[row].name;
			case 2:
				return new Double(species[row].constant);
			case 3:
				return species[row].source;
			default:
				return null;
			}
		}

		public void setValueAt(Object val, int row, int col) {
			if (col == 2) {
				if (val == null) {
					return;		// number format error
				}
				double v = ((Double)val).doubleValue();
				if (species[row].constant == v) {
					return;
				}
				species[row].constant = v;
				main.drawFileInfoWindow(true);
			}
		}
	}


	// --- TableModel for species results in output window ----------------------
	class SpeciesOutputTableModel extends AbstractTableModel
	{
		public int getColumnCount() {
			return calculatedWithActivity ? 6 : 5;
		}

		public int getRowCount() {
			return totSpec;
		}

		public String getColumnName(int col) {
			int c = 0;
			if (col == c++) {
				return "Species";
			}
			if (col == c++) {
				return "Stoich. Matrix";
			}
			if (col == c++) {
				return "Log K";
			}
			if (col == c++) {
				return "Conc. [mol/l]";
			}
			if (calculatedWithActivity) {
				if (col == c++) {
					return "Activity";
				}
			}
			// assert col == c;
			return "Log conc.";
		}

		public Object getValueAt(int row, int col) {
			int c = 0;
			if (col == c++) {
				return species[row].name;
			}
			if (col == c++) {
				StringBuffer line = new StringBuffer();
				for (int b = 0; b < totComp; b++) {
					line.append(' ');
					double v = speciesMat[row][b];
					if (v >= 0) {
						line.append(' ');
					}
					if (printInteger) {
						line.append(Integer.toString((int)v));
					}
					else {
						line.append(MyTools.EXACT_2_DIGITS.format(v));
					}
				}
				return line;
			}
			if (col == c++) {
				return new Double(species[row].constant);
			}
			if (col == c++) {
				return new Double(speConc[row]);
			}
			if (calculatedWithActivity) // cannot use ChemEQL3.isCalcActivCoeff(), since meanwhile
			{
				if (col == c++) // ChemEQL3.restoreProc has resetted calcActivCoeff to no.
				{
					return new Double(MyTools.expo(10, logfSpec[row]) * speConc[row]);
				}
			}
			// assert col == c;
			return new Double(
					speConc[row] <= 0 ? Double.NaN : MyTools.myLog(speConc[row]));
			// return NaN (not a number) if MyTools.myLog is undefined to prevent
			// repeatod pop up of error panel driven by the user event
			// dispatch thread
		}
	}

	// --- TableModel for components results in output window -------------------

	class ComponentsOutputTableModel extends AbstractTableModel
	{
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return totComp;
		}

		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Components";
			case 1:
				return "Mode";
			case 2:
				return "Initial Conc.";
			case 3:
				return "In or out of system";
			default:
				return null;
			}
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return components[row].name;
			}

			Mode mode = components[row].mode;
			if (col == 1) {
				return mode;
			}

			Object col2;
			Object col3;
			if (mode == Mode.SOLID_PHASE) {
				col2 = "unlimited";
				col3 = new Double(errorEstim[row]);
			}
			else if (mode == Mode.CHECKPRECIP && amountPrecipitated[row] != 0) {
				col2 = "unlimited";
				col3 = new Double(-amountPrecipitated[row]);
			}
			else if (mode == Mode.FREE) {
				col2 = new Double(MyTools.expo(10, concEstim[row]));
				col3 = new Double(errorEstim[row]);
			}
			else {
				if (components[row].name.equals("H+")) {
					col2 = "--->";
				}
				else if (components[row].conc != 0) {
					col2 = new Double(components[row].conc);
				}
				else {
					col2 = new Double(0.0);
				}
				col3 = "----";
			}

			if (col == 2) {
				return col2;
			}
			// assert col == 3;
			return col3;
		}
	}

	// --- TableModel for pXpY results in output window -------------------------

	class PxPyOutputTableModel extends AbstractTableModel
	{
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			if (main.graphicsData == null) {
				return 0;
			}
			ArrayList list =
					main.graphicsData.getDataList(Matrix.this, PxPyData.class);
			return list == null ? 0 : list.size();
		}

		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "-log " + components[xComp].name;
			case 1:
				return "-log " + components[yComp].name;
			default:
				return "";
			}
		}

		public Object getValueAt(int row, int col) {
			PxPyData data = (PxPyData)main.graphicsData.getDataList(
					Matrix.this, PxPyData.class).get(row);
			switch (col) {
			case 0:
				return new Double(data.pX);
			case 1:
				return new Double(data.pY);
			case 2:
				return data.pXpYborderOldSpecies;
			case 3:
				return data.pXpYborderNewSpecies;
			default:
				return null;
			}
		}
	}


// --- ComboBoxModel for speciesMat in DeleteSpeciesDialog ---------------------
	class SpeciesNamesModel implements ComboBoxModel
	{
		private int deleteSpecNo = 0;

		public void addListDataListener(ListDataListener l) {
		}

		public void removeListDataListener(ListDataListener l) {
		}

		public Object getElementAt(int i) {
			return species[i];
		}

		public int getSize() {
			return totSpec;
		}

		public Object getSelectedItem() {
			return species[deleteSpecNo];
		}

		public void setSelectedItem(Object anItem) {
			for (int i = 0; i < totSpec; i++) {
				if (anItem == species[i]) {
					deleteSpecNo = i;
					return;
				}
			}
		}

		void deleteSelectedSpecies() {
			for (int a = deleteSpecNo; a < totSpec - 1; a++) /*von da an abw‰rts alle Species um 1 zur¸ckverschieben*/ {
				species[a] = species[a + 1];
				for (int b = 0; b < totComp; b++) {
					speciesMat[a][b] = speciesMat[a + 1][b];	/*st‰ch. Koeffizienten verschieben*/

				}
			}
			totSpec--;
			deleteSpecNo = 0;
		}
	}


// --- ComboBoxModel for components ComponentRangeDialog and Kinetics ----------
	class ComponentsCBModel implements ComboBoxModel
	{
		protected Object selectedComponent = null;

		public void addListDataListener(ListDataListener l) {
		}

		public void removeListDataListener(ListDataListener l) {
		}

		public Object getElementAt(int i) {
			return components[i];
		}

		public int getSize() {
			return totComp;
		}

		public Object getSelectedItem() {
			return selectedComponent;
		}

		public void setSelectedItem(Object anItem) {
			selectedComponent = anItem;
		}
	}

// --- ComboBoxModel for components in ChangeSolidPhaseDialog, -----------------

	class SpecialComponentsCBModel extends ComponentsCBModel
	{
		public void setSelectedItem(Object anItem) {
			if (!((Component)anItem).isModeSolidPhaseOrCheckPrecip()) {
				selectedComponent = anItem;
			}
		}
	}
}
