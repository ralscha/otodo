package ch.rasc.otodo.config.security;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JooqUserDetails implements UserDetails {

  private static final long serialVersionUID = 1L;

  private final Collection<GrantedAuthority> authorities;

  private final String username;

  private final String password;

  private final boolean enabled;

  private final long userDbId;

  private final boolean locked;

  private final boolean expired;

  public JooqUserDetails(long userId, String email, String passwordHash, Boolean enabled,
      LocalDateTime expired, LocalDateTime lockedOut, String authority,
      Duration loginLockDuration) {
    this.userDbId = userId;

    this.username = email;
    this.password = passwordHash;
    this.enabled = enabled;
    this.expired = expired != null;

    this.locked = lockedOut != null && (loginLockDuration == null
        || lockedOut.isAfter(LocalDateTime.now().minus(loginLockDuration)));

    this.authorities = Collections.singleton(new SimpleGrantedAuthority(authority));
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return this.authorities;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  public long getUserDbId() {
    return this.userDbId;
  }

  @Override
  public boolean isAccountNonExpired() {
    return !this.expired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !this.locked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

}
