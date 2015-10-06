package org.ld4l.bib2lod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ld4l.bib2lod.rdfconversion.BibframeRdfConverter;
import org.ld4l.bib2lod.rdfconversion.ResourceDeduper;

public enum Action {

    // GET_MARC("get_marc"),
    // MARC2MARCXML("marc2marcxml"),
    // Clean up MARCXML records: correct known errors, enhance with ??
    // PREPROCESS_MARCXML("preprocess"),
    // MARCXML2BIBFRAME("marcxml2bibframe"),
    // CONVERT_BNODES("convert_bnodes", BnodeConverter.class),
    // SPLIT_TYPES("split_types", TypeSplitter.class),
    DEDUPE_RESOURCES("dedupe", ResourceDeduper.class),
    CONVERT_BIBFRAME_RDF("convert_bibframe", BibframeRdfConverter.class);
    // RESOLVE_TO_EXTERNAL_ENTITIES);
    
    private final String label;    
    private final Class<?> processorClass;

    // TODO What is the type on Class? Java won't accept Class<RdfProcessor> 
    // when instantiating subclasses.
    Action(String action, Class<?> processorClass) {
        this.label = action;
        this.processorClass = processorClass;
    }
    
    public String label() {
        return this.label;
    }
    
    public Class<?> processorClass() {
        return this.processorClass;
    }
     
    private static final Map<String, Action> LOOKUP_BY_LABEL = 
            new HashMap<String, Action>();
    
    private static final List<String> VALID_ACTIONS = 
            new ArrayList<String>();

    static {
        for (Action action : Action.values()) {
            String label = action.label;
            LOOKUP_BY_LABEL.put(label, action);
            VALID_ACTIONS.add(label);
        }
    }

    public static Action get(String action) { 
        return LOOKUP_BY_LABEL.get(action); 
    }
    

    public static List<String> validActions() {
        return VALID_ACTIONS;
    }
    
}
