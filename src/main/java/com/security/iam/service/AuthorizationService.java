package com.security.iam.service;

import com.security.iam.model.*;
import com.security.iam.repository.*;
import jakarta.annotation.PostConstruct;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.casbin.adapter.JDBCAdapter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public final class AuthorizationService {

    private final JDBCAdapter casbinJDBCAdapter;
    private final IApplicationRepository applicationRepository;
    private final IAssetRepository assetRepository;
    private final IRoleRepository roleRepository;
    private final IJunctionTableRepository junctionTableRepository;
    private final ITagRepository tagRepository;
    private Enforcer enforcer;

    @Autowired
    public AuthorizationService(JDBCAdapter casbinJDBCAdapter,
                                IApplicationRepository applicationRepository,
                                IAssetRepository assetRepository,
                                IRoleRepository roleRepository,
                                IJunctionTableRepository junctionTableRepository,
                                ITagRepository tagRepository) {
        this.casbinJDBCAdapter = casbinJDBCAdapter;
        this.applicationRepository = applicationRepository;
        this.assetRepository = assetRepository;
        this.roleRepository = roleRepository;
        this.junctionTableRepository = junctionTableRepository;
        this.tagRepository = tagRepository;
    }

    @PostConstruct
    public void init() {
        // Because the DB is empty at first,
        // so we need to load the policy from the file adapter (.CSV) first.
        enforcer = new Enforcer(
            "src/main/resources/casbin/model.conf",
            "src/main/resources/casbin/policy.csv");
        // This is a trick to save the current policy to the DB.
        // We can't call e.savePolicy() because the adapter in the enforcer is still the file adapter.
        // The current policy means the policy in the jCasbin enforcer (aka in memory).
        casbinJDBCAdapter.savePolicy(enforcer.getModel());
        // Clear the current policy.
        enforcer.clearPolicy();
        // After that, simply recreate enforcer with jdbc adapter.
        enforcer = new Enforcer(
            "src/main/resources/casbin/model.conf",
            casbinJDBCAdapter
        );
    }

    public Application createApplication(Application application) {
        return applicationRepository.save(application);
    }

    public Asset createAsset(Asset asset) {
        return assetRepository.save(asset);
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public String addAssetToApplication(String applicationUid, String assetName) {
        Asset existingAsset = assetRepository.findByName(assetName);
        Application existingApplication = applicationRepository.findByUid(applicationUid);
        existingApplication.getAssets().add(existingAsset);
        applicationRepository.save(existingApplication);
        return String.format(
            "Asset %s for application with uid %s saved",
            assetName, applicationUid);
    }

    public String addRoleToApplication(String applicationUid, String roleName) {
        Role existingRole = roleRepository.findByName(roleName);
        Application existingApplication = applicationRepository.findByUid(applicationUid);
        existingApplication.getRoles().add(existingRole);
        applicationRepository.save(existingApplication);
        return String.format(
            "Role %s for application with uid %s saved",
            roleName, applicationUid);
    }

    public boolean checkPermissions(Policy policy) {
        return enforcer.enforce(
            policy.getSub(),
            policy.getObj(),
            policy.getAct(),
            policy.getAppUid()
        );
    }

    public List<List<String>> getPolicies() {
        enforcer.loadPolicy();
        return enforcer.getPolicy();
    }

    public void addPolicy(Policy policy) {
        if (!enforcer.enforce(
            policy.getSub(),
            policy.getObj(),
            policy.getAct(),
            policy.getAppUid())) {
            enforcer.addPolicy(
                policy.getSub(),
                policy.getObj(),
                policy.getAct(),
                policy.getAppUid());
        }
    }

    public void modifyPolicy(Policy oldPolicy, Policy newPolicy) {
        if (enforcer.enforce(
            oldPolicy.getSub(),
            oldPolicy.getObj(),
            oldPolicy.getAct(),
            oldPolicy.getAppUid())) {
            enforcer.removePolicy(
                oldPolicy.getSub(),
                oldPolicy.getObj(),
                oldPolicy.getAct(),
                oldPolicy.getAppUid());
            enforcer.addPolicy(
                newPolicy.getSub(),
                newPolicy.getObj(),
                newPolicy.getAct(),
                newPolicy.getAppUid());
        }
    }

    public void dropPolicy(Policy policy) {
        if (enforcer.enforce(
            policy.getSub(),
            policy.getObj(),
            policy.getAct(),
            policy.getAppUid())) {
            enforcer.removePolicy(
                policy.getSub(),
                policy.getObj(),
                policy.getAct(),
                policy.getAppUid());
        }
    }

    public void createTag(String tagName) {
        tagRepository.save(Tag.builder().name(tagName).build());
    }

    @Transactional
    public boolean addTagToAsset(AssetTagRequest assetTagRequest) {
        Asset asset = assetRepository.findByName(
            assetTagRequest.getAssetName());
        Tag tag = tagRepository.findByName(
            assetTagRequest.getTagName());
        junctionTableRepository.createAssetTagAssociationToJunctionTable(
            asset.getId(),
            tag.getId()
        );
        return true;
    }

    @Transactional
    public boolean removeAssetFromApp(String appUid, Asset asset) {
        // find asset and get its id
        Asset foundAsset =
            assetRepository.findByName(asset.getName());
        // find app from uid
        Application app = applicationRepository.findByUid(appUid);

        if (app != null) {
            // delete the app-asset association
            junctionTableRepository.deleteAppAssetAssociationFromJunctionTable(
                app.getId(), foundAsset.getId()
            );
            // remove form casbin_rule, for specific appUid:
            // Get all policies
            List<List<String>> allPolicies = enforcer.getPolicy();
            // Iterate over policies and remove those that match the specified criteria
            allPolicies = new ArrayList<>(allPolicies);
            for (List<String> policy : allPolicies) {
                if (policy.get(1).equals(foundAsset.getName())
                    && policy.get(3).equals(appUid)) {
                    enforcer.removePolicy(policy);
                }
            }
            return true;
        }
        return false;
    }

    @Transactional
    public boolean removeRoleFromApp(String appUid, Role role) {
        // find role and get its id
        Role foundRole =
            roleRepository.findByName(role.getName());
        // find app from uid
        Application app = applicationRepository.findByUid(appUid);

        if (app != null) {
            // delete the app-role association
            junctionTableRepository.deleteAppRoleAssociationFromJunctionTable(
                app.getId(), foundRole.getId()
            );
            // remove form casbin_rule, for specific appUid:
            // Get all policies
            List<List<String>> allPolicies = enforcer.getPolicy();
            // Iterate over policies and remove those that match the specified criteria
            allPolicies = new ArrayList<>(allPolicies);
            for (List<String> policy : allPolicies) {
                if (policy.get(0).equals(foundRole.getName())
                        && policy.get(3).equals(appUid)) {
                    enforcer.removePolicy(policy);
                }
            }
            return true;
        }
        return false;
    }

    public boolean hasPolicy(Policy policy) {
        return enforcer.enforce(
            policy.getSub(),
            policy.getObj(),
            policy.getAct(),
            policy.getAppUid()
        );
    }

    public boolean applicationExists(String applicationUid) {
        return applicationRepository.existsByUid(
            applicationUid
        );
    }

    public boolean assetExists(String assetName) {
        return assetRepository.existsByName(
            assetName
        );
    }

    public boolean roleExists(String roleName) {
        return roleRepository.existsByName(
            roleName
        );
    }

    public boolean assetForAppAlreadyExists(
            String applicationUid,
            String assetName) {
        Asset foundAsset = assetRepository.findByName(assetName);
        Set<Application> applicationSet = foundAsset.getApplications();
        if (applicationSet == null || applicationSet.isEmpty()) {
            return false;
        }
        return applicationSet.contains(
            applicationRepository.findByUid(applicationUid));
    }

    public boolean roleForAppAlreadyExists(String applicationUid, String roleName) {
        Role foundRole = roleRepository.findByName(roleName);
        Set<Application> applicationSet = foundRole.getApplications();
        if (applicationSet == null || applicationSet.isEmpty()) {
            return false;
        }
        return applicationSet.contains(
            applicationRepository.findByUid(applicationUid));
    }

    public boolean tagExists(String tagName) {
        return tagRepository.existsByName(tagName);
    }
}