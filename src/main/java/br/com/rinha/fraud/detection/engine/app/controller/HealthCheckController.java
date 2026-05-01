package br.com.rinha.fraud.detection.engine.app.controller;

import java.lang.management.ManagementFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

  @GetMapping("/ready")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/threads")
  public String getThreads() {
    var bean = ManagementFactory.getThreadMXBean();

    return """
        {
          "current": %d,
          "daemon": %d,
          "peak": %d
        }
        """.formatted(
        bean.getThreadCount(),
        bean.getDaemonThreadCount(),
        bean.getPeakThreadCount()
    );
  }

}
