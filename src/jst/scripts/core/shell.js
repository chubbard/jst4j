function include( url ) {
    context.include( url );
}

function exit( code ) {
    code = code || 0;
    java.lang.System.exit( code );
}

function print() {
    for( var i = 0; i < arguments.length; i++ ) {
        shell.print( arguments[i] );
        shell.print( ' ' );
    }
    shell.println();
}

function version() {
    if( arguments.length > 0 ) {
        context.setLanguageVersion( arguments[0] );
    }
    return context.getLanguageVersion();
}
