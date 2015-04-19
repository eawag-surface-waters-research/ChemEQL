package ch.eawag.chemeql;

import java.text.DecimalFormat;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class MyTools extends Object
{
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
		callAlert(ex.toString(), false);
	}

	static void showError(String msg) {
		callAlert(msg, false);
	}

	static void showWarning(String msg) {
		callAlert(msg, true);
	}

	private static void callAlert(String msg, boolean isWarning) {
		JTextArea ta = new JTextArea(msg, 4, 30);
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
		if (isWarning) {
			JOptionPane.showMessageDialog(null, c, ChemEql.APP_TITLE + " Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		else {
			JOptionPane.showMessageDialog(null, c, ChemEql.APP_TITLE + " Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
