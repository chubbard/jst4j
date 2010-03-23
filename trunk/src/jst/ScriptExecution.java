package jst;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ScriptExecution {
    private static final Logger logger = Logger.getLogger( ScriptExecution.class );

    private TemplateContext context;
    private ServerSideTemplate template;
    private Context jsContext;
    private Scriptable scope;

    public ScriptExecution( TemplateContext context ) {
        this( null, context );
    }

    public ScriptExecution( ServerSideTemplate template, TemplateContext context ) {
        this.template = template;
        this.context = context;
        this.jsContext = Context.enter();
        this.jsContext.setLanguageVersion(TemplateContext.JAVASCRIPT_VERSION);

        scope = jsContext.newObject( context.getParent() );
        scope.setPrototype( context.getParent() );
        scope.setParentScope( null );

        addVariable( "context", this );
        addVariable( "logger", logger );
    }

    public void setLanguageVersion( int version ) {
        if( Context.isValidLanguageVersion( version ) ) {
            jsContext.setLanguageVersion( version );
        }
    }

    public int getLanguageVersion() {
        return jsContext.getLanguageVersion();
    }

    public Object invoke() throws IOException {
        try {
            logger.debug( "Invoking script " + template.getName() );
            return template.execute( scope, jsContext );
        } finally {
            Context.exit();
        }
    }

    public ScriptExecution mixin(String mixinName, Object mixin) {
        addVariable( "Template.__" + mixinName, mixin );

        Set<String> methods = new HashSet<String>();

        Class mixinClass = mixin.getClass();

        while( mixinClass != Object.class ) {
            for(Method method : mixinClass.getMethods() ) {
                if( !methods.contains( method.getName() ) ) {
                    String script = String.format("Template.%1$s = function() { return this.__%2$s[\"%1$s\"].apply( this.__%2$s, arguments ); }", method.getName(), mixinName );
                    jsContext.evaluateString( scope, script, "Mixin " + mixinName + "." + method.getName() + "()", 1, null );
                    methods.add( method.getName() );
                }
            }
            mixinClass = mixinClass.getSuperclass();
        }
        return this;
    }

    public ScriptExecution addVariable(String varName, Object variable) {
        if( logger.isDebugEnabled() ) {
            logger.debug( "Adding variable " + varName );
        }
        Scriptable current = scope;
        String[] varPath = varName.split("\\.");
        for( int i = 0; current != null && i < varPath.length; i++ ) {
            if( i + 1 >= varPath.length ) {
                ScriptableObject.putProperty( current, varPath[i], Context.javaToJS( variable, current ) );
                break;
            } else {
                Object value = ScriptableObject.getProperty( current, varPath[i] );
                if( value instanceof Scriptable ) {
                    current = (Scriptable)value;
                } else {
                    current = null;
                }
            }
        }
        return this;
    }

    public Object execute(Script script) {
        return script.exec( jsContext, scope );
    }

    public Object execute( String url, Scriptable data ) throws IOException {
        if( !url.endsWith(".jst") ) {
            url += ".jst";
        }
        ScriptExecution execution = context.load( url );        
        return execution.evaluate( data );
    }

    public void include(String url) throws IOException {
        Script script = context.loadScript( jsContext, url );
        script.exec( jsContext, scope );
    }

    public void setLayout(String url) throws IOException {
        ServerSideTemplate layout = context.loadTemplate( url );
        layout.includeAsLayout( jsContext, scope );
    }

    public Object evaluate(Scriptable data) throws IOException {
        addVariable( "params", data );
        return invoke();
    }

    public Object evaluate( String script ) throws IOException {
        return jsContext.evaluateString( scope, script, "command line", 1, null );
    }

    public boolean stringIsCompilableUnit(String line) {
        return jsContext.stringIsCompilableUnit(line);
    }
}
