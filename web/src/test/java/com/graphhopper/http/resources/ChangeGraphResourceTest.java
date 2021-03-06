/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.http.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.http.GraphHopperApplication;
import com.graphhopper.http.util.GraphHopperServerTestConfiguration;
import com.graphhopper.util.Helper;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.File;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.ClassRule;
import org.junit.Test;
import static com.graphhopper.http.util.TestUtils.clientTarget;

/**
 * @author Peter Karich
 */
public class ChangeGraphResourceTest {
    private static final String DIR = "./target/andorra-gh/";

    private static final GraphHopperServerTestConfiguration config = new GraphHopperServerTestConfiguration();

    static {
        config.getGraphHopperConfiguration().
                putObject("graph.flag_encoders", "car").
                putObject("web.change_graph.enabled", true).
                putObject("graph.location", DIR).
                putObject("datareader.file", "../core/files/andorra.osm.pbf");
    }

    @ClassRule
    public static final DropwizardAppRule<GraphHopperServerTestConfiguration> app = new DropwizardAppRule(
            GraphHopperApplication.class, config);

    @AfterClass
    public static void cleanUp() {
        Helper.removeDir(new File(DIR));
    }

    @Test
    public void testBlockAccessViaPoint() throws Exception {
        Response response = clientTarget(app, "route?point=42.531453,1.518946&point=42.511178,1.54006").request().buildGet().invoke();
        assertEquals(200, response.getStatus());
        JsonNode json = response.readEntity(JsonNode.class);
        assertFalse(json.get("info").has("errors"));
        double distance = json.get("paths").get(0).get("distance").asDouble();
        assertTrue("distance wasn't correct:" + distance, distance > 3000);
        assertTrue("distance wasn't correct:" + distance, distance < 3500);

        // block road
        String geoJson = "{"
                + "\"type\": \"FeatureCollection\","
                + "\"features\": [{"
                + "  \"type\": \"Feature\","
                + "  \"geometry\": {"
                + "    \"type\": \"Point\","
                + "    \"coordinates\": [1.521692, 42.522969]"
                + "  },"
                + "  \"properties\": {"
                + "    \"vehicles\": [\"car\"],"
                + "    \"access\": false"
                + "  }}]}";

        response = clientTarget(app, "/change").request().post(Entity.json(geoJson));
        assertEquals(200, response.getStatus());
        json = response.readEntity(JsonNode.class);
        assertEquals(1, json.get("updates").asInt());

        // route around blocked road => longer
        response = clientTarget(app, "/route?point=42.531453,1.518946&point=42.511178,1.54006").request().buildGet().invoke();
        assertEquals(200, response.getStatus());
        json = response.readEntity(JsonNode.class);
        assertFalse(json.get("info").has("errors"));

        distance = json.get("paths").get(0).get("distance").asDouble();
        assertTrue("distance wasn't correct:" + distance, distance > 5300);
        assertTrue("distance wasn't correct:" + distance, distance < 5800);
    }
}
