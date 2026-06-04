package br.com.rinha.fraud.detection.engine.domain.entity;

public class RiskDataEntity {

  private final short[] vectors;
  private final int dim;
  private final int[] bucketStarts;

  public RiskDataEntity(short[] vectors, int dim, int[] bucketStarts) {
    this.vectors = vectors;
    this.dim = dim;
    this.bucketStarts = bucketStarts;
  }

  public short[] getVectors() {
    return vectors;
  }

  public int getDim() {
    return dim;
  }

  public int[] getBucketStarts() {
    return bucketStarts;
  }
}
