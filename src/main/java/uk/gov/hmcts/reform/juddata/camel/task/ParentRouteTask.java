package uk.gov.hmcts.reform.juddata.camel.task;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.IS_EXCEPTION_HANDLED;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.JUDICIAL_USER_PROFILE_ORCHESTRATION;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.SCHEDULER_STATUS;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.exception.RouteFailedException;
import uk.gov.hmcts.reform.juddata.camel.processor.HeaderValidationProcessor;
import uk.gov.hmcts.reform.juddata.camel.route.beans.JsrAuditRow;
import uk.gov.hmcts.reform.juddata.camel.service.AuditProcessingService;
import uk.gov.hmcts.reform.juddata.camel.util.DataLoadUtil;
import uk.gov.hmcts.reform.juddata.camel.validator.JsrValidatorInitializer;

@Component
@Slf4j
public class ParentRouteTask implements Tasklet {

    @Autowired
    ProducerTemplate producerTemplate;

    @Value("${start-route}")
    private String startRoute;

    @Autowired
    CamelContext camelContext;

    @Autowired
    DataLoadUtil dataLoadUtil;

    @Autowired
    AuditProcessingService schedulerAuditProcessingService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    HeaderValidationProcessor headerValidationProcessor;

    @Autowired
    JsrValidatorInitializer jsrValidatorInitializer;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        try {
            camelContext.getGlobalOptions().remove(IS_EXCEPTION_HANDLED);
            camelContext.getGlobalOptions().remove(SCHEDULER_STATUS);
            dataLoadUtil.setGlobalConstant(camelContext, JUDICIAL_USER_PROFILE_ORCHESTRATION);
            producerTemplate.sendBody(startRoute, "starting JRD orchestration");
        } catch (Exception ex) {
            log.error("::judicial-user-profile-orchestration route failed::", ex.getMessage());
            if (ex instanceof RouteFailedException) {
                JsrAuditRow jsrAuditRow = headerValidationProcessor.getJsrAuditRow();
                if (nonNull(jsrAuditRow) && jsrAuditRow.getIsMainRoute()) {
                    headerValidationProcessor.auditHeaderException();
                }
            }
        } finally {
            //runs Job Auditing
            schedulerAuditProcessingService.auditSchedulerStatus(camelContext);
            jsrValidatorInitializer.auditJsrExceptions(true);
        }
        return RepeatStatus.FINISHED;
    }
}
