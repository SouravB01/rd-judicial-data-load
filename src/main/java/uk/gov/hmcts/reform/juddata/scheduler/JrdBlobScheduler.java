package uk.gov.hmcts.reform.juddata.scheduler;

import java.io.IOException;
import java.security.NoSuchProviderException;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.orchestrator.BlobStoreOrchestrator;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Component
public class JrdBlobScheduler {

    @Autowired
    BlobStoreOrchestrator blobStoreOrchestrator;

    @Scheduled(cron = "${scheduler.config}")
    public void runBlobScheduler() throws IOException, NoSuchProviderException {
        /*try {
            blobStoreOrchestrator.execute();
        } catch (InvalidKeyException e) {
            log.error("Invalid blob store credentials",  e);
        }*/
    }
}
