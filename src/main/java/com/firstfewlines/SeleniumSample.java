package com.firstfewlines;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Delta;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import io.github.pramcharan.wd.binary.downloader.WebDriverBinaryDownloader;
import io.github.pramcharan.wd.binary.downloader.enums.BrowserType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SeleniumSample {

    public static void main(String[] argv) throws Exception {
        WebDriverBinaryDownloader.create().downloadLatestBinaryAndConfigure(BrowserType.CHROME);

        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "iPhone 6");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        WebDriver webDriver = new ChromeDriver(chromeOptions);

        webDriver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        webDriver.navigate().to(new URL(argv[0]));
        WebDriverWait wait = new WebDriverWait(webDriver, 20);

        ExpectedCondition<Boolean> expectation = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");

        wait.until(expectation);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("html")));
        WebElement element = webDriver.findElement(By.tagName("html"));
        String ps = element.getAttribute("innerHTML");
        Document doc = Jsoup.parseBodyFragment(ps);
        try {
            String currentUsersHomeDir = System.getProperty("user.home");
            File target = new File(currentUsersHomeDir + File.separator + "downloaded_source.html");
            if (target.exists() && !target.isDirectory()) {
                System.out.println("Html Source file exist ");
                File file2 = new File(currentUsersHomeDir + File.separator + "old_downloaded_source.html");

                target.renameTo(file2);
                FileWriter fs = new FileWriter(currentUsersHomeDir + File.separator + "downloaded_source.html");
                fs.write(doc.html());

                List<String> original = Files.readAllLines(target.toPath());
                List<String> revised = Files.readAllLines(file2.toPath());

                Patch<String> patch = DiffUtils.diff(original, revised);

                for (Delta<String> delta : patch.getDeltas()) {
                    System.out.println(delta);
                }

            } else {
                System.out.println("This is the firs run of the program - creating html source file");
                FileWriter fs = new FileWriter(currentUsersHomeDir + File.separator + "downloaded_source.html");
                fs.write(doc.html());
                System.out.println("Downloaded source can be found via: "+ currentUsersHomeDir + File.separator + "downloaded_source.html");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        webDriver.quit();
    }
}
