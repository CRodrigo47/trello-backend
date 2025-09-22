package com.crodrigo47.trelloBackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    public void assignUser(User u) {
        this.assignedTo = u;    
        u.addTask(this);        
    }

    public void unassignUser() {
        if (this.assignedTo != null) {
            this.assignedTo.removeTask(this);
            this.assignedTo = null;           
        }
    }

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnore
    private Board board;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        FUTURE,
        IN_PROGRESS,
        DONE
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
