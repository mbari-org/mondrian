package org.mbari.mondrian.services;

import org.mbari.vars.oni.sdk.r1.models.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UsersService {

    CompletableFuture<List<User>> findAllUsers();

    CompletableFuture<User> create(User user);

    CompletableFuture<Optional<User>> update(User user);
}
