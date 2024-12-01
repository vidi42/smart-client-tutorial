# SMART Client Tutorial

Sample Java Spring application to guide through the SMART on FHIR and SMART on openEHR OIDC flows.

The application only supports the Provider Standalone Launch Type for now, but hopefully more will be added later.

See here for [FHIR](https://www.hl7.org/fhir/smart-app-launch/app-launch.html#launch-app-ehr-launch) or [openEHR](https://specifications.openehr.org/releases/ITS-REST/development/smart_app_launch.html#_context_selection).

## Quickstart

1. Clone the repository
2. `./gradlew build`
3. `./gradlew bootRun`
4. Open http://localhost:5050 in the browser
5. Follow the flows in there

### Public test servers

For SMART on FHIR there is the SMART Launcher at https://launch.smarthealthit.org/.

For SMART on openEHR there are no public test servers as it's a more recent specification. 
However, there is a mock SMART on openEHR that can be started by using the 
[docker-compose](./docker/docker-compose.yml). It's nothing fancy, just a Keycloak and a mockserver
that mimic the SMART endpoints and scopes (no patient selection step, the patient selection is hard coded in the token).


## Mentions

This tutorial app was inspired by the tutorials and videos of https://github.com/GinoCanessa 
, documentation from [HL7 SMART App Launch](https://www.hl7.org/fhir/smart-app-launch/)
and [SMART on openEHR](https://specifications.openehr.org/releases/ITS-REST/development/smart_app_launch.html) documentation.
