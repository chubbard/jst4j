function Template( templateFunction, templateObject ) {
    this.__output = [];
    this.__template = templateFunction;
    this.__templateObject = templateObject;
//    this.__scripts = [];
//    this.__styles = [];
}

Template.prototype = {
    evaluate : function() {
        this.contentForLayout = this.__template.apply( this, Array.fromArguments(arguments) );
        if( this.__layout ) {
            logger.info("Rendering with layout.");
            return this.__layout.__template.call( this );
        } else {
            logger.info("Rendering without layout.");
            return this.contentForLayout;
        }
    },
    collectContentFor : function( name, callback ) {
        if( !(callback instanceof Function) ) {
            this[name] = function() { return callback; }
        } else {
            this[name] = callback;
        }
    },
    render : function( partial, options ) {
        var javaTemplateObject = runtime.read( partial );
        var template = new Template( eval( '' + javaTemplateObject.getName() ), javaTemplateObject );
        var formalParams = template.getFormalParameters();
        var actualParams = [];
        for each( var p in Iterator(formalParams) ) {
            actualParams.push( options[p] );
        }
        return template.evaluate.apply( template, actualParams );
    },
    getFormalParameters: function() {
      return this.__templateObject.getFormalParameters();  
    },
    include : function( jsScript ) {
        runtime.include( jsScript );
    },
    layout : function( layout ) {
        var javaTemplateObject = runtime.read( layout );
        this.__layout = new Template( eval( '' + javaTemplateObject.getName() ), javaTemplateObject );
//    },
//    script : function( url, options ) {
//        this.__scripts.push( Html.script(url,options) );
//    },
//    css : function( url, options ) {
//        this.__styles.push( Html.css(url,options) );
    }
};

