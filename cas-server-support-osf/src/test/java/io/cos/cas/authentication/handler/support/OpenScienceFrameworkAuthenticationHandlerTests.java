package io.cos.cas.authentication.handler.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.cos.cas.AbstractTestUtils;
import io.cos.cas.adaptors.postgres.daos.OpenScienceFrameworkDaoImpl;
import io.cos.cas.adaptors.postgres.handlers.OpenScienceFrameworkAuthenticationHandler;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkGuid;
import io.cos.cas.adaptors.postgres.models.OpenScienceFrameworkUser;
import io.cos.cas.authentication.OpenScienceFrameworkCredential;

/**
 * This class tests the {@link OpenScienceFrameworkAuthenticationHandler} class.
 *
 * @author nguyenminhtrung
 * @since 21.10.0
 */
public class OpenScienceFrameworkAuthenticationHandlerTests {

    @Test
    public void handleIsReRegisterdUserFlow() throws Exception {
        final OpenScienceFrameworkAuthenticationHandler authenticationHandler = new OpenScienceFrameworkAuthenticationHandler();
        OpenScienceFrameworkUser user = new OpenScienceFrameworkUser();
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObj = new JsonObject();

        // user with empty jobs
        ReflectionTestUtils.setField(user, "username", "username");
        ReflectionTestUtils.setField(user, "jobs", jsonArray);
        assertEquals(Boolean.TRUE, authenticationHandler.isReRegisteredUser(user));

        // user has jobs, not set institution
        user = new OpenScienceFrameworkUser();
        jsonArray = new JsonArray();
        jsonObj = new JsonObject();
        jsonArray.add(jsonObj);
        jsonObj.addProperty("institution", "institution");

        ReflectionTestUtils.setField(user, "username", "username");
        ReflectionTestUtils.setField(user, "jobs", jsonArray);
        assertEquals(Boolean.TRUE, authenticationHandler.isReRegisteredUser(user));

        // user has full data
        jsonArray = new JsonArray();
        jsonObj = new JsonObject();
        jsonArray.add(jsonObj);
        jsonObj.addProperty("institution", "institution");
        jsonObj.addProperty("institution_ja", "institution_ja");

        ReflectionTestUtils.setField(user, "username", "username");
        ReflectionTestUtils.setField(user, "jobs", jsonArray);
        ReflectionTestUtils.setField(user, "familyName", "familyName");
        ReflectionTestUtils.setField(user, "familyNameJa", "familyNameJa");
        ReflectionTestUtils.setField(user, "givenName", "givenName");
        ReflectionTestUtils.setField(user, "givenNameJa", "givenNameJa");
        assertEquals(Boolean.FALSE, authenticationHandler.isReRegisteredUser(user));
    }

    @Test
    public void handleIsReRegisterdUserAuthenticateFlow() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = AbstractTestUtils.getRequestWithShibbolethHeaders();
        final MockRequestContext mockContext = AbstractTestUtils.getContextWithCredentials(mockHttpServletRequest);
        // set mock request
        RequestContextHolder.setRequestContext(mockContext);

        final OpenScienceFrameworkDaoImpl openScienceFrameworkDao = mock(OpenScienceFrameworkDaoImpl.class);
        final OpenScienceFrameworkAuthenticationHandler authenticationHandler = new OpenScienceFrameworkAuthenticationHandler();
        final OpenScienceFrameworkCredential osfCredential = new OpenScienceFrameworkCredential();

        authenticationHandler.setOpenScienceFrameworkDao(openScienceFrameworkDao);
        osfCredential.setUsername(AbstractTestUtils.CONST_MAIL);
        osfCredential.setInstitutionId(AbstractTestUtils.CONST_INSTITUTION_ID);
        osfCredential.setRemotePrincipal(true);

        // Create data in case re-register user
        final OpenScienceFrameworkUser user = new OpenScienceFrameworkUser();
        final JsonArray jsonArray = new JsonArray();
        final JsonObject jsonObj = new JsonObject();
        jsonArray.add(jsonObj);

        ReflectionTestUtils.setField(user, "username", "username");
        ReflectionTestUtils.setField(user, "jobs", jsonArray);
        ReflectionTestUtils.setField(user, "familyName", "familyName");
        ReflectionTestUtils.setField(user, "familyNameJa", "familyNameJa");
        ReflectionTestUtils.setField(user, "givenName", "givenName");
        ReflectionTestUtils.setField(user, "givenNameJa", "givenNameJa");
        ReflectionTestUtils.setField(user, "registered", Boolean.TRUE);
        ReflectionTestUtils.setField(user, "dateConfirmed", new Date());
        ReflectionTestUtils.setField(user, "givenNameJa", "givenNameJa");

        // mock findOneUserByEmail
        Mockito.when(openScienceFrameworkDao.findOneUserByEmail(Mockito.anyString())).thenReturn(user);

        // mock findGuidByUser
        final OpenScienceFrameworkGuid guid = new OpenScienceFrameworkGuid();
        ReflectionTestUtils.setField(guid, "guid", "guid");
        Mockito.when(openScienceFrameworkDao.findGuidByUser(Mockito.any())).thenReturn(guid);

        authenticationHandler.authenticate(osfCredential);
    }
}
