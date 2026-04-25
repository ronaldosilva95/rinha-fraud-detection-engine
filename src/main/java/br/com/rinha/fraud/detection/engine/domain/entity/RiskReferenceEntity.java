package br.com.rinha.fraud.detection.engine.domain.entity;

import java.math.BigDecimal;
import java.util.List;

public record RiskReferenceEntity(List<BigDecimal> vector, String label) {

}
