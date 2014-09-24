package ch.eawag.chemeql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;


public class SplashScreen extends JWindow
{
	SplashScreen(URL graphicsFile, Frame owner, int waitTime)
	{
		super(owner);
		JLabel l = new JLabel(new ImageIcon(graphicsFile));
		l.setBorder(BorderFactory.createRaisedBevelBorder());
		getContentPane().add(l,BorderLayout.CENTER);
		pack();
		Dimension ownerSize = new Dimension(800, 500); //		Dimension ownerSize = owner.getSize();
		Dimension labelSize = l.getPreferredSize();
		setLocation(ownerSize.width/2 - (labelSize.width/2),
			ownerSize.height/2 - (labelSize.height/2));
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				setVisible(false);
				dispose();
			}
		});
		final int pause = waitTime;
		final Runnable closerRunner = new Runnable()
		{
			public void run()
			{
				setVisible(false);
				dispose();
			}
		};
		Runnable waitRunner = new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(pause);
					SwingUtilities.invokeAndWait(closerRunner);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					// can catch InvocationTargetException
					// can catch InterruptedException
				}
			}
		};
		setVisible(true);
		Thread splashThread = new Thread(waitRunner, "SplashThread");
		splashThread.start();
	}
}