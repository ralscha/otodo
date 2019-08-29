package ch.rasc.otodo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.passpol.BreachDatabase;
import com.codahale.passpol.PasswordPolicy;
import com.samskivert.mustache.Mustache;

@Configuration
public class AppConfig {

  @Bean
  public Mustache.Compiler mustacheCompiler() {
    return Mustache.compiler();
  }

  @Bean
  public PasswordPolicy passwordPolicy() {
    return new PasswordPolicy(BreachDatabase.top100K(), 8, 256);
  }

}
