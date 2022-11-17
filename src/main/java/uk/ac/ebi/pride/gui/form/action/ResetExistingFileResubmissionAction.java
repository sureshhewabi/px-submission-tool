//package uk.ac.ebi.pride.gui.form.action;
//
//import uk.ac.ebi.pride.App;
//import uk.ac.ebi.pride.AppContext;
//import uk.ac.ebi.pride.data.model.DataFile;
//import uk.ac.ebi.pride.data.model.ResubmissionFileChangeState;
//import uk.ac.ebi.pride.toolsuite.gui.GUIUtilities;
//
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//import java.util.List;
//
///**
// * Action for resetting the resubmission files. It will remove newly added files and load ony files that were already
// * submitted with the previous submission
// *
// * @author Rui Wang
// * @version $Id$
// */
//public class ResetExistingFileResubmissionAction extends AbstractAction {
//
////    String userName;
////    String password;
//
//    public ResetExistingFileResubmissionAction() {
//        super(App.getInstance().getDesktopContext().getProperty("resubmission.reset.button.title"),
//                GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("add.file.button.small.icon")));
//        this.putValue(SHORT_DESCRIPTION, App.getInstance().getDesktopContext().getProperty("add.file.button.tooltip"));
//
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//
//        AppContext appContext= (AppContext) App.getInstance().getDesktopContext();
//        List<DataFile> prevouslySubmittedFiles = appContext.getResubmissionRecord().getResubmission().getDataFiles();
//        appContext.getResubmissionRecord().getResubmission().getResubmission().clear();
//        for (DataFile dataFile: prevouslySubmittedFiles) {
//            appContext.getResubmissionRecord().getResubmission().getResubmission().put(dataFile, ResubmissionFileChangeState.NONE);
//        }
//
//
////
////        this.userName = appContext.getSubmissionRecord().getUserName();
////        this.password = appContext.getSubmissionRecord().getPassword();;
////
////        // process the selected files or folder
////        GetPrideProjectFilesTask task = new GetPrideProjectFilesTask(this.userName, this.password.toCharArray());
////
////        // set gui blocker
////        task.setGUIBlocker(new DefaultGUIBlocker(task, GUIBlocker.Scope.NONE, null));
////
////        // execute file selection task
////        App.getInstance().getDesktopContext().addTask(task);
//    }
//}
