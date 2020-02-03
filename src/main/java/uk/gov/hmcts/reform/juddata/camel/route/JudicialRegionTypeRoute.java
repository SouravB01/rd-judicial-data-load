package uk.gov.hmcts.reform.juddata.camel.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.mapper.JudicialRegionTypeRowMapper;


@Component
public class JudicialRegionTypeRoute extends RouteBuilder {

    @Autowired
    JudicialRegionTypeRowMapper judicialRegionTypeRowMapper;

    @Override
    public void configure() throws Exception {

       /*from("azure-blob://rddemo/jrdtest/region_type.csv?credentials=#credsreg&operation=updateBlockBlob")
                .id("role-route")
               //.startupOrder(7)
                .to("file://blobdirectory5?noop=true&fileExist=Override").end();

        from("file://blobdirectory5?noop=true&fileExist=Override")
               // .startupOrder(8)
                .unmarshal() .bindy(BindyType.Csv, JudicialRegionType.class)
                .process(new JudicialRegionTypeProcessor())
                .split().body()
                .bean(judicialRegionTypeRowMapper , "getMap")
                .to("sql:insert into region_type (region_id,region_desc_en,region_desc_cy) values(:#region_id,:#region_desc_en,:#region_desc_cy)?dataSource=dataSource")
                .to("log:test?showAll=true")
                .end();*/
    }

}