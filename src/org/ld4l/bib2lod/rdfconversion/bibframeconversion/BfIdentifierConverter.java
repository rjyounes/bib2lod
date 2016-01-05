package org.ld4l.bib2lod.rdfconversion.bibframeconversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ld4l.bib2lod.rdfconversion.BfProperty;
import org.ld4l.bib2lod.rdfconversion.Ld4lProperty;
import org.ld4l.bib2lod.rdfconversion.Ld4lType;
import org.ld4l.bib2lod.rdfconversion.Vocabulary;

public class BfIdentifierConverter extends BfResourceConverter {

    private static final Logger LOGGER = 
            LogManager.getLogger(BfIdentifierConverter.class);
    
    private static final Map<BfProperty, Ld4lType> PROPERTY_TO_TYPE = 
            new HashMap<BfProperty, Ld4lType>();
    static {
        PROPERTY_TO_TYPE.put(BfProperty.BF_ANSI, Ld4lType.ANSI);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_CODEN, Ld4lType.CODEN);             
        PROPERTY_TO_TYPE.put(BfProperty.BF_DISSERTATION_IDENTIFIER,                
                Ld4lType.DISSERTATION_IDENTIFIER);
        PROPERTY_TO_TYPE.put(BfProperty.BF_DOI, Ld4lType.DOI);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_EAN, Ld4lType.EAN);              
        PROPERTY_TO_TYPE.put(BfProperty.BF_FINGERPRINT, Ld4lType.FINGERPRINT);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_HDL, Ld4lType.HDL); 
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISAN, Ld4lType.ISAN);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISBN, Ld4lType.ISBN);                     
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISBN10, Ld4lType.ISBN10);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISBN13, Ld4lType.ISBN13);               
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISMN, Ld4lType.ISMN);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISO, Ld4lType.ISO);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISSN, Ld4lType.ISSN);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISSNL, Ld4lType.ISSNL);               
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISSUE_NUMBER, Ld4lType.ISSUE_NUMBER);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_ISTC, Ld4lType.ISTC);              
        PROPERTY_TO_TYPE.put( BfProperty.BF_ISWC, Ld4lType.ISWC);                   
        PROPERTY_TO_TYPE.put(BfProperty.BF_LC_OVERSEAS_ACQ, 
                Ld4lType.LC_OVERSEAS_ACQ_NUMBER);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_LCCN, Ld4lType.LCCN);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_LEGAL_DEPOSIT, 
                Ld4lType.LEGAL_DEPOSIT_NUMBER);               
        PROPERTY_TO_TYPE.put(BfProperty.BF_LOCAL, 
                Ld4lType.LOCAL_ILS_IDENTIFIER);               
        PROPERTY_TO_TYPE.put(BfProperty.BF_MATRIX_NUMBER, 
                Ld4lType.MATRIX_NUMBER);                
        PROPERTY_TO_TYPE.put(BfProperty.BF_MUSIC_PLATE, 
                Ld4lType.MUSIC_PLATE_NUMBER);               
        PROPERTY_TO_TYPE.put(BfProperty.BF_MUSIC_PUBLISHER_NUMBER, 
                Ld4lType.MUSIC_PUBLISHER_NUMBER);
        PROPERTY_TO_TYPE.put(BfProperty.BF_NBAN, Ld4lType.NBAN);
        PROPERTY_TO_TYPE.put(BfProperty.BF_NBN, Ld4lType.NBN);
        PROPERTY_TO_TYPE.put(BfProperty.BF_POSTAL_REGISTRATION, 
                Ld4lType.POSTAL_REGISTRATION_NUMBER);
        PROPERTY_TO_TYPE.put(BfProperty.BF_PUBLISHER_NUMBER, 
                Ld4lType.PUBLISHER_NUMBER);
        PROPERTY_TO_TYPE.put(BfProperty.BF_REPORT_NUMBER, 
                Ld4lType.TECHNICAL_REPORT_NUMBER);
        PROPERTY_TO_TYPE.put(BfProperty.BF_SICI, Ld4lType.SICI);
        PROPERTY_TO_TYPE.put(BfProperty.BF_STOCK_NUMBER, Ld4lType.STOCK_NUMBER);
        PROPERTY_TO_TYPE.put(BfProperty.BF_STRN, Ld4lType.STRN);
        PROPERTY_TO_TYPE.put(BfProperty.BF_STUDY_NUMBER, Ld4lType.STUDY_NUMBER);
        PROPERTY_TO_TYPE.put(BfProperty.BF_UPC, Ld4lType.UPC);
        PROPERTY_TO_TYPE.put(BfProperty.BF_VIDEORECORDING_NUMBER, 
                Ld4lType.VIDEO_RECORDING_NUMBER);
    }
    
    private static final List<BfProperty> BF_IDENTIFIER_PROPS =
            new ArrayList<BfProperty>();
    static {
        BF_IDENTIFIER_PROPS.addAll(PROPERTY_TO_TYPE.keySet());
        BF_IDENTIFIER_PROPS.add(BfProperty.BF_IDENTIFIER);
        BF_IDENTIFIER_PROPS.add(BfProperty.BF_SYSTEM_NUMBER);
    }
    
    private static final List<Property> IDENTIFIER_PROPS = 
            BfProperty.properties(BF_IDENTIFIER_PROPS);
    
    // TODO change to String => Ld4lType?
    private static final Map<String, String> IDENTIFIER_PREFIXES = 
            new HashMap<String, String>();
    static {
        // Many of these are not instantiated in the data, so it's a guess as
        // to what the string value would be.
//        IDENTIFIER_PREFIXES.put("CStRLIN", );
//        IDENTIFIER_PREFIXES.put("NIC", );        
        IDENTIFIER_PREFIXES.put("OCoLC", "OclcIdentifier");
        IDENTIFIER_PREFIXES.put("OCoLC-I", "OclcIdentifier");
        IDENTIFIER_PREFIXES.put("OCoLC-M", "OclcIdentifier");
    }
    
    // Examples: Harvard: ocn188263227, ocm78759487 
    private Pattern OCLC_PREFIXED_IDENTIFIER = 
            Pattern.compile("^(oc[mn]?)(\\d+)");
    
    // Examples: Cornell: (OCoLC)fst00845993, (OCoLC)150088819 
    // Stanford: (OCoLC-I)268808835, (CSt)notisAAA0017, (OCoLC-M)1122955
    private Pattern PREFIXED_IDENTIFIER = 
            Pattern.compile("^\\((.+)\\)?(.+)");

    
    public BfIdentifierConverter(String localNamespace, Statement statement) {
        super(localNamespace, statement);
    }

    public static List<Property> getIdentifierProps() {
        return IDENTIFIER_PROPS;
    }
    
    protected Model convertModel() {
        
//        LOGGER.debug("Identifier model: ");
//        RdfProcessor.printModel(inputModel, Level.DEBUG);

        Resource relatedResource = linkingStatement.getSubject();
        outputModel.add(relatedResource, Ld4lProperty.IDENTIFIED_BY.property(),
                subject);
        
        // Maybe break into two main methods, one for type and one for value,
        // but they're not completely independent: the type may be derived from 
        // the value, or (for worldcat ids) the value from the type.
      
        String newIdentifierValue = null;
        Statement identifierValueStmt = 
                subject.getProperty(BfProperty.BF_IDENTIFIER_VALUE.property());

        RDFNode identifierValue = null; 
        if (identifierValueStmt != null) {
            identifierValue = identifierValueStmt.getObject();
            if (identifierValue.isLiteral()) {
                newIdentifierValue = 
                        identifierValue.asLiteral().getLexicalForm();
 
// This is true of worldcat, but is it otherwise true?
//            } else {
//                newIdentifierValue = 
//                        identifierValue.asResource().getLocalName();
//            }
        }
        
        /*
         * Identifier type
         */
        
        Ld4lType identifierType = 
                getIdentifierTypeFromPredicate(relatedResource);
        
        // Note that Biframe often redundantly specifies type with both a 
        // predicate and a scheme:
        // <http://draft.ld4l.org/cornell/102063instance16> <http://bibframe.org/vocab/lccn> _:bnode47102063 .
        // _:bnode47102063 <http://bibframe.org/vocab/identifierScheme> <http://id.loc.gov/vocabulary/identifiers/lccn> . 
        if (identifierType == null) {
            identifierType = getIdentifierTypeFromIdentifierScheme();
        }
        
        if (identifierType == null) {
            identifierType = getIdentifierTypeFromIdentifierValue();
            // use prefix on identifierValue     
            // if no prefix and all digits, assume LocalIlsIdentifier
        }
        
        // We may want to assign the supertype in any case, to simplify the
        // queries used to build the search index, since there's no reasoner to
        // infer the supertype.
        if (identifierType == null) {
            identifierType = Ld4lType.IDENTIFIER;
        }
        
        outputModel.add(subject, RDF.type, identifierType.ontClass());

        
        /*
         * Identifier value
         */
        
        // Is this more generally, if the value is a resource, use the local 
        // name as the new value?
        if (identifierType == Ld4lType.OCLC_IDENTIFIER) {
            newIdentifierValue = subject.getLocalName();
            
        } else {
            // No else case. newIdentifierValue already assigned from 
            // bf:identifierValue above.
        }

        if (newIdentifierValue != null) {
            outputModel.add(subject, RDF.value, newIdentifierValue);
        }
            
//            /*
//             * Get scheme from identifierScheme, if possible
//             * Else get from value, if possible
//             * else make generic Identifier
//             * 
//             * remainder of value is rdf:value of the identifier entity
//             * 
//             * Use fast code from TopicConverter - move to ??
//             * Topic must also add the identifier statements
//             */
// /* also need to deal with scheme in predicate: 
//  * <bf:lccn>
//
//      <bf:Identifier>
//        <bf:identifierValue>74227179</bf:identifierValue>
//        <bf:identifierScheme>lccn</bf:identifierScheme>
//      </bf:Identifier>
//    </bf:lccn>
//      */
//            String identifierType = null;
//            
//            Statement schemeStmt = subject.getProperty(
//                    BfProperty.BF_IDENTIFIER_SCHEME.property());
//
//            // Determine the Identifier subtype from the identifierScheme
//            // statement, if there is one.
//            if (schemeStmt != null) {
//                
//                RDFNode scheme = schemeStmt.getResource();
//                
//                if (scheme.isResource()) {
//                    // Cornell and Harvard: 
//                    // http://id.loc.gov/vocabulary/identifiers/lccn
//                    // http://id.loc.gov/vocabulary/identifiers/systemNumber
//                    identifierType = scheme.asResource().getLocalName();
//                            
//                     
//                } else {
//                    // Stanford: 
//                    // <bf:identifierScheme>lccn</bf:identifierScheme>
//                    // <bf:identifierScheme>systemNumber</bf:identifierScheme> 
//                    // <bf:identifierScheme>musicPublisherNumber</bf:identifierScheme>
//                    identifierType = scheme.asLiteral().getLexicalForm();
//                            
//                }
//                
//                // "systemNumber" is generic - doesn't specify any particular
//                // scheme, so delete.
//                if (identifierType.equals("systemNumber")) {
//                    identifierType = null;
//                } else if (identifierType != null) {
//                    identifierType = StringUtils.capitalize(identifierType);
//                            
//                }        
//            }
//    
//            Statement valueStmt = subject.getProperty(
//                    BfProperty.BF_IDENTIFIER_VALUE.property());
//
//            String value = null;
//            String prefix = null;
//            String identifierValue = null;
//            
//            if (valueStmt != null) {
//                value = valueStmt.getString();
//
//// TODO - match 2 patterns defined above
////                Matcher m = IDENTIFIER_VALUE.matcher(value);
////                if (m.find()) {
////                    prefix = m.group(1);
////                    identifierValue = m.group(2);
////                }                       
//            }
//
//            if (identifierValue != null) {
//                outputModel.add(subject, RDF.value, identifierValue);
//            }
//            
//            // If there was no identifierScheme statement, try to get the 
//            // identifier type from the identifierValue prefix
//            if (identifierType == null ||
//                    // The type SystemNumber is generic - try to find more 
//                    // specific type from the id value prefix
//                    identifierType.equals("SystemNumber")) {
//                if (prefix != null) {
//                   if  (IDENTIFIER_PREFIXES.keySet().contains(prefix)) {
//                       identifierType = 
//                               IDENTIFIER_PREFIXES.get(prefix);
//                   }
//                        
//                // No prefix   
//                } else {
//                    // If value is all digits, assume a local ILS identifier
//                    if (Pattern.matches("^\\d+$", identifierValue)) {
//                        identifierType = "LocalIlsIdentifier";
//                    }
//                }
//                // otherwise keep SystemNumber
//            }
//            
//            // If no subtype can be determined, assign the generic Identifier
//            // type.
//            if (identifierType == null) {
//                identifierType = "Identifier";
//            }
//
////            Resource identifierType = ResourceFactory.createResource(
////                    OntNamespace.LD4L.uri() + identifierType);
////            outputModel.add(subject, RDF.type, identifierType);
//

        }
        
        return outputModel;
    }

    private Ld4lType getIdentifierTypeFromIdentifierValue() {
        
        return null;
        
    }

    private Ld4lType getIdentifierTypeFromIdentifierScheme() {
        
        Ld4lType identifierType = null;
        String schemeValue = null;
        
        Statement schemeStmt = subject.getProperty(
                BfProperty.BF_IDENTIFIER_SCHEME.property());
        if (schemeStmt != null) {
            RDFNode scheme = schemeStmt.getObject();
            
            // Cornell, Harvard
            if (scheme.isResource()) {
                schemeValue = scheme.asResource().getLocalName();
                
            // Stanford
            } else if (scheme.isLiteral()) {
                schemeValue = scheme.asLiteral().getLexicalForm();
            }
            
            if (! schemeValue.equals("systemNumber")) {
                schemeValue = StringUtils.capitalize(schemeValue);
                // Will be null if it doesn't exist.
                identifierType = Ld4lType.get(schemeValue);            
            } 
        }
        
        return identifierType;
    }

    private Ld4lType getIdentifierTypeFromPredicate(Resource relatedResource) {
            
        Ld4lType identifierType = null;
        
        if (subject.getNameSpace().equals(Vocabulary.WORLDCAT.uri())) {
            identifierType = Ld4lType.OCLC_IDENTIFIER;
        
        } else {
            
            Property predicate = linkingStatement.getPredicate();
            
            BfProperty bfProp = BfProperty.get(predicate);
                        
            if (bfProp == null) {
                // This would be an oversight; log for review.
                LOGGER.warn("No handling defined for property " 
                        + predicate.getURI() + " linking " 
                        + relatedResource.getURI() + " to its identifier "
                        + subject.getURI() + ".");   
                
            } else if (PROPERTY_TO_TYPE.keySet().contains(bfProp)) {
                identifierType = PROPERTY_TO_TYPE.get(bfProp);                
            }
            
        }
        return identifierType;        
    }
    

}
