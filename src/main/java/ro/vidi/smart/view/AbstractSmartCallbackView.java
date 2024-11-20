/* (C)2024 */
package ro.vidi.smart.view;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientResponseException;
import ro.vidi.smart.AppState;
import ro.vidi.smart.OidcClient;
import ro.vidi.smart.SmartServerClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractSmartCallbackView<T extends SmartServerClient> extends VerticalLayout implements HasUrlParameter<String> {

    private final T smartServerClient;
    private final OidcClient oidcClient;

    private final TextArea authorizationCode;
    private final TextArea encodedState;
    private final TextArea accessToken;
    private final Button accessData;

    private OIDCTokenResponse accessTokenResponse = null;

    protected AbstractSmartCallbackView(String viewName, String viewDescription, T smartServerClient, OidcClient oidcClient) {

        this.smartServerClient = smartServerClient;
        this.oidcClient = oidcClient;

        authorizationCode = new TextArea("Code");
        authorizationCode.setWidth("100%");
        authorizationCode.setReadOnly(true);
        authorizationCode.setPlaceholder("Code received from the oauth flow");
        authorizationCode.addValueChangeListener(
                event -> {
                    if (event.getValue() != null) {
                        authorizationCode.setHelperComponent(
                                new Html(
                                        """
                                                    <p>View on <a href="https://jwt.io?token=%s" target="_blank">jwt.io</a> (pre-populated).</p>
                                                """
                                                .formatted(event.getValue())));
                    } else {
                        authorizationCode.setHelperComponent(null);
                    }
                });

        encodedState = new TextArea("State");
        encodedState.setWidth("100%");
        encodedState.setReadOnly(true);
        encodedState.setPlaceholder("Code received from the oauth flow");
        encodedState.setHelperComponent(
                new Html(
                        """
                                <p>
                                    Base64 encoded JSON containing the details passed when starting the flow.
                                </p>
                                """));

        Button getAccessTokenButton = new Button("Get access token");
        getAccessTokenButton.addClickListener(event -> obtainAccessToken());

        accessToken = new TextArea("Access Token");
        accessToken.setWidth("100%");
        accessToken.setReadOnly(true);
        accessToken.setHelperComponent(getAccessTokenHelpText());

        Button restartFlowButton = new Button("Go back home");
        restartFlowButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));

        accessData = new Button("Access Data");
        accessData.setEnabled(false);
        accessData.addClickListener(
                event -> {
                    Map<String, List<String>> queryParameters = new HashMap<>();
                    queryParameters.put(
                            "token",
                            Collections.singletonList(
                                    accessTokenResponse
                                            .getOIDCTokens()
                                            .getAccessToken()
                                            .getValue()));
                    queryParameters.put(
                            "patientId",
                            Collections.singletonList(extractPatientId(accessTokenResponse)));
                    queryParameters.put(
                            "state", Collections.singletonList(encodedState.getValue()));
                    getUI().ifPresent(
                            ui ->
                                    ui.navigate(
                                            getDataClientPath(),
                                            new QueryParameters(queryParameters)));
                });

        HorizontalLayout buttonLayout = new HorizontalLayout(restartFlowButton, accessData);
        buttonLayout.setWidth("100%");

        add(
                new H1(viewName),
                new Text(viewDescription),
                authorizationCode,
                encodedState,
                getAccessTokenButton,
                accessToken,
                buttonLayout);
    }

    protected abstract String extractPatientId(OIDCTokenResponse accessTokenResponse);

    private void obtainAccessToken() {

        try {
            AppState state = smartServerClient.decodeState(encodedState.getValue());

            TokenResponse tokenResponse = oidcClient.getAccessToken(state.getTokenUrl(), authorizationCode.getValue(), state.getClientId(), state.getClientSecret(), state.getCallbackUrl());

            if (tokenResponse.indicatesSuccess()) {
                this.accessTokenResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
                this.accessToken.setValue(smartServerClient.toPrettyJson(accessTokenResponse.toJSONObject().toJSONString()));
                this.accessData.setEnabled(true);
            } else {
                this.accessToken.setValue(((TokenErrorResponse) tokenResponse).toJSONObject().toJSONString());
                this.accessData.setEnabled(false);
            }


        } catch (IOException
                 | RestClientResponseException
                 | URISyntaxException
                 | ParseException e) {
            log.error(e.getMessage(), e);
            ViewUtils.showNotificationError("Cannot obtain access token. Check the logs.");
            this.accessData.setEnabled(false);
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        queryParameters.getSingleParameter("code").ifPresent(authorizationCode::setValue);
        queryParameters.getSingleParameter("state").ifPresent(encodedState::setValue);
    }

    protected abstract Html getAccessTokenHelpText();

    protected abstract String getDataClientPath();
}
