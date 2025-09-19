package com.crodrigo47.trelloBackend.exception;

public class TaskNotFoundException extends RuntimeException{
        public TaskNotFoundException(String msg) {
        super(msg);
    }
}
