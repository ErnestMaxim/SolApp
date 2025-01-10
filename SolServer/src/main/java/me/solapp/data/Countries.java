package me.solapp.data;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "countries")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Countries {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "country_iso", nullable = false)
    private String countryIso;
}