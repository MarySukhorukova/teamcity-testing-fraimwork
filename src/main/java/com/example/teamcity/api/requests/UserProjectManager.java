package com.example.teamcity.api.requests;

import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.spec.Specifications;
import static com.example.teamcity.api.enums.Endpoint.*;


public class UserProjectManager {

    public static void createUserAndProject(User user, Project project, CheckedRequests superUserCheckRequests) {
        superUserCheckRequests.getRequest(USERS).create(user);
        var userRequests = new CheckedRequests(Specifications.authSpec(user));
        userRequests.<Project>getRequest(PROJECTS).create(project);
    }
}