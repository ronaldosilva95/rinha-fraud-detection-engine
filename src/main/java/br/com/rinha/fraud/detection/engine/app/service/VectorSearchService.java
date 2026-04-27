package br.com.rinha.fraud.detection.engine.app.service;

import br.com.rinha.fraud.detection.engine.app.constants.ApiConstants;
import br.com.rinha.fraud.detection.engine.domain.entity.RiskDataEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VectorSearchService {

  private final RiskDataEntity RISK_REFERENCE_LIST;

  public VectorSearchService(@Qualifier("riskRereference") RiskDataEntity riskRereference) {
    this.RISK_REFERENCE_LIST = riskRereference;
  }

  public double getScoreByNearestNeighbors(double[] queryVector) {
    int sizeBestIndex = ApiConstants.SIZE_BEST_INDEX;
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
      double distance = 0.0;

      double d0 = vectors[base] - queryVector[0];
      distance += d0 * d0;

      double d1 = vectors[base + 1] - queryVector[1];
      distance += d1 * d1;

      double d2 = vectors[base + 2] - queryVector[2];
      distance += d2 * d2;
      double d3 = vectors[base + 3] - queryVector[3];
      distance += d3 * d3;
      double d4 = vectors[base + 4] - queryVector[4];
      distance += d4 * d4;
      double d5 = vectors[base + 5] - queryVector[5];
      distance += d5 * d5;
      double d6 = vectors[base + 6] - queryVector[6];
      distance += d6 * d6;
      double d7 = vectors[base + 7] - queryVector[7];
      distance += d7 * d7;
      double d8 = vectors[base + 8] - queryVector[8];
      distance += d8 * d8;
      double d9 = vectors[base + 9] - queryVector[9];
      distance += d9 * d9;
      double d10 = vectors[base + 10] - queryVector[10];
      distance += d10 * d10;
      double d11 = vectors[base + 11] - queryVector[11];
      distance += d11 * d11;
      double d12 = vectors[base + 12] - queryVector[12];
      distance += d12 * d12;
      double d13 = vectors[base + 13] - queryVector[13];
      distance += d13 * d13;


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

}