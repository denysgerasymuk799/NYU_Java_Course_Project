package com.unobank.transaction_service.domain_logic.enums;

public enum Events {
    TRANSACTION_TOPUP("EventTransactionTopUp"),
    TRANSACTION_REQUEST("EventTransactionRequest"),
    TRANSACTION_CREATED("EventTransactionCreated"),
    RESERVATION_SUCCESS("EventReservationSuccess"),
    RESERVATION_FAILURE("EventReservationFailure"),
    RESERVATION_CANCEL("EventReservationCancel"),
    TRANSACTION_PENDING("EventTransactionPending"),
    TRANSACTION_SUCCESS("EventTransactionSuccess"),
    TRANSACTION_FAILURE("EventTransactionFailure"),
    TRANSACTION_CANCELLED("EventTransactionCancelled");

    public final String label;

    private Events(String label) {
        this.label = label;
    }
}
