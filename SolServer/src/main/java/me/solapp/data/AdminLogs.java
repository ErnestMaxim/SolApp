package me.solapp.data;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "admin_logs")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class AdminLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer adminId;

    @Column(nullable = false, length = 255)
    private String action;

    @Column(nullable = false)
    private Timestamp timestamp;
}