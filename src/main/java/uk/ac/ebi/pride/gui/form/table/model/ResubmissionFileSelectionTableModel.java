package uk.ac.ebi.pride.gui.form.table.model;

import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.validation.SubmissionValidator;
import uk.ac.ebi.pride.gui.util.Constant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Table model for file selection model
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ResubmissionFileSelectionTableModel extends PxTableModel implements PropertyChangeListener {
    public enum TableHeader {
        FILE_ID("File ID", "File ID"),
        FILE_NAME("File Name", "File name"),
        PATH("PATH / URL", "File path or URL"),
        SIZE("File Size(bytes)", "File size"),
        TYPE("File Type", "File type"),
        REMOVAL("Remove", "Delete file or URL"),
        VALIDATION("Validation", "Validation");

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


    public ResubmissionFileSelectionTableModel() {
        this.appContext = (AppContext) App.getInstance().getDesktopContext();
        this.appContext.addPropertyChangeListener(this);
    }

    @Override
    protected void initializeTableModel() {
        FileSelectionTableModel.TableHeader[] headers = FileSelectionTableModel.TableHeader.values();
        for (FileSelectionTableModel.TableHeader header : headers) {
            columnNames.put(header.getHeader(), header.getToolTip());
        }
    }


    @Override
    public int getRowCount() {
        return appContext.getSubmissionRecord().getSubmission().getDataFiles().size();
    }

    public void removeAllRows(){
        appContext.getSubmissionRecord().getSubmission().removeAllDataFiles();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return FileSelectionTableModel.TableHeader.TYPE.getHeader().equals(getColumnName(columnIndex)) || super.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        List<DataFile> dataFiles = appContext.getSubmissionRecord().getSubmission().getDataFiles();

        if (!dataFiles.isEmpty() && rowIndex >= 0 && columnIndex >= 0) {
            DataFile dataFile = dataFiles.get(rowIndex);

            if (FileSelectionTableModel.TableHeader.FILE_ID.getHeader().equals(getColumnName(columnIndex))) {
                return dataFile.getFileId();
            } else {
                String fileName = dataFile.getFileName();
                if (FileSelectionTableModel.TableHeader.FILE_NAME.getHeader().equals(getColumnName(columnIndex))) {
                    return fileName;
                } else if (FileSelectionTableModel.TableHeader.PATH.getHeader().equals(getColumnName(columnIndex))) {
                    return dataFile.getFilePath();
                } else if (FileSelectionTableModel.TableHeader.TYPE.getHeader().equals(getColumnName(columnIndex))) {
                    return dataFile.getFileType();
                } else if (FileSelectionTableModel.TableHeader.SIZE.getHeader().equals(getColumnName(columnIndex))) {
                    return dataFile.getFileSize();
                } else if (FileSelectionTableModel.TableHeader.REMOVAL.getHeader().equals(getColumnName(columnIndex))) {
                    return dataFile;
                } else if (FileSelectionTableModel.TableHeader.VALIDATION.getHeader().equals(getColumnName(columnIndex))) {
                    return SubmissionValidator.validateDataFile(dataFile).hasSuccess() && !Constant.PX_SUBMISSION_SUMMARY_FILE.equals(fileName);
                }
            }
        }

        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        List<DataFile> dataFiles = appContext.getSubmissionRecord().getSubmission().getDataFiles();

        if (FileSelectionTableModel.TableHeader.TYPE.getHeader().equals(getColumnName(columnIndex))) {
            DataFile dataFile = dataFiles.get(rowIndex);
            appContext.setFileType(dataFile, (ProjectFileType) aValue);
        } else {
            super.setValueAt(aValue, rowIndex, columnIndex);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (AppContext.ADD_NEW_DATA_FILE.equals(propName) || AppContext.REMOVE_DATA_FILE.equals(propName) || AppContext.NEW_SUBMISSION_FILE.equals(propName)) {
            fireTableDataChanged();
        }
    }
}
