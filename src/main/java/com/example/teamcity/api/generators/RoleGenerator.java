package com.example.teamcity.api.generators;

import com.example.teamcity.api.requests.RolesRequests;
import com.example.teamcity.api.spec.Specifications;

public class RoleGenerator {

    public static void generateProjectAdmin(String projectId, String username) {
        new RolesRequests(Specifications.superUserSpec())
                .assignProjectRole(username, "PROJECT_ADMIN", projectId);
    }
}