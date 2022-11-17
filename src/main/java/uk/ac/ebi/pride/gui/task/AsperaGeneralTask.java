package uk.ac.ebi.pride.gui.task;

import com.asperasoft.faspmanager.FaspManager;
import com.asperasoft.faspmanager.FaspManagerException;
import com.asperasoft.faspmanager.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.data.exception.SubmissionFileException;
import uk.ac.ebi.pride.data.io.SubmissionFileWriter;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.gui.data.SubmissionRecord;
import uk.ac.ebi.pride.gui.form.SummaryDescriptor;
import uk.ac.ebi.pride.gui.task.ftp.UploadErrorMessage;
import uk.ac.ebi.pride.gui.task.ftp.UploadMessage;
import uk.ac.ebi.pride.gui.task.ftp.UploadStoppedMessage;
import uk.ac.ebi.pride.gui.util.Constant;
import uk.ac.ebi.pride.gui.util.OSDetector;
import uk.ac.ebi.pride.gui.util.SubmissionRecordSerializer;
import uk.ac.ebi.pride.toolsuite.gui.desktop.DesktopContext;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskAdapter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/** Parent class, handles most Aspera transfer general functionality. */
public abstract class AsperaGeneralTask extends TaskAdapter<Void, UploadMessage> {

  public static final Logger logger = LoggerFactory.getLogger(AsperaGeneralTask.class);

  /** The submission record. */
  SubmissionRecord submissionRecord;

  /** Set contains files need to be submitted along with the folder name */
  Set<File> filesToSubmit;
  /** Iterator of the set of files to submit. */
  Iterator<File> filesToSubmitIter;

  /** Total file size need to be uploaded */
  long totalFileSize;

  /** Default constructor, initializes class variables. */
  private AsperaGeneralTask() {
    this.filesToSubmit = Collections.synchronizedSet(new LinkedHashSet<>());
    this.totalFileSize = 0;
  }

  /**
   * Constructor, initializes class variables including the submission record.
   *
   * @param submissionRecord the submission record to set
   */
  AsperaGeneralTask(SubmissionRecord submissionRecord) {
    this();
    this.submissionRecord = submissionRecord;
  }

  /** Prepare the Aspera-based submission. */
  private void prepareSubmission() {
    logger.debug("Preparing for uploading an entire submission");
    Set<File> files = Collections.synchronizedSet(new LinkedHashSet<>());
    File submissionFile = createSubmissionFile();
    if (submissionFile != null) {
      files.add(submissionFile);
    }
    for (DataFile dataFile : submissionRecord.getSubmission().getDataFiles()) {
      totalFileSize += dataFile.getFile().length();
      if (dataFile.isFile()) {
        files.add(dataFile.getFile());
      }
    }
    filesToSubmit = files;
    filesToSubmitIter = filesToSubmit.iterator();
  }

  /**
   * Performs the Aspera upload in the background.
   *
   * @return null when completed
   * @throws Exception any problems performing the Aspera upload.
   */
  @Override
  protected Void doInBackground() throws Exception {
    serializeSubmissionReport();
    prepareSubmission();
    asperaUpload();
    waitUpload();
    return null;
  }

  /**
   * Handles uploading of files using Asopera
   *
   * @throws FaspManagerException problems using the Aspera API to perform the upload
   * @throws UnsupportedEncodingException problems creating a temporary file
   */
  abstract void asperaUpload() throws FaspManagerException, UnsupportedEncodingException;

  /**
   * Gets the root path of Aspera binary
   *
   * @return root path in string
   */
  private String getAbsolutePath() throws UnsupportedEncodingException {
    String jarDir;
    String jarPath =
        AsperaGeneralTask.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    logger.debug("Jar Path: {}", jarPath);
    String decodedJarPath = URLDecoder.decode(jarPath, "UTF-8");
    File jarFile = new File(decodedJarPath);
    File jarParent = new File(jarFile.getParent());
    if (!decodedJarPath.endsWith("jar")) {
      jarDir = jarParent.getParent();
    } else {
      jarDir = jarParent.getAbsolutePath();
    }
    return jarDir;
  }

  /**
   * Choses which version of the Aspera binary to use, depending on operating system.
   *
   * @return the aspera binary location as a String.
   * @throws UnsupportedEncodingException Unable to create a temporary file.
   */
  String chooseAsperaBinary() throws UnsupportedEncodingException {
    final OSDetector.OS os = OSDetector.getOS();
    final DesktopContext appContext = App.getInstance().getDesktopContext();
    String jarDir = getAbsolutePath();
    String ascpLocation = "";
    switch (os) {
      case MAC:
        ascpLocation = appContext.getProperty("aspera.client.mac.binary");
        break;
      case LINUX_32:
        ascpLocation = appContext.getProperty("aspera.client.linux32.binary");
        break;
      case LINUX_64:
        ascpLocation = appContext.getProperty("aspera.client.linux64.binary");
        break;
      case WINDOWS:
        ascpLocation = appContext.getProperty("aspera.client.windows.binary");
        break;
      default:
        String msg = "Unsupported platform detected:" + OSDetector.os + " arch: " + OSDetector.arch;
        logger.error(msg);
        publish(new UploadErrorMessage(this, null, msg));
    }
    return jarDir + File.separator + ascpLocation;
  }

  /**
   * Creates a submission file
   *
   * @return boolean true for successfully creating a submission file, false otherwise.
   */
  private File createSubmissionFile() {

    AppContext appContext = (AppContext) App.getInstance().getDesktopContext();
    try {
      SecureRandom random = new SecureRandom();
      File tempDir =
          new File(System.getProperty("java.io.tmpdir") + File.separator + random.nextLong());
      logger.info("Created temp directory? " + tempDir.mkdir());
      File submissionFile =
          new File(
              tempDir.getAbsolutePath() + File.separator + Constant.PX_SUBMISSION_SUMMARY_FILE);
      logger.info("Create temporary submission summary file : " + submissionFile.getAbsolutePath());
      SubmissionFileWriter.write(submissionRecord.getSubmission(), submissionFile);
      if(appContext.isResubmission()){
        SummaryDescriptor.addResubmissionSummary(submissionFile.getAbsolutePath(), appContext);
      }
      SummaryDescriptor.addToolVersionAndLicenseToSummary(submissionFile.getAbsolutePath(), appContext);
      return submissionFile;
    } catch (SubmissionFileException ex) {
      String msg = "Failed to create submission file";
      logger.error(msg, ex);
      publish(new UploadErrorMessage(this, null, msg));
    }
    return null;
  }

  /**
   * Waits fo the Aspera upload to complete.
   *
   * @throws InitializationException Problems starting the transfer.
   * @throws InterruptedException Problems sleepging the method.
   */
  private void waitUpload() throws InitializationException, InterruptedException {
    FaspManager faspManager = FaspManager.getSingleton();
    while (faspManager.isRunning()) {
      Thread.sleep(30000);
    } // wait for Aspera transfer to finish
  }

  /** Serializes the submission record. */
  private void serializeSubmissionReport() {
    try {
      SubmissionRecordSerializer.serialize(submissionRecord);
    } catch (IOException ioe) {
      logger.error("Failed to save submission record");
    }
  }

  /** Publishes a message if the transfer has been cancelled. */
  @Override
  protected void cancelled() {
    publish(new UploadStoppedMessage(this, submissionRecord));
  }
}
