package com.example.backend.wishlist;

import com.example.backend.register.UserEntity;
import com.example.backend.search.Hotel;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "dibs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class DibsEntity {

    @EmbeddedId
    private DibsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("uId")
    @JoinColumn(name = "u_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hId")
    @JoinColumn(name = "h_id")
    private Hotel hotel;
}