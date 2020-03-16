package uk.gov.hmcts.reform.juddata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.beans.JudicialOfficeAppointment;
import uk.gov.hmcts.reform.juddata.camel.beans.JudicialUserProfile;
import uk.gov.hmcts.reform.juddata.camel.util.ValidationUtils;
import uk.gov.hmcts.reform.juddata.camel.util.ValidationUtilsImpl;

import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.BLOBPATH;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.CSVBINDER;

@Slf4j
@Component
public class FileReadProcessor implements Processor {

    private static final String COMMA_DELIMITER = ",";


    ValidationUtils validationUtils=new ValidationUtilsImpl();

    @Override
    public void process(Exchange exchange) throws NoSuchFieldException {
        log.info("::FileReadProcessor starts::");
        String blobFilePath = (String) exchange.getProperty(BLOBPATH);
        CamelContext context = exchange.getContext();
        ConsumerTemplate consumer = context.createConsumerTemplate();
        exchange.getMessage().setBody(consumer.receiveBody(blobFilePath, 600000));
        String list= exchange.getIn().getBody(String.class);
        checkBinder(exchange, list);
        log.info("::FileReadProcessor ends::");
    }

    public void checkBinder(Exchange exchange, String list) throws NoSuchFieldException {

        switch(exchange.getProperty(CSVBINDER).toString())
        {
            case "judicialUserProfile":
                System.out.println("judicialUserProfile");
                validationUtils.valid(JudicialUserProfile.class , list);
                break;
            case "judicialOfficeAppointment":
                System.out.println("judicialOfficeAppointment");
                validationUtils.valid(JudicialOfficeAppointment.class , list);
                break;
            default:
                System.out.println("no match");
        }
    }


}
