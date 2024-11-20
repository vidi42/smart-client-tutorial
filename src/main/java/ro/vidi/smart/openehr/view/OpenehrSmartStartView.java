/* (C)2024 */
package ro.vidi.smart.openehr.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import ro.vidi.smart.AppState;
import ro.vidi.smart.OidcClient;
import ro.vidi.smart.SmartMetadata;
import ro.vidi.smart.openehr.OpenehrClient;
import ro.vidi.smart.view.AbstractSmartStartView;
import ro.vidi.smart.view.ViewUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Route(OpenehrSmartStartView.OPENEHR_SMART_START_ROUTE)
@Slf4j
public class OpenehrSmartStartView extends AbstractSmartStartView<OpenehrClient> {

    public static final String OPENEHR_SMART_START_ROUTE = "openehr/smart-start";

    protected OpenehrSmartStartView(OpenehrClient openehrClient, OidcClient oidcClient) {
        super("SMART on openEHR start",
                "Configure the openEHR Server URL, discover the Auth URLs, then start the"
                        + " authorization flow.",
                openehrClient, oidcClient);
    }

    @Override
    protected String getDefaultClientId() {
        return "smart-client";
    }

    @Override
    protected String getDefaultClientSecret() {
        return "";
    }

    @Override
    protected String getCallbackPath() {
        return "openehr/smart-callback";
    }

    @Override
    protected Set<String> getDefaultScopes() {
        return new HashSet<>(Arrays.asList("openid", "launch/patient", "patient/aql-*.s"));
    }

    @Override
    protected String getSmartServerLabelText() {
        return "openEHR Server URL";
    }

    @Override
    protected Html getSmartServerHelpText() {
        return new Html(
                "<p></p>");
    }

    @Override
    protected Html getAuthorizationUrlHelpText() {
        return new Html("""
                <div></div>
                """);
    }

    @Override
    protected Html getScopesHelpText() {
        return new Html(
                """
                        <p></p>
                        """);
    }

    @Override
    protected AppState buildAppState(String smartServerUrl) {
        AppState appState = super.buildAppState(smartServerUrl);

        SmartMetadata smartMetadata = getSmartMetadata();
        if (smartMetadata != null) {
            try {
                JSONObject jsonObject = new JSONObject(smartMetadata.getJsonResponse());
                String openehrBaseUrl = jsonObject
                        .getJSONObject("services").getJSONObject("org.openehr.rest").getString("baseUrl");
                appState.setServerUrl(openehrBaseUrl);
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
                ViewUtils.showNotificationError("Cannot parse smart metadata");
            }
        }
        return appState;
    }
}
