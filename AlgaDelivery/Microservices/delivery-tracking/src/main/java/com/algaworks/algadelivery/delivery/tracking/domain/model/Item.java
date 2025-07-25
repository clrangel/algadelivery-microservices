package com.algaworks.algadelivery.delivery.tracking.domain.model;

import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
@Getter
public class Item {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private String name;

    @Setter(AccessLevel.PACKAGE) //Protege o domínio contra mudanças indevidas
    private Integer quantity;

    private Delivery delivery;

    //Static Factory - Cria uma delivery vazia
    static Item brandNew(String name, Integer quantity, Delivery delivery) {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName(name);
        item.setQuantity(quantity);
        item.setDelivery(delivery);
        return item;
    }
}
