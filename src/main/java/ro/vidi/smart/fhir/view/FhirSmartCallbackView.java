/* (C)2024 */
package ro.vidi.smart.fhir.view;

import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.router.Route;

import lombok.extern.slf4j.Slf4j;
import ro.vidi.smart.fhir.FhirClient;
import ro.vidi.smart.OidcClient;
import ro.vidi.smart.view.AbstractSmartCallbackView;

@Route(FhirSmartCallbackView.FHIR_CALLBACK_ROUTE)
@Slf4j
public class FhirSmartCallbackView extends AbstractSmartCallbackView<FhirClient> {

    public static final String FHIR_CALLBACK_ROUTE = "fhir/smart-callback";

    public FhirSmartCallbackView(FhirClient fhirClient, OidcClient oidcClient) {
        super("SMART on FHIR Callback", "Now that the authorization code was obtained, exchange it for an access"
                        + " token to be used when accessing the FHIR data.",
                fhirClient, oidcClient);
    }

    @Override
    protected String getDataClientPath() {
        return FhirDataClientView.FHIR_DATA_CLIENT_ROUTE;
    }

    @Override
    protected String extractPatientId(OIDCTokenResponse accessTokenResponse) {
        return String.valueOf(accessTokenResponse.getCustomParameters().get("patient"));
    }

    @Override
    protected Html getAccessTokenHelpText() {
        return new Html(
                """
                        <p>
                            See <a href="https://www.hl7.org/fhir/smart-app-launch/app-launch.html#obtain-access-token" target="_blank">How to exchange authorization code for access token</a> for more details.
                        </p>
                        """);
    }
}
