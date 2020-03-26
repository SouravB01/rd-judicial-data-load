
                                                                                                                                                                                                                                                                                                                                
package uk.gov.hmcts.reform.juddata.camel.processor;

    import static org.mockito.Mockito.mock;
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

public class ExceptionProcessorTest {

    private uk.gov.hmcts.reform.juddata.camel.processor.ExceptionProcessor exceptionProcessorUnderTest;

@Before
public void setUp() {
                                exceptionProcessorUnderTest = new ExceptionProcessor() ;
                exceptionProcessorUnderTest.emailService =  mock(uk.gov.hmcts.reform.juddata.camel.util.EmailService.class) ;
                exceptionProcessorUnderTest.jrdUtility =  mock(uk.gov.hmcts.reform.juddata.camel.util.JrdUtility.class) ;
}
                
    @Test     public void testProcess() throws Exception {
    // Setup
                final org.apache.camel.Exchange exchange = null;

    // Run the test
 exceptionProcessorUnderTest.process(exchange);

        // Verify the results
        verify(exceptionProcessorUnderTest.jrdUtility).schedularAuditUpdate(any(org.apache.camel.Exchange.class));
        verify(exceptionProcessorUnderTest.emailService).sendMail("to","subject","body");
    }
                                                                }

