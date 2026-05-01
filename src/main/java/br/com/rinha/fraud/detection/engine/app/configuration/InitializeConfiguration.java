package br.com.rinha.fraud.detection.engine.app.configuration;

import br.com.rinha.fraud.detection.engine.domain.entity.RiskDataEntity;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class InitializeConfiguration {

  private static final int DIM = 14;
  private static final int HOURS = 24;
  private static final int DAYS = 7;
  private static final int TX_BUCKETS = 4;
  private static final int BUCKET_COUNT = 8 * HOURS * DAYS * TX_BUCKETS;
  private static final int REFERENCES_MAGIC = 0x52524546;
  private static final int REFERENCES_VERSION = 1;
  private static final String REFERENCES_BIN = "/references.bin";

  private final ObjectMapper mapper = new ObjectMapper();

  @Bean("mccRiskScore")
  public Map<String, Double> initializieMccRiskScore() throws IOException {
    try (var inputStream = openResource("/mcc_risk.json")) {
      return mapper.readValue(inputStream, new TypeReference<Map<String, Double>>() {
      });
    }
  }

  @Bean("riskRereference")
  public RiskDataEntity initializeScoreReference() throws IOException {
    var bucketCounts = countBuckets();
    var bucketStarts = new int[BUCKET_COUNT + 1];
    for (var i = 0; i < BUCKET_COUNT; i++) {
      bucketStarts[i + 1] = bucketStarts[i] + bucketCounts[i];
    }

    var size = bucketStarts[BUCKET_COUNT];
    var vectors = new short[size * DIM];
    var labels = new byte[size];
    loadReferences(vectors, labels, bucketStarts.clone());

    return new RiskDataEntity(vectors, labels, DIM, bucketStarts);
  }

  private int[] countBuckets() throws IOException {
    var counts = new int[BUCKET_COUNT];
    try (var inputStream = new DataInputStream(new BufferedInputStream(openResource(REFERENCES_BIN)))) {
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

  private void loadReferences(short[] vectors, byte[] labels, int[] bucketPositions) throws IOException {
    try (var inputStream = new DataInputStream(new BufferedInputStream(openResource(REFERENCES_BIN)))) {
      var size = readHeader(inputStream);
      var vector = new short[DIM];
      for (var i = 0; i < size; i++) {
        for (var j = 0; j < DIM; j++) {
          vector[j] = inputStream.readShort();
        }
        var label = inputStream.readByte();
        var bucket = bucket(vector[9], vector[10], vector[11], vector[3], vector[4], vector[8]);
        var position = bucketPositions[bucket]++;
        var vectorIndex = position * DIM;
        for (var j = 0; j < DIM; j++) {
          vectors[vectorIndex++] = vector[j];
        }
        labels[position] = label;
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

  private int bucket(short online, short cardPresent, short knownMerchant, short hourValue, short dayValue,
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
