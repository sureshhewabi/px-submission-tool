package uk.ac.ebi.pride.gui.task.encryption;

import uk.ac.ebi.pride.toolsuite.gui.task.Task;

public class EncryptionMessage {
    private Task source;
    private long bytesRead;
    int noOfFilesProcessed;
    int totalNoOfFiles;

    public EncryptionMessage(Task source, long bytesRead, int noOfFilesProcessed, int totalNoOfFiles) {
        this.source = source;
        this.bytesRead = bytesRead;
        this.noOfFilesProcessed = noOfFilesProcessed;
        this.totalNoOfFiles = totalNoOfFiles;
    }

    public Task getSource() {
        return source;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public int getTotalNoOfFiles() {
        return totalNoOfFiles;
    }

    public int getNoOfFilesProcessed() {
        return noOfFilesProcessed;
    }
}
