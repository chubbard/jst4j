package jst.spring;

import jst.TemplateContext;
import jst.FileTemplateLoader;
import jst.ScriptExecution;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.io.File;

import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

public class JavascriptTemplateBean implements ServletContextAware {
    private TemplateContext context;

    private Map<String,Object> variables;
    private Map<String,Object> mixins;
    private List<String> resourcePaths = new ArrayList<String>();
    private ServletContext servletContext;

    public JavascriptTemplateBean() {
        variables = new HashMap<String,Object>();
        mixins = new HashMap<String,Object>();
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getMixins() {
        return mixins;
    }

    public void setMixins(Map<String, Object> mixins) {
        this.mixins = mixins;
    }

    public List<String> getResourcePaths() {
        return resourcePaths;
    }

    public void setResourcePaths(List<String> resourcePaths) {
        this.resourcePaths = resourcePaths;
    }

    public ScriptExecution load( String url ) throws IOException {
        initializeContext();

        ScriptExecution execution = context.load( url );

        for( String name : variables.keySet() ) {
            execution.addVariable( name, variables.get( name ) );
        }

        for( String name : mixins.keySet() ) {
            execution.mixin( name, mixins.get( name ) );
        }

        return execution;
    }

    public Object evaluate( String url, Map<String,Object> data ) throws IOException {
        ScriptExecution execution = load(url);
        for( String key : data.keySet() ) {
            execution.addVariable( key, data.get(key) );
        }
        return execution.invoke();
    }

    private void initializeContext() throws IOException {
        if( context == null ) {
            context = new TemplateContext();
            for( String resourcePath : resourcePaths ) {
                context.addLoader( new FileTemplateLoader(getResource(resourcePath)) );
            }
        }
    }

    private File getResource(String resourcePath) {
        return new File( servletContext != null ? servletContext.getRealPath( resourcePath ) : resourcePath );
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
