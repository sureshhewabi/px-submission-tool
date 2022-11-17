package uk.ac.ebi.pride.gui.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.archive.dataprovider.project.SubmissionType;
import uk.ac.ebi.pride.archive.submission.model.File.ProjectFile;
import uk.ac.ebi.pride.archive.submission.model.File.ProjectFileList;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.Resubmission;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.gui.data.SubmissionRecord;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.form.dialog.TaskDialog;
import uk.ac.ebi.pride.gui.task.FileScanAndValidationTask;
import uk.ac.ebi.pride.gui.task.GetPrideProjectFilesTask;
import uk.ac.ebi.pride.gui.task.ResubmissionFileScanAndValidationTask;
import uk.ac.ebi.pride.gui.task.ftp.UploadMessage;
import uk.ac.ebi.pride.gui.util.DataFileValidationMessage;
import uk.ac.ebi.pride.gui.util.ValidationState;
import uk.ac.ebi.pride.toolsuite.gui.blocker.DefaultGUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.blocker.GUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.task.Task;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskEvent;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskListener;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskListenerAdapter;

import javax.help.HelpBroker;
import java.io.File;
import java.util.List;

/**
 * Navigation descriptor for file mapping form
 *
 * @author Rui Wang
 * @version $Id$
 */
public class FileResubmissionDescriptor extends ContextAwareNavigationPanelDescriptor implements TaskListener<DataFileValidationMessage, Void> {

    private static final Logger logger = LoggerFactory.getLogger(FileResubmissionDescriptor.class);
    ProjectFileList projectFileList;
    FileResubmissionForm form;
    PrideProjectFilesTaskListener projectFilesTaskListener;

    public FileResubmissionDescriptor(String id, String title, String desc) {
        super(id, title, desc, new FileResubmissionForm());
        form = (FileResubmissionForm) getNavigationPanel();
        projectFilesTaskListener = new PrideProjectFilesTaskListener();
    }

    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.file.mapping", "javax.help.SecondaryWindow", "main");
    }


    @Override
    public void beforeHidingForNextPanel() {
        // validate the content in the table
        FileResubmissionForm form = (FileResubmissionForm) getNavigationPanel();
        Submission submission = appContext.getSubmissionRecord().getSubmission();
        Resubmission resubmission = appContext.getResubmissionRecord().getResubmission();
        Task fileSelectionValidationTask = new ResubmissionFileScanAndValidationTask(submission, resubmission);

        fileSelectionValidationTask.addTaskListener(form);
        fileSelectionValidationTask.addTaskListener(this);
        TaskDialog<DataFileValidationMessage, Void> taskDialog = new TaskDialog<DataFileValidationMessage, Void>(((App) App.getInstance()).getMainFrame(),
                appContext.getProperty("file.selection.validation.dialog.title"),
                appContext.getProperty("file.selection.validation.dialog.message"),
                false);
        taskDialog.setLocationRelativeTo(app.getMainFrame());
        taskDialog.setVisible(true);
        fileSelectionValidationTask.addTaskListener(taskDialog);
        fileSelectionValidationTask.addOwner(FileResubmissionDescriptor.this);
        fileSelectionValidationTask.setGUIBlocker(new DefaultGUIBlocker(fileSelectionValidationTask, GUIBlocker.Scope.NONE, null));

        appContext.addTask(fileSelectionValidationTask);
        ValidationState state = form.doValidation();
        if (!ValidationState.ERROR.equals(state)) {
            // notify
            form.hideWarnings();
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
        } else {
            // notify validation error
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, true, false);
        }
    }


    @Override
    public void beforeHidingForPreviousPanel() {
        //hide any visible warning balloon tip
        FileResubmissionForm form = (FileResubmissionForm) getNavigationPanel();
        form.hideWarnings();
        firePropertyChange(BEFORE_HIDING_FOR_PREVIOUS_PANEL_PROPERTY, false, true);
    }

    @Override
    public void displayingPanel() {
        SubmissionRecord submissionRecord = appContext.getSubmissionRecord();
        String username = submissionRecord.getUserName().trim();
        String password = submissionRecord.getPassword().trim();
        // TODO Temporary assigned to test
        submissionRecord.getSubmission().getProjectMetaData().setResubmissionPxAccession("PDX0031231");
        appContext.setSubmissionsType(SubmissionType.COMPLETE);
        appContext.setResubmission(true);

        // launch a new task to retrieve Existing project files from API call
        Task task = new GetPrideProjectFilesTask(username, password.toCharArray());
        task.addTaskListener(projectFilesTaskListener);

        // set gui blocker
        task.setGUIBlocker(new DefaultGUIBlocker(task, GUIBlocker.Scope.NONE, null));

        // execute submitted file finding task
        App.getInstance().getDesktopContext().addTask(task);

        firePropertyChange(DISPLAYING_PANEL_PROPERTY, false, true);
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
    public void succeed(TaskEvent<DataFileValidationMessage> taskEvent) {
        DataFileValidationMessage message = taskEvent.getValue();
        if (message.getState().equals(ValidationState.SUCCESS)) {
            FileResubmissionForm form = (FileResubmissionForm) getNavigationPanel();
            form.hideWarnings();
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
        } else {
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, true, false);
        }
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

    private class PrideProjectFilesTaskListener implements TaskListener<ProjectFileList, String> {

        @Override
        public void started(TaskEvent<Void> taskEvent) {

        }

        @Override
        public void process(TaskEvent<List<String>> taskEvent) {

        }

        @Override
        public void finished(TaskEvent<Void> taskEvent) {

        }

        @Override
        public void failed(TaskEvent<Throwable> taskEvent) {

        }

        @Override
        public void succeed(TaskEvent<ProjectFileList> taskEvent) {
            projectFileList =  taskEvent.getValue();
            logger.info("Number of files found: " + projectFileList.getProjectFiles().size());
            int count = 0;
            for (ProjectFile projectFile : projectFileList.getProjectFiles()) {
                DataFile dataFile = new DataFile(count++, new File(projectFile.getFileName()), null, projectFile.getFileType(), projectFile.getFileSize() ,null, Integer.toString(count));
                ((AppContext) App.getInstance().getDesktopContext()).addResubmissionDataFile(dataFile);
            }
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
    }
}
