/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.spring.tests;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import org.testng.annotations.AfterClass;


/**
 * Test singleton resources that are managed by spring.<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class AbstractTest {
    
    private GrizzlyWebServer ws ;

    /**
     * Get the HTTP port for the Web application.
     * @param defaultPort the default HTTP port to use.
     * @return the HTTP port.
     */
    private static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    /**
     * Get the base URI for the Web application.
     * @return the base URI.
     */
    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/spring").port(getPort(9998)).build();
    }

    /**
     * The base URI of the Web application.
     */
    private static final URI BASE_URI = getBaseURI();

    public void start() {
        start(null);
    }

    public void start(Map<String, String> initParams) {        
        String appConfig =  this.getClass().getName();
        appConfig = appConfig.replace(".", "/") + "-config.xml";

        try {
            stop();

            ws = new GrizzlyWebServer(BASE_URI.getPort());
            ServletAdapter sa = new ServletAdapter();
            sa.setServletInstance(SpringServlet.class.newInstance());
            sa.setServletPath("/spring");

            if (initParams != null) {
                for (Map.Entry<String, String> e : initParams.entrySet()) {
                    sa.addInitParameter(e.getKey(), e.getValue());
                }
            }

            sa.addServletListener("org.springframework.web.context.ContextLoaderListener");
            sa.addContextParameter("contextConfigLocation", "classpath:" + appConfig);

            ws.addGrizzlyAdapter(sa, new String[] {""} );
            ws.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() throws Exception {
        try {
            if (ws != null) {
                ws.stop();
            }
        } finally {
            ws = null;
        }
    }
    
//    @BeforeClass
//    public void setUp() throws Exception {
//        startGrizzly(BASE_URI);
//    }

    @AfterClass
    public void tearDown() throws Exception {
        stop();
    }

    public WebResource resource(String path) {
        return Client.create().resource(BASE_URI).path(path);
    }
}