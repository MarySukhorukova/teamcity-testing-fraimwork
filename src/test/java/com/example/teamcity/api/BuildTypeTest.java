package com.example.teamcity.api;

import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.RolesRequests;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
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

        var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES).read(testData.getBuildType().getId());

        softy.assertEquals(testData.getBuildType().getName(), createdBuildType.getName(), "Build type name is not correct");
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
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        new RolesRequests(Specifications.superUserSpec())
                .assignProjectRole(testData.getUser().getUsername(), "PROJECT_ADMIN", testData.getProject().getId());

        testData.getBuildType().getProject().setLocator(null);
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

        superUserCheckRequests.getRequest(USERS).create(user1);
        var user1Requests = new CheckedRequests(Specifications.authSpec(user1));
        user1Requests.<Project>getRequest(PROJECTS).create(project1);

        new RolesRequests(Specifications.superUserSpec())
                .assignProjectRole(user1.getUsername(), "PROJECT_ADMIN", project1.getId());

        superUserCheckRequests.getRequest(USERS).create(user2);
        var user2Requests = new CheckedRequests(Specifications.authSpec(user2));
        user2Requests.<Project>getRequest(PROJECTS).create(project2);

        new RolesRequests(Specifications.superUserSpec())
                .assignProjectRole(user2.getUsername(), "PROJECT_ADMIN", project2.getId());
        testData.getBuildType().setProject(project1);
        testData.getBuildType().getProject().setLocator(null);
        BuildType unauthBuildType = TestDataGenerator.generate(BuildType.class);
        unauthBuildType.setProject(project1);
        Response response = new UncheckedBase(Specifications.authSpec(user2), BUILD_TYPES)
                .create(unauthBuildType);

        softy.assertEquals(response.getStatusCode(), HttpStatus.SC_FORBIDDEN, "User2 should not be able to create BuildType in project1!");
    }
}
