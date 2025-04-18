import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class YandexSearchTest {
    private WebDriver driver;
    private static final String PROPERTIES_FILE = "application.properties";
    private static final Properties properties = new Properties();

    static {
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
        WebDriverManager.chromedriver().driverVersion(properties.getProperty("chrome.version")).setup();
    }

    @BeforeMethod
    public void setup() {
        Path profileDir = Paths.get(System.getProperty("user.dir"), "target", "profile");
        try {
            if (!Files.exists(profileDir)) {
                Files.createDirectories(profileDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize profile directory", e);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-data-dir=" + System.getProperty("user.dir") + "/target/profile");

        driver = new ChromeDriver(options);
    }

    @Test
    public void testYandexSearch() throws InterruptedException {
        System.out.println("Testing Yandex Search");
        // 1. Открыть Яндекс
        driver.get("https://ya.ru");

        // Ждём (время для ручного ввода капчи, если появится)
        TimeUnit.SECONDS.sleep(Long.parseLong(properties.getProperty("delay")));

        // 2. Ввести в строке поиска «руддщ цкщдв»
        WebElement searchInput = driver.findElement(By.xpath("//input[@aria-label='Запрос']"));
        searchInput.sendKeys("руддщ цкщдв");

        // 3. Нажать на кнопку «Найти»
        WebElement searchButton = driver.findElement(By.xpath("//button[normalize-space()='Найти']"));
        searchButton.click();

        // Ждём (время для ручного ввода капчи, если появится)
        TimeUnit.SECONDS.sleep(Long.parseLong(properties.getProperty("delay")));

        // 4. Проверить, что строка поиска заполнена значением "hello world"
        WebElement searchInputAfter = driver.findElement(By.xpath("//input[@aria-label='Запрос']"));
        String searchFieldValue = searchInputAfter.getDomAttribute("value");
        assertEquals(searchFieldValue, "hello world", "Search field equals 'hello world'");

        // И проверить, что название окна содержит "hello world"
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("hello world"), "Page title contain 'hello world'");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}