package org.ld4l.bib2lod.rdfconversion.bibframeconversion;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ld4l.bib2lod.rdfconversion.BfProperty;
import org.ld4l.bib2lod.rdfconversion.BfType;
import org.ld4l.bib2lod.rdfconversion.BibframeConverter;

/*
 * Provides conversions shared by Works and Instances - e.g., Title conversion
 * methods.
 */
public abstract class BfBibResourceConverter extends BfResourceConverter {

    private static final Logger LOGGER = 
            LogManager.getLogger(BfBibResourceConverter.class);

    
    public BfBibResourceConverter(BfType bfType, String localNamespace) {
        super(bfType, localNamespace);
    }
  
    // titleProp is either workTitle or instanceTitle
    protected void convertTitles(BfProperty titleProp) {

        // Get the value of any bf:titleStatement statements
        StmtIterator titleStmtIterator = subject.listProperties(
                BfProperty.BF_TITLE_STATEMENT.property());

        // This empties out the iterator
        // retractions.add(titleStmtIterator);
        List<Literal> titleLiterals = new ArrayList<Literal>();
        while (titleStmtIterator.hasNext()) {
            Statement stmt = titleStmtIterator.nextStatement();
            Literal literal = stmt.getLiteral();
            // Normalize the title string for comparison to existing Title
            // labels.
            String value = BfTitleConverter.normalizeTitle(literal.getString());
            LOGGER.debug("Adding literal with value '" + value + "' to list");
            titleLiterals.add(
                    model.createLiteral(value, literal.getLanguage()));
            retractions.add(stmt);
        }

        List<Statement> titles = model.listStatements(subject, 
                titleProp.property(), (RDFNode) null).toList();
        for (Statement stmt : titles) {
            convertTitle(stmt, titleLiterals);
        }
         
        // Create a new title object for any titleStatement literals that 
        // haven't matched an existing title.
        // NB We don't have to test each of these literals against one another,
        // because if they're identical Jena will already have removed the 
        // duplicates. If they're not identical (e.g., due to a language value),
        // they should both be retained.
        for (Literal literal : titleLiterals) {
            LOGGER.debug("Creating new title with label " + literal.toString());
            createTitle(literal);            
        }
        
        applyRetractions();           
    }

    // TODO This method is also used by BfWorkConverter. Need to combine.
    private void convertTitle(Statement statement, 
            List<Literal> titleLiterals) {

        BfResourceConverter converter = 
                new BfTitleConverter(this.localNamespace);
        
        // Identify the title resource and build its associated model (i.e.,
        // statements in which it is the subject or object).
        Resource title = BibframeConverter.getSubjectModelToConvert(
                statement.getResource());

        assertions.add(converter.convertSubject(title));
        
        Literal label = title.getProperty(RDFS.label).getLiteral();
        if (label != null) {
            boolean removed = titleLiterals.removeIf(i -> label.sameValueAs(i));
            LOGGER.debug(removed 
                    ? "Found a match for bf:titleStatement '" 
                    + label.toString() + "': removing"
                    : "No bf:titleStatement matching title '" 
                    + label.toString() + "'");
        }
    }
    
    private void createTitle(Literal label) {
        
        BfTitleConverter converter = new BfTitleConverter(this.localNamespace);
        assertions.add(converter.create(subject, label));
    }
}