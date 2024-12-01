/* (C)2024 */
package ro.vidi.smart.openehr.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private final TextArea aqlQuery;
    private final TextArea patientData;

    public OpenehrDataClientView(OpenehrClient smartServerClient) {
        super("openEHR Client",
                "Now that the Access Token was obtained, use it to read data from the openEHR"
                        + " server.", smartServerClient);


        aqlQuery = new TextArea("AQL Query");
        aqlQuery.setWidth("100%");
        aqlQuery.addValueChangeListener(event -> {

            if (StringUtils.contains(event.getValue(), "$ehrId")) {
                aqlQuery.setHelperComponent(
                        new Html(
                                """
                                        <p>
                                            Note that the only supported parameter is ehrId (ehrId=%s).
                                        </p>
                                        """.formatted(getPatientId().getValue())));
            } else {
                aqlQuery.setHelperComponent(null);
            }
        });


        Button getPatientDetails = new Button("Get Patient Results");
        getPatientDetails.addClickListener(event -> getPatientData());

        patientData = new TextArea("Patient Results");
        patientData.setWidth("100%");
        patientData.setReadOnly(true);

        getAccessSmartDataLayout().add(aqlQuery, getPatientDetails, patientData);
    }

    private void getPatientData() {
        OpenehrClient openehrClient = getSmartServerClient();
        try {
            AppState appState = openehrClient.decodeState(getEncodedState().getValue());
            this.patientData.setValue(openehrClient.toPrettyJson(
                    openehrClient.getAqlResult(appState.getServerUrl(), aqlQuery.getValue(), getAccessToken().getValue(), getPatientId().getValue())));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            ViewUtils.showNotificationError("Cannot obtain patient details. Check the logs.");
        }
    }
}
