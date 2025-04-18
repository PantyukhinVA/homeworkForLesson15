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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class YandexSearchTest {
    private WebDriver driver;

    @BeforeClass
    public static void setupAll() {
        WebDriverManager.chromedriver().driverVersion("126.0.6478.126").setup();
    }

    @BeforeMethod
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
    }

    @Test
    public void testYandexSearch() throws InterruptedException {
        // 1. Открыть Яндекс
        driver.get("https://ya.ru");

        // 2. Ввести в строке поиска «руддщцкщдв»
        WebElement searchInput = driver.findElement(By.xpath("//input[@aria-label='Запрос']"));
        searchInput.sendKeys("руддщцкщдв");

        // 3. Нажать на кнопку «Найти»
        WebElement searchButton = driver.findElement(By.xpath("//button[normalize-space()='Найти']"));
        searchButton.click();

        // 4. Проверить, что строка поиска заполнена значением "hello world"
        WebElement searchInputAfter = driver.findElement(By.xpath("//input[@aria-label='Запрос']"));
        String searchFieldValue = searchInputAfter.getAttribute("value");
        assertEquals(searchFieldValue, "hello world", "Search field doesn't contain 'hello world'");

        // И проверить, что название окна содержит "hello world"
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("hello world"), "Page title doesn't contain 'hello world'");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}