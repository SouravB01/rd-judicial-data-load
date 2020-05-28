package uk.gov.hmcts.reform.juddata.camel.helper;

import uk.gov.hmcts.reform.juddata.camel.route.beans.RouteProperties;

public class JrdTestSupport {

    private JrdTestSupport() {

    }

    public static RouteProperties createRoutePropertiesMock() {

        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setBinder("Binder");
        routeProperties.setBlobPath("Blobpath");
        routeProperties.setChildNames("childNames");
        routeProperties.setMapper("mapper");
        routeProperties.setProcessor("processor");
        routeProperties.setRouteName("routeName");
        routeProperties.setSql("sql");
        routeProperties.setTruncateSql("truncateSql");
        return routeProperties;
    }

}
