package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    private String password;

    @ManyToMany(mappedBy = "users")
    private Set<Board> boards;
    
    @OneToMany(mappedBy = "assignedTo")
    private Set<Task> tasks;
}
