package uk.ac.ebi.pride.gui.util;

import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.archive.dataprovider.utils.SubmissionTypeConstants;
import uk.ac.ebi.pride.data.model.DataFile;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.ac.ebi.pride.gui.task.FileScanAndValidationTask.OLINK_EXP;

/**
 * @author Rui Wang
 * @version $Id$
 */
public final class WarningMessageGenerator {

    public static String getInvalidPRIDEXMLFileWarning(List<DataFile> prideXmlFiles) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>The following PRIDE XML don't have protein identifications</b><br/>");
        for (DataFile prideXmlFile : prideXmlFiles) {
            errMsg.append("<li>");
            errMsg.append(prideXmlFile.getFile().getName());
            errMsg.append("</li>");
        }
        errMsg.append("</html>");

        return errMsg.toString();
    }

    public static String getInvalidResultFileWarning() {
        return "<html>" + "<b>Invalid result file detected, please submit results in any of the supported file formats (see accompanying documentation)</b><br/>" + "</html>";
    }

    public static String getCrosslinkingWarning(SubmissionTypeConstants submissionType) {

        StringBuilder warningMsg = new StringBuilder();
        warningMsg.append("<html>");
        if (submissionType.equals(SubmissionTypeConstants.COMPLETE)) {
            warningMsg.append("We have detected that this submission is a COMPLETE Crosslinking dataset. <br />" +
                    "Before proceeding, please review the guidelines on <a href=\"https://www.ebi.ac.uk/pride/markdownpage/crosslinking\">How to submit complete crosslinking dataset</a> " +
                    "to ensure that your files and submission meet all requirements" +
                    "<br />to be included in the newly developed <a href=\"https://www.ebi.ac.uk/pride/archive/crosslinking \">PRIDE Crosslinking Resource</a>.");
        } else if (submissionType.equals(SubmissionTypeConstants.PARTIAL)) {
            warningMsg.append("We have detected that this submission is a PARTIAL Crosslinking dataset. <br />" +
                    "You can include your submission in the newly developed <a href=\"https://www.ebi.ac.uk/pride/archive/crosslinking \">PRIDE Crosslinking Resource</a> " +
                    "<br />if you make a COMPLETE submission and the files meet all the requirements. <br />" +
                    "Please review the guidelines on <a href=\"https://www.ebi.ac.uk/pride/markdownpage/crosslinking\">How to submit complete crosslinking dataset.</a> ");
        }
        return warningMsg.toString();
    }

    public static String getCancelPartialSubmission() {
        return "<html>" + "<b>If you do not want to continue please close the submisison tool</b><br/>" + "</html>";
    }

    public static String getInvalidSDRFFileWarning() {
        return "<html>" + "<b>Invalid EXPERIMENTAL_DESIGN/SDRF file detected, please check log file for detailed error report</b><br/>" + "</html>";
    }

    public static String getWiffScanMissingWarning() {
        return "<html>" + "<b>It is recommended to submit wiff scan files associated with wiff files</b><br/>" + "</html>";
    }

    public static String getBafFileWarning() {
        return "<html>" + "<b>It is recommended to submit whole .d directory as zipped instead of individual BAF files</b><br/>" + "</html>";
    }

    public static String getExperimentalDesignFileMissingWarning() {
        return "It is recommended to submit a <a href=\"https://github.com/bigbio/proteomics-metadata-standard\">Sample to Data file format (SDRF)</a> File<br/>";
    }

    public static String getMultipleResultFileFormatWarning(String formatA, String formatB) {

        return "<html>" + "<b>Both " + formatA + " and " + formatB + " detected, please submit results using one file format only</b><br/>" + "</html>";
    }

    public static String getInvalidMzIdentMLVersionWarning(List<DataFile> mzIdentMLFiles) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>The following mzIdentML are not version 1.1.0 or 1.2.0 </b><br/>");
        for (DataFile mzIdentMLFile : mzIdentMLFiles) {
            errMsg.append("<li>");
            errMsg.append(mzIdentMLFile.getFile().getName());
            errMsg.append("</li>");
        }
        errMsg.append("</html>");

        return errMsg.toString();
    }

    public static String getInvalidFilesWarning(List<DataFile> invalidDataFiles) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>The following files ARE NOT VALID</b><br/>");
        for (DataFile dataFile :
                invalidDataFiles) {
            errMsg.append("<li>");
            errMsg.append(dataFile.getFile().getName());
            errMsg.append("</li>");
        }
        errMsg.append("</html>");
        return errMsg.toString();
    }

    public static String getModifiedFilesWarning(List<DataFile> missingModifiedFile) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>The following files modified, but new files are not uploaded</b><br/>");
        for (DataFile dataFile :
                missingModifiedFile) {
            errMsg.append("<li>");
            errMsg.append(dataFile.getFile().getName());
            errMsg.append("</li>");
        }
        errMsg.append("</html>");
        return errMsg.toString();
    }


    public static String getDuplicateFilesWarning(List<String> missingModifiedFile) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>The following filenames are already present, please select modify in the below panel to update it</b><br/>");
        for (String dataFile :
                missingModifiedFile) {
            errMsg.append("<li>");
            errMsg.append(dataFile);
            errMsg.append("</li>");
        }
        errMsg.append("</html>");
        return errMsg.toString();
    }

    public static String getAllFilesAreBeingRemoved() {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>You are trying to delete all the files from the submission leaving less than 2 files or no files</b><br/>");
        errMsg.append("</html>");
        return errMsg.toString();
    }

    public static String getMissingReferencedFilesWarning(Map<DataFile, Set<String>> invalidFiles) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>The following files, REFERENCE MISSING FILES</b><br/>");
        errMsg.append("<ol>");
        for (DataFile dataFile :
                invalidFiles.keySet()) {
            errMsg.append("<li>" + dataFile.getFile().getName() + "</li>");
            errMsg.append("<ul>");
            for (String missingFile :
                    invalidFiles.get(dataFile)) {
                errMsg.append("<li>" + missingFile + "</li>");
            }
            errMsg.append("</ul>");
        }
        errMsg.append("</ol>");
        errMsg.append("</html>");
        return errMsg.toString();
    }

    public static String getInvalidMzIdentMLSpectraDataWarning(List<DataFile> mzIdentMLFiles) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>The following mzIdentML do not contain reference to original spectrum files</b><br/>");
        for (DataFile mzIdentMLFile : mzIdentMLFiles) {
            errMsg.append("<li>");
            errMsg.append(mzIdentMLFile.getFile().getName());
            errMsg.append("</li>");
        }
        errMsg.append("</html>");

        return errMsg.toString();
    }

    public static String getInvalidMzIdentMLPeakFilesaWarning(List<DataFile> mzIdentMLFiles) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>The following mzIdentML are not referenced to 'PEAK' files</b><br/>");
        for (DataFile mzIdentMLFile : mzIdentMLFiles) {
            errMsg.append("<li>");
            errMsg.append(mzIdentMLFile.getFile().getName());
            errMsg.append("</li>");
        }
        errMsg.append("</html>");
        return errMsg.toString();
    }

    public static String getMzIdentMLPeakListFilWarning() {

        return "<html>" + "<b>Please add the following spectrum files, they are referenced by your mzIdentML files</b><br/>" + "</html>";
    }

    /**
     * Show a warning dialog, alert user that they should use PRIDE Converter to convert search output to PRIDE XML files.
     *
     * @return true means continue and ignore warning, false means stop at the current step
     */
    public static boolean showSupportedSearchFileWarning() {
        int n = JOptionPane.showConfirmDialog(((App) App.getInstance()).getMainFrame(),
                "<html>If you’re using <i>Mascot</i> Search Files, please, export them to mzIdentML directly.<br/>" +
                        "Once converted, a complete ProteomeXchagne submission can be made instead.<br/><br/>" +
                        "<b>Would you like to continue with the current partial submission?</b><html>",
                "Convertible Search Engine Output Detected",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        return n == 0;
    }

    public static String getResultFileWarning() {

        return "<html>" + "<b>Result files found in your partial submission.</b><br/>" + "You can either go to the previous step to select the complete submission option or simply remove these files." + "</html>";
    }

    /**
     * Show warning for the raw only submission, dta, mgf, ms2 or pkl files should be assigned to PEAK type
     */
    public static String getUnsupportedRawFileWarning() {

        // Create the balloon tip
        return "<html>" + "<b>Unsupported raw files found</b><br/>" + "zip file cannot contain multiple .RAW files." + "</html>";
    }

    /**
     * Show warning for submitting submission.px
     */
    public static String getSubmissionPxWarning() {

        // Create the balloon tip
        return "<html>" + "<b>Unsupported file found</b><br/>" + "Please remove submission.px file from the upload file list" + "</html>";
    }

    /**
     * Show warning for the raw only submission, it should contain result files or search files
     */
    public static String getRawOnlyWarning() {

        // Create the balloon tip
        return "<html>" + "<b>RESULT files or SEARCH files found in your raw files only submission.</b><br/>" + "You can either go to the previous step to select a different submission option or simply remove these files." + "</html>";
    }

    /**
     * Show warning of invalid files
     */
    public static String getInvalidFileWarning(int numOfInvalidFiles) {

        return String.valueOf(numOfInvalidFiles) + " invalid files detected, the file must exist and not empty.";
    }

    /**
     * Show warning of invalid files including reports
     */
    public static String getInvalidFileWarning(int numOfInvalidFiles, List<String> validationErrorMessages) {
        return String.valueOf(numOfInvalidFiles) + " invalid files detected:\n" + validationErrorMessages.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    /**
     * Show warning for missing files
     */
    public static String getMissedFileWarning(SubmissionTypeConstants submissionType,
                                              boolean hasResultFile,
                                              boolean hasSearchFile,
                                              boolean hasRawFile) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("<html>");
        errMsg.append("<b>Please make sure you have the following information:</b><br/>");

        if (submissionType.equals(SubmissionTypeConstants.COMPLETE) && !hasResultFile) {
            errMsg.append("<li>");
            errMsg.append("mzIdentML or mzTab Result files (plus spectrum files)");
            errMsg.append("</li>");
        } else if (!hasSearchFile) {
            errMsg.append("<li>");
            errMsg.append("Search engine output, such as: identification results produced by the analysis tool of your choice");
            errMsg.append("</li>");
        }

        if (!hasRawFile) {
            errMsg.append("<li>");
            errMsg.append("Raw instrument output files");
            errMsg.append("</li>");
        }

        errMsg.append("</html>");

        return errMsg.toString();
    }

    /**
     * Show warning for mixed supported and unsupported result files
     */
    public static String getUnsupportedResultFileWarning() {

        return "<html>" + "<b>Unsupported result files found</b><br/>" + "Please only add mzIdentML or mzTab Result Files" + "</html>";
    }

    public static String getUnsupportedSearchFileWarning() {

        return "<html>" + "<b>Unsupported search files found</b><br/>" + "Please choose complete submission option if you have results in mzIdentML or mzTab format" + "</html>";
    }

    public static String getMissingImageDataFileWarning() {
        return "<html>" + "<b>Missing mass spec imaging data</b><br/>" + "Please include .imzML files which contains mass spectrometry imaging data" + "</html>";
    }

    public static String getInvalidSubmissionType(boolean hasAdatOrNPXFile) {
        if(hasAdatOrNPXFile) {
            return "<html>" + "<b>Your submission contains .adat/.bml/.npx files</b><br/>" + "Please please go back and select Affinity Proteomics as Submission Type" + "</html>";
        }
        return "<html>" + "<b>Your Submission type is Affinity Proteomics</b><br/>" + "Please upload .adat/.bml/.parquet files as RAW files or go back to change the SubmissionType" + "</html>";

    }

    public static String getInvalidExpMethod(String instrumentType) {
        if(instrumentType.equals(OLINK_EXP)) {
            return "<html>" + "<b>Your submission contains .adat/.bml files</b><br/>" + "Please please go back and select Somascan as Experiment method" + "</html>";
        }
        return "<html>" + "<b>Your submission contains .npx.csv result files</b><br/>" + "Please please go back and select Olink as Experiment method" + "</html>";

    }

    public static String getInvalidFileWarning(String fileName) {
        return "<html>" + "<b>Your Submission file "+ fileName + " is invalid</b><br/>" + "Please check the logs for more details" + "</html>";

    }
}
