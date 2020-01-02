package ch.rasc.otodo.config.security;

import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import ch.rasc.otodo.config.AppProperties;

@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final AppLogoutSuccessHandler appLogoutSuccessHandler;

  private final AuthHeaderFilter authHeaderFilter;

  public SecurityConfig(AppLogoutSuccessHandler appLogoutSuccessHandler, DSLContext dsl,
      AppProperties appProperties) {
    this.appLogoutSuccessHandler = appLogoutSuccessHandler;
    this.authHeaderFilter = new AuthHeaderFilter(dsl, appProperties);
  }

  @Scheduled(fixedDelayString = "PT15S")
  void updateLastAccess() {
    this.authHeaderFilter.updateLastAccess();
  }

  @Bean
  @Override
  protected AuthenticationManager authenticationManager() throws Exception {
    return authentication -> {
      throw new AuthenticationServiceException("Cannot authenticate " + authentication);
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new Argon2PasswordEncoder(16, 32, 8, 1 << 16, 4);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    var pathPrefix = "/be";

    // @formatter:off
    http
    .headers(customizer -> customizer.defaultsDisabled().cacheControl())
    .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .csrf(customizer -> customizer.disable())
  	.logout(customizer -> {
  	  customizer.logoutSuccessHandler(this.appLogoutSuccessHandler);
  	  customizer.logoutUrl(pathPrefix + "/logout");
  	})
    .authorizeRequests(customizer -> {
      customizer.antMatchers(
          pathPrefix + "/login",
          pathPrefix + "/signup",
          pathPrefix + "/confirm-signup",
          pathPrefix + "/reset-password-request",
          pathPrefix + "/reset-password",
          pathPrefix + "/confirm-email-change",
          pathPrefix + "/client-error",
          pathPrefix + "/csp-error")
            .permitAll()
          .antMatchers(pathPrefix + "/admin/**").hasAuthority("ADMIN")
      	  .anyRequest().authenticated();
    })
    .exceptionHandling(customizer -> customizer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
    .addFilterAfter(this.authHeaderFilter, SecurityContextPersistenceFilter.class);
    // @formatter:on
  }

}
