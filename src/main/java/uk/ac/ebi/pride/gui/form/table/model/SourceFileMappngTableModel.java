package uk.ac.ebi.pride.gui.form.table.model;

import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.gui.util.Constant;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.archive.dataprovider.project.SubmissionType;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Table model for showing all the result files and enable adding file mappings
 *
 * @author Rui Wang
 * @version $Id$
 */
public class SourceFileMappngTableModel extends PxTableModel implements PropertyChangeListener {
    public enum TableHeader {
        FILE_NAME("File Name", "File name"),
        PATH("PATH / URL", "File path or URL"),
        TYPE("Type", "File type");
//        NUMBER_OF_MAPPINGS("#Relations", "Number of related files");
//        MAPPING("Add Relation", "Assign new related files");

        private final String header;

        private final String toolTip;

        private TableHeader(String header, String tooltip) {
            this.header = header;
            this.toolTip = tooltip;
        }

        public String getHeader() {
            return header;
        }

        public String getToolTip() {
            return toolTip;
        }

    }

    private final AppContext appContext;

    public SourceFileMappngTableModel() {
        this.appContext = (AppContext) App.getInstance().getDesktopContext();
        this.appContext.addPropertyChangeListener(this);
    }

    @Override
    protected void initializeTableModel() {
        TableHeader[] headers = TableHeader.values();
        for (TableHeader header : headers) {
            columnNames.put(header.getHeader(), header.getToolTip());
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return super.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    public int getRowCount() {
        SubmissionType submissionType = appContext.getSubmissionType();
        if (submissionType == null) {
            return 0;
        } else {
            return submissionType.equals(SubmissionType.COMPLETE) ?
                    appContext.getSubmissionFilesByType(ProjectFileType.RESULT).size() :
                    appContext.getSubmissionFilesByType(ProjectFileType.SEARCH).size();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        List<DataFile> dataFiles = appContext.getSubmissionType().equals(SubmissionType.COMPLETE) ?
                appContext.getSubmissionFilesByType(ProjectFileType.RESULT) :
                appContext.getSubmissionFilesByType(ProjectFileType.SEARCH);

        if (!dataFiles.isEmpty() && rowIndex >= 0 && columnIndex >= 0) {
            DataFile dataFile = dataFiles.get(rowIndex);

            if (TableHeader.FILE_NAME.getHeader().equals(getColumnName(columnIndex))) {
                if (dataFile.isFile()) {
                    return dataFile.getFile().getName();
                } else {
                    return Constant.URL;
                }
            } else if (TableHeader.PATH.getHeader().equals(getColumnName(columnIndex))) {
                if (dataFile.isFile()) {
                    return dataFile.getFile().getAbsolutePath();
                } else {
                    return dataFile.getUrl().toString();
                }
            } else if (TableHeader.TYPE.getHeader().equals(getColumnName(columnIndex))) {
                return dataFile.getFileType();
            }
//            } else if (TableHeader.NUMBER_OF_MAPPINGS.getHeader().equals(getColumnName(columnIndex))) {
//                return dataFile.getFileMappings().size();
//            }
//            else if (TableHeader.MAPPING.getHeader().equals(getColumnName(columnIndex))) {
//                return dataFile;
//            }
        }

        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (AppContext.ADD_NEW_DATA_FILE.equals(propName) || AppContext.REMOVE_DATA_FILE.equals(propName) || AppContext.CHANGE_DATA_FILE_TYPE.equals(propName)
                || AppContext.ADD_NEW_DATA_FILE_MAPPING.equals(propName) || AppContext.REMOVE_DATA_FILE_MAPPING.equals(propName)
                || AppContext.NEW_SUBMISSION_FILE.equals(propName)
                || AppContext.SUBMISSION_TYPE_CHANGED.equals(propName)) {
            fireTableDataChanged();
        }
    }
}
