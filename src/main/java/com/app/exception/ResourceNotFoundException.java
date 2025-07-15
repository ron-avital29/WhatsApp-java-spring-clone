package com.app.exception;

/**
 * ForbiddenException is thrown when a user attempts to perform an action
 * that they are not authorized to do, such as accessing a resource they
 * do not have permission for.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
