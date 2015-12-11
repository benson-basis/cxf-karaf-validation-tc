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

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.debugConfiguration;
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

    @Configuration
    public Option[] configure() throws Exception {
        loadProps();
        karafDebug = false;

        return options(karafDistributionConfiguration()
                        .frameworkUrl(
                                maven()
                                        .groupId("com.basistech.ws")
                                        .artifactId("bean-validation-assembly")
                                        .type("tar.gz")
                                        .version(projectVersion))
                        .karafVersion(karafVersion).name("Apache Karaf")
                        .unpackDirectory(new File("target/pax"))
                        .useDeployFolder(false),
                keepRuntimeFolder(),
                configureConsole().ignoreLocalConsole(),
                logLevel(LogLevelOption.LogLevel.INFO),
                when(karafDebug).useOptions(debugConfiguration()),
                junitBundles(),
                systemProperty("pax.exam.osgi.unresolved.fail").value("true"),
                systemProperty("org.ops4j.pax.exam.rbc.rmi.host").value("localhost")
        );
    }

    @Test
    public void pokeService() throws Exception {
        Thread.sleep(3000);
        URL url = new URL("http://localhost:8181/cxf/validate");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int code = connection.getResponseCode();
        Assert.assertEquals(400, code); // BAD_REQUEST due to validation error
    }
}
