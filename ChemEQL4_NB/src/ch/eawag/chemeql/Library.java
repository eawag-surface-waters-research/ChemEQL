package ch.eawag.chemeql;

import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.Serializable;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.event.ListDataListener;


class Library implements Serializable
{
	private static final char TAB = Tokenizer.TAB;
	private static final char CR = Tokenizer.CR;

	static final int libColumns = 150;
	private static final int libLines = 2500;
	static final String EQUALS_STRING = "   <==>   ";
		// accessed by NewSpeciesDialog.doProceed()

	int libTotComp;
	int libTotSpec;
	String[] libCompNames = new String[libColumns];
	Species[] libSpecies = new Species[libLines];
	int[][] libSpecMat = new int[libLines][libColumns];

	private transient ComponentsListModel componentsListModel;
	private transient SpeciesListModel speciesListModel;
	private transient ComboBoxModel speciesCBModel;
	private transient ComboBoxModel componentsCBModel;
//	private transient ChemEqlGuiController main;
	private transient boolean isRegularLib;	// false: library is solid phases library

	private static String defaultBinFileName(final boolean isRegularLib)
	{
		return isRegularLib ? "CQLJ.RegularLib" : "CQLJ.SolidsLib";
	}

	private static String defaultTextFileName(final boolean isRegularLib)
	{
		return isRegularLib ? "CQL.Library" : "CQL.spLibrary";
	}

	static File binLibFile(final boolean isRegularLib)
	{
		return new File(
			System.getProperty("user.home"), defaultBinFileName(isRegularLib));
	}

	// read binary library file from the users home directory
	static Library readBinLibrary(final boolean isRegularLib)
	{
		Library result = null;
		try
		{
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(binLibFile(isRegularLib))));
			result = (Library)in.readObject();
			result.initializeLibrary(isRegularLib);
			in.close();
		}
		catch (Exception ex)
		{
			MyTools.showException(ex);
			result = null;
		}
		return result;
	}

	// write library as binary file to default location
	String writeBinary()
	{
		String result = null;
		try
		{
			File f = binLibFile(isRegularLib);
			ObjectOutputStream out = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(f)));
			out.writeObject(this);
			out.close();
			result = f.getCanonicalPath();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		return result;
	}

	Library(boolean regLibrary)
	{
		initializeLibrary(regLibrary);
	}

	private void initializeLibrary(boolean regLibrary)
	{
		isRegularLib = regLibrary;
	}

	String libraryType()
	{
		return isRegularLib ? "Regular" : "Solid Phases";
	}

	String defaultTextFileName()
	{
		return defaultTextFileName(isRegularLib);
	}

	AbstractListModel getComponentsListModel()
	{
		if (componentsListModel == null)
			componentsListModel = new ComponentsListModel();
		return componentsListModel;
	}

	AbstractListModel getSpeciesListModel()
	{
		if (speciesListModel == null)
			speciesListModel = new SpeciesListModel();
		return speciesListModel;
	}

	ComboBoxModel getSpeciesCBModel()
	{
		if (speciesCBModel == null)
			speciesCBModel = new SpeciesCBModel();
		return speciesCBModel;
	}

	ComboBoxModel getComponentsCBModel()
	{
		if (componentsCBModel == null)
			componentsCBModel = new ComponentsCBModel();
		return componentsCBModel;
	}


	/* reads library from an EXCEL-text file */
	void importLib(final InputStream inStream, final String msg)
		throws IOException
	{
		String s;
		ProgressMonitorInputStream progrMon =
			new ProgressMonitorInputStream(null, msg,inStream);	//TEST
		progrMon.getProgressMonitor().setMillisToDecideToPopup(0);
		progrMon.getProgressMonitor().setMillisToPopup(0);
	 	InputStreamReader reader = new InputStreamReader(progrMon);
		Tokenizer myRead = new Tokenizer(reader);
		myRead.nextItem();		/*skip first item*/
		libTotComp = 0; /*we are in the second field now: component names are read and counted*/
		do
		{
			s = myRead.nextItem();
			if (!myRead.isItemEmpty() && myRead.delimiter() != CR)
			/*wenn in dem Feld ein Name steht*/
			{
				libCompNames[libTotComp++] = s;
				if (libTotComp == libColumns)
				{
					throw new DataFormatException(
					"Error importing library: Library has more columns than allocated.");
				}
			}
		}
		while (myRead.delimiter() != CR && myRead.notEOF());

		libTotSpec = 0;	/*read specNames, coefficients, logK, and libSource*/
		while (myRead.notEOF() && libTotSpec <= libLines)
		{
			s = myRead.nextItem();	/*check if (this line is a species or empty*/
			if (!myRead.isItemEmpty())	/*in this case it is a species and not an empty line*/
			{
				String name = s;
				double constant = 0;
				String source = "";

				for (int b=0; b < libTotComp; b++)	/*liest die Koeffizienten in einer Zeile*/
				{
					s = myRead.nextItem();
					if (myRead.isItemEmpty())	/*falls das Feld leer ist, ist der Koeffizient 0 gemeint*/
						libSpecMat[libTotSpec][b] = 0;
					else
					{
						try {libSpecMat[libTotSpec][b] = myRead.itemToInteger();}
						catch (NumberFormatException ex)
						{
							throw new DataFormatException(
							"Error importing library: Coefficient for species '"
							+ name + "' and component '"
							+ libCompNames[b] + "' is not an integer!");
						}
					}
				}

				s = myRead.nextItem();
				try {constant = myRead.itemToDouble();}	/*liest logK*/
				catch (NumberFormatException ex)
				{
					throw new DataFormatException(
						"Error importing library: Log K for species '"
						+ name + "' is not a real number!");
				}

				if (myRead.delimiter() != CR)
				{	/* liest Temp und Ionenstärke */
					s = myRead.nextItem();
					source = s + TAB;
				}

				if (myRead.delimiter() != CR)
				{	/* liest Literaturangabe */
					s = myRead.nextItem();
					source = source + s;
				}

				libSpecies[libTotSpec] = new Species(name,constant,source);
				libTotSpec++;
				myRead.skipToEOL(); /*liest bis und mit 'CR' -> Zeilenende*/
			} /* if not empty line */
		} /* end of while: myRead.notEOF() && libTotSpec <= libLines */
		reader.close();
	}

	/* exports library as tab delimited text file */
	void exportTo(final File outputFile) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		writer.write(isRegularLib ? "CQL-Library" : "CQL-spLibrary");
												/*im ersten Feld das Erkennungszeichen*/
		writer.write(TAB);

		for (int b=0; b < libTotComp; b++)
		{
			writer.write(libCompNames[b]);	/*Namen der Komponenten schreiben*/
			writer.write(TAB);
		}
		writer.write(CR);			// writer.newLine() ??

		for (int a=0; a < libTotSpec; a++)
		{
			writer.write(libSpecies[a].name);
			writer.write(TAB);
			for (int b=0; b < libTotComp; b++)
			{
				writer.write(Integer.toString(libSpecMat[a][b]));	/*Coeffizienten*/
				writer.write(TAB);
			}
			writer.write(MyTools.EXACT_4_DIGITS.format(libSpecies[a].constant));	/*Konstanten*/
			writer.write(TAB);
			writer.write(libSpecies[a].source);	/*Lit.angabe, CR ist schon drin beim Powerbook, nicht beim Quadra...*/
			if (libSpecies[a].source.charAt(libSpecies[a].source.length()-1) != CR)
				writer.write(CR);				/*neue Zeile. Ein CR einsetzen funktioniert einfach nicht!!!*/
		}
		writer.close();
	}


	boolean nameAlreadyInUse(String specName)
	{
		// should of course use binary search here
		for (int a=0; a < libTotSpec; a++)
			if (libSpecies[a].name.equals(specName))
				return true;
		return false;
	}

	private String removeLeadingNonAlphaNum(String str)
	{
		int i = 0;
		while (!Character.isLetterOrDigit(str.charAt(i)))
			i++;

		return i == 0 ? str : str.substring(i);
	}

	void changedComponentName(String changedName, int i)
	{
		libCompNames[i] = changedName;
		componentsListModel.changed(i);
	}

	int insertComponent(String newComp, final int insertNo)
	{
		for (int b=libTotComp; b > insertNo; b--)
		{
			libCompNames[b] = libCompNames[b-1];		// alle oberen Comps um 1 verschieben
			for (int a=0; a < libTotSpec; a++)
				libSpecMat[a][b] = libSpecMat[a][b-1];	// alle oberen Koeff. um 1 verschieben
		}
		libCompNames[insertNo] = newComp;				// neue Comp einfügen
		for (int a=0; a < libTotSpec; a++)
			libSpecMat[a][insertNo] = 0;					// neue Koeff. einfügen

		if (isRegularLib)
		{
			int[] stoichCoeffs = new int[libColumns];	// all elements zero
			stoichCoeffs[insertNo] = 1;
			insertSpeciesPrim(new Species(newComp,0,String.valueOf(TAB)),stoichCoeffs);
		}

		libTotComp++;
		componentsListModel.added(insertNo);

		return insertNo;
	}


	void deleteComponentAtIndex(final int deleteCompNo)
	{
		/*zuerst die Spezies löschen: */
		for (int a=0; a < libTotSpec; a++)
		{
			if (libSpecMat[a][deleteCompNo] != 0)
				deleteSpeciesAtIndexPrim(a);
		}
		/*Comps um 1 zurückverschieben*/
		for (int b=deleteCompNo; b < libTotComp-1; b++)
		{
			libCompNames[b] = libCompNames[b+1];

			/*stoch. Koeffizienten in allen Specs löschen*/
			for (int a=0; a < libTotSpec; a++)
				libSpecMat[a][b] = libSpecMat[a][b+1];
		}
		libTotComp--;
		componentsListModel.removed(deleteCompNo);
	}

	void changedSpecies(int i)
	{
		speciesListModel.changed(i);
	}

	// called from EditSpeciesDialog
	int insertSpecies(Species newSpecies, int[] newStoichCoeffs)
	{
		int insertNo = insertSpeciesPrim(newSpecies,newStoichCoeffs);
		speciesListModel.added(insertNo);
		return insertNo;
	}

	/*Die neue Species wird alphabetisch eingeordnet*/
	private int insertSpeciesPrim(Species newSpecies, int[] newStoichCoeffs)
	{
		// Suche muss von vorne beginnen, da die Spezies nur im vorderen
		// Teil der Library alphabetisch sortiert sind. Außerdem werden führende
		// nicht-alphanumerische Zeichen, z.B. '(' ignoriert.
		// Die Suche nach der Einfügestelle sollte eigentlich binär erfolgen!
		int insertNo = 0;
		String newSpecName = removeLeadingNonAlphaNum(newSpecies.name);
		while (insertNo < libTotSpec && newSpecName.compareToIgnoreCase(
				removeLeadingNonAlphaNum(libSpecies[insertNo].name)) > 0)
			insertNo++;
		for (int a=libTotSpec; a > insertNo; a--)
		{
			libSpecies[a] = libSpecies[a-1];
			libSpecMat[a] = libSpecMat[a-1];
		}
		libSpecies[insertNo] = newSpecies;
		libSpecMat[insertNo] = newStoichCoeffs;
		libTotSpec++;
		return insertNo;
	}

	void deleteSpeciesAtIndex(final int index)
	{
		deleteSpeciesAtIndexPrim(index);
		speciesListModel.removed(index);
	}

	private void deleteSpeciesAtIndexPrim(final int index)
	{
		for (int a=index; a < libTotSpec-1; a++)	/*von da an abwärts alle Specs um 1 zurückverschieben*/
		{
			libSpecies[a] = libSpecies[a+1];	// Namen, logK und Lit.angabe verschieben*/
			for (int b=0; b < libTotComp; b++)
				libSpecMat[a][b] = libSpecMat[a+1][b];	/*stoch. Koeffizienten verschieben*/
		}
		libTotSpec--;
	}

	String equationFor(int speciesNo)
	{
		return equationFor(libSpecMat[speciesNo],libSpecies[speciesNo].name);
	}

	String equationFor(int[] stoichCoeffs, String specName)
	{
		StringBuilder educts = new StringBuilder(30);
		StringBuilder products = new StringBuilder(30);

		for (int a=0; a < stoichCoeffs.length; a++)
		{
			if (stoichCoeffs[a] > 0)					/* Edukte */
			{
				if (educts.length() > 0)
					educts.append("  +  ");
				if (stoichCoeffs[a] > 1)
					educts.append(stoichCoeffs[a]);
				educts.append(libCompNames[a]);
			}
			if (stoichCoeffs[a] < 0)					/*Produkte*/
			{
				if (stoichCoeffs[a] < -1)
					products.append(Math.abs(stoichCoeffs[a]));
				products.append(libCompNames[a]);
				products.append("  +  ");
			}
		}
		return educts.toString() + EQUALS_STRING + products.toString()
			+ specName;
	}


// --- ListModel for EditComponentsDialog --------------------------------

	private class ComponentsListModel extends AbstractListModel
	{
		public Object getElementAt(int index)
		{
			return libCompNames[index];
		}

		public int getSize()
		{
			return libTotComp;
		}

		private void removed(int index)
		{
			fireIntervalRemoved(this,index,index);
		}

		private void added(int index)
		{
			fireIntervalAdded(this,index,index);
		}

		private void changed(int index)
		{
			fireContentsChanged(this,index,index);
		}
	}

// --- ListModel for EditSpeciesDialog --------------------------------

	private class SpeciesListModel extends AbstractListModel
	{
		public Object getElementAt(int index)
		{
			return libSpecies[index];
		}

		public int getSize()
		{
			return libTotSpec;
		}

		private void removed(int index)
		{
			fireIntervalRemoved(this,index,index);
		}

		private void added(int index)
		{
			fireIntervalAdded(this,index,index);
		}

		private void changed(int index)
		{
			fireContentsChanged(this,index,index);
		}
	}

// --- ComboBoxModel for ChangeSolidPhaseDialog --------------------------------

	private class SpeciesCBModel implements ComboBoxModel
	{
		private Object selectedSpecies = null;

		public void addListDataListener(ListDataListener l) {}
		public void removeListDataListener(ListDataListener l) {}

		public Object getElementAt(int i)
		{
			return libSpecies[i];
		}

		public int getSize()
		{
			return libTotSpec;
		}

		public Object getSelectedItem()
		{
			return selectedSpecies;
		}

		public void setSelectedItem(Object anItem)
		{
			selectedSpecies = anItem;
		}
	}


	// --- ComboBoxModel for New Species Dialog ---------------------------------

	private class ComponentsCBModel implements ComboBoxModel
	{
		private Object selectedComponent = null;

		public void addListDataListener(ListDataListener l) {}
		public void removeListDataListener(ListDataListener l) {}

		public Object getElementAt(int i)
		{
			return libCompNames[i];
		}

		public int getSize()
		{
			return libTotComp;
		}

		public Object getSelectedItem()
		{
			return selectedComponent;
		}

		public void setSelectedItem(Object anItem)
		{
			selectedComponent = anItem;
		}
	}
}