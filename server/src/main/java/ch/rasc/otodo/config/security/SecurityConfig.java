package ch.rasc.otodo.config.security;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private AuthHeaderFilter authHeaderFilter;

  @Autowired
  private AppLoginSuccessHandler appLoginSuccessHandler;

  @Autowired
  private AppLogoutSuccessHandler appLogoutSuccessHandler;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    var pathPrefix = "/be";

    // @formatter:off
    http
    .headers().defaultsDisabled().cacheControl()
      .and().and()
    .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
    .csrf().disable()
  	.formLogin()
  	  .successHandler(this.appLoginSuccessHandler)
  	  .failureHandler((request, response, exception) ->
  	                  response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED"))
  	  .loginProcessingUrl(pathPrefix + "/login")
  	  .permitAll()
      .and()
  	.logout()
  	  .logoutSuccessHandler(this.appLogoutSuccessHandler)
  	  .logoutUrl(pathPrefix + "/logout")
      .and()
    .authorizeRequests()
      .antMatchers(
          pathPrefix + "/signup",
          pathPrefix + "/confirm-signup",
          pathPrefix + "/reset-password-request",
          pathPrefix + "/reset-password",
          pathPrefix + "/confirm-email-change",
          pathPrefix + "/client-error",
          pathPrefix + "/csp-error")
      .permitAll()
      .antMatchers(pathPrefix + "/admin/**").hasAuthority("ADMIN")
	    .anyRequest().authenticated()
      .and()
    .exceptionHandling()
      .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
    .and()
      .addFilterBefore(this.authHeaderFilter, UsernamePasswordAuthenticationFilter.class);
    // @formatter:on
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

}
