package br.com.rinha.fraud.detection.engine.app.service;

import static br.com.rinha.fraud.detection.engine.app.constants.ApiConstants.DEFAULT_VALUE_ONE;
import static br.com.rinha.fraud.detection.engine.app.constants.ApiConstants.DEFAULT_VALUE_ZERO;
import static br.com.rinha.fraud.detection.engine.app.constants.ApiConstants.QUERY_BUFFER;
import static br.com.rinha.fraud.detection.engine.app.constants.ApiConstants.VALUE_TO_ROUND_OPERATION;

import br.com.rinha.fraud.detection.engine.app.constants.ApiConstants;
import br.com.rinha.fraud.detection.engine.app.dto.FraudScoreRequest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

  private final Map<String, Double> MCC_RISK_SCORE;
  private final VectorSearchService vectorSearchService;

  public FraudDetectionService(@Qualifier("mccRiskScore") Map<String, Double> mccRiskScore,
      VectorSearchService vectorSearchService) {
    this.MCC_RISK_SCORE = mccRiskScore;
    this.vectorSearchService = vectorSearchService;
  }

  public double calculateRiskScore(FraudScoreRequest request) {
    double[] currentVector = QUERY_BUFFER.get();

    currentVector[0] = limitValue(Math.round((request.transaction().amount() / ApiConstants.MAX_AMOUNT) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[1] = limitValue(Math.round((request.transaction().installments() / ApiConstants.MAX_INSTALLMENTS) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[2] = limitValue(Math.round(((request.transaction().amount() / request.customer().avg_amount()) / ApiConstants.AMOUNT_VS_AVG_RATIO) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[3] = limitValue(Math.round((request.transaction().requested_at().getHour() / 23.0) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[4] = limitValue(Math.round(((request.transaction().requested_at().getDayOfWeek().getValue()-1) / 6.0) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);


    if (request.last_transaction() != null) {
      currentVector[5] = limitValue(Math.round((ChronoUnit.MINUTES.between(request.last_transaction().timestamp(), request.transaction().requested_at()) / ApiConstants.MAX_MINUTES) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION); //5
      currentVector[6] = limitValue(Math.round((request.last_transaction().km_from_current() / ApiConstants.MAX_KM) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION); //6
    } else {
      currentVector[5] = ApiConstants.DEFAULT_VALUE_WITHOUT_LAST_TRANSACTION; //5
      currentVector[6] = ApiConstants.DEFAULT_VALUE_WITHOUT_LAST_TRANSACTION; //6
    }

    currentVector[7] = limitValue(Math.round((request.terminal().km_from_home() / ApiConstants.MAX_KM) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[8] = limitValue(Math.round((request.customer().tx_count_24h() / ApiConstants.MAX_TX_COUNT_24H) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[9] = request.terminal().is_online() ? DEFAULT_VALUE_ONE : DEFAULT_VALUE_ZERO;
    currentVector[10] = request.terminal().card_present() ? DEFAULT_VALUE_ONE : DEFAULT_VALUE_ZERO;
    currentVector[11] = getMerchant(request.customer().known_merchants(), request.merchant().id());

    currentVector[12] = limitValue(getMccRisk(request.merchant().mcc()));
    currentVector[13] = limitValue(Math.round((request.merchant().avg_amount() / ApiConstants.MAX_MERCHANT_AVG_AMOUNT) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);

    var score = vectorSearchService.getScoreByNearestNeighbors(currentVector);
    return score;
  }

  /**
   * Método otimizado para quando os valores já foram parseados diretamente do JSON
   * Evita deserialização completa para ganho de performance
   * Assume que o vetor já foi preenchido com os valores parseados
   */
  public double calculateRiskScore(final double[] request, long requestedAt) {
    double[] currentVector = QUERY_BUFFER.get();

    var transactionAmount = request[0];

    var dateRequestedAt = Instant.ofEpochMilli(requestedAt).atZone(ZoneOffset.UTC);

    currentVector[0] = limitValue(Math.round((transactionAmount / ApiConstants.MAX_AMOUNT) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[1] = limitValue(Math.round((request[1] / ApiConstants.MAX_INSTALLMENTS) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[2] = limitValue(Math.round(((transactionAmount / request[3]) / ApiConstants.AMOUNT_VS_AVG_RATIO) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[3] = limitValue(Math.round((dateRequestedAt.getHour() / 23.0) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[4] = limitValue(Math.round(((dateRequestedAt.getDayOfWeek().getValue()-1) / 6.0) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);

    var lastTransaction = request[12];
    if (lastTransaction > 0) {
      var date = Instant.ofEpochMilli((long) lastTransaction);
      currentVector[5] = limitValue(Math.round((ChronoUnit.MINUTES.between(date, dateRequestedAt) / ApiConstants.MAX_MINUTES) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION); //5
      currentVector[6] = limitValue(Math.round((request[13] / ApiConstants.MAX_KM) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION); //6
    } else {
      currentVector[5] = ApiConstants.DEFAULT_VALUE_WITHOUT_LAST_TRANSACTION; //5
      currentVector[6] = ApiConstants.DEFAULT_VALUE_WITHOUT_LAST_TRANSACTION; //6
    }

    currentVector[7] = limitValue(Math.round((request[11] / ApiConstants.MAX_KM) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[8] = limitValue(Math.round((request[4] / ApiConstants.MAX_TX_COUNT_24H) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);
    currentVector[9] = request[9];
    currentVector[10] = request[10];
    currentVector[11] = request[6];

    currentVector[12] = limitValue(request[7]);
    currentVector[13] = limitValue(Math.round((request[8] / ApiConstants.MAX_MERCHANT_AVG_AMOUNT) * VALUE_TO_ROUND_OPERATION) / VALUE_TO_ROUND_OPERATION);

    var score = vectorSearchService.getScoreByNearestNeighbors(currentVector);
    return score;
  }

  private double getMerchant(String[] merchants, String merchantId) {
    if (merchants == null) {
      return DEFAULT_VALUE_ONE;
    }

    for (int i = 0; i < merchants.length; i++) {
      if (merchants[i].equals(merchantId)) {
        return DEFAULT_VALUE_ZERO;
      }
    }

    return DEFAULT_VALUE_ONE;
  }

  private double getMccRisk(String mcc) {
    return MCC_RISK_SCORE.getOrDefault(mcc, 0.5);
  }

  private double limitValue(double value) {
    if (value > 1) {
      return 1;
    }

    if (value < 0) {
      return 0;
    }

    return value;
  }

}
