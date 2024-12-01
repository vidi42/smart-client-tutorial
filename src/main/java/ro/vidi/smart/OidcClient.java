/* (C)2024 */
package ro.vidi.smart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.AbstractConfigurationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.WellKnownPathComposeStrategy;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@Component
public class OidcClient {

    private final Environment environment;

    private final ObjectMapper mapper;

    public OidcClient(Environment environment, ObjectMapper mapper) {
        this.environment = environment;
        this.mapper = mapper;
    }

    /**
     * Builds an authorize URL as described in the <a
     * href="https://www.hl7.org/fhir/smart-app-launch/app-launch.html">SMART App Launch Doc</a>.
     *
     * <pre>
     * https://ehr/authorize?
     *      response_type=code&
     *      client_id=app-client-id&
     *      redirect_uri=https%3A%2F%2Fapp%2Fafter-auth&
     *      launch=xyz123&
     *      scope=launch+patient%2FObservation.rs+patient%2FPatient.rs+openid+fhirUser&
     *      state=98wrghuwuogerg97&
     *      aud=https://ehr/fhir
     * </pre>
     * <p>
     * This is similar for FHIR and openEHR.
     */
    public URI buildAuthorizationUrl(
            String smartServerUrl,
            String authorizeUrl,
            String clientId,
            Set<String> scopes,
            String callbackPath,
            AppState state)
            throws URISyntaxException, JsonProcessingException {

        Scope scope = new Scope();
        scopes.forEach(scope::add);
        String callbackUrl = "http://127.0.0.1:" + environment.getProperty("local.server.port") + "/" + callbackPath;
        state.setCallbackUrl(callbackUrl);
        return new AuthenticationRequest.Builder(
                ResponseType.CODE,
                scope,
                new ClientID(clientId),
                new URI(callbackUrl))
                .endpointURI(new URI(authorizeUrl))
                .responseType(ResponseType.CODE)
                .state(new State(new String(Base64.encodeBase64(mapper.writeValueAsString(state).getBytes()))))
                .customParameter("aud", smartServerUrl)
                .build()
                .toURI();
    }

    public TokenResponse getAccessToken(String tokenUrl, String authCode, String clientId, String clientSecret, String callbackUrl)
            throws URISyntaxException, IOException, ParseException {
        URI tokenEndpoint = new URI(tokenUrl);
        AuthorizationGrant codeGrant =
                new AuthorizationCodeGrant(
                        new AuthorizationCode(authCode),
                        new URI(callbackUrl));

        TokenRequest request;
        if (StringUtils.isEmpty(clientSecret)) {
            request = new TokenRequest(tokenEndpoint, new ClientID(clientId), codeGrant);
        } else {
            request = new TokenRequest(tokenEndpoint, new ClientSecretPost(new ClientID(clientId), new Secret(clientSecret)), codeGrant);
        }

        return OIDCTokenResponseParser.parse(request.toHTTPRequest().send());
    }

    public SmartMetadata getWellKnownInfo(String smartServerUrl, String wellKnownPath) throws IOException, ParseException {
        SmartOIDCProviderConfigurationRequest smartOIDCProviderConfigurationRequest =
                new SmartOIDCProviderConfigurationRequest(smartServerUrl, wellKnownPath);
        OIDCProviderMetadata parse =
                OIDCProviderMetadata.parse(
                        smartOIDCProviderConfigurationRequest
                                .toHTTPRequest()
                                .send()
                                .getContentAsJSONObject());

        SmartMetadata smartMetadata = new SmartMetadata();
        smartMetadata.setAuthorizeUrl(parse.getAuthorizationEndpointURI().toString());
        smartMetadata.setTokenUrl(parse.getTokenEndpointURI().toString());
        smartMetadata.setJsonResponse(parse.toJSONObject().toJSONString());

        return smartMetadata;
    }

    static class SmartOIDCProviderConfigurationRequest extends AbstractConfigurationRequest {

        public SmartOIDCProviderConfigurationRequest(String smartServerBaseUrl, String wellKnownPath) {
            super(URI.create(smartServerBaseUrl), wellKnownPath, WellKnownPathComposeStrategy.POSTFIX);
        }
    }

}
