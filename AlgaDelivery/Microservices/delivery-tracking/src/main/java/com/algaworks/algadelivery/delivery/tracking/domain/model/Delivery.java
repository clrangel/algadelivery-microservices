package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
@Getter
public class Delivery {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private UUID courierId;

    private DeliveryStatus status;

    private OffsetDateTime placedAt;
    private OffsetDateTime assignedAt;
    private OffsetDateTime expectedDeliveryAt;
    private OffsetDateTime fulfilledAt;

    private BigDecimal distanceFee;
    private BigDecimal courierPayout;
    private BigDecimal totalCost;

    private Integer totalItems;

    private ContactPoint sender;
    private ContactPoint recipient;

    private List<Item> items = new ArrayList<>();

    //Metodo estatico
    //Static Factory - Cria uma delivery vazia
    public static Delivery draft() {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setStatus( DeliveryStatus.DRAFT);
        delivery.setTotalItems(0);
        delivery.setTotalCost(BigDecimal.ZERO);
        delivery.setCourierPayout(BigDecimal.ZERO);
        delivery.setDistanceFee(BigDecimal.ZERO);
        return delivery;
    }

    //Adiciona item
    public UUID addItem(String name, int quantity) {
        Item item = Item.brandNew(name, quantity, this);
        items.add(item);
        calculateTotalItems();
        return item.getId();
    }

    //Calcula todos os itens
    private void calculateTotalItems() {
        int totalItems = getItems().stream().mapToInt(Item::getQuantity).sum();
        setTotalItems(totalItems);
    }

    //Remove um item
    public void removeItem(UUID itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        calculateTotalItems();
    }

    //Remove todos os itens
    public void removeItems() {
        items.clear();
        calculateTotalItems();
    }

    //Altera apenas a quantidade de um item existente na lista de itens da entrega.
    //Recalcula o total de itens após a alteração.
    public void changeItemQuantity(UUID itemId, int quantity) {
        Item item = getItems().stream().filter(i -> i.getId().equals(itemId))
                .findFirst().orElseThrow();

        item.setQuantity(quantity);
        calculateTotalItems();
    }

    // 7
    //Atualiza os dados da entrega com base nas informações de preparação recebidas.
    public void editPreparationDetails(PreparationDetails details) {
        verifyIfCanBeEdited();

        setSender(details.getSender());
        setRecipient(details.getRecipient());
        setDistanceFee(details.getDistanceFee());
        setCourierPayout(details.getCourierPayout());

        setExpectedDeliveryAt(OffsetDateTime.now().plus(details.getExpectedDeliveryTime()));
        setTotalCost(this.getDistanceFee().add(this.getCourierPayout()));
    }


    // 1
    //Marca a entrega como pronta para retirada, alterando o status e registrando o horário.
    public void place() {
        verifyIfCanBePlaced();
        this.changeStatusTo(DeliveryStatus.WAITING_FOR_COURIER);
        this.setPlacedAt(OffsetDateTime.now());
    }

    // 2
    //Atribui o entregador à entrega, define status como "EM_TRANSITO" e registra o horário da retirada.
    public void pickUp(UUID courierId) {
        this.setCourierId(courierId);
        this.changeStatusTo(DeliveryStatus.IN_TRANSIT);
        this.setAssignedAt(OffsetDateTime.now());
    }

    // 3
    // Finaliza a entrega, alterando o status para "ENTREGUE" e registrando a data de conclusão.
    public void markAsDelivered() {
        this.changeStatusTo(DeliveryStatus.DELIVERED);
        this.setFulfilledAt(OffsetDateTime.now());
    }

    // 4
    //Verifica se a entrega está preenchida e em status de rascunho antes de ser finalizada.
    //Lança exceção se a entrega não estiver pronta para ser colocada em andamento.
    //Etapa de validação importante antes do place().
    private void verifyIfCanBePlaced() {
        if (!isFilled()) {
            throw new DomainException();
        }
        if (!getStatus().equals(DeliveryStatus.DRAFT)) {
            throw new DomainException();
        }
    }

    // 8
    //Valida e altera o status da entrega.
    private void verifyIfCanBeEdited() {
        if (!getStatus().equals(DeliveryStatus.DRAFT)) {
            throw new DomainException();
        }
    }

    // 5
    //Verifica se todos os dados obrigatórios da entrega estão preenchidos.
    private boolean isFilled() {
        return this.getSender() != null
                && this.getRecipient() != null
                && this.getTotalCost() != null;
    }

    // 9
    //Valida e realiza a transição de status da entrega, lançando exceção se inválida.
    private void changeStatusTo(DeliveryStatus newStatus) {
        if (newStatus != null && this.getStatus().canNotChangeTo(newStatus)) {
            throw new DomainException(
                    "Invalid status transition from " + this.getStatus() +
                            " to " + newStatus
            );
        }
        this.setStatus(newStatus);
    }

    // 6
    //Contém os detalhes necessários para preparar uma entrega,
    //como remetente, destinatário, taxa por distância, pagamento ao entregador e tempo estimado de entrega.
    @Getter
    @AllArgsConstructor
    @Builder
    public static class PreparationDetails {
        private ContactPoint sender;
        private ContactPoint recipient;
        private BigDecimal distanceFee;
        private BigDecimal courierPayout;
        private Duration expectedDeliveryTime;
    }


    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

}
