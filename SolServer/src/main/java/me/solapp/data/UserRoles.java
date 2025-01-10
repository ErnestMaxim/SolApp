package me.solapp.data;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UserRoles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="role_name",nullable = false, unique = true)
    private String roleName;

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(roleName);
    }
}