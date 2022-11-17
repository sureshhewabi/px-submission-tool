package uk.ac.ebi.pride.gui.form;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.form.panel.SummaryItemPanel;
import uk.ac.ebi.pride.gui.navigation.Navigator;
import uk.ac.ebi.pride.gui.task.CalculateChecksumTask;
import uk.ac.ebi.pride.gui.task.checksum.ChecksumMessage;
import uk.ac.ebi.pride.toolsuite.gui.blocker.DefaultGUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.blocker.GUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.task.Task;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskEvent;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskListener;
import uk.ac.ebi.pride.utilities.util.Tuple;

import javax.help.HelpBroker;
import javax.swing.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateChecksumDescriptor extends ContextAwareNavigationPanelDescriptor implements TaskListener<DataFile, ChecksumMessage> {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionTypeDescriptor.class);

    public static Map<String, Tuple<String, String>> checksumCalculatedFiles = new HashMap<>();

    public boolean dontCalculateChecksum = true;

    public CalculateChecksumDescriptor(String id, String title, String desc) {
        super(id, title, desc, new CalculateChecksumForm());
    }

    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.checksum.type", "javax.help.SecondaryWindow", "main");
    }

    @Override
    public void displayingPanel() {
        Navigator navigator = ((App) App.getInstance()).getNavigator();
        JButton nextButton = navigator.getNextButton();

        nextButton.setText(appContext.getProperty("checksum.submit.button.title"));
        nextButton.setEnabled(false);

        int option = JOptionPane.showConfirmDialog(app.getMainFrame(),
                appContext.getProperty("checksum.confirm.message"),
                appContext.getProperty("checksum.confirm.title"),
                JOptionPane.YES_NO_OPTION, JOptionPane.YES_NO_OPTION);

        if (option != JOptionPane.YES_OPTION) {
            dontCalculateChecksum = true;
            removeChecksumFile();
            nextButton.setEnabled(true);
        } else {
            dontCalculateChecksum = false;
            // enable cancel button
            CalculateChecksumForm calculateChecksumForm = (CalculateChecksumForm) CalculateChecksumDescriptor.this.getNavigationPanel();
            calculateChecksumForm.enableCancelButton(true);
            // set the default upload message
            calculateChecksumForm.setProgressMessage(appContext.getProperty("checksum.default.message"));
            Task newTask = new CalculateChecksumTask(appContext.getSubmissionRecord().getSubmission());
            newTask.addTaskListener(this);
            // set task's gui blocker
            newTask.setGUIBlocker(new DefaultGUIBlocker(newTask, GUIBlocker.Scope.NONE, null));
            // add task listeners
            appContext.addTask(newTask);
            firePropertyChange(DISPLAYING_PANEL_PROPERTY, false, true);
        }
    }

    private void removeChecksumFile() {
        List<DataFile> files = ((AppContext) App.getInstance().getDesktopContext()).getSubmissionRecord().getSubmission().getDataFiles();
        for (DataFile file : files) {
            if (file.getFileName().equals("checksum.txt"))
                ((AppContext) App.getInstance().getDesktopContext()).removeDatafile(file);
        }
    }

    @Override
    public void beforeHidingForNextPanel() {
        try {
            if (dontCalculateChecksum || checkAndWriteChecksum(appContext.getSubmissionRecord().getSubmission().getDataFiles())) {
                firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
            }
        } catch (Exception ex) {
            logger.error("Error in writing checksum file");
        }
    }

    @Override
    public boolean toSkipPanel() {
        final String resubmissionPxAccession = appContext.getSubmissionRecord().getSubmission().getProjectMetaData().getResubmissionPxAccession();
        return resubmissionPxAccession != null && resubmissionPxAccession.length()>0;
    }

    private boolean checkAndWriteChecksum(List<DataFile> dataFiles) throws Exception {
        logger.info("Writing calculated checksum to checksum.txt");
        Files.write("#Checksum File\n".getBytes(), SummaryItemPanel.checksumFile);
        int countOfChecksumCalculatedFiles = 0;
        for (DataFile dataFile : dataFiles) {
            try {
                if (checksumCalculatedFiles.containsKey(dataFile.getFilePath()) &&
                        !dataFile.getFile().getName().equals("checksum.txt")) {
                    Files.append(dataFile.getFilePath() + "\t" +
                                    checksumCalculatedFiles.get(dataFile.getFilePath()).getValue() + "\n",
                            SummaryItemPanel.checksumFile, Charset.defaultCharset());
                    countOfChecksumCalculatedFiles++;
                } else if (!dataFile.getFile().getName().equals("checksum.txt")) {
                    return false;
                }
            } catch (Exception ex) {
                logger.error("Error in adding file " + dataFile.getFile().getName() + " to checksum.txt");
                logger.error(ex.getMessage());
                return false;
            }
        }
        logger.info("Checksum calculated and written to checksum.txt for all files");
        logger.info("Files count " + countOfChecksumCalculatedFiles);
        return true;
    }


    @Override
    public void started(TaskEvent<Void> taskEvent) {

    }

    @Override
    public void process(TaskEvent<List<ChecksumMessage>> taskEvent) {
        List<ChecksumMessage> checksumMessages = taskEvent.getValue();
        for (ChecksumMessage checksumMessage : checksumMessages) {
            try {
                CalculateChecksumForm calculateChecksumForm = (CalculateChecksumForm) CalculateChecksumDescriptor.this.getNavigationPanel();
                calculateChecksumForm.setProgress(checksumMessage.getNoOfFilesProcessed(), checksumMessage.getTotalNoOfFiles());
                Tuple<String, String> fileChecksum = checksumMessage.getFileChecksum();
                String filePath = fileChecksum.getKey();
                if(!checksumCalculatedFiles.containsKey(filePath)) {
                    checksumCalculatedFiles.put(filePath, fileChecksum);
                }
            } catch (Exception exception) {
                JOptionPane.showConfirmDialog(app.getMainFrame(),
                        appContext.getProperty("checksum.error.message"),
                        appContext.getProperty("checksum.error.title"),
                        JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    @Override
    public void succeed(TaskEvent<DataFile> taskEvent) {
    }

    @Override
    public void finished(TaskEvent<Void> event) {
    }

    @Override
    public void failed(TaskEvent<Throwable> event) {
        JOptionPane.showConfirmDialog(app.getMainFrame(),
                appContext.getProperty("checksum.file.selection.error.message"),
                appContext.getProperty("checksum.file.error.title"),
                JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void cancelled(TaskEvent<Void> event) {
    }

    @Override
    public void interrupted(TaskEvent<InterruptedException> iex) {
    }

    @Override
    public void progress(TaskEvent<Integer> progress) {
    }
}
