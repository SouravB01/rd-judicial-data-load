package uk.gov.hmcts.reform.juddata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

@Slf4j
public class JudicialOfficeAuthorisationFileReadProcessor implements Processor {

    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) {
        CamelContext context = exchange.getContext();
        ConsumerTemplate consumer = context.createConsumerTemplate();
        exchange.getOut().setBody(consumer.receiveBody("azure-blob://rddemo/jrdtest/judicial_office_authorisation.csv?"
               + "credentials=#credsreg&operation=updateBlockBlob"));
        log.info(" Judicial Office Authorisation File Read Processor success");
    }
}
