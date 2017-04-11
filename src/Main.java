import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] argv) throws InterruptedException, IOException, TimeoutException, SQLException, ClassNotFoundException {
        new EmailTreatment();
        new InformationWarningTreatment();
        new ErrorTreatment();

        new Producer();
    }
}
