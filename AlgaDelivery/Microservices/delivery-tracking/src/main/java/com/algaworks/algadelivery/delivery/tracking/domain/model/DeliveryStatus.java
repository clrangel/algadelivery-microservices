package com.algaworks.algadelivery.delivery.tracking.domain.model;

import java.util.Arrays;
import java.util.List;

public enum DeliveryStatus {
    DRAFT,
    WAITING_FOR_COURIER(DRAFT),
    IN_TRANSIT(WAITING_FOR_COURIER),
    DELIVERED(IN_TRANSIT);

    //Lista os status válidos que podem preceder o status atual.
    private final List<DeliveryStatus> previousStatuses;

    DeliveryStatus(DeliveryStatus... previousStatuses) {
        this.previousStatuses = Arrays.asList(previousStatuses);
    }

    //Verifica se a transição para um novo status NÃO é permitida,
    //ou seja, se o status atual não está na lista de status permitidos antes do novo status.
    public boolean canNotChangeTo(DeliveryStatus newStatus) {
        DeliveryStatus current = this;
        return !newStatus.previousStatuses.contains(current);
    }

    //Verifica se a transição para um novo status é permitida.
    public boolean canChangeTo(DeliveryStatus newStatus) {
        return !canNotChangeTo(newStatus);
    }
}
