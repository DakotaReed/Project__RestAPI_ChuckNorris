import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.testng.Assert.assertEquals;

@Listeners(ListenersAuto.class)
public class ChuckNorris {

    String url = "https://api.chucknorris.io";

    public RequestSpecification httpRequest;
    public Response response;
    private JsonPath jp;
    private List<String[]> listArrUrlValueJokesCSV;
    private String[] rowUrlValue;
    static WebDriver driver;

    @BeforeMethod
    public void start() {
        getURL(url);
    }

    @Test
    public void test01_printAllCategories() {
        response = httpRequest.get(url+"/jokes/categories");
        List<String> categories = Arrays.asList(response.getBody().asString().substring(2, response.getBody().asString().length() - 2).split("\",\""));
        for (String category : categories)
            System.out.println(category);
        assertEquals(response.getStatusCode(), 200);
    }
    @Test
    public void test02_verifyCharlieOrBarak() {
        response = httpRequest.get(url+"/jokes/search?query=charlie sheen");
        jp = response.jsonPath();
        int totalCharlie = Integer.parseInt(jp.get("total").toString());
        System.out.println("Charlie Sheen: "+jp.get("total").toString());

        getURL(url);
        response = httpRequest.get(url+"/jokes/search?query=barack obama");
        jp = response.jsonPath();
        int totalBarack = Integer.parseInt(jp.get("total").toString());
        System.out.println("Barack Obama: "+jp.get("total").toString());

        if (totalCharlie > totalBarack)
            System.out.println("More Jokes About Charlie Sheen");
        else
            System.out.println("More Jokes About Barack Obama");

        assertEquals(response.getStatusCode(), 200);
    }
    @Test
    public void test03_getTenJokesInCSV() throws IOException {
        listArrUrlValueJokesCSV = new ArrayList<>();
        for (int i=0; i<10; i++) {
            getURL(url);
            response = httpRequest.get(url+"/jokes/random");
            jp = response.jsonPath();
            rowUrlValue = new String[]{jp.get("url").toString(), jp.get("value").toString()};
            System.out.println(jp.get("url").toString()+" .... "+jp.get("value").toString());
            listArrUrlValueJokesCSV.add(rowUrlValue);
            System.out.println(listArrUrlValueJokesCSV.size());
        }
        writeToCsvRandomJokes(listArrUrlValueJokesCSV);

        assertEquals(response.getStatusCode(), 200);
    }
    @Test
    public void test04_createMovieJokeCSV() throws IOException {
        listArrUrlValueJokesCSV = new ArrayList<>();
        getURL(url);
        response = httpRequest.get(url+"/jokes/random?category=movie");
        jp = response.jsonPath();
        rowUrlValue = new String[]{jp.get("url").toString(), jp.get("value").toString()};
        System.out.println(jp.get("url").toString()+" .... "+jp.get("value").toString());
        listArrUrlValueJokesCSV.add(rowUrlValue);
        System.out.println(listArrUrlValueJokesCSV.size());
        writeToCsvRandomMovieJokes(listArrUrlValueJokesCSV);

        assertEquals(response.getStatusCode(), 200);
    }
    @Test(dataProvider = "data-provider-UrlValueJoke", dataProviderClass = ChuckNorris.class)
    public void test05_verifyJokeServerWeb(String url, String expectedJoke){
        char[] arrLettersExp = expectedJoke.toCharArray();
        for (char letter : arrLettersExp) {
            if (String.valueOf(letter).equals("\""))
                expectedJoke = expectedJoke.replace("\"", "");
        }

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(8, TimeUnit.SECONDS);
        driver.get(url);

        String jokeWeb = driver.findElement(By.id("joke_value")).getText();
        char[] arrLettersAct = jokeWeb.toCharArray();
        for (char letter : arrLettersAct) {
            if (String.valueOf(letter).equals("\""))
                jokeWeb = jokeWeb.replace("\"", "");
        }
        driver.quit();
        System.out.println("Joke Displayed on Web is:    "+jokeWeb);
        System.out.println("Expected Joke is:    "+expectedJoke);

        assertEquals(jokeWeb, expectedJoke);
    }

    @DataProvider(name = "data-provider-UrlValueJoke")
    public Object[][] getDataObjectURL() {
        return getDataFromCSV("E:\\Automation\\Project__RestAPI_ChuckNorris\\CSVFiles\\RandomMovieJokes.csv");
    }
    public static Object[][] getDataFromCSV(String filePath) {
        Object[][] data = new Object[1][2];
        List<String> csvData = readCSV(filePath);
        for (int i=0; i<2; i++) {
            data[0][i] = csvData.get(0).split(",")[i];
        }
        return data;
    }
    public static List<String> readCSV(String csvFile) {
        List<String> lines = null;
        File file = new File(csvFile);
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private void getURL(String url) {
        RestAssured.baseURI = url;
        httpRequest = RestAssured.given();
        httpRequest.header("Content-Type", "application/json");
    }
    
    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }
    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
    public void writeToCsvRandomJokes(List<String[]> dataLines) throws IOException {
        File csvOutputFile = new File("E:\\Automation\\Project__RestAPI_ChuckNorris\\CSVFiles\\RandomJokes.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
    }
    public void writeToCsvRandomMovieJokes(List<String[]> dataLines) throws IOException {
        File csvOutputFile = new File("E:\\Automation\\Project__RestAPI_ChuckNorris\\CSVFiles\\RandomMovieJokes.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
    }

}