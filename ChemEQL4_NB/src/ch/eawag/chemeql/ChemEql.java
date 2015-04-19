package ch.eawag.chemeql;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


/**
 *
 * @author kaibrassel
 */
public class ChemEql extends Application
{
	static {
		// set english locale to get english dialogs and number formats anyway
		// (must happen *before* creating static dialogs and the main window,
		// since dialogs inherit their locale from their parent view, namely the
		// main window.
		Locale.setDefault(Locale.ENGLISH);
	}

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ROOT);
	public static final boolean IS_MAC_OSX = OS_NAME.contains("mac");
	public static final boolean IS_LINUX = OS_NAME.contains("linux");
	public static final boolean IS_WINDOWS = OS_NAME.contains("windows");

	private static Path APP_DATA_DIR = null;

	public static synchronized Path getApplicationDataFolder() {
		if (APP_DATA_DIR == null) {
			if (IS_WINDOWS) {
				APP_DATA_DIR = Paths.get(System.getenv("APPDATA"), "ChemEQL"); //NOI18N
			}
			else if (IS_MAC_OSX) {
				APP_DATA_DIR = Paths.get(System.getProperty("user.home"), "Library/Application Support/", "ChemEQL"); //NOI18N
			}
			else if (IS_LINUX) {
				APP_DATA_DIR = Paths.get(System.getProperty("user.home"), "/.chemeql"); //NOI18N
			}
		}
		assert APP_DATA_DIR != null;
		return APP_DATA_DIR;
	}

	private static final Logger LOGGER = Logger.getLogger("ch.eawag.chemeql", "ch.eawag.chemeql.i18n");

	public static Logger getLogger() {
		return LOGGER;
	}

	public static final ResourceBundle I18N = java.util.ResourceBundle.getBundle("ch.eawag.chemeql.i18n");

	public static String i18n(String key) {
		return I18N.getString(key);
	}

	public static final String APP_TITLE = i18n("application-TITLE");

	/**
	 * Looks up a message from i18n.properties and replaces the given the string parameters.
	 *
	 * @param key to look up
	 * @param params parameters
	 * @return a translated and formatted string for the given key
	 */
	public static String i18n(String key, Object... params) {
		return MessageFormat.format(i18n(key), params);
	}

	public static ChemEql INSTANCE;

	public static ChemEql instance() {
		return INSTANCE;
	}

	public static Stage mainStage() {
		return INSTANCE.getMainStage();
	}

	private Stage mainStage;

	public Stage getMainStage() {
		return mainStage;
	}

	public ChemEql() {
		super();
		INSTANCE = this;
	}

	@Override
	public void start(Stage stage) throws Exception {
		mainStage = stage;
		// set application icon
		mainStage.getIcons().add(new Image("ch/eawag/chemeql/resources/icon.jpg"));
		// put main menu top of the screen for Mac OS X (JDK 1.4+)
		getLogger().config(System.getProperties().toString());
		getLogger().info(i18n("startup-LOG", APP_TITLE));

		Parent root = FXMLLoader.load(getClass().getResource("ChemEqlGui.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add("ch/eawag/chemeql/ChemEqlGui.css");
		mainStage.setScene(scene);
		mainStage.setX(50);
		mainStage.setY(30);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		mainStage.setWidth(screenSize.width - 100);
		mainStage.setWidth(screenSize.height - 60);
		mainStage.setTitle(APP_TITLE);
		mainStage.setOnCloseRequest(windowEvent -> {
			if (quitOk()) {
				System.exit(0); // required to exit AWT
			}
			else {
				windowEvent.consume();
			}
		});
		mainStage.show();

//		FILE_CHOOSER = new JFileChooser(System.getProperty("user.home"));
//		new Main();
	}

	static String appHeader(String commandSymbol) {
		StringBuilder sb = new StringBuilder(ChemEql.APP_TITLE);
		sb.append(ChemEql.i18n("headerDelimiter-DIALOG"));
		sb.append(ChemEql.i18n(commandSymbol));
		return sb.toString();
	}

	public boolean quitOk() {
//		return Dialogs.create().title(APP_TITLE).message(ChemEql.i18n("quit-DIALOG"))
//				.actions(Actions.OK, Actions.CANCEL).showConfirm() == Dialog.Actions.OK;
		return true;	 // TODO confirmation required?
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX
	 * application. main() serves only as fallback in case the
	 * application can not be launched through deployment artifacts,
	 * e.g., in IDEs with limited FX support. NetBeans ignores main().
	 * <p>
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
