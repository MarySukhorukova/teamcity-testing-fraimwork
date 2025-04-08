package com.example.teamcity.api;

import com.example.teamcity.api.generators.RoleGenerator;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.RolesRequests;
import com.example.teamcity.api.requests.UserProjectManager;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
import com.example.teamcity.asserts.AssertHelpers;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.example.teamcity.api.enums.Endpoint.*;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest {
    @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest() {
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());
        testData.getBuildType().getProject().setLocator(null);
        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());

        var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES).read("id:" + testData.getBuildType().getId());
        AssertHelpers.assertBuildTypeEquals(softy, testData.getBuildType(), createdBuildType);
    }

    @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {
        var buildTypeWithSameId = generate(Arrays.asList(testData.getProject()), BuildType.class, testData.getBuildType().getId());

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());
        testData.getBuildType().getProject().setLocator(null);
        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());
        new UncheckedBase(Specifications.authSpec(testData.getUser()), BUILD_TYPES)
                .create(buildTypeWithSameId)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("The build configuration / template ID \"%s\" is already used by another configuration or template".formatted(testData.getBuildType().getId())));
    }

    @Test(description = "Project admin should be able to create build type for their project", groups = {"Positive", "Roles"})
    public void projectAdminCreatesBuildTypeTest() {
        UserProjectManager.createUserAndProject(testData.getUser(), testData.getProject(), superUserCheckRequests);
        RoleGenerator.generateProjectAdmin(testData.getProject().getId(), testData.getUser().getUsername());

        testData.getBuildType().getProject().setLocator(null);
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));
        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());

        Response response = new UncheckedBase(Specifications.authSpec(testData.getUser()), BUILD_TYPES)
                .read(testData.getBuildType().getId());

        softy.assertEquals(response.getStatusCode(), HttpStatus.SC_OK, "Status code is incorrect");
    }

    @Test(description = "Project admin should not be able to create build type for not their project", groups = {"Negative", "Roles"})
    public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {
        User user1 = TestDataGenerator.generate(User.class);
        User user2 = TestDataGenerator.generate(User.class);
        Project project1 = TestDataGenerator.generate(Project.class);
        Project project2 = TestDataGenerator.generate(Project.class);

        UserProjectManager.createUserAndProject(user1, project1, superUserCheckRequests);
        RoleGenerator.generateProjectAdmin(project1.getId(), user1.getUsername());

        UserProjectManager.createUserAndProject(user2, project2, superUserCheckRequests);
        RoleGenerator.generateProjectAdmin(project2.getId(), user2.getUsername());

        testData.getBuildType().setProject(project1);
        testData.getBuildType().getProject().setLocator(null);
        BuildType unauthBuildType = TestDataGenerator.generate(BuildType.class);
        unauthBuildType.setProject(project1);

        Response response = new UncheckedBase(Specifications.authSpec(user2), BUILD_TYPES)
                .create(unauthBuildType);

        softy.assertEquals(response.getStatusCode(), HttpStatus.SC_FORBIDDEN, "User2 should not be able to create BuildType in project1!");
    }
}
