package jst;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.MessageFormat;

public class ServerSideTemplate {
    private static final Pattern jsDelimeters = Pattern.compile("<[%*]={0,2}(.+?)-?[*%]>", Pattern.MULTILINE );
    private static final Logger logger = Logger.getLogger( ServerSideTemplate.class );

    private String name;
    private String url;
    private String generatedSource;
    private TemplateLoader sourceLoader;
    private Script script;
    private long compiledTimestamp;
    private boolean isDebug = true;
    private String sanitizingFunction = null;

    public ServerSideTemplate(String url, TemplateLoader scriptLoader, boolean isDebug ) throws FileNotFoundException {
        this.url = url;
        this.sourceLoader = scriptLoader;
        this.name = toFunctionName();
        this.isDebug = isDebug;
    }

    private String createPageScript( CharSequence template ) {
        StringBuilder currentBuffer = new StringBuilder();
        Matcher matcher = jsDelimeters.matcher( template );
        String functionDef = null, functionEnding = null;
        int start = 0;
        while( start != template.length() ) {
            if( matcher.find( start ) ) {
                int plainTextEnd = matcher.start();

                currentBuffer.append( createOutput( template.subSequence( start, plainTextEnd ) ) );
                String expression = matcher.group();
                String scriptletContent = matcher.group(1);

                if( expression.startsWith("<%==") ) {
                    currentBuffer.append( createInlineExpression( scriptletContent, false ) );
                } else if( expression.startsWith("<%=") ) {
                    currentBuffer.append( createInlineExpression( scriptletContent, true ) );
                } else if( expression.startsWith("<*") ) {
                    if( functionDef == null ) {
                        functionDef = scriptletContent;
                    } else if( functionEnding == null ) {
                        functionEnding = scriptletContent;
                    } else {
                        throw new TemplateException( this, "<* *> scriptlets have already been used for function definition and function termination." );
                    }
                } else {
                    addExpresion(currentBuffer, scriptletContent);
                }
                start = matcher.end();
                if( expression.endsWith("-%>") ) {
                    start = eatRemainingWhitespace(start, template, currentBuffer);
                }

            } else if( start < template.length() ) {
                currentBuffer.append( createOutput( template.subSequence( start, template.length() ) ) );
                start = template.length();
            } else {
                start = template.length();
            }
        }
        wrapFunction(currentBuffer, functionDef, functionEnding);

        this.compiledTimestamp = System.currentTimeMillis();
        return currentBuffer.toString();
    }

    private int eatRemainingWhitespace(int start, CharSequence template, StringBuilder currentBuffer) {
        for( int i = start; i < template.length(); i++ ) {
            if( template.charAt(i) == '\n' ) {
                currentBuffer.append( createOutput( template.subSequence( start, i ).toString().trim() ) );
                start = i + 1;
                break;
            } else if( !Character.isWhitespace( template.charAt(i) ) ) {
                break;
            }
        }
        return start;
    }

    private void wrapFunction(StringBuilder currentBuffer, String functionDef, String functionEnding) {
        createFunctionDefinition( currentBuffer, functionDef );
        currentBuffer.append( "return this.__output.join('');\n");
        if( functionDef != null & functionEnding == null ) {
            logger.warn("Template {0} uses a function definition scriplet (i.e. <* *> tags) but doesn't close it. with an ending definition.  It will be assumed closed, but it's not a great practice to be so loose.");
        }
        currentBuffer.append( "}\n" );
    }

    private void createFunctionDefinition(StringBuilder builder, String functionDefinition ) {
        if( functionDefinition == null ) {
            functionDefinition = "function() {";
        }
        builder.insert( 0, MessageFormat.format("Template.prototype.{0} = {1}\n", name, functionDefinition ) );
    }

    private String toFunctionName() {
        return "_" + StringUtil.toCamelCase( stripExtension( url ) ).replaceAll( escapeSeperator(), "_" );
    }

    private String escapeSeperator() {
        return File.separator.equals("\\") ? "\\\\" : File.separator;
    }

    private String stripExtension(String scriptFile) {
        int extension = scriptFile.lastIndexOf('.');
        int start = scriptFile.indexOf("/") + 1;
        if( extension > 0 ) {
            return scriptFile.substring(start, extension);
        } else {
            return scriptFile.substring(start);
        }
    }

    private void addExpresion(StringBuilder currentBuffer, String scriptletContent) {
        if( scriptletContent.length() > 0 ) {
            currentBuffer.append( scriptletContent );
            currentBuffer.append( "\n" );
        }
    }

    private CharSequence createInlineExpression(String inlineExpression, boolean sanitize) {
        if( inlineExpression.length() > 0 ) {
            inlineExpression = stripTrailingSemiColon(inlineExpression);
            if( sanitize && sanitizingFunction != null ) {
                return "this.__output.push( " + sanitizingFunction + "( " + inlineExpression + ") );\n";
            } else {
                return "this.__output.push( " + inlineExpression + ");\n";
            }
        } else {
            return "";
        }
    }

    private String stripTrailingSemiColon(String inlineExpression) {
        inlineExpression = inlineExpression.trim();
        if( inlineExpression.endsWith(";") ) {
            inlineExpression = inlineExpression.substring(0, inlineExpression.length() - 1 );
        }
        return inlineExpression;
    }

    private CharSequence createOutput(CharSequence charSequence) {
        if( charSequence.length() > 0 ) {
            return "this.__output.push('" + StringEscapeUtils.escapeJavaScript( charSequence.toString() ) + "' );\n";
        } else {
            return "";
        }
    }

    private StringBuffer readTemplate(InputStream scriptStream) throws IOException {
        StringBuffer temp = new StringBuffer();
        try {
            LineNumberReader reader = new LineNumberReader( new InputStreamReader( scriptStream ) );
            String line;
            while( (line = reader.readLine() ) != null ) {
                temp.append(line);
                temp.append("\n");
            }
        } finally {
            scriptStream.close();
        }
        return temp;
    }

    public boolean shouldRefresh() {
        return script == null || ( isDebug && sourceLoader.shouldRefresh( url, compiledTimestamp ) );
    }

    public synchronized Script compile( Context cx ) throws IOException {
        try {
            if( logger.isInfoEnabled() ) {
                logger.info("Compiling view for url: " + url );
            }
            script = cx.compileString( createTemplate(), url, 1, null );
            return script;
        } catch( EvaluatorException ex ) {
            throw new TemplateException( this, ex );
        } finally {
            if( logger.isDebugEnabled() ) {
                logger.debug( getGeneratedSource() );
            }
        }
    }

    public String toString() {
        try {
            return createTemplate();
        } catch( IOException e ) {
            throw (RuntimeException)new RuntimeException( e.getMessage() ).initCause( e );
        }
    }

    private String createTemplate() throws IOException {
        StringBuffer template = readTemplate( sourceLoader.load( url ) );
        this.generatedSource = createPageScript( template);
        return generatedSource;
    }

    public Script getScript() {
        return script;
    }

    public String getName() {
        return name;
    }

    public String getGeneratedSource() {
        return generatedSource;
    }

    public Object execute(Scriptable scope, Context context) throws IOException {
        include(context, scope);
        return context.evaluateString( scope, MessageFormat.format("new Template( Template.prototype.{0} ).__evaluate();", name), "jst_evaluate", 1, null );
    }

    public void include(Context context, Scriptable scope) throws IOException {
        synchronized( this ) {
            if( shouldRefresh() ) {
                compile( context );
            }
        }
        getScript().exec( context, scope );
    }

    public void includeAsLayout( Context context, Scriptable scope ) throws IOException {
        include( context, scope );
        context.evaluateString( scope, MessageFormat.format("Template.prototype.__layout = Template.prototype.{0};", name), "jst_layout", 1, null );
    }


    public String getSanitizingFunction() {
        return sanitizingFunction;
    }

    public void setSanitizingFunction(String sanitizingFunction) {
        this.sanitizingFunction = sanitizingFunction;
    }

    public static void main( String[] args ) throws IOException {
        FileTemplateLoader loader = new FileTemplateLoader( new File( "web/resources" ) );
        ServerSideTemplate script = new ServerSideTemplate( "timeOfDay.jst", loader, true);
        System.out.println( script.toString() );

//        File layoutFile = new File( "web/resources/default.jst" );
//        ServerSideTemplate layout = new ServerSideTemplate( "asset/list", layoutFile, true);
//        System.out.println( layout.toString() );
    }

}
