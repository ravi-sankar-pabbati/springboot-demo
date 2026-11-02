package com.example.demo.todo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "due_date")
    private LocalDate dueDate;

    protected Todo() { }

    public Todo(String title, String description, boolean completed, LocalDate dueDate) {
        update(title, description, completed, dueDate);
    }

    public void update(String title, String description, boolean completed, LocalDate dueDate) {
        this.title = title.strip();
        this.description = description;
        this.completed = completed;
        this.dueDate = dueDate;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public LocalDate getDueDate() { return dueDate; }
}
