package io.cos.cas.authentication.handler.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.login.AccountException;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import io.cos.cas.AbstractTestUtils;
import io.cos.cas.adaptors.postgres.types.DelegationProtocol;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;
import io.cos.cas.authentication.exceptions.InstitutionLoginAvailabilityException;
import io.cos.cas.authentication.exceptions.InstitutionLoginFailedException;
import io.cos.cas.mock.MockNormalizeRemotePrincipal;
import io.cos.cas.mock.MockNormalizeRemotePrincipalWithEntitlement;
import io.cos.cas.mock.MockNotifyRemotePrincipalAuthenticated;

/**
 * This class tests the {@link OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsAction} class.
 *
 * @author Longze Chen
 * @since  19.3.0
 */
public class OpenScienceFrameworkPrincipalFromRequestRemoteUserNonInteractiveCredentialsActionTests {

    private static final String TICKET_GRANTING_TICKET_ID
            = "TGT-00-xxxxxxxxxxxxxxxxxxxxxxxxxx.cas0";

    private static final String PAC4J_DELEGATION_PROFILE_ID = "MockProfile#0001-1234-5678";

    private static final String CONST_CAS_CLIENT_NAME = "CasClient";

    private static final String CONST_ORCID_CLIENT_NAME = "OrcidClient";

    @Test (expected = InstitutionLoginFailedException.class)
    public void handleInstitutionMissingInstitutionId() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithShibbolethHeaders();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNormalizeRemotePrincipal osfRemoteAuthenticate
                = new MockNormalizeRemotePrincipal(centralAuthenticationService);

        final OpenScienceFrameworkCredential osfCredential = new OpenScienceFrameworkCredential();
        osfCredential.setUsername(AbstractTestUtils.CONST_MAIL);
        osfCredential.setInstitutionId("");
        try {
            osfRemoteAuthenticate.notifyRemotePrincipalAuthenticated(osfCredential);
        } catch (final AccountException e) {
            assertEquals(e.getMessage(), "Empty identity provider");
            throw e;
        }
    }

    @Test (expected = InstitutionLoginFailedException.class)
    public void handleInstitutionMissingUsername() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithShibbolethHeaders();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNormalizeRemotePrincipal osfRemoteAuthenticate
                = new MockNormalizeRemotePrincipal(centralAuthenticationService);

        final OpenScienceFrameworkCredential osfCredential = new OpenScienceFrameworkCredential();
        osfCredential.setUsername("");
        osfCredential.setInstitutionId(AbstractTestUtils.CONST_INSTITUTION_ID);
        try {
            osfRemoteAuthenticate.notifyRemotePrincipalAuthenticated(osfCredential);
        } catch (final AccountException e) {
            assertEquals(e.getMessage(), "Missing email (username)");
            throw e;
        }
    }

    @Test (expected = InstitutionLoginFailedException.class)
    public void handleInstitutionMissingNames() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithShibbolethHeaders();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNormalizeRemotePrincipal osfRemoteAuthenticate
                = new MockNormalizeRemotePrincipal(centralAuthenticationService);

        final OpenScienceFrameworkCredential osfCredential = new OpenScienceFrameworkCredential();
        osfCredential.setUsername(AbstractTestUtils.CONST_MAIL);
        osfCredential.setInstitutionId(AbstractTestUtils.CONST_INSTITUTION_ID);
        try {
            osfRemoteAuthenticate.notifyRemotePrincipalAuthenticated(osfCredential);
        } catch (final AccountException e) {
            assertEquals(e.getMessage(), "Missing user's names");
            throw e;
        }
    }

    @Test (expected = InstitutionLoginFailedException.class)
    public void handleInstitutionValidRemotePrincipal() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithShibbolethHeaders();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNormalizeRemotePrincipal osfRemoteAuthenticate
                = new MockNormalizeRemotePrincipal(centralAuthenticationService);
        osfRemoteAuthenticate.setFullname(AbstractTestUtils.CONST_DISPLAY_NAME);

        final OpenScienceFrameworkCredential osfCredential = new OpenScienceFrameworkCredential();
        osfCredential.setUsername(AbstractTestUtils.CONST_MAIL);
        osfCredential.setInstitutionId(AbstractTestUtils.CONST_INSTITUTION_ID);
        try {
            osfRemoteAuthenticate.notifyRemotePrincipalAuthenticated(osfCredential);
        } catch (final AccountException e) {
            assertEquals(e.getMessage(), "Communication Error between OSF CAS and OSF API");
            throw e;
        }
    }

    @Test
    public void verifyInstitutionSamlShibbolethFlow() throws Exception {

        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithShibbolethHeaders();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNotifyRemotePrincipalAuthenticated osfRemoteAuthenticate
                = new MockNotifyRemotePrincipalAuthenticated(centralAuthenticationService);
        final Event event = osfRemoteAuthenticate.doExecute(mockContext);

        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);
        assertTrue(credential.isRemotePrincipal());
        assertEquals(credential.getUsername(), AbstractTestUtils.CONST_MAIL);
        assertEquals(credential.getInstitutionId(), AbstractTestUtils.CONST_INSTITUTION_ID);
        assertEquals(credential.getDelegationProtocol(), DelegationProtocol.SAML_SHIB);
        assertEquals(
                credential.getDelegationAttributes().get(AbstractTestUtils.CONST_SHIB_IDENTITY_PROVIDER),
                AbstractTestUtils.CONST_INSTITUTION_IDP
        );
        assertEquals("success", event.getId());
    }

    @Test
    public void verifyInstitutionCasPac4jFlow() throws Exception {

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AbstractTestUtils.getPrincipal(PAC4J_DELEGATION_PROFILE_ID));
        when(authentication.getAttributes()).thenReturn(AbstractTestUtils.getAuthenticationAttributes(CONST_CAS_CLIENT_NAME));

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(authentication);
        when(tgt.getId()).thenReturn(TICKET_GRANTING_TICKET_ID);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(
                any(String.class),
                any(TicketGrantingTicket.class.getClass())
        )).thenReturn(tgt);

        final MockNotifyRemotePrincipalAuthenticated osfRemoteAuthenticate
                = new MockNotifyRemotePrincipalAuthenticated(centralAuthenticationService);

        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest, tgt.getId());

        final Event event = osfRemoteAuthenticate.doExecute(mockContext);
        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);

        assertTrue(credential.isRemotePrincipal());
        assertEquals(credential.getUsername(), AbstractTestUtils.CONST_MAIL);
        assertEquals(credential.getInstitutionId(), AbstractTestUtils.CONST_INSTITUTION_ID);
        assertEquals(credential.getDelegationProtocol(), DelegationProtocol.CAS_PAC4J);
        assertEquals(
                credential.getDelegationAttributes().get(AbstractTestUtils.CONST_CAS_IDENTITY_PROVIDER),
                CONST_CAS_CLIENT_NAME
        );
        assertEquals("success", event.getId());
    }

    @Test
    public void verifyOrcidClientFlow() throws Exception {

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AbstractTestUtils.getPrincipal(PAC4J_DELEGATION_PROFILE_ID));
        when(authentication.getAttributes()).thenReturn(AbstractTestUtils.getAuthenticationAttributes(CONST_ORCID_CLIENT_NAME));

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(authentication);
        when(tgt.getId()).thenReturn(TICKET_GRANTING_TICKET_ID);


        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        when(centralAuthenticationService.getTicket(
                any(String.class),
                any(TicketGrantingTicket.class.getClass())
        )).thenReturn(tgt);

        final MockNotifyRemotePrincipalAuthenticated osfRemoteAuthenticate
                = new MockNotifyRemotePrincipalAuthenticated(centralAuthenticationService);

        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest, tgt.getId());

        final Event event = osfRemoteAuthenticate.doExecute(mockContext);
        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);

        assertTrue(credential.isRemotePrincipal());
        assertEquals(credential.getDelegationProtocol(), DelegationProtocol.OAUTH_PAC4J);
        assertEquals("success", event.getId());

        assertNull(credential.getUsername());
        assertNull(credential.getInstitutionId());
        assertEquals(credential.getDelegationAttributes(), Collections.EMPTY_MAP);
    }

    @Test
    public void verifyUsernameVerificationKeyFlow() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithUsernameAndVerificationKey();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNotifyRemotePrincipalAuthenticated osfRemoteAuthenticate
                = new MockNotifyRemotePrincipalAuthenticated(centralAuthenticationService);
        final Event event = osfRemoteAuthenticate.doExecute(mockContext);

        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);

        assertEquals(credential.getUsername(), AbstractTestUtils.CONST_MAIL);
        assertEquals(credential.getVerificationKey(), AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        assertEquals("success", event.getId());

        assertFalse(credential.isRemotePrincipal());
        assertNull(credential.getInstitutionId());
        assertNull(credential.getDelegationProtocol());
        assertEquals(credential.getDelegationAttributes(), Collections.EMPTY_MAP);
    }

    @Test
    public void verifyLoginAvailabilityGetEntitlementFlow() throws Exception {
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNormalizeRemotePrincipalWithEntitlement osfRemoteAuthenticate = new MockNormalizeRemotePrincipalWithEntitlement(
                centralAuthenticationService);
        List<String> entitlementList = new ArrayList<String>();

        // Verify in case single entitlement
        entitlementList = osfRemoteAuthenticate.getEntitlements(AbstractTestUtils.CONST_SINGLE_ENTITLEMENT_INPUT);
        assertEquals(entitlementList.size(), AbstractTestUtils.CONST_SINGLE_ENTITLEMENTS_OUTPUT.length);
    }

    @Test
    public void verifyLoginAvailabilitySingleEntitlementFlow() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils
                .getRequestWithShibbolethHeadersAndSingleEntitlement();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNormalizeRemotePrincipalWithEntitlement osfRemoteAuthenticate = new MockNormalizeRemotePrincipalWithEntitlement(
                centralAuthenticationService);

        final OpenScienceFrameworkCredential osfCredential = new OpenScienceFrameworkCredential();
        osfCredential.setInstitutionId(AbstractTestUtils.CONST_INSTITUTION_ID);
        osfCredential.setUsername(AbstractTestUtils.CONST_MAIL);

        osfRemoteAuthenticate.setSingleEntitlement(true);
        osfRemoteAuthenticate.setLoginAvailability(true);
        osfRemoteAuthenticate
                .setInstitutionsLoginAvailabilityUrl(AbstractTestUtils.CONST_INSTITUTION_LOGIN_AVAILABILITY_URL);

        try {
            osfRemoteAuthenticate.notifyRemotePrincipalAuthenticated(osfCredential);
        } catch (final AccountException e) {
        }
    }

    @Test(expected = InstitutionLoginAvailabilityException.class)
    public void verifyLoginAvailabilityExceptionFlow() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils
                .getRequestWithShibbolethHeadersAndSingleEntitlement();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNormalizeRemotePrincipalWithEntitlement osfRemoteAuthenticate = new MockNormalizeRemotePrincipalWithEntitlement(
                centralAuthenticationService);

        final OpenScienceFrameworkCredential osfCredential = new OpenScienceFrameworkCredential();
        osfCredential.setInstitutionId(AbstractTestUtils.CONST_INSTITUTION_ID);
        osfCredential.setUsername(AbstractTestUtils.CONST_MAIL);

        osfRemoteAuthenticate.setSingleEntitlement(true);
        osfRemoteAuthenticate.setLoginAvailability(false);
        osfRemoteAuthenticate
                .setInstitutionsLoginAvailabilityUrl(AbstractTestUtils.CONST_INSTITUTION_LOGIN_AVAILABILITY_URL);
        osfRemoteAuthenticate.notifyRemotePrincipalAuthenticated(osfCredential);
    }

    /**
     * Verifies that a Shibboleth (SAML) authentication flow correctly handles an {@code AUTH-} prefixed request header
     * whose value is {@code null}.
     */
    @Test
    public void verifyInstitutionSamlShibbolethFlowWithNullHeaderValue() throws Exception {

        // The name of the AUTH- prefixed header whose value will be forced to null.
        final String nullAttributeHeaderName = "AUTH-NullAttribute";
        final String nullAttributeKey = "NullAttribute"; // stripped prefix

        // Build a MockHttpServletRequest that reports nullAttributeHeaderName in getHeaderNames() but
        // returns null from getHeader() for that specific header, triggering the null-branch in the loop.
        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest() {
            @Override
            public String getHeader(final String name) {
                if (nullAttributeHeaderName.equalsIgnoreCase(name)) {
                    return null;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                final List<String> names = new ArrayList<>(Collections.list(super.getHeaderNames()));
                if (!names.contains(nullAttributeHeaderName)) {
                    names.add(nullAttributeHeaderName);
                }
                return Collections.enumeration(names);
            }
        };

        // Add standard Shibboleth headers so the SAML Shibboleth branch is entered.
        mockHttpServletRequest.addHeader("AUTH-Shib-Session-ID", AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        mockHttpServletRequest.addHeader("REMOTE_USER", AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        mockHttpServletRequest.addHeader("AUTH-Shib-Identity-Provider", AbstractTestUtils.CONST_INSTITUTION_IDP);
        mockHttpServletRequest.addHeader("AUTH-displayName", AbstractTestUtils.CONST_DISPLAY_NAME);
        mockHttpServletRequest.addHeader("AUTH-givenName", "James");
        mockHttpServletRequest.addHeader("AUTH-familyName", "Steward");
        mockHttpServletRequest.addHeader("AUTH-mail", AbstractTestUtils.CONST_MAIL);

        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNotifyRemotePrincipalAuthenticated osfRemoteAuthenticate
                = new MockNotifyRemotePrincipalAuthenticated(centralAuthenticationService);

        final Event event = osfRemoteAuthenticate.doExecute(mockContext);

        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);

        // The flow must still complete successfully.
        assertEquals("success", event.getId());
        assertTrue(credential.isRemotePrincipal());
        assertEquals(DelegationProtocol.SAML_SHIB, credential.getDelegationProtocol());

        // The null-valued AUTH- header must appear in delegationAttributes with a null value,
        // confirming that the `if (headerValue == null) { decodedValue = headerValue; }` branch was taken.
        assertTrue(
                "delegationAttributes must contain the key for the null-valued header",
                credential.getDelegationAttributes().containsKey(nullAttributeKey)
        );
        assertNull(
                "delegationAttributes value for the null-valued header must be null",
                credential.getDelegationAttributes().get(nullAttributeKey)
        );
    }
}
