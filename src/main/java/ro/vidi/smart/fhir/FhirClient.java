/* (C)2024 */
package ro.vidi.smart.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.client.api.IBasicClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.UriType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ro.vidi.smart.SmartMetadata;
import ro.vidi.smart.SmartServerClient;

@Component
public class FhirClient implements SmartServerClient {

    @Value("${smart.fhir.base.url}")
    private String defaultFhirServerUrl;

    private final ObjectMapper mapper;

    public FhirClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String getDefaultSmartServerUrl() {
        return defaultFhirServerUrl;
    }

    public SmartMetadata getSmartMetadata(String smartServerUrl) {
        IBasicClient client =
                FhirContext.forR4().newRestfulClient(IBasicClient.class, smartServerUrl);

        CapabilityStatement capabilityStatement =
                (CapabilityStatement) client.getServerConformanceStatement();

        SmartMetadata smartMetadata = new SmartMetadata();
        smartMetadata.setJsonResponse(convertResourceToString(capabilityStatement));
        for (CapabilityStatement.CapabilityStatementRestComponent rest :
                capabilityStatement.getRest()) {
            if (rest.getSecurity() != null) {
                for (Extension extension : rest.getSecurity().getExtension()) {
                    if (extension
                                    .getUrl()
                                    .equals(
                                            "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris")
                            && !CollectionUtils.isEmpty(extension.getExtension())) {
                        for (Extension smartExtension : extension.getExtension()) {
                            switch (smartExtension.getUrl()) {
                                case "authorize":
                                    smartMetadata.setAuthorizeUrl(
                                            ((UriType) smartExtension.getValue()).getValue());
                                    break;
                                case "token":
                                    smartMetadata.setTokenUrl(
                                            ((UriType) smartExtension.getValue()).getValue());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        }
        return smartMetadata;
    }

    @Override
    public ObjectMapper getMapper() {
        return mapper;
    }


    public Patient getPatient(String fhirServerUrl, String accessToken, String patientId) {
        IPatientClient client =
                FhirContext.forR4().newRestfulClient(IPatientClient.class, fhirServerUrl);
        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(accessToken);
        client.registerInterceptor(authInterceptor);

        return client.readPatient(new IdType(patientId));
    }

    public String convertResourceToString(IBaseResource resource) {
        return FhirContext.forR4().newJsonParser().encodeResourceToString(resource);
    }

    private interface IPatientClient extends IBasicClient {
        /**
         * Read a patient from a server by ID
         */
        @Read
        Patient readPatient(@IdParam IdType theId);
    }
}
