//package uk.ac.ebi.pride.gui.form;
//
//import uk.ac.ebi.pride.gui.form.comp.ContextAwareNavigationPanelDescriptor;
//import uk.ac.ebi.pride.gui.util.ValidationState;
//import uk.ac.ebi.pride.archive.dataprovider.project.SubmissionType;
//
//import javax.help.HelpBroker;
//
///**
// * Navigation descriptor for file mapping form
// *
// * @author Rui Wang
// * @version $Id$
// */
//public class FileMappingDescriptor extends ContextAwareNavigationPanelDescriptor {
//    public FileMappingDescriptor(String id, String title, String desc) {
//        super(id, title, desc, new FileMappingForm());
//    }
//
//    @Override
//    public void getHelp() {
//        HelpBroker hb = appContext.getMainHelpBroker();
//        hb.showID("help.file.mapping", "javax.help.SecondaryWindow", "main");
//    }
//
//    @Override
//    public boolean toSkipPanel() {
//        return appContext.getSubmissionType().equals(SubmissionType.RAW);
//    }
//
//    @Override
//    public void beforeHidingForNextPanel() {
//        // validate the content in the table
//        FileMappingForm form = (FileMappingForm) getNavigationPanel();
//        ValidationState state = form.doValidation();
//        if (!ValidationState.ERROR.equals(state)) {
//            // notify
//            form.hideWarnings();
//            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, false, true);
//        } else {
//            // notify validation error
//            firePropertyChange(BEFORE_HIDING_FOR_NEXT_PANEL_PROPERTY, true, false);
//        }
//    }
//
//    @Override
//    public void beforeHidingForPreviousPanel() {
//        //hide any visible warning balloon tip
//        FileMappingForm form = (FileMappingForm) getNavigationPanel();
//        form.hideWarnings();
//        firePropertyChange(BEFORE_HIDING_FOR_PREVIOUS_PANEL_PROPERTY, false, true);
//    }
//}
