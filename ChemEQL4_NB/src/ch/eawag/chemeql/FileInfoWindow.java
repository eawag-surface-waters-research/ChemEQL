package ch.eawag.chemeql;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;


class FileInfoWindow extends JInternalFrame 
{

	private Matrix matrix;
	private JTable componentsTable;
	private JTable adsorptionParametersTable;
	private JTextField multiCalculationTF;
	private JTextField pHTF;
	private JTable speciesTable;

	FileInfoWindow(Main main, Matrix matrix)
	{
		super("",true,true,true,true);
		this.matrix = matrix;
		setTitle(matrix.toString());
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

		// create components
		componentsTable = new ComponentTable(
			main,matrix.getComponentsTableModel());
		adsorptionParametersTable = new AdsorptionParamsTable(
			matrix.getAdsorptionTableModel());
		multiCalculationTF = new JTextField("-> Calculation with an array of concentrations");
		multiCalculationTF.setOpaque(false);
		Dimension prefTextboxSize =
			new Dimension(Integer.MAX_VALUE,multiCalculationTF.getMinimumSize().height);
		multiCalculationTF.setMaximumSize(prefTextboxSize);
		multiCalculationTF.setBorder(BorderFactory.createEmptyBorder(6,0,6,0));
		pHTF = new JTextField("pHTF");
		pHTF.setOpaque(false);
		pHTF.setEditable(false);
		pHTF.setMaximumSize(prefTextboxSize);
		pHTF.setBorder(BorderFactory.createEmptyBorder(6,0,6,0));
		speciesTable = new SpeciesTable(main,matrix.getSpeciesTableModel());

		// layout components
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[]{1.0f};
		JPanel panel = new JPanel(gbl);
		panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.fill = gbc.HORIZONTAL;
		panel.add(componentsTable,gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		gbc.fill = gbc.HORIZONTAL;
		gbc.insets = new Insets(25,8,15,0);
		panel.add(adsorptionParametersTable,gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 2;
		gbc.fill = gbc.HORIZONTAL;
		gbc.insets = new Insets(10,8,0,0);
		panel.add(multiCalculationTF,gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 3;
		gbc.fill = gbc.HORIZONTAL;
		gbc.insets = new Insets(10,8,0,0);
		panel.add(pHTF,gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 4;
		gbc.fill = gbc.HORIZONTAL;
		gbc.insets = new Insets(10,0,0,0);
		panel.add(speciesTable,gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 5;
		gbc.fill = gbc.VERTICAL;
		gbc.weighty = 1.0;
		panel.add(new JPanel(),gbc);

		JScrollPane scroller = new JScrollPane(panel);
		scroller.setOpaque(false);
		scroller.getViewport().setOpaque(false);
		getContentPane().add(scroller,BorderLayout.CENTER);
	}


	void update()
	{
		((AbstractTableModel)componentsTable.getModel()).fireTableStructureChanged();
		TableColumnModel cm = componentsTable.getColumnModel();
		for (int i=0; i<cm.getColumnCount(); i++)
			setColumnWidths(cm.getColumn(i),60,150);

		if (matrix.adsorption())
		{
			((AbstractTableModel)adsorptionParametersTable.getModel()).fireTableStructureChanged();
			cm = adsorptionParametersTable.getColumnModel();
			setColumnWidths(cm.getColumn(0),180,180);
			setColumnWidths(cm.getColumn(1),90,90);
			for (int i=2; i<cm.getColumnCount(); i++)
				setColumnWidths(cm.getColumn(i),60,60);
		}
		adsorptionParametersTable.setVisible(matrix.adsorption());

		multiCalculationTF.setVisible(matrix.multiConc > 1);

		if (matrix.isHorHplusAndFree())
		{
			double conc = matrix.getMultiConcForLast();
			if (conc == 0.0)
				pHTF.setText("ENTER A CONCENTRATION!");
			else
				pHTF.setText("-> pH is constant: "
					+ MyTools.EXACT_3_DIGITS.format(-MyTools.myLog(conc)));
			pHTF.setVisible(true);
		}
		else
			pHTF.setVisible(false);

		((AbstractTableModel)speciesTable.getModel()).fireTableStructureChanged();
		cm = speciesTable.getColumnModel();
		setColumnWidths(cm.getColumn(0),35,35);
		setColumnWidths(cm.getColumn(1),40,120);
		setColumnWidths(cm.getColumn(2),40,70);
		setColumnWidths(cm.getColumn(3),40,100);
	}

	private void setColumnWidths(TableColumn col, int min, int max)
	{
		col.setPreferredWidth(min);
		col.setMaxWidth(max);
	}

	// --- custom table for component data

	static class ComponentTable extends MyTable
	{
		private TableCellRenderer componentRenderer;
		private TableCellEditor componentEditor;
		private TableCellRenderer modeRenderer;
		private TableCellEditor modeEditorTotalFree;
		private TableCellEditor modeEditorPhaseCheckPrecip;
		private TableCellRenderer concentrationRenderer;
		private TableCellEditor concentrationEditor;
		
		ComponentTable(Main main, TableModel model)
		{
			super(model,false);
			componentRenderer = new CustomRenderer(
				getFont().deriveFont(Font.BOLD),SwingConstants.CENTER);
			componentEditor = new CustomTextEditor(SwingConstants.CENTER);
			modeRenderer = new CustomRenderer(
				getFont().deriveFont(Font.ITALIC),SwingConstants.CENTER);
			modeEditorTotalFree = new DefaultCellEditor(
				new JComboBox(new Mode[]{Mode.TOTAL,Mode.FREE}));
			modeEditorPhaseCheckPrecip = new DefaultCellEditor(
				new JComboBox(new Mode[]{Mode.SOLID_PHASE,Mode.CHECKPRECIP}));
			concentrationRenderer = new CustomConcentrationsRenderer(
				getFont(),SwingConstants.CENTER,main);
			concentrationEditor = new CustomDoubleEditor(SwingConstants.CENTER);
		}

		public TableCellRenderer getCellRenderer(int row, int col)
		{
			switch (row)
			{
				case 0: return componentRenderer;
				case 1: return modeRenderer;
				case 2: return concentrationRenderer;
				default: return null;
			}
		}
		
		public boolean isCellEditable(int row, int col)
		{
			return true;
		}

		public TableCellEditor getCellEditor(int row, int col)
		{
			switch (row)
			{
				case 0: return componentEditor;
				case 1:
					if (this.getValueAt(row,col) == Mode.TOTAL
						|| this.getValueAt(row,col) == Mode.FREE)
						return modeEditorTotalFree;
					if (this.getValueAt(row,col) == Mode.SOLID_PHASE
						|| this.getValueAt(row,col) == Mode.CHECKPRECIP)
						return modeEditorPhaseCheckPrecip;
					return null;	// component mode is not editable
				case 2: return concentrationEditor;
				default: return null;
			}
		}
	}


	// --- custom table for adsorption parameters

	static class AdsorptionParamsTable extends MyTable
	{
		private TableCellRenderer nameRenderer;
		private TableCellRenderer digits2Renderer;
		private TableCellRenderer digits3Renderer;

		AdsorptionParamsTable(TableModel model)
		{
			super(model,false);
			nameRenderer = new CustomRenderer(getFont().deriveFont(Font.BOLD));
			digits2Renderer = new CustomNumberRenderer(getFont(),SwingConstants.RIGHT,2);
			digits3Renderer = new CustomNumberRenderer(getFont(),SwingConstants.RIGHT,3);
		}

		public TableCellRenderer getCellRenderer(int row, int col)
		{
			if (col == 0)
				return nameRenderer;
			switch (row)
			{
				case 0: return stringRenderer;
				case 1: return digits2Renderer;
				case 2: return rightAlignRenderer;
				case 3: return digits3Renderer;
				case 4: return col == 1 ? digits3Renderer : stringRenderer;
				case 5: return digits2Renderer;
				case 6: return digits2Renderer;
				default: return null;
			}
		}
	
		public boolean isCellEditable(int row, int col)
		{
			return row > 0 && col > 0 && !(row == 4 && col > 1);
		}

		public TableCellEditor getCellEditor(int row, int col)
		{
			return new CustomDoubleEditor(SwingConstants.RIGHT);
		}
	}


	// --- custom table for species data

	static class SpeciesTable extends MyTable
	{
		private TableCellRenderer logKRenderer;
		private TableCellEditor logKEditor;
		private TableCellRenderer logKSourceRenderer;

		SpeciesTable(Main main, TableModel model)
		{
			super(model,false);
			logKRenderer = new CustomConstantsRenderer(getFont(),SwingConstants.RIGHT,main);
			logKEditor = new CustomDoubleEditor(SwingConstants.RIGHT);
			logKSourceRenderer = new CustomRenderer(getFont().deriveFont(10.0f));
		}

		public TableCellRenderer getCellRenderer(int row, int col)
		{
			switch (col)
			{
				case 0: return rightAlignRenderer;
				case 1: return stringRenderer;
				case 2: return logKRenderer;
				case 3: return logKSourceRenderer;
				default: return null;
			}
		}
		
		public boolean isCellEditable(int row, int col)
		{
			return col == 2;
		}

		public TableCellEditor getCellEditor(int row, int col)
		{
			if (col == 2)
				return logKEditor;
			return null;
		}
	}
}