importPackage( Packages.jst );

var Html = {
    tag : function( tagname, attrs, body ) {
        var t = [ "<", tagname ];
        for( var key in attrs ) {
            if( attrs[key]  && !(attrs[key] instanceof Function) ) {
                t.push( ' ' );
                t.push( key );
                t.push( '="' );
                t.push( attrs[key] );
                t.push( '"');
            }
        }
        if( body ) {
            t.push(">");
            if( body instanceof String ) {
                t.push(body);
            } else if( body instanceof Array ) {
                t.push( body.join("\n") );
            } else {
                t.push( body );
            }
            t.push("</");
            t.push(tagname);
            t.push(">");
        } else {
            t.push("/>");
        }
        return t.join("");
    },
    css : function( url, options ) {
        options = options || {};
        options.media = options.media || 'screen';
        options.href = url;
        options.rel = 'Stylesheet';
        options.type = 'text/css';

        return this.tag( 'link', options );
    },
    script : function( url, options ) {
        options = options || {};
        options.src = url;
        options.type = 'text/javascript';
        return this.tag('script', options, ' ' );
    },
    link : function( url, text, options ) {
        options = options || {};
        options.href = url;

        return this.tag('a', options, text );
    },
    html : function( value ) {
        return StringUtil.sanitize( value );
    }
};

var Form = {
    start : function( url, options ) {
        options = options || {};
        options.action = url;
        options.method = options.method || "post";
        return Html.tag( "form", options, ' ' );
    },
    textfield : function( name, options ) {
        options = options || {};
        options.type = "text";
        options.name = name;
        return Html.tag( "input", options );
    },
    radio : function( name, value, checked, options ) {
        options = options || {};
        options.type = "radio";
        options.name = name;
        options.value = value;
        if( checked ) {
            options.checked = "checked";
        }
        return Html.tag( "input", options );
    },
    checkbox : function( name, value, checked, options ) {
        options = options || {};
        options.type = "checkbox";
        options.name = name;
        options.value = value;
        if( checked ) {
            options.checked = "checked";
        }
        return Html.tag( "input", options );
    },
    button : function( name, value, options ) {
        options = options || {};
        options.type = "button";
        options.name = name;
        options.value = value;
        return Html.tag( "input", options );
    },
    submit : function( name, value, options ) {
        options = options || {};
        options.type = "submit";
        options.name = name;
        options.value = value;
        return Html.tag( "input", options );
    },
    select : function( name, keys, select, options ) {
        options = options || {};
        options.name = name;
        // todo keys should just be a map possibly, or something simple (not array of maps)
        var opts = keys.map( function( item, index, arr ) {
            if( select && select == item.value || select == index ) {
                return Html.tag("option", { value: item.value }, item.text )
            } else {
                return Html.tag("option", { value: item.value, selected: "selected" }, item.text )
            }
        } );

        return Html.tag( "select", options, opts );
    },
    textarea : function( name, rows, cols, options ) {
        options = options || {};
        options.name = name;
        options.rows = rows;
        options.cols = cols;
        return Html.tag( "textarea", options );
    },
    password : function( name, options ) {
        options = options || {};
        options.type = "password";
        options.name = name;
        return Html.tag( "input", options );
    },
    endForm : function() {
        return "</form>";
    },
    fileUpload : function( name, options ) {
        options = options || {};
        options.name = name;
        options.type = "file";
        
        return Html.tag("input", options );
    },
    hidden : function( name, value, options ) {
        options = options || {};
        options.name = name;
        options.type = "hidden";
        if( value ) {
            options.value = value;
        }

        return Html.tag("input", options );
    },
    imageButton : function( name, src, options ) {
        options = options || {};
        options.name = name;
        options.src = src;
        options.type = "image";
        return Html.tag( "input", options );
    }
};
