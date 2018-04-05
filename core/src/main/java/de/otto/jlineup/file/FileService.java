package de.otto.jlineup.file;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import de.otto.jlineup.RunStepConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileService {

    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String DIVIDER = "_";
    public static final String PNG_EXTENSION = ".png";
    private static final int MAX_URL_TO_FILENAME_LENGHT = 180;

    private final RunStepConfig runStepConfig;

    public FileService(RunStepConfig runStepConfig) {
        this.runStepConfig = runStepConfig;
    }

    @VisibleForTesting
    Path createDirIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        Files.createDirectories(path);
        return path;
    }

    @VisibleForTesting
    void clearDirectory(String path) throws IOException {
        Path directory = Paths.get(path);
        Files.newDirectoryStream(directory).forEach(file -> {
            try {

                if (Files.isDirectory(file)) {
                    clearDirectory(file.toAbsolutePath().toString());
                }

                Files.delete(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public void createOrClearReportDirectory() {
        createOrClearDirectoryBelowWorkingDir(runStepConfig.getWorkingDirectory(), runStepConfig.getReportDirectory());
    }

    private Path getScreenshotDirectory() {
        return Paths.get(String.format("%s/%s", runStepConfig.getWorkingDirectory(), runStepConfig.getScreenshotsDirectory()));
    }

    private Path getReportDirectory() {
        return Paths.get(String.format("%s/%s", runStepConfig.getWorkingDirectory(), runStepConfig.getReportDirectory()));
    }

    public void createWorkingDirectoryIfNotExists() {
        try {
            createDirIfNotExists(runStepConfig.getWorkingDirectory());
        } catch (IOException e) {
            System.err.println("Could not create or open working directory.");
            System.exit(1);
        }
    }

    public void createOrClearScreenshotsDirectory() {
        createOrClearDirectoryBelowWorkingDir(runStepConfig.getWorkingDirectory(), runStepConfig.getScreenshotsDirectory());
    }

    private void createOrClearDirectoryBelowWorkingDir(String workingDirectory, String subDirectory) {
        try {
            final String subDirectoryPath = workingDirectory + "/" + subDirectory;
            createDirIfNotExists(subDirectoryPath);
            clearDirectory(subDirectoryPath);
        } catch (IOException e) {
            System.err.println("Could not create or open " + subDirectory + " directory.");
            System.exit(1);
        }
    }

    @VisibleForTesting
    String generateScreenshotFileName(String url, String urlSubPath, int width, int yPosition, String type) {

        String fileName = generateScreenshotFileNamePrefix(url, urlSubPath)
                + String.format("%04d", width)
                + DIVIDER
                + String.format("%05d", yPosition)
                + DIVIDER
                + type;

        fileName = fileName + PNG_EXTENSION;

        return fileName;
    }

    private String generateScreenshotFileNamePrefix(String url, String urlSubPath) {

        @SuppressWarnings("deprecation") String hash = Hashing.sha1().hashString(url + urlSubPath, Charsets.UTF_8).toString().substring(0, 7);

        if (urlSubPath.equals("/") || urlSubPath.equals("")) {
            urlSubPath = "root";
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        String fileNamePrefix = url + DIVIDER + urlSubPath + DIVIDER;
        fileNamePrefix = fileNamePrefix.replace("://", DIVIDER);
        fileNamePrefix = fileNamePrefix.replaceAll("[^A-Za-z0-9\\-_]", DIVIDER);

        if (fileNamePrefix.length() > MAX_URL_TO_FILENAME_LENGHT) {
            fileNamePrefix = fileNamePrefix.substring(0, MAX_URL_TO_FILENAME_LENGHT) + DIVIDER;
        }
        
        fileNamePrefix = fileNamePrefix + hash + DIVIDER;

        return fileNamePrefix;
    }

    @VisibleForTesting
    String getScreenshotPath(String url, String urlSubPath, int width, int yPosition, String step) {
        return runStepConfig.getWorkingDirectory() + (runStepConfig.getWorkingDirectory().endsWith("/") ? "" : "/")
                + runStepConfig.getScreenshotsDirectory() + (runStepConfig.getScreenshotsDirectory().endsWith("/") ? "" : "/")
                + generateScreenshotFileName(url, urlSubPath, width, yPosition, step);
    }

    private String getScreenshotPath(String fileName) {
        return runStepConfig.getWorkingDirectory() + (runStepConfig.getWorkingDirectory().endsWith("/") ? "" : "/")
                + runStepConfig.getScreenshotsDirectory() + (runStepConfig.getScreenshotsDirectory().endsWith("/") ? "" : "/")
                + fileName;
    }

    public BufferedImage readScreenshot(String fileName) throws IOException {
        return ImageIO.read(new File(getScreenshotPath(fileName)));
    }

    private void writeScreenshot(String fileName, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", new File(fileName));
    }

    @VisibleForTesting
    List<String> getFileNamesMatchingPattern(Path directory, String matcherPattern) throws IOException {
        final List<String> files = new ArrayList<>();

        final PathMatcher pathMatcher = FileSystems.getDefault()
                .getPathMatcher(matcherPattern);

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
                directory, pathMatcher::matches)) {
            dirStream.forEach(filePath -> files.add(filePath.getFileName().toString()));
        }
        files.sort(Comparator.naturalOrder());
        return files;
    }

    public String writeScreenshot(BufferedImage image, String url,
                                  String urlSubPath, int windowWidth, int yPosition, String step) throws IOException {
        final String screenshotPath =
                getScreenshotPath(url,
                        urlSubPath, windowWidth,
                        yPosition, step);
        writeScreenshot(screenshotPath, image);
        return screenshotPath;
    }

    public List<String> getFilenamesForStep(String path, String url, String step) throws IOException {
        final String matcherPattern = "glob:**" + generateScreenshotFileNamePrefix(url, path) + "*_*_" + step + ".png";
        Path screenshotDirectory = getScreenshotDirectory();
        return getFileNamesMatchingPattern(screenshotDirectory, matcherPattern);
    }

    public String getRelativePathFromReportDirToScreenshotsDir() {
        Path screenshotDirectory = getScreenshotDirectory().toAbsolutePath();
        Path reportDirectory = getReportDirectory().toAbsolutePath();
        Path relative = reportDirectory.relativize(screenshotDirectory);
        return relative.toString().equals("") ? "" : relative.toString() + "/";
    }

    public void writeJsonReport(String reportJson) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(new FileOutputStream(getReportDirectory().toString() + "/report.json"))) {
            out.print(reportJson);
        }
    }

    public void writeHtmlReport(String htmlReport) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(new FileOutputStream(getReportDirectory().toString() + "/report.html"))) {
            out.print(htmlReport);
        }
    }
}

