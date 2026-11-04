package com.example.demo.todo;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoService {
    private static final Logger log = LoggerFactory.getLogger(TodoService.class);

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public List<TodoResponse> list() {
        log.debug("Listing todos");
        return repository.findAll(Sort.by("id")).stream().map(TodoResponse::from).toList();
    }

    public TodoResponse get(Long id) {
        log.debug("Loading todo id={}", id);
        return TodoResponse.from(find(id));
    }

    @Transactional
    public TodoResponse create(TodoRequest request) {
        log.debug("Creating todo");
        return TodoResponse.from(repository.save(new Todo(request.title(), request.description(), Boolean.TRUE.equals(request.completed()), request.dueDate())));
    }

    @Transactional
    public TodoResponse update(Long id, TodoRequest request) {
        log.debug("Updating todo id={}", id);
        Todo todo = find(id);
        todo.update(request.title(), request.description(), Boolean.TRUE.equals(request.completed()), request.dueDate());
        return TodoResponse.from(todo);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting todo id={}", id);
        repository.delete(find(id));
    }

    private Todo find(Long id) {
        return repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
    }
}
