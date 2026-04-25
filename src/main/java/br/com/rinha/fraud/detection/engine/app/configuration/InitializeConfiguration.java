package br.com.rinha.fraud.detection.engine.app.configuration;

import br.com.rinha.fraud.detection.engine.domain.entity.RiskReferenceEntity;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class InitializeConfiguration {

  @Bean("mccRiskScore")
  public Map<String, BigDecimal> initializieMccRiskScore() {
    var inputStream = getClass().getResourceAsStream("/mcc_risk.json");;

    var object = new ObjectMapper().readValue(inputStream, new TypeReference<Map<String, BigDecimal>>() {});
    return object;
  }

  @Bean("riskRereference")
  public List<RiskReferenceEntity> initializeScoreReference() {
    var inputStream = getClass().getResourceAsStream("/references.json");
//    var inputStream = resourceLoader.getResource("classpath:example-reference.json").getInputStream();

    var object = new ObjectMapper().readValue(inputStream, new TypeReference<List<RiskReferenceEntity>>() {});
    return object;
  }
}
