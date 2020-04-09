package uk.gov.hmcts.reform.juddata.camel.processor;

import static java.util.Collections.singletonList;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.binder.JudicialRegionType;
import uk.gov.hmcts.reform.juddata.camel.validator.JsrValidatorInitializer;

@Slf4j
@Component
public class JudicialRegionTypeProcessor extends DefaultProcessor<JudicialRegionType> {


    @Autowired
    JsrValidatorInitializer<JudicialRegionType> judicialRegionTypeJsrValidatorInitializer;

    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) throws Exception {

        List<JudicialRegionType> judicialRegionTypes;

        judicialRegionTypes = (exchange.getIn().getBody() instanceof List)
                ? (List<JudicialRegionType>) exchange.getIn().getBody()
                : singletonList((JudicialRegionType) exchange.getIn().getBody());

        log.info("Region type Records count before Validation:: " + judicialRegionTypes.size());
        List<JudicialRegionType> filteredRegionTypes = validate(judicialRegionTypeJsrValidatorInitializer,
                judicialRegionTypes);
        log.info("Region type Records count after Validation:: " + filteredRegionTypes.size());
        audit(judicialRegionTypeJsrValidatorInitializer, exchange);

        exchange.getMessage().setBody(filteredRegionTypes);
    }
}
