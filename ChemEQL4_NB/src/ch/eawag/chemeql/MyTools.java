package ch.eawag.chemeql;

import java.awt.Font;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class MyTools extends Object
{
	public static final boolean IS_MAC_OSX;
	public static final boolean IS_LINUX;
	public static final boolean IS_WINDOWS;

	static {
		String osName = System.getProperty("os.name").toLowerCase();
		IS_MAC_OSX = osName.contains("mac os x");
		IS_LINUX = osName.contains("linux");
		IS_WINDOWS = osName.contains("windows");
		if (!(IS_MAC_OSX || IS_LINUX || IS_WINDOWS)) {
			showError(String.format("Cannot start ChemEQL: Operating System \"%s\" unknown!", osName));
			System.exit(0);
		}
	}

	private static final double LOG10 = Math.log(10);

	static final DecimalFormat EXACT_2_DIGITS = new DecimalFormat("0.00");
	static final DecimalFormat EXACT_3_DIGITS = new DecimalFormat("0.000");
	static final DecimalFormat EXACT_4_DIGITS = new DecimalFormat("0.0000");
	static final DecimalFormat UPTO_3_DIGITS = new DecimalFormat("0.0##");
	static final DecimalFormat UPTO_6_DIGITS = new DecimalFormat("0.0#####");
	static final DecimalFormat SCI_NOTATION = new DecimalFormat("0.0#E0");

	static final double expo(double y, double z) /*y^z*/ {
		if (y <= 0) {
			throw new ArithmeticException(
					"Numeric error: base of exponent <=0. Check your matrix for sense...");
		}
		return Math.pow(y, z);	// was: exp(z * ln(y))
	}

	static final double myLog(double x) {
		if (x <= 0) {
			ArithmeticException ex = new ArithmeticException(
					"Numeric error: log of 0 or a negative number. Check your matrix for sense...");
			showException(ex);
		}
		return Math.log(x) / LOG10;
	}

	static final double mySinh(double x) {
		return (Math.exp(x) - Math.exp(-x)) / 2;
	}

	static final double myCosh(double x) {
		return (Math.exp(x) + Math.exp(-x)) / 2;
	}

	static void showException(Exception ex) {
		ex.printStackTrace();
		showError(ex.toString());
	}

	static void showError(String msg) {
		callAlert(msg, JOptionPane.ERROR_MESSAGE);
	}

	static void showWarning(String msg) {
		callAlert(msg, JOptionPane.WARNING_MESSAGE);
	}

	static void showInfo(String msg) {
		callAlert(msg, JOptionPane.INFORMATION_MESSAGE);
	}

	private static void callAlert(String msg, int msgType) {
		JTextArea ta = new JTextArea(msg, 6, 40);
//		ta.setBackground(null);
		ta.setOpaque(false);
		ta.setBorder(null);
		ta.setEditable(false);
		ta.setFocusable(false);
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		ta.setFont(new Font("sansserif", Font.BOLD, 14));
		JScrollPane c = new JScrollPane(ta,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		c.setBorder(null);
		c.setOpaque(false);
		JOptionPane.showMessageDialog(null, c, ChemEQL3.AN, msgType);
	}

	public static Path getAppDataPath() {
		try {
			if (IS_WINDOWS) {
				return Paths.get(System.getenv("APPDATA"), "ChemEQL");
			}
			else if (IS_MAC_OSX) {
				return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "ChemEQL");
			}
			else if (IS_LINUX) {
				return Paths.get(System.getProperty("user.home"), ".chemeql");
			}
			else {
				// last resort
				return Paths.get("/ChemEQL");
			}
		} catch (Exception ex) {
			// last resort
			return Paths.get("/ChemEQL");
		}
	}
}
