package ch.rasc.otodo.config;

import java.time.Duration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Configuration
public class WebConfig {

	@Bean
	WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
				List<MediaType> cspReportMediaTypes = List.of(new MediaType("application", "csp-report"));

				var cspReportConverter = new JacksonJsonHttpMessageConverter() {
					@Override
					public List<MediaType> getSupportedMediaTypes() {
						return cspReportMediaTypes;
					}
				};

				builder.registerDefaults().addCustomConverter(cspReportConverter);
			}

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				Bandwidth limit = Bandwidth.builder().capacity(60).refillGreedy(60, Duration.ofMinutes(1)).build();
				Bucket bucket = Bucket.builder().addLimit(limit).build();
				registry.addInterceptor(new RateLimitInterceptor(bucket, 1)).addPathPatterns("/login", "/signup");
			}

		};
	}

}
