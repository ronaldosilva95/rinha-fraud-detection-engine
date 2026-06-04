package br.com.rinha.fraud.detection.engine.app.controller;

import static br.com.rinha.fraud.detection.engine.app.constants.ApiConstants.QUERY_BUFFER_DESERIALIZER;

import br.com.rinha.fraud.detection.engine.app.dto.FraudScoreRequest;
import br.com.rinha.fraud.detection.engine.app.service.FraudDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("fraud-score")
public class FraudScoreController {

  private final FraudDetectionService service;
  private ObjectMapper mapper = new ObjectMapper();
  private final Map<String, Double> MCC_RISK_SCORE;

  public FraudScoreController(FraudDetectionService service,
      @Qualifier("mccRiskScore") Map<String, Double> mccRiskScore) {
    this.service = service;
    this.MCC_RISK_SCORE = mccRiskScore;
  }

  @PostMapping("/v2")
  public ResponseEntity<String> analyzeFraudScore(@RequestBody FraudScoreRequest request) {
    long t0 = System.nanoTime();
    var score = service.calculateRiskScore(request);
    long t1 = System.nanoTime();
    System.out.println("calc: " + (t1 - t0)/1_000_000.0 + " ms");
    return ResponseEntity.ok("{\"approved\":"+ (score < 0.6) + ",\"fraud_score\":" +score+ "}");
  }

  @PostMapping
  public ResponseEntity<String> parseToVector(HttpServletRequest request) throws Exception {
    short[] vector = QUERY_BUFFER_DESERIALIZER.get();;
    vector[12] = -1;
    List<String> merchants = new ArrayList<>();

    JsonParser parser = mapper.createParser(request.getInputStream());

    long requestedAt = 0;
    long lastTimestamp = 0;

    String currentField = null;
    String currentParent = null;

    while (parser.nextToken() != null) {

      JsonToken token = parser.currentToken();

      if (token == JsonToken.PROPERTY_NAME) {
        currentField = parser.currentName();
        continue;
      }

      if (token == JsonToken.START_OBJECT) {
        currentParent = currentField;
        continue;
      }

      if (token == JsonToken.END_OBJECT) {
        currentParent = null;
        continue;
      }

      boolean isValue = token == JsonToken.VALUE_STRING || token == JsonToken.VALUE_NUMBER_INT ||
          token == JsonToken.VALUE_NUMBER_FLOAT || token == JsonToken.VALUE_TRUE ||
          token == JsonToken.VALUE_FALSE || token == JsonToken.START_ARRAY || token == JsonToken.END_ARRAY;

      if (!isValue) continue;

      // 🔥 transaction - ESCALAR DIRETO
      if ("transaction".equals(currentParent)) {
        switch (currentField) {
          case "amount" -> vector[0] = scale(parser.getDoubleValue());
          case "installments" -> vector[1] = scale(parser.getDoubleValue());
          case "requested_at" -> {
            String ts = parser.getText();
            requestedAt = Instant.parse(ts).toEpochMilli();
            vector[2] = (short) (requestedAt / 1_000_000); // scale timestamp
          }
        }
      }

      // 🔥 customer
      else if ("customer".equals(currentParent)) {
        switch (currentField) {
          case "avg_amount" -> vector[3] = scale(parser.getDoubleValue());
          case "tx_count_24h" -> vector[4] = scale(parser.getDoubleValue());

          case "known_merchants" -> {
            if (token == JsonToken.START_ARRAY) {
              while (parser.nextToken() != JsonToken.END_ARRAY) {
                merchants.add(parser.getText());
              }
              vector[5] = 0;
            }
          }
        }
      }

      // 🔥 merchant
      else if ("merchant".equals(currentParent)) {
        switch (currentField) {
          case "id" -> {
            short knowMerchant = 10_000; // DEFAULT_VALUE_ONE escalado
            String merchantValueId = parser.getString();
            for (String merchant : merchants) {
              if (merchantValueId.equals(merchant)) {
                knowMerchant = 0; // DEFAULT_VALUE_ZERO escalado
                break;
              }
            }
            vector[6] = knowMerchant;
          }
          case "mcc" -> {
            double mccRisk = MCC_RISK_SCORE.getOrDefault(parser.getString(), 0.5);
            vector[7] = scale(mccRisk);
          }
          case "avg_amount" -> vector[8] = scale(parser.getDoubleValue());
        }
      }

      // 🔥 terminal
      else if ("terminal".equals(currentParent)) {
        switch (currentField) {
          case "is_online" -> vector[9] = parser.getBooleanValue() ? (short) 10_000 : (short) 0;
          case "card_present" -> vector[10] = parser.getBooleanValue() ? (short) 10_000 : (short) 0;
          case "km_from_home" -> vector[11] = scale(parser.getDoubleValue());
        }
      }

      // 🔥 last_transaction
      else if ("last_transaction".equals(currentParent)) {
        switch (currentField) {
          case "timestamp" -> {
            String ts = parser.getText();
            lastTimestamp = Instant.parse(ts).toEpochMilli();
            vector[12] = (short) (lastTimestamp / 1_000_000); // scale timestamp
          }
          case "km_from_current" -> vector[13] = scale(parser.getDoubleValue());
        }
      }
    }

    var score = service.calculateRiskScore(vector, requestedAt);
    return ResponseEntity.ok("{\"approved\":"+ (score < 0.6) + ",\"fraud_score\":" +score+ "}");
  }

  private short scale(double value) {
    return (short) Math.round(value * 10_000);
  }

}

