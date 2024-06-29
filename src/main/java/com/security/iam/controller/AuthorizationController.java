package com.security.iam.controller;

import com.security.iam.model.*;
import com.security.iam.service.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.casbin.casdoor.entity.CasdoorUser;
import org.casbin.casdoor.exception.CasdoorAuthException;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/iam")
public final class AuthorizationController {

    @Autowired
    private final AuthorizationService authorizationService;

    @jakarta.annotation.Resource
    private CasdoorAuthService casdoorAuthService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/checkPermissions")
    public ResponseEntity<String> checkPermissions(
            @RequestBody Policy policy) {
        if (authorizationService.checkPermissions(policy)) {
            return ResponseEntity.ok(policy.getSub()+ " has access");
        } else {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Access denied for " + policy.getSub());
        }
    }

    @GetMapping("/policies")
    public ResponseEntity<List<List<String>>> getPolicies() {
        return ResponseEntity.ok().body(authorizationService.getPolicies());
    }

    @PostMapping("/policy/add")
    public ResponseEntity<String> addPolicy(@RequestBody Policy policy) {
        if (authorizationService.hasPolicy(policy)) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(policy + " already exists");
        }
        authorizationService.addPolicy(policy);
        return ResponseEntity.ok().body(policy + " created");
    }

    @PutMapping("/policy/modify")
    public ResponseEntity<String> modifyPolicy(
            @RequestBody ModifyPolicyRequest modifyPolicyRequest) {
        if (!authorizationService.hasPolicy(modifyPolicyRequest.getOldPolicy())) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(modifyPolicyRequest.getOldPolicy() + " does not exist");
        }
        authorizationService.modifyPolicy(
            modifyPolicyRequest.getOldPolicy(),
            modifyPolicyRequest.getNewPolicy()
        );
        return ResponseEntity.ok().body(modifyPolicyRequest.getOldPolicy() + " modified");
    }

    @DeleteMapping("/policy/drop")
    public ResponseEntity<String> dropPolicy(@RequestBody Policy policy) {
        if (!authorizationService.hasPolicy(policy)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(policy + " does not exist");
        }
        authorizationService.dropPolicy(policy);
        return ResponseEntity.ok().body(policy + " removed");
    }

    @PostMapping("/application/create")
    public ResponseEntity<String> createApplication(@RequestBody Application application) {
        if (authorizationService.applicationExists(application.getUid())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Application " + application.getName() + " already exists");
        }
        application.setAssets(Collections.emptySet());
        authorizationService.createApplication(application);
        return ResponseEntity.ok().body("Application " + application.getName() + " created");
    }

    @PostMapping("/asset/create")
    public ResponseEntity<String> createAsset(@RequestBody Asset asset) {
        if (authorizationService.assetExists(asset.getName())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Asset " + asset.getName() + " already exists");
        }
        asset.setApplications(Collections.emptySet());
        authorizationService.createAsset(asset);
        return ResponseEntity.ok().body("Asset " + asset.getName() + " created");
    }

    @DeleteMapping("/asset/remove_from_app")
    public ResponseEntity<String> removeAssetFromApp(
        @RequestBody DeleteAssetRequest deleteAssetRequest) {
        Asset asset = deleteAssetRequest.getAsset();
        String appUid = deleteAssetRequest.getAppUid();
        if (!authorizationService.assetExists(
                asset.getName())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Asset " + asset.getName() + " doesn't exist");
        }
        if (!authorizationService.applicationExists(appUid)) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("application with uid " + appUid + " doesn't exist");
        }
        boolean deleted = authorizationService
                .removeAssetFromApp(appUid, asset);
        if (deleted) {
            return ResponseEntity.ok().body(asset.getName() + " removed");
        }
        return ResponseEntity.ok().body("Removal for " + asset.getName() + " failed");
    }

    @PutMapping("/asset/add_to_app")
    public ResponseEntity<String> addAssetToApplication(
            @RequestBody AssetAndApplicationRequest requestBody) {
        String assetName = requestBody.getAssetName();
        String applicationUid = requestBody.getApplicationUid();
        if (!authorizationService.assetExists(assetName)
                || !authorizationService.applicationExists(applicationUid)) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Asset " + assetName + " or application with uid "
                    + applicationUid + " doesn't exist");
        }
        if (authorizationService.assetForAppAlreadyExists(applicationUid,
                assetName)) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Asset " + assetName + " or application with uid "
                    + applicationUid + " already exists");
        }
        return ResponseEntity.ok().body(
            authorizationService.addAssetToApplication(
                applicationUid,
                assetName
            ));
    }

    @PostMapping("/asset/add_tag")
    public ResponseEntity<String> createTagForAsset(
            @RequestBody AssetTagRequest assetTagRequest) {
        if (!authorizationService.assetExists(
                assetTagRequest.getAssetName())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Asset " + assetTagRequest.getAssetName() + " doesn't exist");
        }
        if (!authorizationService.tagExists(
                assetTagRequest.getTagName())) {
            authorizationService.createTag(assetTagRequest.getTagName());
        }
        if (authorizationService.addTagToAsset(assetTagRequest)) {
            return ResponseEntity.ok().body(String.format(
                "Tag %s created for asset %s",
                assetTagRequest.getTagName(),
                assetTagRequest.getAssetName()));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Tag creation failed");
    }

    @PostMapping("/role/create")
    public ResponseEntity<String> createRole(@RequestBody Role role) {
        if (authorizationService.roleExists(role.getName())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Role " + role.getName() + " already exists");
        }
        authorizationService.createRole(role);
        return ResponseEntity.ok().body("Role " + role.getName() + " created");
    }

    @DeleteMapping("/role/remove_from_app")
    public ResponseEntity<String> removeRoleFromApp(
            @RequestBody DeleteRoleRequest deleteRoleRequest) {
        Role role = deleteRoleRequest.getRole();
        String appUid = deleteRoleRequest.getAppUid();
        if (!authorizationService.roleExists(role.getName())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Role " + role.getName() + " doesn't exist");
        }
        if (!authorizationService.applicationExists(appUid)) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("application with uid " + appUid + " doesn't exist");
        }
        boolean deleted = authorizationService
                .removeRoleFromApp(appUid, role);
        if (deleted) {
            return ResponseEntity.ok().body(role.getName() + " removed");
        }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Removal for " + role.getName() + " failed");
    }

    @PutMapping("/role/add_to_app")
    public ResponseEntity<String> addRoleToApplication(
            @RequestBody RoleAndApplicationRequest requestBody) {
        String roleName = requestBody.getRoleName();
        String applicationUid = requestBody.getApplicationUid();
        if (!authorizationService.roleExists(roleName)
                || !authorizationService.applicationExists(applicationUid)) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(String.format(
                    "Role %s or application with uid %s doesn't exist",
                    roleName, applicationUid));
        }
        if (authorizationService.roleForAppAlreadyExists(applicationUid, roleName)) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(String.format(
                    "Role %s for application with uid %s already exists",
                    roleName, applicationUid));
        }
        return ResponseEntity.ok().body(
            authorizationService.addRoleToApplication(
                applicationUid,
                roleName
            ));
    }

    @RequestMapping("toLogin")
    public String toLogin() {
        return "redirect:" + casdoorAuthService.getSigninUrl(
                "http://localhost:7001/login");
    }

    @RequestMapping("login")
    public String login(String code, String state, HttpServletRequest request) {
        String token = "";
        CasdoorUser user = null;
        try {
            token = casdoorAuthService.getOAuthToken(code, state);
            user = casdoorAuthService.parseJwtToken(token);
        } catch (CasdoorAuthException e) {
            e.printStackTrace();
        }
        HttpSession session = request.getSession();
        session.setAttribute("casdoorUser", user);
        return "redirect:/";
    }
}
