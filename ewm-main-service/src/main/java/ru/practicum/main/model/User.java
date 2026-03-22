package ru.practicum.main.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uq_email", columnNames = "email")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 250)
    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;
}