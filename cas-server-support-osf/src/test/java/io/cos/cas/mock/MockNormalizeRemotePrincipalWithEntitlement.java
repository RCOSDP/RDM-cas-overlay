package io.cos.cas.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.BasicHttpEntity;
import org.jasig.cas.CentralAuthenticationService;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.springframework.util.StringUtils;

import io.cos.cas.AbstractTestUtils;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;

/**
 * This class mocks the {@code OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction} class.
 * @author nguyenminhtrung
 * @since 21.10.0
 */
public class MockNormalizeRemotePrincipalWithEntitlement extends MockNormalizeRemotePrincipal {

    private boolean isSingleEntitlement;
    private String loginAvailability;

    public MockNormalizeRemotePrincipalWithEntitlement(final CentralAuthenticationService centralAuthenticationService) {
        super(centralAuthenticationService);
        this.isSingleEntitlement = false;
        this.loginAvailability = "";
    }

    @Override
    protected JSONObject normalizeRemotePrincipal(final OpenScienceFrameworkCredential credential) {

        final JSONObject provider = new JSONObject();
        final JSONObject user = new JSONObject();
        user.put("username", credential.getUsername());
        user.put("fullname", AbstractTestUtils.CONST_DISPLAY_NAME);

        if (isSingleEntitlement) {
            user.put("eduPersonEntitlement", AbstractTestUtils.CONST_SINGLE_ENTITLEMENT_INPUT);
        }

        provider.put("id", credential.getInstitutionId());
        provider.put("user", user);

        return new JSONObject().put("provider", provider);
    }

    protected HttpResponse callLoginAvailabilityAPI(final JSONObject bodyObj) throws IOException, ClientProtocolException {
        final HttpResponse mockedResponse = Mockito.mock(HttpResponse.class);
        final StatusLine statusLine = Mockito.mock(StatusLine.class);
        final BasicHttpEntity entity = new BasicHttpEntity();
        String bodyResponse = "{\"meta\":{\"version\":\"2.0\"}}";
        if (StringUtils.hasText(this.loginAvailability)) {
            bodyResponse = "{\"login_availability\":\"" + this.loginAvailability + "\",\"meta\":{\"version\":\"2.0\"}}";
            Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        } else {
            Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);
        }

        entity.setContent(new ByteArrayInputStream(bodyResponse.getBytes()));
        Mockito.when(mockedResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(mockedResponse.getEntity()).thenReturn(entity);
        return mockedResponse;
    }

    public void setSingleEntitlement(final boolean isSingleEntitlement) {
        this.isSingleEntitlement = isSingleEntitlement;
    }

    public void setLoginAvailability(final String loginAvailability) {
        this.loginAvailability = loginAvailability;
    }

}
