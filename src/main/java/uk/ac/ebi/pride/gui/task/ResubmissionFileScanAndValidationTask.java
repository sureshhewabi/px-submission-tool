package uk.ac.ebi.pride.gui.task;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.archive.dataprovider.project.SubmissionType;
import uk.ac.ebi.pride.data.model.*;
import uk.ac.ebi.pride.data.mztab.parser.MzTabFullDocumentQuickParser;
import uk.ac.ebi.pride.data.mztab.parser.MzTabParser;
import uk.ac.ebi.pride.data.mztab.parser.exceptions.MzTabParserException;
import uk.ac.ebi.pride.data.util.FileUtil;
import uk.ac.ebi.pride.data.util.MassSpecFileFormat;
import uk.ac.ebi.pride.data.validation.SubmissionValidator;
import uk.ac.ebi.pride.data.validation.ValidationMessage;
import uk.ac.ebi.pride.data.validation.ValidationReport;
import uk.ac.ebi.pride.gui.util.*;
import uk.ac.ebi.pride.jaxb.model.CvParam;
import uk.ac.ebi.pride.jaxb.model.SampleDescription;
import uk.ac.ebi.pride.jaxb.xml.unmarshaller.PrideXmlUnmarshaller;
import uk.ac.ebi.pride.jaxb.xml.unmarshaller.PrideXmlUnmarshallerFactory;
import uk.ac.ebi.pride.sdrf.validate.Main;
import uk.ac.ebi.pride.sdrf.validate.model.ValidationError;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskAdapter;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validate all the files selected in the file selection step
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ResubmissionFileScanAndValidationTask extends TaskAdapter<DataFileValidationMessage, Void> {

    private static final Logger logger = LoggerFactory.getLogger(ResubmissionFileScanAndValidationTask.class);

    private static final String MZIDENTML_ACCEPTED_VERSION_1_1_0 = "1.1.0";
    private static final String MZIDENTML_ACCEPTED_VERSION_1_2_0 = "1.2.0";
    public static final Pattern MZIDENTML_PEAK_LIST_FILE_PATTERN = Pattern.compile("^[^<]*<SpectraData[^>]*location=\"([^\"]+)\"[^>]*>.*$");
    public static final String MZIDENTML_PEAK_LIST_FILE_END = "</SpectraData>";
    public static final String PRIDEXML_SAMPLE_DESCRIPTION_BEGIN = "<sampleDescription";
    public static final String PRIDEXML_SAMPLE_DESCRIPTION_END = "/sampleDescription>";
    public static final String PRIDEXML_GEL_FREE_IDENTIFICATION = "GelFreeIdentification";
    public static final String PRIDEXML_TWO_DIMENSIONAL_IDENTIFICATION = "TwoDimensionalIdentification";
    public static final Pattern PRIDEXML_SAMPLE_DESCRIPTION_PATTERN = Pattern.compile(".*<sampleDescription[^>]*/>.*");

    private Submission submission;
    private Resubmission resubmission;
    private AppContext appContext;
    private App app;

    public ResubmissionFileScanAndValidationTask(Submission submission, Resubmission resubmission) {
        this.submission = submission;
        this.resubmission = resubmission;
        this.app = (App) App.getInstance();
        this.appContext = (AppContext) app.getDesktopContext();
    }

    @Override
    protected DataFileValidationMessage doInBackground() throws Exception {


        //2. If file is modified, check if the new file has been uploaded
        List<DataFile> missingModifiedFile = findMissingModifiedFiles();
        if (missingModifiedFile.size() > 0) {
            return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getModifiedFilesWarning(missingModifiedFile));
        }

        // generate validation results
        QuickValidationResult quickValidationResult = runQuickValidation(submission.getDataFiles());
        setProgress(10);

        // cannot have invalidate files
        if (quickValidationResult.getNumOfInvalidFiles() > 0) {
            //return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getInvalidFileWarning(quickValidationResult.numOfInvalidFiles));
            // Assuming we only get Validation Messages of type ERROR, but we check anyway
            return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getInvalidFileWarning(quickValidationResult.numOfInvalidFiles,
                    quickValidationResult.getValidationReport().getMessages().stream()
                            .filter(m -> m.getType() == ValidationMessage.Type.ERROR).map(m -> m.getMessage()).collect(Collectors.toList())));
        }
        setProgress(20);

        SubmissionType submissionType = submission.getProjectMetaData().getSubmissionType();

        List<DataFile> prideXmlDataFiles = submission.getDataFilesByFormat(MassSpecFileFormat.PRIDE);
        List<DataFile> prideXmlDataFilesResub = resubmission.getDataFilesByFormat(MassSpecFileFormat.PRIDE);
        boolean noPrideXml = prideXmlDataFiles.isEmpty() && prideXmlDataFilesResub.isEmpty();

        List<DataFile> mzIdentMLDataFiles = submission.getDataFilesByFormat(MassSpecFileFormat.MZIDENTML);
        List<DataFile> mzIdentMLDataFilesResub = resubmission.getDataFilesByFormat(MassSpecFileFormat.MZIDENTML);
        boolean noMzIdentML = mzIdentMLDataFiles.isEmpty() && mzIdentMLDataFilesResub.isEmpty();

        // Get provided mzTab files
        List<DataFile> mzTabDataFiles = submission.getDataFilesByFormat(MassSpecFileFormat.MZTAB);
        List<DataFile> mzTabDataFilesResub = resubmission.getDataFilesByFormat(MassSpecFileFormat.MZTAB);
        boolean mzTabFilesHaveBeenProvided = !(mzTabDataFiles.isEmpty() && mzTabDataFilesResub.isEmpty());

        boolean noRawFile = submission.getDataFileByType(ProjectFileType.RAW).isEmpty() && resubmission.getDataFileByType(ProjectFileType.RAW).isEmpty();

        List<DataFile> mzMlFiles = submission.getDataFilesByFormat(MassSpecFileFormat.INDEXED_MZML);
        List<DataFile> mzMlFilesResub = resubmission.getDataFilesByFormat(MassSpecFileFormat.INDEXED_MZML);

        boolean noSearchFile = submission.getDataFileByType(ProjectFileType.SEARCH).isEmpty() && resubmission.getDataFileByType(ProjectFileType.SEARCH).isEmpty();

        List<DataFile> resultDataFiles = submission.getDataFileByType(ProjectFileType.RESULT);
        List<DataFile> resultDataFilesResub = resubmission.getDataFileByType(ProjectFileType.RESULT);
        boolean noResultFile = resultDataFiles.isEmpty() && resultDataFilesResub.isEmpty();

        if (submissionType.equals(SubmissionType.COMPLETE)) {

            // should have both result files and raw files
            if (noResultFile || noRawFile) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMissedFileWarning(submissionType, !noResultFile, !noSearchFile, !noRawFile));
            }
            setProgress(30);

//            // cannot have unsupported result files
//            if (quickValidationResult.hasUnsupportedResultFile() && !quickValidationResult.isUrlBasedResultFilePresent()) {
//                // construct error message
//                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getUnsupportedResultFileWarning());
//            }
//            setProgress(40);
//
//            if (noPrideXml && noMzIdentML && !mzTabFilesHaveBeenProvided && !quickValidationResult.isUrlBasedResultFilePresent()) {
//                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getInvalidResultFileWarning());
//            }

            // cannot have both PRIDE xml and mzIdentML at the same time
            if (!noPrideXml && !noMzIdentML) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMultipleResultFileFormatWarning("PRIDE XML", "mzIdentML"));
            } else if (!noPrideXml && mzTabFilesHaveBeenProvided) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMultipleResultFileFormatWarning("PRIDE XML", "mzTab"));
            } else if (!noMzIdentML && mzTabFilesHaveBeenProvided) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMultipleResultFileFormatWarning("mzIdentML", "mzTab"));
            }

            if (!noPrideXml) {
                // cannot have PRIDE XML result files with no identifications
                List<DataFile> invalidPrideXmlFiles = runPrideXmlProteinIdentValidation(prideXmlDataFiles);
                if (invalidPrideXmlFiles.size() > 0) {
                    return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getInvalidPRIDEXMLFileWarning(invalidPrideXmlFiles));
                }

                // scan for sample details
                scanPrideXmlSampleDetails(prideXmlDataFiles);
            }
            setProgress(50);

            if (!noMzIdentML) {
                // cannot have mzIdentML other than version 1.1.0
                List<DataFile> invalidMzIdentMLFiles = runMzIdentMLVersionValidation(mzIdentMLDataFiles);
                if (invalidMzIdentMLFiles.size() > 0) {
                    return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getInvalidMzIdentMLVersionWarning(invalidMzIdentMLFiles));
                }
                setProgress(60);

                // mzIdentML files are required to contain reference to original spectrum files
                List<DataFile> invalidMzIdentMLSpectraDataFiles = runMzIdentMLSpectraDataValidation(mzIdentMLDataFiles);
                if (invalidMzIdentMLSpectraDataFiles.size() > 0) {
                    return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getInvalidMzIdentMLSpectraDataWarning(invalidMzIdentMLSpectraDataFiles));
                }
                setProgress(70);

                // cannot have mzIdentML without peak list files
                Map<DataFile, List<String>> invalidMzIdentMLPeakListFiles = runMzIdentMLPeakListFileScanAndValidation(submission.getDataFiles(), new ArrayList<>(resubmission.getResubmission().keySet()));
                if (invalidMzIdentMLPeakListFiles.size() > 0) {
                    DataFileValidationMessage validationMessage = new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMzIdentMLPeakListFilWarning());
                    validationMessage.addDataFileValidationResults(invalidMzIdentMLPeakListFiles);
                    return validationMessage;
                }
            }
            // mzTab files support
            if (mzTabFilesHaveBeenProvided) {
                // validate the file (parse + validation)
                // mzTab files to validate
                List<DataFile> invalidMzTabFiles = validateMzTabFiles(mzTabDataFiles);
                setProgress(65);
                if (invalidMzTabFiles.size() > 0) {
                    return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getInvalidFilesWarning(invalidMzTabFiles));
                }
                int currentProgressValue = 70;
                setProgress(currentProgressValue);
                // extract SampleMetaData information from the mzTabFile
                int increment = 0;
                if (mzTabDataFiles.size() > 0) {
                    increment = 10 / mzTabDataFiles.size(); // Yes, I want the int truncated float here
                }
                for (DataFile mzTabDataFile :
                        mzTabDataFiles) {
                    mzTabDataFile.setSampleMetaData(MzTabHelper.getSampleMetaData(mzTabDataFile.getMzTabDocument()));
                    currentProgressValue += increment;
                    setProgress(currentProgressValue);
                }
                setProgress(75);
                // Scan mzTab files for ms-run file references that may be missing in the list of provided files,
                // this could render those mzTab files invalid
                Map<DataFile, Set<String>> mzTabFilesMissingReferencedFiles = new HashMap<>();
                mzTabFilesMissingReferencedFiles = checkMzTabFileReferences(mzTabDataFiles);
                // Throw error if missing referenced files
                if (mzTabFilesMissingReferencedFiles.size() > 0) {
                    return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMissingReferencedFilesWarning(mzTabFilesMissingReferencedFiles));
                }
                setProgress(80);
                }

        } else if (submissionType.equals(SubmissionType.PARTIAL)) {
            // should have both search engine output and raw files
            if (noSearchFile || noRawFile) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMissedFileWarning(submissionType, !noResultFile, !noSearchFile, !noRawFile));
            }
            setProgress(40);

            // cannot have supported search engine output, these should be converted to PRIDE XMl
            // using PRIDE Converter
            if (quickValidationResult.hasSupportedSearchFile()) {
                boolean result = WarningMessageGenerator.showSupportedSearchFileWarning();
                scanForFileMappings();
                return new DataFileValidationMessage(result ? ValidationState.SUCCESS : ValidationState.ERROR);
            }
            setProgress(60);

            if (quickValidationResult.hasUnsupportedSearchFile()) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getUnsupportedSearchFileWarning());
            }

            // cannot have result files
            if (!noResultFile) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getResultFileWarning());
            }
            setProgress(80);

            // ms image data
            if (quickValidationResult.hasImagingRawFile() && !quickValidationResult.hasImagingDataFile()) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMissingImageDataFileWarning());
            }
            setProgress(85);

        } else {
            // must have raw files
            if (noRawFile) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getMissedFileWarning(submissionType, !noResultFile, !noSearchFile, !noRawFile));
            }
            setProgress(50);

            // cannot have search engine output or result files
            if (!noSearchFile || !noResultFile) {
                return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getRawOnlyWarning());
            }
            setProgress(80);
        }

        // cannot have unsupported raw files, such as: pkl
        if (quickValidationResult.hasUnsupportedRawFile()) {
            return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getUnsupportedRawFileWarning());
        }

        setProgress(90);
        // cannot have submission px file
        if (quickValidationResult.hasSubmissionPxFile()) {
            return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getSubmissionPxWarning());
        }

        setProgress(95);
        // pre-scan for file relation
        // but only for non-bulkmode
        if (!appContext.isBulkMode()) {
            scanForFileMappings();
        }

        if (!checkIfWiffFileHasScanFile(submission.getDataFiles())) {
            return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getWiffScanMissingWarning());
        }

        if (checkBafFiles(submission.getDataFiles())) {
            return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getBafFileWarning());
        }

        boolean isSdrfFound = false;
        for(DataFile dataFile : submission.getDataFiles()){
            if(dataFile.getFileType().equals(ProjectFileType.EXPERIMENTAL_DESIGN)){
                isSdrfFound = true;
                Set<ValidationError> errors = Main.validate(dataFile.getFilePath(),true);
                if(errors.size() !=0){
                    logger.error("Error in file " + dataFile.getFileName());
                    return new DataFileValidationMessage(ValidationState.ERROR, WarningMessageGenerator.getInvalidSDRFFileWarning());
                }
            }
        }

        setProgress(100);

        if (!isSdrfFound) {
//            App app = (App) App.getInstance();
            JLabel label = new JLabel();
            Font font = label.getFont();
            StringBuilder style = new StringBuilder("font-family:" + font.getFamily() + ";");
            style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
            style.append("font-size:" + font.getSize() + "pt;");
            // html content
            JEditorPane jEditorPane = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" //
                    + WarningMessageGenerator.getExperimentalDesignFileMissingWarning() + "</body></html>");
            jEditorPane.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    try {
                        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                            Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                    }
                }
            });
            jEditorPane.setEditable(false);
            jEditorPane.setBackground(label.getBackground());
            JOptionPane.showConfirmDialog(app.getMainFrame(),
                    jEditorPane,
                    appContext.getProperty("missing.experimental.design.file.dialog.title"),
                    JOptionPane.CLOSED_OPTION, JOptionPane.WARNING_MESSAGE);
            return new DataFileValidationMessage(ValidationState.SUCCESS, WarningMessageGenerator.getExperimentalDesignFileMissingWarning());
        }

        return new DataFileValidationMessage(ValidationState.SUCCESS);
    }

    private List<DataFile> findMissingModifiedFiles(){
        List<DataFile> missingModifiedFile = new ArrayList<>();
        for(Map.Entry<DataFile, ResubmissionFileChangeState> resubmissionFile: resubmission.getResubmission().entrySet()){
            if(resubmissionFile.getValue().equals(ResubmissionFileChangeState.MODIFIED)){
                boolean isFound=false;
                for(DataFile newlyUploadedFile : submission.getDataFiles()){
                    if(newlyUploadedFile.getFileName().equals(resubmissionFile.getKey().getFileName())){
                        isFound=true;
                    }
                }
                if(!isFound){
                    missingModifiedFile.add(resubmissionFile.getKey());
                }
            }
        }
        return missingModifiedFile;
    }



    private boolean checkBafFiles(List<DataFile> dataFiles) {
        for (DataFile dataFile : dataFiles) {
            String fileName = dataFile.getFileName();
            if (fileName.endsWith(MassSpecFileFormat.BRUKER_BAF.getFileExtension())
                    || fileName.endsWith(MassSpecFileFormat.BRUKER_YEP.getFileExtension())
                    || fileName.endsWith(MassSpecFileFormat.BRUKER_FID.getFileExtension())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfWiffFileHasScanFile(List<DataFile> dataFiles) {
        int countOfWiffFiles = 0;
        int countOfWiffScanFiles = 0;
        for (DataFile dataFile : dataFiles) {
            String fileName = dataFile.getFile().getName();
            if (fileName.endsWith(MassSpecFileFormat.ABI_WIFF.getFileExtension())) {
                countOfWiffFiles++;
            }
            if (fileName.endsWith(MassSpecFileFormat.ABI_WIFF.getFileExtension() + ".scan")) {
                countOfWiffScanFiles++;
            }
        }

        if (countOfWiffFiles > 0 && countOfWiffFiles != countOfWiffScanFiles) {
            return false;
        }

        return true;
    }

    private Map<DataFile, Set<String>> checkMzTabFileReferences(List<DataFile> mzTabDataFiles) {
        Map<DataFile, Set<String>> filesMissingReferences = new HashMap<>();
        Map<String, DataFile> dataFiles = new HashMap<>();
        for (DataFile dataFile : Stream.of(submission.getDataFiles(), new ArrayList<>(resubmission.getResubmission().keySet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList())) { // newly addded files + already submitted files
            try {
                // If the datafile is not a file, it is a URL
                URL dataFileToAdd = (dataFile.isFile() ? new URL("file://" + dataFile.getFilePath().toString()) : dataFile.getUrl());
                // We only care about file names
                dataFiles.put(FilenameUtils.getName(dataFileToAdd.toString().toLowerCase()), dataFile);
            } catch (MalformedURLException e) {
                logger.error("PLEASE, REVIEW file reference '" + dataFile.getFilePath().toString() + "' as it could not be parsed as a URL, by adding 'file://' protocol");
            }
        }
        for (DataFile mzTabFile : mzTabDataFiles) {
            for (int msRunIndex :
                    mzTabFile.getMzTabDocument().getMetaData().getAvailableMsRunIndexes()) {
                // According to mzTab format specification, not only the presence of ms-run is mandatory, but also a
                // location specification. This can be null, so we need to take care of that case
                // QUESTION - What if the mzTab references a URL that is not a file, and this has not been included in
                //              the submission files, but it is accesible on the internet? Is it considered a missed
                //              reference?
                // AGREEMENT - URL attachements are not allowed in the submission process, thus, any mzTab file that
                //              contains URL references to non-local files, has to be considered invalid and the user
                //              will get notified about the missing references
                boolean errorFlagged = false;
                String errorLogMsg = "";
                String errorEntry = "";
                if (mzTabFile.getMzTabDocument().getMetaData().getMsRunEntry(msRunIndex).getLocation() != null) {
                    String referencedFile = FilenameUtils.getName(mzTabFile.getMzTabDocument().getMetaData().getMsRunEntry(msRunIndex).getLocation().toString());

                    if (!dataFiles.keySet().stream().anyMatch(dataFile -> dataFile.contains(referencedFile.toLowerCase()))) {
                        // Flag the error
                        errorFlagged = true;
                        // The referenced file is not part of the submission files
                        // Error log message
                        errorLogMsg = "mzTab file '" + mzTabFile.getFilePath()
                                + "' references MISSING FILE '"
                                + mzTabFile.getMzTabDocument().getMetaData().getMsRunEntry(msRunIndex).getLocation().toString()
                                + "'";
                        // Error Entry
                        errorEntry = mzTabFile.getMzTabDocument().getMetaData().getMsRunEntry(msRunIndex).getLocation().toString();
                    } else {
                        // The file is in the list of files part of the current submission process
                        // Check that the referenced file is a Peak List / RAW file
                        DataFile referencedDataFile = dataFiles.get(referencedFile.toLowerCase());
                        if (referencedDataFile == null) {
                            referencedDataFile = dataFiles.get(referencedFile.toLowerCase() + ".gz");
                        }
                        ProjectFileType referencedDataFileType = referencedDataFile.getFileType();
                        if (referencedDataFileType != ProjectFileType.PEAK && referencedDataFileType != ProjectFileType.RAW) {
                            // Flag the error
                            errorFlagged = true;
                            // Log the error
                            errorLogMsg = "mzTab file '" + mzTabFile.getFilePath()
                                    + "' references NON-Peak List/RAW file '"
                                    + referencedFile
                                    + "', which is NOT ALLOWED";
                            // Report the error
                            errorEntry = "NON-Peak List/RAW referenced file '"
                                    + referencedFile
                                    + "', is NOT ALLOWED";
                        } else {
                            mzTabFile.addFileMapping(referencedDataFile);
                        }
                    }
                } else {
                    // ms-run location can't be null for submission purposes
                    errorFlagged = true;
                    errorLogMsg = "mzTab file '" + mzTabFile.getFilePath()
                            + "' references a NULL ms-run location, this is NOT ALLOWED for submissions";
                    errorEntry = "NULL ms-run location is NOT ALLOWED for submissions";
                }
                // Check if an error has been found
                if (errorFlagged) {
                    // Log the error
                    logger.error(errorLogMsg);
                    // Create the error entry for the current file
                    if (!filesMissingReferences.containsKey(mzTabFile)) {
                        filesMissingReferences.put(mzTabFile, new HashSet<String>());
                    }
                    filesMissingReferences.get(mzTabFile).add(errorEntry);
                }
                // And keep checking for other referenced files, to give the user a complete report of all the
                // missing files in one go
            }
        }
        return filesMissingReferences;
    }

    private List<DataFile> validateMzTabFiles(List<DataFile> mzTabDataFiles) {
        List<DataFile> invalidFiles = new ArrayList<>();
        for (DataFile dataFile :
                mzTabDataFiles) {
            // Use the full document quick parser to get the MzTabDocuments in the DataFile objects
            MzTabParser parser = new MzTabFullDocumentQuickParser(dataFile.getFile());
            try {
                // Parse the file
                parser.parse();
                // Set the product document in the DataFile itself for later use
                dataFile.setMzTabDocument(parser.getMzTabDocument());
            } catch (MzTabParserException e) {
                logger.error("Invalid mzTab file '"
                        + dataFile.getFile().getName()
                        + "', MAIN ERROR: '"
                        + e.getMessage()
                        + "'. PLEASE, REFER TO LOG FILES FOR MORE DETAILED INFORMATION");
                invalidFiles.add(dataFile);
            }
        }
        return invalidFiles;
    }

    private void scanForFileMappings() {
        List<DataFile> dataFiles = Stream.of(submission.getDataFiles(), resubmission.getResubmission().keySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toList()); ;
        List<DataFile> resultOrSearchFiles = getResultOrSearchFile(dataFiles, submission.getProjectMetaData().getSubmissionType());
        dataFiles.removeAll(resultOrSearchFiles);

        Set<DataFile> rawFiles = new HashSet<>();
        Set<DataFile> peakFiles = new HashSet<>();

        // single result file, map the rest to the result file
        if (resultOrSearchFiles.size() == 1) {
            DataFile resultOrSearchFile = resultOrSearchFiles.get(0);
            for (DataFile dataFile : dataFiles) {
                if (!resultOrSearchFile.containsFileMapping(dataFile)) {
                    appContext.addFileMapping(resultOrSearchFile, dataFile);
                }
            }
        }

        // scan for every file
        for (DataFile resultOrSearchFile : resultOrSearchFiles) {
            for (DataFile dataFile : dataFiles) {
                if (isDataFileRelated(resultOrSearchFile, dataFile) && !resultOrSearchFile.containsFileMapping(dataFile)) {
                    appContext.addFileMapping(resultOrSearchFile, dataFile);
                }

                if (dataFile.getFileType().equals(ProjectFileType.RAW)) {
                    rawFiles.add(dataFile);
                } else if (dataFile.getFileType().equals(ProjectFileType.PEAK)) {
                    peakFiles.add(dataFile);
                }
            }
        }

        // single raw file
        if (rawFiles.size() == 1) {
            addFileMapping(resultOrSearchFiles, rawFiles.iterator().next());
        }

        // single peak file
        if (peakFiles.size() == 1) {
            addFileMapping(resultOrSearchFiles, peakFiles.iterator().next());
        }
    }

    private void addFileMapping(List<DataFile> resultOrSearchFiles, DataFile dataFile) {
        for (DataFile resultOrSearchFile : resultOrSearchFiles) {
            if (!resultOrSearchFile.containsFileMapping(dataFile)) {
                appContext.addFileMapping(resultOrSearchFile, dataFile);
            }
        }
    }

    private List<DataFile> getResultOrSearchFile(List<DataFile> dataFiles, SubmissionType type) {
        List<DataFile> holder = new ArrayList<DataFile>();

        // decide project file type to look for
        ProjectFileType typeToLookFor;
        if (type.equals(SubmissionType.PARTIAL)) {
            typeToLookFor = ProjectFileType.SEARCH;
        } else {
            typeToLookFor = ProjectFileType.RESULT;
        }

        // find data file
        for (DataFile dataFile : dataFiles) {
            if (dataFile.getFileType().equals(typeToLookFor)) {
                holder.add(dataFile);
            }
        }

        return holder;
    }

    private boolean isDataFileRelated(DataFile fileOne, DataFile fileTwo) {
        String fileOneName = removeUnwantedCharacters(fileOne.getFileName());
        String fileTwoName = removeUnwantedCharacters(fileTwo.getFileName());

        return fileOneName.length() > 1 && fileTwoName.length() > 1 && (fileOneName.startsWith(fileTwoName) || fileTwoName.startsWith(fileOneName));
    }

    private String removeUnwantedCharacters(String fileName) {
        String replacedString = fileName;
        int mid = fileName.lastIndexOf(".");
        if (mid > 0) {
            replacedString = fileName.substring(0, mid + 1).toLowerCase();
        }

        return replacedString.replace("-", "").replace("_", "").replace(".", "").replace(" ", "");
    }

    /**
     * Validate a list of data files
     *
     * @param dataFiles a list of data files
     * @return ValidationResult stores all the validation results
     */
    private QuickValidationResult runQuickValidation(List<DataFile> dataFiles) {
        QuickValidationResult result = new QuickValidationResult();
        for (DataFile dataFile : dataFiles) {
            String fileName = dataFile.getFileName();
            logger.debug("runQuickValidation(): SubmissionValidator.validateDataFile(" + fileName + ")");

            ValidationReport validationReport = SubmissionValidator.validateDataFile(dataFile);
            if (validationReport.hasError()) {
                logger.error("runQuickValidation(): SubmissionValidator.validateDataFile(" + fileName + ") ERROR: "
                        + validationReport.getMessages().stream().map(e -> e.toString()).reduce(",", String::concat));
                result.incrementNumOfInvalidFiles();
                // Combine the reports
                result.getValidationReport().combine(validationReport);
            }

            if (Constant.PX_SUBMISSION_SUMMARY_FILE.equals(fileName)) {
                result.setSubmissionPxFile(true);
            }

            MassSpecFileFormat fileFormat = dataFile.getFileFormat();
            ProjectFileType fileType = dataFile.getFileType();
            if (ProjectFileType.RESULT.equals(fileType)) {
                if (dataFile.isUrl()) {
                    result.setUrlBasedResultFilePresent(true);
                }
                if (fileFormat == null) {
                    result.setUnsupportedResultFile(true);
                } else {
                    result.setSupportedResultFile(true);
                    if (MassSpecFileFormat.PRIDE.equals(fileFormat)) {
                        result.setPrideXml(true);
                    } else if (MassSpecFileFormat.MZIDENTML.equals(fileFormat)) {
                        result.setMzIdentML(true);
                    } else if (MassSpecFileFormat.MZTAB.equals(fileFormat)) {
                        result.setMztab(true);
                    }
                }
            } else if (ProjectFileType.RAW.equals(fileType)) {
                if (!isValidRawCompressedFile(dataFile)) {
                    result.setUnsupportedRawFile(true);
                } else {
                    result.setSupportedRawFile(true);
                    if (MassSpecFileFormat.IBD.equals(fileFormat)) {
                        result.setImagingRawFile(true);
                    }
                }
            } else if (ProjectFileType.SEARCH.equals(fileType)) {
                if (PrideConverterSupport.isSupported(dataFile)) {
                    result.setSupportedSearchFile(true);
                } else if (MassSpecFileFormat.PRIDE.equals(fileFormat) || MassSpecFileFormat.MZIDENTML.equals(fileFormat)) {
                    result.setUnsupportedSearchFile(true);
                }
            } else if (ProjectFileType.MS_IMAGE_DATA.equals(fileType)) {
                result.setImagingDataFile(true);
            }
        }

        return result;
    }

    /**
     * There cannot be multiple raw files in zipped into a single zip file.
     * This method checks if the zipped file contain more than one raw files.
     *
     * @param dataFile Data file
     * @return Boolean values. If multiple raw files found, returns false.
     */
    private boolean isValidRawCompressedFile(DataFile dataFile) {
        boolean isValid = true;
//        int rawFileCount = 0;
//
//        String ext = FileUtil.getFileExtension(dataFile.getFile());
//
//        try {
//            if (ext != null) {
//                if ("zip".equalsIgnoreCase(ext)) {
//                    ZipFile zipFile = new ZipFile(dataFile.getFile());
//                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
//                    while (entries.hasMoreElements() && rawFileCount <= 1) {
//                        ZipEntry entry = entries.nextElement();
//                        if (!entry.isDirectory()) {
//                            String fileName = entry.getName();
//                            String fileExtension = FileUtil.getFileExtension(fileName);
//                            if (fileExtension != null) {
//                                if (fileExtension.toUpperCase().equals("RAW")) {
//                                    rawFileCount++;
//                                }
//                            } else {
//                                isValid = false;
//                                break;
//                            }
//                        }
//                    }
//
//                } // gzip can compress only single file, therefore, we do not need to check gzip
//            }
//        } catch (IOException e) {
//            isValid = false;
//            e.printStackTrace();
//        }
//
//       if ((rawFileCount > 1)) {
//          isValid = false;
//           logger.error("Multiple .RAW files found in " + dataFile.getFile().getName() + ". Please unzip them and upload individually");
//        }
        return isValid;
    }

    /**
     * Scan pride xml for sample related metadata, this will avoid asking user
     * to input them again
     */
    private void scanPrideXmlSampleDetails(List<DataFile> dataFiles) throws IOException {
        for (DataFile dataFile : dataFiles) {
            InputStream prideInputStream = null;

            try {
                // get file as input stream
                prideInputStream = FileUtil.getFileInputStream(dataFile.getFile());

                // scan the file
                StringBuilder builder = readSampleDescription(prideInputStream);

                // parse sample description
                String sample = builder.toString().trim();
                if (sample.length() > 0) {
                    PrideXmlUnmarshaller unmarshaller = PrideXmlUnmarshallerFactory.getInstance().initializeUnmarshaller();
                    try {
                        SampleDescription sampleDescription = unmarshaller.unmarshal(sample, SampleDescription.class);
                        updateSampleMetaData(dataFile, sampleDescription);
                    } catch (JAXBException e) {
                        logger.error("Failed to parse sample description for PRIDE XML file: " + dataFile.getFile().getName(), e);
                    }
                }
            } finally {
                if (prideInputStream != null) {
                    prideInputStream.close();
                }
            }
        }
    }

    /**
     * Read sample description xml from pride xml file input stream
     */
    private StringBuilder readSampleDescription(InputStream prideInputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(prideInputStream));
        StringBuilder builder = new StringBuilder();
        boolean withinSampleDescription = false;
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = PRIDEXML_SAMPLE_DESCRIPTION_PATTERN.matcher(line);
            if (line.contains(PRIDEXML_SAMPLE_DESCRIPTION_END) || matcher.matches()) {
                builder.append(line);
                break;
            } else if (line.contains(PRIDEXML_SAMPLE_DESCRIPTION_BEGIN) || withinSampleDescription) {
                builder.append(line);
                withinSampleDescription = true;
            }
        }
        return builder;
    }

    /**
     * Update sample metadata of a given data file using a given sample
     * description
     */
    private void updateSampleMetaData(DataFile dataFile, SampleDescription sampleDescription) {
        SampleMetaData sampleMetaData = dataFile.getSampleMetaData();

        if (sampleMetaData == null) {
            sampleMetaData = new SampleMetaData();
            dataFile.setSampleMetaData(sampleMetaData);
        }

        List<CvParam> cvParams = sampleDescription.getCvParam();
        for (CvParam param : cvParams) {
            String cvLabel = param.getCvLabel();

            SampleMetaData.Type type = SampleInformationScanHelper.getSampleMetaDataType(cvLabel);

            if (type != null) {
                uk.ac.ebi.pride.data.model.CvParam value = new uk.ac.ebi.pride.data.model.CvParam(cvLabel, param.getAccession(), param.getName(), null);
                sampleMetaData.addMetaData(type, value);
            }
        }
    }

    /**
     * validate whether pride xml contains protein identifications
     *
     * @param prideXmlDatafiles a list of data files
     * @return a list of invalid pride xml
     */
    private List<DataFile> runPrideXmlProteinIdentValidation(List<DataFile> prideXmlDatafiles) throws IOException {
        List<DataFile> invalidPrideXmlFiles = new ArrayList<DataFile>();

        for (DataFile dataFile : prideXmlDatafiles) {
            File file = dataFile.getFile();
            if (!isCompressed(file)) {
                InputStream inputStream = null;
                try {
                    inputStream = FileUtil.getFileInputStream(file);
                    String endOfFile = FileUtil.tail(file, 7000);

                    if (!endOfFile.contains(PRIDEXML_GEL_FREE_IDENTIFICATION) && !endOfFile.contains(PRIDEXML_TWO_DIMENSIONAL_IDENTIFICATION)) {
                        invalidPrideXmlFiles.add(dataFile);
                    }

                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        }

        return invalidPrideXmlFiles;
    }

    /**
     * validate whether mzIdentML is version 1.1.0
     *
     * @param dataFiles a list of all data files
     * @return a list of invalid mzIdentML files
     */
    private List<DataFile> runMzIdentMLVersionValidation(List<DataFile> dataFiles) throws IOException {
        List<DataFile> invalidMzIdentMLFiles = new ArrayList<DataFile>();

        for (DataFile dataFile : dataFiles) {
            BufferedReader fileReader = null;
            InputStream inputStream = null;
            try {
                inputStream = FileUtil.getFileInputStream(dataFile.getFile());
                // read the first ten lines
                fileReader = new BufferedReader(new InputStreamReader(inputStream));

                boolean correctVersion = false;
                for (int i = 0; i < 5; i++) {
                    String line = fileReader.readLine();
                    if (line.contains("version=\"" + MZIDENTML_ACCEPTED_VERSION_1_1_0 + "\"") || line.contains("version=\"" + MZIDENTML_ACCEPTED_VERSION_1_2_0 + "\"")) {
                        correctVersion = true;
                        break;
                    }
                }

                if (!correctVersion) {
                    invalidMzIdentMLFiles.add(dataFile);
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (fileReader != null) {
                    fileReader.close();
                }
            }

        }

        return invalidMzIdentMLFiles;
    }

    /**
     * Validate whether mzIdentML contains SpectraData section
     *
     * @param dataFiles a list of all data files
     * @return a list of invalid mzIdentMl files which don't contain SpectraData
     * section
     * @throws IOException
     */
    private List<DataFile> runMzIdentMLSpectraDataValidation(List<DataFile> dataFiles) throws IOException {
        List<DataFile> invalidMzIdentMLFiles = new ArrayList<>();

        for (DataFile dataFile : dataFiles) {
            if (MassSpecFileFormat.MZIDENTML.equals(dataFile.getFileFormat())) {
                Set<String> peakListFileNames = parsePeakListFileNames(dataFile.getFile());

                if (peakListFileNames.isEmpty()) {
                    invalidMzIdentMLFiles.add(dataFile);
                }
            }
        }

        return invalidMzIdentMLFiles;
    }

    /**
     * Validate whether the peak list files referenced by mzIdentML files are
     * present
     *
     * @param dataFiles a list of all data files
     * @return a map of original mzIdentML data file and missing peak list file
     * name
     */
    private Map<DataFile, List<String>> runMzIdentMLPeakListFileScanAndValidation(List<DataFile> dataFiles, List<DataFile> resubmissionSubmittedFiles) throws IOException {
        Map<DataFile, List<String>> invalidMzIdentMLFiles = new HashMap<>();
        for (DataFile dataFile : dataFiles) {
            if (MassSpecFileFormat.MZIDENTML.equals(dataFile.getFileFormat())) {
                Set<String> peakListFileNames = parsePeakListFileNames(dataFile.getFile());
                for (String peakListFileName : peakListFileNames) {
                    boolean present = false;
                    for (DataFile spectraFile : dataFiles) { // newly added files
                        String spectraFileName = FileUtil.getDecompressedFileName(spectraFile.getFile());
                        if (peakListFileName.equalsIgnoreCase(spectraFileName)) {
                            present = true;
                            break;
                        }
                    }
                    for (DataFile spectraFile : resubmissionSubmittedFiles) { // already submitted files
                        if (peakListFileName.equalsIgnoreCase(spectraFile.getFileName())) {
                            present = true;
                            break;
                        }
                    }
                    if (!present) {
                        List<String> nonePresentPreakListFiles = invalidMzIdentMLFiles.get(dataFile);
                        if (nonePresentPreakListFiles == null) {
                            nonePresentPreakListFiles = new ArrayList<>();
                            invalidMzIdentMLFiles.put(dataFile, nonePresentPreakListFiles);
                        }
                        nonePresentPreakListFiles.add(peakListFileName);
                    }
                }
            }
        }
        return invalidMzIdentMLFiles;
    }

    private Set<String> parsePeakListFileNames(File mzIdentMLFile) throws IOException {
        Set<String> peakListFileNames = new HashSet<>();

        InputStream mzIdentMLInputStream = null;

        try {
            // get file as input stream
            mzIdentMLInputStream = FileUtil.getFileInputStream(mzIdentMLFile);

            // scan the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(mzIdentMLInputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(MZIDENTML_PEAK_LIST_FILE_END)) {
                    break;
                }

                Matcher matcher = MZIDENTML_PEAK_LIST_FILE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String filePath = matcher.group(1);
                    String fileName = FileUtil.getRealFileName(filePath);
                    peakListFileNames.add(fileName);
                }
            }

        } finally {
            if (mzIdentMLInputStream != null) {
                mzIdentMLInputStream.close();
            }
        }

        return peakListFileNames;
    }

    private boolean isCompressed(File file) {
        return FileUtil.isZipped(file) || FileUtil.isGzipped(file);
    }

    private static class QuickValidationResult {

        // WARNING - Why nothing has been initialized?
        boolean supportedResultFile;
        boolean urlBasedResultFilePresent;
        boolean prideXml;
        boolean mzIdentML;
        boolean mztab;
        boolean unsupportedResultFile;
        boolean supportedRawFile;
        boolean unsupportedRawFile;
        boolean supportedSearchFile;
        boolean unsupportedSearchFile;
        int numOfInvalidFiles = 0;
        boolean submissionPxFile;
        boolean imagingRawFile;
        boolean imagingDataFile;
        private ValidationReport validationReport = new ValidationReport();

        public boolean hasSupportedResultFile() {
            return supportedResultFile;
        }

        public void setSupportedResultFile(boolean supportedResultFile) {
            this.supportedResultFile = supportedResultFile;
        }

        public boolean isUrlBasedResultFilePresent() {
            return urlBasedResultFilePresent;
        }

        public void setUrlBasedResultFilePresent(boolean urlBasedResultFilePresent) {
            this.urlBasedResultFilePresent = urlBasedResultFilePresent;
        }

        public boolean hasPrideXml() {
            return prideXml;
        }

        public void setPrideXml(boolean prideXml) {
            this.prideXml = prideXml;
        }

        public boolean hasMzIdentML() {
            return mzIdentML;
        }

        public void setMzIdentML(boolean mzIdentML) {
            this.mzIdentML = mzIdentML;
        }

        public void setMztab(boolean mztab) {
            this.mztab = mztab;
        }

        public boolean hasMzTab() {
            return mztab;
        }

        public boolean hasUnsupportedResultFile() {
            return unsupportedResultFile;
        }

        public void setUnsupportedResultFile(boolean unsupportedResultFile) {
            this.unsupportedResultFile = unsupportedResultFile;
        }

        public boolean hasMixedResultFile() {
            return supportedResultFile && unsupportedResultFile;
        }

        public boolean hasResultFile() {
            return supportedResultFile || unsupportedResultFile;
        }

        public boolean hasSupportedRawFile() {
            return supportedRawFile;
        }

        public void setSupportedRawFile(boolean supportedRawFile) {
            this.supportedRawFile = supportedRawFile;
        }

        public boolean hasUnsupportedRawFile() {
            return unsupportedRawFile;
        }

        public void setUnsupportedRawFile(boolean unsupportedRawFile) {
            this.unsupportedRawFile = unsupportedRawFile;
        }

        private boolean hasSubmissionPxFile() {
            return submissionPxFile;
        }

        private void setSubmissionPxFile(boolean submissionPxFile) {
            this.submissionPxFile = submissionPxFile;
        }

        public boolean hasMixedRawFile() {
            return supportedRawFile && unsupportedRawFile;
        }

        public boolean hasRawFile() {
            return supportedRawFile || unsupportedRawFile;
        }

        public boolean hasSupportedSearchFile() {
            return supportedSearchFile;
        }

        public void setSupportedSearchFile(boolean supportedSearchFile) {
            this.supportedSearchFile = supportedSearchFile;
        }

        public boolean hasUnsupportedSearchFile() {
            return unsupportedSearchFile;
        }

        public void setUnsupportedSearchFile(boolean unsupportedSearchFile) {
            this.unsupportedSearchFile = unsupportedSearchFile;
        }

        public boolean hasMixedSearchFile() {
            return supportedSearchFile && unsupportedSearchFile;
        }

        public boolean hasSearchFile() {
            return supportedSearchFile || unsupportedSearchFile;
        }

        public int getNumOfInvalidFiles() {
            return numOfInvalidFiles;
        }

        public void incrementNumOfInvalidFiles() {
            this.numOfInvalidFiles++;
        }

        public boolean hasImagingRawFile() {
            return imagingRawFile;
        }

        public void setImagingRawFile(boolean imagingRawFile) {
            this.imagingRawFile = imagingRawFile;
        }

        public boolean hasImagingDataFile() {
            return imagingDataFile;
        }

        public void setImagingDataFile(boolean imagingDataFile) {
            this.imagingDataFile = imagingDataFile;
        }


        public ValidationReport getValidationReport() {
            return validationReport;
        }

        public void setValidationReport(ValidationReport validationReport) {
            this.validationReport = validationReport;
        }
    }
}
