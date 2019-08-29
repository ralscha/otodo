package ch.rasc.otodo.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

  // don't send back anything in case of an error
  @Bean
  public ErrorAttributes errorAttributes() {
    return new DefaultErrorAttributes() {
      @Override
      public Map<String, Object> getErrorAttributes(WebRequest webRequest,
          boolean includeStackTrace) {
        return Map.of();
      }
    };
  }

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
    };
  }

}
