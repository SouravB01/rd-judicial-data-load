package uk.gov.hmcts.reform.juddata.camel.mapper;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.juddata.camel.helper.JrdUnitTestHelper.createJudicialUserProfileMock;

import java.time.LocalDate;
import java.util.Map;
import org.junit.Test;
import uk.gov.hmcts.reform.juddata.camel.binder.JudicialUserProfile;

public class JudicialUserProfileRowMapperTest {
    @Test
    public void should_return_JudicialUserProfileRow_response() {

        LocalDate currentDate = LocalDate.now();

        JudicialUserProfile judicialUserProfileMock = createJudicialUserProfileMock(currentDate);

        JudicialUserProfileRowMapper judicialUserProfileRowMapper = new JudicialUserProfileRowMapper();
        Map<String, Object> response = judicialUserProfileRowMapper.getMap(judicialUserProfileMock);

        assertEquals("elinksid_1", response.get("elinks_id"));
        assertEquals("personalCode_1", response.get("personal_code"));
        assertEquals("title", response.get("title"));
        assertEquals("knownAs", response.get("known_as"));
        assertEquals("surname", response.get("surname"));
        assertEquals("fullName", response.get("full_name"));
        assertEquals("postNominals", response.get("post_nominals"));
        assertEquals("contractTypeId", response.get("contract_type"));
        assertEquals("workpatterns", response.get("work_pattern"));
        assertEquals("some@hmcts.net", response.get("email_id"));
        assertEquals(currentDate, response.get("joining_date"));
        assertEquals(currentDate, response.get("last_working_date"));
        assertEquals(true, response.get("active_flag"));
        assertEquals(currentDate.toString(), response.get("extracted_date"));

    }
}
         
         