package org.ld4l.bib2lod.rdfconversion.bibframeconversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ld4l.bib2lod.rdfconversion.BfProperty;
import org.ld4l.bib2lod.rdfconversion.BfType;
import org.ld4l.bib2lod.rdfconversion.Ld4lProperty;
import org.ld4l.bib2lod.rdfconversion.Ld4lType;
import org.ld4l.bib2lod.rdfconversion.RdfProcessor;

public class BfTitleConverter extends BfResourceConverter {

    private static final Logger LOGGER = 
            LogManager.getLogger(BfTitleConverter.class);

    
    private static final List<BfType> TYPES_TO_CONVERT = 
            new ArrayList<BfType>();
    static {
        TYPES_TO_CONVERT.add(BfType.BF_TITLE);
    }
    
    private static final List<BfType> TYPES_TO_RETRACT = 
            new ArrayList<BfType>();
    static {

    }
    
    private static final List<BfProperty> PROPERTIES_TO_CONVERT = 
            new ArrayList<BfProperty>();
    static {
        PROPERTIES_TO_CONVERT.add(BfProperty.BF_INSTANCE_TITLE);
        PROPERTIES_TO_CONVERT.add(BfProperty.BF_WORK_TITLE);
    }

    private static final Map<BfProperty, Ld4lProperty> PROPERTY_MAP =
            new HashMap<BfProperty, Ld4lProperty>();
    static {

    }
    
    private static final List<BfProperty> PROPERTIES_TO_RETRACT = 
            new ArrayList<BfProperty>();
    static {

    }
    
    private static final List<String> NON_SORT_STRINGS = 
            new ArrayList<String>();
    static {
        NON_SORT_STRINGS.add("A ");
        NON_SORT_STRINGS.add("The ");
        // TODO Are there others?
    }
    
    
    protected BfTitleConverter(String localNamespace) {
        super(localNamespace);
    }
    
    
    @Override
    protected void convertModel() {
        
      convertTitleValue();
      super.convertModel();
    }
    
    private void convertTitleValue() {
        
        Statement titleValueStmt = 
                subject.getProperty(BfProperty.BF_TITLE_VALUE.property());
        
        if (titleValueStmt != null) {

            retractions.add(titleValueStmt);

            // bf:titleValue string and language
            String titleValueString = 
                    normalizeTitle(titleValueStmt.getString());
            String titleValueLanguage = titleValueStmt.getLanguage();
            
            // This will be the rdfs:label of the Title
            String fullTitleString = titleValueString;
       
            Resource nonSortElement = createNonSortTitleElement(
                    titleValueString, titleValueLanguage, subject, assertions);

            // This will be the rdfs:label value of the MainTitleElement. In
            // the absence of other title elements, it's the full bf:titleValue
            // string. If there's a non-sort element, remove it from the full title
            // title string to get the main title element string.         
            String mainTitleString = 
                    removeNonSortString(titleValueString, nonSortElement);
               
            Resource mainTitleElement = 
                    createTitleElement(Ld4lType.MAIN_TITLE_ELEMENT, 
                            mainTitleString, titleValueLanguage);
            
            // Non-sort element precedes main title element
            if (nonSortElement != null) {
                assertions.add(nonSortElement, Ld4lProperty.NEXT.property(), 
                        mainTitleElement);
            }
            
            Resource subtitleElement = null;
                         
            Statement subtitleStmt = 
                    subject.getProperty(BfProperty.BF_SUBTITLE.property());            
            if (subtitleStmt != null) {
                
                retractions.add(subtitleStmt);
                
                // Create the subtitle element
                String subtitleValue = normalizeTitle(subtitleStmt.getString());
                String language = subtitleStmt.getLanguage();
                subtitleElement = createTitleElement(
                        Ld4lType.SUBTITLE_ELEMENT, subtitleValue, language);
                
                // When there's a subtitle, the titleValue contains only the 
                // main title, not the full title. The rdfs:label (full title)
                // concatenates both main title and subtitle strings.
                fullTitleString += " " + subtitleValue;
                
                // Order the mainTitleElement and subtitleElement
                assertions.add(mainTitleElement, Ld4lProperty.NEXT.property(), 
                        subtitleElement);
          
            } 
            
            // Add the rdfs:label of the Title
            assertions.add(
                    subject, RDFS.label, fullTitleString, titleValueLanguage);
            
            // TODO Part name/number elements
        }
    }
    
    private Resource createNonSortTitleElement(String titleString, 
            String titleLanguage, Resource title, Model model) {
        
        Resource nonSortElement = null;
        
        // Create non-sort element from initial definite/indefinite article
        for (String nonSortString : NON_SORT_STRINGS) {
            
            if (titleString.startsWith(nonSortString)) {
                
                nonSortElement = 
                        createTitleElement(Ld4lType.NON_SORT_TITLE_ELEMENT, 
                                nonSortString, titleLanguage, title, model);
      
                break;                 
            } 
        }
        
        return nonSortElement;
    }

    private Resource createTitleElement(Ld4lType titleElementType, String value, 
            String language) {
        
        return createTitleElement(
                titleElementType, value, language, subject, assertions);
    }
    
    /*
     * String value should be sent in normalized or unnormalized, as needed by
     * the caller.
     */
    private Resource createTitleElement(
            Ld4lType titleElementType, String value, String language, 
            Resource title, Model model) {

        Resource titleElement = 
                model.createResource(RdfProcessor.mintUri(localNamespace));
        model.add(title, Ld4lProperty.HAS_PART.property(), titleElement);
        model.add(titleElement, RDFS.label, value, language);
        model.add(titleElement, RDF.type, titleElementType.ontClass());
        
        return titleElement;                  
    }
    
//  private Resource createTitleElement(Ld4lType titleElementType, 
//          Literal literal) {
//      return createTitleElement(titleElementType, literal, false);
//  }
//  
//  private Resource createTitleElement(Ld4lType titleElementType, 
//          Literal literal, boolean normalize) {
//
//      String value = literal.getLexicalForm();
//      if (normalize) {
//          value = normalizeTitle(value);
//      }
//      return createTitleElement(titleElementType, value, 
//              literal.getLanguage());        
//  }
    
    static String normalizeTitle(String title) {
        /*
         * Remove final period, final spaces, final ellipsis
         * Final ellipsis occurs in rare cases when the bf:label contains title 
         * plus author, separated by ellipsis. The LC converter splits these 
         * and retains the ellipsis in the titleValue.
         * Examples:
         <http://ld4l.library.cornell.edu/individual/2235title31> <http://bibframe.org/vocab/label> "Old Orange Houses ...  by Mildred Parker Seese." . 
         <http://ld4l.library.cornell.edu/individual/2235title31> <http://bibframe.org/vocab/titleValue> "Old Orange Houses ..." . 
        
         <http://ld4l.library.cornell.edu/individual/2537title132> <http://bibframe.org/vocab/label> "A manual of veterinary sanitary science and police ... / by George Fleming." . 
         <http://ld4l.library.cornell.edu/individual/2537title132> <http://bibframe.org/vocab/titleValue> "A manual of veterinary sanitary science and police ..." .               
        */
        return title.replaceAll("[\\s.]+$", "");
    }

//  private Literal normalizeTitle(Literal literal) {
//  
//  if (literal == null) {
//      return null;
//  }
//  // Create a new literal with a normalized string value and the language
//  // of the original literal.
//  String value = normalizeTitle(literal.getLexicalForm());
//
//  String language = literal.getLanguage();
//  
//  // OK if language is null or empty
//  return literal.getModel().createLiteral(value, language); 
//}
    
    public Model create(Resource subject, Literal titleStatementLiteral) {
        
        Model model = ModelFactory.createDefaultModel();
        Resource title = 
                model.createResource(RdfProcessor.mintUri(localNamespace));
        model.add(title, RDF.type, Ld4lType.TITLE.ontClass());        
        model.add(subject, Ld4lProperty.HAS_TITLE.property(), title);
        
        String titleStatementString = 
                normalizeTitle(titleStatementLiteral.getLexicalForm());
        
        String language = titleStatementLiteral.getLanguage();
      
        // The full bf:titleStatement string is the rdfs:label of the title
        model.add(title, RDFS.label, titleStatementString);
        
        Resource nonSortElement = 
                createNonSortTitleElement(
                        titleStatementString, language, title, model);

        // This will be the rdfs:label value of the MainTitleElement. In
        // the absence of other title elements, it's the full bf:titleStatement
        // string. If there's a non-sort element, remove it from the full title
        // string to get the main title element string.
        String mainTitleString = 
                removeNonSortString(titleStatementString, nonSortElement);
       
        // TODO Perhaps try to parse into subtitle and part name elements - but 
        // seems very error-prone.
           
        Resource mainTitleElement = 
                createTitleElement(Ld4lType.MAIN_TITLE_ELEMENT, 
                        mainTitleString, language, title, model);
        
        // Non-sort element precedes main title element
        if (nonSortElement != null) {
            model.add(nonSortElement, Ld4lProperty.NEXT.property(), 
                    mainTitleElement);
        }

        return model;
    }
    
    private String removeNonSortString(
            String mainTitleString, Resource nonSortElement) {
        
        if (nonSortElement != null) {
            // Remove the non-sort string from the full title string to get the 
            // main title element string 
            String nonSortString = 
                    nonSortElement.getProperty(RDFS.label).getString();
            mainTitleString = mainTitleString.substring(nonSortString.length());
        }
        
        return mainTitleString;
    }
    
    @Override 
    protected List<BfType> getBfTypesToConvert() {
        return TYPES_TO_CONVERT;
    }
    
    @Override 
    protected List<BfType> getBfTypesToRetract() {
        return TYPES_TO_RETRACT;
    }
    
    @Override
    protected List<BfProperty> getBfPropertiesToConvert() {
        return PROPERTIES_TO_CONVERT;
    }
   
    @Override
    protected Map<BfProperty, Ld4lProperty> getBfPropertyMap() {
        return PROPERTY_MAP;
    }

    @Override
    protected List<BfProperty> getBfPropertiesToRetract() {
        return PROPERTIES_TO_RETRACT;
    }



}