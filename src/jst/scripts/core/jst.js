Object.prototype.merge = function( obj, overwrite ) {
    for( var key in obj ) {
        if( !obj.constructor.prototype[key] && (typeof this[key] == "undefined" || overwrite) ) {
            this[key] = obj[key];
        }
    }
    return this;
};

Object.prototype.properties = function() {
    return Iterator( this._propertiesGenerator() );
};

Object.prototype._propertiesGenerator = function() {
    for( var key in this ) {
        if( !this.constructor.prototype[key] ) {
            yield key;
        }
    }
};

String.prototype.format = function() {
    var expr = /{(\d+)}/gi;
    var index = 0;
    var result = "";
    var match = null;
    do {
        match = expr.exec( this );
        if( match ) {
            result += this.substring( index, match.index ) + arguments[ match[1] ];
            index = match.index + match[0].length;
        } else {
            result += this.substring( index );
        }
    } while( match );
    return result;
};

String.prototype.xml = function() {
    return StringUtil.escapeXml( this );
};

String.prototype.html = function() {
    return StringUtil.sanitize( StringUtil.escapeHtml( this ) );
};

Function.prototype.delegate = function( ) {
    var _method = this;
    var args = Array.prototype.slice.call(arguments);
    var scope = args.shift();
    return function() {
        _method.apply( scope, args );
    };
};

Function.prototype.curry = function() {
    if (!arguments.length) return this;
    var _method = this;
    var args = Array.prototype.slice.call(arguments);
    return function() {
      return _method.apply(this, args.concat( Array.prototype.slice.call(arguments) ) );
    };
};


if( !Array.prototype.reduce ) {
    Array.prototype.reduce = function( reducer, initial ) {
        var reduction = initial;
        for( var i = 0; i < this.length; i++ ) {
            reduction = reducer.call( this, reduction, this[i], i, this );
        }
        return reduction;
    };
}

Array.prototype.format = function( template ) {
    return this.map( function( item ) {
        return template.format( item );
    } );
}


