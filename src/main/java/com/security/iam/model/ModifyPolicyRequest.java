package com.security.iam.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public final class ModifyPolicyRequest {
    private Policy oldPolicy;
    private Policy newPolicy;
}
