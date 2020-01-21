package uk.ac.ebi.pride.gui.task;

import uk.ac.ebi.pride.archive.PGPEncryptionFactory;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.Submission;

import java.util.List;

public class EncryptFileFactory {

    public static void encryptFiles(Submission submission) throws Exception {
        List<DataFile> dataFiles = submission.getDataFiles();
        for (DataFile dataFile : dataFiles) {
            PGPEncryptionFactory.encrypt(dataFile.getFile());
        }
    }
}
