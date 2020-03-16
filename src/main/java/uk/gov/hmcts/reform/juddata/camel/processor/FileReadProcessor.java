package uk.gov.hmcts.reform.juddata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.BLOBPATH;

@Slf4j
@Component
public class FileReadProcessor implements Processor {

    private static final String COMMA_DELIMITER = ",";

    @Override
    public void process(Exchange exchange) {
        log.info("::FileReadProcessor starts::");
        String blobFilePath = (String) exchange.getProperty(BLOBPATH);
        CamelContext context = exchange.getContext();
        ConsumerTemplate consumer = context.createConsumerTemplate();
        exchange.getMessage().setBody(consumer.receiveBody(blobFilePath, 600000));
        String list= exchange.getIn().getBody(String.class);

        String[] ss = getRecordFromList(list);
        System.out.println("Lissstt "+ss.length);
        log.info("::FileReadProcessor ends::");
    }

    public String[] getRecordFromList(String list) {
        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(list);) {
            while (scanner.hasNextLine()) {
                records.add(getRecordFromLine(scanner.nextLine()));
            }

        }
        return records.get(0).toString().split(",");
    }

    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }

}
