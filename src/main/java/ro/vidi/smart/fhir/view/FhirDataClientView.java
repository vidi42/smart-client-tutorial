/* (C)2024 */
package ro.vidi.smart.fhir.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import ro.vidi.smart.AppState;
import ro.vidi.smart.fhir.FhirClient;
import ro.vidi.smart.view.AbstractDataClientView;
import ro.vidi.smart.view.ViewUtils;

import java.io.IOException;

import static ro.vidi.smart.fhir.view.FhirDataClientView.FHIR_DATA_CLIENT_ROUTE;

@Route(FHIR_DATA_CLIENT_ROUTE)
@Slf4j
public class FhirDataClientView extends AbstractDataClientView<FhirClient> {

    public static final String FHIR_DATA_CLIENT_ROUTE = "fhir/smart-data-client";

    private final TextArea patientDetails;

    public FhirDataClientView(FhirClient smartServerClient) {
        super("FHIR Client",
                "Now that the Access Token was obtained, use it to read data from the FHIR"
                        + " server.",
                smartServerClient);

        Button getPatientDetails = new Button("Get Patient Details");
        getPatientDetails.addClickListener(event -> obtainPatientDetails());

        patientDetails = new TextArea("Patient Details");
        patientDetails.setWidth("100%");
        patientDetails.setReadOnly(true);

        getAccessSmartDataLayout().add(getPatientDetails, patientDetails);
    }

    private void obtainPatientDetails() {
        FhirClient fhirClient = getSmartServerClient();
        try {
            AppState appState = fhirClient.decodeState(getEncodedState().getValue());
            patientDetails.setValue(fhirClient.toPrettyJson(
                    fhirClient.convertResourceToString(
                            fhirClient.getPatient(
                                    appState.getServerUrl(),
                                    getAccessToken().getValue(),
                                    getPatientId().getValue()))));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            ViewUtils.showNotificationError("Cannot obtain patient details. Check the logs.");
        }
    }
}
