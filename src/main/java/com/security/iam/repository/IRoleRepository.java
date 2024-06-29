package com.security.iam.repository;

import com.security.iam.model.Role;
import org.springframework.data.repository.CrudRepository;

public interface IRoleRepository extends CrudRepository<Role, Long> {
    boolean existsByName(String name);

    Role findByName(String name);
}
