/* (C)2024 */
package ro.vidi.smart.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ro.vidi.smart.SmartServerClient;

@Getter
@Slf4j
public abstract class AbstractDataClientView<T extends SmartServerClient> extends VerticalLayout implements HasUrlParameter<String> {

    private final T smartServerClient;

    private final TextArea accessToken;
    private final TextField patientId;
    private final TextArea encodedState;
    private final VerticalLayout accessSmartDataLayout;

    protected AbstractDataClientView(String viewName, String viewDescription, T smartServerClient) {
        this.smartServerClient = smartServerClient;

        accessToken = new TextArea("Access Token");
        accessToken.setWidth("100%");
        accessToken.setReadOnly(true);
        accessToken.setPlaceholder("Code received from the oauth flow");
        accessToken.addValueChangeListener(
                event -> {
                    if (event.getValue() != null) {
                        accessToken.setHelperComponent(
                                new Html(
                                        """
                                                <p>View on <a href="https://jwt.io?token=%s" target="_blank">jwt.io</a> (pre-populated).</p>
                                                """
                                                .formatted(event.getValue())));
                    } else {
                        accessToken.setHelperComponent(null);
                    }
                });
        patientId = new TextField("Patient ID");
        patientId.setWidth("100%");
        patientId.setReadOnly(true);

        encodedState = new TextArea("State");
        encodedState.setWidth("100%");
        encodedState.setReadOnly(true);
        encodedState.setPlaceholder("Code received from the oauth flow");
        encodedState.setHelperComponent(
                new Html(
                        """
                                <p>
                                    Base64 encoded JSON containing the state of the application.
                                </p>
                                """));

        accessSmartDataLayout = new VerticalLayout();
        accessSmartDataLayout.setWidth("100%");
        accessSmartDataLayout.setPadding(false);

        Button restartFlowButton = new Button("Go back home");
        restartFlowButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));

        add(
                new H1(viewName),
                new Text(viewDescription),
                accessToken,
                patientId,
                accessSmartDataLayout,
                restartFlowButton);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        queryParameters.getSingleParameter("token").ifPresent(accessToken::setValue);
        queryParameters.getSingleParameter("patientId").ifPresent(patientId::setValue);
        queryParameters.getSingleParameter("state").ifPresent(encodedState::setValue);
    }
}
