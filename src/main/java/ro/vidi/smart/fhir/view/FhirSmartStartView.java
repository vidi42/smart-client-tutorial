/* (C)2024 */
package ro.vidi.smart.fhir.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import ro.vidi.smart.OidcClient;
import ro.vidi.smart.fhir.FhirClient;
import ro.vidi.smart.view.AbstractSmartStartView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ro.vidi.smart.fhir.view.FhirSmartStartView.FHIR_SMART_START_ROUTE;

@Route(FHIR_SMART_START_ROUTE)
public class FhirSmartStartView extends AbstractSmartStartView<FhirClient> {

    public static final String FHIR_SMART_START_ROUTE = "fhir/smart-start";

    private final Button discoverUsingFhirMetadata;

    public FhirSmartStartView(FhirClient fhirClient, OidcClient oidcClient) {
        super("SMART on FHIR Start",
                "Configure the FHIR Server URL, discover the Auth URLs, then start the"
                        + " authorization flow.",
                fhirClient, oidcClient);
        discoverUsingFhirMetadata = new Button("Discover URLs using FHIR Metadata");
        discoverUsingFhirMetadata.addClickListener(event -> setSmartMetadata(getSmartServerClient()::getSmartMetadata));
        getSmartMetadatDiscoverLayout().add(discoverUsingFhirMetadata);
    }

    @Override
    protected String getCallbackPath() {
        return FhirSmartCallbackView.FHIR_CALLBACK_ROUTE;
    }

    @Override
    protected Set<String> getDefaultScopes() {
        return new HashSet<>(Arrays.asList("openid", "fhirUser", "profile", "launch/patient", "patient/*.r"));
    }

    @Override
    protected String getSmartServerLabelText() {
        return "FHIR Server URL";
    }

    @Override
    protected Html getSmartServerHelpText() {
        return new Html(
                "<p>See <a href=\"https://launch.smarthealthit.org\""
                        + " target=\"_blank\">https://launch.smarthealthit.org</a> for"
                        + " simulating a FHIR server.</p>");
    }

    @Override
    protected Html getAuthorizationUrlHelpText() {
        return new Html(
                """
                        <div>
                            <p>
                                See <a href="https://www.hl7.org/fhir/smart-app-launch/app-launch.html#obtain-authorization-code" target="_blank">How to obtain authorization code</a> for more details on the URL structure.
                            </p>
                            <p>
                                The state is a JSON containing details passed to the authorization flow and sent back into the callback.
                            </p>
                        </div>
                        """);
    }

    @Override
    protected Html getScopesHelpText() {
        return new Html(
                """
                        <p>
                            See <a href="https://www.hl7.org/fhir/smart-app-launch/scopes-and-launch-context.html" target="_blank">FHIR Scopes and launch context doc.</a> for more details.
                        </p>
                        """);
    }

    @Override
    protected String getDefaultClientId() {
        return "smart-on-fhir-tutorial";
    }

    @Override
    protected String getDefaultClientSecret() {
        return null;
    }

    @Override
    protected void enableDiscoverMetadataButtons(boolean enabled) {
        super.enableDiscoverMetadataButtons(enabled);
        discoverUsingFhirMetadata.setEnabled(enabled);
    }
}
