package uk.ac.ebi.pride.gui.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.data.io.SubmissionFileWriter;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.ResubmissionFileChangeState;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.navigation.Navigator;

import javax.help.HelpBroker;
import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * SummaryDescriptor
 *
 * @author Rui Wang
 * @version $Id$
 */
public class SummaryDescriptor extends ContextAwareNavigationPanelDescriptor {

    private static final Logger logger = LoggerFactory.getLogger(SummaryDescriptor.class);

    public SummaryDescriptor(String id, String title, String desc) {
        super(id, title, desc, new SummaryForm());
    }

    /**
     * Method to be performed to show help document
     * Override this method to add help functionality
     */
    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.submission.summary", "javax.help.SecondaryWindow", "main");
    }

    @Override
    public void displayingPanel() {
        Navigator navigator = ((App) App.getInstance()).getNavigator();
        JButton nextButton = navigator.getNextButton();

        SummaryForm summaryForm = (SummaryForm) getNavigationPanel();
        nextButton.setText(appContext.getProperty("summary.next.button.title"));
        if(!summaryForm.getJCheckBox().isSelected()){
            nextButton.setEnabled(false);
        }
    }

    @Override
    public void beforeHidingForNextPanel() {
        boolean isFileExported = exportSummary(appContext);
        if (isFileExported) {
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
        } else {
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, true, false);
        }
    }

    /**
     * This method exports the px summary file
     */
    public static boolean exportSummary(AppContext appContext) {

        boolean isFileExported = false;
        // create file chooser
        JFileChooser fileChooser = new JFileChooser(appContext.getOpenFilePath());

        // set file chooser title
        fileChooser.setDialogTitle(appContext.getProperty("export.summary.dialog.title"));

        // set dialog type
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

        // set default selected file
        fileChooser.setSelectedFile(
                new File(
                        appContext.getOpenFilePath()
                                + System.getProperty("file.separator")
                                + appContext.getProperty("export.summary.default.summary.file.name")));

        // set selection mode
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        // show dialog
        int result = fileChooser.showDialog(((App) App.getInstance()).getMainFrame(), null);

        // check the selection results from open file dialog
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                if (!selectedFile.exists()) {
                    boolean created = selectedFile.createNewFile();
                    if (!created) {
                        logger.error("Failed to create summary file: " + selectedFile.getAbsolutePath());
                        JOptionPane.showMessageDialog(
                                ((App) App.getInstance()).getMainFrame(),
                                appContext.getProperty("export.summary.error.dialog.message"),
                                appContext.getProperty("export.summary.error.dialog.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return isFileExported;
                    }
                }
                Submission submission = appContext.getSubmissionRecord().getSubmission();
                SubmissionFileWriter.write(submission, selectedFile);
                if(appContext.isResubmission()){
                    addResubmissionSummary(selectedFile.getAbsolutePath(), appContext);
                }
                addToolVersionAndLicenseToSummary(selectedFile.getAbsolutePath(), appContext);
                isFileExported = true;
            } catch (Exception ex) {
                logger.error("Failed to export summary file: " + selectedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(
                        ((App) App.getInstance()).getMainFrame(),
                        appContext.getProperty("export.summary.error.dialog.message"),
                        appContext.getProperty("export.summary.error.dialog.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return isFileExported;
    }

    public static void addToolVersionAndLicenseToSummary(String fileName, AppContext appContext) {
        try {
            FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("COM\tVersion:" + appContext.getProperty("px.submission.tool.version"));
            bw.newLine();
            bw.write("COM\tDataset License:" + appContext.getProperty("px.submission.dataset.version"));
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * All all the resubmission files to the submission.px as a comment
     * @param fileName Submission.px full path
     * @param appContext AppContext
     */
    public static void addResubmissionSummary(String fileName, AppContext appContext) {
        try {
            FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            Map<DataFile, ResubmissionFileChangeState> resubmissionFiles  = appContext.getResubmissionRecord().getResubmission().getResubmission();
            for (Map.Entry<DataFile, ResubmissionFileChangeState> resubmissionFile : resubmissionFiles.entrySet()) {
                bw.write("COM\tResubmission\t" +  resubmissionFile.getKey().getFileName() + "\t" + resubmissionFile.getKey().getFileType().getName() + "\t" + resubmissionFile.getKey().getFileSize() + "\t" +  resubmissionFile.getValue());
                bw.newLine();
            }
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
