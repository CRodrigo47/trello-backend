package com.crodrigo47.trelloBackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToMany
    @JoinTable(
        name = "board_user",
        joinColumns = @JoinColumn(name = "board_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private Set<Task> tasks;
}
