package com.example.teamcity.asserts;

import com.example.teamcity.api.models.BuildType;
import org.testng.asserts.SoftAssert;

public class AssertHelpers {

    public static void assertBuildTypeEquals(SoftAssert softly, BuildType expected, BuildType actual) {
        softly.assertEquals(actual.getId(), expected.getId(), "BuildType.id is not correct");
        softly.assertEquals(actual.getName(), expected.getName(), "BuildType.name is not correct");

        if (expected.getProject() != null && actual.getProject() != null) {
            softly.assertEquals(actual.getProject().getId(), expected.getProject().getId(), "BuildType.project.id is not correct");
            softly.assertEquals(actual.getProject().getName(), expected.getProject().getName(), "BuildType.project.name is not correct");
        }
    }
}
