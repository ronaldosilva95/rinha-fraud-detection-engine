package br.com.rinha.fraud.detection.engine.app.service;

import br.com.rinha.fraud.detection.engine.domain.entity.RiskReferenceEntity;
import br.com.rinha.fraud.detection.engine.domain.entity.VectorMatchEntity;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class VectorSearchService {

  /**
   * Calcula a distância euclidiana entre dois vetores
   */
  public BigDecimal calculateEuclideanDistance(List<BigDecimal> vector1, List<BigDecimal> vector2) {
    BigDecimal sumOfSquares = BigDecimal.ZERO;
    for (int i = 0; i < vector1.size(); i++) {
      BigDecimal diff = vector1.get(i).subtract(vector2.get(i));
      sumOfSquares = sumOfSquares.add(diff.multiply(diff, new MathContext(4)));
    }
    return sumOfSquares;

    // √ da soma dos quadrados
//    return new BigDecimal(Math.sqrt(sumOfSquares.doubleValue()), new MathContext(4));
  }

  /**
   * Busca os K vizinhos mais próximos (KNN - K-Nearest Neighbors)
   */
  public List<VectorMatchEntity> findNearestNeighbors(List<BigDecimal> queryVector,
      List<RiskReferenceEntity> dataPoints, int k) {

    return dataPoints.parallelStream().map(data -> new VectorMatchEntity(
            data, calculateEuclideanDistance(queryVector, data.vector())))
        .sorted(Comparator.comparing(VectorMatchEntity::distance))
        .limit(k)
        .collect(Collectors.toList());
  }

}