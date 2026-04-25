package br.com.rinha.fraud.detection.engine.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FraudScoreRequest(String id, TransactionRequest transaction, CustomerRequest customer,
                                MerchantRequest merchant, TerminalRequest terminal,
                                LastTransactionRequest last_transaction) {

  public record TransactionRequest(BigDecimal amount, BigDecimal installments, LocalDateTime requested_at) {

  }

  public record CustomerRequest(BigDecimal avg_amount, int tx_count_24h, List<String> know_merchant) {

  }

  public record MerchantRequest(String id, String mcc, BigDecimal avg_amount) {

  }

  public record TerminalRequest(boolean is_online, boolean card_present, BigDecimal km_from_home) {

  }

  public record LastTransactionRequest(LocalDateTime timestamp, BigDecimal km_from_current) {

  }

}
