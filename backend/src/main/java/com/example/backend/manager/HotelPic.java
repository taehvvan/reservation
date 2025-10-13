package com.example.backend.manager;

import com.example.backend.search.Hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hotel_pic")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HotelPic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dir", length = 255)
    private String dir;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "main_image_flag")
    private Boolean mainImageFlag;

    @ManyToOne
    @JoinColumn(name = "h_id")
    private Hotel hotel;
}
