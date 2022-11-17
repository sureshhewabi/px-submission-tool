package uk.ac.ebi.pride.gui.form;

import uk.ac.ebi.pride.data.model.Contact;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
import uk.ac.ebi.pride.gui.util.ValidationState;

import javax.help.HelpBroker;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class LabHeadDescriptor extends ContextAwareNavigationPanelDescriptor {

    public LabHeadDescriptor(String id, String title, String desc) {
        super(id, title, desc, new LabHeadForm());
    }

    /**
     * Method to be performed to show help document
     * Override this method to add help functionality
     */
    @Override
    public void getHelp() {
        HelpBroker hb = appContext.getMainHelpBroker();
        hb.showID("help.lab.head", "javax.help.SecondaryWindow", "main");
    }

    @Override
    public void beforeDisplayingPanel() {
        Submission submission = appContext.getSubmissionRecord().getSubmission();
        Contact contact = submission.getProjectMetaData().getLabHeadContact();

        if (contact != null) {
            // set previous username and password
            setExistingLabHead(contact);
        }

        firePropertyChange(BEFORE_DISPLAY_PANEL_PROPERTY, false, true);
    }

    private void setExistingLabHead(Contact contact) {
        LabHeadForm form = (LabHeadForm) getNavigationPanel();
        form.setLabHeadName(contact.getName());
        form.setAffiliation(contact.getAffiliation());
        form.setEmail(contact.getEmail());
    }

    @Override
    public void beforeHidingForNextPanel() {
        // validate the content in the table
        LabHeadForm form = (LabHeadForm) getNavigationPanel();
        ValidationState state = form.doValidation();
        if (!ValidationState.ERROR.equals(state)) {
            saveFormContent();
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
        } else {
            // notify validation error
            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, true, false);
        }
    }

    @Override
    public void beforeHidingForPreviousPanel() {
        //hide any visible warning balloon tip
        LabHeadForm form = (LabHeadForm) getNavigationPanel();
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
        LabHeadForm form = (LabHeadForm) getNavigationPanel();
        Submission submission = appContext.getSubmissionRecord().getSubmission();

        Contact contact = submission.getProjectMetaData().getLabHeadContact();
        if (contact == null) {
            contact = new Contact();
            submission.getProjectMetaData().setLabHeadContact(contact);
        }

        contact.setName(form.getLabHeadName());
        contact.setAffiliation(form.getAffiliation());
        contact.setEmail(form.getEmail());
    }
}
