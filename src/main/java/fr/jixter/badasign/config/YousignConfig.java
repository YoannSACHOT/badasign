package fr.jixter.badasign.config;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class YousignConfig {

  private static final Logger logger = LoggerFactory.getLogger(YousignConfig.class);

  private final String baseUrl;
  private final String apiKey;

  public YousignConfig(
      @Value("${yousign.api.base-url}") String baseUrl,
      @Value("${yousign.api.api-key}") String apiKey) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
  }

  /** Creates a configured RestTemplate for Yousign API calls */
  @Bean
  public RestTemplate yousignRestTemplate() {
    logger.info("Configuring Yousign RestTemplate with base URL: {}", baseUrl);

    RestTemplate restTemplate = new RestTemplate();

    // Add interceptor to set authentication headers
    restTemplate.setInterceptors(Collections.singletonList(authenticationInterceptor()));

    return restTemplate;
  }

  /** Creates an interceptor to add authentication headers to all requests */
  private ClientHttpRequestInterceptor authenticationInterceptor() {
    return (request, body, execution) -> {
      HttpHeaders headers = request.getHeaders();

      // Add Authorization header with Bearer token
      headers.setBearerAuth(apiKey);

      // Set Content-Type and Accept headers
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

      // Add User-Agent header
      headers.set("User-Agent", "Badasign/1.0");

      logger.debug("Added authentication headers to request: {}", request.getURI());

      return execution.execute(request, body);
    };
  }

  /** Gets the configured base URL for Yousign API */
  public String getBaseUrl() {
    return baseUrl;
  }

  /** Gets the configured API key (masked for security) */
  public String getMaskedApiKey() {
    if (apiKey == null || apiKey.length() < 8) {
      return "***";
    }
    return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
  }
}
