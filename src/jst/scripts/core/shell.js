/**
 * Creates a Template object from the given URL.  The url should be within the file path
 * understood by the shell.
 *
 * @param url a path relative to one of paths registered in the shell.
 */
function load( url ) {
    var javaTemplateObject = runtime.read( url );
    var template = new Template( eval('' + javaTemplateObject.getName()), javaTemplateObject );
    return template;
}

/**
 * Function that will exit the shell.
 *
 * @param code optional parameter used to set as the return code for the process.
 */
function exit( code ) {
    code = code || 0;
    java.lang.System.exit( code );
}

/**
 * Prints the given arguments to the shell separating them by a single space.
 */
function print() {
    for( var i = 0; i < arguments.length; i++ ) {
        shell.print( arguments[i] );
        shell.print( ' ' );
    }
    shell.println();
}

/**
 * Returns the version of Javascript language in use by the interpreter.
 */
function version() {
    if( arguments.length > 0 ) {
        runtime.setLanguageVersion( arguments[0] );
    }
    return runtime.getLanguageVersion();
}
