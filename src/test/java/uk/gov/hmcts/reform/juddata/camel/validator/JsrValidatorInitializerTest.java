package uk.gov.hmcts.reform.juddata.camel.validator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.juddata.camel.helper.JrdTestSupport.createJudicialUserProfileMock;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.SCHEDULER_NAME;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.SCHEDULER_START_TIME;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.reform.juddata.camel.binder.JudicialUserProfile;
import uk.gov.hmcts.reform.juddata.camel.route.beans.RouteProperties;

public class JsrValidatorInitializerTest {

    static JsrValidatorInitializer<JudicialUserProfile> judicialUserProfileJsrValidatorInitializer = new JsrValidatorInitializer<>();

    @BeforeClass
    public static void beforeAll() throws Exception {
        judicialUserProfileJsrValidatorInitializer.initializeFactory();
    }

    @Test
    public void testValidate() {
        List<JudicialUserProfile> judicialUserProfiles = new ArrayList<>();
        Date currentDate = new Date();
        LocalDateTime dateTime = LocalDateTime.now();
        JudicialUserProfile profile = createJudicialUserProfileMock(currentDate, dateTime);
        judicialUserProfiles.add(profile);
        JsrValidatorInitializer<JudicialUserProfile> judicialUserProfileJsrValidatorInitializerSpy = spy(judicialUserProfileJsrValidatorInitializer);
        judicialUserProfileJsrValidatorInitializerSpy.validate(judicialUserProfiles);
        verify(judicialUserProfileJsrValidatorInitializerSpy, times(1)).validate(any());
    }

    @Test
    public void testAuditJsrExceptions() {
        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setTableName("test");
        routeProperties.setIsMainRoute(true);
        when(exchange.getIn()).thenReturn(message);
        CamelContext camelContext = new DefaultCamelContext();
        Map<String, String> map = new HashMap<>();
        map.put(SCHEDULER_START_TIME, String.valueOf(new Date().getTime()));
        map.put(SCHEDULER_NAME,"test");
        camelContext.setGlobalOptions(map);

        when(message.getHeader(ROUTE_DETAILS)).thenReturn(routeProperties);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        setField(judicialUserProfileJsrValidatorInitializer, "jdbcTemplate", jdbcTemplate);
        setField(judicialUserProfileJsrValidatorInitializer, "camelContext", camelContext);
        setField(judicialUserProfileJsrValidatorInitializer, "jsrThresholdLimit", 5);

        JsrValidatorInitializer<JudicialUserProfile> judicialUserProfileJsrValidatorInitializerSpy = spy(judicialUserProfileJsrValidatorInitializer);

        List<JudicialUserProfile> judicialUserProfiles = new ArrayList<>();
        Date currentDate = new Date();
        LocalDateTime dateTime = LocalDateTime.now();
        JudicialUserProfile profile = createJudicialUserProfileMock(currentDate, dateTime);
        profile.setSurName(null);
        judicialUserProfiles.add(profile);
        judicialUserProfileJsrValidatorInitializerSpy.validate(judicialUserProfiles);
        int[][] intArray = new int[1][];
        when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(), any())).thenReturn(intArray);

        judicialUserProfileJsrValidatorInitializerSpy.initializeJsrExceptions(exchange);
        verify(judicialUserProfileJsrValidatorInitializerSpy, times(1)).initializeJsrExceptions(any());

        judicialUserProfileJsrValidatorInitializerSpy.auditJsrExceptions(true);
        verify(judicialUserProfileJsrValidatorInitializerSpy, times(1)).auditJsrExceptions(true);
    }
}
