package ch.eawag.chemeql;

import de.vseit.showit.DataCollection;
import de.vseit.showit.PlotView;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;


public class ChemEqlGuiController
{
	@FXML
	private TitledPane pHOptions;
	@FXML
	private TitledPane pHRangeOptions;
	@FXML
	private TitledPane componentRangeOptions;
	@FXML
	private TitledPane adsorptionRange;
	@FXML
	private TitledPane logKRangeOptions;
	@FXML
	private TitledPane kineticsOptions;
	@FXML
	private TitledPane activityOptions;

//	@FXML
//	ToggleGroup activityToggleGroup;
	@FXML
	private RadioButton debyeHückelRB;
	@FXML
	private RadioButton güntelbergRB;
	@FXML
	private RadioButton daviesRB;

	@FXML
	private MenuBar menuBar;
	@FXML
	private MenuItem openMI;
	@FXML
	private MenuItem saveDataMI;
	@FXML
	private SeparatorMenuItem quitSeparator;
	@FXML
	private MenuItem quitMI;

	@FXML
	private ToggleGroup numFormat;
	@FXML
	private Toggle numFormatLinRMI;
	@FXML
	private Toggle numFormatLogRMI;
	@FXML
	private MenuItem pXpYMI;
	@FXML
	private MenuItem saveMatrixMI;
	@FXML
	MenuItem stopMI;
	@FXML
	MenuItem pHrangeMI;
	@FXML
	private MenuItem solidPhasesMI;
	@FXML
	MenuItem pHconstMI;
	@FXML
	private MenuItem fileInfoMI;
	@FXML
	MenuItem graphMI;
	@FXML
	private MenuItem restoreMI;
	@FXML
	private Menu runMenu;
	@FXML
	MenuItem logKrangeMI;
	@FXML
	private MenuItem kinetikMI;
	@FXML
	private Menu matrixMenu;
	@FXML
	private Menu modeMenu;
	@FXML
	private MenuItem deleteSpeciesMI;
	@FXML
	MenuItem adsRangeMI;
	@FXML
	private MenuItem printMI;
	@FXML
	MenuItem replaceHbyOHMI;
	@FXML
	private MenuItem goMI;
	@FXML
	MenuItem compRangeMI;
	@FXML
	Menu formatMenu;

	private final FileChooser fileChooser = new FileChooser();
	private static final ExtensionFilter CQL_FILTER = new ExtensionFilter(ChemEql.APP_TITLE + " Matrices (*.cql, *.txt)",
			"*.cql", "*.txt");
	private static final ExtensionFilter XLS_FILTER = new ExtensionFilter("Output Data, tab delimited (*.xls)", "*.xls");

	@FXML
	public void initialize()
	{
		fileChooser.getExtensionFilters().addAll(CQL_FILTER, XLS_FILTER);
		menuBar.setUseSystemMenuBar(ChemEql.IS_MAC_OSX);
		quitSeparator.setVisible(!ChemEql.IS_MAC_OSX);
		quitMI.setVisible(!ChemEql.IS_MAC_OSX);

		pHOptions.expandedProperty().addListener(new OptionTitleChanger("pH", "No pH setting"));
		pHOptions.expandedProperty().setValue(false);
		pHRangeOptions.expandedProperty().addListener(new OptionTitleChanger("pH range", "No pH range"));
		pHRangeOptions.expandedProperty().setValue(false);
		componentRangeOptions.expandedProperty().addListener(new OptionTitleChanger("Component range",
				"No Component range"));
		componentRangeOptions.expandedProperty().setValue(false);
		adsorptionRange.expandedProperty().addListener(new OptionTitleChanger("Adsorption range", "No Adsorption range"));
		adsorptionRange.expandedProperty().setValue(false);
		logKRangeOptions.expandedProperty().addListener(new OptionTitleChanger("log K range", "No log K range"));
		logKRangeOptions.expandedProperty().setValue(false);
		kineticsOptions.expandedProperty().addListener(new OptionTitleChanger("Kinetics", "No Kinetics"));
		kineticsOptions.expandedProperty().setValue(false);
		activityOptions.expandedProperty().addListener(new OptionTitleChanger("Activity", "No Activity"));
		activityOptions.expandedProperty().setValue(false);

//		calcActivCoeff.selectedToggleProperty().addListener(
//				changeEvent -> ActivityInputDialog.getInstance().setVisible(true));
		matrix = new Matrix(this);		// create global data structures
		setBooleans();
		doKinetik = false;
		createAllWindows();	// must create internal frames *after* desktop has
		// been made visible for having desktop bounds be set

		// initialize glass pane for blocking events in selectAndReadLibrary
//		myGlass = getGlassPane();
//		blockingGlass = new JPanel();
//		blockingGlass.setOpaque(false);
//		MouseInputAdapter adapter = new MouseInputAdapter()
//		{
//		};
//		blockingGlass.addMouseListener(adapter);
//		blockingGlass.addMouseMotionListener(adapter);
		// import standard libraries from jar file if binary libs don't exist yet
		try {
			String t = " library sucessfully imported and stored as binary file at ";
			if (!Library.binLibFile(true).exists()) {
//				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				regLibrary = new Library(true);
				regLibrary.importLib(
						ChemEqlGuiController.class.getResourceAsStream("resources/CQL.Library"),
						"Importing standard regular library");
				String path = regLibrary.writeBinary();
				System.out.println("Regular" + t + path);
			}
			if (!Library.binLibFile(false).exists()) {
//				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				spLibrary = new Library(false);
				spLibrary.importLib(
						ChemEqlGuiController.class.getResourceAsStream("resources/CQL.spLibrary"),
						"Importing standard solid phases library");
				String path = spLibrary.writeBinary();
				System.out.println("Solid phases" + t + path);
			}
		} catch (IOException ex) {
			regLibrary = spLibrary = null;
			MyTools.showException(ex);
		} finally {
//			setCursor(Cursor.getDefaultCursor());
		}
	}

	@FXML
	void openCmd(ActionEvent event)
	{
		/* eine Matrix einlesen */
		flushMatrixProc();
		try {
			fileChooser.setTitle("Open matrix");
			fileChooser.setSelectedExtensionFilter(CQL_FILTER);
			File file = fileChooser.showOpenDialog(ChemEql.mainStage());
			if (file != null) {
				// user has not canceled file dialog: go read my matrix!
//				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				matrix.datInput(file);
				matrixView.setMatrix(matrix);
				setFileInfoWindowTitle(file);
				saveMatrixMI.setDisable(true);
//				matrixLoadedUpdate();
				matrixIsLoaded = true;
			}
		} catch (IOException ex) {
			MyTools.showException(ex);
		} finally {
//			setCursor(Cursor.getDefaultCursor());
//			fileChooser.getExtensionFilters().remove(CQL_FILTER);
		}
	}

	@FXML
	void readLibCmd(ActionEvent event)
	{
		/* eine binäre Bibliothek aus dem Preferences-Folder einlesen */
		flushMatrixProc();
		if (regLibrary == null) /* falls binäre Library noch nie geöffnet...*/ {
			regLibrary = Library.readBinLibrary(true);
		}
		if (regLibrary == null) {
			return;// Error while reading library: Exit!
		}
		SelectComponentsDialog.getInstance(null).show(regLibrary);

		if (matrixIsLoaded) {
			saveMatrixMI.setDisable(false);
			matrixLoadedUpdate();
		}
	}

	@FXML
	void saveMatrixCmd(ActionEvent event)
	{
		try {
			fileChooser.setTitle("Save current matrix");
			fileChooser.setSelectedExtensionFilter(CQL_FILTER);
			fileChooser.setInitialFileName("z.txt");
			File file = fileChooser.showSaveDialog(ChemEql.mainStage());
			if (file == null) {
				return;		// Exit!
			}
			// user has not canceled file dialog: save if file is new or shall be replaced
			if (!file.getName().endsWith(".txt") && !file.getName().endsWith(".cql")) {
				file = new File(file.getParentFile(), file.getName() + ".txt");
			}
			if (!file.exists() || JOptionPane.showConfirmDialog(null,
					"Replace existing matrix '" + file.getName() + "'?",
					"Confirm", JOptionPane.YES_NO_OPTION)
					== JOptionPane.YES_OPTION) {
//				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				matrix.save(file);
				setFileInfoWindowTitle(file);
			}
		} catch (IOException ex) {
			MyTools.showException(ex);
		} finally {
//			setCursor(Cursor.getDefaultCursor());
//			fileChooser.getExtensionFilters().remove(CQL_FILTER);
		}
	}

	@FXML
	void saveDataCmd(ActionEvent event)
	{
		try {
			fileChooser.setTitle("Save current data");
			fileChooser.setSelectedExtensionFilter(XLS_FILTER);
			fileChooser.setInitialFileName("z.xls");
			File file = fileChooser.showSaveDialog(ChemEql.mainStage());
			if (file == null) {
				return;		// Exit!
			}
			// user has not canceled file dialog: save if file is new or shall be replaced
			if (!file.getName().endsWith(".xls")) {
				file = new File(file.getParentFile(), file.getName() + ".xls");
			}
			if (!file.exists() || JOptionPane.showConfirmDialog(null,
					"Replace existing data file '" + file.getName() + "'?",
					"Confirm", JOptionPane.YES_NO_OPTION)
					== JOptionPane.YES_OPTION) {
//				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				outputWindow.saveData(file);
			}
		} catch (IOException ex) {
			MyTools.showException(ex);
		} finally {
//			setCursor(Cursor.getDefaultCursor());
//			fileChooser.getExtensionFilters().remove(XLS_FILTER);
		}
	}

	@FXML
	void importLibCmd(ActionEvent event)
	{
		/* eine neue Bibliothek (Text-File) einlesen */
		flushMatrixProc();
		selectAndReadLibrary();
	}

	@FXML
	void exportLibCmd(ActionEvent event)
	{
		/* eine Bibliothek auslesen */
		flushMatrixProc();
		exportLibrary();
	}

	@FXML
	void printCmd(ActionEvent event)
	{
//		JInternalFrame selected = desktopPane.getSelectedFrame();
//		if (selected == null) {
//			MyTools.showError("No Window selected for printing!");
//		}
//		else
//TODO		PrintUtilities.printComponent(selected);
//				DOBildDrucken;
	}

	@FXML
	void settingsCmd(ActionEvent event)
	{
		settingsDialog.setVisible(true);
	}

	@FXML
	void iterationParamsCmd(ActionEvent event)
	{
		iterationParametersDialog.setVisible(true);
	}

	@FXML
	void quitCmd(ActionEvent event)
	{
		if (ChemEql.instance().quitOk()) {
			Platform.exit();
			System.exit(0); // required to exit AWT
		}
	}

	@FXML
	void editRegLibComponentsCmd(ActionEvent event)
	{
		if (regLibrary == null) {
			regLibrary = Library.readBinLibrary(true);
		}
		if (regLibrary == null) {
			return;// Error while reading library: Exit!
		}
		EditComponentsDialog.getInstance(this).show(regLibrary);
	}

	@FXML
	void editRegLibSpeciesCmd(ActionEvent event)
	{
		if (regLibrary == null) {
			regLibrary = Library.readBinLibrary(true);
		}
		if (regLibrary == null) {
			return;// Error while reading library: Exit!
		}
		EditSpeciesDialog.getInstance(null).show(regLibrary);
	}

	@FXML
	void editSPLibComponentsCmd(ActionEvent event)
	{
		if (spLibrary == null) {
			spLibrary = Library.readBinLibrary(false);
		}
		if (spLibrary == null) {
			return;// Error while reading library: Exit!
		}
		EditComponentsDialog.getInstance(null).show(spLibrary);
	}

	@FXML
	void editSPLibSpeciesCmd(ActionEvent event)
	{
		if (spLibrary == null) {
			spLibrary = Library.readBinLibrary(false);
		}
		if (spLibrary == null) {
			return;// Error while reading library: Exit!
		}
		EditSpeciesDialog.getInstance(null).show(spLibrary);
	}

	@FXML
	void fileInfoCmd(ActionEvent event)
	{
		fileInfoMI.setDisable(true);
		updateFileInfoWindow(false);
	}

	@FXML
	void replaceHbyOHCmd(ActionEvent event)
	{
//		matrix.replaceHbyOHProc();
		updateFileInfoWindow(true);
	}

	@FXML
	void deleteSpeciesCmd(ActionEvent event)
	{
		DeleteSpeciesDialog.getInstance(ChemEqlGuiController.this).setVisible(true);
		updateFileInfoWindow(true);
	}

	@FXML
	void solidPhasesCmd(ActionEvent event)
	{
		ChangeSolidPhaseDialog.getInstance(ChemEqlGuiController.this).setVisible(true);
		updateFileInfoWindow(true);
	}

	@FXML
	void restoreCmd(ActionEvent event)
	{
		restoreProc();
	}

	@FXML
	void pHconstCmd(ActionEvent event)
	{
//		PHConstDialog.getInstance(Main.this).setVisible(true);
	}

	@FXML
	void pHrangeCmd(ActionEvent event)
	{
//		PHRangeDialog.getInstance(Main.this).setVisible(true);
	}

	@FXML
	void compRangeCmd(ActionEvent event)
	{
//		ComponentRangeDialog.getInstance(Main.this).setVisible(true);
	}

	@FXML
	void adsRangeCmd(ActionEvent event)
	{
//		AdsorptionRangeDialog.getInstance(Main.this).setVisible(true);
	}

	@FXML
	void logKrangeCmd(ActionEvent event)
	{
//		LogKRangeDialog.getInstance(Main.this).setVisible(true);
	}

	@FXML
	void kinetikCmd(ActionEvent event)
	{
//		KineticsDialog.getInstance(Main.this).setVisible(true);
	}

	@FXML
	void pXpYCmd(ActionEvent event)
	{
//		PxPyDiagramDialog.getInstance(this).setVisible(true);
	}

	@FXML
	private WebView htmlView;

	@FXML
	void activityInfoCmd(ActionEvent event)
	{
		URL urlActivityHelp = getClass().getResource("resources/activityHelp.html");
		htmlView.getEngine().load(urlActivityHelp.toExternalForm());
//		Dialogs.create().title("Activity Info").message(message)
//		JTextPane tp = new JTextPane();
//		tp.setEditable(false);
//		try {
//			tp.setPage(Main.class.getResource("resources/activityHelp.html"));
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//		JScrollPane scroller = new JScrollPane(tp,
//				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//
//		JOptionPane pane = new JOptionPane(scroller,
//				JOptionPane.INFORMATION_MESSAGE);
//		JInternalFrame dialog = pane.createInternalFrame(
//				desktopPane, "Activity Info");
//		dialog.setResizable(true);
//		dialog.setBounds(10, 40, 600, 350);
//		dialog.setVisible(true);
//		activityInfoCmd.setEnabled(false);
//		dialog.addInternalFrameListener(new InternalFrameAdapter()
//		{
//			public void internalFrameClosed(InternalFrameEvent e)
//			{
//				activityInfoCmd.setEnabled(true);
//			}
//		});
	}

	@FXML
	void graphCmd(ActionEvent event)
	{
		GraphicsDialog.getInstance(this).setVisible(true);
	}

	@FXML
	void goCmd(ActionEvent event)
	{
//		matrixView.setVisible(false);
		goMI.setDisable(true);
		stopMI.setDisable(false);

		// Calculation gets its own thread so the GUI will not be blocked
		// and can be updated with higher priority. Also required to allow
		// the user to interrupt calculation via menu. Subsequent actions
		// are included in the thread to be executed after calculation has
		// finished
		calculationThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try {
					matrix.runProc(activitySetting());
				} catch (CalculationStoppedException ex) {
					;
				} catch (final Exception ex) {
					try {
						SwingUtilities.invokeAndWait(new Runnable()
						{
							public void run()
							{
								MyTools.showException(ex);
							}
						});
					} catch (InvocationTargetException | InterruptedException e) {
					}
				} finally {
					restoreProc();
					goMI.setDisable(false);
					stopMI.setDisable(true);
					saveDataMI.setDisable(false);
					restoreMI.setDisable(false);
					fileInfoMI.setDisable(false);
				}
			}
		}, ChemEql.APP_TITLE + "-Calculation");
		calculationThread.setPriority(
				Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
		calculationThread.start();
	}

	@FXML
	void stopCmd(ActionEvent event)
	{
		stopMI.setDisable(true);
		runMenu.setDisable(true);
	}

	@FXML
	void aboutCmd(ActionEvent event)
	{
		new SplashScreen(getClass().getResource("resources/about.gif"), null, 20000);
	}

	static final boolean TESTING = false;

	// this is for platform independence
	Matrix matrix;

	private Library regLibrary;
	Library spLibrary;

//	private java.awt.Component myGlass;
	private Thread calculationThread;

	// settings
	boolean compRange;
	boolean pHfix;
	boolean pHrange;
	boolean adsRange;
	boolean doDrawGraph;
	boolean doKinetik;
	OutputFormat outputFormat;
	boolean logKrange;
	boolean dopXpYdiagram;
	boolean activityOutput;
	boolean matrixIsLoaded;
	private boolean pXpYplot;

	double pHrangeStart;
	double pHrangeEnd;
	double pHrangeStep;

	boolean compRangeIsLog;			// was logarithm

	// windows
	@FXML
	SplitPane centerSplitPane;
	MatrixView matrixView;
	@FXML
	MatrixInfoController matrixInfoViewController;
	private OutputWindow outputWindow;
	JInternalFrame graphicsWindow;
	PlotView plotView;
	DataCollection graphicsData;
	int[] drawSpecs;

	// these two dialogs cannot be created lazily like the others, since they
	// provide values whose default values have to be accessible right from the
	// start
	SettingsDialog settingsDialog = SettingsDialog.getInstance(this);
	IterationParametersDialog iterationParametersDialog = IterationParametersDialog.getInstance(this);

	private void setBooleans()
	{
		set1();
		set2();
		doKinetik = false;
		matrixIsLoaded = false;
		iterationParametersDialog.autoConvCrit = false;
		iterationParametersDialog.markIter = false;
	}

	// private helper methods (new in ChemEQLJava)
	private void set1()
	{
		compRange = false;
		pHfix = false;
		pHrange = false;
		adsRange = false;
		doDrawGraph = false;
		compRangeIsLog = false;
		setNumFormatToLinear();
		outputFormat = OutputFormat.REGULAR;
	}

	// private helper methods (new in ChemEQLJava)
	private void set2()
	{
		iterationParametersDialog.maxIterations = IterationParametersDialog.MAX_ITERATIONS_DEFAULT;
		activityOptions.setExpanded(false); // was calcActivCoeff.selectToggle(null);
		setNumFormatToLinear();
	}

	private void restoreProc()
	{
		matrixMenu.setDisable(false);	/* sind während des Rechnens disabled worden */

		runMenu.setDisable(false);
		goMI.setDisable(false);
		stopMI.setDisable(true);
		set2();
		activityOutput = false;
		doKinetik = false;

		if (matrix.multiConc == 1) {
			set1();
			logKrange = false;
			dopXpYdiagram = false;
			doKinetik = false;

			formatMenu.setDisable(true);
			graphMI.setDisable(true);
			logKrangeMI.setDisable(false);
			kinetikMI.setDisable(false);
			pXpYMI.setDisable(false);
			if (!matrixView.isVisible()) {
				fileInfoMI.setDisable(false);
			}

			if (matrix.isHorHplusAndFree()) {
				pHconstMI.setDisable(false);
				pHrangeMI.setDisable(false);
			}
			if (matrix.isTotal()) /*if (one of the modes is 'total') activate CompRange*/ {
				compRangeMI.setDisable(false);
			}
			adsRangeMI.setDisable(!matrix.adsorption());
		} /* if multiConc == 1 */

		goMI.setDisable(false);
		activityOptions.setDisable(false);
//		setCursor(Cursor.getDefaultCursor());
	}

	private void flushMatrixProc()
	{
		matrix.initialize(); /*setzt den Inhalt aller globalen Handles =0 oder ''*/

		// initializes also compNo and specNo with 0

		set1();
		set2();

		logKrange = false;
		pXpYplot = false;
		activityOutput = false;	/*output in activities instead of concentrations*/

		iterationParametersDialog.autoConvCrit = false;
		iterationParametersDialog.markIter = false;

		openMI.setDisable(false);
		saveMatrixMI.setDisable(true);
		matrixMenu.setDisable(true);
		modeMenu.setDisable(true);
		runMenu.setDisable(true);

		graphicsWindow.setVisible(false);
		outputWindow.setVisible(false);
//		matrixView.setVisible(false);
	}

	private void selectAndReadLibrary()
	{
		final Library lib;

		fileChooser.setTitle("Import library");
		final File inputFile = fileChooser.showOpenDialog(ChemEql.mainStage());
		if (inputFile == null) {
			return;				// Exit!
		}

		// Choose what kind of library the imported library will be stored as
		Object[] options = {"Regular library", "Solid phases library"};
		int answer = JOptionPane.showOptionDialog(null, "Save library as:",
				ChemEql.APP_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);
		if (answer == JOptionPane.YES_OPTION) {
			lib = regLibrary = new Library(true);
		}
		else if (answer == JOptionPane.NO_OPTION) {
			lib = spLibrary = new Library(false);
		}
		else {
			//assert answer == JOptionPane.CLOSED_OPTION;
			return;																// Exit!
		}

		try {
			importLibFromStream(lib, new FileInputStream(inputFile),
					"Importing library " + inputFile);
		} catch (FileNotFoundException ex) {
			// should never happen
			regLibrary = spLibrary = null;
			MyTools.showException(ex);
		}
	}

	private void importLibFromStream(final Library lib, final InputStream inStream,
			final String msg)
	{
		// library reading must take place in its own thread to allow the
		// ProgressMonitor to be updated. Subsequent actions are included in the
		// new thread in order so they wait for finishing import
		Thread importThread = new Thread(new Runnable()
		{
			public void run()
			{
//				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					lib.importLib(inStream, msg);
					String path = lib.writeBinary();
					JOptionPane.showMessageDialog(null,
							(lib == regLibrary ? "Regular" : "Solid phases")
							+ " library sucessfully imported and stored\nas binary file at "
							+ path + ".", ChemEql.APP_TITLE, JOptionPane.INFORMATION_MESSAGE);
					if (lib == regLibrary) {
						SelectComponentsDialog.getInstance(null).show(regLibrary);
						if (matrixIsLoaded) {
							saveMatrixMI.setDisable(false);
							matrixLoadedUpdate();
						}
					}
				} catch (InterruptedIOException ex) {
					// user interrupted import
					if (lib == regLibrary) {
						regLibrary = null;
					}
					else {
						// assert lib == spLibrary;
						spLibrary = null;
					}
				} catch (IOException | HeadlessException ex) {
					if (lib == regLibrary) {
						regLibrary = null;
					}
					else {
						// assert lib == spLibrary;
						spLibrary = null;
					}
					MyTools.showException(ex);
				} finally {
//					setGlassPane(myGlass);
//					myGlass.setVisible(true);
//					setCursor(Cursor.getDefaultCursor());
				}
			}
		}, ChemEql.APP_TITLE + "-Library-Import");

//		setGlassPane(blockingGlass);
//		blockingGlass.setVisible(true);
		importThread.setPriority(
				Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
		importThread.start();
	}

	private void exportLibrary()
	{
		final Library lib;
		String fileName;

		// Choose what kind of library to be exported
		Object[] options = {"Regular Library", "Solid Phases Library"};
		int answer = JOptionPane.showOptionDialog(null, "Select Library to Export:",
				ChemEql.APP_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);
		if (answer == 0) {
			if (regLibrary == null) {
				regLibrary = Library.readBinLibrary(true);
			}
			lib = regLibrary;
		}
		else if (answer == 1) {
			if (spLibrary == null) {
				spLibrary = Library.readBinLibrary(false);
			}
			lib = spLibrary;
		}
		else {
			//assert answer == JOptionPane.CLOSED_OPTION;
			return;																			// Exit!
		}
		if (lib == null) {
			return;				// Error while reading library: Exit!
		}
		try {
			fileChooser.setTitle("Export " + options[answer]);
			fileChooser.setInitialFileName(lib.defaultTextFileName());
			File file = fileChooser.showSaveDialog(null);
			if (file == null) {
				return;				// Exit!
			}
//			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			lib.exportTo(file);
		} catch (IOException ex) {
			MyTools.showException(ex);
		} finally {
//			setCursor(Cursor.getDefaultCursor());
		}
	}

	void reloadLibrary(Library lib)
	{
		if (lib == regLibrary) {
			regLibrary = Library.readBinLibrary(true);
		}
		else {
			spLibrary = Library.readBinLibrary(false);
		}
	}

	private void matrixLoadedUpdate()
	{
		if (matrix.isHorHplusAndFree()) {
			pHfix = true;
			pHrange = true;
			pHrangeStart = -matrix.concEstim[matrix.totComp - 1];
			pHrangeEnd = pHrangeStart;
			pHrangeStep = pHrangeEnd;
		}

//		pageSetUpCmd.setDisable(true);
		printMI.setDisable(false);
		matrixMenu.setDisable(false);
		fileInfoMI.setDisable(true);
		if (matrix.isLastCompName("H+") || matrix.isLastCompName("OH-")) {
			replaceHbyOHMI.setDisable(false);
		}
		deleteSpeciesMI.setDisable(false);
		solidPhasesMI.setDisable(false);
		modeMenu.setDisable(false);
		restoreMI.setDisable(false);
		logKrangeMI.setDisable(false);
		activityOptions.setDisable(false);
//		noCmd.setDisable(false);
//		debyeHueckelCmd.setDisable(false);
//		guentelbergCmd.setDisable(false);
//		daviesCmd.setDisable(false);
		runMenu.setDisable(false);
		goMI.setDisable(false);
		stopMI.setDisable(true);
		if (matrix.multiConc == 1) {
			compRangeMI.setDisable(false);
			kinetikMI.setDisable(false);
			pXpYMI.setDisable(false);
			formatMenu.setDisable(true);
		}
		else {
			compRangeMI.setDisable(true);
			formatMenu.setDisable(false);
		}
		adsRangeMI.setDisable(!(matrix.adsorption() && matrix.multiConc == 1));

		if (matrix.isHorHplusAndFree()) /*pH is constant*/ {
			pHconstMI.setDisable(false);
			if (matrix.multiConc == 1) {
				pHrangeMI.setDisable(false);
			}
		}
		else /*pH is not constant*/ {
			pHconstMI.setDisable(true);
			if (matrix.multiConc == 1) {
				pHrangeMI.setDisable(true);
			}
		}
//		setCursor(Cursor.getDefaultCursor());
		/*makes cursor an arrow*/

		updateFileInfoWindow(false);
	}

	private void createAllWindows()
	{
		matrixView = new MatrixView();
		centerSplitPane.getItems().add(0, matrixView);
//		matrixView.setVisible(false);
//		matrixView.addInternalFrameListener(new InternalFrameAdapter()
//		{
//			public void internalFrameClosing(InternalFrameEvent ev)
//			{
//				fileInfoMI.setDisable(false);
//			}
//		});
//		desktopPane.add(matrixView);

		outputWindow = new OutputWindow(null, matrix);
//		outputWindow.setBounds(
//				30, 30, desktopPane.getWidth() - 40, desktopPane.getHeight() - 40);
		outputWindow.setVisible(false);
//		desktopPane.add(outputWindow);

		graphicsWindow = new JInternalFrame("Graphics", true, true, true, true);
		graphicsWindow.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		graphicsData
				= new DataCollection(new Class[]{SpeciesData.class, PxPyData.class

				}
				);
		plotView = new PlotView(graphicsData);

		graphicsWindow.getContentPane()
				.add(plotView, BorderLayout.CENTER);
//		graphicsWindow.setBounds(
//				0, 15, desktopPane.getWidth() - 20, desktopPane.getHeight() - 15);
		graphicsWindow.setVisible(false);
//		desktopPane.add(graphicsWindow);
	}

	void repaintDataWindows()
	{
//		matrixView.repaint();
		outputWindow.repaint();
	}

	void dataOutput()
	{
		outputWindow.update();
	}

	void updateFileInfoWindow(boolean enableSaveMatrixCmd)
	{
		// ?? is it sensible to adjust window size and position automatically??
		//		wide = 70 * totComp + 30;
		//		if (wide > qd.screenBits.Bounds.right - 100)
		//			wide = qd.screenBits.Bounds.right - 100;
		//		if (wide < 300)
		//			wide = 300;
		//		high = v + 25;
		//		if (high > qd.screenBits.Bounds.Bottom - 50)
		//			high = qd.screenBits.Bounds.Bottom - 80;
		//		h = trunc((qd.screenBits.Bounds.Bottom - high) / 2);
		//		v = trunc((qd.screenBits.Bounds.right - wide) / 2);
		//		moveWindow(FileInfoWndw, v, h, true);
		//		SizeWindow(FileInfoWndw, wide, high, true);

		if (enableSaveMatrixCmd) {
			saveMatrixMI.setDisable(false);
		}

		matrixInfoViewController.setMatrix(matrix);
		matrixView.setVisible(true);
		fileInfoMI.setDisable(true);
		if (matrix.isLastCompName("H+")) {
			replaceHbyOHMI.setText("Replace H+ by OH-");
			pHconstMI.setText("Change pH ...");
			pHrangeMI.setText("pH range ...");
		}
		else if (matrix.isLastCompName("OH-")) {
			replaceHbyOHMI.setText("Replace OH- by H+");
			pHconstMI.setText("Change pOH ...");
			pHrangeMI.setText("pOH range ...");
		}
		else {
			throw new IllegalStateException("Last component must be H+ or OH-!");
		}
	}

	private void setFileInfoWindowTitle(File f)
	{
		StringBuilder t = new StringBuilder(ChemEql.APP_TITLE);
		t.append(" - ").append(f.getName());
		if (f.getParent() != null) {
			t.append(" (").append(f.getParent()).append(")");
		}
		ChemEql.mainStage().setTitle(t.toString());
	}

	boolean isLogNumFormat()
	{
		return numFormat.getSelectedToggle() == numFormatLogRMI;
	}

	void setNumFormatToLinear()
	{
		numFormat.selectToggle(numFormatLinRMI);
	}

	void setNumFormatToLogarithmic()
	{
		numFormat.selectToggle(numFormatLogRMI);
	}

	private Activity activitySetting()
	{
		if (activityOptions.isExpanded()) {
			if (debyeHückelRB.isSelected()) {
				return Activity.DEBYEHUECKEL;
			}
			else if (güntelbergRB.isSelected()) {
				return Activity.GUENTELBERG;
			}
			else if (daviesRB.isSelected()) {
				return Activity.DAVIES;
			}
		}
		return Activity.NO;
	}

	String getCalcActivityName()
	{
		return activitySetting().getValue();
	}

	@SuppressWarnings("fallthrough")
	void setActivity(Activity activity)
	{
		switch (activity) {
		case NO:
			activityOptions.setExpanded(false);
			break;
		case DEBYEHUECKEL:
			debyeHückelRB.setSelected(true);
		case GUENTELBERG:
			güntelbergRB.setSelected(true);
		case DAVIES:
			daviesRB.setSelected(true);
			activityOptions.setExpanded(true);
		}
	}


	static class OptionTitleChanger implements InvalidationListener
	{
		String titleWhenExpanded;
		String titleWhenCollapsed;

		public OptionTitleChanger(String titleWhenExpanded, String titleWhenCollapsed)
		{
			this.titleWhenExpanded = titleWhenExpanded;
			this.titleWhenCollapsed = titleWhenCollapsed;
		}

		@Override
		public void invalidated(Observable observable)
		{
			BooleanProperty expanded = (BooleanProperty)observable;
			((TitledPane)expanded.getBean()).setText(expanded.get() ? titleWhenExpanded : titleWhenCollapsed);
		}
	}
}
