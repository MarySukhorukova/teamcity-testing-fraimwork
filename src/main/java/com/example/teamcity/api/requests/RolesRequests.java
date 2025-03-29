package com.example.teamcity.api.requests;

import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.models.Role;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import java.util.List;

public class RolesRequests {
    private final RequestSpecification spec;

    public RolesRequests(RequestSpecification spec) {
        this.spec = spec;
    }

    public void assignProjectRole(String userId, String roleId, String projectId) {
        Roles roles = new Roles(List.of(new Role(roleId, "p:" + projectId)));
        RestAssured.given()
                .spec(spec)
                .body(roles)
                .when()
                .put("/app/rest/users/username:" + userId + "/roles")
                .then()
                .assertThat().statusCode(anyOf(is(HttpStatus.SC_NO_CONTENT), is(HttpStatus.SC_OK)));

    }
}
