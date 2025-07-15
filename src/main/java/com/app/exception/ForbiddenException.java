package com.app.exception;

/**
 * ForbiddenException is thrown when a user attempts to perform an action
 * that they are not authorized to do, such as accessing a resource they
 * do not have permission for.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String msg) {
        super(msg);
    }
}
