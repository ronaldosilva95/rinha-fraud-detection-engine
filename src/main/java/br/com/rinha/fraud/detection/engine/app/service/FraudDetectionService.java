package br.com.rinha.fraud.detection.engine.app.service;

import br.com.rinha.fraud.detection.engine.app.constants.ApiConstants;
import br.com.rinha.fraud.detection.engine.app.dto.FraudScoreRequest;
import br.com.rinha.fraud.detection.engine.app.dto.FraudScoreResponse;
import br.com.rinha.fraud.detection.engine.domain.entity.RiskReferenceEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

  private final Map<String, BigDecimal> MCC_RISK_SCORE;
  private final List<RiskReferenceEntity> RISK_REFERENCE_LIST;
  private final VectorSearchService vectorSearchService;

  public FraudDetectionService(@Qualifier("mccRiskScore") Map<String, BigDecimal> mccRiskScore,
      @Qualifier("riskRereference") List<RiskReferenceEntity> riskReferenceList, VectorSearchService vectorSearchService) {
    this.MCC_RISK_SCORE = mccRiskScore;
    this.RISK_REFERENCE_LIST = riskReferenceList;
    this.vectorSearchService = vectorSearchService;
  }

  public FraudScoreResponse calculateRiskScore(FraudScoreRequest request) {

    List<BigDecimal> currentVector = new ArrayList<>();

    //0
    currentVector.add(limitValue(request.transaction().amount().divide(ApiConstants.MAX_AMOUNT, 4, RoundingMode.HALF_UP)));

    //1
    currentVector.add(limitValue(request.transaction().installments().divide(ApiConstants.MAX_INSTALLMENTS, 4, RoundingMode.HALF_UP)));

    //2
//    currentVector.add(limitValue(request.transaction().amount()
//        .divide(request.customer().avg_amount(), 4, RoundingMode.HALF_UP)
//        .divide(ApiConstants.AMOUNT_VS_AVG_RATIO, 4, RoundingMode.HALF_UP)));

    //3
    currentVector.add(limitValue(new BigDecimal(request.transaction().requested_at().getHour()).divide(new BigDecimal("23"), 4, RoundingMode.HALF_UP)));

    //4
    currentVector.add(limitValue(new BigDecimal(request.transaction().requested_at().getDayOfWeek().getValue() -1 ).divide(new BigDecimal("6"), 4, RoundingMode.HALF_UP)));

    if (request.last_transaction() != null) {
      currentVector.add(limitValue(new BigDecimal(ChronoUnit.MINUTES.between(request.transaction().requested_at(), request.last_transaction().timestamp())) .divide(ApiConstants.MAX_MINUTES, 4, RoundingMode.HALF_UP))); //5
      currentVector.add(limitValue(request.last_transaction().km_from_current().divide(ApiConstants.MAX_KM))); //6
    } else {
      currentVector.add(ApiConstants.DEFAULT_VALUE_WITHOUT_LAST_TRANSACTION); //5
      currentVector.add(ApiConstants.DEFAULT_VALUE_WITHOUT_LAST_TRANSACTION); //6
    }

    //7
    currentVector.add(limitValue(request.terminal().km_from_home().divide(ApiConstants.MAX_KM, 4, RoundingMode.HALF_UP)));

    //8
    currentVector.add(limitValue(new BigDecimal(request.customer().tx_count_24h()).divide(new BigDecimal(ApiConstants.MAX_TX_COUNT_24H), 4, RoundingMode.HALF_UP)));

    //9
    currentVector.add(request.terminal().is_online() ? BigDecimal.ONE : BigDecimal.ZERO);

    //10
    currentVector.add(request.terminal().card_present() ? BigDecimal.ONE : BigDecimal.ZERO);

    //11
    currentVector.add(request.customer().know_merchant() != null && request.customer().know_merchant().contains(request.merchant().id()) ? BigDecimal.ZERO : BigDecimal.ONE);

    //12
    currentVector.add(limitValue(getMccRisk(request.merchant().mcc())));

    //13
    currentVector.add(limitValue(request.merchant().avg_amount().divide(ApiConstants.MAX_MERCHANT_AVG_AMOUNT, 4, RoundingMode.HALF_UP)));

    var neighbors = vectorSearchService.findNearestNeighbors(currentVector, RISK_REFERENCE_LIST, 5);
    var score = neighbors.stream().filter(a -> a.data().label().equals("fraud")).count() / 5.0;

    return new FraudScoreResponse(score < 0.6, score);
  }

  private BigDecimal getMccRisk(String mcc) {
    return MCC_RISK_SCORE.getOrDefault(mcc, new BigDecimal("0.5"));
  }

  private BigDecimal limitValue(BigDecimal value) {
    if (value.compareTo(BigDecimal.ONE) > 0) {
      return new BigDecimal("1");
    }

    return value;
  }

}
