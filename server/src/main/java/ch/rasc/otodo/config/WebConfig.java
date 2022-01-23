package ch.rasc.otodo.config;

import java.time.Duration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Configuration
public class WebConfig {

  @Bean
  public WebMvcConfigurer webMvcConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        List<MediaType> cspReportMediaTypes = List
            .of(new MediaType("application", "csp-report"));

        HttpMessageConverter<Object> cspReportConverter = new MappingJackson2HttpMessageConverter() {
          @SuppressWarnings("null")
          @Override
          public List<MediaType> getSupportedMediaTypes() {
            return cspReportMediaTypes;
          }
        };

        converters.add(cspReportConverter);
      }

      @Override
      public void addInterceptors(InterceptorRegistry registry) {
        Refill refill = Refill.greedy(60, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(60, refill);
        Bucket bucket = Bucket.builder().addLimit(limit).build();
        registry.addInterceptor(new RateLimitInterceptor(bucket, 1))
            .addPathPatterns("/login", "/signup");
      }

    };
  }

}
