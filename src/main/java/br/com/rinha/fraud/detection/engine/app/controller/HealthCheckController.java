package br.com.rinha.fraud.detection.engine.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

  @GetMapping("/ready")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("OK");
  }

}
