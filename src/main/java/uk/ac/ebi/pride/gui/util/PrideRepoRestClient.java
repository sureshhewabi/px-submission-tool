package uk.ac.ebi.pride.gui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.pride.gui.data.Credentials;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class PrideRepoRestClient {

    private static final Logger logger = LoggerFactory.getLogger(PrideRepoRestClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;


    /**
     * Constructor
     * @param baseUrl     PRIDE Repo REST API base URL
     */
    public PrideRepoRestClient(String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    }

    /**
     * This method construct the URL with URI parameters and Query parameters and
     * perform a get call
     * //TODO retry logics
     *
     * @param url         Path after the base URL
     * @param uriParams   URI parameters
     * @param queryParams Query parameters
     * @return JSON object in String format
     */
    public String sendGetRequestWithRetry(String url, Map<String, String> uriParams, MultiValueMap<String, String> queryParams,Credentials credentials) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + url);
        if (queryParams != null) {
            uriBuilder.queryParams(queryParams);
        }
        URI completeUrl = (uriParams != null) ? uriBuilder.buildAndExpand(uriParams).toUri() : uriBuilder.build().toUri();

        return makeGetRequest(completeUrl, credentials);
    }

    /**
     * This method sets HTTP headers, perform the rest call and returns results in String format
     *
     * @param uri constructed URL with URI and query parameters
     * @return
     */
    private String makeGetRequest(URI uri, Credentials credentials) {
        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers;
            if(credentials != null){
                 headers = createHeaders(credentials);
            }else {
                 headers = createHeaders();
            }

            // build the request
            HttpEntity entity = new HttpEntity( headers);

            logger.info("GET Request : " + uri);
            response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            HttpStatus statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.OK && statusCode != HttpStatus.CREATED && statusCode != HttpStatus.ACCEPTED) {
                String errorMessage = "[GET] Received invalid response for : " + uri + " : " + response;
                logger.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        } catch (RestClientException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        return response.getBody();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return headers;
    }

    private HttpHeaders createHeaders(Credentials credentials) {
        HttpHeaders headers = createHeaders();
        String authStr = credentials.getUsername() + ":" + credentials.getPassword();
        String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes());
        headers.set("Authorization", "Basic " + base64Creds);
        return headers;
    }
}
