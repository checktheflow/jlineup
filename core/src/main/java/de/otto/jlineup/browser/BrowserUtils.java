package de.otto.jlineup.browser;

import de.otto.jlineup.RunStepConfig;
import de.otto.jlineup.config.JobConfig;
import de.otto.jlineup.config.Step;
import de.otto.jlineup.config.UrlConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.otto.jlineup.config.JobConfig.DEFAULT_PATH;
import static java.lang.invoke.MethodHandles.lookup;

public class BrowserUtils {

    private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());
    private static final boolean CHROME_DETERMINISTIC_OPTIONS = true;

    public static String buildUrl(String url, String path, final Map<String, String> envMapping) {
        if (envMapping != null && !envMapping.isEmpty()) {
            for (Map.Entry<String, String> envMappingEntry : envMapping.entrySet()) {
                final String fromEnvironment = envMappingEntry.getKey();
                final String toEnvironment = envMappingEntry.getValue();
                url = url.replace("https://" + fromEnvironment + ".", "https://" + toEnvironment + ".");
                url = url.replace("http://" + fromEnvironment + ".", "http://" + toEnvironment + ".");
                url = url.replace("." + fromEnvironment + ".", "." + toEnvironment + ".");
            }
        }
        return buildUrl(url, path);
    }

    static String buildUrl(String url, String path) {
        if (path == null) {
            path = DEFAULT_PATH;
        }
        if (!url.endsWith("/") && !path.equals("") && !path.startsWith("/")) {
            url = url + "/";
        }
        if (url.endsWith("/") && path.startsWith("/")) {
            path = path.substring(1);
        }
        return url + path;
    }

    synchronized WebDriver getWebDriverByConfig(JobConfig jobConfig, RunStepConfig runStepConfig) {
        return getWebDriverByConfig(jobConfig, runStepConfig, JobConfig.DEFAULT_WINDOW_WIDTH);
    }

    synchronized WebDriver getWebDriverByConfig(JobConfig jobConfig, RunStepConfig runStepConfig, int width) {
        WebDriver driver;
        if (jobConfig.browser.isFirefox()) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            options.setProfile(getFirefoxProfileWithDisabledAnimatedGifs());
            options.addArguments(runStepConfig.getFirefoxParameters());
            if (jobConfig.browser.isHeadless()) {
                options.setHeadless(true);
                options.addArguments("-width", width + "", "-height", jobConfig.windowHeight + "");
            }

            LOG.debug("Creating firefox with options: {}", options.toString());
            driver = new FirefoxDriver(options);
        } else if (jobConfig.browser.isChrome()) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();

            //To work in a headless env, this is needed
            options.addArguments("--no-sandbox");
            options.addArguments(runStepConfig.getChromeParameters());

            //These options my help to convince Chrome to render deterministically
            //This is important for the pixel-perfect comparison of before and after steps
            //There were problems to render Webfonts, SVGs and progressive JPGs deterministically on huge pages (i.e. otto.de)
            //Beware of dragons
            if (CHROME_DETERMINISTIC_OPTIONS) {
                options.addArguments("--disable-threaded-animation");
                //options.addArguments("--disable-threaded-compositing"); // This option breaks rendering completely as of Chrome 70 (2019-01-07)
                options.addArguments("--disable-threaded-scrolling");
                options.addArguments("--num-raster-threads=1");
                options.addArguments("--disable-histogram-customizer");
                options.addArguments("--disable-composited-antialiasing");
            }

            if (jobConfig.browser.isHeadless()) {
                options.setHeadless(true);
                options.addArguments("--window-size=" + width + "," + jobConfig.windowHeight);
            }
            LOG.debug("Creating chrome with options: {}", options.toString());
            driver = new ChromeDriver(options);
        } else {
            WebDriverManager.phantomjs().forceCache().setup();
            driver = new PhantomJSDriver();
        }
        driver.manage().timeouts().pageLoadTimeout(jobConfig.pageLoadTimeout, TimeUnit.SECONDS);
        return driver;
    }

    private FirefoxProfile getFirefoxProfileWithDisabledAnimatedGifs() {
        FirefoxProfile firefoxProfileHeadless = new FirefoxProfile();
        firefoxProfileHeadless.setPreference("image.animation_mode", "none");
        return firefoxProfileHeadless;
    }

    static List<ScreenshotContext> buildScreenshotContextListFromConfigAndState(RunStepConfig runStepConfig, JobConfig jobConfig) throws JLineupException {
        List<ScreenshotContext> screenshotContextList = new ArrayList<>();
        Map<String, UrlConfig> urls = jobConfig.urls;

        for (final Map.Entry<String, UrlConfig> urlConfigEntry : urls.entrySet()) {
            final UrlConfig urlConfig = urlConfigEntry.getValue();
            final List<Integer> resolutions = urlConfig.windowWidths;
            final List<String> paths = urlConfig.paths;
            for (final String path : paths) {
                screenshotContextList.addAll(
                        resolutions.stream()
                                .map(windowWidth ->
                                        new ScreenshotContext(prepareDomain(runStepConfig, urlConfigEntry.getKey()), path, windowWidth,
                                                runStepConfig.getStep() == Step.before, urlConfigEntry.getValue(), getFullPathOfReportDir(runStepConfig)))
                                .collect(Collectors.toList()));
            }
        }
        return screenshotContextList;
    }

    public static String getFullPathOfReportDir(RunStepConfig runStepConfig) {
        if (runStepConfig.getReportDirectory() == null) {
            return null;
        }
        return Paths.get(runStepConfig.getWorkingDirectory(), runStepConfig.getReportDirectory()).toAbsolutePath().toString();
    }

    public static String prepareDomain(final RunStepConfig runStepConfig, final String url) {
        String processedUrl = url;
        for (Map.Entry<String, String> replacement : runStepConfig.getUrlReplacements().entrySet()) {
            processedUrl = processedUrl.replace(replacement.getKey(), replacement.getValue());
        }
        return processedUrl;
    }

    public static String prependHTTPIfNotThereAndToLowerCase(String url) {
        String ret = url.toLowerCase();
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://") && !url.startsWith("ftp://")) {
            ret = "http://" + ret;
        }
        return ret;
    }
}