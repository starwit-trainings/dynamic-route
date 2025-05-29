package de.starwit;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.activemq.ActiveMQConnectionFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
    private static final Logger log = LogManager.getLogger(App.class);

    private static ActiveMQConnectionFactory factory;
    private static Connection connection;
    private static Session session;

    static Properties config = new Properties();

    public static void main(String[] args) {
        loadProperties();

        String sampleDataLocation = config.getProperty("sample-data.location") == null ? "sampledata/studentresults1.json" : config.getProperty("sample-data.location");
        List<StudentResult> studentResults = loadSampleData(sampleDataLocation);

        String brokerURL = config.getProperty("broker.url") == null ? "tcp://localhost:61616" : config.getProperty("broker.url");
        String username = config.getProperty("broker.username") == null ? "admin" : config.getProperty("broker.username");
        String password = config.getProperty("broker.password") == null ? "admin" : config.getProperty("broker.password");

        String inqueue = config.getProperty("inqueue") == null ? "inbox" : config.getProperty("inqueue");

        factory = new ActiveMQConnectionFactory(username, password, brokerURL);
        factory.setClientID("sample-producer-" + factory.toString());

        try {
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue(inqueue));

            for (StudentResult studentResult : studentResults) {
                String message = createMessage(studentResult);
                TextMessage msg = session.createTextMessage(message);
                producer.send(msg);
                log.info("Message send " + msg);
                Thread.sleep(1000);
            }

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            log.error("JMS error - exit " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("Thread problem " + e.getMessage());
        }
    }

    private static String createMessage(StudentResult studentResult) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(studentResult);
        } catch (JsonProcessingException e) {
            log.error("Can't create message " + e.getMessage());
            return null;
        }
    }

    private static List<StudentResult> loadSampleData(String sampleDataLocation) {
        //load student results from json file at sampleDataLocation
        ObjectMapper mapper = new ObjectMapper();
        
        File jsonFile = new File(sampleDataLocation);        
        try {
            InputStream in = new FileInputStream(jsonFile);
            List<StudentResult> studentResults = mapper.readValue(in, mapper.getTypeFactory().constructCollectionType(List.class, StudentResult.class));
            return studentResults;
        } catch (IOException e) {
            log.error("Can't load sample data " + e.getMessage());
            return null;
        }
        
    }

    private static void loadProperties() {
        try (InputStream in = App.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                config.load(in);
            } else {
                log.error("Can't find property file");
                System.exit(1);
            }

        } catch (IOException e) {
            log.error("Can't load property file, exiting " + e.getMessage());
            System.exit(1); // exit with error status
        }
    }
}
