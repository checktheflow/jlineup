package de.otto.jlineup.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import de.otto.jlineup.browser.Browser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class Config {

    public static final String LINEUP_CONFIG_DEFAULT_PATH = "./lineup.json";

    public static final String EXAMPLE_URL = "https://www.example.com";

    public static final Browser.Type DEFAULT_BROWSER = Browser.Type.PHANTOMJS;
    public static final float DEFAULT_MAX_DIFF = 0;
    public static final int DEFAULT_WINDOW_HEIGHT = 800;
    public static final float DEFAULT_GLOBAL_WAIT_AFTER_PAGE_LOAD = 0f;
    public static final List<Integer> DEFAULT_WINDOW_WIDTHS = ImmutableList.of(800);
    public static final List<String> DEFAULT_PATHS = ImmutableList.of("/");
    public static final int DEFAULT_MAX_SCROLL_HEIGHT = 100000;
    public static final int DEFAULT_WAIT_AFTER_PAGE_LOAD = 0;
    public static final int DEFAULT_WAIT_AFTER_SCROLL = 0;
    public static final int DEFAULT_WAIT_FOR_NO_ANIMATION_AFTER_SCROLL = 0;
    public static final int DEFAULT_WARMUP_BROWSER_CACHE_TIME = 0;
    public static final int DEFAULT_WAIT_FOR_FONTS_TIME = 0;
    public static final int DEFAULT_THREADS = 1;
    public static final int DEFAULT_REPORT_FORMAT = 2;
    public static final int DEFAULT_PAGELOAD_TIMEOUT = 120;
    public static final int DEFAULT_SCREENSHOT_RETRIES = 0;

    public final Map<String, UrlConfig> urls;
    public final Browser.Type browser;
    @SerializedName(value = "wait-after-page-load", alternate = "async-wait")
    public final Float globalWaitAfterPageLoad;
    @SerializedName(value = "page-load-timeout")
    public final int pageLoadTimeout;
    @SerializedName("window-height")
    public final Integer windowHeight;
    @SerializedName("report-format")
    public final Integer reportFormat;
    @SerializedName("debug")
    public final boolean debug;
    @SerializedName("screenshot-retries")
    public final int screenshotRetries;

    private final static Gson gson = new Gson();
    public int threads;

    /* Used by GSON to set default values */
    public Config() {
        urls = null;
        browser = DEFAULT_BROWSER;
        globalWaitAfterPageLoad = DEFAULT_GLOBAL_WAIT_AFTER_PAGE_LOAD;
        pageLoadTimeout = DEFAULT_PAGELOAD_TIMEOUT;
        windowHeight = DEFAULT_WINDOW_HEIGHT;
        threads = DEFAULT_THREADS;
        debug = false;
        screenshotRetries = DEFAULT_SCREENSHOT_RETRIES;
        reportFormat = DEFAULT_REPORT_FORMAT;
    }

    public Config(final Map<String, UrlConfig> urls, final Browser.Type browser, final Float globalWaitAfterPageLoad, final Integer pageLoadTimeout, final Integer windowHeight, final Integer threads, final Integer screenshotRetries, final Integer reportFormat, final boolean debug) {
        this.urls = urls;
        this.browser = browser != null ? browser : DEFAULT_BROWSER;
        this.globalWaitAfterPageLoad = globalWaitAfterPageLoad != null ? globalWaitAfterPageLoad : DEFAULT_GLOBAL_WAIT_AFTER_PAGE_LOAD;
        this.pageLoadTimeout = pageLoadTimeout != null ? pageLoadTimeout : DEFAULT_PAGELOAD_TIMEOUT;
        this.windowHeight = windowHeight != null ? windowHeight : DEFAULT_WINDOW_HEIGHT;
        this.threads = threads != null ? threads : DEFAULT_THREADS;
        this.screenshotRetries = screenshotRetries != null ? screenshotRetries : DEFAULT_SCREENSHOT_RETRIES;
        this.reportFormat = Objects.isNull(reportFormat) ? null : reportFormat.equals(DEFAULT_REPORT_FORMAT) ? null : reportFormat;
        this.debug = debug;
    }

    public static Config defaultConfig() {
        return defaultConfig(EXAMPLE_URL);
    }

    public static Config defaultConfig(String url) {
        return new Config(ImmutableMap.of(url, new UrlConfig()), null, null, null, null, null, null, null, false);
    }

    @Override
    public String toString() {
        return "Config{" +
                "urls=" + urls +
                ", browser=" + browser +
                ", globalWaitAfterPageLoad=" + globalWaitAfterPageLoad +
                ", pageLoadTimeout=" + pageLoadTimeout +
                ", windowHeight=" + windowHeight +
                ", reportFormat=" + reportFormat +
                ", debug=" + debug +
                ", screenshotRetries=" + screenshotRetries +
                ", threads=" + threads +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Config config = (Config) o;

        if (pageLoadTimeout != config.pageLoadTimeout) return false;
        if (debug != config.debug) return false;
        if (screenshotRetries != config.screenshotRetries) return false;
        if (threads != config.threads) return false;
        if (urls != null ? !urls.equals(config.urls) : config.urls != null) return false;
        if (browser != config.browser) return false;
        if (globalWaitAfterPageLoad != null ? !globalWaitAfterPageLoad.equals(config.globalWaitAfterPageLoad) : config.globalWaitAfterPageLoad != null)
            return false;
        if (windowHeight != null ? !windowHeight.equals(config.windowHeight) : config.windowHeight != null)
            return false;
        return reportFormat != null ? reportFormat.equals(config.reportFormat) : config.reportFormat == null;
    }

    @Override
    public int hashCode() {
        int result = urls != null ? urls.hashCode() : 0;
        result = 31 * result + (browser != null ? browser.hashCode() : 0);
        result = 31 * result + (globalWaitAfterPageLoad != null ? globalWaitAfterPageLoad.hashCode() : 0);
        result = 31 * result + pageLoadTimeout;
        result = 31 * result + (windowHeight != null ? windowHeight.hashCode() : 0);
        result = 31 * result + (reportFormat != null ? reportFormat.hashCode() : 0);
        result = 31 * result + (debug ? 1 : 0);
        result = 31 * result + screenshotRetries;
        result = 31 * result + threads;
        return result;
    }

    public static Config exampleConfig() {
        return new Config(ImmutableMap.of("http://www.example.com",
                new UrlConfig(
                        ImmutableList.of("/","someOtherPath"),
                        DEFAULT_MAX_DIFF,
                        ImmutableList.of(
                                new Cookie("exampleCookieName", "exampleValue", "http://www.example.com", "/", new Date(1000L), true)
                        ),
                        ImmutableMap.of("live", "www"),
                        ImmutableMap.of("exampleLocalStorageKey", "value"),
                        ImmutableMap.of("exampleSessionStorageKey", "value"),
                        ImmutableList.of(600,800,1000),
                        DEFAULT_MAX_SCROLL_HEIGHT,
                        DEFAULT_WAIT_AFTER_PAGE_LOAD,
                        DEFAULT_WAIT_AFTER_SCROLL,
                        DEFAULT_WAIT_FOR_NO_ANIMATION_AFTER_SCROLL,
                        DEFAULT_WARMUP_BROWSER_CACHE_TIME,
                        "console.log('This is JavaScript!')",
                        DEFAULT_WAIT_FOR_FONTS_TIME
                )),
                Browser.Type.PHANTOMJS,
                DEFAULT_GLOBAL_WAIT_AFTER_PAGE_LOAD,
                DEFAULT_PAGELOAD_TIMEOUT,
                DEFAULT_WINDOW_HEIGHT,
                DEFAULT_THREADS,
                DEFAULT_SCREENSHOT_RETRIES,
                DEFAULT_REPORT_FORMAT,
                false
        );
    }

    public static Config readConfig(final Parameters parameters) throws FileNotFoundException {
        return Config.readConfig(parameters.getWorkingDirectory(), parameters.getConfigFile());
    }

    public static Config readConfig(final String workingDir, final String configFileName) throws FileNotFoundException {

        List<String> searchPaths = new ArrayList<>();
        Path configFilePath = Paths.get(workingDir + "/" + configFileName);
        searchPaths.add(configFilePath.toString());
        if (!Files.exists(configFilePath)) {
            configFilePath = Paths.get(configFileName);
            searchPaths.add(configFilePath.toString());
            if (!Files.exists(configFilePath)) {
                configFilePath = Paths.get(LINEUP_CONFIG_DEFAULT_PATH);
                searchPaths.add(configFilePath.toString());
                if (!Files.exists(configFilePath)) {
                    throw new FileNotFoundException("Config file not found. Search locations were: " + Arrays.toString(searchPaths.toArray()));
                }
            }
        }

        BufferedReader br = new BufferedReader(new FileReader(configFilePath.toString()));
        return gson.fromJson(br, Config.class);

    }

}
