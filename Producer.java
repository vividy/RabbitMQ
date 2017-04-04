import com.github.javafaker.Faker;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeoutException;

class Producer {
    private final static String QUEUE_ERROR = "errors";
    private final static String QUEUE_LOG = "logs";
    private final static String QUEUE_MAIL = "mails";
    private final static boolean TRUE = true;
    private Connection connection;
    private Faker faker = new Faker();

    Producer() throws InterruptedException, IOException, TimeoutException {
        generateMessage();
    }

    public void finalize() throws IOException {
        connection.close();
    }

    /*
     * O for mail
     * 1 for information/warning
     * 2 for error
     */
    private void generateMessage() throws InterruptedException, IOException, TimeoutException {
        Random randomGenerator = new Random();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();

        while (TRUE) {
            Thread.sleep(1000);
            switch (randomGenerator.nextInt(3)) {
                case 0:
                    sendMails();
                    break;
                case 1:
                    sendLogs();
                    break;
                case 2:
                    sendErrors();
                    break;
            }
        }
    }

    private void sendErrors() throws IOException, TimeoutException {
        System.out.println(" [2] Sent 'Errors'");
        Channel channelError = connection.createChannel();

        JsonObject jo = Json.createObjectBuilder()
                .add("Type", "Erreur")
                .add("Date", (new Date()).toString())
                .add("Body", faker.chuckNorris().fact())
                .build();
        channelError.basicPublish("", QUEUE_ERROR, null, jo.toString().getBytes());

        channelError.close();
    }

    private void sendLogs() throws IOException, TimeoutException {
        System.out.println(" [1] Sent 'Logs'");
        Channel channelLog = connection.createChannel();

        JsonObject jo = Json.createObjectBuilder()
                .add("Type", "Information/Avertissement")
                .add("Date", (new Date()).toString())
                .add("Body", faker.chuckNorris().fact())
                .build();
        channelLog.basicPublish("", QUEUE_LOG, null, jo.toString().getBytes());

        channelLog.close();
    }

    private void sendMails() throws IOException, TimeoutException {
        System.out.println(" [0] Sent 'Mails'");
        JsonObject jo = Json.createObjectBuilder()
                .add("From", faker.internet().emailAddress())
                .add("To", faker.internet().emailAddress())
                .add("Subject", faker.shakespeare().asYouLikeItQuote())
                .add("Body", faker.chuckNorris().fact())
                .build();
        Channel channelMail = connection.createChannel();

        channelMail.basicPublish("", QUEUE_MAIL, null, jo.toString().getBytes());

        channelMail.close();
    }
}