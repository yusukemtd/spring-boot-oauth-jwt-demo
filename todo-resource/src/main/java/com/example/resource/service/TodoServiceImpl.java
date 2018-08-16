package com.example.resource.service;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.resource.mapper.TodoMapper;
import com.example.resource.model.Todo;

@Service
@Transactional
public class TodoServiceImpl implements TodoService {

  private static final int MAX_UNFINISHED_COUNT = 10;

  @Autowired
  TodoMapper todoMapper;

  @Override
  @Transactional(readOnly = true)
  public Todo findOne(String todoId) {
    return todoMapper.findOne(todoId);
  }

  @Override
  @Transactional(readOnly = true)
  public Collection<Todo> findAll() {
    return todoMapper.findAll();
  }

  @Override
  public Todo create(Todo todo) {
    long unfinishedCount = todoMapper.countByFinished(false);
    if (unfinishedCount >= MAX_UNFINISHED_COUNT) {
      // error
    }
    todo.setTodoId(UUID.randomUUID().toString());
    todo.setCreatedAt(new Date());
    todo.setFinished(false);
    todoMapper.create(todo);

    return todo;
  }

  @Override
  public Todo finish(String todoId) {
    Todo todo = findOne(todoId);
    if (todo.isFinished()) {
      // error
    }
    todo.setFinished(true);
    todoMapper.update(todo);
    return todo;
  }

  @Override
  public void delete(String todoId) {
    Todo todo = findOne(todoId);
    todoMapper.delete(todo);
  }
}
