package uk.gov.hmcts.reform.juddata.camel.validator;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.SCHEDULER_NAME;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.SCHEDULER_START_TIME;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.juddata.camel.route.beans.JsrAuditRow;
import uk.gov.hmcts.reform.juddata.camel.route.beans.RouteProperties;

@Component
@Slf4j
public class JsrValidatorInitializer<T> {

    private Validator validator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${invalid-header-sql}")
    String invalidHeaderSql;

    @Autowired
    CamelContext camelContext;

    private Set<ConstraintViolation<T>> constraintViolations;

    private List<JsrAuditRow> jsrAuditRows;

    @Value("${invalid-jsr-sql}")
    String invalidJsrSql;

    @Value("${jsr-threshold-limit}")
    int jsrThresholdLimit;

    @PostConstruct
    public void initializeFactory() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        jsrAuditRows = new ArrayList<>();
    }

    /**
     * JSR validation.
     *
     * @param binders List
     * @return List binder list
     */
    public List<T> validate(List<T> binders) {

        log.info("::::JsrValidatorInitializer data processing validate starts::::");

        this.constraintViolations = new LinkedHashSet<>();

        List<T> binderFilter = new ArrayList<>();

        for (T binder : binders) {
            Set<ConstraintViolation<T>> constraintViolations = validator.validate(binder);
            if (constraintViolations.size() == 0) {
                binderFilter.add(binder);
            }

            this.constraintViolations.addAll(constraintViolations);
        }
        log.info("::::JsrValidatorInitializer data processing validate complete::::");
        return binderFilter;
    }

    /**
     * Intializing JSR Exception.
     *
     * @param exchange Exchange
     */
    public void initializeJsrExceptions(Exchange exchange) {

        log.info("::::JsrValidatorInitializer data processing audit start::::");
        RouteProperties routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        String schedulerTime = camelContext.getGlobalOptions().get(SCHEDULER_START_TIME);

        List<ConstraintViolation<T>> violationList = constraintViolations.stream().limit(jsrThresholdLimit)
                .collect(Collectors.toList());

        for (ConstraintViolation violation : violationList) {
            JsrAuditRow jsrAuditRow = JsrAuditRow.builder()
                    .tableName(routeProperties.getTableName())
                    .scheduledTime(new Timestamp(Long.valueOf(schedulerTime)))
                    .schedulerName(camelContext.getGlobalOptions().get(SCHEDULER_NAME))
                    .keyField(getKeyFiled(violation.getRootBean()))
                    .invalidField(violation.getPropertyPath().toString())
                    .message(violation.getMessage())
                    .currentTime(new Timestamp(new Date().getTime()))
                    .isMainRoute(routeProperties.getIsMainRoute())
                    .build();
            jsrAuditRows.add(jsrAuditRow);
        }
    }

    /**
     * Intializing JSR Exception.
     *
     * @param isMainRoute boolean
     */
    public void auditJsrExceptions(boolean isMainRoute) {

        List<JsrAuditRow> auditRows = jsrAuditRows.stream().filter(row -> row.getIsMainRoute().equals(isMainRoute))
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(
                invalidJsrSql,
                auditRows,
                10,
                new ParameterizedPreparedStatementSetter<JsrAuditRow>() {
                    public void setValues(PreparedStatement ps, JsrAuditRow argument) throws SQLException {
                        ps.setString(1, argument.getTableName());
                        ps.setTimestamp(2, argument.getScheduledTime());
                        ps.setString(3, argument.getSchedulerName());
                        ps.setString(4, argument.getKeyField());
                        ps.setString(5, argument.getInvalidField());
                        ps.setString(6, argument.getMessage());
                        ps.setTimestamp(7, argument.getCurrentTime());
                    }
                });
    }

    /**
     * get key fields.
     *
     * @param bean Object
     * @return String
     */
    private String getKeyFiled(Object bean) {
        Class objectClass = bean.getClass();
        try {
            for (Field field : objectClass.getDeclaredFields()) {

                DataField dataField = AnnotationUtils.findAnnotation(field,
                        DataField.class);
                if (dataField.pos() == 1) {
                    field.setAccessible(TRUE);
                    return (String) field.get(bean);
                }
            }
        } catch (IllegalAccessException ex) {
            throw new RouteFailedException("JSR auditing failed getting auditing key values");
        }
        return "";
    }

    public Set<ConstraintViolation<T>> getConstraintViolations() {
        return constraintViolations;
    }

    public List<JsrAuditRow> getJsrAuditRows() {
        return jsrAuditRows;
    }
}

