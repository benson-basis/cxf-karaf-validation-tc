/******************************************************************************
 * * This data and information is proprietary to, and a valuable trade secret
 * * of, Basis Technology Corp.  It is given in confidence by Basis Technology
 * * and may only be used as permitted under the license agreement under which
 * * it has been distributed, and in no other way.
 * *
 * * Copyright (c) 2015 Basis Technology Corporation All rights reserved.
 * *
 * * The technical data and information provided herein are provided with
 * * `limited rights', and the computer software provided herein is provided
 * * with `restricted rights' as those terms are defined in DAR and ASPR
 * * 7-104.9(a).
 ******************************************************************************/

package com.basistech.ws.validation;

import com.basistech.ws.beanvalidation.BeanValidation;
import com.google.common.io.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Constants;

import javax.inject.Inject;
import javax.validation.Validator;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;


/**
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ValidationIT {

    static String karafVersion;
    static String basedir;
    static String projectVersion;
    static boolean karafDebug;

    @Inject
    BeanValidation validation;

    private static void loadProps() throws Exception {
        URL configPropUrl = Resources.getResource(ValidationIT.class, "test-config.properties");
        Properties props = new Properties();
        try (InputStream propStream = configPropUrl.openStream()) {
            props.load(propStream);
        }

        karafVersion = props.getProperty("karaf.version");
        basedir = props.getProperty("basedir");
        projectVersion = props.getProperty("project.version");
    }

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*");
        return probe;
    }

    @Configuration
    public Option[] configure() throws Exception {
        loadProps();
        karafDebug = false;

        return options(karafDistributionConfiguration()
                        .frameworkUrl(
                                maven()
                                        .groupId("org.apache.karaf")
                                        .artifactId("apache-karaf")
                                        .type("zip")
                                        .version(karafVersion))
                        .karafVersion(karafVersion).name("Apache Karaf")
                        .unpackDirectory(new File("target/pax"))
                        .useDeployFolder(false),
                keepRuntimeFolder(),
                configureConsole().ignoreLocalConsole(),
                logLevel(LogLevelOption.LogLevel.INFO),
                features(
                        maven().groupId("com.basistech.ws").artifactId("bean-validation-feature-core")
                                .version(projectVersion).classifier("features").type("xml"),
                        "bean-validation-feature-core"
                ),
                features(maven().groupId("org.apache.karaf.features")
                        .artifactId("standard")
                        .version(karafVersion)
                        .classifier("features")
                        .type("xml"),
                        "scr"),
                when(karafDebug).useOptions(debugConfiguration()),
                junitBundles(),
                systemProperty("pax.exam.osgi.unresolved.fail").value("true"),
                systemProperty("org.ops4j.pax.exam.rbc.rmi.host").value("localhost"),
                editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.defaultLocalRepoAsRemote", "true")
        );
    }

    @Test
    public void getValidators() throws Exception {
        // if it doesn't throw, we're fairly happy.
        validation.createValidator();
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        for (int x = 0; x < 1000; x++) {
            Callable<Validator> task = new Callable<Validator>() {
                @Override
                public Validator call() throws Exception {
                    return validation.createValidator();
                }
            };
            Future<Validator> validator1 = threadPool.submit(task);
            Future<Validator> validator2 = threadPool.submit(task);
            validator1.get();
            validator2.get();
        }
    }
}
