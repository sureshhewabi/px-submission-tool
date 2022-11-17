package uk.ac.ebi.pride.gui.form;

import uk.ac.ebi.pride.data.model.ProjectMetaData;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.util.ValidationState;

import javax.help.HelpBroker;
import java.util.Set;

/**
 * Additional metadata such as: parent project, pubmed id, resubmission accession
 *
 * @author Rui Wang
 * @version $Id$
 */
public class AdditionalDatasetDetailsDescriptor extends ContextAwareNavigationPanelDescriptor {
    public AdditionalDatasetDetailsDescriptor(String id, String title, String desc) {
        super(id, title, desc, new AdditionalDatasetDetailsForm());
    }

    /**
     * Method to be performed to show help document
     * Override this method to add help functionality
     */
    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.additional.dataset.details", "javax.help.SecondaryWindow", "main");
    }

    @Override
    public void beforeDisplayingPanel() {

        updateFormContent();

        firePropertyChange(BEFORE_DISPLAY_PANEL_PROPERTY, false, true);
    }

    @Override
    public boolean toSkipPanel() {
        final String resubmissionPxAccession = appContext.getSubmissionRecord().getSubmission().getProjectMetaData().getResubmissionPxAccession();
        return resubmissionPxAccession != null;
    }

    private void updateFormContent() {
        Submission submission = appContext.getSubmissionRecord().getSubmission();
        ProjectMetaData projectMetaData = submission.getProjectMetaData();

        AdditionalDatasetDetailsForm form = (AdditionalDatasetDetailsForm) getNavigationPanel();
        form.setSelectedProjectTags(projectMetaData.getProjectTags());
        form.setPubMedIds(projectMetaData.getPubmedIds());
        form.setReanalysisPXAccessions(projectMetaData.getReanalysisAccessions());
        form.setOtherOmicsDatasetLink(projectMetaData.getOtherOmicsLink());
    }

    @Override
    public void beforeHidingForNextPanel() {
        AdditionalDatasetDetailsForm form = (AdditionalDatasetDetailsForm) getNavigationPanel();
        ValidationState state = form.doValidation();
        if (!ValidationState.ERROR.equals(state)) {
            // save user input
            saveFormContent();

            // hide warnings
            form.hideWarnings();

            // notify success
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
        } else {
            // notify validation error
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, true, false);
        }
    }

    @Override
    public void beforeHidingForPreviousPanel() {
        AdditionalDatasetDetailsForm form = (AdditionalDatasetDetailsForm) getNavigationPanel();
        form.hideWarnings();
        saveFormContent();
        firePropertyChange(BEFORE_HIDING_FOR_PREVIOUS_PANEL_PROPERTY, false, true);
    }

    /**
     * Save the content from the form to AppContext
     */
    private void saveFormContent() {
        AdditionalDatasetDetailsForm form = (AdditionalDatasetDetailsForm) getNavigationPanel();
        Submission submission = appContext.getSubmissionRecord().getSubmission();
        ProjectMetaData projectMetaData = submission.getProjectMetaData();

        projectMetaData.clearProjectTags();
        Set<String> selectedProjectTags = form.getSelectedProjectTags();
        projectMetaData.addProjectTags(selectedProjectTags.toArray(new String[selectedProjectTags.size()]));

        projectMetaData.clearPubmedIds();
        Set<String> pubMedIds = form.getPubMedIds();
        projectMetaData.addPubmedIds(pubMedIds.toArray(new String[pubMedIds.size()]));

        projectMetaData.clearReanalysisPxAccessions();
        Set<String> reanalysisPXAccessions = form.getReanalysisPXAccessions();
        projectMetaData.addReanalysisPxAccessions(reanalysisPXAccessions.toArray(new String[reanalysisPXAccessions.size()]));

        projectMetaData.setOtherOmicsLink(form.getOtherOmicsDatasetLink());
    }
}
