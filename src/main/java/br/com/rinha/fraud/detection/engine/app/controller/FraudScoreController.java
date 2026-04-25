package br.com.rinha.fraud.detection.engine.app.controller;

import br.com.rinha.fraud.detection.engine.app.dto.FraudScoreRequest;
import br.com.rinha.fraud.detection.engine.app.dto.FraudScoreResponse;
import br.com.rinha.fraud.detection.engine.app.service.FraudDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("fraud-score")
public class FraudScoreController {

  private final FraudDetectionService service;

  public FraudScoreController(FraudDetectionService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<FraudScoreResponse> analyzeFraudScore(@RequestBody FraudScoreRequest request) {
    var response = service.calculateRiskScore(request);
    return ResponseEntity.ok(response);
  }

}
