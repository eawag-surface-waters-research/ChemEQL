package ch.eawag.chemeql;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.InternalFrameEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import de.vseit.showit.PlotView;
import de.vseit.showit.DataCollection;
// import de.vseit.util.PrintUtilities;

	
//	public void quit()
//	{
//		int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
//		if (option == JOptionPane.YES_OPTION)
//		{
//			System.exit(0);
//		}
//	}


public class ChemEQL3 extends JFrame
{
	static final boolean TESTING = false;

	static
	{
		// set english locale to get english dialogs and number formats anyway
		// (must happen *before* creating static dialogs and the main window,
		// since dialogs inherit their locale from their parent view, namely the
		// main window.
		Locale.setDefault(Locale.ENGLISH);
	}
	
	static final String AN = "ChemEQL";
	private static JFileChooser FILE_CHOOSER;
   private static FileFilter CQL_FILTER = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory() || pathname.getName().endsWith(".cql");
		}
		
		public String getDescription()
		{
			return ChemEQL3.AN+" Matrices";
		}
	};
   private static FileFilter XLS_FILTER = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory() || pathname.getName().endsWith(".xls");
		}
		
		public String getDescription()
		{
			return "Output Data (tab delimited)";
		}
	};

	// this is for platform independence
	private static final int MENU_SHORTCUT_KEY =
		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	private static final boolean IS_MAC_OSX =
		System.getProperty("os.name").toLowerCase().startsWith("mac os x");

	Matrix matrix;

	private Library regLibrary;
	Library spLibrary;

	private Component myGlass;
	private JPanel blockingGlass;
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
	ButtonGroup numFormat;
	ButtonGroup calcActivCoeff;
	
	private JDesktopPane desktopPane;
	
	// windows
	FileInfoWindow fileInfoWindow;
	private OutputWindow outputWindow;
	JInternalFrame graphicsWindow;
	PlotView plotView;
	DataCollection graphicsData;
	int[] drawSpecs;
	
	// these two dialogs cannot be created lazily like the others, since they
	// provide values whose default values have to be accessible roght from the
	// start
	SettingsDialog settingsDialog = SettingsDialog.getInstance(this);
	IterationParametersDialog iterationParametersDialog =
			IterationParametersDialog.getInstance(this);

	public static void main(String args[])
	{
		try
		{	// set native L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex)
		{ex.printStackTrace();}
		
		// put main menu top of the screen for Mac OS X (JDK 1.4+)
		System.setProperty("apple.laf.useScreenMenuBar","true");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",ChemEQL3.AN);
		if (TESTING)
			System.out.println(System.getProperties().toString());

		// create file chooser *after* setting the L&F
		FILE_CHOOSER = new JFileChooser(System.getProperty("user.home"));
		new ChemEQL3();
	}


	private ChemEQL3()
	{
		System.out.println("Starting "+AN+", version 3.01 (8th of Sept. 2009)");

		// make application respond to the Mac OS X application menu
		macOSXRegistration();

		Dimension screenSize;
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screenSize.width;
		int h = screenSize.height;
		setBounds(50,30,w-100,h-60);
		setTitle(AN);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent evt)
			{
				// special confirmation required??
				exitApplication();
			}
		});

		desktopPane = new JDesktopPane();
		getContentPane().add(desktopPane,BorderLayout.CENTER);
		
		matrix = new Matrix(this);		// create global data structures
		setJMenuBar(createMenu());
		setBooleans();
		doKinetik = false;
		setVisible(true);
		createAllWindows();	// must create internal frames *after* desktop has
		// been made visible for having desktop bounds be set
		
		// initialize glass pane for blocking events in selectAndReadLibrary
		myGlass = getGlassPane();
		blockingGlass = new JPanel();
		blockingGlass.setOpaque(false);
		MouseInputAdapter adapter = new MouseInputAdapter()
		{};
		blockingGlass.addMouseListener(adapter);
		blockingGlass.addMouseMotionListener(adapter);
		
		
		// show welcome
		new SplashScreen(ChemEQL3.class.getResource("resources/startup.gif"), this, 1300);

		// import standard libraries from jar file if binary libs don't exist yet
		try
		{
			String t = " library sucessfully imported and stored as binary file at ";
			if (!Library.binLibFile(true).exists())
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				regLibrary = new Library(ChemEQL3.this,true);
				regLibrary.importLib(ChemEQL3.class.getResourceAsStream("resources/CQL.Library"),
					"Importing standard regular library");
				String path = regLibrary.writeBinary();
				System.out.println("Regular" + t + path);
			}
			if (!Library.binLibFile(false).exists())
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				spLibrary = new Library(ChemEQL3.this,false);
				spLibrary.importLib(ChemEQL3.class.getResourceAsStream("resources/CQL.spLibrary"),
					"Importing standard solid phases library");
				String path = spLibrary.writeBinary();
				System.out.println("Solid phases" + t + path);
			}
		}
		catch (Exception ex)
		{
			regLibrary = spLibrary = null;
			MyTools.showException(ex);
		}
		finally
		{
			setCursor(Cursor.getDefaultCursor());
		}
	}

	// generic registration with the Mac OS X application menu. Checks the
	// platform, then attempts to register with the Apple EAWT.
	// This method calls OSXAdapter.registerMacOSXApplication() and OSXAdapter.enablePrefs().
	private void macOSXRegistration()
	{
		if (IS_MAC_OSX)
		{
			try
			{
				Class osxAdapter = Class.forName("ch.eawag.chemeql.OSXAdapter");
				Class[] defArgs = {ChemEQL3.class};
				Method registerMethod =
					osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
				if (registerMethod != null)
				{
					Object[] args = { this };
					registerMethod.invoke(osxAdapter, args);
				}
				// This is slightly gross. to reflectively access methods with boolean args,
				// use "boolean.class", then pass a Boolean object in as the arg, which apparently
				// gets converted for you by the reflection system.
//				defArgs[0] = boolean.class;
//				Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
//				if (prefsEnableMethod != null)
//				{
//					Object args[] = {Boolean.TRUE};
//					prefsEnableMethod.invoke(osxAdapter, args);
//				}
			}
			catch (NoClassDefFoundError e)
			{
				// This will be thrown first if the OSXAdapter is loaded on a system
				// without the EAWT because OSXAdapter extends ApplicationAdapter
				System.err.println("This version of Mac OS X does not support the Apple EAWT. Application Menu handling has been disabled (" + e + ")");
			}
			catch (ClassNotFoundException e)
			{
				// This shouldn't be reached; if there's a problem with the
				// OSXAdapter we should get the above NoClassDefFoundError first.
				System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
			}
			catch (Exception e)
			{
				System.err.println("Exception while loading the OSXAdapter:");
				e.printStackTrace();
			}
		}
	}

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
		numFormat.setSelected(NumFormatEnum.LINEAR,true);
		outputFormat = OutputFormat.REGULAR;
	}
	
	// private helper methods (new in ChemEQLJava)
	private void set2()
	{
		iterationParametersDialog.maxIterations =
		IterationParametersDialog.MAX_ITERATIONS_DEFAULT;
		calcActivCoeff.setSelected(ActivityEnum.NO,true);
		numFormat.setSelected(NumFormatEnum.LINEAR,true);
	}
	
	private void restoreProc()
	{
		matrixMenu.setEnabled(true);	/* sind während des Rechnens disabled worden */
		runMenu.setEnabled(true);
		goCmd.setEnabled(true);
		stopCmd.setEnabled(false);
		set2();
		activityOutput = false;
		doKinetik = false;
		
		if (matrix.multiConc == 1)
		{
			set1();
			logKrange = false;
			dopXpYdiagram = false;
			doKinetik = false;
			
			formatMenu.setEnabled(false);
			graphCmd.setEnabled(false);
			logKrangeCmd.setEnabled(true);
			kinetikCmd.setEnabled(true);
			pXpYCmd.setEnabled(true);
			if (!fileInfoWindow.isVisible())
				fileInfoCmd.setEnabled(true);
			
			if (matrix.isHorHplusAndFree())
			{
				pHconstCmd.setEnabled(true);
				pHrangeCmd.setEnabled(true);
			}
			if (matrix.isTotal()) /*if (one of the modes is 'total') activate CompRange*/
				compRangeCmd.setEnabled(true);
			adsRangeCmd.setEnabled(matrix.adsorption());
		} /* if multiConc == 1 */
		goCmd.setEnabled(true);
		activitiesMenu.setEnabled(true);
		setCursor(Cursor.getDefaultCursor());
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
		
		openCmd.setEnabled(true);
		saveMatrixCmd.setEnabled(false);
		matrixMenu.setEnabled(false);
		modeMenu.setEnabled(false);
		runMenu.setEnabled(false);
		
		graphicsWindow.setVisible(false);
		outputWindow.setVisible(false);
		fileInfoWindow.setVisible(false);
	}
	
	
	private void selectAndReadLibrary()
	{
		final Library lib;
		final File inputFile;

		FILE_CHOOSER.setDialogTitle("Import library");
		int returnVal = FILE_CHOOSER.showOpenDialog(this);
		if (returnVal == JFileChooser.CANCEL_OPTION) return;				// Exit!
		inputFile = FILE_CHOOSER.getSelectedFile();
		
		// assert returnVal == JFileChooser.APPROVE_OPTION;
		// Choose what kind of library the imported library will be
		// stored as
		Object[] options = {"Regular library","Solid phases library"};
		int answer = JOptionPane.showOptionDialog(this,"Save library as:",
		AN,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,
		null,options,options[0]);
		if (answer == JOptionPane.YES_OPTION)
			lib = regLibrary = new Library(this,true);
		else if (answer == JOptionPane.NO_OPTION)
			lib = spLibrary = new Library(this,false);
		else
		{
			//assert answer == JOptionPane.CLOSED_OPTION;
			return;																// Exit!
		}

		try
		{
			importLibFromStream(lib,new FileInputStream(inputFile),
				"Importing library " + inputFile);
		}
		catch (FileNotFoundException ex)
		{
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
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try
				{
					lib.importLib(inStream,msg);
					String path = lib.writeBinary();
					JOptionPane.showMessageDialog(ChemEQL3.this,
						(lib == regLibrary ? "Regular" : "Solid phases") +
						" library sucessfully imported and stored\nas binary file at "
						+ path + ".",AN,JOptionPane.INFORMATION_MESSAGE);
					if (lib == regLibrary)
					{
						SelectComponentsDialog.getInstance(ChemEQL3.this).show(regLibrary);
						if (matrixIsLoaded)
						{
							saveMatrixCmd.setEnabled(true);
							matrixLoadedUpdate();
						}
					}
				}
				catch (InterruptedIOException ex)
				{
					// user interrupted import
					if (lib == regLibrary)
						regLibrary = null;
					else
					{
						// assert lib == spLibrary;
						spLibrary = null;
					}
				}
				catch (Exception ex)
				{
					if (lib == regLibrary)
						regLibrary = null;
					else
					{
						// assert lib == spLibrary;
						spLibrary = null;
					}
					MyTools.showException(ex);
				}
				finally
				{
					setGlassPane(myGlass);
					myGlass.setVisible(true);
					setCursor(Cursor.getDefaultCursor());
				}
			}
		},AN+"-Library-Import");
		
		setGlassPane(blockingGlass);
		blockingGlass.setVisible(true);
		importThread.setPriority(
			Math.max(Thread.MIN_PRIORITY,Thread.currentThread().getPriority()-1));
		importThread.start();
	}


   private void exportLibrary()
   {
      final Library lib;
      String fileName;
		
		// Choose what kind of library to be exported
      Object[] options =
      {"Regular Library","Solid Phases Library"};
      int answer = JOptionPane.showOptionDialog(this,"Select Library to Export:",
        AN,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,
         null,options,options[0]);
      if (answer == 0)
      {
         if (regLibrary == null)
            regLibrary = Library.readBinLibrary(this,true);
         lib = regLibrary;
      }
      else if (answer == 1)
      {
         if (spLibrary == null)
            spLibrary = Library.readBinLibrary(this,false);
         lib = spLibrary;
      }
		else
		{
			//assert answer == JOptionPane.CLOSED_OPTION;
			return;																			// Exit!
		}
		if (lib == null) return;				// Error while reading library: Exit!
		try
		{
			FILE_CHOOSER.setDialogTitle("Export " + options[answer]);
			File file = new File(
				FILE_CHOOSER.getCurrentDirectory(),lib.defaultTextFileName());
			FILE_CHOOSER.setSelectedFile(file);
			int returnVal = FILE_CHOOSER.showSaveDialog(this);
			if (returnVal == JFileChooser.CANCEL_OPTION) return;				// Exit!
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			lib.exportTo(FILE_CHOOSER.getSelectedFile());
		}
		catch (IOException ex)
		{
			MyTools.showException(ex);
		}
		finally
		{
			setCursor(Cursor.getDefaultCursor());
		}
	}
	
	void reloadLibrary(Library lib)
	{
		if (lib == regLibrary)
			regLibrary = Library.readBinLibrary(this,true);
		else
			spLibrary = Library.readBinLibrary(this,false);
	}

	private void matrixLoadedUpdate()
	{
		if (matrix.isHorHplusAndFree())
		{
			pHfix = true;
			pHrange = true;
			pHrangeStart = -matrix.concEstim[matrix.totComp-1];
			pHrangeEnd = pHrangeStart;
			pHrangeStep = pHrangeEnd;
		}
		
		
//		pageSetUpCmd.setEnabled(true);
		printCmd.setEnabled(true);
		matrixMenu.setEnabled(true);
		fileInfoCmd.setEnabled(false);
		if (matrix.isLastCompName("H+") || matrix.isLastCompName("OH-"))
			replaceHbyOHCmd.setEnabled(true);
		deleteSpeciesCmd.setEnabled(true);
		solidPhasesCmd.setEnabled(true);
		modeMenu.setEnabled(true);
		restoreCmd.setEnabled(true);
		logKrangeCmd.setEnabled(true);
		activitiesMenu.setEnabled(true);
		noCmd.setEnabled(true);
		debyeHueckelCmd.setEnabled(true);
		guentelbergCmd.setEnabled(true);
		daviesCmd.setEnabled(true);
		runMenu.setEnabled(true);
		goCmd.setEnabled(true);
		stopCmd.setEnabled(false);
		if (matrix.multiConc == 1)
		{
			compRangeCmd.setEnabled(true);
			kinetikCmd.setEnabled(true);
			pXpYCmd.setEnabled(true);
			formatMenu.setEnabled(false);
		}
		else
		{
			compRangeCmd.setEnabled(false);
			formatMenu.setEnabled(true);
		}
		adsRangeCmd.setEnabled(matrix.adsorption() && matrix.multiConc == 1);
		
		if (matrix.isHorHplusAndFree())		/*pH is constant*/
		{
			pHconstCmd.setEnabled(true);
			if (matrix.multiConc == 1)
				pHrangeCmd.setEnabled(true);
		}
		else											/*pH is not constant*/
		{
			pHconstCmd.setEnabled(false);
			if (matrix.multiConc == 1)
				pHrangeCmd.setEnabled(false);
		}
		setCursor(Cursor.getDefaultCursor());
		/*makes cursor an arrow*/

		drawFileInfoWindow(false);
	}
	
	
	void exitApplication()
	{
		System.exit(0);
	}
	
	
	private void createAllWindows()
	{
		fileInfoWindow = new FileInfoWindow(this, matrix);
		fileInfoWindow.setLocation(5,35);
		fileInfoWindow.setBounds(5,35,400,400);
		fileInfoWindow.setVisible(false);
		fileInfoWindow.addInternalFrameListener(new InternalFrameAdapter()
		{
			public void internalFrameClosing(InternalFrameEvent ev)
			{
				fileInfoCmd.setEnabled(true);
			}
		});
		desktopPane.add(fileInfoWindow);
		
		outputWindow = new OutputWindow(this, matrix);
		outputWindow.setBounds(
		30,30,desktopPane.getWidth()-40,desktopPane.getHeight()-40);
		outputWindow.setVisible(false);
		desktopPane.add(outputWindow);
		
		graphicsWindow = new JInternalFrame("Graphics",true,true,true,true);
		graphicsWindow.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		graphicsData =
			new DataCollection(new Class[]{SpeciesData.class,PxPyData.class});
		plotView = new PlotView(graphicsData);
		
		graphicsWindow.getContentPane().add(plotView,BorderLayout.CENTER);
		graphicsWindow.setBounds(
		0,15,desktopPane.getWidth()-20,desktopPane.getHeight()-15);
		graphicsWindow.setVisible(false);
		desktopPane.add(graphicsWindow);
	}
	
	void repaintDataWindows()
	{
		fileInfoWindow.repaint();
		outputWindow.repaint();
	}
	
	void dataOutput()
	{
		outputWindow.update();
	}
	
	void drawFileInfoWindow(boolean enableSaveMatrixCmd)
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
		
		if (enableSaveMatrixCmd)
			saveMatrixCmd.setEnabled(true);
		
		fileInfoWindow.update();
		fileInfoWindow.setVisible(true);
		fileInfoCmd.setEnabled(false);
		if (matrix.isLastCompName("H+"))
		{
			replaceHbyOHCmd.setText("Replace H+ by OH-");
			pHconstCmd.setText("Change pH ...");
			pHrangeCmd.setText("pH range ...");
		}
		else if (matrix.isLastCompName("OH-"))
		{
			replaceHbyOHCmd.setText("Replace OH- by H+");
			pHconstCmd.setText("Change pOH ...");
			pHrangeCmd.setText("pOH range ...");
		}
		else
			throw new IllegalStateException("Last component must be H+ or OH-!");
	}

	private void setFileInfoWindowTitle(File f)
	{
		String t = "Matrix " + f.getName();
		if (f.getParent() != null)
			t = t + " (" + f.getParent() + ")";
		fileInfoWindow.setTitle(t);
	}

	boolean isCalcActivCoeff(ActivityEnum sym)
	{
		return calcActivCoeff.isSelected(sym);
	}
	
	String getCalcActivCoeff()
	{
		return ((ActivityEnum)calcActivCoeff.getSelection()).getValue();
	}
	
	boolean isLogNumFormat()
	{
		return numFormat.isSelected(NumFormatEnum.LOGARITHMIC);
	}
	
// -----------------------------------------------------------------------------
// --- declare and create menu items -------------------------------------------
// -----------------------------------------------------------------------------
	
	// file menu
	private JMenu fileMenu;
	private JMenuItem openCmd;
	private JMenuItem readLibCmd;
	private JMenuItem saveMatrixCmd;
	private JMenuItem saveDataCmd;
	private JMenuItem importLibCmd;
	private JMenuItem exportLibCmd;
	private JMenuItem printCmd;
	private JMenuItem settingsCmd;
	private JMenuItem iterationParamsCmd;
	private JMenuItem quitCmd;
	// library menu items
	private JMenu libraryMenu;
	private JMenuItem editRegLibComponentsCmd;
	private JMenuItem editRegLibSpeciesCmd;
	private JMenuItem editSPLibComponentsCmd;
	private JMenuItem editSPLibSpeciesCmd;
	// matrix menu items
	private JMenu matrixMenu;
	private JMenuItem fileInfoCmd;
	JMenuItem replaceHbyOHCmd;
	private JMenuItem deleteSpeciesCmd;
	private JMenuItem solidPhasesCmd;
	// option menu items
	private JMenu modeMenu;
	private JMenuItem restoreCmd;
	JMenuItem pHconstCmd;
	JMenuItem pHrangeCmd;
	JMenuItem compRangeCmd;
	JMenuItem adsRangeCmd;
	JMenuItem logKrangeCmd;
	private JMenuItem kinetikCmd;
	private JMenuItem pXpYCmd;
	JMenu formatMenu;
	private JRadioButtonMenuItem subFormLin;
	private JRadioButtonMenuItem subFormLog;
	private JMenu activitiesMenu;
	private JRadioButtonMenuItem noCmd;
	private JRadioButtonMenuItem debyeHueckelCmd;
	private JRadioButtonMenuItem guentelbergCmd;
	private JRadioButtonMenuItem daviesCmd;
	private JMenuItem activityInfoCmd;
	JMenuItem graphCmd;
	// run menu items
	private JMenu runMenu;
	private JMenuItem goCmd;
	JMenuItem stopCmd;
	// about menu items
	private JMenuItem aboutCmd;
	
	private JMenuBar createMenu()
	{
		JMenuBar menuBar = new JMenuBar();
		
		numFormat = new ButtonGroup();
		calcActivCoeff = new ButtonGroup();
		
		// ----- file menu -----
		fileMenu = new JMenu("File");
		
		openCmd = new JMenuItem("Open Matrix ...");
		openCmd.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_O,MENU_SHORTCUT_KEY));
		openCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				/* eine Matrix einlesen */
				flushMatrixProc();
				try
				{
					FILE_CHOOSER.setDialogTitle("Open matrix");
					FILE_CHOOSER.addChoosableFileFilter(CQL_FILTER);
					FILE_CHOOSER.setFileFilter(CQL_FILTER);
					int returnVal = FILE_CHOOSER.showOpenDialog(ChemEQL3.this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						// user has not canceled file dialog: go read my matrix!
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						File file = FILE_CHOOSER.getSelectedFile();
						matrix.datInput(file);
						setFileInfoWindowTitle(file);
						saveMatrixCmd.setEnabled(false);
						matrixIsLoaded = true;
						matrixLoadedUpdate();
					}
					else
						return;																// Exit!
				}
				catch (IOException ex)
				{
					MyTools.showException(ex);
				}
				finally
				{
					setCursor(Cursor.getDefaultCursor());
					FILE_CHOOSER.setFileFilter(FILE_CHOOSER.getAcceptAllFileFilter());
					FILE_CHOOSER.removeChoosableFileFilter(CQL_FILTER);
				}
			}
		});
		fileMenu.add(openCmd);
		
		readLibCmd = new JMenuItem("Access Library ...");
		readLibCmd.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_L,MENU_SHORTCUT_KEY));
		readLibCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				/* eine binäre Bibliothek aus dem Preferences-Folder einlesen */
				flushMatrixProc();
				if (regLibrary == null) /* falls binäre Library noch nie geöffnet...*/
					regLibrary = Library.readBinLibrary(ChemEQL3.this,true);
				if (regLibrary == null) return;// Error while reading library: Exit!

				SelectComponentsDialog.getInstance(ChemEQL3.this).show(regLibrary);

				if (matrixIsLoaded)
				{
					saveMatrixCmd.setEnabled(true);
					matrixLoadedUpdate();
				}
			}
		});
		fileMenu.add(readLibCmd);

		fileMenu.add(new JSeparator());
		
		saveMatrixCmd = new JMenuItem("Save Matrix ...");
		saveMatrixCmd.setEnabled(false);
		saveMatrixCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				try
				{
					FILE_CHOOSER.setDialogTitle("Save current matrix");
					FILE_CHOOSER.addChoosableFileFilter(CQL_FILTER);
					FILE_CHOOSER.setFileFilter(CQL_FILTER);
					File file = new File(FILE_CHOOSER.getCurrentDirectory(),"z.cql");
					FILE_CHOOSER.setSelectedFile(file);
					int returnVal = FILE_CHOOSER.showSaveDialog(ChemEQL3.this);
					if (returnVal == JFileChooser.CANCEL_OPTION) return;		// Exit!
					
					// user has not canceled file dialog: save if file is new or
					// shall be replaced
					file = FILE_CHOOSER.getSelectedFile();
					if (!file.getName().endsWith(".cql"))
						file = new File(file.getParentFile(),file.getName() + ".cql");
					if (!file.exists() || JOptionPane.showConfirmDialog(ChemEQL3.this,
						"Replace existing matrix '" + file.getName() + "'?",
						"Confirm",JOptionPane.YES_NO_OPTION)
						== JOptionPane.YES_OPTION)
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						matrix.save(file);
						setFileInfoWindowTitle(file);
					}
				}
				catch (IOException ex)
				{
					MyTools.showException(ex);
				}
				finally
				{
					setCursor(Cursor.getDefaultCursor());
					FILE_CHOOSER.setFileFilter(FILE_CHOOSER.getAcceptAllFileFilter());
					FILE_CHOOSER.removeChoosableFileFilter(CQL_FILTER);
				}
			}
		});
		fileMenu.add(saveMatrixCmd);
		
		saveDataCmd = new JMenuItem("Save Data ...");
		saveDataCmd.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_S,MENU_SHORTCUT_KEY));
		saveDataCmd.setEnabled(false);
		saveDataCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				try
				{
					FILE_CHOOSER.setDialogTitle("Save current data");
					FILE_CHOOSER.addChoosableFileFilter(XLS_FILTER);
					FILE_CHOOSER.setFileFilter(XLS_FILTER);
					File file = new File(FILE_CHOOSER.getCurrentDirectory(),"z.xls");
					FILE_CHOOSER.setSelectedFile(file);
					int returnVal = FILE_CHOOSER.showSaveDialog(ChemEQL3.this);
					if (returnVal == JFileChooser.CANCEL_OPTION) return;		// Exit!
					
					// user has not canceled file dialog: save if file is new or
					// shall be replaced
					file = FILE_CHOOSER.getSelectedFile();
					if (!file.getName().endsWith(".xls"))
						file = new File(file.getParentFile(),file.getName() + ".xls");
					if (!file.exists() || JOptionPane.showConfirmDialog(ChemEQL3.this,
						"Replace existing data file '" + file.getName() + "'?",
						"Confirm",JOptionPane.YES_NO_OPTION)
						== JOptionPane.YES_OPTION)
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						outputWindow.saveData(file);
					}
				}
				catch (IOException ex)
				{
					MyTools.showException(ex);
				}
				finally
				{
					setCursor(Cursor.getDefaultCursor());
					FILE_CHOOSER.setFileFilter(FILE_CHOOSER.getAcceptAllFileFilter());
					FILE_CHOOSER.removeChoosableFileFilter(XLS_FILTER);
				}
			}
		});
		fileMenu.add(saveDataCmd);
		
		fileMenu.add(new JSeparator());
		
		importLibCmd = new JMenuItem("Import Library ...");
		importLibCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				/* eine neue Bibliothek (Text-File) einlesen */
				flushMatrixProc();
				selectAndReadLibrary();
			}
		});
		fileMenu.add(importLibCmd);
		
		exportLibCmd = new JMenuItem("Export Library ...");
		exportLibCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				/* eine Bibliothek auslesen */
				flushMatrixProc();
				exportLibrary();
			}
		});
		fileMenu.add(exportLibCmd);
		
		fileMenu.add(new JSeparator());
		
//		pageSetUpCmd = new JMenuItem("Page Setup ...");
//		pageSetUpCmd.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				//				DoPageSetUp;
//			}
//		});
//		fileMenu.add(pageSetUpCmd);
		
		printCmd = new JMenuItem("Print Selected Window ...");
		printCmd.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_P,MENU_SHORTCUT_KEY));
		printCmd.setEnabled(false);
		printCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				JInternalFrame selected = desktopPane.getSelectedFrame();
				if (selected == null)
					MyTools.showError("No Window selected for printing!");
//				else
//TODO					PrintUtilities.printComponent(selected);
				//				DOBildDrucken;
			}
		});
		fileMenu.add(printCmd);
		
		fileMenu.add(new JSeparator());
		
		settingsCmd = new JMenuItem("Settings ...");
		settingsCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				settingsDialog.setVisible(true);
			}
		});
		fileMenu.add(settingsCmd);
		
		iterationParamsCmd = new JMenuItem("Iteration Parameters ...");
		iterationParamsCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				iterationParametersDialog.setVisible(true);
			}
		});
		fileMenu.add(iterationParamsCmd);
		
		if (!IS_MAC_OSX)
		{
			fileMenu.add(new JSeparator());

			quitCmd = new JMenuItem("Quit");
			quitCmd.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent. VK_Q,MENU_SHORTCUT_KEY));
			quitCmd.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					exitApplication();
				}
			});
			fileMenu.add(quitCmd);
		}
		menuBar.add(fileMenu);
		
		// ----- library menu -----
		libraryMenu = new JMenu("Libraries");

		editRegLibComponentsCmd = new JMenuItem("Edit Regular Lib. Components ...");
		editRegLibComponentsCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (regLibrary == null)
					regLibrary = Library.readBinLibrary(ChemEQL3.this,true);
				if (regLibrary == null) return;// Error while reading library: Exit!
				EditComponentsDialog.getInstance(ChemEQL3.this).show(regLibrary);
			}
		});
		libraryMenu.add(editRegLibComponentsCmd);
		
		editRegLibSpeciesCmd = new JMenuItem("Edit Regular Lib. Species ...");
		editRegLibSpeciesCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (regLibrary == null)
					regLibrary = Library.readBinLibrary(ChemEQL3.this,true);
				if (regLibrary == null) return;// Error while reading library: Exit!
				EditSpeciesDialog.getInstance(ChemEQL3.this).show(regLibrary);
			}
		});
		libraryMenu.add(editRegLibSpeciesCmd);
		
		libraryMenu.add(new JSeparator());

		editSPLibComponentsCmd = new JMenuItem("Edit Solid Phases Lib. Components ...");
		editSPLibComponentsCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (spLibrary == null)
					spLibrary = Library.readBinLibrary(ChemEQL3.this,false);
				if (spLibrary == null) return;// Error while reading library: Exit!
				EditComponentsDialog.getInstance(ChemEQL3.this).show(spLibrary);
			}
		});
		libraryMenu.add(editSPLibComponentsCmd);
		
		editSPLibSpeciesCmd = new JMenuItem("Edit Solid Phases Lib. Species ...");
		editSPLibSpeciesCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (spLibrary == null)
					spLibrary = Library.readBinLibrary(ChemEQL3.this,false);
				if (spLibrary == null) return;// Error while reading library: Exit!
				EditSpeciesDialog.getInstance(ChemEQL3.this).show(spLibrary);
			}
		});
		libraryMenu.add(editSPLibSpeciesCmd);
		
		menuBar.add(libraryMenu);
		
		// ----- matrix menu -----
		matrixMenu = new JMenu("Matrix");
		matrixMenu.setEnabled(false);
		
		fileInfoCmd = new JMenuItem("Show current matrix");
		fileInfoCmd.setEnabled(false);
		fileInfoCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				fileInfoCmd.setEnabled(false);
				drawFileInfoWindow(false);
			}
		});
		matrixMenu.add(fileInfoCmd);
		
		matrixMenu.add(new JSeparator());
		
		replaceHbyOHCmd = new JMenuItem("Replace H+ by OH-");
		replaceHbyOHCmd.setEnabled(false);
		replaceHbyOHCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				matrix.replaceHbyOHProc();
				drawFileInfoWindow(true);
			}
		});
		matrixMenu.add(replaceHbyOHCmd);
		
		deleteSpeciesCmd = new JMenuItem("Delete Species ...");
		deleteSpeciesCmd.setEnabled(false);
		deleteSpeciesCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				DeleteSpeciesDialog.getInstance(ChemEQL3.this).setVisible(true);
				drawFileInfoWindow(true);
			}
		});
		matrixMenu.add(deleteSpeciesCmd);
		matrixMenu.add(new JSeparator());
		
		solidPhasesCmd = new JMenuItem("Insert Solid Phase ...");
		solidPhasesCmd.setEnabled(false);
		solidPhasesCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				ChangeSolidPhaseDialog.getInstance(ChemEQL3.this).setVisible(true);
				drawFileInfoWindow(true);
			}
		});
		matrixMenu.add(solidPhasesCmd);
		
		menuBar.add(matrixMenu);
		
		// ----- options menu -----
		modeMenu = new JMenu("Options");
		modeMenu.setEnabled(false);
		
		restoreCmd = new JMenuItem("Restore");
		restoreCmd.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_R,MENU_SHORTCUT_KEY));
		restoreCmd.setEnabled(false);
		restoreCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				restoreProc();
			}
		});
		modeMenu.add(restoreCmd);
		
		modeMenu.add(new JSeparator());
		
		pHconstCmd = new JMenuItem("Change pH ...");
		pHconstCmd.setEnabled(false);
		pHconstCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				PHConstDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		});
		modeMenu.add(pHconstCmd);
		
		pHrangeCmd = new JMenuItem("pH range ...");
		pHrangeCmd.setEnabled(false);
		pHrangeCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				PHRangeDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		});
		modeMenu.add(pHrangeCmd);
		
		compRangeCmd = new JMenuItem("Component range ...");
		compRangeCmd.setEnabled(false);
		compRangeCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				ComponentRangeDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		});
		modeMenu.add(compRangeCmd);
		
		adsRangeCmd = new JMenuItem("Adsorption range ..."); /* titration with particles */
		adsRangeCmd.setEnabled(false);
		adsRangeCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				AdsorptionRangeDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		});
		
		modeMenu.add(adsRangeCmd);
		
		logKrangeCmd = new JMenuItem("log K range ...");
		logKrangeCmd.setEnabled(false);
		logKrangeCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				LogKRangeDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		});
		modeMenu.add(logKrangeCmd);
		
		modeMenu.add(new JSeparator());
		
		kinetikCmd = new JMenuItem("Kinetics ...");
		kinetikCmd.setEnabled(false);
		kinetikCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				KineticsDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		});
		modeMenu.add(kinetikCmd);
		
		modeMenu.add(new JSeparator());
		
		pXpYCmd = new JMenuItem("pX-pY Diagram ...");
		pXpYCmd.setEnabled(false);
		pXpYCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				PxPyDiagramDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		});
		modeMenu.add(pXpYCmd);
		
		modeMenu.add(new JSeparator());
		
		formatMenu = new JMenu("Format");
		
		subFormLin = new JRadioButtonMenuItem("Linear");
		subFormLin.setModel(NumFormatEnum.LINEAR);
		subFormLin.setSelected(true);
		numFormat.add(subFormLin);
		formatMenu.add(subFormLin);
		
		subFormLog = new JRadioButtonMenuItem("Logarithmic");
		subFormLog.setModel(NumFormatEnum.LOGARITHMIC);
		numFormat.add(subFormLog);
		formatMenu.add(subFormLog);
		
		modeMenu.add(formatMenu);
		
		modeMenu.add(new JSeparator());
		
		activitiesMenu = new JMenu("Activity");
		ActionListener openActivityInputDialog = new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				ActivityInputDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		};
		
		noCmd = new JRadioButtonMenuItem("no");
		noCmd.setModel(ActivityEnum.NO);
		noCmd.setEnabled(false);
		noCmd.setSelected(true);
		calcActivCoeff.add(noCmd);
		activitiesMenu.add(noCmd);
		
		activitiesMenu.add(new JSeparator());
		
		debyeHueckelCmd = new JRadioButtonMenuItem("Debye-Hückel ...");
		debyeHueckelCmd.setModel(ActivityEnum.DEBYEHUECKEL);
		debyeHueckelCmd.setEnabled(false);
		calcActivCoeff.add(debyeHueckelCmd);
		debyeHueckelCmd.addActionListener(openActivityInputDialog);
		activitiesMenu.add(debyeHueckelCmd);
		
		guentelbergCmd = new JRadioButtonMenuItem("Güntelberg ...");
		guentelbergCmd.setModel(ActivityEnum.GUENTELBERG);
		guentelbergCmd.setEnabled(false);
		calcActivCoeff.add(guentelbergCmd);
		guentelbergCmd.addActionListener(openActivityInputDialog);
		activitiesMenu.add(guentelbergCmd);
		
		daviesCmd = new JRadioButtonMenuItem("Davies ...");
		daviesCmd.setModel(ActivityEnum.DAVIES);
		daviesCmd.setEnabled(false);
		calcActivCoeff.add(daviesCmd);
		daviesCmd.addActionListener(openActivityInputDialog);
		activitiesMenu.add(daviesCmd);
		
		activitiesMenu.add(new JSeparator());
		
		activityInfoCmd = new JMenuItem("Info ...");
		activityInfoCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				JTextPane tp = new JTextPane();
				tp.setEditable(false);
				try
				{
					tp.setPage(ChemEQL3.class.getResource("resources/activityHelp.html"));
				}
				catch (IOException ex)
				{ex.printStackTrace();}
				JScrollPane scroller = new JScrollPane(tp,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				
				JOptionPane pane = new JOptionPane(scroller,
				JOptionPane.INFORMATION_MESSAGE);
				JInternalFrame dialog = pane.createInternalFrame(
				desktopPane,"Activity Info");
				dialog.setResizable(true);
				dialog.setBounds(10,40,600,350);
				dialog.setVisible(true);
				activityInfoCmd.setEnabled(false);
				dialog.addInternalFrameListener(new InternalFrameAdapter()
				{
					public void internalFrameClosed(InternalFrameEvent e)
					{
						activityInfoCmd.setEnabled(true);
					}
				});
			}
		});
		activitiesMenu.add(activityInfoCmd);
		
		modeMenu.add(activitiesMenu);
		
		modeMenu.add(new JSeparator());
		
		graphCmd = new JMenuItem("Graphics ...");
		graphCmd.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_D,MENU_SHORTCUT_KEY));
		graphCmd.setEnabled(false);
		graphCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				GraphicsDialog.getInstance(ChemEQL3.this).setVisible(true);
			}
		});
		modeMenu.add(graphCmd);
		
		menuBar.add(modeMenu);
		
		// ----- run menu -----
		runMenu = new JMenu("Run");
		runMenu.setEnabled(false);
		
		goCmd = new JMenuItem("Go");
		goCmd.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_G,MENU_SHORTCUT_KEY));
		goCmd.setEnabled(false);
		goCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				fileInfoWindow.setVisible(false);
				goCmd.setEnabled(false);
				stopCmd.setEnabled(true);
				
				// Calculation gets its own thread so the GUI will not be blocked
				// and can be updated with higher priority. Also required to allow
				// the user to interrupt calculation via menu. Subsequent actions
				// are included in the thread to be executed after calculation has
				// finished
				calculationThread = new Thread(new Runnable()
				{
					public void run()
					{
						try
						{matrix.runProc();}
						catch (CalculationStoppedException ex)
						{
							;
						}
						catch (final Exception ex)
						{
							try
							{
								SwingUtilities.invokeAndWait(new Runnable()
								{
									public void run()
									{
										MyTools.showException(ex);
									}
								});
							}
							catch (InvocationTargetException e)
							{}
							catch (InterruptedException e)
							{}
						}
						finally
						{
							restoreProc();
							goCmd.setEnabled(true);
							stopCmd.setEnabled(false);
							saveDataCmd.setEnabled(true);
							restoreCmd.setEnabled(true);
							fileInfoCmd.setEnabled(true);
						}
					}
				},AN+"-Calculation");
				calculationThread.setPriority(
				Math.max(Thread.MIN_PRIORITY,Thread.currentThread().getPriority()-1));
				calculationThread.start();
			}
		});
		runMenu.add(goCmd);
		
		stopCmd = new JMenuItem("Stop");
		stopCmd.setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,MENU_SHORTCUT_KEY));
		stopCmd.setEnabled(false);
		stopCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				stopCmd.setEnabled(false);
				runMenu.setEnabled(false);
			}
		});
		runMenu.add(stopCmd);
		
		menuBar.add(runMenu);
		
		// ----- about menu -----
		if (!IS_MAC_OSX)
		{
			JMenu aboutMenu = new JMenu("About");
			aboutCmd = new JMenuItem("About "+AN+" ...");
			aboutCmd.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					showAboutScreen();
				}
			});
			aboutMenu.add(aboutCmd);

			menuBar.add(aboutMenu);
		}

		return menuBar;
	} // end of createMenu()

	void showAboutScreen()
	{
		new SplashScreen(ChemEQL3.class.getResource("resources/about.gif"), this, 20000);
	}
}
