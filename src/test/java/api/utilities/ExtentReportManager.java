package api.utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ExtentReportManager implementing ITestListener.
 */
public class ExtentReportManager implements ITestListener {

    private static ExtentReports extent;
    private static ExtentSparkReporter spark;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    // Use project-relative reports folder (e.g., <project-root>/reports)
    private static final String REPORT_DIR = System.getProperty("user.dir") + File.separator + "reports";
    private static final String SCREENSHOT_DIR = System.getProperty("user.dir") + File.separator + "target" + File.separator + "screenshots";

    private synchronized void initReport(ITestContext context) {
        if (extent != null) return;

        try {
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (Exception e) {
            System.err.println("Could not create report dir: " + e.getMessage());
        }

        String timestamp = sdf.format(new Date());
        String reportPath = REPORT_DIR + File.separator + "AutomationReport_" + timestamp + ".html";

        System.out.println("Extent report path: " + Paths.get(reportPath).toAbsolutePath());

        spark = new ExtentSparkReporter(reportPath);
        spark.config().setDocumentTitle("Automation Test Report");
        spark.config().setReportName("Regression Results - " + context.getName());
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");

        extent = new ExtentReports();
        extent.attachReporter(spark);

        // optional meta info
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
        extent.setSystemInfo("User", System.getProperty("user.name"));
        extent.setSystemInfo("Env", context.getSuite().getName());
    }

    @Override
    public void onStart(ITestContext context) {
        initReport(context);
        System.out.println("ExtentReportManager: onStart - report initialized");
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        if (desc == null) desc = testName;
        ExtentTest test = extent.createTest(testName, desc);
        testThreadLocal.set(test);
        testThreadLocal.get().log(Status.INFO, "Test started: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = testThreadLocal.get();
        if (test != null) test.log(Status.PASS, "Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = testThreadLocal.get();
        if (test == null) {
            test = extent.createTest(result.getMethod().getMethodName());
            testThreadLocal.set(test);
        }

        Throwable t = result.getThrowable();
        if (t != null) test.fail(t);

        try {
            Path screenshotsPath = Paths.get(SCREENSHOT_DIR);
            if (Files.exists(screenshotsPath) && Files.isDirectory(screenshotsPath)) {
                String methodName = result.getMethod().getMethodName();
                File dir = screenshotsPath.toFile();
                File[] matches = dir.listFiles((d, name) -> name.toLowerCase().contains(methodName.toLowerCase()));
                if (matches != null && matches.length > 0) {
                    String imagePath = matches[0].getAbsolutePath();
                    test.fail("Screenshot on Failure", MediaEntityBuilder.createScreenCaptureFromPath(imagePath).build());
                }
            }
        } catch (Exception e) {
            test.info("Failed to attach screenshot: " + e.getMessage());
        }

        test.log(Status.FAIL, "Test Failed");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = testThreadLocal.get();
        if (test == null) {
            test = extent.createTest(result.getMethod().getMethodName());
            testThreadLocal.set(test);
        }
        test.log(Status.SKIP, "Test Skipped");
        Throwable t = result.getThrowable();
        if (t != null) test.skip(t);
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
            System.out.println("ExtentReportManager: onFinish - report flushed to disk");
        }
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { /* no-op */ }
}
