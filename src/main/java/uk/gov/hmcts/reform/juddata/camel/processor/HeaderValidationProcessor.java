package uk.gov.hmcts.reform.juddata.camel.processor;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.HEADER_EXCEPTION;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.SCHEDULER_NAME;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.SCHEDULER_START_TIME;

import com.opencsv.CSVReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.juddata.camel.route.beans.JsrAuditRow;
import uk.gov.hmcts.reform.juddata.camel.route.beans.RouteProperties;

@Component
public class HeaderValidationProcessor implements Processor {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamelContext camelContext;


    @Value("${invalid-header-sql}")
    String invalidHeaderSql;

    private JsrAuditRow jsrAuditRow;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {

        RouteProperties routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        String csv = exchange.getIn().getBody(String.class);
        CSVReader reader = new CSVReader(new StringReader(csv));
        String[] header = reader.readNext();
        Field[] allFields = applicationContext.getBean(routeProperties.getBinder())
                .getClass().getDeclaredFields();
        List<Field> csvFields = new ArrayList<>();

        for (Field field : allFields) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                csvFields.add(field);
            }
        }

        //Auditing in database if headers are missing
        if (header.length > csvFields.size()) {
            exchange.getIn().setHeader(HEADER_EXCEPTION, HEADER_EXCEPTION);
            String schedulerTime = camelContext.getGlobalOptions().get(SCHEDULER_START_TIME);
            String schedulerName = camelContext.getGlobalOptions().get(SCHEDULER_NAME);

            jsrAuditRow = JsrAuditRow.builder().fileName(routeProperties.getFileName())
                    .scheduledTime(new Timestamp(Long.valueOf(schedulerTime)))
                    .schedulerName(schedulerName)
                    .message("Mismatch headers in csv for ::" + routeProperties.getFileName())
                    .currentTime(new Timestamp(new Date().getTime()))
                    .isMainRoute(routeProperties.getIsMainRoute())
                    .build();
            throw new RouteFailedException("Mismatch headers in csv for ::" + routeProperties.getFileName());
        }

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(Charset.forName("UTF-8")));
        exchange.getMessage().setBody(inputStream);
    }

    public JsrAuditRow getJsrAuditRow() {
        return jsrAuditRow;
    }

    public void auditHeaderException() {
        if (nonNull(jsrAuditRow)) {
            Object[] params = new Object[]{jsrAuditRow.getFileName(), jsrAuditRow.getScheduledTime(),
                    jsrAuditRow.getSchedulerName(), jsrAuditRow.getMessage(), jsrAuditRow.getCurrentTime()};
            jdbcTemplate.update(invalidHeaderSql, params);
        }
    }
}
