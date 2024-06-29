# IAM System (incomplete)

## Overview

This repository contains my initial attempt at creating an 
Identity and 
Access 
Management 
(IAM) 
system using the Casbin library. 
The system manages 
applications, assets, roles, and policies for fine-grained access control.

## Features

- **Application Management**: Create and manage applications.
- **Asset Management**: Create and manage assets and associate them with applications.
- **Role Management**: Create and manage roles and associate them with applications.
- **Policy Management**: Define and enforce access control policies.
- **Tagging**: Add tags to assets for easier categorization and management.
- **Integration with Casbin**: For handling authorization and access control.
- **Integration with Casdoor**: For authentication.

## Prerequisites

- Java 17+
- Docker and Docker Compose
- Maven

## Getting Started

### Setting Up the Database
```
docker-compose up -d
```

### Configure the Application

1. Update `src/main/resources/application.properties` with your database 
credentials if needed: 
```
spring.datasource.url=jdbc:postgresql://localhost:5436/iam-db
spring.datasource.username=user
spring.datasource.password=password
```
2. Ensure the Casbin model and policy files are correctly placed in 
   `src/main/resources/casbin/`.

---

### Casbin Configuration
Ensure the Casbin model (model.conf) and policy (policy.csv) files are placed in `src/main/resources/casbin/`:

###### Model Configuration (model.conf)
```
[request_definition]
r = sub, obj, act, appUid

[policy_definition]
p = sub, obj, act, appUid

[role_definition]
g = _, _

[policy_effect]
e = some(where (p.eft == allow))

[matchers]
m = g(r.sub, p.sub) && keyMatch(r.obj, p.obj) && (r.act == p.act || p.act == "*") && (r.appUid == p.appUid)
```

###### Policy Configuration (policy.csv)
```
p, alice, data1, read, app1
p, bob, data2, write, app2
```

## API Endpoints

### Authentication

- **Redirect to Login**: `/iam/toLogin`
- **Login Callback**: `/iam/login`

### Application Management

- **Create Application**: `POST /iam/application/create`
- **Check Application Existence**: `GET /iam/application/exists/{uid}`

### Asset Management

- **Create Asset**: `POST /iam/asset/create`
- **Add Asset to Application**: `PUT /iam/asset/add_to_app`
- **Remove Asset from Application**: `DELETE /iam/asset/remove_from_app`
- **Add Tag to Asset**: `POST /iam/asset/add_tag`

### Role Management

- **Create Role**: `POST /iam/role/create`
- **Add Role to Application**: `PUT /iam/role/add_to_app`
- **Remove Role from Application**: `DELETE /iam/role/remove_from_app`

### Policy Management

- **Check Permissions**: `POST /iam/checkPermissions`
- **Get Policies**: `GET /iam/policies`
- **Add Policy**: `POST /iam/policy/add`
- **Modify Policy**: `PUT /iam/policy/modify`
- **Drop Policy**: `DELETE /iam/policy/drop`


### Contributing
Feel free to open issues or submit pull requests if you have suggestions or improvements.

### License
This project is licensed under the MIT License - see the LICENSE file for details.

