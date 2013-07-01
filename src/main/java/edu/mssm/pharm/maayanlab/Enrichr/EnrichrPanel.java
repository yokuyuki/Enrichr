package edu.mssm.pharm.maayanlab.Enrichr;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentCategory;
import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentLibrary;
import edu.mssm.pharm.maayanlab.common.bio.EnrichedTerm;
import edu.mssm.pharm.maayanlab.common.core.FileUtils;
import edu.mssm.pharm.maayanlab.common.core.SettingsChanger;
import edu.mssm.pharm.maayanlab.common.swing.FileDrop;
import edu.mssm.pharm.maayanlab.common.swing.UIUtils;

public class EnrichrPanel extends JPanel {

	static Logger log = Logger.getLogger(EnrichrPanel.class.getSimpleName());
	
	private static final long serialVersionUID = 7312294713338308079L;
	
	// L2N process holder to call from nested class
	private EnrichrBatcher app;
	
	// Tree for tree pane
	private JTree tree;
	
	// JPanels
	private JPanel panel;
	
	// UI elements
	private JFileChooser openChooser, saveChooser;
	private JTextField openPath, savePath;
	private JTextArea inputTextArea;
	private JButton viewButton, runButton;
	private ProgressMonitor progressMonitor;	// progress bar
	
	// Checkboxes for the different databases
	private ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	
	// Formatter for scientific notation
	private final DecimalFormat scientificNotation = new DecimalFormat("0.##E0");
	
	private String output;	// output string
	
	public static void main(String[] args) {

		if (args.length == 0) {			
			// Schedule a job for the EDT
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					createAndShowGUI();
				}
			});
		}
		else{
			EnrichrBatcher.main(args);
		}
	}
	
	private static void createAndShowGUI() {
		// Try to use Nimbus look and feel
		try {            
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
           log.warning("Nimbus: " + e);
        }
        
        // Create and set up the window
        JFrame appFrame = new JFrame("Enrichr");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Add content to the window
        EnrichrPanel appPanel = new EnrichrPanel();
        appFrame.setContentPane(appPanel);
        
        // Display the window
        appFrame.setResizable(false);
        appFrame.pack();
        appFrame.setVisible(true);
	}
	
	public EnrichrPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		if (!Boolean.getBoolean("verbose"))
            log.setLevel(Level.WARNING);
		
		// Attach instance to variable so nested classes can reference it
		panel = this;
		
		// File choosers
		openChooser = new JFileChooser(System.getProperty("user.dir"));
		openChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = openChooser.getSelectedFile();
				if (file.canRead() && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
					setupIO(file);
			}
		});
		saveChooser = new JFileChooser(System.getProperty("user.dir"));
		saveChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = saveChooser.getSelectedFile();
				if (file != null && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
					if (!file.getName().endsWith(".xml")) {
						file = new File(file.getAbsolutePath() + ".xml");
						saveChooser.setSelectedFile(file);
					}
					
					savePath.setText(file.getAbsolutePath());
				}
			}
		});
		
		// Select input/output file button
		JButton openFileButton = new JButton("Input Genes");
		openFileButton.setPreferredSize(new Dimension(300, 30));
		openFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openChooser.showOpenDialog(panel);
			}			
		});
		JButton saveFileButton = new JButton("Output Summary");
		saveFileButton.setPreferredSize(new Dimension(300, 30));
		saveFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveChooser.showSaveDialog(panel);
			}			
		});
		
		// Text fields
		openPath = new JTextField();
		savePath = new JTextField();
		
		// File Drop
		new FileDrop(openPath, new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				if (files[0].canRead()) {
					setupIO(files[0]);
					openChooser.setSelectedFile(files[0]);
				}
			}
		});
		
		// Tree
		tree = new JTree(new DefaultMutableTreeNode("Ready to enrich!"));
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		
		// Scroll panes
		inputTextArea = new JTextArea(20, 20);
		JScrollPane inputTextPane = new JScrollPane(inputTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		inputTextPane.setPreferredSize(new Dimension(300, 300));
		JScrollPane outputTreePane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		outputTreePane.setPreferredSize(new Dimension(300, 300));
		
		// File Drop
		new FileDrop(inputTextArea, new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				if (files[0].canRead()) {
					setupIO(files[0]);
					openChooser.setSelectedFile(files[0]);
				}
			}
		});
		
		// Start button
		runButton = new JButton("Find Enriched Terms");
		runButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				output = savePath.getText();
				
				try {
					if (!output.equals("") && FileUtils.validateList(UIUtils.getTextAreaText(inputTextArea))) {
						app = new EnrichrBatcher();
						setSettings(app);
						
						DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Currently running enrichment analysis... please wait.");
						DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
						tree.setModel(treeModel);
						tree.setRootVisible(true);
						
						progressMonitor = new ProgressMonitor(panel, "Running Enrichment Analaysis", "", 0, 100);
						progressMonitor.setMillisToDecideToPopup(0);
						progressMonitor.setProgress(0);
						
						final Task task = new Task();
						app.setTask(task);
						task.addPropertyChangeListener(new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								if ("progress" == evt.getPropertyName()) {
									int progress = (Integer) evt.getNewValue();
									progressMonitor.setProgress(progress);								
								}
								else if ("note" == evt.getPropertyName()) {
									String message = (String) evt.getNewValue();
									progressMonitor.setNote(message);
								}
								
								if (progressMonitor.isCanceled()) {
									task.cancel(true);
									app.cancel();
								}
							}
						});
						task.execute();
						runButton.setEnabled(false);
					}
					else {
						JOptionPane.showMessageDialog(panel, "No save location specified.", "No Save Location", JOptionPane.WARNING_MESSAGE);
					}
				} catch (ParseException e1) {
					if (e1.getErrorOffset() == -1)
						JOptionPane.showMessageDialog(panel, "Input list is empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
					else
						JOptionPane.showMessageDialog(panel, e1.getMessage() + " at line " + (e1.getErrorOffset() + 1) +" is not a valid Entrez Gene Symbol.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
				}
			}
			
		});
		
		// View Button
		viewButton = new JButton("View Results");
		viewButton.setEnabled(false);
		viewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(new File(output));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(panel, "Unable to open " + output, "Unable to open file", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		// Advanced Settings
		JPanel bgBox = new JPanel();
		
		JLabel bgLabel = new JLabel("<html>Select<br>enrichment:</html>");
		bgLabel.setHorizontalAlignment(JLabel.LEFT);
		bgBox.add(bgLabel);
		
		JPanel checkboxBox = new JPanel(new GridLayout(0, 3));
		
		for (EnrichmentCategory category : ResourceLoader.getInstance().getCategories()) {
			for (EnrichmentLibrary library : category.getLibraries()) {
				JCheckBox checkbox = new JCheckBox(library.getName().replace("_", " "), true);
				checkboxes.add(checkbox);
				checkboxBox.add(checkbox);
			}
		}
				
		bgBox.add(checkboxBox);
		
		// Input and output box
		JPanel ioBox = new JPanel();
		ioBox.setLayout(new GridLayout(2,2));
		ioBox.add(openFileButton);
		ioBox.add(saveFileButton);
		ioBox.add(openPath);
		ioBox.add(savePath);
		
		// Panes
		JPanel textPanesBox = new JPanel();
		textPanesBox.setLayout(new BoxLayout(textPanesBox, BoxLayout.LINE_AXIS));
		textPanesBox.add(inputTextPane);
		textPanesBox.add(outputTreePane);
		
		// Button box
		JPanel buttonBox = new JPanel();
		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.LINE_AXIS));
		buttonBox.add(runButton);
		buttonBox.add(viewButton);
		
		// Advanced settings box
		JPanel advancedSettingsBox = new JPanel();
		advancedSettingsBox.setLayout(new BoxLayout(advancedSettingsBox, BoxLayout.PAGE_AXIS));
		advancedSettingsBox.setBorder(BorderFactory.createTitledBorder("Advanced Settings"));
		advancedSettingsBox.add(bgBox);
		
		// Add all the panels together
		this.add(ioBox);
		this.add(textPanesBox);
		this.add(Box.createRigidArea(new Dimension(0,10)));
		this.add(buttonBox);
		this.add(advancedSettingsBox);
	}
	
	public void setSettings(SettingsChanger changer) {
		for (JCheckBox checkbox : checkboxes) {
			changer.setSetting(checkbox.getText().replace(" ", "_"), Boolean.toString(checkbox.isSelected()));
		}
	}
	
	public void setInputTextArea(Collection<String> list) {
		UIUtils.setTextAreaText(inputTextArea, list);
	}
	
	// Allow X2K to set output file
	public void setupIO(File inputFile) {
		openPath.setText(inputFile.getAbsolutePath());
		UIUtils.setTextAreaText(inputTextArea, FileUtils.readFile(inputFile));
		
		File outputFile = new File(System.getProperty("user.dir"), FileUtils.stripFileExtension(inputFile.getName()) + ".enrichment.xml");
		saveChooser.setSelectedFile(outputFile);
		savePath.setText(outputFile.getAbsolutePath());
	}
	
	public void updateTree(HashMap<String, ArrayList<EnrichedTerm>> resultsMap) {
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();		
		
		for (String bgType : resultsMap.keySet()) {			
			DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(bgType);
			
			treeModel.insertNodeInto(categoryNode, rootNode, rootNode.getChildCount());
			rootNode.add(categoryNode);
			
			for (EnrichedTerm term : resultsMap.get(bgType)) {
				DefaultMutableTreeNode termNode = new DefaultMutableTreeNode(term.getName() + " (" + scientificNotation.format(term.getPValue()) + ")");
				
				treeModel.insertNodeInto(termNode, categoryNode, categoryNode.getChildCount());
				categoryNode.add(termNode);
			}
		}
	}
	
	class Task extends SwingWorker<Void, Void> {
		@Override
		protected Void doInBackground() throws Exception {
			app.run(UIUtils.getTextAreaText(inputTextArea));
			
			try {
				Thread.sleep(500);
				setProgress(100);
			} catch(InterruptedException e ) { }
			
			return null;
		}
		
		@Override
		public void done() {
			if (isCancelled()) {
				cancelled();
			}
			else {
				// Update tree
				updateTree(app.getEnrichmentResults());
				tree.expandRow(0);
				tree.setRootVisible(false);
				
				// Output
				app.writeFile(output);	// io is expensive so do last
				
				// UI clean up
				runButton.setEnabled(true);
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
					viewButton.setEnabled(true);
			}
		}
		
		private void cancelled() {
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Last run was cancelled.");
			DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
			tree.setModel(treeModel);
			tree.setRootVisible(true);
			
			runButton.setEnabled(true);
		}	
	}
}
