package de.starwit;

import org.apache.camel.builder.RouteBuilder;

public class DynamicRoute extends RouteBuilder {

    String inqueue = "inbox";
    String selectorField = "schoolId";

    public DynamicRoute(String inqueue, String selectorField) {
        super();
        this.inqueue = inqueue;
        this.selectorField = selectorField;
    }

    public void configure() throws Exception {
        from("activemq:queue:" + inqueue)
                .routeId("schoolIdDynamicRouter")
                .log("Received message: ${body}")
                // optional: convert from JSON to a Map or POJO
                .unmarshal().json() // converts to Map by default

                // Use toD() for dynamic routing
                .toD("activemq:queue:${body[" + selectorField + "]}")
                .log("Routed to queue: ${body[schoolId]}");
    }

}
