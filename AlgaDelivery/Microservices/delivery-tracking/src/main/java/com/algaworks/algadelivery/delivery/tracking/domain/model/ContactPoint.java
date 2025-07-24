package com.algaworks.algadelivery.delivery.tracking.domain.model;

import lombok.*;

@EqualsAndHashCode
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class ContactPoint {

    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String name;
    private String phone;
}
