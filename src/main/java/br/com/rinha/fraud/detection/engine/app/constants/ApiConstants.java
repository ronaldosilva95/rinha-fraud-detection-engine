package br.com.rinha.fraud.detection.engine.app.constants;

public class ApiConstants {

  public static final double MAX_AMOUNT = 10000.0;
  public static final double MAX_INSTALLMENTS = 12.0;
  public static final double AMOUNT_VS_AVG_RATIO = 10.0;
  public static final double MAX_MINUTES = 1440.0;
  public static final double MAX_KM = 1000.0;
  public static final int MAX_TX_COUNT_24H = 20;
  public static final double MAX_MERCHANT_AVG_AMOUNT = 10000.0;

  public static final double DEFAULT_VALUE_ONE = 1.0;
  public static final double DEFAULT_VALUE_ZERO = 0.0;
  public static final double DEFAULT_VALUE_WITHOUT_LAST_TRANSACTION = -1.0;
  public static final double VALUE_TO_ROUND_OPERATION = 10000.0;
}
