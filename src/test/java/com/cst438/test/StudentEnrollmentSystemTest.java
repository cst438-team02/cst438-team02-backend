package com.cst438.test;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * System test for student enrollment functionality
 * Tests the scenario where a student navigates from the home page to enroll in a section,
 * selects a section, and verifies it was added to their schedule.
 * 
 * SETUP INSTRUCTIONS:
 * 1. Download ChromeDriver from https://chromedriver.chromium.org/downloads
 *    - Make sure to download the version that matches your Chrome browser version
 * 2. Extract the chromedriver.exe (Windows) or chromedriver (Mac/Linux) file
 * 3. Update the CHROME_DRIVER_FILE_LOCATION constant below with the path to your ChromeDriver
 * 4. Make sure the frontend application is running at the URL specified in the URL constant
 * 5. Update the element IDs in the ELEMENT_IDS section to match your frontend implementation
 */
public class StudentEnrollmentSystemTest {

    // ======== CONFIGURATION SECTION - UPDATE THESE VALUES ========
    
    /**
     * Path to the ChromeDriver executable
     * For Windows: Use format like "C:/path/to/chromedriver.exe"
     * For Mac/Linux: Use format like "/path/to/chromedriver"
     */
    public static final String CHROME_DRIVER_FILE_LOCATION =
            "C:/chromedriver-win32/chromedriver.exe";

    /**
     * URL of the frontend application
     */
    public static final String URL = "http://localhost:3000";
    
    /**
     * Delay between actions in milliseconds
     */
    public static final int SLEEP_DURATION = 1000; // 1 second

    // ======== TEST DATA SECTION ========
    
    /**
     * Student ID to use for the test
     */
    private static final int STUDENT_ID = 3; // Thomas Edison
    
    /**
     * Year to use for schedule viewing
     */
    private static final String YEAR = "2024";
    
    /**
     * Semester to use for schedule viewing
     */
    private static final String SEMESTER = "Spring";

    // ======== ELEMENT IDS - UPDATE TO MATCH YOUR FRONTEND ========
    
    /**
     * Element IDs used in the test
     * Update these to match the actual IDs in your frontend implementation
     */
    private static class ELEMENT_IDS {
        // Navigation elements
        static final String STUDENT_LINK = "student";
        static final String STUDENT_ID_INPUT = "studentId";
        static final String LOGIN_BUTTON = "login";
        static final String ENROLL_SECTIONS_LINK = "enrollSections";
        static final String VIEW_SCHEDULE_LINK = "viewSchedule";
        
        // Section listing elements
        static final String SECTION_TABLE = "sectionTable";
        
        // Enrollment confirmation elements
        static final String CONFIRM_DIALOG_XPATH = "//div[contains(@class, 'confirm-dialog')]//button[text()='Yes']";
        static final String ENROLLMENT_MESSAGE = "enrollmentMessage";
        
        // Schedule viewing elements
        static final String SCHEDULE_YEAR_INPUT = "scheduleYear";
        static final String SCHEDULE_SEMESTER_INPUT = "scheduleSemester";
        static final String VIEW_BUTTON = "viewButton";
        static final String SCHEDULE_TABLE = "scheduleTable";
    }

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUpDriver() throws Exception {
        // Set properties required by Chrome Driver
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // Start the driver
        driver = new ChromeDriver(ops);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Navigate to the application
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void testStudentEnrollsInSection() throws Exception {
        // Step 1: Navigate to the student dashboard
        WebElement studentLink = driver.findElement(By.id(ELEMENT_IDS.STUDENT_LINK));
        studentLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Step 2: Enter student ID and click login
        driver.findElement(By.id(ELEMENT_IDS.STUDENT_ID_INPUT)).sendKeys(String.valueOf(STUDENT_ID));
        driver.findElement(By.id(ELEMENT_IDS.LOGIN_BUTTON)).click();
        Thread.sleep(SLEEP_DURATION);

        // Step 3: Navigate to the "Enroll in Sections" page
        WebElement enrollLink = driver.findElement(By.id(ELEMENT_IDS.ENROLL_SECTIONS_LINK));
        enrollLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Step 4: The page should display a list of open sections
        // Find a section to enroll in (first section in the list)
        WebElement sectionTable = driver.findElement(By.id(ELEMENT_IDS.SECTION_TABLE));
        List<WebElement> rows = sectionTable.findElements(By.tagName("tr"));
        
        // Skip header row
        assertTrue(rows.size() > 1, "No sections available for enrollment");
        
        // Get the first section's details for verification later
        WebElement firstSectionRow = rows.get(1);
        List<WebElement> cells = firstSectionRow.findElements(By.tagName("td"));
        
        String courseId = cells.get(0).getText();
        String sectionId = cells.get(1).getText();
        String courseTitle = cells.get(2).getText();
        
        // Click the "Enroll" button for the first section
        WebElement enrollButton = firstSectionRow.findElement(By.tagName("button"));
        enrollButton.click();
        Thread.sleep(SLEEP_DURATION);
        
        // Step 5: Confirm enrollment in the popup
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath(ELEMENT_IDS.CONFIRM_DIALOG_XPATH)));
        confirmButton.click();
        Thread.sleep(SLEEP_DURATION);
        
        // Step 6: Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id(ELEMENT_IDS.ENROLLMENT_MESSAGE)));
        assertTrue(successMessage.getText().contains("Successfully enrolled"), 
                "Expected success message not found");
        
        // Step 7: Navigate to view schedule page
        WebElement viewScheduleLink = driver.findElement(By.id(ELEMENT_IDS.VIEW_SCHEDULE_LINK));
        viewScheduleLink.click();
        Thread.sleep(SLEEP_DURATION);
        
        // Step 8: Enter year and semester to view schedule
        driver.findElement(By.id(ELEMENT_IDS.SCHEDULE_YEAR_INPUT)).sendKeys(YEAR);
        driver.findElement(By.id(ELEMENT_IDS.SCHEDULE_SEMESTER_INPUT)).sendKeys(SEMESTER);
        driver.findElement(By.id(ELEMENT_IDS.VIEW_BUTTON)).click();
        Thread.sleep(SLEEP_DURATION);
        
        // Step 9: Verify the newly enrolled section appears in the schedule
        WebElement scheduleTable = driver.findElement(By.id(ELEMENT_IDS.SCHEDULE_TABLE));
        List<WebElement> scheduleRows = scheduleTable.findElements(By.tagName("tr"));
        
        boolean foundEnrolledSection = false;
        for (int i = 1; i < scheduleRows.size(); i++) { // Skip header row
            List<WebElement> scheduleCells = scheduleRows.get(i).findElements(By.tagName("td"));
            if (scheduleCells.get(0).getText().equals(courseId) && 
                scheduleCells.get(1).getText().equals(sectionId) &&
                scheduleCells.get(2).getText().equals(courseTitle)) {
                foundEnrolledSection = true;
                break;
            }
        }
        
        assertTrue(foundEnrolledSection, "Newly enrolled section not found in student schedule");
    }
}