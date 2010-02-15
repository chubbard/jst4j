function Template( templateFunction ) {
    this.__output = [];
    this.__template = templateFunction;
}

Template.prototype = {
    __evaluate : function() {
        if( this.__layout ) {
            this.contentForLayout = this.__template.call( this );
            return this.__layout.call( this );
        } else {
            return this.__template.call( this );
        }
    },
    collectContentFor : function( name, callback ) {
        this[name] = callback;
    },
    render : function( partial, options ) {
        return context.execute( partial, options );
    },
    include : function( jsScript ) {
        context.include( jsScript );
    },
    layout : function( layout ) {
        context.setLayout( layout );
        return this._layout.call( this );
    }
};

