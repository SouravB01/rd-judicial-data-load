package uk.gov.hmcts.reform.juddata.camel.processor;

import static java.util.Objects.isNull;
import static org.apache.camel.Exchange.EXCEPTION_CAUGHT;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.IS_EXCEPTION_HANDLED;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExceptionProcessor implements Processor {

    @Autowired
    CamelContext camelContext;

    @Override
    public void process(Exchange exchange) throws Exception {

        if (isNull(exchange.getContext().getGlobalOptions().get(IS_EXCEPTION_HANDLED))) {
            Exception exception = (Exception) exchange.getProperty(EXCEPTION_CAUGHT);
            log.error("::::exception in route for data processing::::" + exception);
            exchange.getContext().getGlobalOptions().put(IS_EXCEPTION_HANDLED, Boolean.TRUE.toString());
        }
    }

}
