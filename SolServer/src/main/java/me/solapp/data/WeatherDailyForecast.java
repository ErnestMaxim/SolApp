package me.solapp.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "weather_daily_forecast")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class WeatherDailyForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate; // No @Temporal annotation

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private Cities city;

    @Column(name = "max_temperature", nullable = false)
    private Double maxTemperature;

    @Column(name = "min_temperature", nullable = false)
    private Double minTemperature;

    @ManyToOne
    @JoinColumn(name = "weather_status_id", nullable = false)
    private WeatherStatus weatherStatus;
}
