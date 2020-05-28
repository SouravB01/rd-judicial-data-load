package uk.gov.hmcts.reform.juddata.camel.validator;

public class JsrValidatorInitializerTest {

    //static JsrValidatorInitializer<JudicialUserProfile> judicialUserProfileJsrValidatorInitializer = new JsrValidatorInitializer<>();

    //    @BeforeClass
    //    public static void beforeAll() throws Exception {
    //        judicialUserProfileJsrValidatorInitializer.initializeFactory();
    //    }
    //
    //    @Test
    //    public void testValidate() {
    //        List<JudicialUserProfile> judicialUserProfiles = new ArrayList<>();
    //        Date currentDate = new Date();
    //        LocalDateTime dateTime = LocalDateTime.now();
    //        JudicialUserProfile profile = createJudicialUserProfileMock(currentDate, dateTime);
    //        judicialUserProfiles.add(profile);
    //        JsrValidatorInitializer<JudicialUserProfile> judicialUserProfileJsrValidatorInitializerSpy = spy(judicialUserProfileJsrValidatorInitializer);
    //        judicialUserProfileJsrValidatorInitializerSpy.validate(judicialUserProfiles);
    //        verify(judicialUserProfileJsrValidatorInitializerSpy, times(1)).validate(any());
    //    }
    //
    //    @Test
    //    public void testAuditJsrExceptions() {
    //        Exchange exchange = mock(Exchange.class);
    //        Message message = mock(Message.class);
    //        RouteProperties routeProperties = new RouteProperties();
    //        routeProperties.setTableName("test");
    //        when(exchange.getIn()).thenReturn(message);
    //        CamelContext camelContext = new DefaultCamelContext();
    //        Map<String, String> map = new HashMap<>();
    //        map.put(SCHEDULER_START_TIME, String.valueOf(new Date().getTime()));
    //        camelContext.setGlobalOptions(map);
    //
    //        when(message.getHeader(ROUTE_DETAILS)).thenReturn(routeProperties);
    //        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    //        final PlatformTransactionManager platformTransactionManager = mock(PlatformTransactionManager.class);
    //        final TransactionStatus transactionStatus = mock(TransactionStatus.class);
    //        setField(judicialUserProfileJsrValidatorInitializer, "platformTransactionManager", platformTransactionManager);
    //        setField(judicialUserProfileJsrValidatorInitializer, "jdbcTemplate", jdbcTemplate);
    //        setField(judicialUserProfileJsrValidatorInitializer, "camelContext", camelContext);
    //        setField(judicialUserProfileJsrValidatorInitializer, "jsrThresholdLimit", 5);
    //
    //        JsrValidatorInitializer<JudicialUserProfile> judicialUserProfileJsrValidatorInitializerSpy = spy(judicialUserProfileJsrValidatorInitializer);
    //
    //        List<JudicialUserProfile> judicialUserProfiles = new ArrayList<>();
    //        Date currentDate = new Date();
    //        LocalDateTime dateTime = LocalDateTime.now();
    //        JudicialUserProfile profile = createJudicialUserProfileMock(currentDate, dateTime);
    //        profile.setSurName(null);
    //        judicialUserProfiles.add(profile);
    //        judicialUserProfileJsrValidatorInitializerSpy.validate(judicialUserProfiles);
    //        int[][] intArray = new int[1][];
    //        when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(), any())).thenReturn(intArray);
    //        when(platformTransactionManager.getTransaction(any())).thenReturn(transactionStatus);
    //        doNothing().when(platformTransactionManager).commit(transactionStatus);
    //
    //        judicialUserProfileJsrValidatorInitializerSpy.auditJsrExceptions(exchange);
    //        verify(judicialUserProfileJsrValidatorInitializerSpy, times(1)).validate(any());
    //    }
}
