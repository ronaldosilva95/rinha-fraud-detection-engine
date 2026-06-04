package br.com.rinha.fraud.detection.engine.app.configuration;

import br.com.rinha.fraud.detection.engine.domain.entity.RiskDataEntity;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitializeConfiguration {

  private static final int DIM = 14;
  private static final int DIM_WITH_LABEL = 15;
  public static final int HOURS = 24;
  public static final int DAYS = 7;
  public static final int TX_BUCKETS = 20;
  private static final int BUCKET_COUNT = 8 * HOURS * DAYS * TX_BUCKETS;
  private static final int REFERENCES_MAGIC = 0x52524546;
  private static final int REFERENCES_VERSION = 1;
  private static final String REFERENCES_BIN = "/references.bin";

  @Bean("mccRiskScore")
  public Map<String, Double> initializieMccRiskScore() {
    Map<String, Double> map = new HashMap<>();
    map.put("5411", 0.15);
    map.put("5812", 0.30);
    map.put("5912", 0.20);
    map.put("5944", 0.45);
    map.put("7801", 0.80);
    map.put("7802", 0.75);
    map.put("7995", 0.85);
    map.put("4511", 0.35);
    map.put("5311", 0.25);
    map.put("5999", 0.50);
    return map;
  }

  @Bean("riskRereference")
  public RiskDataEntity initializeScoreReference() throws IOException {
    var bucketCounts = countBuckets();
    var bucketStarts = new int[BUCKET_COUNT + 1];
    for (var i = 0; i < BUCKET_COUNT; i++) {
      bucketStarts[i + 1] = bucketStarts[i] + bucketCounts[i];
    }

    var size = bucketStarts[BUCKET_COUNT];
    var vectors = new short[size * DIM_WITH_LABEL];
    loadReferences(vectors, bucketStarts.clone());

    return new RiskDataEntity(vectors, DIM_WITH_LABEL, bucketStarts);
  }

  @Bean
  public RiskDataEntity warmup(RiskDataEntity riskRereference) {
    // Warmup the application by accessing the risk reference data
    var vectors = riskRereference.getVectors();
    var dim = riskRereference.getDim();
    var bucketStarts = riskRereference.getBucketStarts();

    // Access some elements to ensure they are loaded into memory
    for (int i = 0; i < Math.min(10, bucketStarts.length - 1); i++) {
      int start = bucketStarts[i];
      int end = bucketStarts[i + 1];
      for (int j = start; j < Math.min(start + 10, end); j++) {
        int vectorIndex = j * dim;
        // Access the first few dimensions of the vector
        for (int k = 0; k < Math.min(5, dim); k++) {
          short value = vectors[vectorIndex + k];
        }
      }
    }
    return riskRereference;
  }

  private int[] countBuckets() throws IOException {
    var counts = new int[BUCKET_COUNT];
    try (var inputStream = new DataInputStream(
        new BufferedInputStream(openResource(REFERENCES_BIN)))) {
      var size = readHeader(inputStream);
      var vector = new short[DIM];
      for (var i = 0; i < size; i++) {
        for (var j = 0; j < DIM; j++) {
          vector[j] = inputStream.readShort();
        }
        inputStream.readByte();
        counts[bucket(vector[9], vector[10], vector[11], vector[3], vector[4], vector[8])]++;
      }
    }
    return counts;
  }

  private void loadReferences(short[] vectors, int[] bucketPositions) throws IOException {
    try (var inputStream = new DataInputStream(
        new BufferedInputStream(openResource(REFERENCES_BIN)))) {
      var size = readHeader(inputStream);
      var vector = new short[DIM_WITH_LABEL];
      for (var i = 0; i < size; i++) {
        for (var j = 0; j < DIM_WITH_LABEL; j++) {
          if (j == 14) {
            var label = inputStream.readByte();
            vector[j] = label;
          } else {
            vector[j] = inputStream.readShort();
          }
        }

        var bucket = bucket(vector[9], vector[10], vector[11], vector[3], vector[4], vector[8]);
        var position = bucketPositions[bucket]++;
        var vectorIndex = position * DIM_WITH_LABEL;
        for (var j = 0; j < DIM_WITH_LABEL; j++) {
          vectors[vectorIndex++] = vector[j];
        }
      }
    }
  }

  private int readHeader(DataInputStream inputStream) throws IOException {
    var magic = inputStream.readInt();
    if (magic != REFERENCES_MAGIC) {
      throw new IOException("Invalid references binary magic: " + magic);
    }

    var version = inputStream.readInt();
    if (version != REFERENCES_VERSION) {
      throw new IOException("Unsupported references binary version: " + version);
    }

    var dim = inputStream.readInt();
    if (dim != DIM) {
      throw new IOException("Invalid references vector dimension: " + dim);
    }

    return inputStream.readInt();
  }

  private int bucket(short online, short cardPresent, short knownMerchant, short hourValue,
      short dayValue,
      short txCountValue) {
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

    var hour = Math.round((hourValue * 23.0f) / 10_000.0f);
    if (hour < 0) {
      hour = 0;
    } else if (hour >= HOURS) {
      hour = HOURS - 1;
    }

    var day = Math.round((dayValue * 6.0f) / 10_000.0f);
    if (day < 0) {
      day = 0;
    } else if (day >= DAYS) {
      day = DAYS - 1;
    }

    var txBucket = txCountValue / 2_500;
    if (txBucket < 0) {
      txBucket = 0;
    } else if (txBucket >= TX_BUCKETS) {
      txBucket = TX_BUCKETS - 1;
    }

    return (((binaryBucket * HOURS) + hour) * DAYS + day) * TX_BUCKETS + txBucket;
  }

  private InputStream openResource(String resourceName) throws IOException {
    var inputStream = getClass().getResourceAsStream(resourceName);
    if (inputStream == null) {
      throw new IOException("Resource not found: " + resourceName);
    }
    return inputStream;
  }
}
