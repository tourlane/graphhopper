package com.graphhopper.routing.util.spatialrules;

import com.graphhopper.routing.util.spatialrules.countries.AustriaSpatialRule;
import com.graphhopper.routing.util.spatialrules.countries.GermanySpatialRule;
import com.graphhopper.routing.util.spatialrules.countries.NamibiaSpatialRule;
import com.graphhopper.routing.util.spatialrules.countries.IcelandSpatialRule;
import com.graphhopper.util.shapes.Polygon;

import java.util.List;

public class CountriesSpatialRuleFactory implements SpatialRuleLookupBuilder.SpatialRuleFactory {
    @Override
    public SpatialRule createSpatialRule(String id, List<Polygon> polygons) {
        switch (id) {
            case "AUT":
                AustriaSpatialRule austriaSpatialRule = new AustriaSpatialRule();
                austriaSpatialRule.setBorders(polygons);
                return austriaSpatialRule;
            case "DEU":
                GermanySpatialRule germanySpatialRule = new GermanySpatialRule();
                germanySpatialRule.setBorders(polygons);
                return germanySpatialRule;
            case "NAM":
                NamibiaSpatialRule namibiaSpatialRule = new NamibiaSpatialRule();
                namibiaSpatialRule.setBorders(polygons);
                return namibiaSpatialRule;
            case "ISL":
                IcelandSpatialRule icelandSpatialRule = new IcelandSpatialRule();
                icelandSpatialRule.setBorders(polygons);
                return icelandSpatialRule;

        }
        return SpatialRule.EMPTY;
    }
}
