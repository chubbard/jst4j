package jst.shell;

import jst.TemplateContext;
import jst.FileTemplateLoader;
import jst.ScriptExecution;

import java.io.IOException;
import java.io.File;
import java.io.LineNumberReader;
import java.io.InputStreamReader;

import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Context;

public class Shell {

    TemplateContext templateContext;
    ScriptExecution execution;

    public Shell() throws IOException {
        templateContext = new TemplateContext();
        templateContext.addLoader( new FileTemplateLoader( new File(".") ) );

        execution = templateContext.start();
        execution.include("core/shell.js");
//        execution.evaluate("function include( url ) { context.include( url ); }");
//        execution.evaluate("function exit( code ) { code = code || 0; java.lang.System.exit( code ); }");
//        execution.evaluate("function print() { for( var i = 0; i < arguments.length; i++ ) { java.lang.System.out.print( arguments[i] ); } java.lang.System.out.println(); }");
    }

    public Object eval( String input ) throws IOException {
        return execution.evaluate( input );
    }

    private String readCommand(LineNumberReader in, int numOfCommand ) throws IOException {
        String command = "";
        String line = null;
        do {
            line = in.readLine();
            command += line + "\n";
            if( line != null && execution.stringIsCompilableUnit(command) ) {
                return command;
            }
            System.out.print( numOfCommand + " * ");
        } while( line != null );

        return null;
    }

    public static void main(String[] args) throws IOException {
        Shell shell = new Shell();

        for( String arg : args ) {
            shell.templateContext.addLoader( new FileTemplateLoader( new File( arg ) ) );
        }

        LineNumberReader in = new LineNumberReader( new InputStreamReader( System.in ) );
        boolean done = false;
        int command = 1;
        do {
            try {
                System.out.print( command + " > ");
                String source = shell.readCommand( in, command );
                if( source != null ) {
                    Object val = shell.eval( source );
                    String result = Context.toString(val);
                    System.out.println( result );
                } else {
                    done = true;
                }
            } catch( EcmaError error ) {
                System.out.println( "On line: " + error.lineNumber() + ": " + error.getErrorMessage() );
            } catch( EvaluatorException ex ) {
                System.out.println("On line: " + ex.lineNumber() + ": " + ex.getMessage() );
            }
            command++;
        } while( !done );
    }
}
