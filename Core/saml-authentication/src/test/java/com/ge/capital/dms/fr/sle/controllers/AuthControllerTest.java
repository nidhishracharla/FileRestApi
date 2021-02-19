package com.ge.capital.dms.fr.sle.controllers;

import com.ge.capital.dms.WithMockSaml;
import com.ge.capital.dms.fr.sle.controllers.AuthController;
import com.ge.capital.dms.fr.sle.dto.ApiToken;
import com.nimbusds.jose.JOSEException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author slemoine
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthControllerTest {

    @WithMockSaml(samlAssertFile = "/saml-auth-assert.xml")
    @Test
    public void testAuthController() throws JOSEException {

        final AuthController authController = new AuthController();

        final ResponseEntity<String> apiToken = authController.token();
        
        System.out.println("Token:     "+apiToken);

        Assert.assertNotNull(apiToken);
        Assert.assertTrue(((CharSequence) apiToken).length() > 0);
    }
}
