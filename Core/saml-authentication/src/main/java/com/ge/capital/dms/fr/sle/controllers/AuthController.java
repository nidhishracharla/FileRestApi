package com.ge.capital.dms.fr.sle.controllers;

import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.ge.capital.dms.fr.sle.config.SecurityConstant;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author slemoine
 */
@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/token")
    @ResponseBody
    public ResponseEntity<String> token() throws JOSEException {
    	System.out.println("Entered the Auth Controller Token method.....");
        final DateTime dateTime = DateTime.now();
        //build claims
        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        jwtClaimsSetBuilder.expirationTime(dateTime.plusMinutes(120).toDate());
        jwtClaimsSetBuilder.claim("APP", "SAMPLE");
        //signature
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaimsSetBuilder.build());
        signedJWT.sign(new MACSigner(SecurityConstant.JWT_SECRET));
        System.out.println(" Generated token :"+signedJWT.serialize());
        return new ResponseEntity<String>(signedJWT.serialize(), HttpStatus.OK);
       
    }
}
