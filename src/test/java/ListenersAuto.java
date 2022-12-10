import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public  class ListenersAuto implements ITestListener {
    public void onStart(ITestContext execution) {
        System.out.println("------------------Starting Execution------------------");
    }

    public void onFinish(ITestContext execution) {
        System.out.println("------------------Execution Ended------------------");
    }

    public void onTestStart(ITestResult test) {
        System.out.println("------------------Starting Test: " + test.getName() + "------------------");
    }

    public void onTestSuccess(ITestResult test) {
        System.out.println("------------------Test: " + test.getName() + " Passed------------------");
    }

    public void onTestFailure(ITestResult test) {
        System.out.println("------------------Test: " + test.getName() + " Failed------------------");
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult test) {
        System.out.println("???");
    }

    public void onTestSkipped(ITestResult test) {
        System.out.println("------------------Skipping Test: " + test.getName() + "------------------");
    }
}