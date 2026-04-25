package br.com.rinha.fraud.detection.engine.domain.entity;

import java.math.BigDecimal;

public record VectorMatchEntity(RiskReferenceEntity data, BigDecimal distance) {

}
