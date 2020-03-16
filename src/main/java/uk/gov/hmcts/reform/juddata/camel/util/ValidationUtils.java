package uk.gov.hmcts.reform.juddata.camel.util;

import org.springframework.stereotype.Component;

@Component
public interface ValidationUtils {
    public int valid(Class validateClass, String object) throws NoSuchFieldException;
}
