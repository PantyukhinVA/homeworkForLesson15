import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class YandexSearchTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String PROPERTIES_FILE = "application.properties";
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = YandexSearchTest.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Файл " + PROPERTIES_FILE + " не найден!");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации", e);
        }
    }

    @BeforeClass
    public static void setupAll() {
        WebDriverManager.chromedriver().browserVersion(properties.getProperty("chrome.version", "")).setup();
    }

    @BeforeMethod
    public void setup() {
        initializeProfileDirectory();
        ChromeOptions options = createChromeOptions();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(getTimeout()));
    }

    private void initializeProfileDirectory() {
        Path profileDir = Paths.get(System.getProperty("user.dir"), "target", "profile");
        try {
            if (!Files.exists(profileDir)) {
                Files.createDirectories(profileDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию профиля", e);
        }
    }

    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--no-sandbox", // Отключение sandbox для работы в Docker/CI
                "--disable-dev-shm-usage", // Решение проблем с ограниченными ресурсами
                "--remote-debugging-port=9222", // Важно для исправления ошибки DevToolsActivePort
                "--disable-gpu", // Отключение GPU (может помочь в CI)
                "--window-size=1920,1080",
                "--user-data-dir=" + System.getProperty("user.dir") + "/target/profile"
        );

        return options;
    }

    private long getTimeout() {
        return Long.parseLong(properties.getProperty("timeout", "10"));
    }

    @Test
    public void testYandexSearch() {
        System.out.println("Запуск теста поиска в Яндексе");

        openYandex();
        performSearch();
        verifySearchResults();
    }

    private void openYandex() {
        driver.get("https://ya.ru");
        waitForPotentialCaptcha();
    }

    private void performSearch() {
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@aria-label='Запрос']")));
        searchInput.sendKeys("руддщ цкщдв");

        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Найти']")));
        searchButton.click();

        waitForPotentialCaptcha();
    }

    private void waitForPotentialCaptcha() {
        try {
            Thread.sleep(getDelay() * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Прерывание ожидания капчи", e);
        }
    }

    private long getDelay() {
        return Long.parseLong(properties.getProperty("delay", "5"));
    }

    private void verifySearchResults() {
        WebElement searchInputAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@aria-label='Запрос']")));
        String searchFieldValue = searchInputAfter.getAttribute("value");
        assertEquals(searchFieldValue, "hello world", "Поле поиска должно содержать 'hello world'");

        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("hello world"), "Заголовок страницы должен содержать 'hello world'");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}