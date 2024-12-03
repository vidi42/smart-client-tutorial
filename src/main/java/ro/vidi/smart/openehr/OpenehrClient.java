/* (C)2024 */
package ro.vidi.smart.openehr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ro.vidi.smart.SmartMetadata;
import ro.vidi.smart.SmartServerClient;

@Component
public class OpenehrClient implements SmartServerClient {

    @Getter
    @Value("${smart.openehr.base.url}")
    private String defaultSmartServerUrl;

    @Getter
    @Value("${smart.openehr.client-id}")
    private String defaultClientId;

    private final ObjectMapper mapper;

    private final RestClient.Builder restClientBuilder;

    public OpenehrClient(ObjectMapper mapper, RestClient.Builder restClientBuilder) {
        this.mapper = mapper;
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public SmartMetadata getSmartMetadata(String smartServerUrl) {
        return null;
    }

    @Override
    public ObjectMapper getMapper() {
        return mapper;
    }

    public String getAqlResult(String openehrServerUrl, String aqlQuery, String accessToken, String ehrId) {

        RestClient restClient = restClientBuilder.baseUrl(openehrServerUrl).build();
        try {
            return restClient.get().uri(uriBuilder -> uriBuilder
                            .path("/query/aql")
                            .queryParam("q", aqlQuery)
                            .queryParam("query_parameters", "ehrId=" + ehrId)
                            .build())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve().body(String.class);
        } catch (HttpClientErrorException ex) {
            return ex.getResponseBodyAs(String.class);
        }

    }
}
