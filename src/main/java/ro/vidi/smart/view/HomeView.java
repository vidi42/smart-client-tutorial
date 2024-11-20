/* (C)2024 */
package ro.vidi.smart.view;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import ro.vidi.smart.fhir.view.FhirSmartStartView;
import ro.vidi.smart.openehr.view.OpenehrSmartStartView;

@Route("")
public class HomeView extends VerticalLayout {

    public HomeView() {

        Button startSmartOnFhir = new Button("Start SMART on FHIR",
                event -> getUI().ifPresent(ui -> ui.navigate(FhirSmartStartView.FHIR_SMART_START_ROUTE)));
        Button startSmartOnOpenehr = new Button("Start SMART on openEHR",
                event -> getUI().ifPresent(ui -> ui.navigate(OpenehrSmartStartView.OPENEHR_SMART_START_ROUTE)));

        add(
                new H1("Welcome"),
                new Text("This Sample application will guide you trough the SMART flow."),
                startSmartOnFhir, startSmartOnOpenehr);
    }
}
