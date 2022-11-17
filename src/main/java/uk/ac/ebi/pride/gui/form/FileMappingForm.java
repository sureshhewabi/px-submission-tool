///*
// * Created by JFormDesigner on Thu Oct 27 09:38:15 BST 2011
// */
//
//package uk.ac.ebi.pride.gui.form;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import uk.ac.ebi.pride.data.model.DataFile;
//import uk.ac.ebi.pride.toolsuite.gui.GUIUtilities;
//import uk.ac.ebi.pride.gui.form.table.TableFactory;
//import uk.ac.ebi.pride.gui.form.table.model.SourceFileMappngTableModel;
//import uk.ac.ebi.pride.gui.form.table.model.TargetFileMappingTableModel;
//import uk.ac.ebi.pride.gui.util.BalloonTipUtil;
//import uk.ac.ebi.pride.gui.util.ValidationState;
//import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
//import uk.ac.ebi.pride.archive.dataprovider.project.SubmissionType;
//
//import javax.swing.*;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
//import java.awt.*;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///**
// * This form is responsible for assigning file mappings
// *
// * @author Rui Wang
// * @version $Id$
// */
//public class FileMappingForm extends Form {
//    private static final Logger logger = LoggerFactory.getLogger(FileMappingForm.class);
//    private static final float DEFAULT_TITLE_FONT_SIZE = 15f;
//
//    /**
//     * Result file table
//     */
//    private JTable resultFileTable;
//
//    /**
//     * Result file table model
//     */
//    private SourceFileMappngTableModel resultFileTableModel;
//    /**
//     * Mapping file table
//     */
//    private JTable mappingFileTable;
//
//    /**
//     * Mapping file table model
//     */
//    private TargetFileMappingTableModel mappingFileTableModel;
//
//    FileMappingForm() {
//        initComponents();
//    }
//
//    private void initComponents() {
//        // setup the main panel
//        this.setLayout(new GridLayout(2, 1, 0, 20));
//
//        // result file panel
//        JPanel resultFilePanel = new JPanel(new BorderLayout());
//
//        // result file top panel
//        JPanel resultFileTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//
//        // result file label
//        JLabel resultFileLabel = new JLabel(appContext.getProperty("result.file.label.title"));
//        resultFileLabel.setFont(resultFileLabel.getFont().deriveFont(DEFAULT_TITLE_FONT_SIZE));
//        JLabel resultFileDescLabel = new JLabel(appContext.getProperty("result.file.description.title"));
//        resultFileDescLabel.setForeground(Color.gray);
//
//        resultFileTopPanel.add(resultFileLabel);
//        resultFileTopPanel.add(Box.createRigidArea(new Dimension(5, 0)));
//        resultFileTopPanel.add(resultFileDescLabel);
//        resultFilePanel.add(resultFileTopPanel, BorderLayout.NORTH);
//
//        // result file table
////        resultFileTable = TableFactory.createSourceFileMappingTable();
//
//        // result file table model
//        resultFileTableModel = (SourceFileMappngTableModel) resultFileTable.getModel();
//
//        // register selection listener
//        ListSelectionModel selectionModel = resultFileTable.getSelectionModel();
//        selectionModel.addListSelectionListener(new ResultFileSelectionListener());
//
//        // scroll pane
//        JScrollPane resultFileScrollPane = new JScrollPane(resultFileTable,
//            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        resultFilePanel.add(resultFileScrollPane, BorderLayout.CENTER);
//
//        this.add(resultFilePanel);
//
//        // mapping file panel
//        JPanel mappingFilePanel = new JPanel(new BorderLayout());
//
//        // mapping file top panel
//        JPanel mappingFileTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//
//
//        // mapping file label
//        JLabel mappingFileLabel = new JLabel(appContext.getProperty("mapping.file.label.title"));
//        mappingFileLabel.setFont(resultFileLabel.getFont().deriveFont(DEFAULT_TITLE_FONT_SIZE));
//        JLabel mappingFileDescLabel = new JLabel(appContext.getProperty("mapping.file.description.title"));
//        mappingFileDescLabel.setForeground(Color.gray);
//
//        mappingFileTopPanel.add(mappingFileLabel);
//        mappingFileTopPanel.add(Box.createRigidArea(new Dimension(5, 0)));
//        mappingFileTopPanel.add(mappingFileDescLabel);
//        mappingFilePanel.add(mappingFileTopPanel, BorderLayout.NORTH);
//
//        // mapping file table
//        mappingFileTable = TableFactory.createTargetFileMappingTable();
//
//        // mapping file table model
//        mappingFileTableModel = (TargetFileMappingTableModel) mappingFileTable.getModel();
//
//        //scroll pane
//        JScrollPane mappingFileScrollPane = new JScrollPane(mappingFileTable,
//            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        mappingFilePanel.add(mappingFileScrollPane, BorderLayout.CENTER);
//
//        this.add(mappingFilePanel);
//    }
//
//    @Override
//    public ValidationState doValidation() {
//        ValidationState result;
//        int resultOrSearchCount = 0;
//        List<DataFile> resultOrSearchFiles = SubmissionType.COMPLETE.equals(appContext.getSubmissionType()) ?
//            appContext.getSubmissionFilesByType(ProjectFileType.RESULT) :
//            appContext.getSubmissionFilesByType(ProjectFileType.SEARCH);
//        Set<DataFile> foundMappedRawFiles = new HashSet<>();
//        Set<DataFile> allRawFiles = new HashSet<>(appContext.getSubmissionFilesByType(ProjectFileType.RAW));
//        for (DataFile resultOrSearchFile : resultOrSearchFiles) {
//            if (resultOrSearchFile.getFileMappings().isEmpty() || !resultOrSearchFile.hasRawMappings()) {
//                resultOrSearchCount++;
//                logger.warn("No mapping file(s) found for " + resultOrSearchFile.getFileName());
//            } else if (!resultOrSearchFile.getFileMappings().isEmpty() && resultOrSearchFile.hasRawMappings()) {
//                List<DataFile> fileMappings = resultOrSearchFile.getFileMappings();
//                for (DataFile dataFile : fileMappings) {
//                    if (ProjectFileType.RAW.equals(dataFile.getFileType())) {
//                        foundMappedRawFiles.add(dataFile);
//                    }
//                }
//            }
//        }
//        if ((0<resultOrSearchCount) || (!allRawFiles.equals(foundMappedRawFiles))) {
//            String fileType = SubmissionType.COMPLETE.equals(appContext.getSubmissionType()) ? "result" : "search";
//            if (warningBalloonTip != null) {
//                warningBalloonTip.closeBalloon(); // close previous balloon tip
//            }
//            JLabel newWarningContents;
//            if (0<resultOrSearchCount) {
//                newWarningContents = new JLabel("<html>" +
//                    "<b>Please make sure all '" + fileType + "' files have at least one 'raw' file mapping:</b><br/>" + "<li>" +
//                    resultOrSearchCount + " '" + fileType + "' file" + (resultOrSearchCount<2 ? " is" : "s are") +
//                    " missing file mappings." + "</li>" + "</html>");
//            } else { // (!allRawFiles.equals(foundMappedRawFiles))
//                Set<DataFile> allRawFilesCopy = new HashSet<>();
//                allRawFilesCopy.addAll(allRawFiles);
//                allRawFilesCopy.removeAll(foundMappedRawFiles);
//
//                for(DataFile dataFile : allRawFilesCopy){
//                    logger.warn("Raw file is not mapped! " + dataFile.getFileName());
//                }
//
//                newWarningContents = new JLabel("<html>" +
//                    "<b>Please make sure all 'raw' files have been mapped by at least one '" + fileType + "' file:</b><br/>" + "<li>" +
//                    (allRawFiles.size()-foundMappedRawFiles.size()) + " 'raw' file" +
//                    ((allRawFiles.size()-foundMappedRawFiles.size())<2 ? " has" : "s have") +
//                    " not been mapped." + "</li>" + "</html>");
//            }
//            newWarningContents.setIcon(GUIUtilities.loadIcon(appContext.getProperty("warning.message.icon")));
//            warningBalloonTip = BalloonTipUtil.createErrorBalloonTip(resultFileTable, newWarningContents);
//            showWarnings(); // show balloon warning tip
//            result = ValidationState.ERROR;
//        } else {
//            result = ValidationState.SUCCESS;
//        }
//        return result;
//    }
//
//    /**
//     * Close the warning balloon tip
//     */
//    public void hideWarnings() {
//        if (warningBalloonTip != null && warningBalloonTip.isVisible()) {
//            warningBalloonTip.closeBalloon();
//        }
//    }
//
//    private void showWarnings() {
//        warningBalloonTip.setVisible(true);
//        this.revalidate();
//        this.repaint();
//    }
//
//
//    /**
//     * Get result file table
//     *
//     * @return JTable  result file table
//     */
//    public JTable getResultFileTable() {
//        return resultFileTable;
//    }
//
//    /**
//     * Get mapping file table
//     *
//     * @return JTable  mapping file table
//     */
//    public JTable getMappingFileTable() {
//        return mappingFileTable;
//    }
//
//    /**
//     * Listen to the row selection on the result file table
//     */
//    private class ResultFileSelectionListener implements ListSelectionListener {
//
//        @Override
//        public void valueChanged(ListSelectionEvent e) {
//            if (!e.getValueIsAdjusting()) {
//                int rowNum = resultFileTable.getSelectedRow();
//                int rowCnt = resultFileTable.getRowCount();
//                if (rowCnt > 0 && rowNum >= 0) {
//                    // get table model
//                    // fire a property change event with selected identification id
//                    int columnNum = resultFileTableModel.getColumnIndex(SourceFileMappngTableModel.TableHeader.MAPPING.getHeader());
//
//                    DataFile selectedResultFile = (DataFile) resultFileTableModel.getValueAt(resultFileTable.convertRowIndexToModel(rowNum), columnNum);
//
//                    mappingFileTableModel.setDataFile(selectedResultFile);
//                }
//            }
//        }
//    }
//}
