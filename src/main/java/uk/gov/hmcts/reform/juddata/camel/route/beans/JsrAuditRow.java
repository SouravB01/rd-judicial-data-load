package uk.gov.hmcts.reform.juddata.camel.route.beans;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class JsrAuditRow {

    String tableName;

    Timestamp scheduledTime;

    String schedulerName;

    String keyField;

    String invalidField;

    String message;

    Timestamp currentTime;

    String fileName;

    Boolean isMainRoute;
}
