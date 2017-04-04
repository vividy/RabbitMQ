import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] argv) throws InterruptedException, IOException, TimeoutException {
        new EmailTreatment();
        new InformationWarningTreatment();
        new ErrorTreatment();

        new Producer();
    }
}
