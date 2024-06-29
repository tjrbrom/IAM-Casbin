package com.security.iam.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public final class Policy {
    private String sub;
    private String obj;
    private String act;
    private String appUid;
}
