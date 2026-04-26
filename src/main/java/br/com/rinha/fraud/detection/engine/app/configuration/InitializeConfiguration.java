package br.com.rinha.fraud.detection.engine.app.configuration;

import br.com.rinha.fraud.detection.engine.domain.entity.RiskDataEntity;
import br.com.rinha.fraud.detection.engine.domain.entity.RiskReferenceEntity;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class InitializeConfiguration {

  @Bean("mccRiskScore")
  public Map<String, Double> initializieMccRiskScore() throws IOException {
    try (var inputStream = getClass().getResourceAsStream("/mcc_risk.json")) {
      var object = new ObjectMapper().readValue(inputStream,
          new TypeReference<Map<String, Double>>() {
          });
      return object;
    }
  }

  @Bean("riskRereference")
  public RiskDataEntity initializeScoreReference() throws IOException {
    try (var inputStream = getClass().getResourceAsStream("/references.json")) {
//    try (var inputStream = getClass().getResourceAsStream("/example-reference.json")) {
      var list = new ObjectMapper().readValue(inputStream,
          new TypeReference<List<RiskReferenceEntity>>() {
          });


      var size = list.size();
      double[][] vectors = new double[size][14];
      String[] labels = new String[size];

      for(int i = 0 ; i < size; i++) {
        var item = list.get(i);
        vectors[i] = item.vector();
        labels[i] = item.label();
      }

      return new RiskDataEntity(vectors, labels);
    }
  }
}
