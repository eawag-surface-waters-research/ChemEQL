package ch.eawag.chemeql;

import com.apple.eawt.*;

public class OSXAdapter extends ApplicationAdapter
{
	private static OSXAdapter theAdapter;
	private static com.apple.eawt.Application theApplication;

	// reference to the app with quit, about, prefs code
	private Main main;

	private OSXAdapter(Main m)
	{
		main = m;
	}
	
	// implemented handler methods. These are basically hooks into existing
	// functionality from the main app, as if it came over from another platform.
	public void handleAbout(ApplicationEvent ae)
	{
		ae.setHandled(true);
		main.showAboutScreen();
	}
	
	public void handlePreferences(ApplicationEvent ae)
	{
//		main.preferences();
		ae.setHandled(true);
	}
	
	public void handleQuit(ApplicationEvent ae)
	{
		//	You MUST setHandled(false) if you want to delay or cancel the quit.
		//	This is important for cross-platform development -- have a universal
		//	quit routine that chooses whether or not to quit, so the
		// functionality is identical on all platforms. This example simply
		// cancels the AppleEvent-based quit and defers to that universal method.
		ae.setHandled(false);
		main.exitApplication();
	}

	// The main entry-point for this functionality. This is the only method
	// that needs to be called at runtime, and it can easily be done using
	// reflection
	public static void registerMacOSXApplication(Main main)
	{
		if (theApplication == null)
			theApplication = new com.apple.eawt.Application();
		if (theAdapter == null)
			theAdapter = new OSXAdapter(main);
		theApplication.addApplicationListener(theAdapter);
	}

	// Another static entry point for EAWT functionality.  Enables the
	// "Preferences..." menu item in the application menu.
	public static void enablePrefs(boolean enabled)
	{
		if (theApplication == null)
			theApplication = new com.apple.eawt.Application();
		theApplication.setEnabledPreferencesMenu(enabled);
	}
}