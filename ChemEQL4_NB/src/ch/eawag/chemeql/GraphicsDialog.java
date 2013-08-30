package ch.eawag.chemeql;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


class GraphicsDialog extends ProceedCancelDialog
{
	private static GraphicsDialog INSTANCE;
	static GraphicsDialog getInstance(Main parent)
	{
		if (INSTANCE == null)
			INSTANCE = new GraphicsDialog(parent);
		return INSTANCE;
	}
	
	// Constructor for creating a bean
	public GraphicsDialog()
	{
		initComponents();
	}
	
	private GraphicsDialog(Main parent)
	{
		super(parent);
		initComponents();
		setLocation(400,200);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent arg0)
			{
				speciesList.setModel(main.matrix.createSpeciesNamesModel());
				proceedButton.setEnabled(true);
			}
		});
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
   private void initComponents()//GEN-BEGIN:initComponents
   {
      javax.swing.JLabel label;
      javax.swing.JScrollPane scrollPane;

      jScrollPane1 = new javax.swing.JScrollPane();
      label = new javax.swing.JLabel();
      scrollPane = new javax.swing.JScrollPane();
      speciesList = new javax.swing.JList();

      getControls().setLayout(new java.awt.BorderLayout(0, 6));

      setTitle("Graphics");
      label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      label.setText("Select species for graph:");
      getControls().add(label, java.awt.BorderLayout.NORTH);

      scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setPreferredSize(new java.awt.Dimension(110, 180));
      scrollPane.setViewportView(speciesList);

      getControls().add(scrollPane, java.awt.BorderLayout.CENTER);

      pack();
   }//GEN-END:initComponents
	
	protected void doCancel()
	{
		main.doDrawGraph = false;
		main.drawSpecs = new int[0];
		super.doCancel();
	}
	
	protected void doProceed()
	{
		if (speciesList.getSelectedIndices().length > 10)
			MyTools.showError("Maximum of 10 species can be drawn!");
		else
		{
			main.drawSpecs = speciesList.getSelectedIndices();
			main.doDrawGraph = true;
			super.doProceed();
		}
	}
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JList speciesList;
   // End of variables declaration//GEN-END:variables
}
