package com.security.iam.repository;

import com.security.iam.model.Tag;
import org.springframework.data.repository.CrudRepository;

public interface ITagRepository extends CrudRepository<Tag, Long> {

    Tag findByName(String tag);

    boolean existsByName(String tagName);
}
