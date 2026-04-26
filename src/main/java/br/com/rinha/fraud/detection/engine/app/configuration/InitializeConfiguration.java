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
      var list = new ObjectMapper().readValue(inputStream,
          new TypeReference<List<RiskReferenceEntity>>() {
          });

      var size = list.size();
      var dim = 14;

      double[] vectors = new double[size * dim];
      String[] labels = new String[size];

      int index = 0;
      for (int i = 0; i < size; i++) {
        var item = list.get(i);
        var currentVector = item.vector();

        for (int j = 0; j < dim; j++) {
          vectors[index++] = currentVector[j];
        }

        labels[i] = item.label();
      }

      return new RiskDataEntity(vectors, labels, dim);
    }
  }
}
