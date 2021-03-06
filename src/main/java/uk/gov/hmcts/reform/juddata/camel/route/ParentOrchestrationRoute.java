package uk.gov.hmcts.reform.juddata.camel.route;

import static java.util.Arrays.copyOf;
import static org.apache.commons.lang.WordUtils.uncapitalize;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.BLOBPATH;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.CHILD_ROUTES;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.CSVBINDER;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.DIRECT_ROUTE;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.FILE_NAME;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.ID;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.INSERT_SQL;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.MAPPER;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.MAPPING_METHOD;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.ORCHESTRATED_ROUTE;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.PROCESSOR;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.ROUTE;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.TABLE_NAME;
import static uk.gov.hmcts.reform.juddata.camel.util.MappingConstants.TRUNCATE_SQL;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.FailedToCreateRouteException;
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
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.juddata.camel.processor.ArchiveAzureFileProcessor;
import uk.gov.hmcts.reform.juddata.camel.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.juddata.camel.processor.FileReadProcessor;
import uk.gov.hmcts.reform.juddata.camel.processor.HeaderValidationProcessor;
import uk.gov.hmcts.reform.juddata.camel.route.beans.RouteProperties;
import uk.gov.hmcts.reform.juddata.camel.service.EmailService;

/**
 * This class is Judicial User Profile Router Triggers Orchestrated data loading.
 */
@Component
public class ParentOrchestrationRoute {

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

    @Value("${start-route}")
    private String startRoute;

    @Value("${scheduler-name}")
    private String schedulerName;

    @Autowired
    CamelContext camelContext;

    @Value("${archival-path}")
    String archivalPath;

    @Value("${archival-file-names}")
    List<String> archivalFileNames;

    @Autowired
    ArchiveAzureFileProcessor azureFileProcessor;

    @Value("${archival-route}")
    String archivalRoute;

    @Value("${archival-cred}")
    String archivalCred;

    @Autowired
    HeaderValidationProcessor headerValidationProcessor;

    @Autowired
    EmailService emailService;


    @SuppressWarnings("unchecked")
    @Transactional("txManager")
    public void startRoute() throws FailedToCreateRouteException {

        String parentRouteName = camelContext.getGlobalOptions().get(ORCHESTRATED_ROUTE);
        String childNames = ROUTE + "." + parentRouteName + "." + CHILD_ROUTES;

        List<String> dependantRoutes = environment.containsProperty(childNames)
                ? environment.getProperty(childNames, List.class) : new ArrayList<>();
        dependantRoutes.add(0, parentRouteName);

        List<RouteProperties> routePropertiesList = getRouteProperties(dependantRoutes);

        try {
            camelContext.addRoutes(
                    new SpringRouteBuilder() {
                        @Override
                        public void configure() throws Exception {

                            onException(Exception.class)
                                    .handled(true)
                                    .process(exceptionProcessor)
                                    .process(emailService)
                                    .markRollbackOnly()
                                    .end();

                            String[] directChild = new String[dependantRoutes.size()];

                            getDependents(directChild, dependantRoutes);
                            directChild = copyOf(directChild, directChild.length + 1);
                            //add last child route as  archival
                            directChild[directChild.length - 1] = archivalRoute;

                            //Started direct route with multicast all the configured routes eg.application-jrd-router.yaml
                            //with Transaction propagation required
                            from(startRoute)
                                    .transacted()
                                    .policy(springTransactionPolicy)
                                    .multicast()
                                    .stopOnException()
                                    .to(directChild).end();

                            //Archive Blob files
                            from(archivalRoute)
                                    .loop(archivalFileNames.size()).copy()
                                    .process(azureFileProcessor)
                                    .toD(archivalPath + "${header.filename}?" + archivalCred)
                                    .end()
                                    .end();


                            for (RouteProperties route : routePropertiesList) {

                                Expression exp = new SimpleExpression(route.getBlobPath());

                                from(DIRECT_ROUTE + route.getRouteName()).id(DIRECT_ROUTE + route.getRouteName())
                                        .transacted()
                                        .policy(springTransactionPolicy)
                                        .setHeader(ROUTE_DETAILS, () -> route)
                                        .setProperty(BLOBPATH, exp)
                                        .process(fileReadProcessor)
                                        .process(headerValidationProcessor)
                                        .split(body()).unmarshal().bindy(BindyType.Csv,
                                        applicationContext.getBean(route.getBinder()).getClass())
                                        .to(route.getTruncateSql())
                                        .process((Processor) applicationContext.getBean(route.getProcessor()))
                                        .split().body()
                                        .streaming()
                                        .bean(applicationContext.getBean(route.getMapper()), MAPPING_METHOD)
                                        .to(route.getSql())
                                        .end();
                            }
                        }
                    });
        } catch (Exception ex) {
            throw new FailedToCreateRouteException("Judicial Data Load - ParentOrchestrationRoute failed to start", startRoute, startRoute, ex);
        }
    }


    private void getDependents(String[] directChild, List<String> dependents) {
        int index = 0;
        for (String child : dependents) {
            directChild[index] = (DIRECT_ROUTE).concat(child);
            index++;
        }
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
            //only applicable for parent
            properties.setChildNames(environment.getProperty(
                    ROUTE + "." + child + "." + CHILD_ROUTES));
            properties.setRouteName(environment.getProperty(
                    ROUTE + "." + child + "." + ID));
            properties.setSql(environment.getProperty(
                    ROUTE + "." + child + "." + INSERT_SQL));
            properties.setTruncateSql(environment.getProperty(
                    ROUTE + "." + child + "." + TRUNCATE_SQL)
                    == null ? "log:test" : environment.getProperty(
                    ROUTE + "." + child + "." + TRUNCATE_SQL));
            properties.setBlobPath(environment.getProperty(
                    ROUTE + "." + child + "." + BLOBPATH));
            properties.setMapper(uncapitalize(environment.getProperty(
                    ROUTE + "." + child + "." + MAPPER)));
            properties.setBinder(uncapitalize(environment.getProperty(ROUTE + "."
                    + child + "." + CSVBINDER)));
            properties.setProcessor(uncapitalize(environment.getProperty(ROUTE + "."
                    + child + "." + PROCESSOR)));
            properties.setFileName(environment.getProperty(
                    ROUTE + "." + child + "." + FILE_NAME));
            properties.setTableName(environment.getProperty(
                    ROUTE + "." + child + "." + TABLE_NAME));
            routePropertiesList.add(index, properties);
            index++;
        }
        return routePropertiesList;
    }
}
