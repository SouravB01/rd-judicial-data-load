
                                                                                                                                                                                                                                                
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

public class EmailServiceTest {

 @Mock         private org.springframework.mail.javamail.JavaMailSender mockMailSender;

 @InjectMocks     private uk.gov.hmcts.reform.juddata.camel.util.EmailService emailServiceUnderTest;

@Before
public void setUp() {
 initMocks(this);
                     }
                
    @Test     public void testSendMail() throws Exception {
    // Setup
        
    // Run the test
 emailServiceUnderTest.sendMail("to","subject","body");

        // Verify the results
        verify( mockMailSender ).send(new org.springframework.mail.SimpleMailMessage());
    }
                                                
    @Test     public void testSendMail_JavaMailSenderThrowsMailException() throws Exception {
    // Setup
                doThrow(org.springframework.mail.MailException.class).when( mockMailSender ).send(new org.springframework.mail.SimpleMailMessage());

    // Run the test
 emailServiceUnderTest.sendMail("to","subject","body");

        // Verify the results
    }
}

