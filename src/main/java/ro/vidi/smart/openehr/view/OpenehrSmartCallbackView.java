/* (C)2024 */
package ro.vidi.smart.openehr.view;

import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import ro.vidi.smart.OidcClient;
import ro.vidi.smart.openehr.OpenehrClient;
import ro.vidi.smart.view.AbstractSmartCallbackView;
import ro.vidi.smart.view.ViewUtils;

import java.text.ParseException;

@Route(OpenehrSmartCallbackView.OPENEHR_CALLBACK_ROUTE)
@Slf4j
public class OpenehrSmartCallbackView extends AbstractSmartCallbackView<OpenehrClient> {

    public static final String OPENEHR_CALLBACK_ROUTE = "openehr/smart-callback";

    public OpenehrSmartCallbackView(OpenehrClient openehrClient, OidcClient oidcClient) {
        super("SMART on openEHR Callback",
                "Now that the authorization code was obtained, exchange it for an access"
                        + " token to be used when accessing the openEHR data.",
                openehrClient, oidcClient);
    }

    @Override
    protected String extractPatientId(OIDCTokenResponse accessTokenResponse) {
        try {
            return accessTokenResponse.getOIDCTokens().getIDToken().getJWTClaimsSet().getStringClaim("ehr_id");
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            ViewUtils.showNotificationError("Cannot extract the patient id (ehr_id).");
            return null;
        }
    }

    @Override
    protected Html getAccessTokenHelpText() {
        return new Html(
                """
                        <p>
                        </p>
                        """
        );
    }

    @Override
    protected String getDataClientPath() {
        return OpenehrDataClientView.OPENEHR_DATA_CLIENT_ROUTE;
    }
}
