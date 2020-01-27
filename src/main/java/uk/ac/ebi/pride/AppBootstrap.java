package uk.ac.ebi.pride;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.util.IOUtilities;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class AppBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(AppBootstrap.class);

    public static void main(String[] args) {
        new AppBootstrap().go();
    }

    /**
     * Method to run the PX Submission tool.
     */
    private void go() {
        StringBuilder cmdBuffer = getCommand();
        callCommand(cmdBuffer);
    }

    private void callCommand(StringBuilder cmdBuffer) {
        Process process;
        try {
            logger.info(cmdBuffer.toString());
            process = Runtime.getRuntime().exec(cmdBuffer.toString());

            StreamProxy errorStreamProxy = new StreamProxy(process.getErrorStream(), System.err);
            StreamProxy outStreamProxy = new StreamProxy(process.getInputStream(), System.out);

            errorStreamProxy.start();
            outStreamProxy.start();

        } catch (IOException e) {
            logger.error("Error while bootstrapping the ProteomeXchange submission tool", e);
        }
    }

    private StringBuilder getCommand() {
        // read bootstrap properties
        Properties bootstrapProps = getBootstrapSettings();

        // createAttributedSequence the command
        StringBuilder cmdBuffer = new StringBuilder();
        cmdBuffer.append("java -cp ");
        if (isWindowsPlatform()) {
            cmdBuffer.append("\"");
        }

        cmdBuffer.append(System.getProperty("java.class.path"));
        if (isWindowsPlatform()) {
            cmdBuffer.append("\"");
        }

        // get proxy details
        String proxyHost = bootstrapProps.getProperty("px.proxy.host");
        String proxyPort = bootstrapProps.getProperty("px.proxy.port");

        if (proxyHost != null && proxyPort != null) {
            cmdBuffer.append(" -Dhttp.proxyHost=");
            cmdBuffer.append(proxyHost);
            cmdBuffer.append(" -Dhttp.proxyPort=");
            cmdBuffer.append(proxyPort);
        }

        // get upload protocol
        String uploadProtocol = bootstrapProps.getProperty("px.upload.protocol");
        if (uploadProtocol != null) {
            cmdBuffer.append(" -Dpx.upload.protocol=");
            cmdBuffer.append(uploadProtocol);
        }

        // get number of threads encrypting
        String numberOfThreadsEncrypting = bootstrapProps.getProperty("px.encryption.threads.size");
        if (numberOfThreadsEncrypting != null) {
            cmdBuffer.append(" -Dpx.encryption.threads.size=");
            cmdBuffer.append(numberOfThreadsEncrypting);
        }

        // Get training setting
        String trainingModeStatus = bootstrapProps.getProperty("training.mode.status");
        if (trainingModeStatus != null) {
            if (trainingModeStatus.equals(AppContext.TRAINING_MODE_STATUS_ON)) {
                cmdBuffer.append(" -Dtraining.mode.status=");
                cmdBuffer.append(trainingModeStatus);
            }
        }

        cmdBuffer.append(" ");
        cmdBuffer.append(App.class.getName());
        return cmdBuffer;
    }

    /**
     * Check whether it is Windows platform
     *
     * @return boolean  true means it is running on windows
     */
    private boolean isWindowsPlatform() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Windows");
    }


    /**
     * Read bootstrap settings from config/config.props file.
     *
     * @return Properties   bootstrap settings.
     */
    public static Properties getBootstrapSettings() {
        // load properties
        Properties props = new Properties();

        InputStream inputStream = null;
        try {
            URL pathURL = IOUtilities.getFullPath(AppBootstrap.class, "config/config.props");
            File file = IOUtilities.convertURLToFile(pathURL);
            // input stream of the property file
            inputStream = new FileInputStream(file);
            props.load(inputStream);
        } catch (IOException e) {
            logger.error("Failed to load config/config.props file", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Failed to close InputStream while reading config.props file", e);
                }
            }
        }

        return props;
    }

    /**
     * StreamProxy redirect the output stream and error stream to screen.
     */
    private static class StreamProxy extends Thread {
        final InputStream is;
        final PrintStream os;

        StreamProxy(InputStream is, PrintStream os) {
            this.is = is;
            this.os = os;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    os.println(line);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }
}
