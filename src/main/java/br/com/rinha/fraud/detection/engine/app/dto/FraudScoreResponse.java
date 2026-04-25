package br.com.rinha.fraud.detection.engine.app.dto;

public record FraudScoreResponse(boolean approved, double fraud_score) {

}
