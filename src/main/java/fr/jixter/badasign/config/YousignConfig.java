package fr.jixter.badasign.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class YousignConfig {

  private static final Logger logger = LoggerFactory.getLogger(YousignConfig.class);

  private final String baseUrl;
  private final String apiKey;

  public YousignConfig(
      @Value("${yousign.api.base-url}") String baseUrl,
      @Value("${yousign.api.api-key}") String apiKey) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
    logger.info("YousignConfig: apiKey={}", apiKey);
  }
}
