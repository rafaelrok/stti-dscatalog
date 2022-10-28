package com.rafaelvieira.letmebuy.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.rafaelvieira.letmebuy.enums.TypePayment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author rafae
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@JsonTypeName("paymentWithCard")
@Table(name = "tb_payment_card")
public class PaymentCard extends Payment {
    private static final long serialVersionUID = 1L;

    private Integer numberOfInstallments;

    public PaymentCard(Integer id, TypePayment status, Order order, Integer numberOfInstallments) {
        super(id, status, order);
        this.numberOfInstallments = numberOfInstallments;
    }
}
