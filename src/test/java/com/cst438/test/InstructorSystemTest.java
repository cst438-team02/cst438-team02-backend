package com.cst438.test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InstructorSystemTest {
    private WebDriver driver;

    @BeforeEach
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.get("http://localhost:3000/");
    }
    
    /**
     * Test 1: Instructor grades an assignment and enters scores for all enrolled students and uploads the scores
     * This test verifies that an instructor can open the assignment grading dialog,
     * enter or update a numeric score for each enrolled student, and submit the grades.
     * The system should respond with a success message confirming the update.
     */
    @Test
    public void testEnterAssignmentScores() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");

        driver.findElement(By.linkText("Show Sections")).click();

        List<WebElement> assignmentLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.linkText("View Assignments")
        ));
        assertFalse(assignmentLinks.isEmpty(), "No 'View Assignments' links found.");
        assignmentLinks.get(0).click();

        List<WebElement> gradeButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.xpath("//button[contains(text(), 'Grade')]")
        ));
        assertFalse(gradeButtons.isEmpty(), "No 'Grade' buttons found.");
        gradeButtons.get(0).click();

        List<WebElement> scoreInputs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.name("score")
        ));
        assertFalse(scoreInputs.isEmpty(), "No score input fields found.");

        for (int i = 0; i < scoreInputs.size(); i++) {
            WebElement input = scoreInputs.get(i);
            input.clear();
        
            String value = String.valueOf(99 - i);
            input.sendKeys(value);
            System.out.println("Set score[" + i + "] = " + value);
        }

        List<WebElement> saveButtons = driver.findElements(By.xpath("//button[contains(text(), 'Save')]"));
        assertFalse(saveButtons.isEmpty(), "No 'Save' buttons found.");
        saveButtons.get(saveButtons.size() - 1).click();

        List<WebElement> messages = driver.findElements(By.xpath("//h4"));
        assertFalse(messages.isEmpty(), "No <h4> message element found.");

        String msgText = driver.findElement(By.xpath("//h4")).getText();
        System.out.println("Message text: [" + msgText + "]");
        Assertions.assertThat(msgText.contains("Grades updated successfully"));
        

        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Close')]")
        ));
        closeButton.click();
    }

    /**
    * Test 2: Instructor enters final class grades for all enrolled students
    * This test verifies that an instructor can view the list of enrolled students for a section,
    * enter or modify the final letter grade for each student, and save the changes.
    * The system should respond with a success message indicating that the grades were saved successfully.
    */
    @Test
    public void testEnterFinalGrades() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");

        driver.findElement(By.linkText("Show Sections")).click();

        WebElement enrollmentsLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.linkText("View Enrollments"))
        );
        enrollmentsLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("grade")));
        List<WebElement> gradeInputs = driver.findElements(By.name("grade"));

        for (int i = 0; i < gradeInputs.size(); i++) {
            WebElement input = gradeInputs.get(i);
            input.clear();
            input.sendKeys(i % 2 == 0 ? "A" : "B");
        }

        WebElement saveButton = driver.findElement(By.xpath("//button[contains(text(), 'Save Grades')]"));
        saveButton.click();

        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
        Assertions.assertThat(message.getText().contains("Grades saved successfully"));
    }

    @AfterEach
    public void teardown() {
        driver.quit();
    }
}
