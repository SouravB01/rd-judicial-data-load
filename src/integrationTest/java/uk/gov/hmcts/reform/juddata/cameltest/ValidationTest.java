package uk.gov.hmcts.reform.juddata.cameltest;

import uk.gov.hmcts.reform.juddata.camel.beans.JudicialUserProfile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class ValidationTest
{
    public static void main(String[] args) throws IllegalAccessException {

        checkConstraints();
    }

    public static void checkConstraints() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        int  dd= JudicialUserProfile.class.getDeclaredFields().length;
        System.out.println("Length "+dd);
        //It validates bean instances
        Validator validator = factory.getValidator();

        JudicialUserProfile user = new JudicialUserProfile();
        user.setElinksId("11");
        user.setPersonalCode("sushsabtbbt");
        //Validate bean
        Set<ConstraintViolation<JudicialUserProfile>> constraintViolations = validator.validate(user);

        //Show errors
        if (constraintViolations.size() > 0) {
            for (ConstraintViolation<JudicialUserProfile> violation : constraintViolations) {
                System.out.println(violation.getMessage());
            }
        } else {
            System.out.println("Valid Object");
        }
    }
}
