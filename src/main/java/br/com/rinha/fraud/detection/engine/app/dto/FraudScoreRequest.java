package br.com.rinha.fraud.detection.engine.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FraudScoreRequest(String id, TransactionRequest transaction, CustomerRequest customer,
                                MerchantRequest merchant, TerminalRequest terminal,
                                LastTransactionRequest last_transaction) {

  public record TransactionRequest(double amount, double installments, LocalDateTime requested_at) {

  }

  public record CustomerRequest(double avg_amount, double tx_count_24h, List<String> known_merchants) {

  }

  public record MerchantRequest(String id, String mcc, double avg_amount) {

  }

  public record TerminalRequest(boolean is_online, boolean card_present, double km_from_home) {

  }

  public record LastTransactionRequest(LocalDateTime timestamp, double km_from_current) {

  }

}
