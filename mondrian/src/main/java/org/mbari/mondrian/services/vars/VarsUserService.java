package org.mbari.mondrian.services.vars;

import org.mbari.mondrian.services.UsersService;
import org.mbari.vars.services.UserService;
import org.mbari.vars.services.model.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class VarsUserService implements UsersService {

    private final UserService userService;

    public VarsUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public CompletableFuture<List<User>> findAllUsers() {
        return userService.findAllUsers();
    }

    @Override
    public CompletableFuture<User> create(User user) {
        return userService.create(user);
    }

    @Override
    public CompletableFuture<Optional<User>> update(User user) {
        return userService.update(user);
    }
}
