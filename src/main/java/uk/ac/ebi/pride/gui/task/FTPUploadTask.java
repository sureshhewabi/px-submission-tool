package uk.ac.ebi.pride.gui.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.data.exception.SubmissionFileException;
import uk.ac.ebi.pride.data.io.SubmissionFileWriter;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.gui.form.SummaryDescriptor;
import uk.ac.ebi.pride.toolsuite.gui.blocker.DefaultGUIBlocker;
import uk.ac.ebi.pride.toolsuite.gui.blocker.GUIBlocker;
import uk.ac.ebi.pride.gui.data.SubmissionRecord;
import uk.ac.ebi.pride.gui.task.ftp.*;
import uk.ac.ebi.pride.gui.util.Constant;
import uk.ac.ebi.pride.gui.util.SubmissionRecordSerializer;
import uk.ac.ebi.pride.toolsuite.gui.task.Task;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskAdapter;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskEvent;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskListener;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Task for uploading all submission files
 *
 * @author Rui Wang
 * @version $Id$
 */
public class FTPUploadTask extends TaskAdapter<Void, UploadMessage> implements TaskListener<UploadMessage, UploadMessage> {
    public static final Logger logger = LoggerFactory.getLogger(FTPUploadTask.class);

    public static final int NUMBER_OF_CONCURRENT_UPLOAD = 3;

    private SubmissionRecord submissionRecord;
    /**
     * Map contains files need to be submitted along with the folder name
     */
    private Set<DataFile> fileToSubmit;
    /**
     * Map contains files have failed submission
     */
    private Set<DataFile> fileFailToSubmit;

    /**
     * Total file size need to be uploaded
     */
    private long totalFileSize;
    /**
     * Total file size been uploaded
     */
    private long uploadFileSize;

    /**
     * Number of ongoing sub tasks
     */
    private int ongoingSubTasks;
    /**
     * indicates whether the upload has already finished
     */
    private boolean uploadFinished;

    /**
     * Constructor used for a new submission
     *
     * @param submissionRecord submission record
     */
    public FTPUploadTask(SubmissionRecord submissionRecord) {
        this.submissionRecord = submissionRecord;
        this.fileToSubmit = Collections.synchronizedSet(new LinkedHashSet<DataFile>());
        this.fileFailToSubmit = Collections.synchronizedSet(new LinkedHashSet<DataFile>());
        this.totalFileSize = 0;
        this.uploadFileSize = 0;
        this.ongoingSubTasks = 0;
        this.uploadFinished = false;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // save submission initial progress
        serializeSubmissionReport();

        // prepare for ftp upload
        prepareSubmission();

        if (fileToSubmit.size() == 0) {
            // all files has already been submitted
            uploadFinished = true;
            publish(new UploadSuccessMessage(this));
        } else {
            // upload submission files
            for (int i = 0; i < NUMBER_OF_CONCURRENT_UPLOAD; i++) {
                if (!fileToSubmit.isEmpty()) {
                    ongoingSubTasks++;
                    uploadFile();
                }
            }
        }

        return null;
    }

    /**
     * Prepare for upload an entire submission
     */
    private void prepareSubmission() {
        logger.debug("Preparing for uploading an entire submission");

        // add submission summary file
        if (!submissionRecord.isSummaryFileUploaded()) {
            File submissionFile = createSubmissionFile();
            if (submissionFile != null) {
                DataFile dataFile = new DataFile();
                dataFile.setFile(submissionFile);
                fileToSubmit.add(dataFile);
            }
        }

        // prepare for submission
        for (DataFile dataFile : submissionRecord.getSubmission().getDataFiles()) {
            long fileSize = dataFile.getFileSize();
            totalFileSize += fileSize;
            if (!submissionRecord.isUploaded(dataFile)) {
                fileToSubmit.add(dataFile);
            }

            if (submissionRecord.isUploaded(dataFile)) {
                uploadFileSize += fileSize;
            }
        }
    }

    /**
     * Create submission file
     *
     * @return boolean true indicates success
     */
    private File createSubmissionFile() {
        AppContext appContext = (AppContext) App.getInstance().getDesktopContext();
        try {
            // create a random temporary directory
            SecureRandom random = new SecureRandom();
            File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + random.nextLong());
            tempDir.mkdir();

            File submissionFile = new File(tempDir.getAbsolutePath() + File.separator + Constant.PX_SUBMISSION_SUMMARY_FILE);

            logger.debug("Create temporary submission summary file : " + submissionFile.getAbsolutePath());

            // write out submission details
            SubmissionFileWriter.write(submissionRecord.getSubmission(), submissionFile);
            if(appContext.isResubmission()){
                SummaryDescriptor.addResubmissionSummary(submissionFile.getAbsolutePath(), appContext);
            }
            SummaryDescriptor.addToolVersionAndLicenseToSummary(submissionFile.getAbsolutePath(),(AppContext) App.getInstance().getDesktopContext());

            return submissionFile;

        } catch (SubmissionFileException ex) {
            String msg = "Failed to create submission file";
            logger.error(msg, ex);
            publish(new UploadErrorMessage(this, null, msg));
        }
        return null;
    }

    /**
     * Upload next file in the fileToUpload set
     */
    private synchronized void uploadFile() {
        DataFile fileToUpload = fileToSubmit.iterator().next();
        fileToSubmit.remove(fileToUpload);
        logger.debug("Upload file: " + fileToUpload.getFileName());
        // Get FTP upload service from factory
        Task task = UploadServiceFactory.createFileFtpUploadTask(fileToUpload, submissionRecord.getUploadDetail());
        List<Object> owners = this.getOwners();
        for (Object owner : owners) {
            task.addOwner(owner);
        }
        task.addTaskListener(this);
        task.setGUIBlocker(new DefaultGUIBlocker(task, GUIBlocker.Scope.NONE, null));
        App.getInstance().getDesktopContext().addTask(task);
    }

    private void serializeSubmissionReport() {
        try {
            SubmissionRecordSerializer.serialize(submissionRecord);
        } catch (IOException ioe) {
            logger.error("Failed to save submission record");
        }
    }

    @Override
    public void process(TaskEvent<List<UploadMessage>> listTaskEvent) {
        for (UploadMessage uploadMessage : listTaskEvent.getValue()) {
            String fileName = uploadMessage.getDataFile().getFileName();
            if (uploadMessage instanceof UploadProgressMessage) {
                if (!Constant.PX_SUBMISSION_SUMMARY_FILE.equals(fileName)) {
                    uploadFileSize += ((UploadProgressMessage) uploadMessage).getBytesTransferred();
                    int totalNumOfFiles = submissionRecord.getSubmission().getDataFiles().size();
                    int uploadedNumOfFiles = submissionRecord.getUploadedFiles().size();
                    publish(new UploadProgressMessage(this, uploadMessage.getDataFile(), totalFileSize, uploadFileSize, totalNumOfFiles, uploadedNumOfFiles));
                }
            } else if (uploadMessage instanceof UploadFileSuccessMessage) {
                ongoingSubTasks--;
                logger.debug("Finished upload file: " + fileName);
                if (Constant.PX_SUBMISSION_SUMMARY_FILE.equals(fileName)) {
                    submissionRecord.setSummaryFileUploaded(true);
                } else {
                    submissionRecord.addUploadedFiles(uploadMessage.getDataFile());
                }

                // serialize submission progress report
                serializeSubmissionReport();

                if (!fileToSubmit.isEmpty()) {
                    ongoingSubTasks++;
                    // file ftp upload task
                    uploadFile();
                } else if (ongoingSubTasks == 0) {
                    if (fileFailToSubmit.isEmpty()) {
                        int totalNumOfFiles = submissionRecord.getSubmission().getDataFiles().size();
                        publish(new UploadProgressMessage(this, null, totalFileSize, totalFileSize, totalNumOfFiles, totalNumOfFiles));
                        if (!uploadFinished) {
                            uploadFinished = true;
                            publish(new UploadSuccessMessage(this));
                        }
                    } else {
                        publish(new UploadStoppedMessage(this, submissionRecord));
                    }
                }
            } else if (uploadMessage instanceof UploadErrorMessage) {
                ongoingSubTasks--;
                logger.debug("Failed to upload file: " + fileName);
                publish(uploadMessage);
            } else if (uploadMessage instanceof UploadCancelMessage) {
                logger.debug("Cancelled upload: " + fileName);
                ongoingSubTasks--;
                logger.debug("Running submission sub tasks: " + ongoingSubTasks);
                if (ongoingSubTasks == 0) {
                    fileToSubmit.clear();
                    publish(new UploadStoppedMessage(this, submissionRecord));
                }
            }
        }
    }

    @Override
    public void started(TaskEvent<Void> event) {
    }

    @Override
    public void succeed(TaskEvent<UploadMessage> voidTaskEvent) {
    }

    @Override
    public void progress(TaskEvent<Integer> progress) {
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
}
