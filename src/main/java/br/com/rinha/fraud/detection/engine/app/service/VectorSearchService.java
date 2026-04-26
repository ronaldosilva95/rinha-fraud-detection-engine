package br.com.rinha.fraud.detection.engine.app.service;

import br.com.rinha.fraud.detection.engine.domain.entity.RiskDataEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VectorSearchService {

  private final RiskDataEntity RISK_REFERENCE_LIST;

  public VectorSearchService(@Qualifier("riskRereference") RiskDataEntity riskRereference) {
    this.RISK_REFERENCE_LIST = riskRereference;
  }

  /**
   * Calcula a distância euclidiana entre dois vetores
   */
  public double calculateEuclideanDistance(double[] vector1, double[] vector2) {
    double sumOfSquares = 0.0;
    for (int i = 0; i < vector1.length; i++) {
      double diff = vector1[i] - vector2[i];
      sumOfSquares += diff * diff;
    }
    return sumOfSquares;

    // √ da soma dos quadrados
//    return new BigDecimal(Math.sqrt(sumOfSquares.doubleValue()), new MathContext(4));
  }

  public double getScoreByNearestNeighbors(double[] queryVector) {
    int sizeBestIndex = 5;
    double[] bestDistance = new double[sizeBestIndex];
    int[] bestIndexes = new int[sizeBestIndex];

    // inicializa com infinito
    for (int i = 0; i < sizeBestIndex; i++) {
      bestDistance[i] = Double.MAX_VALUE;
      bestIndexes[i] = -1;
    }

    var vectors = RISK_REFERENCE_LIST.getVectors();
    var labels = RISK_REFERENCE_LIST.getLabels();
    var dim = RISK_REFERENCE_LIST.getDim();

    int size = vectors.length;
    int total = size / dim;

    for (int i = 0; i < total; i++) {
      int base = i * dim;

      // 🔥 distância inline (sem chamada de método)
      double distance =
          sq(vectors[base] - queryVector[0]) +
              sq(vectors[base + 1] - queryVector[1]) +
              sq(vectors[base + 2] - queryVector[2]) +
              sq(vectors[base + 3] - queryVector[3]) +
              sq(vectors[base + 4] - queryVector[4]) +
              sq(vectors[base + 5] - queryVector[5]) +
              sq(vectors[base + 6] - queryVector[6]) +
              sq(vectors[base + 7] - queryVector[7]) +
              sq(vectors[base + 8] - queryVector[8]) +
              sq(vectors[base + 9] - queryVector[9]) +
              sq(vectors[base + 10] - queryVector[10]) +
              sq(vectors[base + 11] - queryVector[11]) +
              sq(vectors[base + 12] - queryVector[12]) +
              sq(vectors[base + 13] - queryVector[13]);

      // encontra o pior dos melhores
      int worstIdx = 0;
      for (int j = 1; j < sizeBestIndex; j++) {
        if (bestDistance[j] > bestDistance[worstIdx]) {
          worstIdx = j;
        }
      }

      // substitui se encontrou melhor
      if (distance < bestDistance[worstIdx]) {
        bestDistance[worstIdx] = distance;
        bestIndexes[worstIdx] = i;
      }
    }

    int fraudCount = 0;
    for (var idx : bestIndexes) {
      if (labels[idx].equals("fraud")) {
        fraudCount++;
      }
    }

    return fraudCount / 5.0;
  }

  private static double sq(double x) {
    return x * x;
  }

}