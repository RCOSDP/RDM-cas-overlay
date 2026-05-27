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

    /**
     * Verifies that a Shibboleth (SAML) authentication flow correctly re-encodes ALL Japanese {@code AUTH-*} headers
     * (displayName, givenName, familyName, organizationName / o, organizationalUnit / ou,
     * jaOrganizationName / jao, jaOrganizationalUnitName / jaou)
     * delivered as ISO-8859-1 mojibake back to the original UTF-8 strings.
     *
     * <p>Apache / the Servlet container reads HTTP header bytes and constructs Java {@code String} objects using
     * ISO-8859-1 (Latin-1), which is the HTTP/1.1 default for header values. When the IdP sends UTF-8 encoded
     * Japanese text, the bytes are misinterpreted and the resulting Java Strings are "mojibake".
     * The fix re-encodes every AUTH-* header: {@code new String(headerValue.getBytes("ISO-8859-1"), "UTF-8")}.</p>
     */
    @Test
    public void verifyInstitutionSamlShibbolethFlowWithJapaneseDisplayName() throws Exception {

        // Original Japanese values as they should appear after correct decoding.
        final String originalDisplayName            = "\u5c71\u7530 \u592a\u90ce";    // 山田 太郎
        final String originalGivenName              = "\u592a\u90ce";                 // 太郎
        final String originalFamilyName             = "\u5c71\u7530";                 // 山田
        final String originalOrganizationName       = "\u5927\u962a\u5927\u5b66";     // 大阪大学  (o)
        final String originalOrganizationalUnit     = "\u7406\u5b66\u90e8";           // 理学部    (ou)
        final String originalJaOrganizationName     = "\u5927\u962a\u5927\u5b66";     // 大阪大学  (jao)
        final String originalJaOrganizationalUnit   = "\u7406\u5b66\u90e8";           // 理学部    (jaou)

        // Simulate the mojibake that Apache/Servlet creates:
        // the IdP sends UTF-8 bytes; the container interprets them as ISO-8859-1 (Latin-1).
        final String mojibakeDisplayName          = new String(originalDisplayName.getBytes("UTF-8"),          "ISO-8859-1");
        final String mojibakeGivenName            = new String(originalGivenName.getBytes("UTF-8"),            "ISO-8859-1");
        final String mojibakeFamilyName           = new String(originalFamilyName.getBytes("UTF-8"),           "ISO-8859-1");
        final String mojibakeOrganizationName     = new String(originalOrganizationName.getBytes("UTF-8"),     "ISO-8859-1");
        final String mojibakeOrganizationalUnit   = new String(originalOrganizationalUnit.getBytes("UTF-8"),   "ISO-8859-1");
        final String mojibakeJaOrganizationName   = new String(originalJaOrganizationName.getBytes("UTF-8"),   "ISO-8859-1");
        final String mojibakeJaOrganizationalUnit = new String(originalJaOrganizationalUnit.getBytes("UTF-8"), "ISO-8859-1");

        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader("AUTH-Shib-Session-ID",       AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        mockHttpServletRequest.addHeader("REMOTE_USER",                AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        mockHttpServletRequest.addHeader("AUTH-Shib-Identity-Provider", AbstractTestUtils.CONST_INSTITUTION_IDP);
        mockHttpServletRequest.addHeader("AUTH-displayName",            mojibakeDisplayName);
        mockHttpServletRequest.addHeader("AUTH-givenName",              mojibakeGivenName);
        mockHttpServletRequest.addHeader("AUTH-familyName",             mojibakeFamilyName);
        mockHttpServletRequest.addHeader("AUTH-o",                      mojibakeOrganizationName);     // organizationName
        mockHttpServletRequest.addHeader("AUTH-ou",                     mojibakeOrganizationalUnit);   // organizationalUnit
        mockHttpServletRequest.addHeader("AUTH-jao",                    mojibakeJaOrganizationName);   // jaOrganizationName
        mockHttpServletRequest.addHeader("AUTH-jaou",                   mojibakeJaOrganizationalUnit); // jaOrganizationalUnitName
        mockHttpServletRequest.addHeader("AUTH-mail",                   AbstractTestUtils.CONST_MAIL);

        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNotifyRemotePrincipalAuthenticated osfRemoteAuthenticate
                = new MockNotifyRemotePrincipalAuthenticated(centralAuthenticationService);

        final Event event = osfRemoteAuthenticate.doExecute(mockContext);

        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);

        assertEquals("success", event.getId());
        assertTrue(credential.isRemotePrincipal());
        assertEquals(DelegationProtocol.SAML_SHIB, credential.getDelegationProtocol());

        // All AUTH-* Japanese fields must be re-encoded from ISO-8859-1 mojibake to original UTF-8.
        final String decodedDisplayName          = (String) credential.getDelegationAttributes().get("displayName");
        final String decodedGivenName            = (String) credential.getDelegationAttributes().get("givenName");
        final String decodedFamilyName           = (String) credential.getDelegationAttributes().get("familyName");
        final String decodedOrganizationName     = (String) credential.getDelegationAttributes().get("o");
        final String decodedOrganizationalUnit   = (String) credential.getDelegationAttributes().get("ou");
        final String decodedJaOrganizationName   = (String) credential.getDelegationAttributes().get("jao");
        final String decodedJaOrganizationalUnit = (String) credential.getDelegationAttributes().get("jaou");

        assertEquals(
                "AUTH-displayName must be re-encoded from mojibake to original Japanese UTF-8",
                originalDisplayName, decodedDisplayName
        );
        assertEquals(
                "AUTH-givenName must be re-encoded from mojibake to original Japanese UTF-8",
                originalGivenName, decodedGivenName
        );
        assertEquals(
                "AUTH-familyName must be re-encoded from mojibake to original Japanese UTF-8",
                originalFamilyName, decodedFamilyName
        );
        assertEquals(
                "AUTH-o (organizationName) must be re-encoded from mojibake to original Japanese UTF-8",
                originalOrganizationName, decodedOrganizationName
        );
        assertEquals(
                "AUTH-ou (organizationalUnit) must be re-encoded from mojibake to original Japanese UTF-8",
                originalOrganizationalUnit, decodedOrganizationalUnit
        );
        assertEquals(
                "AUTH-jao (jaOrganizationName) must be re-encoded from mojibake to original Japanese UTF-8",
                originalJaOrganizationName, decodedJaOrganizationName
        );
        assertEquals(
                "AUTH-jaou (jaOrganizationalUnitName) must be re-encoded from mojibake to original Japanese UTF-8",
                originalJaOrganizationalUnit, decodedJaOrganizationalUnit
        );
    }

    /**
     * Verifies that a Shibboleth (SAML) authentication flow correctly re-encodes ALL Japanese {@code AUTH-*} headers
     * (displayName, givenName, familyName, organizationName / o, organizationalUnit / ou,
     * jaOrganizationName / jao, jaOrganizationalUnitName / jaou)
     * when the display name contains a full-width organization prefix followed by a full-width Japanese personal name
     * — a common real-world format from Japanese IdPs.
     */
    @Test
    public void verifyInstitutionSamlShibbolethFlowWithJapaneseMultibyteDisplayName() throws Exception {

        // Full-width organization prefix + full-width personal name (real-world IdP format).
        final String originalDisplayName
                = "\u56fd\u7acb\u60c5\u5831\u5b66\u7814\u7a76\u6240\u3000\u9234\u6728\u4e00\u90ce"; // 国立情報学研究所　鈴木一郎
        final String originalGivenName              = "\u4e00\u90ce";                                       // 一郎
        final String originalFamilyName             = "\u9234\u6728";                                       // 鈴木
        final String originalOrganizationName       = "\u56fd\u7acb\u60c5\u5831\u5b66\u7814\u7a76\u6240"; // 国立情報学研究所  (o)
        final String originalOrganizationalUnit     = "\u30b3\u30f3\u30d4\u30e5\u30fc\u30bf\u79d1\u5b66"; // コンピュータ科学 (ou)
        final String originalJaOrganizationName     = "\u56fd\u7acb\u60c5\u5831\u5b66\u7814\u7a76\u6240"; // 国立情報学研究所  (jao)
        final String originalJaOrganizationalUnit   = "\u30b3\u30f3\u30d4\u30e5\u30fc\u30bf\u79d1\u5b66"; // コンピュータ科学 (jaou)

        // Simulate Apache / Servlet container mojibake: UTF-8 bytes read as ISO-8859-1.
        final String mojibakeDisplayName          = new String(originalDisplayName.getBytes("UTF-8"),          "ISO-8859-1");
        final String mojibakeGivenName            = new String(originalGivenName.getBytes("UTF-8"),            "ISO-8859-1");
        final String mojibakeFamilyName           = new String(originalFamilyName.getBytes("UTF-8"),           "ISO-8859-1");
        final String mojibakeOrganizationName     = new String(originalOrganizationName.getBytes("UTF-8"),     "ISO-8859-1");
        final String mojibakeOrganizationalUnit   = new String(originalOrganizationalUnit.getBytes("UTF-8"),   "ISO-8859-1");
        final String mojibakeJaOrganizationName   = new String(originalJaOrganizationName.getBytes("UTF-8"),   "ISO-8859-1");
        final String mojibakeJaOrganizationalUnit = new String(originalJaOrganizationalUnit.getBytes("UTF-8"), "ISO-8859-1");

        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader("AUTH-Shib-Session-ID",        AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        mockHttpServletRequest.addHeader("REMOTE_USER",                 AbstractTestUtils.CONST_NOT_EMPTY_STRING);
        mockHttpServletRequest.addHeader("AUTH-Shib-Identity-Provider", AbstractTestUtils.CONST_INSTITUTION_IDP);
        mockHttpServletRequest.addHeader("AUTH-displayName",            mojibakeDisplayName);
        mockHttpServletRequest.addHeader("AUTH-givenName",              mojibakeGivenName);
        mockHttpServletRequest.addHeader("AUTH-familyName",             mojibakeFamilyName);
        mockHttpServletRequest.addHeader("AUTH-o",                      mojibakeOrganizationName);
        mockHttpServletRequest.addHeader("AUTH-ou",                     mojibakeOrganizationalUnit);
        mockHttpServletRequest.addHeader("AUTH-jao",                    mojibakeJaOrganizationName);
        mockHttpServletRequest.addHeader("AUTH-jaou",                   mojibakeJaOrganizationalUnit);
        mockHttpServletRequest.addHeader("AUTH-mail",                   AbstractTestUtils.CONST_MAIL);

        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);

        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        final MockNotifyRemotePrincipalAuthenticated osfRemoteAuthenticate
                = new MockNotifyRemotePrincipalAuthenticated(centralAuthenticationService);

        final Event event = osfRemoteAuthenticate.doExecute(mockContext);

        final OpenScienceFrameworkCredential credential
                = (OpenScienceFrameworkCredential) mockContext.getFlowScope().get(AbstractTestUtils.CONST_CREDENTIAL);

        assertEquals("success", event.getId());
        assertTrue(credential.isRemotePrincipal());
        assertEquals(DelegationProtocol.SAML_SHIB, credential.getDelegationProtocol());

        // All AUTH-* Japanese fields must be re-encoded from ISO-8859-1 mojibake to original UTF-8.
        final String decodedDisplayName          = (String) credential.getDelegationAttributes().get("displayName");
        final String decodedGivenName            = (String) credential.getDelegationAttributes().get("givenName");
        final String decodedFamilyName           = (String) credential.getDelegationAttributes().get("familyName");
        final String decodedOrganizationName     = (String) credential.getDelegationAttributes().get("o");
        final String decodedOrganizationalUnit   = (String) credential.getDelegationAttributes().get("ou");
        final String decodedJaOrganizationName   = (String) credential.getDelegationAttributes().get("jao");
        final String decodedJaOrganizationalUnit = (String) credential.getDelegationAttributes().get("jaou");

        assertEquals(
                "AUTH-displayName with full-width org prefix must be re-encoded correctly to original Japanese UTF-8",
                originalDisplayName, decodedDisplayName
        );
        assertEquals(
                "AUTH-givenName must be re-encoded from mojibake to original Japanese UTF-8",
                originalGivenName, decodedGivenName
        );
        assertEquals(
                "AUTH-familyName must be re-encoded from mojibake to original Japanese UTF-8",
                originalFamilyName, decodedFamilyName
        );
        assertEquals(
                "AUTH-o (organizationName) must be re-encoded from mojibake to original Japanese UTF-8",
                originalOrganizationName, decodedOrganizationName
        );
        assertEquals(
                "AUTH-ou (organizationalUnit) must be re-encoded from mojibake to original Japanese UTF-8",
                originalOrganizationalUnit, decodedOrganizationalUnit
        );
        assertEquals(
                "AUTH-jao (jaOrganizationName) must be re-encoded from mojibake to original Japanese UTF-8",
                originalJaOrganizationName, decodedJaOrganizationName
        );
        assertEquals(
                "AUTH-jaou (jaOrganizationalUnitName) must be re-encoded from mojibake to original Japanese UTF-8",
                originalJaOrganizationalUnit, decodedJaOrganizationalUnit
        );
    }
}
