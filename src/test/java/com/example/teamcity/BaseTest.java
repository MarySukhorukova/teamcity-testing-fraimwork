package com.example.teamcity;

import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.TestData;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.spec.Specifications;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Method;
import java.util.Arrays;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

public class BaseTest {
    protected SoftAssert softy;
    protected CheckedRequests superUserCheckRequests = new CheckedRequests(Specifications.superUserSpec());
    protected CheckedRequests UserCheckRequests;
    protected TestData testData;

    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        softy = new SoftAssert();
        testData = generate();
    }

    @AfterMethod(alwaysRun = true)
    public void afterTest(ITestResult result) {
        softy.assertAll();
        boolean isUiTest = Arrays.asList(result.getMethod().getGroups()).contains("UI");

        if (!isUiTest) {
            System.out.println("Cleaning up after API test...");
            TestDataStorage.getStorage().deleteCreatedEntities();
        } else {
            System.out.println("Skipping cleanup for UI test.");
        }
    }
}

