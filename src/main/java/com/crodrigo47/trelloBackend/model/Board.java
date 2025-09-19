package com.crodrigo47.trelloBackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
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

    public void addUser(User u){
        users.add(u);
        u.getBoards().add(this);
    }

    public void removeUser(User u){
        users.remove(u);
        u.getBoards().remove(this);
    }

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks;

    public void addTask(Task t){
        tasks.add(t);
        t.setBoard(this);
    }

    public void removeTask(Task t){
        tasks.remove(t);
        t.setBoard(null);
    }

}
