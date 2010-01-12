function include( url ) {
    context.include( url );
}

function exit( code ) {
    code = code || 0;
    java.lang.System.exit( code );
}

function print() {
    for( var i = 0; i < arguments.length; i++ ) {
        java.lang.System.out.print( arguments[i] );
    }
    java.lang.System.out.println();
}
