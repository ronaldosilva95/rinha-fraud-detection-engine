package br.com.rinha.fraud.detection.engine.app.constants;

import java.math.BigDecimal;

public class ApiConstants {

  public static final BigDecimal MAX_AMOUNT = new BigDecimal("10000");
  public static final BigDecimal MAX_INSTALLMENTS = new BigDecimal("12");
  public static final BigDecimal AMOUNT_VS_AVG_RATIO = new BigDecimal("10");
  public static final BigDecimal MAX_MINUTES = new BigDecimal("1440");
  public static final BigDecimal MAX_KM = new BigDecimal("1000");
  public static final int MAX_TX_COUNT_24H = 20;
  public static final BigDecimal MAX_MERCHANT_AVG_AMOUNT= new BigDecimal("10000");
  public static final BigDecimal DEFAULT_VALUE_WITHOUT_LAST_TRANSACTION = new BigDecimal("-1");
}
