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

    for (int i = 0; i < RISK_REFERENCE_LIST.getVectors().length; i++) {
      var distance = calculateEuclideanDistance(queryVector, RISK_REFERENCE_LIST.getVectors()[i]);

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
      if (RISK_REFERENCE_LIST.getLabels()[idx].equals("fraud")) {
        fraudCount++;
      }
    }

    return fraudCount / 5.0;
  }

}