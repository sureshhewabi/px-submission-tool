package uk.ac.ebi.pride.gui.form;

import uk.ac.ebi.pride.data.model.ProjectMetaData;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.util.ValidationState;

import javax.help.HelpBroker;
import java.util.Set;

/**
 * Navigation descriptor for metadata form
 *
 * @author Rui Wang
 * @version $Id$
 *          <p/>
 *          todo: check help
 */
public class ProjectMetaDataDescriptor extends ContextAwareNavigationPanelDescriptor {
    public ProjectMetaDataDescriptor(String id, String title, String desc) {
        super(id, title, desc, new ProjectMetaDataForm());
    }

    /**
     * Method to be performed to show help document
     * Override this method to add help functionality
     */
    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.meta.data", "javax.help.SecondaryWindow", "main");
    }

    @Override
    public void beforeDisplayingPanel() {
        Submission submission = appContext.getSubmissionRecord().getSubmission();
        ProjectMetaDataForm form = (ProjectMetaDataForm) getNavigationPanel();
        form.setProjectMetaData(submission.getProjectMetaData());
        firePropertyChange(BEFORE_DISPLAY_PANEL_PROPERTY, false, true);
    }

    @Override
    public void beforeHidingForNextPanel() {
        // validate the content in the table
        ProjectMetaDataForm form = (ProjectMetaDataForm) getNavigationPanel();
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
        //hide any visible warning balloon tip
        ProjectMetaDataForm form = (ProjectMetaDataForm) getNavigationPanel();
        form.hideWarnings();
        saveFormContent();

        firePropertyChange(BEFORE_HIDING_FOR_PREVIOUS_PANEL_PROPERTY, false, true);
    }

    @Override
    public boolean toSkipPanel() {
        final String resubmissionPxAccession = appContext.getSubmissionRecord().getSubmission().getProjectMetaData().getResubmissionPxAccession();
        return resubmissionPxAccession != null && resubmissionPxAccession.length()>0;
    }

    /**
     * Save the content from the form to AppContext
     */
    private void saveFormContent() {
        ProjectMetaDataForm form = (ProjectMetaDataForm) getNavigationPanel();
        ProjectMetaData metaData = form.getProjectMetaData();
        Submission submission = appContext.getSubmissionRecord().getSubmission();
        ProjectMetaData storedMetaData = submission.getProjectMetaData();
        if (storedMetaData == null) {
            submission.setProjectMetaData(metaData);
        } else {
            storedMetaData.setProjectTitle(metaData.getProjectTitle());
            storedMetaData.setKeywords(metaData.getKeywords());
            storedMetaData.setProjectDescription(metaData.getProjectDescription());
            storedMetaData.setSampleProcessingProtocol(metaData.getSampleProcessingProtocol());
            storedMetaData.setDataProcessingProtocol(metaData.getDataProcessingProtocol());

            Set<String> pubmedIds = metaData.getPubmedIds();
            storedMetaData.addPubmedIds(pubmedIds.toArray(new String[pubmedIds.size()]));

            Set<String> reanalysisAccessions = metaData.getReanalysisAccessions();
            storedMetaData.addReanalysisPxAccessions(reanalysisAccessions.toArray(new String[reanalysisAccessions.size()]));

            storedMetaData.setOtherOmicsLink(metaData.getOtherOmicsLink());
        }
    }
}
