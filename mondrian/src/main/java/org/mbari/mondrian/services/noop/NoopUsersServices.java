package org.mbari.mondrian.services.noop;

import org.mbari.mondrian.services.UsersService;
import org.mbari.vars.services.model.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NoopUsersServices implements UsersService {

    @Override
    public CompletableFuture<List<User>> findAllUsers() {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<User> create(User user) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented in NOOP service"));
    }

    @Override
    public CompletableFuture<Optional<User>> update(User user) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Not implemented in NOOP service"));
    }
}
