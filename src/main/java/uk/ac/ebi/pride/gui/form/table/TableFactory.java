package uk.ac.ebi.pride.gui.form.table;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.TableColumnExt;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.data.model.ResubmissionFileChangeState;
import uk.ac.ebi.pride.gui.form.table.editor.ComboBoxCellEditor;
import uk.ac.ebi.pride.gui.form.table.editor.SampleMetaDataButtonCellEditor;
import uk.ac.ebi.pride.gui.form.table.listener.*;
import uk.ac.ebi.pride.gui.form.table.model.*;
import uk.ac.ebi.pride.gui.form.table.renderer.*;
import uk.ac.ebi.pride.toolsuite.gui.GUIUtilities;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 * TableFactory is responsible for creating all the tables in this application
 *
 * @author Rui Wang
 * @version $Id$
 */
public class TableFactory {
    private static final int CHECKBOX_COLUMN_WIDTH = 20;
    private static final int REMOVAL_COLUMN_WIDTH = 60;
    private static final int MAPPING_COLUMN_WIDTH = 110;
    private static final int MAPPING_COUNT_COLUMN_WIDTH = 120;
    private static final int ANNOTATED_STATUS_COLUMN_WIDTH = 120;
    private static final int ANNOTATED_COLUMN_WIDTH = 120;
    private static final int FILE_TYPE_COLUMN_WIDTH = 150;
    private static final int FILE_NAME_COLUMN_WIDTH = 160;
    private static final int FILE_PATH_COLUMN_WIDTH = 400;
    private static final int FILE_SIZE_COLUMN_WIDTH = 40;
    private static final int CV_NAME_COLUMN_WIDTH = 200;
    private static final int CV_ONTOLOGY_COLUMN_WIDTH = 100;
    private static final int CV_ACCESSION_COLUMN_WIDTH = 100;
    private static final int METADATA_TYPE_COLUMN_WIDTH = 50;


    private TableFactory() {
    }

    /**
     * Create a table for submission file selection
     *
     * @return JTable  file selection table
     */
    public static JTable createFileSelectionTable() {
        FileSelectionTableModel tableModel = new FileSelectionTableModel();
        JTable table = new PxTable(tableModel);

        // hide the id column
        TableColumnExt idColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.FILE_ID.getHeader());
        idColumn.setVisible(false);

        // set file name column width
        TableColumnExt nameColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.FILE_NAME.getHeader());
        nameColumn.setPreferredWidth(FILE_NAME_COLUMN_WIDTH);
        nameColumn.setCellRenderer(new InvalidFileSelectionRenderer());
        DefaultTableCellRenderer nameColumnRenderer = new DefaultTableCellRenderer();
        nameColumnRenderer.setHorizontalAlignment(JLabel.LEFT);
        nameColumn.setCellRenderer(nameColumnRenderer);

        // set file path column width
        TableColumnExt pathColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.PATH.getHeader());
        pathColumn.setPreferredWidth(FILE_PATH_COLUMN_WIDTH);
        pathColumn.setCellRenderer(new InvalidFileSelectionRenderer());
        DefaultTableCellRenderer pathColumnRenderer = new DefaultTableCellRenderer();
        pathColumnRenderer.setHorizontalAlignment(JLabel.LEFT);
        pathColumn.setCellRenderer(pathColumnRenderer);

        // set file size column width
        TableColumnExt filesizeColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.SIZE.getHeader());
        filesizeColumn.setPreferredWidth(FILE_SIZE_COLUMN_WIDTH);
        filesizeColumn.setCellRenderer(new InvalidFileSelectionRenderer());
        DefaultTableCellRenderer filesizeColumnRenderer = new DefaultTableCellRenderer();
        filesizeColumnRenderer.setHorizontalAlignment(JLabel.RIGHT);
        filesizeColumn.setCellRenderer(filesizeColumnRenderer);

        // create combo box to select file type
        TableColumnExt fileTypeColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.TYPE.getHeader());
        fileTypeColumn.setCellRenderer(new ComboBoxCellRenderer(ProjectFileType.values()));
        fileTypeColumn.setCellEditor(new ComboBoxCellEditor(ProjectFileType.values()));
        fileTypeColumn.setMinWidth(FILE_TYPE_COLUMN_WIDTH);
        fileTypeColumn.setMaxWidth(FILE_TYPE_COLUMN_WIDTH);

        // removal column
        String removalColHeader = FileSelectionTableModel.TableHeader.REMOVAL.getHeader();
        final TableColumnExt removalColumn = (TableColumnExt) table.getColumn(removalColHeader);
        removalColumn.setCellRenderer(new RemovalCellRenderer());
        removalColumn.setVisible(false);
        removalColumn.setMaxWidth(REMOVAL_COLUMN_WIDTH);
        removalColumn.setMinWidth(REMOVAL_COLUMN_WIDTH);

        // hide validation column
        TableColumnExt validColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.VALIDATION.getHeader());
        validColumn.setVisible(false);

        // add a listener to show/hide removal column
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int rowCnt = ((TableModel) e.getSource()).getRowCount();
                if (rowCnt > 0) {
                    removalColumn.setVisible(true);
                } else {
                    removalColumn.setVisible(false);
                }
            }
        });

        // add listener for removal
        table.addMouseMotionListener(new TableCellMouseMotionListener(table, removalColHeader));
        table.addMouseListener(new RemoveDataFileListener(table, removalColHeader));


        return table;
    }
//
//    /**
//     * Create a table for storing the source files of the file mappings
//     *
//     * @return JTable  source file mapping table
//     */
//    public static JTable createSourceFileNewResubmissionTable() {
//        SourceFileMappngTableModel tableModel = new SourceFileMappngTableModel();
//        JTable table = new PxTable(tableModel);
//
//        // set file name column width
//        TableColumnExt nameColumn = (TableColumnExt) table.getColumn(SourceFileMappngTableModel.TableHeader.FILE_NAME.getHeader());
//        nameColumn.setPreferredWidth(FILE_NAME_COLUMN_WIDTH);
//
//        // set file path column width
//        TableColumnExt pathColumn = (TableColumnExt) table.getColumn(SourceFileMappngTableModel.TableHeader.PATH.getHeader());
//        pathColumn.setPreferredWidth(FILE_PATH_COLUMN_WIDTH);
//
//        // create combo box to select file type
//        TableColumnExt fileTypeColumn = (TableColumnExt) table.getColumn(SourceFileMappngTableModel.TableHeader.TYPE.getHeader());
//        fileTypeColumn.setMinWidth(FILE_TYPE_COLUMN_WIDTH);
//        fileTypeColumn.setMaxWidth(FILE_TYPE_COLUMN_WIDTH);
//
//        // mapping count column
//        TableColumnExt mappingCountColumn = (TableColumnExt) table.getColumn(SourceFileMappngTableModel.TableHeader.FILE_NAME.getHeader());
//        mappingCountColumn.setCellRenderer(new FileMappingCountCellRenderer());
//        mappingCountColumn.setPreferredWidth(MAPPING_COUNT_COLUMN_WIDTH);
//
//        // mapping column
//        String mappingColHeader = SourceFileMappngTableModel.TableHeader.FILE_NAME.getHeader();
//        final TableColumnExt mappingColumn = (TableColumnExt) table.getColumn(mappingColHeader);
//        String text = App.getInstance().getDesktopContext().getProperty("add.file.mapping.button.title");
//        Icon icon = GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("add.file.mapping.button.small.icon"));
//        mappingColumn.setCellRenderer(new ButtonCellRenderer(text, icon));
//        mappingColumn.setCellEditor(new MappingButtonCellEditor(text, icon));
//        mappingColumn.setMaxWidth(MAPPING_COLUMN_WIDTH);
//        mappingColumn.setMinWidth(MAPPING_COLUMN_WIDTH);
//
//        // add listener for removal
//        table.addMouseMotionListener(new TableCellMouseMotionListener(table, mappingColHeader));
//
//
//        return table;
//    }

    /**
     * Create a table for storing the submitted files with a previous submission(which needs to be resubmitted now)
     *
     * @return JTable  submitted file table
     */
    public static JTable createExistingFilesResubmissionTable() {
        ExistingFilesResubmissionTableModel tableModel = new ExistingFilesResubmissionTableModel();
        JTable table = new PxTable(tableModel);

        // set file name column width
        TableColumnExt nameColumn = (TableColumnExt) table.getColumn(ExistingFilesResubmissionTableModel.TableHeader.FILE_NAME.getHeader());
        nameColumn.setPreferredWidth(FILE_NAME_COLUMN_WIDTH);

        // create combo box to select file type
        TableColumnExt fileTypeColumn = (TableColumnExt) table.getColumn(ExistingFilesResubmissionTableModel.TableHeader.TYPE.getHeader());
        fileTypeColumn.setMinWidth(FILE_TYPE_COLUMN_WIDTH);
        fileTypeColumn.setMaxWidth(FILE_TYPE_COLUMN_WIDTH);

        // set file size column width
        TableColumnExt filesizeColumn = (TableColumnExt) table.getColumn(ExistingFilesResubmissionTableModel.TableHeader.SIZE.getHeader());
        filesizeColumn.setPreferredWidth(FILE_SIZE_COLUMN_WIDTH);
        filesizeColumn.setCellRenderer(new InvalidFileSelectionRenderer());
        DefaultTableCellRenderer filesizeColumnRenderer = new DefaultTableCellRenderer();
        filesizeColumnRenderer.setHorizontalAlignment(JLabel.RIGHT);
        filesizeColumn.setCellRenderer(filesizeColumnRenderer);

        // create combo box to select actions
        TableColumnExt actionColumn = (TableColumnExt) table.getColumn(ExistingFilesResubmissionTableModel.TableHeader.ACTION.getHeader());
        actionColumn.setCellRenderer(new ComboBoxCellRenderer(ResubmissionFileChangeState.values()));
        actionColumn.setCellEditor(new ComboBoxCellEditor(new ResubmissionFileChangeState[]{ResubmissionFileChangeState.NONE,ResubmissionFileChangeState.ADDED, ResubmissionFileChangeState.MODIFIED, ResubmissionFileChangeState.DELETED}));
        actionColumn.setMinWidth(FILE_TYPE_COLUMN_WIDTH);
        actionColumn.setMaxWidth(FILE_TYPE_COLUMN_WIDTH);

        return table;
    }


    /**
     * Create a table for storing the target files of the file mappings
     *
     * @return JTable  target file mapping table
     */
    public static JTable createTargetFileMappingTable() {
        TargetFileMappingTableModel tableModel = new TargetFileMappingTableModel();
        JTable table = new PxTable(tableModel);

        // set file name column width
        TableColumnExt nameColumn = (TableColumnExt) table.getColumn(TargetFileMappingTableModel.TableHeader.FILE_NAME.getHeader());
        nameColumn.setPreferredWidth(FILE_NAME_COLUMN_WIDTH);

        // set file path column width
        TableColumnExt pathColumn = (TableColumnExt) table.getColumn(TargetFileMappingTableModel.TableHeader.PATH.getHeader());
        pathColumn.setPreferredWidth(FILE_PATH_COLUMN_WIDTH);

        // create combo box to select file type
        TableColumnExt fileTypeColumn = (TableColumnExt) table.getColumn(TargetFileMappingTableModel.TableHeader.TYPE.getHeader());
        fileTypeColumn.setMinWidth(FILE_TYPE_COLUMN_WIDTH);
        fileTypeColumn.setMaxWidth(FILE_TYPE_COLUMN_WIDTH);

        // removal column
        String removalColHeader = TargetFileMappingTableModel.TableHeader.REMOVAL.getHeader();
        final TableColumnExt removalColumn = (TableColumnExt) table.getColumn(removalColHeader);
        removalColumn.setCellRenderer(new RemovalCellRenderer());
        removalColumn.setVisible(false);
        removalColumn.setMaxWidth(REMOVAL_COLUMN_WIDTH);
        removalColumn.setMinWidth(REMOVAL_COLUMN_WIDTH);

        // add a listener to show/hide removal column
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int rowCnt = ((TableModel) e.getSource()).getRowCount();
                if (rowCnt > 0) {
                    removalColumn.setVisible(true);
                } else {
                    removalColumn.setVisible(false);
                }
            }
        });

        // add listener for removal
        table.addMouseMotionListener(new TableCellMouseMotionListener(table, removalColHeader));
        table.addMouseListener(new RemoveDataFileMappingListener(table, removalColHeader));


        return table;
    }

    /**
     * Create file mapping table
     *
     * @return JTable  file mapping table
     */
    public static JTable createFileNewResubmissionTable() {
        ResubmissionFileSelectionTableModel tableModel = new ResubmissionFileSelectionTableModel();
        JTable table = new PxTable(tableModel);

        // hide the id column
        TableColumnExt idColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.FILE_ID.getHeader());
        idColumn.setVisible(false);

        // set file name column width
        TableColumnExt nameColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.FILE_NAME.getHeader());
        nameColumn.setPreferredWidth(FILE_NAME_COLUMN_WIDTH);
        nameColumn.setCellRenderer(new InvalidFileSelectionRenderer());
        DefaultTableCellRenderer nameColumnRenderer = new DefaultTableCellRenderer();
        nameColumnRenderer.setHorizontalAlignment(JLabel.LEFT);
        nameColumn.setCellRenderer(nameColumnRenderer);

        // set file path column width
        TableColumnExt pathColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.PATH.getHeader());
        pathColumn.setPreferredWidth(FILE_PATH_COLUMN_WIDTH);
        pathColumn.setCellRenderer(new InvalidFileSelectionRenderer());
        DefaultTableCellRenderer pathColumnRenderer = new DefaultTableCellRenderer();
        pathColumnRenderer.setHorizontalAlignment(JLabel.LEFT);
        pathColumn.setCellRenderer(pathColumnRenderer);

        // set file size column width
        TableColumnExt filesizeColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.SIZE.getHeader());
        filesizeColumn.setPreferredWidth(FILE_SIZE_COLUMN_WIDTH);
        filesizeColumn.setCellRenderer(new InvalidFileSelectionRenderer());
        DefaultTableCellRenderer filesizeColumnRenderer = new DefaultTableCellRenderer();
        filesizeColumnRenderer.setHorizontalAlignment(JLabel.RIGHT);
        filesizeColumn.setCellRenderer(filesizeColumnRenderer);

        // create combo box to select file type
        TableColumnExt fileTypeColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.TYPE.getHeader());
        fileTypeColumn.setCellRenderer(new ComboBoxCellRenderer(ProjectFileType.values()));
        fileTypeColumn.setCellEditor(new ComboBoxCellEditor(ProjectFileType.values()));
        fileTypeColumn.setMinWidth(FILE_TYPE_COLUMN_WIDTH);
        fileTypeColumn.setMaxWidth(FILE_TYPE_COLUMN_WIDTH);

        // removal column
        String removalColHeader = FileSelectionTableModel.TableHeader.REMOVAL.getHeader();
        final TableColumnExt removalColumn = (TableColumnExt) table.getColumn(removalColHeader);
        removalColumn.setCellRenderer(new RemovalCellRenderer());
        removalColumn.setVisible(false);
        removalColumn.setMaxWidth(REMOVAL_COLUMN_WIDTH);
        removalColumn.setMinWidth(REMOVAL_COLUMN_WIDTH);

        // hide validation column
        TableColumnExt validColumn = (TableColumnExt) table.getColumn(FileSelectionTableModel.TableHeader.VALIDATION.getHeader());
        validColumn.setVisible(false);

        // add a listener to show/hide removal column
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int rowCnt = ((TableModel) e.getSource()).getRowCount();
                if (rowCnt > 0) {
                    removalColumn.setVisible(true);
                } else {
                    removalColumn.setVisible(false);
                }
            }
        });

        // add listener for removal
        table.addMouseMotionListener(new TableCellMouseMotionListener(table, removalColHeader));
        table.addMouseListener(new RemoveDataFileListener(table, removalColHeader));


        return table;
    }

    /**
     * Create a tree table to display submission summary
     *
     * @return JTable  submission summary table
     */
    public static JTable createSubmissionSummaryTable() {
        SummaryTableTreeModel tableModel = new SummaryTableTreeModel();
        final JXTreeTable table = new JXTreeTable(tableModel);
        table.setClosedIcon(null);
        table.setLeafIcon(null);
        table.setOpenIcon(null);
        table.setRootVisible(false);

//        tableModel.addTreeModelListener(new TreeModelListener() {
//            @Override
//            public void treeNodesChanged(TreeModelEvent e) {
//                expandAllRows();
//            }
//
//            @Override
//            public void treeNodesInserted(TreeModelEvent e) {
//                expandAllRows();
//            }
//
//            @Override
//            public void treeNodesRemoved(TreeModelEvent e) {
//                expandAllRows();
//            }
//
//            @Override
//            public void treeStructureChanged(TreeModelEvent e) {
//                expandAllRows();
//            }
//
//            private void expandAllRows() {
//                table.expandAll();
//                table.revalidate();
//                table.repaint();
//            }
//        });

        return table;
    }

    /**
     * Species metadata table
     */
    public static JTable createMetadataTable(AbstractMetaDataTableModel tableModel) {
        JTable table = new PxTable(tableModel);
        table.setTableHeader(null);

        // set file name column width
        TableColumnExt nameColumn = (TableColumnExt) table.getColumn(AbstractMetaDataTableModel.TableHeader.NAME.getHeader());
        nameColumn.setPreferredWidth(CV_NAME_COLUMN_WIDTH);

        // set file path column width
        TableColumnExt ontologyColumn = (TableColumnExt) table.getColumn(AbstractMetaDataTableModel.TableHeader.ONTOLOGY.getHeader());
//        ontologyColumn.setPreferredWidth(CV_ONTOLOGY_COLUMN_WIDTH);
        ontologyColumn.setVisible(false);

        // create combo box to select file type
        TableColumnExt accessionColumn = (TableColumnExt) table.getColumn(AbstractMetaDataTableModel.TableHeader.ACCESSION.getHeader());
//        accessionColumn.setPreferredWidth(CV_ACCESSION_COLUMN_WIDTH);
        accessionColumn.setVisible(false);

        // removal column
        String removalColHeader = AbstractMetaDataTableModel.TableHeader.REMOVAL.getHeader();
        final TableColumnExt removalColumn = (TableColumnExt) table.getColumn(removalColHeader);
        removalColumn.setCellRenderer(new RemovalCellRenderer());
        removalColumn.setMaxWidth(REMOVAL_COLUMN_WIDTH);
        removalColumn.setMinWidth(REMOVAL_COLUMN_WIDTH);

        // add listener for removal
        table.addMouseMotionListener(new TableCellMouseMotionListener(table, removalColHeader));
        table.addMouseListener(new RemoveMetadataListener(table, removalColHeader));

        return table;
    }

    public static JTable createParentProjectTable(ProjectTagTableModel tableModel) {
        JTable table = new PxTable(tableModel);

        TableColumnExt selectionColumn = (TableColumnExt) table.getColumn(ProjectTagTableModel.TableHeader.SELECTION.getHeader());
        selectionColumn.setMaxWidth(CHECKBOX_COLUMN_WIDTH);

        return table;
    }


    /**
     * Create result file table for gathering sample metadata
     */
    public static JTable createResultFileTable() {
        ResultFileTableModel tableModel = new ResultFileTableModel();
        JTable table = new PxTable(tableModel);

        // set file name column width
        TableColumnExt nameColumn = (TableColumnExt) table.getColumn(ResultFileTableModel.TableHeader.FILE_NAME.getHeader());
        nameColumn.setPreferredWidth(FILE_NAME_COLUMN_WIDTH);

        // set file path column width
        TableColumnExt pathColumn = (TableColumnExt) table.getColumn(ResultFileTableModel.TableHeader.PATH.getHeader());
        pathColumn.setPreferredWidth(FILE_PATH_COLUMN_WIDTH);

        // create combo box to select file type
        TableColumnExt fileTypeColumn = (TableColumnExt) table.getColumn(ResultFileTableModel.TableHeader.TYPE.getHeader());
        fileTypeColumn.setMinWidth(FILE_TYPE_COLUMN_WIDTH);
        fileTypeColumn.setMaxWidth(FILE_TYPE_COLUMN_WIDTH);

        // annotation status
        TableColumnExt annotationStatusColumn = (TableColumnExt) table.getColumn(ResultFileTableModel.TableHeader.ANNOTATION_STATUS.getHeader());
        annotationStatusColumn.setCellRenderer(new BooleanCellRenderer());
        annotationStatusColumn.setPreferredWidth(ANNOTATED_STATUS_COLUMN_WIDTH);

        // annotation
        String annotationHeader = ResultFileTableModel.TableHeader.ANNOTATION.getHeader();
        final TableColumnExt annotationColumn = (TableColumnExt) table.getColumn(annotationHeader);
        String text = App.getInstance().getDesktopContext().getProperty("add.sample.metadata.button.title");
        Icon icon = GUIUtilities.loadIcon(App.getInstance().getDesktopContext().getProperty("add.sample.metadata.button.small.icon"));
        annotationColumn.setCellRenderer(new ButtonCellRenderer(text, icon));
        annotationColumn.setCellEditor(new SampleMetaDataButtonCellEditor(text, icon));
        annotationColumn.setMaxWidth(ANNOTATED_COLUMN_WIDTH);
        annotationColumn.setMinWidth(ANNOTATED_COLUMN_WIDTH);

        // add listener for removal
        table.addMouseMotionListener(new TableCellMouseMotionListener(table, annotationHeader));

        return table;
    }

    public static JTable createSampleMetaDataTable() {
        SampleMetaDataTableModel tableModel = new SampleMetaDataTableModel();
        JTable table = new PxTable(tableModel);

        // set metadata type column width
        TableColumnExt metaDataTypeColumn = (TableColumnExt) table.getColumn(SampleMetaDataTableModel.TableHeader.METADATA_TYPE.getHeader());
        metaDataTypeColumn.setPreferredWidth(METADATA_TYPE_COLUMN_WIDTH);

        // set metadata value column width
        TableColumnExt metaDataValueColumn = (TableColumnExt) table.getColumn(SampleMetaDataTableModel.TableHeader.METADATA_VALUE.getHeader());
        metaDataValueColumn.setPreferredWidth(CV_NAME_COLUMN_WIDTH);

        // set ontology column width
        TableColumnExt ontologyColumn = (TableColumnExt) table.getColumn(SampleMetaDataTableModel.TableHeader.ONTOLOGY.getHeader());
        ontologyColumn.setMinWidth(CV_ONTOLOGY_COLUMN_WIDTH);
        ontologyColumn.setMaxWidth(CV_ONTOLOGY_COLUMN_WIDTH);
        ontologyColumn.setVisible(false);

        // set ontology accession column width
        TableColumnExt ontologyAccessionColumn = (TableColumnExt) table.getColumn(SampleMetaDataTableModel.TableHeader.ONTOLOGY_ACCESSION.getHeader());
        ontologyAccessionColumn.setMinWidth(CV_ACCESSION_COLUMN_WIDTH);
        ontologyAccessionColumn.setMaxWidth(CV_ACCESSION_COLUMN_WIDTH);
        ontologyAccessionColumn.setVisible(false);

        // removal column
        String removalColHeader = TargetFileMappingTableModel.TableHeader.REMOVAL.getHeader();
        final TableColumnExt removalColumn = (TableColumnExt) table.getColumn(removalColHeader);
        removalColumn.setCellRenderer(new RemovalCellRenderer());
        removalColumn.setVisible(false);
        removalColumn.setMaxWidth(REMOVAL_COLUMN_WIDTH);
        removalColumn.setMinWidth(REMOVAL_COLUMN_WIDTH);

        // add a listener to show/hide removal column
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int rowCnt = ((TableModel) e.getSource()).getRowCount();
                if (rowCnt > 0) {
                    removalColumn.setVisible(true);
                } else {
                    removalColumn.setVisible(false);
                }
            }
        });

        // add listener for removal
        table.addMouseMotionListener(new TableCellMouseMotionListener(table, removalColHeader));
        table.addMouseListener(new RemoveSampleMetaDataListener(table, removalColHeader));


        return table;
    }
}
