package uk.ac.ebi.pride.gui.task;

import uk.ac.ebi.pride.archive.PGPEncryptionFactory;
import uk.ac.ebi.pride.data.model.DataFile;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.gui.task.encryption.EncryptionMessage;
import uk.ac.ebi.pride.gui.util.Constant;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EncryptFileTask extends TaskAdapter<Boolean, EncryptionMessage> {

    private Submission submission;

    public EncryptFileTask(Submission submission) {
        this.submission = submission;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        final int NUMBER_OF_THREADS = Integer.parseInt(System.getProperty("px.encryption.threads.size", "1"));
        List<DataFile> dataFiles = submission.getDataFiles();
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Future<Long>> tasks = new ArrayList<>();
        for (DataFile dataFile : dataFiles) {
            tasks.add(executor.submit(new Callable() {
                public Long call() throws Exception {
                    return PGPEncryptionFactory.encrypt(dataFile.getFile());
                }
            }));
        }
        int i = 0;
        int totalNoOfFiles = dataFiles.size();
        Iterator<Future<Long>> it = tasks.iterator();
        while (it.hasNext()) {
            Future future = it.next();
            if (future.isDone()) {
                long bytesRead = (Long) future.get();
                publish(new EncryptionMessage(this, bytesRead, ++i, totalNoOfFiles));
                tasks.remove(future);
            }
            if (!it.hasNext()) {
                it = tasks.iterator();
            }
        }
        return null;
    }
}
