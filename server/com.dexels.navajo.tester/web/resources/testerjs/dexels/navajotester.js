// Will hold the XML document
var xml;

function getScripts() {
    var scriptssource = $("#scripts-template").html();
    var scriptstemplate = Handlebars.compile(scriptssource);

    var foldersource = $("#folder-template").html();
    var foldertemplate = Handlebars.compile(foldersource);

    Handlebars.registerPartial('subscripts', foldertemplate);

    $.getJSON("/testerapi?query=getscripts", function(data) {
        data.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        });
        $("#scripts").html(scriptstemplate(data));


        // $(document).on('click', '.folder', function(e){
        // if ($(this).children().length > 0 ) {
        // $(this).children().remove();
        // } else {
        // // Find higest element
        // var entries = getMyEntries(data, $(this))
        // $( this ).append(foldertemplate(entries));
        // }
        // e.stopPropagation();
        // });

    });

};

function runScript(scriptElement) {
    var instance =  $( "#handlers option:selected" ).text();
    if (instance === "") {
        $('#handlers_chosen').twinkle();    
        return;
    }
    $('.overlay').show();
   
    var script = scriptElement.attr("id");
    $('#loadedScript').text(script);

    var navajoinput = "";
    if (typeof xml != 'undefined') {
        navajoinput = (new XMLSerializer()).serializeToString(xml);
    }

    $.post("/testerapi?query=run&service=" + script, navajoinput, function(data) {
        $('#scriptcontent').removeClass('prettyprinted');
        $('#scriptcontent').text(data)
        prettyPrint();
        parseTmlToHtml($('#HTMLview'), $('#methods'), data);
        $('#HTMLview').show(100);
        $('#TMLview').hide(100);
        $('#TMLSourceview').hide(100);
        $('.overlay').hide(500);
        
        $('html, body').animate({
            scrollTop : 0
        }, 'fast');

    });

    $.get("/testerapi?query=getfilecontent&file=" + script, function(data) {
        $('#scriptsourcecontent').removeClass('prettyprinted');
        $('#scriptsourcecontent').text(data)
        prettyPrint();
    });

}

function updateVisibility(filter, element) {
    var anyMatch = false;

    element.children('li').each(function() {
        var scriptid = $(this).children('div').first().attr('id');
        var className = $(this).children('div').first().attr('class');

        var match = scriptid.search(new RegExp(filter, "i"));
        var isFolder = className === 'folder';

        if (isFolder) {
            // First check for text search
            var match = $(this).text().search(new RegExp(filter, "i"));
            if (match < 0) {
                // no need to check children at all
                $(this).hide()
            } else {
                var childHasMatches = updateVisibility(filter, $(this).children('ul').first());
                if (childHasMatches) {
                    $(this).show()
                    anyMatch = true;
                } else {
                    $(this).hide()
                }
            }

        } else {
            if (match < 0) {
                $(this).hide()
            } else {
                $(this).show()
                anyMatch = true;
            }
        }
    });
    return anyMatch;
}

function getMyEntries(data, element) {
    if (element.parents('.folder').length < 1) {
        var itemsIndex = data.indexOfPath(element.attr('id'));
        var subEntries = data[itemsIndex].entries;
        subEntries.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        });
        return subEntries;
    } else {
        // Find my parents entries and check him
        var myParent = element.parents('.folder').first();
        var myParentEntries = getMyEntries(data, myParent);
        var itemsIndex = myParentEntries.indexOfPath(element.attr('id'));
        var subEntries = myParentEntries[itemsIndex].entries;
        subEntries.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        });
        return subEntries;

    }
};

/* Event handlers */

$(document).on('click', '.script', function() {
    runScript($(this));
});

$(document).on('click', '#HTMLviewLink', function() {
    $('#HTMLview').show(100);
    $('#TMLview').hide(100);
    $('#TMLSourceview').hide(100);
    return false;
});

$(document).on('click', '#TMLviewLink', function() {
    $('#HTMLview').hide(100);
    $('#TMLview').show(100);
    $('#TMLSourceview').hide(100);
    return false;
});

$(document).on('click', '#TMLSourceviewLink', function() {
    $('#HTMLview').hide(100);
    $('#TMLview').hide(100);
    $('#TMLSourceview').show(100);
    return false;
});

$(document).on('input propertychange', '#scriptsFilter', function(evt) {
    // If it's the propertychange event, make sure it's the value that
    // changed.
    if (window.event && event.type == "propertychange" && event.propertyName != "value")
        return;

    var filter = $("#scriptsFilter").val();
    if (filter.length < 3) 
        return;
    
    // Clear any previously set timer before setting a fresh one
    window.clearTimeout($(this).data("timeout"));
    $(this).data("timeout", setTimeout(function() {
       
        updateVisibility(filter, $(".scripts"))
    }, 300));
});

// TML change events
$(document).on('input propertychange', '.tmlinputtext', function(evt) {
    // If it's the propertychange event, make sure it's the value that changed.
    if (window.event && event.type == "propertychange" && event.propertyName != "value")
        return;
    var xpath = $(this).attr('id');
    
    var element = xml.evaluate( xpath, xml, null, XPathResult.ANY_UNORDERED_NODE_TYPE  , null ).singleNodeValue;
   
    if (typeof element != 'undefined') {
        var $element = $(element);
        $element.attr('value',  $(this).val());
    } 
});



$(document).on('input change', '.tmlinputcheckbox', function(evt) {
    var xpath = $(this).attr('id');
    var element = xml.evaluate( xpath, xml, null, XPathResult.ANY_UNORDERED_NODE_TYPE  , null ).singleNodeValue;
    if (typeof element != 'undefined') {
        var $element = $(element);
        $element.attr('value',  $(this).prop('checked'));
    } 
});

$(document).on('input change', '.tmlinputselect', function(evt) {
    var  xpath = $(this).attr('id');
    var $input = $(this);
    var element = xml.evaluate( xpath, xml, null, XPathResult.ANY_UNORDERED_NODE_TYPE  , null ).singleNodeValue;
   
    
    if (typeof element != 'undefined') {
        var $element = $(element);
        $element.children('option').each(function() {
            // find option under current input
            var value = $(this).attr('value');
            
            // See whether the option with this name is now selected
            var isChecked = $input.children('option[value="'+value+'"]').first().prop('selected');
            if (isChecked) {
                $(this).attr('selected', 1);
            } else {
                $(this).attr('selected', 0);
            }   
        }); 
    } 
});


/* Some helpers */
Array.prototype.indexOfPath = function(path) {
    for (var i = 0; i < this.length; i++)
        if (this[i].path === path)
            return i;
    return -1;
}