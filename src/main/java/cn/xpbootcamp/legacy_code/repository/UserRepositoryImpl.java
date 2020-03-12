package cn.xpbootcamp.legacy_code.repository;

import cn.xpbootcamp.legacy_code.entity.User;

public class UserRepositoryImpl implements UserRepository {
    @Override
    public User find(long id) {
        throw new RuntimeException("Database server is connecting......");
    }
}
