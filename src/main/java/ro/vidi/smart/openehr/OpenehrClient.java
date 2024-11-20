/* (C)2024 */
package ro.vidi.smart.openehr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ro.vidi.smart.SmartMetadata;
import ro.vidi.smart.SmartServerClient;

@Component
public class OpenehrClient implements SmartServerClient {

    @Value("${smart.openehr.base.url}")
    private String defaultOpenehrServerUrl;

    private final ObjectMapper mapper;

    private final RestClient.Builder restClientBuilder;

    public OpenehrClient(ObjectMapper mapper, RestClient.Builder restClientBuilder) {
        this.mapper = mapper;
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public String getDefaultSmartServerUrl() {
        return defaultOpenehrServerUrl;
    }

    @Override
    public SmartMetadata getSmartMetadata(String smartServerUrl) {
        return null;
    }

    @Override
    public ObjectMapper getMapper() {
        return mapper;
    }

    public String getPatientData(String openehrServerUrl, String accessToken, String ehrId) {

        RestClient restClient = restClientBuilder.baseUrl(openehrServerUrl).build();
        return restClient.get().uri(uriBuilder -> uriBuilder
                        .path("/query/aql")
                        .queryParam("q", "SELECT o/data/events/time/value, o/data/events/data/items[at0004]/value/magnitude as systolic, o/data/events/data/items[at0005]/value/magnitude as diastolic FROM EHR e CONTAINS COMPOSITION c CONTAINS OBSERVATION o[openEHR-EHR-OBSERVATION.blood_pressure.v1] WHERE e/ehr_id/value=$ehrId")
                        .queryParam("query_parameters", "ehrId=" + ehrId)
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(String.class);


    }
}
