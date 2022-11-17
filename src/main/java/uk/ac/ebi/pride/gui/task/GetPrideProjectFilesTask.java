package uk.ac.ebi.pride.gui.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.archive.submission.model.File.ProjectFile;
import uk.ac.ebi.pride.archive.submission.model.File.ProjectFileList;
import uk.ac.ebi.pride.gui.data.Credentials;
import uk.ac.ebi.pride.gui.util.PrideRepoRestClient;
import uk.ac.ebi.pride.gui.util.Utils;
import uk.ac.ebi.pride.toolsuite.gui.desktop.DesktopContext;
import uk.ac.ebi.pride.toolsuite.gui.task.TaskAdapter;

import java.nio.charset.Charset;
import java.util.*;

/**
 * GetPrideProjectFilesTask retrieves pride Project files of a given Project(by project accession) using pride web service
 *
 * @author Suresh
 * @version $Id$
 */
public class GetPrideProjectFilesTask extends TaskAdapter<ProjectFileList , String> {

    private static final Logger logger = LoggerFactory.getLogger(GetPrideProjectFilesTask.class);

    private final ObjectMapper objectMapper;

    public final Credentials userCredentials;
    public PrideRepoRestClient prideRepoRestClient;

    /**
     * Constructor
     *
     * @param userName pride user name
     * @param password pride password
     */
    public GetPrideProjectFilesTask(String userName, char[] password) {
        DesktopContext context = App.getInstance().getDesktopContext();
        String submissionWSBaseUrl = context.getProperty("px.submission.ws.base.url");

        userCredentials = new Credentials(userName, new String(password));
        this.objectMapper = Utils.getJacksonObjectMapper();
        prideRepoRestClient = new PrideRepoRestClient(submissionWSBaseUrl);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     * <p/>
     * <p/>
     * Note that this method is executed only once.
     * <p/>
     * <p/>
     * Note: this method is executed in a background thread.
     *
     * @return the computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    protected ProjectFileList doInBackground() throws Exception {
        ProjectFileList projectFileList = new ProjectFileList();
        DesktopContext context = App.getInstance().getDesktopContext();
        String prideProjectFilesUrl = context.getProperty("px.project.files.url");

        try {

            // set uri parameters
            Map<String, String> uriParams = new HashMap<>();
            uriParams.put("accession", "PXD035185");

           String response = prideRepoRestClient.sendGetRequestWithRetry(prideProjectFilesUrl, uriParams, null, userCredentials);
            ProjectFile[] projectFiles = objectMapper.readValue(response, ProjectFile[].class);
            for ( ProjectFile projectFile:projectFiles) {
                projectFileList.addProjectFile(projectFile);
            }
            logger.info("projectFileList file count:" + projectFileList.getProjectFiles().size());
            return projectFileList;
//            return response;

        } catch (ResourceAccessException resourceAccessException) {
            publish("Proxy/Firewall issue");
        } catch (HttpClientErrorException ex) {
            publish("Client Error");
        }
        return null;
    }



    HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }
}

