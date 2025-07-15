package com.app.projection;

/**
 * UserProjection is an interface that defines a projection for user data.
 * It is used to retrieve specific fields from the User entity without loading
 * the entire entity, which can improve performance in certain scenarios.
 */
public interface UserProjection {
    Long getId();
    String getUsername();
    String getEmail();
}