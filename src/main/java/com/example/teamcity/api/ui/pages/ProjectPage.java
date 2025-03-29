package com.example.teamcity.api.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class ProjectPage extends BasePage {
    private static final String PROJECT_URL = "/project/%s";

    public SelenideElement title = $("span[class*='ProjectPageHeader__title']");
    private static final SelenideElement configurationsTable = $("#configurations");

    public static ProjectPage open(String projectId) {
        return Selenide.open(PROJECT_URL.formatted(projectId), ProjectPage.class);
    }

    public static void openSettings(String projectId) {
        Selenide.open("/admin/editProject.html?projectId=" + projectId);
    }

    public static boolean isBuildTablePresent() {
        return configurationsTable.exists();
    }
}
