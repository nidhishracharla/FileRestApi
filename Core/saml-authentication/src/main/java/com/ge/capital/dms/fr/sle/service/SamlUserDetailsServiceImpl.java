package com.ge.capital.dms.fr.sle.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import com.ge.capital.dms.fr.sle.model.SamlUserDetails;

/**
 * @author slemoine
 */
public class SamlUserDetailsServiceImpl implements SAMLUserDetailsService {

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        return new SamlUserDetails();
    }
}