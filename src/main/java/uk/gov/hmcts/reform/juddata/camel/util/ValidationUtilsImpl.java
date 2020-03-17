package uk.gov.hmcts.reform.juddata.camel.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class ValidationUtilsImpl implements ValidationUtils {

    private static final String COMMA_DELIMITER = ",";

    @Override
    public int valid(Class validateClass, String object) throws NoSuchFieldException {
        int size = validateClass.getDeclaredFields().length;
        System.out.println(validateClass.getDeclaredFields().length+"Tesssst"+(getRecordFromList(object).length+1));
        if (validateClass.getDeclaredFields().length == getRecordFromList(object).length+1){
            System.out.println("SUCESSS " + validateClass.getName());
        }
        return size;
    }


    public String[] getRecordFromList(String list) {
        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(list);) {
            while (scanner.hasNextLine()) {
                records.add(getRecordFromLine(scanner.nextLine()));
            }

        }
        return records.get(0).toString().split(",");
    }

    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }


//    @SuppressWarnings({"unchecked"})
//    public   <T extends  List>  List getJudicialUserProfiles(Exchange exchange) {
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


}
