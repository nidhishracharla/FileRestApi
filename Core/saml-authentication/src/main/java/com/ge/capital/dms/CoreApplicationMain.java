package com.ge.capital.dms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author pvajjala
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.ge.capital.dms","com.ge.capital.dms.fr.sle",
								"com.ge.capital.dms.fr.sle.config",
								"com.ge.capital.dms.fr.sle.config.jwt",
								"com.ge.capital.dms.fr.sle.controllers",
								"com.ge.capital.dms.fr.sle.controllers.api",
								"com.ge.capital.dms.fr.sle.dto",
								"com.ge.capital.dms.fr.sle.model",
								"com.ge.capital.dms.fr.sle.service",
								"com.ge.capital.dms.dao","com.ge.capital.dms.utility",
								"com.ge.capital.dms.entity","com.ge.capital.dms.service"
								})
@EntityScan({"com.ge.capital.dms.entity"})
public class CoreApplicationMain {
    public static void main(String[] args) {
    	
        SpringApplication.run(CoreApplicationMain.class);
    }
}