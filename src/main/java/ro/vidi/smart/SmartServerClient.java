package ro.vidi.smart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

public interface SmartServerClient {

    String getDefaultSmartServerUrl();

    String getDefaultClientId();

    SmartMetadata getSmartMetadata(String smartServerUrl);

    default AppState decodeState(String stateEncoded) throws IOException {
        return getMapper().readValue(Base64.decodeBase64(stateEncoded.getBytes()), AppState.class);
    }

    default String toPrettyJson(String json) throws JsonProcessingException {
        return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(getMapper().readTree(json));
    }

    ObjectMapper getMapper();
}
