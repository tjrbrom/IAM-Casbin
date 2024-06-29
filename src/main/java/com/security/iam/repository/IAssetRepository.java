package com.security.iam.repository;

import com.security.iam.model.Asset;
import org.springframework.data.repository.CrudRepository;

public interface IAssetRepository extends CrudRepository<Asset, Long> {
    boolean existsByName(String name);

    Asset findByName(String name);
}
