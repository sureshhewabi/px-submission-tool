package uk.ac.ebi.pride.gui.form.table.model;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.AppContext;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.gui.util.Constant;

import javax.swing.tree.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SummaryTableTreeModel provides a table model to display a submission object in tree table format.
 *
 * @author Rui Wang
 * @version $Id$
 */
public class SummaryTableTreeModel extends AbstractTreeTableModel implements PropertyChangeListener {

    public enum TableHeader {
        FILE_NAME("File Name", "File name"),
        PATH("PATH / URL", "File path or URL"),
        TYPE("Type", "File type"),
        SIZE("Size (Mb)", "File size");
//        NUMBER_OF_MAPPINGS("#Mapped files", "Number of mapped source files");

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

    public SummaryTableTreeModel() {
        super(new DataFile());
        this.appContext = (AppContext) App.getInstance().getDesktopContext();
        this.appContext.addPropertyChangeListener(this);
    }

    @Override
    public int getColumnCount() {
        return TableHeader.values().length;
    }

    @Override
    public String getColumnName(int column) {
        return TableHeader.values()[column].getHeader();
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (node.equals(root)) {
            return null;
        }

        DataFile dataFile = (DataFile) node;

        String header = TableHeader.values()[column].getHeader();
        if (TableHeader.FILE_NAME.getHeader().equals(header)) {
            return dataFile.getFileName();
        } else if (TableHeader.PATH.getHeader().equals(header)) {
            return dataFile.getFilePath();
        } else if (TableHeader.TYPE.getHeader().equals(header)) {
            return dataFile.getFileType().toString();
//        } else if (TableHeader.NUMBER_OF_MAPPINGS.getHeader().equals(header)) {
//            return dataFile.getFileMappings().size();
        } else if (TableHeader.SIZE.getHeader().equals(header)) {
            long fileSizeInBytes = dataFile.getFileSize();

            if (fileSizeInBytes >= 0) {
                double fileSize = (fileSizeInBytes * 1.0) / (1024 * 1024);
                DecimalFormat df = new DecimalFormat("#.###");
                return df.format(fileSize);
            } else {
                return Constant.UNKNOWN;
            }
        }

        return null;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent.equals(root)) {
            List<DataFile> dataFiles = appContext.getSubmissionRecord().getSubmission().getDataFiles();
            if (!dataFiles.isEmpty()) {
                return getUnreferencedDataFile(dataFiles, index);
            } else {
                return null;
            }
        }

        DataFile parentDataFile = (DataFile) parent;
        // get all the children
        List<DataFile> childDataFiles = parentDataFile.getFileMappings();
        if (!childDataFiles.isEmpty()) {
            return childDataFiles.get(index);
        }

        return null;
    }

    private DataFile getUnreferencedDataFile(List<DataFile> dataFiles, int index) {
        DataFile result = null;
        int cnt = 0;

        Set<DataFile> referencedDataFiles = getReferencedDataFiles(dataFiles);

        for (DataFile dataFile : dataFiles) {
            if (dataFile.hasMappings() || !referencedDataFiles.contains(dataFile)) {
                if (cnt == index) {
                    result = dataFile;
                    break;
                }
                cnt++;
            }
        }

        return result;
    }

    private Set<DataFile> getReferencedDataFiles(List<DataFile> dataFiles) {
        Set<DataFile> referencedDataFiles = new HashSet<DataFile>();
        for (DataFile dataFile : dataFiles) {
            if (dataFile.hasMappings()) {
                referencedDataFiles.addAll(dataFile.getFileMappings());
            }
        }
        return referencedDataFiles;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent.equals(root)) {
            List<DataFile> dataFiles = appContext.getSubmissionRecord().getSubmission().getDataFiles();
            return countUnreferencedDataFile(dataFiles);
        }

        DataFile parentDataFile = (DataFile) parent;
        // get all the children
        List<DataFile> childDataFiles = parentDataFile.getFileMappings();

        return childDataFiles.size();
    }

    private int countUnreferencedDataFile(List<DataFile> dataFiles) {
        Set<DataFile> referencedDataFiles = getReferencedDataFiles(dataFiles);
        return dataFiles.size() - referencedDataFiles.size();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent.equals(root)) {
            List<DataFile> dataFiles = appContext.getSubmissionRecord().getSubmission().getDataFiles();
            return getIndexOfUnreferencedDataFile(dataFiles, (DataFile) child);
        }

        DataFile parentDataFile = (DataFile) parent;
        // get all the children
        List<DataFile> childDataFiles = parentDataFile.getFileMappings();

        return childDataFiles.indexOf(child);
    }

    private int getIndexOfUnreferencedDataFile(List<DataFile> dataFiles, DataFile file) {
        int cnt = 0;

        Set<DataFile> referencedDataFiles = getReferencedDataFiles(dataFiles);

        for (DataFile dataFile : dataFiles) {
            if (dataFile.hasMappings() || !referencedDataFiles.contains(dataFile)) {
                if (dataFile.equals(file)) {
                    break;
                }
                cnt++;
            }
        }

        return cnt;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (AppContext.ADD_NEW_DATA_FILE.equals(propName) || AppContext.REMOVE_DATA_FILE.equals(propName) || AppContext.CHANGE_DATA_FILE_TYPE.equals(propName)
//                || AppContext.ADD_NEW_DATA_FILE_MAPPING.equals(propName) || AppContext.REMOVE_DATA_FILE_MAPPING.equals(propName)
                || AppContext.NEW_SUBMISSION_FILE.equals(propName)) {
            TreePath path = (root != null) ? new TreePath(root) : null;
            modelSupport.fireTreeStructureChanged(path);
        }
    }
}
