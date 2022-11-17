package uk.ac.ebi.pride.gui.data;

import uk.ac.ebi.pride.archive.submission.model.submission.UploadDetail;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.Resubmission;
import uk.ac.ebi.pride.data.model.Submission;

import java.io.Serializable;
import java.util.*;

/**
 * SubmissionRecord is used to store the details and the progress of a submission
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ResubmissionRecord implements Serializable {
    private Resubmission resubmission;
    private String userName;
    private String password;
    private UploadDetail uploadDetail;
    private boolean summaryFileUploaded = false;
    private Set<DataFile> uploadedFiles;


    public ResubmissionRecord() {
        this.resubmission = new Resubmission();
        this.uploadDetail = null;
        this.uploadedFiles = Collections.synchronizedSet(new HashSet<>());
    }

    public ResubmissionRecord(Resubmission resubmission) {
        this(resubmission, null);
    }

    public ResubmissionRecord(Resubmission resubmission, UploadDetail uploadDetail) {
        this.resubmission = resubmission;
        this.uploadDetail = uploadDetail;
        this.uploadedFiles = Collections.synchronizedSet(new HashSet<DataFile>());
    }

    public Resubmission getResubmission() {
        return resubmission;
    }

    public void setResubmission(Resubmission resubmission) {
        this.resubmission = resubmission;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UploadDetail getUploadDetail() {
        return uploadDetail;
    }

    public void setUploadDetail(UploadDetail uploadDetail) {
        this.uploadDetail = uploadDetail;
    }

    public Set<DataFile> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(Set<DataFile> uploadedFiles) {
        this.uploadedFiles.clear();
        if (uploadedFiles != null) {
            this.uploadedFiles = uploadedFiles;
        }
    }

    public void addUploadedFiles(DataFile dataFile) {
        uploadedFiles.add(dataFile);
    }

    public boolean isUploaded(DataFile dataFile) {
        return uploadedFiles.contains(dataFile);
    }

    public boolean isSummaryFileUploaded() {
        return summaryFileUploaded;
    }

    public void setSummaryFileUploaded(boolean summaryFileUploaded) {
        this.summaryFileUploaded = summaryFileUploaded;
    }
}
