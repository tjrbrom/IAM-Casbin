package com.security.iam.repository;

import com.security.iam.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IJunctionTableRepository extends JpaRepository<Application, Long> {

    @Modifying
    @Query(value = "DELETE FROM application_asset ar WHERE ar.application_id " +
            "= :application_id AND ar.asset_id = :asset_id", nativeQuery = true)
    void deleteAppAssetAssociationFromJunctionTable(@Param("application_id") int application_id, @Param("asset_id") int asset_id);

    @Modifying
    @Query(value = "DELETE FROM application_iam_role ar WHERE ar.application_id = :application_id AND ar.role_id = :role_id", nativeQuery = true)
    void deleteAppRoleAssociationFromJunctionTable(@Param("application_id") int application_id, @Param("role_id") int role_id);

    @Modifying
    @Query(value = "INSERT INTO asset_tag (asset_id, tag_id) VALUES " +
            "(:asset_id, :tag_id)", nativeQuery = true)
    void createAssetTagAssociationToJunctionTable(@Param("asset_id") int asset_id, @Param("tag_id") int tag_id);
}
