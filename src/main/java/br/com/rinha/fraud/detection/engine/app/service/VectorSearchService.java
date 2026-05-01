package br.com.rinha.fraud.detection.engine.app.service;

import br.com.rinha.fraud.detection.engine.app.constants.ApiConstants;
import br.com.rinha.fraud.detection.engine.domain.entity.RiskDataEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VectorSearchService {

  private static final int VECTOR_SCALE = 10_000;
  private static final byte FRAUD_LABEL = 1;
  private static final int HOURS = 24;
  private static final int DAYS = 7;
  private static final int TX_BUCKETS = 4;

  private final RiskDataEntity RISK_REFERENCE_LIST;

  public VectorSearchService(@Qualifier("riskRereference") RiskDataEntity riskRereference) {
    this.RISK_REFERENCE_LIST = riskRereference;
  }

  public double getScoreByNearestNeighbors(double[] queryVector) {
    int sizeBestIndex = ApiConstants.SIZE_BEST_INDEX;
    long[] bestDistance = new long[sizeBestIndex];
    int[] bestIndexes = new int[sizeBestIndex];

    for (int i = 0; i < sizeBestIndex; i++) {
      bestDistance[i] = Long.MAX_VALUE;
      bestIndexes[i] = -1;
    }

    var vectors = RISK_REFERENCE_LIST.getVectors();
    var labels = RISK_REFERENCE_LIST.getLabels();
    var dim = RISK_REFERENCE_LIST.getDim();
    var bucketStarts = RISK_REFERENCE_LIST.getBucketStarts();

    short q0 = scale(queryVector[0]);
    short q1 = scale(queryVector[1]);
    short q2 = scale(queryVector[2]);
    short q3 = scale(queryVector[3]);
    short q4 = scale(queryVector[4]);
    short q5 = scale(queryVector[5]);
    short q6 = scale(queryVector[6]);
    short q7 = scale(queryVector[7]);
    short q8 = scale(queryVector[8]);
    short q9 = scale(queryVector[9]);
    short q10 = scale(queryVector[10]);
    short q11 = scale(queryVector[11]);
    short q12 = scale(queryVector[12]);
    short q13 = scale(queryVector[13]);

    var binaryBucket = binaryBucket(q9, q10, q11);
    var hourBucket = hourBucket(q3);
    var dayBucket = dayBucket(q4);
    var txBucket = txBucket(q8);

    int worstIdx = 0;
    long worstDistance = Long.MAX_VALUE;
    var hourStart = Math.max(0, hourBucket - 3);
    var hourEnd = Math.min(HOURS - 1, hourBucket + 3);
    var txStart = Math.max(0, txBucket - 1);
    var txEnd = Math.min(TX_BUCKETS - 1, txBucket + 1);
    // Days mantém ±1 para cobrir fraudes de dias próximos (sábado/domingo)
    var dayStart = Math.max(0, dayBucket - 1);
    var dayEnd = Math.min(DAYS - 1, dayBucket + 1);

    for (var currentHour = hourStart; currentHour <= hourEnd; currentHour++) {
      for (var currentTx = txStart; currentTx <= txEnd; currentTx++) {
        var bucket = bucket(binaryBucket, currentHour, dayBucket, currentTx);
        var start = bucketStarts[bucket];
        var end = bucketStarts[bucket + 1];

        for (int i = start; i < end; i++) {
          int base = i * dim;
          long distance = 0L;

          int d9 = vectors[base + 9] - q9;
          distance += (long) d9 * d9;
          if (distance >= worstDistance) {
            continue;
          }
          int d10 = vectors[base + 10] - q10;
          distance += (long) d10 * d10;
          if (distance >= worstDistance) {
            continue;
          }
          int d11 = vectors[base + 11] - q11;
          distance += (long) d11 * d11;
          if (distance >= worstDistance) {
            continue;
          }
          int d5 = vectors[base + 5] - q5;
          distance += (long) d5 * d5;
          if (distance >= worstDistance) {
            continue;
          }
          int d6 = vectors[base + 6] - q6;
          distance += (long) d6 * d6;
          if (distance >= worstDistance) {
            continue;
          }
          int d7 = vectors[base + 7] - q7;
          distance += (long) d7 * d7;
          if (distance >= worstDistance) {
            continue;
          }
          int d8 = vectors[base + 8] - q8;
          distance += (long) d8 * d8;
          if (distance >= worstDistance) {
            continue;
          }
          int d12 = vectors[base + 12] - q12;
          distance += (long) d12 * d12;
          if (distance >= worstDistance) {
            continue;
          }
          int d0 = vectors[base] - q0;
          distance += (long) d0 * d0;
          if (distance >= worstDistance) {
            continue;
          }
          int d1 = vectors[base + 1] - q1;
          distance += (long) d1 * d1;
          if (distance >= worstDistance) {
            continue;
          }
          int d2 = vectors[base + 2] - q2;
          distance += (long) d2 * d2;
          if (distance >= worstDistance) {
            continue;
          }
          int d3 = vectors[base + 3] - q3;
          distance += (long) d3 * d3;
          if (distance >= worstDistance) {
            continue;
          }
          int d4 = vectors[base + 4] - q4;
          distance += (long) d4 * d4;
          if (distance >= worstDistance) {
            continue;
          }
          int d13 = vectors[base + 13] - q13;
          distance += (long) d13 * d13;

          if (distance < worstDistance) {
            bestDistance[worstIdx] = distance;
            bestIndexes[worstIdx] = i;

            worstIdx = 0;
            worstDistance = bestDistance[0];
            for (int j = 1; j < sizeBestIndex; j++) {
              if (bestDistance[j] > worstDistance) {
                worstDistance = bestDistance[j];
                worstIdx = j;
              }
            }
          }
        }
      }
    }

    int fraudCount = 0;
    for (var idx : bestIndexes) {
      if (idx >= 0 && labels[idx] == FRAUD_LABEL) {
        fraudCount++;
      }
    }

    return fraudCount / (double) sizeBestIndex;
  }

  private short scale(double value) {
    return (short) Math.round(value * VECTOR_SCALE);
  }

  private int binaryBucket(short online, short cardPresent, short knownMerchant) {
    var binaryBucket = 0;
    if (online > 5_000) {
      binaryBucket |= 1;
    }
    if (cardPresent > 5_000) {
      binaryBucket |= 2;
    }
    if (knownMerchant > 5_000) {
      binaryBucket |= 4;
    }
    return binaryBucket;
  }

  private int hourBucket(short hourValue) {
    var hour = Math.round((hourValue * 23.0f) / 10_000.0f);
    if (hour < 0) {
      return 0;
    }
    return Math.min(hour, HOURS - 1);
  }

  private int dayBucket(short dayValue) {
    var day = Math.round((dayValue * 6.0f) / 10_000.0f);
    if (day < 0) {
      return 0;
    }
    return Math.min(day, DAYS - 1);
  }

  private int txBucket(short txCountValue) {
    var txBucket = txCountValue / 2_500;
    if (txBucket < 0) {
      return 0;
    }
    return Math.min(txBucket, TX_BUCKETS - 1);
  }

  private int bucket(int binaryBucket, int hour, int day, int txBucket) {
    return (((binaryBucket * HOURS) + hour) * DAYS + day) * TX_BUCKETS + txBucket;
  }
}
