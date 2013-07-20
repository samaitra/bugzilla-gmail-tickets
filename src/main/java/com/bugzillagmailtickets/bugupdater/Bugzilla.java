package com.bugzillagmailtickets.bugupdater;

import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Bugzilla {

    static WebDriver driver;
    String hostname = "localhost";
    int port = 4444;
    String bugzillaHostname = "localhost";
    String username="";
    String password="";

    void sessionStart(){
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        capabilities = DesiredCapabilities.firefox();
        try{
        driver = new RemoteWebDriver(new URL("http://"+hostname+":"+port+"/wd/hub"), capabilities);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


        int createBug(String subj, String body) {

        sessionStart();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(bugzillaHostname);
        WebElement loginLink = driver.findElement(By.id("login_link_top"));
        loginLink.click();
        WebElement login = driver.findElement(By.id("Bugzilla_login_top"));
        login.sendKeys(username);
        WebElement pwd = driver.findElement(By.id("Bugzilla_password_top"));
        pwd.sendKeys(password);
        WebElement submit = driver.findElement(By.id("log_in_top"));
        submit.click();
        WebElement fileBug = driver.findElement(By.id("enter_bug"));
        fileBug.click();

        WebElement www = driver.findElement(By.linkText("WWW"));
        www.click();

        WebElement selectComponent = driver.findElement(By.id("component"));
        List<WebElement> allOptions = selectComponent.findElements(By.tagName("option"));
        for (WebElement option : allOptions) {
            if(option.getAttribute("value").equals("FrontEnd")){
            option.click();
            }
        }

        WebElement selectVersion = driver.findElement(By.id("version"));
        List<WebElement> allVersionOptions = selectComponent.findElements(By.tagName("option"));
        for (WebElement option : allVersionOptions) {
            if(option.getAttribute("value").equals("1.0")){
                option.click();
            }
        }

        WebElement selectTicketType = driver.findElement(By.id("cf_type"));
        List<WebElement> allTypeVersionOptions = selectTicketType.findElements(By.tagName("option"));

        for (WebElement option : allTypeVersionOptions){
            if(option.getAttribute("value").equals("Bug")){
                option.click();
            }
        }

        WebElement shortDesc = driver.findElement(By.id("short_desc"));
        shortDesc.sendKeys(subj);

        WebElement comment = driver.findElement(By.id("comment"));
        body = html2text(body);
        comment.sendKeys(body);

        WebElement commit = driver.findElement(By.id("commit"));
        commit.click();

        WebDriverWait wait = new WebDriverWait(driver, 10);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("bz_alias_short_desc_container")));
        String bugText = driver.findElement(By.cssSelector("div.bz_alias_short_desc_container > a")).getText();
        String a[] = bugText.split("\\s");
        int bugId = Integer.parseInt(a[1]);
        System.out.println("bugId = "+bugId);

        sessionStop();

        return bugId;
    }
    void updateBugComment(String bugId, String body) {

        sessionStart();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(bugzillaHostname);
        WebElement loginLink = driver.findElement(By.id("login_link_top"));
        loginLink.click();
        WebElement login = driver.findElement(By.id("Bugzilla_login_top"));
        login.sendKeys(username);
        WebElement pwd = driver.findElement(By.id("Bugzilla_password_top"));
        pwd.sendKeys(password);
        WebElement submit = driver.findElement(By.id("log_in_top"));
        submit.click();

        driver.get(bugzillaHostname+"/show_bug.cgi?id="+bugId+"");
        WebElement comment = driver.findElement(By.id("comment"));
        body = html2text(body);
        comment.sendKeys(body);

        WebElement commit = driver.findElement(By.id("commit"));
        commit.click();

        sessionStop();

    }

    public void sessionStop() {

        driver.quit();
    }

    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }
}