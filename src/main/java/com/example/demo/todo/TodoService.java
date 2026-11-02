package com.example.demo.todo;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public List<TodoResponse> list() {
        return repository.findAll(Sort.by("id")).stream().map(TodoResponse::from).toList();
    }

    public TodoResponse get(Long id) {
        return TodoResponse.from(find(id));
    }

    @Transactional
    public TodoResponse create(TodoRequest request) {
        return TodoResponse.from(repository.save(new Todo(request.title(), request.description(), Boolean.TRUE.equals(request.completed()), request.dueDate())));
    }

    @Transactional
    public TodoResponse update(Long id, TodoRequest request) {
        Todo todo = find(id);
        todo.update(request.title(), request.description(), Boolean.TRUE.equals(request.completed()), request.dueDate());
        return TodoResponse.from(todo);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Todo find(Long id) {
        return repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
    }
}
