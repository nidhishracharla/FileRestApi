package com.ge.capital.dms.fr.sle.config;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
 
public class CorsFilter extends OncePerRequestFilter {
 
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
    	response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		
		response.setHeader("Access-Control-Allow-Headers",
				"authorization, content-type, xsrf-token, Cache-Control, remember-me, WWW-Authenticate, loggedinuser,seqNum");
		response.addHeader("Access-Control-Expose-Headers", "xsrf-token, fileName, Content-Disposition, Content-Length, Content-Type");
		filterChain.doFilter(request, response);
    }
}