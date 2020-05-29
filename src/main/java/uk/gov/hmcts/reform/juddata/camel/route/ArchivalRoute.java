package uk.gov.hmcts.reform.juddata.camel.route;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.juddata.camel.processor.ArchiveAzureFileProcessor;
import uk.gov.hmcts.reform.juddata.camel.processor.ExceptionProcessor;

@Component
public class ArchivalRoute {

    @Autowired
    CamelContext camelContext;

    @Autowired
    ExceptionProcessor exceptionProcessor;

    @Autowired
    ArchiveAzureFileProcessor archiveAzureFileProcessor;

    @Value("${archival-path}")
    String archivalPath;


    @Value("${archival-cred}")
    String archivalCred;

    @Value("${archival-route}")
    String archivalRoute;

    public void archivalRoute(List<String> archivalFiles) {
        try {

            camelContext.addRoutes(
                    new SpringRouteBuilder() {
                        @Override
                        public void configure() throws Exception {

                            onException(Exception.class)
                                    .handled(true)
                                    .process(exceptionProcessor)
                                    .end();

                            from(archivalRoute)
                                    .loop(archivalFiles.size()).copy()
                                    .process(archiveAzureFileProcessor)
                                    .toD(archivalPath + "${header.filename}?" + archivalCred)
                                    .end()
                                    .end();
                        }
                });
        } catch (Exception ex) {
            throw new RouteFailedException(" Data Load - failed for archival ");
        }
    }
}
