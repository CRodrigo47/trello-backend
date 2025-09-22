package com.crodrigo47.trelloBackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    private String token;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    private String email;

    @ManyToMany(mappedBy = "users")
    @JsonIgnore
    private Set<Board> boards;

    public void addBoard(Board b){
        boards.add(b);
        b.getUsers().add(this);
    }

    public void removeBoard(Board b){
        boards.remove(b);
        b.getUsers().remove(this);
    }
    
    @OneToMany(mappedBy = "assignedTo")
    @JsonIgnore
    private Set<Task> tasks;

    public void addTask(Task t){
        tasks.add(t);
        t.setAssignedTo(this);
    }

    public void removeTask(Task t){
        tasks.remove(t);
        t.setAssignedTo(null);
    }
}
