package uk.gov.hmcts.reform.juddata.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.beans.JudicialUserProfile;

import javax.validation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class JudicialUserProfileProcessor implements Processor {

    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) {
        List<JudicialUserProfile> users = new ArrayList<>();
        List<JudicialUserProfile> judicialUserProfiles;
        String  body12=exchange.getIn().getBody(String.class);
        System.out.println("body12  "+body12);
        if (exchange.getIn().getBody() instanceof List) {
            judicialUserProfiles = (List< JudicialUserProfile>) exchange.getIn().getBody();
        } else {
            JudicialUserProfile judicialUserProfile = (JudicialUserProfile) exchange.getIn().getBody();
            judicialUserProfiles = new ArrayList<>();

            judicialUserProfiles.add(judicialUserProfile);
        }


       // judicialUserProfiles=getJudicialUserProfiles( exchange);

        for (JudicialUserProfile user : judicialUserProfiles) {

            checkConstraints(user);
            JudicialUserProfile validUser = fetch(user);
            if (null != validUser) {

                users.add(user);
            } else {
                log.info(" Invalid JudicialUser record ");
            }
            exchange.getMessage().setBody(users);
        }
        log.info("::JudicialUserProfile Records count::" + users.size());
        log.info("::JudicialUserProfile Records count::" + users.toString());
    }


    private JudicialUserProfile fetch(@Valid JudicialUserProfile user) {
        JudicialUserProfile userAfterValidation = null;
        if (null != user.getElinksId()) {

            userAfterValidation = user;

        }
        return userAfterValidation;
    }

//    @SuppressWarnings({"unchecked"})
//    public   <T extends  List>  List getJudicialUserProfiles(Exchange  exchange) {
//        List<T > judicialUserProfiles;
//        if (exchange.getIn().getBody() instanceof List) {
//            judicialUserProfiles = (List<T>) exchange.getIn().getBody();
//        } else {
//            T judicialUserProfile = (T) exchange.getIn().getBody();
//            judicialUserProfiles = new ArrayList<>();
//
//            judicialUserProfiles.add(judicialUserProfile);
//        }
//        return judicialUserProfiles;
//    }

    public static void checkConstraints(JudicialUserProfile judicialUserProfile) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        int  dd= JudicialUserProfile.class.getDeclaredFields().length;
        System.out.println("Length "+dd);
        //It validates bean instances
        Validator validator = factory.getValidator();

        JudicialUserProfile user = new JudicialUserProfile();
        user.setElinksId("11");
        user.setPersonalCode("sushsabtbbt");
        //Validate bean
        Set<ConstraintViolation<JudicialUserProfile>> constraintViolations = validator.validate(judicialUserProfile);

        //Show errors
        if (constraintViolations.size() > 0) {
            for (ConstraintViolation<JudicialUserProfile> violation : constraintViolations) {
                System.out.println("Invalid object"+violation.getMessage());
            }
        } else {
            System.out.println("Valid Object");
        }
    }


}
