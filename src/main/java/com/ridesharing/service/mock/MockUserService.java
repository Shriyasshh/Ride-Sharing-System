package com.ridesharing.service.mock;

import com.ridesharing.model.User;
import com.ridesharing.service.interfaces.UserService;

public class MockUserService implements UserService {

    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public User findById(int userId) {
        for (User u : store.getUsers()) {
            if (u.getUserId() == userId) return u;
        }
        return null;
    }

    @Override
    public boolean updateProfile(User user) {
        for (int i = 0; i < store.getUsers().size(); i++) {
            if (store.getUsers().get(i).getUserId() == user.getUserId()) {
                store.getUsers().set(i, user);
                if (store.getCurrentUser() != null &&
                    store.getCurrentUser().getUserId() == user.getUserId()) {
                    store.setCurrentUser(user);
                }
                return true;
            }
        }
        return false;
    }
}
