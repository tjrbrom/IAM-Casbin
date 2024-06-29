package com.security.iam.repository;

import com.security.iam.model.Application;
import org.springframework.data.repository.CrudRepository;

public interface IApplicationRepository extends CrudRepository<Application, Long> {
    boolean existsByUid(String uid);

    Application findByUid(String uid);
}
