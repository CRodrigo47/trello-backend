package com.crodrigo47.trelloBackend.exception;

public class BoardNotFoundException extends RuntimeException{
        public BoardNotFoundException(String msg) {
        super(msg);
    }
}