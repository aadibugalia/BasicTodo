package com.events

import com.models.TodoModel

data class TodoListEvent(var todoList: TodoModel) {
}