package ch.eawag.chemeql;

import java.io.IOException;
import java.io.Reader;


class Tokenizer
{
	static final char TAB = '\t';
	static final char CR = '\r';

	private static String deleteSpaces(String s) {
		int n = s.length();
		StringBuffer myString = new StringBuffer(n);
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			if (c != ' ' && c != TAB) {
				myString.append(c);
			}
		}
		return myString.toString();
	}

	// -------------
	private String item;
	private char delimitingChar;
	private boolean notEOF;
	private Reader inputFile;

	Tokenizer(Reader file) {
		inputFile = file;
	}

	char delimiter() {
		return delimitingChar;
	}

	String nextItem() throws IOException {
		next();
		return item;
	}

	boolean isItemEmpty() {
		return deleteSpaces(item).length() == 0;
	}

	double itemToDouble() {
		return Double.parseDouble(deleteSpaces(item));
	}

	int itemToInteger() {
		return Integer.parseInt(deleteSpaces(item));
	}

	boolean notEOL() throws IOException {
		return delimiter() != CR && notEOF();
	}

	boolean notEOF() throws IOException {
		return notEOF;
	}

	/* eine Zeile überspringen */
	void skipLine() throws IOException {
		do {
			next();
		}
		while (notEOL());
	}

	/*falls dies noch nicht das Zeilenende ist, dann gehe bis zum Zeilenende (->readln)*/
	void skipToEOL() throws IOException {
		while (notEOL()) {
			next();
		}
	}

	// collect all chars until the next TAB, CR, LF or "{" as the next item
	// comments in curly brackets or skipped
	// communicate the char that delimits the item (TAB, CR or LF) using a global
	// variable with LF being encoded as CR
	private void next() throws IOException {
		char inp;
		StringBuffer ss = new StringBuffer();
		do {
			int ch = inputFile.read();
			inp = (char)ch;
			notEOF = ch != -1;
			if (notEOF) {
				if (inp != TAB && inp != CR && inp != '{') {
					ss.append(inp);
				}
				if (inp == '{') /*Bemerkungen überspringen*/ {
					do {
						inp = (char)inputFile.read();
					}
					while (inp != '}' && inp != TAB);
				}
			}
		}
		while (inp != TAB && inp != CR && notEOF);
		item = ss.toString();
		delimitingChar = inp;
	}
}
