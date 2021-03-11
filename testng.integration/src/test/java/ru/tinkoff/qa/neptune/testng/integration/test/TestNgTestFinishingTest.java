package ru.tinkoff.qa.neptune.testng.integration.test;

import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import ru.tinkoff.qa.neptune.testng.integration.properties.RefreshEachTimeBefore;
import ru.tinkoff.qa.neptune.testng.integration.test.ignored.IgnoredStubTest;
import ru.tinkoff.qa.neptune.testng.integration.test.ignored.entries.IgnoredStubTest2;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.tinkoff.qa.neptune.core.api.concurrency.ObjectContainer.getAllObjects;
import static ru.tinkoff.qa.neptune.testng.integration.properties.RefreshEachTimeBefore.*;
import static ru.tinkoff.qa.neptune.testng.integration.properties.TestNGRefreshStrategyProperty.REFRESH_STRATEGY_PROPERTY;

public class TestNgTestFinishingTest {

    private void runBeforeTheChecking() {
        ContextClass2.refreshCountToZero();
        TestNG testNG=new TestNG();

        List<XmlSuite> testSuites=new ArrayList<>();

        XmlSuite suite = new XmlSuite();
        XmlSuite parent = new XmlSuite();
        suite.setParentSuite(parent);
        suite.setName("FinishSuite");

        XmlTest test = new XmlTest(suite);

        List<XmlClass> testClasses = new ArrayList<>();
        testClasses.add(new XmlClass(TestNgInstantiationTest.class.getName()));
        testClasses.add(new XmlClass(TestNgStubTest.class.getName()));
        testClasses.add(new XmlClass(IgnoredStubTest2.class.getName()));
        testClasses.add(new XmlClass(IgnoredStubTest.class.getName()));

        test.setXmlClasses(testClasses);
        //testNG.setAnnotationTransformer();
        testSuites.add(suite);

        testNG.setXmlSuites(testSuites);
        testNG.run();
    }

    @Test
    public void whenNoRefreshingStrategyIsDefined() {
        runBeforeTheChecking();
        assertThat(ContextClass2.getRefreshCount(), is(9));
    }

    @Test
    public void whenRefreshingStrategyIsBeforeSuite() {
        REFRESH_STRATEGY_PROPERTY.accept(of(SUITE_STARTING));
        try {
            runBeforeTheChecking();
            assertThat(ContextClass2.getRefreshCount(), is(1));
        }
        finally {
            System.getProperties().remove(REFRESH_STRATEGY_PROPERTY.getName());
        }
    }

    @Test
    public void whenRefreshingStrategyIsBeforeTest() {
        REFRESH_STRATEGY_PROPERTY.accept(of(TEST_STARTING));
        try {
            runBeforeTheChecking();
            assertThat(ContextClass2.getRefreshCount(), is(1));
        }
        finally {
            System.getProperties().remove(REFRESH_STRATEGY_PROPERTY.getName());
        }
    }

    @Test
    public void whenRefreshingStrategyIsBeforeClass() {
        REFRESH_STRATEGY_PROPERTY.accept(of(CLASS_STARTING));
        try {
            runBeforeTheChecking();
            assertThat(ContextClass2.getRefreshCount(), is(2));
        }
        finally {
            System.getProperties().remove(REFRESH_STRATEGY_PROPERTY.getName());
        }
    }

    @Test
    public void whenRefreshingStrategyIsBeforeMethod() {
        REFRESH_STRATEGY_PROPERTY.accept(of(BEFORE_METHOD_STARTING));
        try {
            runBeforeTheChecking();
            assertThat(ContextClass2.getRefreshCount(), is(9));
        }
        finally {
            System.getProperties().remove(REFRESH_STRATEGY_PROPERTY.getName());
        }
    }

    @Test
    public void whenRefreshingStrategyIsCombined() {
        REFRESH_STRATEGY_PROPERTY.accept(asList(RefreshEachTimeBefore.values()));
        try {
            runBeforeTheChecking();
            assertThat(ContextClass2.getRefreshCount(), is(9));
        } finally {
            System.getProperties().remove(REFRESH_STRATEGY_PROPERTY.getName());
        }
    }

    @Test
    public void hookTest() {
        TestHook.count = 0;
        runBeforeTheChecking();
        assertThat(TestHook.count, greaterThan(0));
    }

    @AfterClass
    public void afterClass() {
        assertThat(getAllObjects(ContextClass2.class, objectContainer -> true), hasSize(6));
        assertThat(getAllObjects(ContextClass1.class, objectContainer -> true), hasSize(6));
    }
}
