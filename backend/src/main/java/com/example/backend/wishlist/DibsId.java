package com.example.backend.wishlist;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DibsId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "h_id")
    private Long hId;
}