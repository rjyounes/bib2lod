package org.ld4l.bib2lod.rdfconversion.bibframeconversion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ld4l.bib2lod.rdfconversion.OntProperty;
import org.ld4l.bib2lod.rdfconversion.OntType;

public class BfFamilyConverter extends BfResourceConverter {

    private static final Logger LOGGER = 
            LogManager.getLogger(BfFamilyConverter.class);
    
    private static final OntType NEW_TYPE = OntType.FAMILY;
    
    private static final Map<OntProperty, OntProperty> PROPERTY_MAP =
            new HashMap<OntProperty, OntProperty>();
    static {
        PROPERTY_MAP.put(OntProperty.BF_LABEL, OntProperty.NAME);
        PROPERTY_MAP.put(OntProperty.BF_HAS_AUTHORITY, 
                OntProperty.MADSRDF_IS_IDENTIFIED_BY_AUTHORITY);
    }
    
    private static final List<OntProperty> PROPERTIES_TO_RETRACT = 
            Arrays.asList(
                    OntProperty.BF_AUTHORIZED_ACCESS_POINT
            );
            
    
    public BfFamilyConverter(Resource subject) {
        super(subject);
    }
    
    @Override
    protected OntType getNewType() {
        return NEW_TYPE;
    }

    @Override
    protected Map<OntProperty, OntProperty> getPropertyMap() {
        return PROPERTY_MAP;
    }

    @Override
    protected List<OntProperty> getPropertiesToRetract() {
        return PROPERTIES_TO_RETRACT;
    }
    

}