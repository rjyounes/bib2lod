package org.ld4l.bib2lod.rdfconversion.resourcededuping;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ld4l.bib2lod.rdfconversion.BfProperty;
import org.ld4l.bib2lod.rdfconversion.BfType;

public class BfLanguageDeduper extends BfResourceDeduper {

    private static final Logger LOGGER =          
            LogManager.getLogger(BfLanguageDeduper.class);
    
    public BfLanguageDeduper(BfType type) {
        super(type);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Map<String, String> dedupe(Model model) {
        
        LOGGER.debug("Deduping type " + type.toString());

        // Maps local URIs in the Bibframe RDF to deduped URIs (single, unique
        // URI per Language). This map will be used to replace duplicate URIs 
        // for the same Language individual with a single, unique URI.
        Map<String, String> uniqueLangUris = new HashMap<String, String>();
        
        // Maps keys for Language identity matching to the unique Language URIs.         
        Map<String, String> uniqueLangs = new HashMap<String, String>();

        // Execute the query
        Query query = getQuery();        
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();
        
        // Loop through the query results
        while (results.hasNext()) {
            
            QuerySolution soln = results.next();

            String localLangUri = soln.getResource("lang").getURI();
            
            // Get an external URI for this Language, if there is one. We will 
            // replace local language URIs with it throughout the data. We will
            // also use this external URI as the key for identity matching. If
            // it doesn't exist, we use the literal object of bf:languageOfPart.
            String externalLangUri = getExternalLangUri(soln);
            
            // Get key for identity matching.
            String key = getKey(externalLangUri, soln);
            
            // Without a key there's nothing to dedupe on.
            if (key == null) {
                LOGGER.debug("No key for " + localLangUri + "; can't dedupe");
                continue;
            }
            
            // The external Language URI, if there is one,  will be the 
            // replacement URI.      
            String replacementUri = 
                    externalLangUri != null ? externalLangUri : localLangUri;

            if (uniqueLangs.containsKey(key)) {
                
                // We've seen this Language before
                String uniqueLangUri = uniqueLangs.get(key);
                LOGGER.debug("Found matching value for key " + key 
                        + " and language URI " + localLangUri);
                LOGGER.debug(
                        "Adding: " + localLangUri + " => " + uniqueLangUri);                
                // This local Language URI will be replaced by the unique 
                // Language URI throughout the data
                uniqueLangUris.put(localLangUri, uniqueLangUri);

            } else {
                // We haven't seen this Topic before
                LOGGER.debug("New topic: " + localLangUri);
                // For Languages, we're substituting the local URI with an
                // external URI, if it exists.
                uniqueLangUris.put(localLangUri, replacementUri);
                uniqueLangs.put(key, replacementUri);
            }
        }
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("uniqueLangUris map:");
            for (Map.Entry<String, String> entry : uniqueLangUris.entrySet()) {
                LOGGER.debug(entry.getKey() + " => " + entry.getValue());
            }
            LOGGER.debug("uniqueLangs map:");
            for (Map.Entry<String, String> entry : uniqueLangs.entrySet()) {
                LOGGER.debug(entry.getKey() + " => " + entry.getValue());
            }
        }
        
        return uniqueLangUris;        
    }
    
    // TODO Query could be a static final field, since there's no type
    // parameterization.
    /*
     * Language is identified by either a resource in the LC language vocabulary
     * (http://id.loc.gov/vocabulary/languages/) as the object of 
     * bf:languageOfPartUri, or the literal object of bf:languageOfPart. 
     * NB "ofPart" in the predicate names is misleading, since the description
     * reads "Language or notation system used to convey the content of the 
     * resource (associated with part or all of a resource)."
     */
    protected Query getQuery() {
        
        String queryString = 
                "SELECT ?lang ?externalLang ?langName "
                + "WHERE { "
                + "?lang a " + BfType.BF_LANGUAGE.sparqlUri() + " . "
                + "OPTIONAL { ?lang "
                + BfProperty.BF_LANGUAGE_OF_PART_URI.sparqlUri() 
                + " ?externalLang . } "
                + "OPTIONAL { ?lang " 
                + BfProperty.BF_LANGUAGE_OF_PART.sparqlUri() + " ?langName . } "
                + "}";

        LOGGER.debug("QUERY: " + queryString);
        return QueryFactory.create(queryString);

    }
    
    /**
     * Return the Language URI. Construct the URI from an external source, if  
     * there is one; otherwise use the local URI from the RDF.
     * @param soln
     * @return Language URI string
     */   
    private String getExternalLangUri(QuerySolution soln) {
     
        Resource externalLang = soln.getResource("externalLang");
        if (externalLang != null) {
            return externalLang.getURI();
        }
        
        LOGGER.debug("No external Language URI found");
        return null;
    }
    
    private String getKey(String externalLangUri, QuerySolution soln) {
        
        // If there's an external Language URI, use that as the key.
        if (externalLangUri != null) {
            return externalLangUri;
        }

        // Otherwise derive the key from the literal value of bf:languageOfPart 
        Literal langLiteral = soln.getLiteral("langName");
        if (langLiteral != null) {
            return langLiteral.getLexicalForm();
        }
       
        return null;
    }

}
