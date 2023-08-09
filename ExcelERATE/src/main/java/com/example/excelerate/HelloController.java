package com.example.excelerate;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

public class HelloController implements Initializable {


    @FXML
    private TextField dealerToFind_input;

    @FXML
    private TextField otp_input;

    @FXML
    private TextField password_input;

    @FXML
    private TextFlow textflow_log;

    @FXML
    private TextField username_input;


    @FXML
    private Label welcomeText;

    private String dealerToFind = "Andu";
    private static final String NAME = "H2";
    private String username = "tranhov";
    private String password = "Sanphamage19!";
    private String otp = "731533";
    List<String> accountType;
    List<String> accountUCID;
    List<WebElement> accountLink;
    List<Integer> softDeletedAccountIndex = new ArrayList<>();


    @FXML
    protected void onHelloButtonClick() throws IOException {
        FileWriter fileWriterSD = new FileWriter(NAME+"-sd.txt");
        PrintStream o = new PrintStream(new File(NAME) + "-log.txt");
        System.setOut(o);
        accountType = new ArrayList<>();
        accountUCID = new ArrayList<>();
        accountLink = new ArrayList<>();

        welcomeText.setText("About to draw information from website");
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--incognito");
        options.addArguments("--disable-site-isolation-trials");

        WebDriver webDriver = new ChromeDriver(options);
        webDriver.manage().window().maximize();

        webDriver.manage().deleteAllCookies();

        webDriver.manage().timeouts().pageLoadTimeout(50, TimeUnit.SECONDS);
        webDriver.manage().timeouts().implicitlyWait(40, TimeUnit.SECONDS);


        String numberToSearch = "+84983263911";

        webDriver.get("https://mercedes-benz-asia.my.site.com/DealerFM/s/");

        webDriver.findElement(By.xpath("//input[@id=\"userid\"]")).sendKeys(username);
        webDriver.findElement(By.xpath("//button[@id=\"next-btn\"]")).click();
        webDriver.findElement(By.xpath("//input[@id=\"password\"]")).sendKeys(password);
        webDriver.findElement(By.xpath("//button[@id=\"loginSubmitButton\"]")).click();
        webDriver.findElement(By.xpath("//input[@id=\"otp\"]")).sendKeys(otp);
        webDriver.findElement(By.xpath("//input[@type=\"submit\" and not(@disabled)]")).click();

        try (BufferedReader  bufferedReader = new BufferedReader(new FileReader(NAME+".txt"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                new Actions(webDriver).sendKeys(Keys.CONTROL).keyUp(Keys.CONTROL).build().perform();
                accountLink.clear();
                accountUCID.clear();
                accountType.clear();
                softDeletedAccountIndex.clear();

                WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@aria-label, \"Search\")]")));
                System.out.println(line);
                webDriver.findElement(By.xpath("//button[contains(@aria-label, \"Search\")]")).click();
                webDriver.findElement(By.xpath("//input[@placeholder=\"Search...\"]")).clear();
                webDriver.findElement(By.xpath("//input[@placeholder=\"Search...\"]")).sendKeys(line);
                webDriver.findElement(By.xpath("//input[@placeholder=\"Search...\"]")).click();
                webDriver.findElement(By.xpath("//input[@placeholder=\"Search...\"]")).sendKeys(Keys.ENTER);

                try {
                    WebDriverWait waitSecond = new WebDriverWait(webDriver, Duration.ofSeconds(10));
                    waitSecond.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@data-aura-class=\"uiVirtualDataTable\"]//tbody//tr/descendant::th//span//span[@data-aura-class]")));
                } catch (Exception e) {
                    System.out.println("Got error here, can't find the phone number: " + line);
                    continue;
                }
                List<WebElement> listAccountType = webDriver.findElements(By.xpath("//div[1][@class=\"resultsItem slds-col slds-no-flex slds-m-bottom_small\"]//table[@data-aura-class=\"uiVirtualDataTable\"]//tbody//tr/descendant::th//span//span[@data-aura-class]"));
                List<WebElement> listID = webDriver.findElements(By.xpath("//div[@class=\"resultsItem slds-col slds-no-flex slds-m-bottom_small\"][1]/div//div/div//div/div//div/div//div/div//table[@data-aura-class=\"uiVirtualDataTable\"]//tbody//tr/descendant::td//span//span[@data-aura-class and string-length(@title) = 19 and not(contains(title, \" \"))]"));
                List<WebElement> listLink = webDriver.findElements(By.xpath("//div[@class=\"resultsItem slds-col slds-no-flex slds-m-bottom_small\"][1]/div//div/div//div/div//div/div//div/div//table[@data-aura-class=\"uiVirtualDataTable\"]//tbody//tr/descendant::td//span//a[@data-aura-class and string-length(@title) != 0]"));


                if (listAccountType.size() > 0) {
                    // IDEAL:
                    // 1. SOFT DELETED ACCOUNT HAS SAME ID WITH REAL ACCOUNT
                    // 2. REAL ACCOUNT HAS UNIQUE ID
                    boolean containSoftDeleted = false;
                    boolean multipleDifferentUCIDSoftDeleted = false;
                    int numberOfUCIDDuplicatesForPersonAccount = 0;
                    boolean hasPersonAccount = false;

                    for (int i = 0; i < listAccountType.size(); i++) {
                        System.out.println(listAccountType.get(i).getText() + " : " + listID.get(i).getText() + " : " + listLink.get(i).getAttribute("title"));
                    }
                    for (int i = 0; i < listAccountType.size(); i++) {
                        if (listAccountType.get(i).getText().equals("Person Account Soft Deleted")) {
                                for (int a = 0; a < listAccountType.size(); a++) {
                                    if (listAccountType.get(a).getText().equals("Person Account Soft Deleted") && a != i) {
                                        multipleDifferentUCIDSoftDeleted = true;
                                    }
                                    if (listID.get(a).getText().equals(listID.get(i).getText()) && listAccountType.get(a).getText().equals("Person Account") && a != i) {
                                        hasPersonAccount = true;
                                    }
                                }
                            System.out.println("Detected Soft Deleted Account");
                            containSoftDeleted = true;
                            softDeletedAccountIndex.add(i);
                        }

                        if (Character.isDigit(listID.get(i).getText().charAt(0))) {
                            accountType.add(listAccountType.get(i).getText());
                            accountUCID.add(listID.get(i).getText());
                            accountLink.add(listLink.get(i));
                        }
                    }

                    // Run 2D Arrays

                    if (!containSoftDeleted) {
                       continue;
                    }
                    // DID NOT CHECK ONLY ONE PERSON ACCOUNT WITH THE UNIQUE UCID
                    if (containSoftDeleted) {
                        if (multipleDifferentUCIDSoftDeleted == true) {
                            for (int i = 0; i < accountType.size(); i++) {
                                boolean idFound = false;
                                if (accountType.get(i).equals("Person Account")) {
                                    List<WebElement> elements = new ArrayList<>();
                                    elements.add(accountLink.get(i));
                                    for (int a = 0; a < accountType.size(); a++) {
                                        if (accountUCID.get(a).equals(accountUCID.get(i)) && accountType.get(a).equals("Person Account Soft Deleted")) {
                                            elements.add(accountLink.get(a));
                                            idFound = true;
                                        }
                                    }
                                    if (idFound) {
                                        List<String> linkersID = new ArrayList<>();
                                        for (int b = 0; b < elements.size(); b++) {
                                            System.out.println("Called: " + elements.get(b));
                                            new Actions(webDriver).keyDown(Keys.CONTROL).click(elements.get(b)).perform();
                                            if (b == 0) {
                                                linkersID = ChangeTabsToLookForPersonAccount(webDriver);
                                            } else {
                                                DoAutoMergeWithSoftDeletedAccount(webDriver, linkersID, line, fileWriterSD);
                                            }
                                        }
                                        fileWriterSD.flush();
                                        linkersID.clear();
                                        System.gc();
                                    }
                                }
                            }
                            new Actions(webDriver).sendKeys(Keys.CONTROL).keyUp(Keys.CONTROL).build().perform();
                        }
                        // IF CASE 2: CONTAINS SOFT DELETED ACCOUNT BUT DONT HAVE REAL ACCOUNTS


                        // IF NORMAL: ONLY ONE SOFT DELETED ACCOUNT FOUND
                        else if (hasPersonAccount) {
                            for (int i = 0; i < accountType.size(); i++) {
                                if (accountType.get(i).equals("Person Account")) {
                                    if (accountUCID.get(softDeletedAccountIndex.get(0)).equals(accountUCID.get(i))) {
                                        new Actions(webDriver).keyDown(Keys.CONTROL).click(accountLink.get(i)).perform();
                                    }
                                }
                            }
                            //Change tabs and figure out
                            ArrayList<String> tabs = new ArrayList<String>(webDriver.getWindowHandles());
                            webDriver.switchTo().window(tabs.get(1));
                            List<String> linkersID = new ArrayList<>();

                            WebDriverWait waitAccountLinkClickable = new WebDriverWait(webDriver, Duration.ofSeconds(20));
                            waitAccountLinkClickable.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@data-label=\"Account Link\"]")));
                            webDriver.findElement(By.xpath("//a[@data-label=\"Account Link\"]")).click();
                            List<WebElement> dealersLink = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/th//a//span"));
                            List<WebElement> dealersRecordType = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/td[1]//a"));
                            List<WebElement> dealers = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/td[3]//a"));
                            for (int i = 0; i < dealers.size(); i++) {
                                if (dealers.get(i).getAttribute("title").contains(dealerToFind)) {
                                    linkersID.add(dealersLink.get(i).getText());
                                    System.out.println(dealersLink.get(i).getText());
                                }
                            }
                            webDriver.close();
                            webDriver.switchTo().window(tabs.get(0));
                            tabs.clear();
                            if (softDeletedAccountIndex.size() > 0) {
                                for (int i = 0; i < softDeletedAccountIndex.size(); i++) {
                                    if (accountType.get(softDeletedAccountIndex.get(i)).equals("Person Account Soft Deleted")) {
                                        new Actions(webDriver).keyDown(Keys.CONTROL).click(accountLink.get(softDeletedAccountIndex.get(i))).perform();
                                    }
                                }
                            }
                            tabs = new ArrayList<String>(webDriver.getWindowHandles());
                            webDriver.switchTo().window(tabs.get(1));
                            WebDriverWait waitAccountLinkClickable2 = new WebDriverWait(webDriver, Duration.ofSeconds(20));
                            waitAccountLinkClickable2.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@data-label=\"Account Link\"]")));
                            webDriver.findElement(By.xpath("//a[@data-label=\"Account Link\"]")).click();
                            List<WebElement> linksInSoftDeleted = webDriver.findElements(By.xpath("//div[@class=\"windowViewMode-normal oneContent active lafPageHost\"]//flexipage-component2[1]//span[@class=\"lds-shrink-none slds-m-right--xx-small\"]"));
                            String s = linksInSoftDeleted.get(0).getText();
                            s = s.substring(s.indexOf("(") + 1);
                            s = s.substring(0, s.indexOf(")"));
                            boolean caughtMatchingUCID = false;
                            if (Integer.parseInt(s) <= 0) {

                            } else {
                                List<String> dealersSDMatch = new ArrayList<>();
                                List<WebElement> dealersLinkSD = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/th//a//span"));
                                List<WebElement> dealersRecordTypeSD = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/td[1]//a"));
                                List<WebElement> dealersSD = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/td[3]//a"));

                                for (int i = 0; i < dealersSD.size(); i++) {
                                    if (dealersSD.get(i).getAttribute("title").contains(dealerToFind)) {
                                        System.out.println("Found right dealer");
                                        caughtMatchingUCID = true;
                                        dealersSDMatch.add(dealersLinkSD.get(i).getText());
                                    }
                                }
                                if (caughtMatchingUCID) {
                                    fileWriterSD.append(line + "\n");
                                    fileWriterSD.append("------------main account----\n");
                                    for (int i = 0; i < linkersID.size(); i++) {
                                        fileWriterSD.append(linkersID.get(i) + "\n");
                                    }
                                    fileWriterSD.append("---------soft deleted account-------\n");
                                    for (int i = 0; i < dealersSDMatch.size(); i++) {
                                        fileWriterSD.append(dealersSDMatch.get(i) + "\n");
                                    }
                                    fileWriterSD.append("\n\n");
                                }
                            }
                            fileWriterSD.flush();
                            linkersID.clear();
                            webDriver.close();
                            webDriver.switchTo().window(tabs.get(0));
                            tabs.clear();
                        }
                    }
                    wait = null;
                    listLink.clear();
                    listID.clear();
                    listAccountType.clear();
                    System.gc();
                }
                else {
                    System.out.println("No data for this phone number found.");
                    wait = null;
                    listLink.clear();
                    listID.clear();
                    listAccountType.clear();
                    System.gc();
                    continue;
                }
            }
        }
        catch (Exception e) {
            System.out.println("Cannot read file");
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int cellColumn = 0;
        try {
            Path path = Paths.get(NAME+".txt");
            Files.delete(path);
        } catch (IOException e) {
            System.out.println("Cannot delete the selected file: " + NAME+".txt");
        }

        try {
            Path path = Paths.get(NAME+"-sd.txt");
            Files.delete(path);
        } catch (Exception e) {
            System.out.println("Cannot delete the file: " + NAME+"-sd.txt");
        }
        try {
            Path path = Paths.get(NAME+"-log.txt");
            Files.delete(path);
        } catch (Exception e) {
            System.out.println("Cannot delete the file: " + NAME+"-log.txt");
        }
        try {
            OPCPackage pkg = OPCPackage.open(new File(NAME+".xlsx"));
            Workbook workbook = new XSSFWorkbook(pkg);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();

            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                Iterator<Row> iterator = sheet.iterator();
                while (iterator.hasNext())  {
                    Row row = iterator.next();
                    Iterator<Cell> cellIterator =  row.iterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String cellValue = dataFormatter.formatCellValue(cell);
                        if (cellValue.toLowerCase().equals("phone")) {
                            cellColumn = cell.getColumnIndex();
                        }
                    }
                }
            }
            workbook.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        try {
            OPCPackage pkg = OPCPackage.open(new File(NAME+".xlsx"));
            Workbook workbook = new XSSFWorkbook(pkg);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();

            FileWriter fileWriter = new FileWriter(NAME+".txt");
            Set set = new HashSet();

            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                Iterator<Row> iterator = sheet.iterator();
                while (iterator.hasNext())  {
                    Row row = iterator.next();
                    Iterator<Cell> cellIterator =  row.iterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String cellValue = dataFormatter.formatCellValue(cell);
                        if (cell.getColumnIndex() == cellColumn && !cellValue.toLowerCase().equals("phone")) {
                            if (set.add(cellValue)) {
                                fileWriter.append(cellValue + "\n");
                            }
                        }
                    }
                }
            }
            set.clear();
            fileWriter.flush();
            workbook.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        try {
//            onHelloButtonClick();
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public List<String> ChangeTabsToLookForPersonAccount(WebDriver webDriver) {
        //Change tabs and figure out
        ArrayList<String> tabs = new ArrayList<String>(webDriver.getWindowHandles());
        webDriver.switchTo().window(tabs.get(1));
        List<String> linkersID = new ArrayList<>();

        WebDriverWait waitAccountLinkClickable = new WebDriverWait(webDriver, Duration.ofSeconds(20));
        waitAccountLinkClickable.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@data-label=\"Account Link\"]")));
        webDriver.findElement(By.xpath("//a[@data-label=\"Account Link\"]")).click();
        List<WebElement> dealersLink = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/th//a//span"));
        List<WebElement> dealersRecordType = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/td[1]//a"));
        List<WebElement> dealers = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/td[3]//a"));
        for (int i = 0; i < dealers.size(); i++) {
            if (dealers.get(i).getAttribute("title").contains(dealerToFind)) {
                linkersID.add(dealersLink.get(i).getText());
            }
        }
        webDriver.close();
        webDriver.switchTo().window(tabs.get(0));
        tabs.clear();
        return linkersID;
    }

    //
    public void DoAutoMergeWithSoftDeletedAccount(WebDriver webDriver, List<String> linkersID, String phoneNumber, FileWriter fileWriterSD) throws IOException {

        ArrayList<String> tabs = new ArrayList<String>(webDriver.getWindowHandles());
        webDriver.switchTo().window(tabs.get(1));
        WebDriverWait waitAccountLinkClickable2 = new WebDriverWait(webDriver, Duration.ofSeconds(20));
        waitAccountLinkClickable2.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@data-label=\"Account Link\"]")));
        webDriver.findElement(By.xpath("//a[@data-label=\"Account Link\"]")).click();
        List<WebElement> linksInSoftDeleted = webDriver.findElements(By.xpath("//div[@class=\"windowViewMode-normal oneContent active lafPageHost\"]//flexipage-component2[1]//span[@class=\"lds-shrink-none slds-m-right--xx-small\"]"));
        String s = linksInSoftDeleted.get(0).getText();
        s = s.substring(s.indexOf("(") + 1);
        s = s.substring(0, s.indexOf(")"));
        boolean caughtMatchingUCID = false;

        if (Integer.parseInt(s) <= 0) {
        } else {
            List<String> dealersSDMatch = new ArrayList<>();
            List<WebElement> dealersLinkSD = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/th//a//span"));
            List<WebElement> dealersRecordTypeSD = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/td[1]//a"));
            List<WebElement> dealersSD = webDriver.findElements(By.xpath("//tbody[@lightning-datatable_table]/descendant::tr/td[3]//a"));
            for (int i = 0; i < dealersSD.size(); i++) {
                if (dealersSD.get(i).getAttribute("title").contains(dealerToFind)) {
                    fileWriterSD.append("Found here \n");
                    System.out.println("Found right dealer");
                    caughtMatchingUCID = true;
                    dealersSDMatch.add(dealersLinkSD.get(i).getText());
                }
            }
            if (caughtMatchingUCID) {
                fileWriterSD.append(phoneNumber + "\n");
                fileWriterSD.append("------------main account----\n");
                for (int i = 0; i < linkersID.size(); i++) {
                    fileWriterSD.append(linkersID.get(i) + "\n");
                }
                fileWriterSD.append("---------soft deleted account-------\n");
                for (int i = 0; i < dealersSDMatch.size(); i++) {
                    fileWriterSD.append(dealersSDMatch.get(i) + "\n");
                }
                fileWriterSD.append("\n\n");
            }
        }
        webDriver.close();
        webDriver.switchTo().window(tabs.get(0));
        tabs.clear();
    }
}