/* (C)2024 */
package ro.vidi.smart.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.ParseException;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import ro.vidi.smart.AppState;
import ro.vidi.smart.OidcClient;
import ro.vidi.smart.SmartMetadata;
import ro.vidi.smart.SmartServerClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public abstract class AbstractSmartStartView<T extends SmartServerClient> extends VerticalLayout {

    @Getter
    private final T smartServerClient;
    private final OidcClient oidcClient;

    private final TextArea smartServerUrlTextArea;
    private final Button discoverUsingOidcConfig;
    @Getter
    private final HorizontalLayout smartMetadatDiscoverLayout;
    private final TextArea metadataAuthorizeUrl;
    private final TextArea metadataTokenUrl;
    private final TextField clientIdTextField;
    private final TextField clientSecretTextField;
    private final MultiSelectComboBox<String> scopesMultiSelectComboBox;
    private final TextArea authorizationUrlTextArea;
    private final TextArea metadataFullContent;

    @Getter
    private SmartMetadata smartMetadata = null;

    protected AbstractSmartStartView(String viewName, String viewDescription, T smartServerClient, OidcClient oidcClient) {

        this.smartServerClient = smartServerClient;
        this.oidcClient = oidcClient;

        smartServerUrlTextArea = new TextArea(getSmartServerLabelText());
        smartServerUrlTextArea.setWidth("100%");
        smartServerUrlTextArea.setRequired(true);
        smartServerUrlTextArea.addValueChangeListener(
                event -> enableDiscoverMetadataButtons(isValidURL(event.getValue())));
        smartServerUrlTextArea.setHelperComponent(getSmartServerHelpText());

        Button defaultFhirServerUrlButton = new Button("Use default");
        defaultFhirServerUrlButton.addClickListener(
                event -> smartServerUrlTextArea.setValue(smartServerClient.getDefaultSmartServerUrl()));

        HorizontalLayout smartServerUrlDiscoverLayout = new HorizontalLayout(smartServerUrlTextArea, defaultFhirServerUrlButton);
        smartServerUrlDiscoverLayout.setWidth("100%");
        smartServerUrlDiscoverLayout.setVerticalComponentAlignment(
                Alignment.CENTER, defaultFhirServerUrlButton);
        discoverUsingOidcConfig = new Button("Discover URLs using OIDC Configuration");
        discoverUsingOidcConfig.addClickListener(event -> setSmartMetadata(smartServerUrl -> {
            try {
                return oidcClient.getWellKnownInfo(smartServerUrl);
            } catch (IOException | ParseException e) {
                log.error(e.getMessage(), e);
                ViewUtils.showNotificationError("Cannot build the state object. Check the logs.");
            }
            return null;
        }));

        smartMetadatDiscoverLayout =
                new HorizontalLayout(discoverUsingOidcConfig);
        smartMetadatDiscoverLayout.setWidth("100%");

        metadataAuthorizeUrl = new TextArea("Authorize URL");
        metadataAuthorizeUrl.setReadOnly(true);
        metadataAuthorizeUrl.setWidth("100%");

        metadataTokenUrl = new TextArea("Token URL");
        metadataTokenUrl.setReadOnly(true);
        metadataTokenUrl.setWidth("100%");

        metadataFullContent = new TextArea("Metadata content");
        metadataFullContent.setReadOnly(true);
        metadataFullContent.setWidth("100%");
        metadataFullContent.setMaxHeight("200px");

        VerticalLayout urlsLayout = new VerticalLayout(metadataAuthorizeUrl, metadataTokenUrl);
        urlsLayout.setWidth("100%");
        VerticalLayout fullLayout = new VerticalLayout(metadataFullContent);
        fullLayout.setWidth("100%");

        Accordion metadataAccordion = new Accordion();
        metadataAccordion.setWidth("100%");
        metadataAccordion.add("Auth Flow URLs", urlsLayout);
        metadataAccordion.add("Full metadata", fullLayout);

        clientIdTextField = new TextField("Client ID");
        clientIdTextField.setWidth("100%");
        clientIdTextField.setRequired(true);
        clientIdTextField.setValue(getDefaultClientId());
        clientIdTextField.addValueChangeListener(event -> changeAuthorizationUrl());

        clientSecretTextField = new TextField("Client Secret");
        clientSecretTextField.setWidth("100%");
        clientSecretTextField.setValue(getDefaultClientSecret());
        clientSecretTextField.addValueChangeListener(event -> changeAuthorizationUrl());

        Set<String> defaultScopes = getDefaultScopes();

        scopesMultiSelectComboBox = new MultiSelectComboBox<>("Scopes");
        scopesMultiSelectComboBox.setWidth("100%");
        scopesMultiSelectComboBox.setItems(defaultScopes);
        scopesMultiSelectComboBox.select(defaultScopes);
        scopesMultiSelectComboBox.setAllowCustomValue(true);
        scopesMultiSelectComboBox.setRequired(true);
        scopesMultiSelectComboBox.addCustomValueSetListener(
                e -> {
                    String customValue = e.getDetail();
                    Set<String> selectedItems = new HashSet<>(scopesMultiSelectComboBox.getValue());
                    defaultScopes.add(customValue);
                    selectedItems.add(customValue);
                    scopesMultiSelectComboBox.setItems(defaultScopes);
                    scopesMultiSelectComboBox.setValue(selectedItems);
                });
        scopesMultiSelectComboBox.setHelperComponent(getScopesHelpText());
        scopesMultiSelectComboBox.addValueChangeListener(event -> changeAuthorizationUrl());

        var parametersLayout = new HorizontalLayout(clientIdTextField, clientSecretTextField, scopesMultiSelectComboBox);
        parametersLayout.setWidth("100%");

        authorizationUrlTextArea = new TextArea("Authorization URL");
        authorizationUrlTextArea.setReadOnly(true);
        authorizationUrlTextArea.setWidth("100%");
        authorizationUrlTextArea.setHelperComponent(
                getAuthorizationUrlHelpText());

        var startButton = new Button("Authorize");
        startButton.addClickListener(
                click ->
                        getUI().ifPresent(
                                ui ->
                                        ui.getPage()
                                                .setLocation(
                                                        authorizationUrlTextArea
                                                                .getValue())));
        startButton.addClickShortcut(Key.ENTER);

        add(
                new H1(viewName),
                new Text(viewDescription),
                smartServerUrlDiscoverLayout,
                smartMetadatDiscoverLayout,
                metadataAccordion,
                parametersLayout,
                authorizationUrlTextArea,
                startButton);
    }

    protected abstract String getDefaultClientId();

    protected abstract String getDefaultClientSecret();

    protected void enableDiscoverMetadataButtons(boolean enabled) {
        discoverUsingOidcConfig.setEnabled(enabled);
    }

    protected void setSmartMetadata(Function<String, SmartMetadata> obtainSmartMetadata) {
        var smartServerUrl = smartServerUrlTextArea.getValue();

        if (StringUtils.isBlank(smartServerUrl)) {
            return;
        }

        try {
            this.smartMetadata = obtainSmartMetadata.apply(smartServerUrl);

            if (smartMetadata == null) {
                ViewUtils.showNotificationError("Failed to get the smart metadata.");
                return;
            }

            metadataAuthorizeUrl.setValue(smartMetadata.getAuthorizeUrl());
            metadataTokenUrl.setValue(smartMetadata.getTokenUrl());
            metadataFullContent.setValue(smartServerClient.toPrettyJson(smartMetadata.getJsonResponse()));

            authorizationUrlTextArea.setValue(getAuthorizationUrl(smartServerUrl));
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            ViewUtils.showNotificationError("Failed to read smart URLs. Check the logs.");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            ViewUtils.showNotificationError("Cannot build the state object. Check the logs.");
        }
    }

    private void changeAuthorizationUrl() {
        if (authorizationUrlTextArea != null
                && StringUtils.isNoneBlank(
                smartServerUrlTextArea.getValue(), clientIdTextField.getValue())
                && !CollectionUtils.isEmpty(scopesMultiSelectComboBox.getValue())) {
            try {
                authorizationUrlTextArea.setValue(getAuthorizationUrl(smartServerUrlTextArea.getValue()));
            } catch (URISyntaxException | JsonProcessingException e) {
                log.error(e.getMessage(), e);
                ViewUtils.showNotificationError("Failed to build authorization URL. Check the logs.");
            }
        }
    }

    private String getAuthorizationUrl(String smartServerUrl) throws URISyntaxException, JsonProcessingException {
        return oidcClient
                .buildAuthorizationUrl(
                        smartServerUrl,
                        metadataAuthorizeUrl.getValue(),
                        clientIdTextField.getValue(),
                        scopesMultiSelectComboBox.getValue(),
                        getCallbackPath(),
                        buildAppState(smartServerUrl))
                .toString();
    }

    protected AppState buildAppState(String smartServerUrl) {
        AppState state = new AppState();
        state.setTokenUrl(metadataTokenUrl.getValue());
        state.setOtherDetails("local_details");
        state.setClientId(clientIdTextField.getValue());
        state.setClientSecret(clientSecretTextField.getValue());
        state.setServerUrl(smartServerUrl);

        return state;
    }

    public boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    protected abstract String getCallbackPath();

    protected abstract Set<String> getDefaultScopes();

    protected abstract String getSmartServerLabelText();

    protected abstract Html getSmartServerHelpText();

    protected abstract Html getAuthorizationUrlHelpText();

    protected abstract Html getScopesHelpText();

}
