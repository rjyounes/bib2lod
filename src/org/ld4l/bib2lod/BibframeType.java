package org.ld4l.bib2lod;


public enum BibframeType {
    
    ANNOTATION ("Annotation"),
    AUTHORITY ("Authority"),
    INSTANCE ("Instance"),
    ORGANIZATION ("Organization"),
    // External namespace: e.g.
    // CREATOR ("
    PERSON ("Person"),
    TITLE ("Title"),
    TOPIC ("Topic"),
    WORK ("Work");

    private final String namespace;
    private final String localname;
    // private final String uri;

    /** 
     * Constructor for types in external namespaces.
     * 
     * @param namespace
     * @param localname
     */
    BibframeType(String namespace, String localname) {
        this.namespace = namespace;
        this.localname = localname;
        // this.uri = namespace + localname;
    }
    
    /**
     * Constructor for types in Bibframe namespace.
     * @param localname
     */
    BibframeType(String localname) {
        this(Ontology.BIBFRAME.namespace(), localname);
    }
    
    public String uri() {
        // return this.uri;
        return this.namespace + this.localname;
    }
    
    public String namespace() {
        return this.namespace();
    }
    
    public String localname() {
        return this.localname;
    }

}
