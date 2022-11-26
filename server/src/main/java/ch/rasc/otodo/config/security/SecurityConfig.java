package ch.rasc.otodo.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
class SecurityConfig {

  private final AppLogoutSuccessHandler appLogoutSuccessHandler;

  private final AuthHeaderFilter authHeaderFilter;

  public SecurityConfig(AppLogoutSuccessHandler appLogoutSuccessHandler,
      SessionCacheService sessionCacheService) {
    this.appLogoutSuccessHandler = appLogoutSuccessHandler;
    this.authHeaderFilter = new AuthHeaderFilter(sessionCacheService);
  }

  @Bean
  AuthenticationManager authenticationManager() {
    return authentication -> {
      throw new AuthenticationServiceException("Cannot authenticate " + authentication);
    };
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    var pathPrefix = "/be";

    // @formatter:off
    http
    .headers(customizer -> customizer.defaultsDisabled().cacheControl())
    .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .csrf(CsrfConfigurer::disable)
  	.logout(customizer -> {
  	  customizer.logoutSuccessHandler(this.appLogoutSuccessHandler);
  	  customizer.logoutUrl(pathPrefix + "/logout");
  	})
    .authorizeHttpRequests(customizer -> {
      customizer.requestMatchers(
          pathPrefix + "/login",
          pathPrefix + "/signup",
          pathPrefix + "/confirm-signup",
          pathPrefix + "/reset-password-request",
          pathPrefix + "/reset-password",
          pathPrefix + "/confirm-email-change",
          pathPrefix + "/client-error",
          pathPrefix + "/csp-error")
            .permitAll()
          .requestMatchers(pathPrefix + "/admin/**").hasAuthority("ADMIN")
      	  .anyRequest().authenticated();
    })
    .exceptionHandling(customizer -> customizer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
    .addFilterAfter(this.authHeaderFilter, SecurityContextHolderFilter.class);
    // @formatter:on

    return http.build();
  }

}
