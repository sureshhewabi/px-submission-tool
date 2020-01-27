package uk.ac.ebi.pride.gui.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.navigation.Navigator;
import uk.ac.ebi.pride.gui.task.EncryptFileTask;
import uk.ac.ebi.pride.gui.task.encryption.EncryptionMessage;
import uk.ac.ebi.pride.toolsuite.gui.blocker.DefaultGUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.blocker.GUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.task.Task;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskEvent;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskListener;

import javax.help.HelpBroker;
import javax.swing.*;
import java.util.List;

public class EncryptionDescriptor extends ContextAwareNavigationPanelDescriptor implements TaskListener<DataFile, EncryptionMessage> {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionTypeDescriptor.class);

    private EncryptionForm encryptionForm = null;

    public EncryptionDescriptor(String id, String title, String desc) {
        super(id, title, desc, new EncryptionForm());
        encryptionForm = (EncryptionForm) getNavigationPanel();
    }

    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.encryption.type", "javax.help.SecondaryWindow", "main");
    }

    @Override
    public void displayingPanel() {
        Navigator navigator = ((App) App.getInstance()).getNavigator();
        JButton nextButton = navigator.getNextButton();

        nextButton.setText(appContext.getProperty("encryption.submit.button.title"));
        nextButton.setEnabled(false);

        // enable cancel button
        EncryptionForm encryptionForm = (EncryptionForm) EncryptionDescriptor.this.getNavigationPanel();
        encryptionForm.enableCancelButton(true);

        // set the default upload message
        encryptionForm.setProgressMessage(appContext.getProperty("encryption.default.message"));

        Task newTask = new EncryptFileTask(appContext.getSubmissionRecord().getSubmission());
        newTask.addTaskListener(this);
        // set task's gui blocker
        newTask.setGUIBlocker(new DefaultGUIBlocker(newTask, GUIBlocker.Scope.NONE, null));
        // add task listeners
        appContext.addTask(newTask);

        firePropertyChange(DISPLAYING_PANEL_PROPERTY, false, true);
    }

    @Override
    public void beforeHidingForNextPanel() {
        firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
    }


    @Override
    public void started(TaskEvent<Void> taskEvent) {

    }

    @Override
    public void process(TaskEvent<List<EncryptionMessage>> taskEvent) {
        List<EncryptionMessage> encryptionMessages = taskEvent.getValue();
        for (EncryptionMessage encryptionMessage : encryptionMessages) {
            if (encryptionMessage.getBytesRead() < 0) {
                JOptionPane.showConfirmDialog(app.getMainFrame(),
                        appContext.getProperty("encryption.error.message"),
                        appContext.getProperty("encryption.error.title"),
                        JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            EncryptionForm encryptionForm = (EncryptionForm) EncryptionDescriptor.this.getNavigationPanel();
            encryptionForm.setProgress(encryptionMessage.getNoOfFilesProcessed(),encryptionMessage.getTotalNoOfFiles());
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
