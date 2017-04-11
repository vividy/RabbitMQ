import com.rabbitmq.client.*;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

class ErrorTreatment {
    private final static String QUEUE_NAME = "errors";
    private java.sql.Connection conn;

    ErrorTreatment() throws IOException, TimeoutException, java.sql.SQLException, ClassNotFoundException {
        CreateConnectionForDatabase();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [2] Received '" + message + "'");
                try {
                    InsertInDatabase(new JSONObject(message));
                } catch (SQLException | ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

    private void CreateConnectionForDatabase() throws ClassNotFoundException, java.sql.SQLException {
        String myDriver = "org.gjt.mm.mysql.Driver";
        String myUrl = "jdbc:mysql://localhost/TP2";
        Class.forName(myDriver);
        conn = java.sql.DriverManager.getConnection(myUrl, "root", "");
    }

    private void InsertInDatabase(JSONObject json) throws java.sql.SQLException, ParseException {
        String query = "insert into Logs (`Type`, `Date`, `Body`)  values (?, ?, ?)";

        java.util.Date utilDate = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.CANADA).parse(json.getString("Date"));
        java.sql.PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString (1, json.getString("Type"));
        preparedStmt.setDate (2, new Date(utilDate.getTime()));
        preparedStmt.setString (3, json.getString("Body"));

        preparedStmt.execute();
    }

    public void finalize() throws SQLException {
        conn.close();
    }
}
