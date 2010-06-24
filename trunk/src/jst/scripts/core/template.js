function Template( templateFunction ) {
    this.__output = [];
    this.__template = templateFunction;
}

Template.prototype = {
    __evaluate : function() {
        if( this.__layout ) {
            this.contentForLayout = this.__template.call( this );
            logger.info("Rendering with layout.");
            return this.__layout.call( this );
        } else {
            logger.info("Rendering without layout.");
            return this.__template.call( this );
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
        return context.execute( partial, options );
    },
    include : function( jsScript ) {
        context.include( jsScript );
    },
    layout : function( layout ) {
        context.setLayout( layout );
        return this.__layout.call( this );
    }
};

