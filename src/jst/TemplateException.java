package jst;

import org.mozilla.javascript.RhinoException;

public class TemplateException extends RuntimeException {
    private ServerSideTemplate template;
    private int lineNumber = -1;


    public TemplateException( ServerSideTemplate template, String message ) {
        super( message );
        this.template = template;
    }

    public TemplateException(ServerSideTemplate serverSideTemplate, RhinoException ex) {
        super( ex.getMessage(), ex );
        this.template = serverSideTemplate;
        this.lineNumber = ex.lineNumber();
    }

    public ServerSideTemplate getTemplate() {
        return template;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer( super.toString() );
        buffer.append( "\nTemplate source for ");
        buffer.append( template.getName() );
        buffer.append( ":\n" );
        buffer.append( template );
        return buffer.toString();
    }
}
