package com.ge.capital.dms.fr.sle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ge.capital.dms.fr.sle.config.jwt.JwtAuthenticationFilter;
import com.ge.capital.dms.fr.sle.config.jwt.JwtAuthenticationProvider;

/**
 * @author slemoine
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Rest security configuration for /api/
     */

    @Configuration
    @Order(1)
    public static class RestApiSecurityConfig extends WebSecurityConfigurerAdapter {

    	
        private static final String apiMatcher = "/api/**";

        @Override
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {            
        	auth.inMemoryAuthentication()
                .withUser("admin").password("admin").roles("ADMIN")
                .and()
                .withUser("user").password("user").roles("USER");
            
        	
        }
         
        @Bean
        public PasswordEncoder  encoder() {
            return new BCryptPasswordEncoder();
        }
        @Override
    	protected void configure(HttpSecurity httpSecurity) throws Exception {
    		httpSecurity.addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class);
    		httpSecurity.authorizeRequests()
    			.antMatchers(apiMatcher)	
    			//.permitAll()
    			.authenticated()
    			.and().httpBasic().and().csrf().disable();
    		
    	}
        /*@Override
        protected void configure(HttpSecurity http) throws Exception { 
            http.addFilterBefore(new JwtAuthenticationFilter(apiMatcher, super.authenticationManager()), UsernamePasswordAuthenticationFilter.class);
            http.authorizeRequests()
            .antMatchers(apiMatcher)
            //.hasAnyRole("ADMIN","USER").and()
            .authenticated()
            .and()
            .httpBasic()
            .and()
            .csrf().disable();
            
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }*/
        /*protected void configure(HttpSecurity http) throws Exception {

            http.addFilterBefore(new JwtAuthenticationFilter(apiMatcher, super.authenticationManager()), UsernamePasswordAuthenticationFilter.class);
        http.antMatcher(apiMatcher).authorizeRequests()
        .anyRequest().authenticated();

        }
*/
        /*@Override
        protected void configureGlobal(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(new JwtAuthenticationProvider());
        }*/
    
    }
    /**
     * Rest security configuration for /api/
     */
    @Configuration
    @Order(2)
    public static class AuthSecurityConfig extends WebSecurityConfigurerAdapter {

        private static final String apiMatcher = "/auth/token";
        ServletRequestAttributes attr =null;
        @Override
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        	
            auth.inMemoryAuthentication()
                .withUser("admin").password(encoder().encode("admin")).roles("ADMIN")
                .and()
                .withUser("user").password(encoder().encode("user")).roles("USER");
        	
        }
         
        @Bean
        public PasswordEncoder  encoder() {
            return new BCryptPasswordEncoder();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
        	
            http
                    .exceptionHandling()
                    .authenticationEntryPoint(new Http401AuthenticationEntryPoint("SAML2.0 - WEBSSO"));
            
            http.addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class);

            http.authorizeRequests()
            .antMatchers(apiMatcher)
            .fullyAuthenticated()
            .and()
            .httpBasic()
            .and()
            .csrf().disable();
            
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            
        }
        
        /*protected void configure(HttpSecurity http) throws Exception {

            http.addFilterBefore(new JwtAuthenticationFilter(apiMatcher, super.authenticationManager()), UsernamePasswordAuthenticationFilter.class);
        http.antMatcher(apiMatcher).authorizeRequests()
        .anyRequest().authenticated();

        }*/

    }

    /**
     * Saml security config
     */
    @Configuration
    @Import(SamlSecurityConfig.class)
    public static class SamlConfig {

    }

}
