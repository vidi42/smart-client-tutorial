/* (C)2024 */
package ro.vidi.smart.openehr.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import ro.vidi.smart.AppState;
import ro.vidi.smart.openehr.OpenehrClient;
import ro.vidi.smart.view.AbstractDataClientView;
import ro.vidi.smart.view.ViewUtils;

import java.io.IOException;

import static ro.vidi.smart.openehr.view.OpenehrDataClientView.OPENEHR_DATA_CLIENT_ROUTE;

@Route(OPENEHR_DATA_CLIENT_ROUTE)
@Slf4j
public class OpenehrDataClientView extends AbstractDataClientView<OpenehrClient> {

    public static final String OPENEHR_DATA_CLIENT_ROUTE = "openehr/smart-data-client";

    private final TextArea patientData;

    public OpenehrDataClientView(OpenehrClient smartServerClient) {
        super("openEHR Client",
                "Now that the Access Token was obtained, use it to read data from the openEHR"
                        + " server.", smartServerClient);

        Button getPatientDetails = new Button("Get Patient Details");
        getPatientDetails.addClickListener(event -> getPatientData());

        patientData = new TextArea("Patient Details");
        patientData.setWidth("100%");
        patientData.setReadOnly(true);
        patientData.addThemeVariants(TextAreaVariant.LUMO_HELPER_ABOVE_FIELD);
        patientData.setHelperComponent(
                new Html(
                        """
                                <p>
                                    Executes the following AQL query against the openEHR server:</br>
                                    SELECT o/data/events/time/value, o/data/events/data/items[at0004]/value/magnitude as systolic, o/data/events/data/items[at0005]/value/magnitude as diastolic FROM EHR e CONTAINS COMPOSITION c CONTAINS OBSERVATION o[openEHR-EHR-OBSERVATION.blood_pressure.v1] WHERE e/ehr_id/value=$ehrId
                                </p>
                                """));

        getAccessSmartDataLayout().add(getPatientDetails, patientData);
    }

    private void getPatientData() {
        OpenehrClient openehrClient = getSmartServerClient();
        try {
            AppState appState = openehrClient.decodeState(getEncodedState().getValue());
            this.patientData.setValue(openehrClient.toPrettyJson(
                    openehrClient.getPatientData(appState.getServerUrl(), getAccessToken().getValue(), getPatientId().getValue())));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            ViewUtils.showNotificationError("Cannot obtain patient details. Check the logs.");
        }
    }
}
