package com.example.blackjack;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static com.codeborne.selenide.Selenide.*;

public class BlackjackTest {
    public static WebDriver driver1;
    public static WebDriver driver2;

    // set rigged deck
    public static void sendDeck(String deck) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost("http://localhost:3000");
            StringEntity params = new StringEntity(deck);
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(params);
            httpClient.execute(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @BeforeAll
    public static void setUpAll() {
        Configuration.browserSize = "1280x1024";
        SelenideLogger.addListener("allure", new AllureSelenide());
        System.setProperty("webdriver.chrome.driver", "src/test/chromedriver.exe");
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        driver1 = new ChromeDriver();
        driver2 = new ChromeDriver();
        driver1.get("http://localhost:3000/");
        driver2.get("http://localhost:3000/");
    }

    @AfterEach
    public void refresh() throws InterruptedException {
        driver1.navigate().refresh();
        Thread.sleep(100);
        driver2.navigate().refresh();
        Thread.sleep(100);
    }

    @AfterAll
    public static void close() {
        driver1.quit();
        driver2.quit();
    }

    @Test
    public void testPageDisplay() {
        assertEquals("Player 2",driver1.findElement(By.cssSelector("#otherPlayer")).getText());
        assertEquals("Player 1",driver2.findElement(By.cssSelector("#otherPlayer")).getText());
        assertTrue(driver1.findElement(By.id("deal")).isEnabled());
        assertTrue(driver2.findElement(By.id("deal")).isEnabled());
        assertFalse(driver1.findElement(By.id("hit")).isEnabled());
        assertFalse(driver2.findElement(By.id("hit")).isEnabled());
        assertFalse(driver1.findElement(By.id("stand")).isEnabled());
        assertFalse(driver2.findElement(By.id("stand")).isEnabled());
    }

    @Test
    public void testGameStart() {
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();

        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertTrue(driver1.findElement(By.id("hit")).isEnabled());
        assertFalse(driver2.findElement(By.id("hit")).isEnabled());
        assertTrue(driver1.findElement(By.id("stand")).isEnabled());
        assertFalse(driver2.findElement(By.id("stand")).isEnabled());
        assertEquals(2,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(1,driver1.findElements(By.cssSelector("#aiCards>.back")).size());
        assertEquals(2,driver1.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals(1,driver1.findElements(By.cssSelector("#dealerCards>.back")).size());
        assertEquals(2,driver1.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals(1,driver1.findElements(By.cssSelector("#otherCards>.back")).size());
        assertEquals(2,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals(0,driver1.findElements(By.cssSelector("#playerCards>.back")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(1,driver2.findElements(By.cssSelector("#aiCards>.back")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals(1,driver2.findElements(By.cssSelector("#dealerCards>.back")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals(1,driver2.findElements(By.cssSelector("#otherCards>.back")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals(0,driver2.findElements(By.cssSelector("#playerCards>.back")).size());
    }

    @Test
    public void testScore() {
        sendDeck("2,3,4,5,6,7,8,9");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();

        assertEquals("Score: 10",driver1.findElement(By.cssSelector("#dealerScore")).getText());
        assertEquals("Score: 9",driver1.findElement(By.cssSelector("#aiScore")).getText());
        assertEquals("Score: 8",driver1.findElement(By.cssSelector("#otherScore")).getText());
        assertEquals("Score: 10",driver1.findElement(By.cssSelector("#playerScore")).getText());
        assertEquals("Score: 10",driver2.findElement(By.cssSelector("#dealerScore")).getText());
        assertEquals("Score: 9",driver2.findElement(By.cssSelector("#aiScore")).getText());
        assertEquals("Score: 7",driver2.findElement(By.cssSelector("#otherScore")).getText());
        assertEquals("Score: 12",driver2.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreCards() {
        sendDeck("1,2,3,4,5,6,7,8,9");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals("Score: 18",driver1.findElement(By.cssSelector("#playerScore")).getText());
        assertEquals("Score: 16",driver2.findElement(By.cssSelector("#otherScore")).getText());
    }

    @Test
    public void testScoreJQK() {
        sendDeck("2,23,3,4,24,10,11,12");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();

        assertEquals("Score: 10",driver1.findElement(By.cssSelector("#dealerScore")).getText());
        assertEquals("Score: 10",driver1.findElement(By.cssSelector("#aiScore")).getText());
        assertEquals("Score: 10",driver1.findElement(By.cssSelector("#otherScore")).getText());
        assertEquals("Score: 13",driver1.findElement(By.cssSelector("#playerScore")).getText());
        assertEquals("Score: 20",driver2.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreSingleAce() {
        sendDeck("2,3,4,5,6,7,8,0");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();

        assertEquals("Score: 11",driver1.findElement(By.cssSelector("#dealerScore")).getText());
    }

    @Test
    public void testScoreDoubleAce() {
        sendDeck("0,1,2,3,13,4,5,6");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();

        assertEquals("Score: 12",driver1.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreTripleAce() {
        sendDeck("0,1,2,3,13,4,5,6,26");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals("Score: 13",driver1.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreCardsWithSingleAce1() {
        sendDeck("0,1,2,3,8,4,5,6");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();

        assertEquals("Score: 20",driver1.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreCardsWithSingleAce2() {
        sendDeck("0,1,2,3,12,4,5,6");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();

        assertEquals("Score: 21",driver1.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreCardsWithSingleAce3() {
        sendDeck("0,1,2,3,4,5,6,7,14");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals("Score: 18",driver1.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreCardsWithSingleAce4() {
        sendDeck("0,1,2,3,8,5,6,7,9");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals("Score: 20",driver1.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreCardsWithDoubleAce1() {
        sendDeck("0,1,2,3,13,5,6,8,7");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals("Score: 20",driver1.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testScoreCardsWithDoubleAce2() {
        sendDeck("0,1,2,3,13,5,6,8,9");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals("Score: 12",driver1.findElement(By.cssSelector("#playerScore")).getText());
    }

    @Test
    public void testPlayer1Hit() {
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        assertEquals(3,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#otherCards>.card")).size());
    }

    @Test
    public void testPlayer1HitTwice() {
        sendDeck("3,7,8,9,1,10,11,12,2");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton1 = new WebDriverWait(driver1, 10)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton1.click();
        WebElement hitButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton2.click();
        hitButton1.click();
        assertEquals(4,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals(4,driver2.findElements(By.cssSelector("#otherCards>.card")).size());
    }

    @Test
    public void testPlayer1Bust() {
        sendDeck("10,1,2,3,22,5,6,8,9");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertTrue(Integer.parseInt(driver1.findElement(By.cssSelector("#playerScore")).getText().replace("Score: ", "")) > 21);
        assertEquals("Bust", driver1.findElement(By.id("playerStatus")).getText());
        assertEquals("Bust", driver2.findElement(By.id("otherStatus")).getText());
        assertEquals(0,driver2.findElements(By.cssSelector("#otherCards>.back")).size());
    }

    @Test
    public void testPlayer1Stand() {
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        assertEquals(2,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals("Stand", driver1.findElement(By.id("playerStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("otherStatus")).getText());
    }

    @Test
    public void testPlayer1HitAndStand() {
        sendDeck("3,7,8,9,1,10,11,12,2");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton1.click();
        WebElement hitButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton2.click();
        WebElement standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals("Stand", driver1.findElement(By.id("playerStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("otherStatus")).getText());
    }

    @Test
    public void testPlayer2Hit() {
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#playerCards>.card")).size());
    }

    @Test
    public void testPlayer2HitTwice() {
        sendDeck("7,3,8,9,1,0,11,12,2");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        hitButton.click();
        assertEquals(4,driver1.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals(4,driver2.findElements(By.cssSelector("#playerCards>.card")).size());
    }

    @Test
    public void testPlayer2Bust() {
        sendDeck("1,10,2,3,5,22,6,8,9");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertTrue(Integer.parseInt(driver2.findElement(By.cssSelector("#playerScore")).getText().replace("Score: ", "")) > 21);
        assertEquals("Bust", driver1.findElement(By.id("otherStatus")).getText());
        assertEquals("Bust", driver2.findElement(By.id("playerStatus")).getText());
        assertEquals(0,driver1.findElements(By.cssSelector("#otherCards>.back")).size());
    }

    @Test
    public void testPlayer2Stand() {
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        WebElement standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals(2,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals("Stand", driver1.findElement(By.id("otherStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("playerStatus")).getText());
    }

    @Test
    public void testPlayer2HitAndStand() {
        sendDeck("3,7,8,9,1,14,11,12,2,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();
        assertEquals(3,driver1.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals("Stand", driver1.findElement(By.id("otherStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("playerStatus")).getText());
    }

    @Test
    public void testSequenceRound1Player2() {
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertFalse(driver1.findElement(By.id("hit")).isEnabled());
        assertTrue(driver2.findElement(By.id("hit")).isEnabled());
        assertFalse(driver1.findElement(By.id("stand")).isEnabled());
        assertTrue(driver2.findElement(By.id("stand")).isEnabled());
    }

    @Test
    public void testSequenceRound1AI() {
        sendDeck("13,14,1,2,3,4,5,6");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton1.click();
        assertEquals(2,driver1.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals(2,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        WebElement hitButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton2.click();
        assertEquals(3,driver1.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals(3,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
    }

    @Test
    public void testSequenceRound2Player1() {
        sendDeck("13,14,1,2,3,4,5,6");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton1.click();
        WebElement hitButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton2.click();
        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertTrue(driver1.findElement(By.id("hit")).isEnabled());
        assertFalse(driver2.findElement(By.id("hit")).isEnabled());
        assertTrue(driver1.findElement(By.id("stand")).isEnabled());
        assertFalse(driver2.findElement(By.id("stand")).isEnabled());
    }

    @Test
    public void testSequenceRound2Player2() {
        sendDeck("13,14,1,2,3,4,5,6");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton1.click();
        WebElement hitButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton2.click();
        hitButton1.click();
        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertFalse(driver1.findElement(By.id("hit")).isEnabled());
        assertTrue(driver2.findElement(By.id("hit")).isEnabled());
        assertFalse(driver1.findElement(By.id("stand")).isEnabled());
        assertTrue(driver2.findElement(By.id("stand")).isEnabled());
    }

    @Test
    public void testSequencePlayer1Stand() {
        sendDeck("13,14,1,2,3,4,5,6");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertFalse(driver1.findElement(By.id("hit")).isEnabled());
        assertTrue(driver2.findElement(By.id("hit")).isEnabled());
        assertFalse(driver1.findElement(By.id("stand")).isEnabled());
        assertTrue(driver2.findElement(By.id("stand")).isEnabled());
        assertEquals("Stand", driver1.findElement(By.id("playerStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("otherStatus")).getText());
    }

    @Test
    public void testSequencePlayer2Stand() {
        sendDeck("13,14,1,2,0,4,5,6");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        hitButton.click();
        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertTrue(driver1.findElement(By.id("hit")).isEnabled());
        assertFalse(driver2.findElement(By.id("hit")).isEnabled());
        assertTrue(driver1.findElement(By.id("stand")).isEnabled());
        assertFalse(driver2.findElement(By.id("stand")).isEnabled());
        assertEquals("Stand", driver1.findElement(By.id("otherStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("playerStatus")).getText());
    }

    @Test
    public void testSequencePlayer1Bust() {
        sendDeck("10,14,1,2,12,4,5,6,11");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton1.click();
        WebElement hitButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton2.click();
        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertFalse(driver1.findElement(By.id("hit")).isEnabled());
        assertTrue(driver2.findElement(By.id("hit")).isEnabled());
        assertFalse(driver1.findElement(By.id("stand")).isEnabled());
        assertTrue(driver2.findElement(By.id("stand")).isEnabled());
        assertEquals("Bust", driver1.findElement(By.id("playerStatus")).getText());
        assertEquals("Bust", driver2.findElement(By.id("otherStatus")).getText());
    }

    @Test
    public void testSequencePlayer2Bust() {
        sendDeck("13,10,1,2,0,11,5,6,3,12");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton1.click();
        WebElement hitButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton2.click();
        hitButton1.click();
        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertTrue(driver1.findElement(By.id("hit")).isEnabled());
        assertFalse(driver2.findElement(By.id("hit")).isEnabled());
        assertTrue(driver1.findElement(By.id("stand")).isEnabled());
        assertFalse(driver2.findElement(By.id("stand")).isEnabled());
        assertEquals("Bust", driver1.findElement(By.id("otherStatus")).getText());
        assertEquals("Bust", driver2.findElement(By.id("playerStatus")).getText());
    }

    @Test
    public void testGameEnd() {
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertTrue(driver1.findElement(By.id("deal")).isEnabled());
        assertTrue(driver2.findElement(By.id("deal")).isEnabled());
        assertFalse(driver1.findElement(By.id("hit")).isEnabled());
        assertFalse(driver2.findElement(By.id("hit")).isEnabled());
        assertFalse(driver1.findElement(By.id("stand")).isEnabled());
        assertFalse(driver2.findElement(By.id("stand")).isEnabled());
        assertEquals(0,driver1.findElements(By.cssSelector("#dealerCards>.back")).size());
        assertEquals(0,driver1.findElements(By.cssSelector("#aiCards>.back")).size());
        assertEquals(0,driver1.findElements(By.cssSelector("#otherCards>.back")).size());
        assertEquals(0,driver2.findElements(By.cssSelector("#otherCards>.back")).size());
    }

    @Test
    public void testGameRestart() {
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();

        assertFalse(driver1.findElement(By.id("deal")).isEnabled());
        assertFalse(driver2.findElement(By.id("deal")).isEnabled());
        assertTrue(driver1.findElement(By.id("hit")).isEnabled());
        assertTrue(driver1.findElement(By.id("stand")).isEnabled());
        assertEquals(2,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(2,driver1.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals(2,driver1.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals(2,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals(2,driver2.findElements(By.cssSelector("#playerCards>.card")).size());
    }

    @Test
    public void testAIPlayerStandFor21() {
        sendDeck("13,7,10,2,1,11,0,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement  hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals("Stand", driver1.findElement(By.id("aiStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("aiStatus")).getText());
    }

    @Test
    public void testAIPlayerPlayer1Has10() {
        sendDeck("13,5,7,2,11,1,0,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement  standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
    }

    @Test
    public void testAIPlayerPlayer1HasAce() {
        sendDeck("13,5,7,2,0,1,11,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement  standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
    }

    @Test
    public void testAIPlayerPlayer2Has10() {
        sendDeck("13,5,7,2,1,10,0,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement  hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
    }

    @Test
    public void testAIPlayerPlayer2HasAce() {
        sendDeck("13,5,7,2,1,0,10,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement  hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
    }

    @Test
    public void testAIPlayerAssumePlayer1More() {
        sendDeck("13,5,7,2,0,11,10,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement  standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
    }

    @Test
    public void testAIPlayerAssumePlayer1Same() {
        sendDeck("13,5,11,2,8,1,21,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement  standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals("Stand", driver1.findElement(By.id("aiStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("aiStatus")).getText());
    }

    @Test
    public void testAIPlayerAssumePlayer2More() {
        sendDeck("13,5,7,2,11,8,10,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
    }

    @Test
    public void testAIPlayerAssumePlayer2Same() {
        sendDeck("13,5,11,2,1,8,21,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals("Stand", driver1.findElement(By.id("aiStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("aiStatus")).getText());
    }

    @Test
    public void testAIPlayerAssumeBothLess() {
        sendDeck("13,5,11,2,7,1,12,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement  standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();

        assertEquals("Stand", driver1.findElement(By.id("aiStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("aiStatus")).getText());
    }

    @Test
    public void testAIPlayerLessThan18() {
        sendDeck("13,5,7,2,15,8,1,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#aiCards>.card")).size());
    }

    @Test
    public void testAIPlayerBust() {
        sendDeck("13,7,5,2,15,8,9,6,1,11");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals("Bust", driver1.findElement(By.id("aiStatus")).getText());
        assertEquals("Bust", driver2.findElement(By.id("aiStatus")).getText());
    }

    @Test
    public void testDealerLessThan17() {
        sendDeck("13,5,7,2,15,8,1,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#dealerCards>.card")).size());
    }

    @Test
    public void testDealer17WithoutAce() {
        sendDeck("13,5,7,11,15,8,1,6,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals("Stand", driver1.findElement(By.id("dealerStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("dealerStatus")).getText());
    }

    @Test
    public void testDealer17WithAce() {
        sendDeck("13,11,7,5,15,8,1,0,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals(3,driver1.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals(3,driver2.findElements(By.cssSelector("#dealerCards>.card")).size());
    }

    @Test
    public void testDealerMoreThan17() {
        sendDeck("13,11,7,8,15,5,1,0,3,4");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals("Stand", driver1.findElement(By.id("dealerStatus")).getText());
        assertEquals("Stand", driver2.findElement(By.id("dealerStatus")).getText());
    }

    @Test
    public void testDealerBust() {
        sendDeck("13,7,2,5,15,8,6,9,1,11,10");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();

        assertEquals("Bust", driver1.findElement(By.id("dealerStatus")).getText());
        assertEquals("Bust", driver2.findElement(By.id("dealerStatus")).getText());
    }

    @Test
    public void testAllBust() {
        sendDeck("22,7,23,5,24,8,25,9,12,11,10,36");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton1.click();
        WebElement  hitButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton2.click();

        assertEquals("Bust", driver1.findElement(By.id("dealerStatus")).getText());
        assertEquals("Bust", driver1.findElement(By.id("aiStatus")).getText());
        assertEquals("Bust", driver1.findElement(By.id("otherStatus")).getText());
        assertEquals("Bust", driver1.findElement(By.id("playerStatus")).getText());

        assertEquals("All Bust", driver1.findElement(By.id("result")).getText());
        assertEquals("All Bust", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void testPlayer1Win() {
        sendDeck("0,7,23,5,24,8,25,9,12,11");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals("Winner Player 1", driver1.findElement(By.id("result")).getText());
        assertEquals("Winner Player 1", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void testPlayer2Win() {
        sendDeck("7,0,23,5,8,24,25,9,12,11");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals("Winner Player 2", driver1.findElement(By.id("result")).getText());
        assertEquals("Winner Player 2", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void testAIPlayerWin() {
        sendDeck("23,7,0,5,8,24,25,9,12,11");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals("Winner AI Player", driver1.findElement(By.id("result")).getText());
        assertEquals("Winner AI Player", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void testDealerWin() {
        sendDeck("23,7,5,0,8,24,25,9,12,11");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals("Winner Dealer", driver1.findElement(By.id("result")).getText());
        assertEquals("Winner Dealer", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void testTwoPlayersWin() {
        sendDeck("0,13,5,23,22,24,25,9,12,11");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals("Winner Player 1 Player 2", driver1.findElement(By.id("result")).getText());
        assertEquals("Winner Player 1 Player 2", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void testThreePlayersWin() {
        sendDeck("0,13,26,23,9,10,11,12,13");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals("Winner Player 1 Player 2 AI Player", driver1.findElement(By.id("result")).getText());
        assertEquals("Winner Player 1 Player 2 AI Player", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void testFourPlayersWin() {
        sendDeck("0,13,26,39,9,10,11,12,13");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals("Winner Player 1 Player 2 AI Player Dealer", driver1.findElement(By.id("result")).getText());
        assertEquals("Winner Player 1 Player 2 AI Player Dealer", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void test7CardsCharlieBust() {
        sendDeck("0,4,9,10,1,2,11,12,13,14,27,40,16,23");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        hitButton.click();
        hitButton.click();
        hitButton.click();
        hitButton.click();

        assertEquals(7,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals("Bust", driver1.findElement(By.id("playerStatus")).getText());
        assertNotEquals("Winner Player 1", driver1.findElement(By.id("result")).getText());
        assertNotEquals("Winner Player 1", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void test7CardsCharliePlayer1() {
        sendDeck("0,4,9,10,1,2,11,12,13,14,27,40,16,26");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement hitButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        WebElement  standButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        hitButton.click();
        hitButton.click();
        hitButton.click();
        hitButton.click();

        assertEquals(7,driver1.findElements(By.cssSelector("#playerCards>.card")).size());
        assertEquals("7-card Charlie", driver1.findElement(By.id("playerStatus")).getText());
        assertNotEquals("Winner Player 1", driver1.findElement(By.id("result")).getText());
        assertNotEquals("Winner Player 1", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void test7CardsCharliePlayer2() {
        sendDeck("4,0,9,10,2,1,11,12,13,14,27,40,16,26");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton.click();
        WebElement  hitButton = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("hit")));
        hitButton.click();
        hitButton.click();
        hitButton.click();
        hitButton.click();
        hitButton.click();

        assertEquals(7,driver1.findElements(By.cssSelector("#otherCards>.card")).size());
        assertEquals("7-card Charlie", driver1.findElement(By.id("otherStatus")).getText());
        assertNotEquals("Winner Player 2", driver1.findElement(By.id("result")).getText());
        assertNotEquals("Winner Player 2", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void test7CardsCharlieAIPlayer() {
        sendDeck("4,9,2,10,12,11,15,13,16,28,27,40,16,26");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals(7,driver1.findElements(By.cssSelector("#aiCards>.card")).size());
        assertEquals("7-card Charlie", driver1.findElement(By.id("aiStatus")).getText());
        assertNotEquals("Winner AI Player", driver1.findElement(By.id("result")).getText());
        assertNotEquals("Winner AI Player", driver2.findElement(By.id("result")).getText());
    }

    @Test
    public void test7CardsCharlieDealer() {
        sendDeck("4,9,10,2,12,11,13,15,1,14,27,40,0,13");
        driver1.findElement(By.id("deal")).click();
        driver2.findElement(By.id("deal")).click();
        WebElement standButton1 = new WebDriverWait(driver1, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton1.click();
        WebElement  standButton2 = new WebDriverWait(driver2, 5)
                .until(ExpectedConditions.elementToBeClickable(By.id("stand")));
        standButton2.click();

        assertEquals(7,driver1.findElements(By.cssSelector("#dealerCards>.card")).size());
        assertEquals("7-card Charlie", driver1.findElement(By.id("dealerStatus")).getText());
        assertNotEquals("Winner Dealer", driver1.findElement(By.id("result")).getText());
        assertNotEquals("Winner Dealer", driver2.findElement(By.id("result")).getText());
    }
}
