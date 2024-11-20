/* (C)2024 */
package ro.vidi.smart;

import lombok.Data;

@Data
public class AppState {

    private String tokenUrl;

    private String otherDetails;

    private String clientId;

    private String clientSecret;

    private String serverUrl;

    private String callbackUrl;
}
