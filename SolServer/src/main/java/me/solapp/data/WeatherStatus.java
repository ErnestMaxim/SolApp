package me.solapp.data;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "weather_status")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class WeatherStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "weather_status", nullable = false)
    private String weatherStatus;

    @Column(name = "description", nullable = false)
    private String weatherDescription;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherStatus status = (WeatherStatus) o;
        return Objects.equals(id, status.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
