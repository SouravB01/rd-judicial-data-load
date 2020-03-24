package uk.gov.hmcts.reform.juddata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.route.beans.Scheduler_Audit;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
public class Scheduler_AuditProcessor implements Processor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int seqNumber = 0;

    @Value("${Scheduler-insert-jrd-sql}")
    private String schedulerInsertJrdSql;

    /**
     * Processes the message exchange
     *
     * @param exchange the message exchange
     * @throws Exception if an internal processing error has occurred.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) throws Exception {
        KeyHolder holder = new GeneratedKeyHolder();
          String schedulerName= (String) exchange.getIn().getHeader("SchedulerName");

        System.out.println("started for header "+schedulerName);
        Map<String, Object> headers = exchange.getIn().getHeaders();
        Scheduler_Audit scheduler_audit = new Scheduler_Audit();
        Instant start_time = (Instant)exchange.getIn().getHeader("SchedulerStartTime");
       Instant endTime=Instant.now();
        System.out.println("start time "+start_time+" Endtime "+endTime  + "schedular Name" + schedulerName);
        long time_elapsed= Duration.between(start_time,Instant.now()).toMillis();
        jdbcTemplate.update(schedulerInsertJrdSql, generateId(),schedulerName,start_time.toString(),endTime.toString(),"Sucess");
        System.out.println("time_elapsed "+ time_elapsed +"SQL  Done  "+schedulerInsertJrdSql);
    }

    private int generateId() {
        seqNumber = seqNumber + 1;
        return seqNumber;
    }
}
