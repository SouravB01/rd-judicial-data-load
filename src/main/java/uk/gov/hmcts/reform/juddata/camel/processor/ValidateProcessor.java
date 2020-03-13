package uk.gov.hmcts.reform.juddata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.List;
@Component
@Slf4j
public class ValidateProcessor implements Processor  {
    /**
     * Processes the message exchange
     *
     * @param exchange the message exchange
     * @throws Exception if an internal processing error has occurred.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) throws Exception {
        Message in =exchange.getIn();
        Object body=exchange.getIn().getBody();
        System.out.println("Data "+body);
                List<List<String>> data = (List<List<String>>) body;
        System.out.println("data   *****/n "+data);
                List<String> headerLine = data.remove(0);
                System.out.println("header: "+headerLine);
                System.out.println("total lines: "+data.size());

                // iterate over each line
                for( List<String> line : data) {
                    System.out.println("Total columns: "+line.size());
                    System.out.println(line.get(0)); // first column
                }
                exchange.setMessage(in);
    }
}
