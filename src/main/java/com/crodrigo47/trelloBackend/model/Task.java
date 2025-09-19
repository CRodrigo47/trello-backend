package com.crodrigo47.trelloBackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
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

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        FUTURE,
        IN_PROGRESS,
        DONE
    }
}
