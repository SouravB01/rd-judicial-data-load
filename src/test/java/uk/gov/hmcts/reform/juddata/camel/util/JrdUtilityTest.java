
                                                                                                                                                                                                                                                                                
package uk.gov.hmcts.reform.juddata.camel.util;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.ArgumentMatchers.anyInt;
    import static org.mockito.ArgumentMatchers.anyString;
    import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.mockito.stubbing.Answer;

public class JrdUtilityTest {

 @Mock         private org.springframework.jdbc.core.JdbcTemplate mockJdbcTemplate;

 @InjectMocks     private uk.gov.hmcts.reform.juddata.camel.util.JrdUtility jrdUtilityUnderTest;

@Before
public void setUp() {
 initMocks(this);
                     }
                
    @Test     public void testSchedularAuditUpdate() throws Exception {
    // Setup
                final org.apache.camel.Exchange exchange = null;
                            when( mockJdbcTemplate .update("sql","args")).thenReturn( 0 );

    // Run the test
 jrdUtilityUnderTest.schedularAuditUpdate(exchange);

        // Verify the results
    }
                                                
    @Test     public void testSchedularAuditUpdate_JdbcTemplateThrowsDataAccessException() throws Exception {
    // Setup
                final org.apache.camel.Exchange exchange = null;
        when( mockJdbcTemplate .update("sql","args")).thenThrow(org.springframework.dao.DataAccessException.class);

    // Run the test
 jrdUtilityUnderTest.schedularAuditUpdate(exchange);

        // Verify the results
    }
                
    @Test     public void testGetSchedulerHeader() throws Exception {
    // Setup
                final java.time.Instant SchedulerStartTime = java.time.Instant.ofEpochSecond(0L);

    // Run the test
 final java.util.Map<java.lang.String,java.lang.Object> result =  JrdUtility.getSchedulerHeader("scheduler_Name",SchedulerStartTime);

        // Verify the results
    }
                                                                }

