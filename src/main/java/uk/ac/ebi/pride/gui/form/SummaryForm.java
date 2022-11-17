package uk.ac.ebi.pride.gui.form;

import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.gui.form.panel.SummaryItemPanel;
import uk.ac.ebi.pride.gui.form.table.TableFactory;
import uk.ac.ebi.pride.gui.navigation.Navigator;
import uk.ac.ebi.pride.gui.util.BorderUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * SummaryForm shows a summary of the submission
 *
 * @author Rui Wang
 * @version $Id$
 */
public class SummaryForm extends Form {
    private static final float DEFAULT_TITLE_FONT_SIZE = 15f;

    private JTable summaryTable;
    private JTable resubmissionModifiedTable;

    private JCheckBox jCheckBox = new JCheckBox();

    // use GridLayout with 2 rows and 1 column
    JPanel centerPanel = new JPanel(new GridLayout(2,1));


    public SummaryForm() {
        initComponents();
    }

    private void initComponents() {
        // setup the main pane
        this.setLayout(new BorderLayout());

        // setup submission summary description panel
        initSubmissionSummaryDescPanel();

        // setup submission summary table
        initSubmissionSummaryTable();

//        if(appContext.isResubmission()){
            initResubmissionModTable();
//        }

        this.add(centerPanel, BorderLayout.CENTER);

        // set up accept license checkbox
        initDatasetLicenseAcceptCheckbox();
    }

    /**
     * Initialize experiment details panel
     */
    private void initSubmissionSummaryDescPanel() {
        // summary description item panel
        JPanel summaryItemPanel = new SummaryItemPanel();
        summaryItemPanel.setBorder(BorderUtil.createLoweredBorder());

        this.add(summaryItemPanel, BorderLayout.NORTH);
    }


    /**
     * Initialize submission summary table
     */
    private void initSubmissionSummaryTable() {

        // new file top panel
        JPanel newFileTopPanel = new JPanel(new BorderLayout());

        // New file label
        JLabel newFileLabel = new JLabel(appContext.getProperty("resubmission.new.files.label.title"));
        newFileTopPanel.setFont(newFileTopPanel.getFont().deriveFont(DEFAULT_TITLE_FONT_SIZE));

        newFileTopPanel.add(newFileLabel, BorderLayout.NORTH);

        summaryTable = TableFactory.createSubmissionSummaryTable();

        JScrollPane scrollPane = new JScrollPane(summaryTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        newFileTopPanel.add(scrollPane,  BorderLayout.CENTER);
        centerPanel.add(newFileTopPanel);
    }

    /**
     * Initialize submission summary table
     */
    private void initResubmissionModTable() {

        // existing file top panel
        JPanel existingFileTopPanel = new JPanel(new BorderLayout());

        // existing file label
        JLabel existingFileLabel = new JLabel(appContext.getProperty("resubmission.existing.files.label.title"));
        existingFileLabel.setFont(existingFileLabel.getFont().deriveFont(DEFAULT_TITLE_FONT_SIZE));

        existingFileTopPanel.add(existingFileLabel, BorderLayout.NORTH);
//        existingFileTopPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        resubmissionModifiedTable = TableFactory.createExistingFilesResubmissionTable();
        resubmissionModifiedTable.setShowGrid(false);
        resubmissionModifiedTable.setShowHorizontalLines(false);
        resubmissionModifiedTable.setShowVerticalLines(false);
//        this.repaint();

        JScrollPane scrollPane = new JScrollPane(resubmissionModifiedTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        existingFileTopPanel.add(scrollPane,  BorderLayout.CENTER);
        centerPanel.add(existingFileTopPanel);
    }

    public void initDatasetLicenseAcceptCheckbox() {

        Navigator navigator = ((App) App.getInstance()).getNavigator();
        JButton nextButton = navigator.getNextButton();

        jCheckBox.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                nextButton.setEnabled(true);
            }else {
                nextButton.setEnabled(false);
            }
        });

        // html content
        JEditorPane editorPane = new JEditorPane("text/html", appContext.getProperty("summary.dataset.accept.license.text"));
        editorPane.addHyperlinkListener(
                e -> {
                    try {
                        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                            Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        // couldn't display error message
                    }
                });
        editorPane.setEditable(false);
        editorPane.setBackground(new JLabel().getBackground());

        JPanel jPanel = new JPanel();
        jPanel.add(jCheckBox);
        jPanel.add(editorPane);
        this.add(jPanel,BorderLayout.SOUTH);
    }

    public JCheckBox getJCheckBox() {
        return jCheckBox;
    }
}
