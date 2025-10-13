package com.example.backend.reservation;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.backend.register.UserEntity;

import com.example.backend.search.Hotel;
import com.example.backend.search.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "p_id")
    private Integer pId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "u_id")
    private UserEntity user;  // 회원이면 매핑, 비회원이면 null

    @ManyToOne
    @JoinColumn(name = "r_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private Reservation reservation;

    @Column(name = "pay_method")
    private String payMethod;

    @CreationTimestamp
    @Column(name = "pay_date")
    private LocalDateTime payDate = LocalDateTime.now();

    @Column(name = "phone")
    private String phone;

    @Column(name = "payment_key", nullable = false, unique = true)
    private String paymentKey;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Builder
    public Payment(UserEntity user, Room room, Reservation reservation, String payMethod, String phone, String paymentKey, Long amount) {
        this.user = user;
        this.room = room;
        this.reservation = reservation;
        this.payMethod = payMethod;
        this.phone = phone;
        this.paymentKey = paymentKey;
        this.amount = amount;
    }

}