package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {

	// creating a bean on BCryptPasswordEncoder to use encrypt password
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// to get the user details from database with authentication
	@Bean
	public UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImple();
	}

	// it is a DaoAuthenticationProvider in which set userDetails content and object
	// of BCryptPasswordEncoder
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(getUserDetailsService());
		authenticationProvider.setPasswordEncoder(passwordEncoder());

		return authenticationProvider;
	}

	// creating a security filter chain configuration class
	@Bean
	public  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
    	.authorizeHttpRequests()
        .requestMatchers("/admin/**")
    	.hasRole("ADMIN")
    	.requestMatchers("/user/**")
    	.hasRole("USER")
    	.requestMatchers("/**")
    	.permitAll()
    	.and()
       	.formLogin()
       	.loginPage("/signin")
       	.loginProcessingUrl("/dologin")
       	.defaultSuccessUrl("/user/index")
        .and().csrf().disable();
		
		http.authenticationProvider(authenticationProvider());
		
		return http.build();
	}

}
