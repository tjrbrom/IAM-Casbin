package com.security.iam.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(indexes = @Index(columnList = "uid"))
@EntityListeners(AuditingEntityListener.class)
public final class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String uid;

    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false)
    private String secret;

    @Column
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "application_asset",
        joinColumns = @JoinColumn(name = "application_id"),
        inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    private Set<Asset> assets;

    @Column
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "application_iam_role",
        joinColumns = @JoinColumn(name = "application_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column
    private Timestamp createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column
    private Timestamp updatedAt;
}
