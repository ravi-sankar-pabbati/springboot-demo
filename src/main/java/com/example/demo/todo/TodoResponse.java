package com.example.demo.todo;

import java.time.LocalDate;

public record TodoResponse(Long id, String title, String description, boolean completed, LocalDate dueDate) {
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(todo.getId(), todo.getTitle(), todo.getDescription(), todo.isCompleted(), todo.getDueDate());
    }
}
