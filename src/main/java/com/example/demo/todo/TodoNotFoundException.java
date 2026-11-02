package com.example.demo.todo;

public class TodoNotFoundException extends RuntimeException {
    public TodoNotFoundException(Long id) {
        super("TODO with id " + id + " was not found");
    }
}
