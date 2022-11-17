/*
 * Created by JFormDesigner on Thu Oct 27 09:38:15 BST 2011
 */

package uk.ac.ebi.pride.gui.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.archive.submission.model.File.ProjectFileList;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.ResubmissionFileChangeState;
import uk.ac.ebi.pride.gui.form.action.AddFileSelectionAction;
import uk.ac.ebi.pride.gui.form.table.model.*;
import uk.ac.ebi.pride.gui.form.table.TableFactory;
//import uk.ac.ebi.pride.gui.form.table.model.TargetFileNewResubmissionTableModel;
import uk.ac.ebi.pride.gui.util.ValidationState;
import uk.ac.ebi.pride.toolsuite.gui.GUIUtilities;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskEvent;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * This form is responsible for adding resubmission files
 *
 * @author Rui Wang
 * @version $Id$
 */
public class FileResubmissionForm extends Form implements TaskListener<ProjectFileList, Void> {
    private static final Logger logger = LoggerFactory.getLogger(FileResubmissionForm.class);
    private static final float DEFAULT_TITLE_FONT_SIZE = 15f;
    private static final float DEFAULT_BUTTON_FONT_SIZE = 14f;
    private static final int DEFAULT_BUTTON_WIDTH = 120;
    private static final int DEFAULT_BUTTON_HEIGHT = 40;

    /**
     * Existing file table
     */
    private JTable existingFileTable;

    /**
     * Existing file table model
     */
    private ExistingFilesResubmissionTableModel existingFileTableModel;
    /**
     * NewResubmission file table
     */
    private JTable newResubmissionFileTable;

    /**
     * NewResubmission file table model
     */
    private ResubmissionFileSelectionTableModel resubmissionFileSelectionTableModel;

    FileResubmissionForm() {
        initComponents();
    }

    private void initComponents() {
        // setup the main panel
        this.setLayout(new GridLayout(2, 1, 0, 20));

        // ======================= New Resubmission Files =======================

        // new file panel
        JPanel newResubmissionFilePanel = new JPanel(new BorderLayout());

        // new resubmission file top panel
        JPanel newResubmissionFileTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // new file label
        JLabel newFileLabel = new JLabel(appContext.getProperty("resubmission.new.files.label.title"));
        newFileLabel.setFont(newFileLabel.getFont().deriveFont(DEFAULT_TITLE_FONT_SIZE));
        JLabel sampleMetaDataDescLabel = new JLabel(appContext.getProperty("resubmission.new.files.description.title"));
        sampleMetaDataDescLabel.setForeground(Color.gray);

        newResubmissionFileTopPanel.add(newFileLabel);
        newResubmissionFileTopPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        newResubmissionFileTopPanel.add(sampleMetaDataDescLabel);
        newResubmissionFilePanel.add(newResubmissionFileTopPanel, BorderLayout.NORTH);

        // refresh Button
        JButton refreshButton = new JButton(new ResetExistingFileResubmissionAction());
        refreshButton.setFont(refreshButton.getFont().deriveFont(DEFAULT_BUTTON_FONT_SIZE));
        refreshButton.setPreferredSize(new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        newResubmissionFileTopPanel.add(refreshButton,BorderLayout.NORTH);

        // add File Button
        JButton addFilesButton = new JButton(new AddFileSelectionAction());
        addFilesButton.setFont(refreshButton.getFont().deriveFont(DEFAULT_BUTTON_FONT_SIZE));
        addFilesButton.setPreferredSize(new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT));
        newResubmissionFileTopPanel.add(addFilesButton,BorderLayout.NORTH);

        //newResubmissionFileTable table
        newResubmissionFileTable = TableFactory.createFileNewResubmissionTable();

        // mapping file table model
        resubmissionFileSelectionTableModel = (ResubmissionFileSelectionTableModel) newResubmissionFileTable.getModel();

        //scroll pane
        JScrollPane mappingFileScrollPane = new JScrollPane(newResubmissionFileTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        newResubmissionFilePanel.add(mappingFileScrollPane, BorderLayout.CENTER);

        this.add(newResubmissionFilePanel);

        // ======================= Existing Files =======================

        // existing file panel
        JPanel existingFilePanel = new JPanel(new BorderLayout());

        // existing file top panel
        JPanel existingFileTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // existing file label
        JLabel existingFileLabel = new JLabel(appContext.getProperty("resubmission.existing.files.label.title"));
        existingFileLabel.setFont(existingFileLabel.getFont().deriveFont(DEFAULT_TITLE_FONT_SIZE));
        JLabel existingFileDescLabel = new JLabel(appContext.getProperty("resubmission.existing.files.description.title"));
        existingFileDescLabel.setForeground(Color.gray);

        existingFileTopPanel.add(existingFileLabel);
        existingFileTopPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        existingFileTopPanel.add(existingFileDescLabel);
        existingFilePanel.add(existingFileTopPanel, BorderLayout.NORTH);


        // existing file table
        existingFileTable = TableFactory.createExistingFilesResubmissionTable();

        // existing file table model
        existingFileTableModel = (ExistingFilesResubmissionTableModel) existingFileTable.getModel();

        // scroll pane
        JScrollPane existingFileScrollPane = new JScrollPane(existingFileTable,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        existingFilePanel.add(existingFileScrollPane, BorderLayout.CENTER);

        this.add(existingFilePanel);
    }

    /**
     * Get the warning balloon tip
     */
    public void hideWarnings() {
        if (warningBalloonTip != null && warningBalloonTip.isVisible()) {
            warningBalloonTip.closeBalloon();
        }
    }

    private void showWarnings() {
        warningBalloonTip.setVisible(true);
        this.revalidate();
        this.repaint();
    }

    @Override
    public void started(TaskEvent<Void> taskEvent) {

    }

    @Override
    public void process(TaskEvent<List<Void>> taskEvent) {

    }

    @Override
    public void finished(TaskEvent<Void> taskEvent) {

    }

    @Override
    public void failed(TaskEvent<Throwable> taskEvent) {

    }

    @Override
    public void succeed(TaskEvent<ProjectFileList> taskEvent) {

    }

    @Override
    public void cancelled(TaskEvent<Void> taskEvent) {

    }

    @Override
    public void interrupted(TaskEvent<InterruptedException> taskEvent) {

    }

    @Override
    public void progress(TaskEvent<Integer> taskEvent) {

    }

    public class ResetExistingFileResubmissionAction extends AbstractAction {

        public ResetExistingFileResubmissionAction() {
            super(App.getInstance().getDesktopContext().getProperty("resubmission.reset.button.title"),
                    GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("add.file.button.small.icon")));
            this.putValue(SHORT_DESCRIPTION, App.getInstance().getDesktopContext().getProperty("add.file.button.tooltip"));

        }

        @Override
        public void actionPerformed(ActionEvent e) {

            AppContext appContext= (AppContext) App.getInstance().getDesktopContext();
            List<DataFile> previouslySubmittedFiles = appContext.getResubmissionRecord().getResubmission().getDataFiles();
            appContext.getResubmissionRecord().getResubmission().getResubmission().clear();
            appContext.getSubmissionRecord().getSubmission().getDataFiles().clear();
            // remove from newly added files(Submission)
            for (DataFile dataFile: appContext.getSubmissionRecord().getSubmission().getDataFiles()) {
                appContext.removeDatafile(dataFile);
            }
            // update resubmission file summary
            for (DataFile dataFile: previouslySubmittedFiles) {
                appContext.getResubmissionRecord().getResubmission().getResubmission().put(dataFile, ResubmissionFileChangeState.NONE);
            }

            existingFileTable.setModel(existingFileTableModel);
            existingFileTable.repaint();

            newResubmissionFileTable.setModel(resubmissionFileSelectionTableModel);
            newResubmissionFileTable.repaint();
        }
    }

}
