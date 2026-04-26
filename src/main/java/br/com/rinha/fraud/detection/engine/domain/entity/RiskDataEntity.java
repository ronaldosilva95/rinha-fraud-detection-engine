package br.com.rinha.fraud.detection.engine.domain.entity;

public class RiskDataEntity {

  private double[][] vectors;
  private String[] labels;

  public RiskDataEntity(double[][] vectors, String[] labels) {
    this.vectors = vectors;
    this.labels = labels;
  }

  public double[][] getVectors() {
    return vectors;
  }

  public String[] getLabels() {
    return labels;
  }
}
