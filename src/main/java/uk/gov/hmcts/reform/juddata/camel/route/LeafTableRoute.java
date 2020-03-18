package uk.gov.hmcts.reform.juddata.camel.route;

import static org.apache.commons.lang.WordUtils.uncapitalize;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.BLOBPATH;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.CSVBINDER;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.DIRECT_ROUTE;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.ID;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.INSERT_SQL;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.LEAF_ROUTE;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.LEAF_ROUTE_NAMES;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.MAPPER;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.MAPPING_METHOD;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.PROCESSOR;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.TRUNCATE_SQL;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.model.language.SimpleExpression;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.juddata.camel.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.juddata.camel.processor.FileReadProcessor;
import uk.gov.hmcts.reform.juddata.camel.route.beans.RouteProperties;


@Component
public class LeafTableRoute {

    @Autowired
    FileReadProcessor fileReadProcessor;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    Environment environment;

    @Autowired
    SpringTransactionPolicy springTransactionPolicy;

    @Autowired
    ExceptionProcessor exceptionProcessor;

    @Value("${start-leaf-route}")
    private String startLeafRoute;

    @Autowired
    CamelContext camelContext;

    @SuppressWarnings("unchecked")
    @Transactional
    public void startRoute() throws Exception {

        String leafRouteNames = LEAF_ROUTE_NAMES;

        List<String> leafRoutesList = environment.containsProperty(leafRouteNames)
                ? environment.getProperty(leafRouteNames, List.class) : new ArrayList<>();

        List<RouteProperties> routePropertiesList = getRouteProperties(leafRoutesList);

        camelContext.addRoutes(
                new SpringRouteBuilder() {
                    @Override
                    public void configure() throws Exception {

                        //logging exception in global exception handler
                        onException(Exception.class)
                                .handled(true)
                                .process(exceptionProcessor);

                        String[] directRouteNameList = createDirectRoutesForMulticast(leafRoutesList);

                        //Started direct route with multicast all the configured routes eg.application-jrd-leaf-router.yaml
                        //with Transaction propagation required
                        from(startLeafRoute)
                                .transacted()
                                .policy(springTransactionPolicy)
                                .multicast()
                                .stopOnException().to(directRouteNameList).end();

                        for (RouteProperties route : routePropertiesList) {

                            Expression exp = new SimpleExpression(route.getBlobPath());

                            from(DIRECT_ROUTE + route.getRouteName())
                                    .id(DIRECT_ROUTE + route.getRouteName())
                                    .transacted()
                                    .policy(springTransactionPolicy)
                                    .setProperty(BLOBPATH, exp)
                                    .process(fileReadProcessor).unmarshal().bindy(BindyType.Csv,
                                    applicationContext.getBean(route.getBinder()).getClass())
                                    .to(route.getTruncateSql())
                                    .process((Processor) applicationContext.getBean(route.getProcessor()))
                                    .split().body()
                                    .streaming()
                                    .bean(applicationContext.getBean(route.getMapper()), MAPPING_METHOD)
                                    .doTry()
                                    .to(route.getSql())
                                    .doCatch(Exception.class)
                                    .end();
                        }
                    }
                });
    }


    private String[] createDirectRoutesForMulticast(List<String> routeList) {
        int index = 0;
        String[] directRouteNameList = new String[routeList.size()];
        for (String child : routeList) {
            directRouteNameList[index] = (DIRECT_ROUTE).concat(child);
            index++;
        }
        return directRouteNameList;
    }

    /**
     * Sets Route Properties.
     *
     * @param routes routes
     * @return List RouteProperties.
     */
    private List<RouteProperties> getRouteProperties(List<String> routes) {
        List<RouteProperties> routePropertiesList = new LinkedList<>();
        int index = 0;
        for (String child : routes) {
            RouteProperties properties = new RouteProperties();
            properties.setRouteName(environment.getProperty(
                    LEAF_ROUTE + "." + child + "." + ID));
            properties.setSql(environment.getProperty(
                    LEAF_ROUTE + "." + child + "." + INSERT_SQL));
            properties.setTruncateSql(environment.getProperty(
                    LEAF_ROUTE + "." + child + "." + TRUNCATE_SQL)
                    == null ? "log:test" : environment.getProperty(
                    LEAF_ROUTE + "." + child + "." + TRUNCATE_SQL));
            properties.setBlobPath(environment.getProperty(
                    LEAF_ROUTE + "." + child + "." + BLOBPATH));
            properties.setMapper(uncapitalize(environment.getProperty(
                    LEAF_ROUTE + "." + child + "." + MAPPER)));
            properties.setBinder(uncapitalize(environment.getProperty(LEAF_ROUTE + "."
                    + child + "." + CSVBINDER)));
            properties.setProcessor(uncapitalize(environment.getProperty(LEAF_ROUTE + "."
                    + child + "." + PROCESSOR)));
            routePropertiesList.add(index, properties);
            index++;
        }
        return routePropertiesList;
    }
}


