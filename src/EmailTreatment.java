import com.rabbitmq.client.*;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONObject;

import javax.json.JsonObject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

class EmailTreatment {
    private final static String QUEUE_NAME = "mails";
    private java.sql.Connection conn;

    EmailTreatment() throws IOException, TimeoutException, ClassNotFoundException, java.sql.SQLException {
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
                System.out.println(" [0] Received '" + message + "'");
                try {
                    InsertInDatabase(new JSONObject(message));
                } catch (SQLException e) {
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

    private void InsertInDatabase(JSONObject json) throws java.sql.SQLException {
        String query = "insert into Courriels (`From`, `To`, `Subject`, `Body`)  values (?, ?, ?, ?)";

        java.sql.PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString (1, json.getString("From"));
        preparedStmt.setString (2, json.getString("To"));
        preparedStmt.setString (3, json.getString("Subject"));
        preparedStmt.setString (4, json.getString("Body"));

        preparedStmt.execute();
    }

    public void finalize() throws SQLException {
        conn.close();
    }
}
