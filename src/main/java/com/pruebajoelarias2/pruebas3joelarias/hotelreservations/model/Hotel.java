package com.pruebajoelarias2.pruebas3joelarias.hotelreservations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "HOTEL")
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HOTEL_ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre;

    @Column(name = "DIRECCION", nullable = false)
    private String direccion;

    @Column(name = "ESTRELLAS", nullable = false)
    private int estrellas;

    @OneToMany(mappedBy = "hotel")
    private List<Habitacion> habitaciones;

}
