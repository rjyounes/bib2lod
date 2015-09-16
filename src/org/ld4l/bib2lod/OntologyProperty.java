package org.ld4l.bib2lod;

public enum OntologyProperty {

    BF_AUTHORIZED_ACCESS_POINT(Namespace.BIBFRAME, "authorizedAccessPoint"),
    BF_HAS_AUTHORITY(Namespace.BIBFRAME, "hasAuthority"),
    BF_IDENTIFIER(Namespace.BIBFRAME, "identifier");

    
    private final Namespace namespace;
    private final String localname;
    private final String uri;
    
    OntologyProperty(Namespace namespace, String localname) {
        // Or should this be a Namespace?
        this.namespace = namespace;
        this.localname = localname;
        this.uri = namespace + localname;
    }
    
    public Namespace namespace() {
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
}
