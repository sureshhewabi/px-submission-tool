package uk.ac.ebi.pride.gui.form;

import uk.ac.ebi.pride.data.model.*;
import uk.ac.ebi.pride.data.util.ExperimentalFactorUtil;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.util.ValidationState;
import uk.ac.ebi.pride.archive.dataprovider.project.SubmissionType;

import javax.help.HelpBroker;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code SampleMetaDataDescriptor} handles the navigation from and to the {@code SampleMetaDataForm}
 *
 * @author Rui Wang
 * @version $Id$
 * @see SampleMetaDataForm
 */
public class SampleMetaDataDescriptor extends ContextAwareNavigationPanelDescriptor {

    public SampleMetaDataDescriptor(String id, String title, String desc) {
        super(id, title, desc, new SampleMetaDataForm());
    }

    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.exp.details", "javax.help.SecondaryWindow", "main");
    }

    @Override
    public boolean toSkipPanel() {
        final String resubmissionPxAccession = appContext.getSubmissionRecord().getSubmission().getProjectMetaData().getResubmissionPxAccession();
        return !appContext.getSubmissionType().equals(SubmissionType.COMPLETE) || resubmissionPxAccession != null;
    }

    @Override
    public void beforeHidingForNextPanel() {
        // validate the content in the table
        SampleMetaDataForm form = (SampleMetaDataForm) getNavigationPanel();
        ValidationState state = form.doValidation();
        if (!ValidationState.ERROR.equals(state)) {
            saveProjectMetadata();
            // notify
            form.hideWarnings();
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
        } else {
            // notify validation error
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, true, false);
        }
    }

    private void saveProjectMetadata() {
        Submission submission = appContext.getSubmissionRecord().getSubmission();

        // clear existing sample metadata at project level
        ProjectMetaData projectMetaData = submission.getProjectMetaData();
        projectMetaData.clearSpecies();
        projectMetaData.clearTissues();
        projectMetaData.clearDiseases();
        projectMetaData.clearCellTypes();
        projectMetaData.clearInstruments();
        projectMetaData.clearModifications();
        projectMetaData.clearQuantifications();

        // add new sample metadata at project level
        List<DataFile> dataFiles = submission.getDataFiles();
        for (DataFile dataFile : dataFiles) {
            SampleMetaData sampleMetaData = dataFile.getSampleMetaData();
            if (sampleMetaData != null) {
                // species
                Set<CvParam> species = sampleMetaData.getMetaData(SampleMetaData.Type.SPECIES);
                if (species != null) {
                    for (CvParam specy : species) {
                        projectMetaData.addSpecies(specy);
                    }
                }

                // tissues
                Set<CvParam> tissues = sampleMetaData.getMetaData(SampleMetaData.Type.TISSUE);
                if (tissues != null) {
                    for (CvParam tissue : tissues) {
                        projectMetaData.addTissues(tissue);
                    }
                }

                // cell types
                Set<CvParam> cellTypes = sampleMetaData.getMetaData(SampleMetaData.Type.CELL_TYPE);
                if (cellTypes != null) {
                    for (CvParam cellType : cellTypes) {
                        projectMetaData.addCellTypes(cellType);
                    }
                }

                // disease
                Set<CvParam> diseases = sampleMetaData.getMetaData(SampleMetaData.Type.DISEASE);
                if (diseases != null) {
                    for (CvParam disease : diseases) {
                        projectMetaData.addDiseases(disease);
                    }
                }

                // modifications
                Set<CvParam> modifications = sampleMetaData.getMetaData(SampleMetaData.Type.MODIFICATION);
                if (modifications != null) {
                    for (CvParam modification : modifications) {
                        projectMetaData.addModifications(modification);
                    }
                }

                // instruments
                Set<CvParam> instruments = sampleMetaData.getMetaData(SampleMetaData.Type.INSTRUMENT);
                if (instruments != null) {
                    for (CvParam instrument : instruments) {
                        projectMetaData.addInstruments(instrument);
                    }
                }

                // quantifications
                Set<CvParam> quantifications = sampleMetaData.getMetaData(SampleMetaData.Type.QUANTIFICATION_METHOD);
                if (quantifications != null) {
                    for (CvParam quantification : quantifications) {
                        projectMetaData.addQuantifications(quantification);
                    }
                }

                // experimental factor set default values
                // This is a special case, we are adding default value when there is no experimental factor specified
                // In the submission tool, the experimental factor is not mandatory
                Set<CvParam> experimentalFactors = sampleMetaData.getMetaData(SampleMetaData.Type.EXPERIMENTAL_FACTOR);
                if (experimentalFactors == null || experimentalFactors.isEmpty()) {
                    experimentalFactors = new HashSet<CvParam>();
                    experimentalFactors.add(ExperimentalFactorUtil.getExperimentalFactorCvParam(appContext.getProperty("experimental.factor.default.value")));
                    sampleMetaData.setMetaData(SampleMetaData.Type.EXPERIMENTAL_FACTOR, experimentalFactors);
                }
            }
        }


    }

    @Override
    public void beforeHidingForPreviousPanel() {
        //hide any visible warning balloon tip
        SampleMetaDataForm form = (SampleMetaDataForm) getNavigationPanel();
        form.hideWarnings();
        firePropertyChange(BEFORE_HIDING_FOR_PREVIOUS_PANEL_PROPERTY, false, true);
    }


}
