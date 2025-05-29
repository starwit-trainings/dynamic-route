package de.starwit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.jms.ConnectionFactory;

public class App {

    private static final Logger log = LogManager.getLogger(App.class);

    private static Properties config = new Properties();

    public static void main(String... args) throws Exception {

        loadConfig();
        
        // configure and add ActiveMQ connection
        String brokerURL = config.getProperty("broker.url") == null ? "tcp://localhost:61616" : config.getProperty("broker.url");
        String username = config.getProperty("broker.username") == null ? "admin" : config.getProperty("broker.username");
        String password = config.getProperty("broker.password") == null ? "admin" : config.getProperty("broker.password");

        CamelContext context = new DefaultCamelContext();
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(username, password, brokerURL);
        context.addComponent("activemq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        // configure & adding dynamic route
        String inqueue = config.getProperty("inqueue") == null ? "inbox" : config.getProperty("inqueue");
        String selectorField = config.getProperty("selector-field") == null ? "schoolId" : config.getProperty("selector-field");
        context.addRoutes(new DynamicRoute(inqueue, selectorField));

        // Register shutdown hook for graceful stop
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Shutting down Camel context...");
                context.stop();
                context.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));        

        context.start();

        log.info("Camel context started. Press Ctrl+C to exit.");

        // Block the main thread until interrupted
        Thread.currentThread().join();
    }

    private static void loadConfig() {
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

