package ch.eawag.chemeql;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


abstract class KeyListeningTextField extends JTextField implements DocumentListener
{
	KeyListeningTextField() {
		super();
		getDocument().addDocumentListener(this);
	}

	public void insertUpdate(DocumentEvent e) {
		keyPressedActivity();
	}

	public void removeUpdate(DocumentEvent e) {
		keyPressedActivity();
	}

	public void changedUpdate(DocumentEvent e) {
	}

	abstract void keyPressedActivity();
}
