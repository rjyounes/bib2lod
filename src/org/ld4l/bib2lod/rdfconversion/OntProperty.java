package org.ld4l.bib2lod.rdfconversion;


public enum OntProperty {

    BF_AUTHORIZED_ACCESS_POINT(OntNamespace.BIBFRAME, "authorizedAccessPoint"),
    BF_HAS_AUTHORITY(OntNamespace.BIBFRAME, "hasAuthority"),
    BF_IDENTIFIER(OntNamespace.BIBFRAME, "identifier"),
    BF_IDENTIFIER_VALUE(OntNamespace.BIBFRAME, "identifierValue"),
    BF_LABEL(OntNamespace.BIBFRAME, "label"),
    BF_SYSTEM_NUMBER(OntNamespace.BIBFRAME, "systemNumber"),
    MADSRDF_AUTHORITATIVE_LABEL(OntNamespace.MADSRDF, "authoritativeLabel"),
    MADSRDF_IS_MEMBER_OF_MADS_SCHEME(
            OntNamespace.MADSRDF, "isMemberOfMADSScheme"),
    OWL_SAME_AS(OntNamespace.OWL, "sameAs");

    
    private final OntNamespace namespace;
    private final String localname;
    private final String uri;
    
    OntProperty(OntNamespace namespace, String localname) {
        // Or should this be a Namespace?
        this.namespace = namespace;
        this.localname = localname;
        this.uri = namespace.uri() + localname;
    }
    
    public OntNamespace namespace() {
        return this.namespace;
    }
    
    public String namespaceUri() {
        return this.namespace.uri();
    }
    
    public String localname() {
        return this.localname;
    }
    
    public String uri() {
        return this.uri;
    }
    
    public String prefixedForm() {
        return this.namespace.prefix() + ":" + this.localname;
    }
    
    public String sparqlUri() {
        return "<" + this.uri + ">";
    }
}