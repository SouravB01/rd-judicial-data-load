package uk.gov.hmcts.reform.juddata.camel.util;

import org.springframework.stereotype.Component;

@Component
public class ValidationUtilsImpl implements ValidationUtils {


    @Override
    public String valid(Class validateClass, Object object) {
        int  size= validateClass.getDeclaredFields().length;
        return null;
    }
}
