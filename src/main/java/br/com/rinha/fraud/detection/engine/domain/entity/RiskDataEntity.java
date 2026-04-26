package br.com.rinha.fraud.detection.engine.domain.entity;

public class RiskDataEntity {

  private double[] vectors;
  private String[] labels;
  private int dim;

  public RiskDataEntity(double[] vectors, String[] labels, int dim) {
    this.vectors = vectors;
    this.labels = labels;
    this.dim = dim;
  }

  public double[] getVectors() {
    return vectors;
  }

  public String[] getLabels() {
    return labels;
  }

  public int getDim() {
    return dim;
  }
}
