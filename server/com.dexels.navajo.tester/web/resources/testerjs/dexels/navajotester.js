"use strict"

// Holds the input navajo document for the next RPC call
var xml = $.parseXML('<tml documentImplementation="SAXP"><header><transaction rpc_usr="" rpc_name="" rpc_pwd=""/> </header></tml>');;
var serializer = new XMLSerializer();

function getScripts() {
    var scriptssource = $("#scripts-template").html();
    var scriptstemplate = Handlebars.compile(scriptssource);

    var foldersource = $("#folder-template").html();
    var foldertemplate = Handlebars.compile(foldersource);

    Handlebars.registerPartial('subscripts', foldertemplate);

    $.getJSON("/testerapi?query=getscripts", function(data) {
        sortFileObject(data)
        $("#scripts").html(scriptstemplate(data));
    });

};

function sortFileObject(element) {
    $.each(element, function(index, subelem) {
        if (subelem.type  === 'FOLDER') {
           sortFileObject(subelem.entries);
        }
    });
  
    element.sort(function(a, b) {
        if (a.type === 'FILE' && b.type === 'FOLDER') {
            return 1;
        }
        return a.name.localeCompare(b.name);
    });
}

function processLoginForm(){
    hideLoginTable();
    sessionStorage.instance = $( "#handlers option:selected" ).text()
    sessionStorage.user =     $('#navajousername').val();
    sessionStorage.password = $('#navajopassword').val();
    
    $('#navajopassword').val('');
    
    if (sessionStorage.script && !loginTableVisible()) {
        runScript(sessionStorage.script);
    }
    
    return true;
}

function loginTableVisible() {
    var instance =  $( "#handlers option:selected" ).text();
    return (instance === "" || !sessionStorage.user) 
}

function updateInstanceHandlers() {
    if (!sessionStorage.instance) {
        return;
    }
    $('#handlers').val(sessionStorage.instance);
    $('#handlers').trigger("chosen:updated")
    
}

function showLoginTable() {
    $('#loginform').show();
    $('#showLessArrow').show();
    $('#showMoreArrow').hide();
}

function hideLoginTable() {
    $('#loginform').hide();
    $('#showLessArrow').hide();
    $('#showMoreArrow').show();
}

function runScript(script) {
   
    $('#loadedScript').text(script);
    
    if (loginTableVisible()) {
        showLoginTable();
       
        $('.LoginButton').attr('value', 'Run script');
        $('#logintable').trigger('startRumble');
        setTimeout(function(){$('#logintable').trigger('stopRumble');}, 750);
        return;
    }
    
    var instance =  $( "#handlers option:selected" ).text();
    try {
        hourglassOn();
        $('.overlay').show();
        
        // If we have sourcefile visible, show HTML page. Otherwise leave it
        if ($('#TMLSourceview').is(":visible")) {
            $('#TMLSourceview').hide();
            $('#HTMLview').show();
        }

        var navajoinput = prepareInputNavajo(script);
         
        $.post("/navajo/" + instance , navajoinput, function(xmlObj) {
            replaceXml(script, xmlObj);
            var stateObj = { script: script, xml:  serializer.serializeToString(xml) };
            history.pushState(stateObj, script, "tester.html?script="+script);
        });

    } catch(err) {
        console.log("Caugh error " +  err.message);
        $('#HTMLview')[0].innerHTML = "Error on running script: " + err.message;
        $('.overlay').hide();
        hourglassOff();
    }
    
    $.get("/testerapi?query=getfilecontent&file=" + script, function(data) {
        $('#scriptsourcecontent').removeClass('prettyprinted');
        $('#scriptsourcecontent').text(data)
        prettyPrint();
    });
}

function replaceXml(script, xmlObj) {
    xml = xmlObj;
    $('#scriptcontent').removeClass('prettyprinted');
    var xmltext = serializer.serializeToString(xmlObj)
    $('#scriptcontent').text(xmltext)
    prettyPrint();
    parseTmlToHtml(script, $('#HTMLview'), $('#methods'));
   
    $('.overlay').hide(200);
    $('#scriptMainView').show();
    hourglassOff();
    $('html, body').animate({
        scrollTop : 0
    }, 50);
}

function hourglassOn() {
    if ($('style:contains("html.wait")').length < 1) $('<style>').text('html.wait, html.wait * { cursor: wait !important; }').appendTo('head');
    $('html').addClass('wait');
}

function hourglassOff() {
    $('html').removeClass('wait');
}


function prepareInputNavajo(script) {
    var $xml = $(xml);
    var $transaction = $xml.find('tml header transaction')
   
    $transaction.attr('rpc_name', script);
    $transaction.attr('rpc_usr', sessionStorage.user);
    $transaction.attr('rpc_pwd', sessionStorage.password)
    
    return serializer.serializeToString(xml);
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
    var script =  $('#loadedScript').text();
    var stateObj = {script: script,  xml:  serializer.serializeToString(xml) };
    history.replaceState(stateObj, script, "tester.html?script=" + script);
    
    
    runScript($(this).attr("id"));
});

$(document).on('click', '.folder', function() {
    $(this).next().children('ul li').toggle();
});

$(document).on('click', '#showMoreArrow', function() {
    $('#loginform').show(200);
    $('#showLessArrow').show();
    $('#showMoreArrow').hide();
})

$(document).on('click', '#showLessArrow', function() {
    $('#loginform').hide(200);
    $('#showLessArrow').hide();
    $('#showMoreArrow').show();
})


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

$(document).on('click', '.messagediv h3', function() {
    $(this).closest('.messagediv').children('div').each(function() {
        if ($(this).attr('class') !== 'exportcsv') {
            $(this).toggle();
        }
        
    });
    return false;
});


$(document).on('input propertychange', '#scriptsFilter', function(evt) {
    // If it's the propertychange event, make sure it's the value that
    // changed.
    if (window.event && event.type == "propertychange" && event.propertyName != "value")
        return;

    
    // Clear any previously set timer before setting a fresh one
    window.clearTimeout($(this).data("timeout"));
    $(this).data("timeout", setTimeout(function() {
        var filter = $("#scriptsFilter").val();
        if (filter.length == 0) {
            getScripts();
            return;
        }
        
        if (filter.length < 3) 
            return;
        
        updateVisibility(filter, $(".scripts"))
    }, 300));
});

// TML change events
$(document).on('input propertychange', '.tmlinputtext', function(evt) {
    // If it's the propertychange event, make sure it's the value that changed.
    if (window.event && event.type == "propertychange" && event.propertyName != "value")
        return;
    var xpath = $(this).attr('id');
    
    $(document).xpath("*");
    //var element = xml.evaluate( xpath, xml, null, XPathResult.ANY_UNORDERED_NODE_TYPE  , null ).singleNodeValue;
     var element = $(xml).xpath(xpath);
    if (typeof element != 'undefined') {
        var $element = $(element);
        $element.attr('value',  $(this).val());
    } 
});



$(document).on('input change', '.tmlinputcheckbox', function(evt) {
    var xpath = $(this).attr('id');
   // var element = xml.evaluate( xpath, xml, null, XPathResult.ANY_UNORDERED_NODE_TYPE  , null ).singleNodeValue;
    var element = $(xml).xpath(xpath);
    if (typeof element != 'undefined') {
        var $element = $(element);
        $element.attr('value',  $(this).prop('checked'));
    } 
});

$(document).on('input change', '.tmlinputselect', function(evt) {
    var  xpath = $(this).attr('id');
    var $input = $(this);
  //  var element = xml.evaluate( xpath, xml, null, XPathResult.ANY_UNORDERED_NODE_TYPE  , null ).singleNodeValue;
    var element = $(xml).xpath(xpath);
   
    
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


window.onpopstate = function(event) {
    if (!event.state) {
        console.log('clear page')
    } else {
        replaceXml(event.state.script, $.parseXML(event.state.xml));
    }
};


$(document).on('click', '.exportcsv', function() {
    var filename = $(this).children('h3').text().trim();
   
    if (typeof filename === 'undefined') {
        filename = 'export.csv';
    } else {
        filename =  "export" + filename + ".csv";
    }

    var blob = getCsvContent($(this));
   
    if (navigator.msSaveBlob) { // IE 10+
        navigator.msSaveBlob(blob, filename)
    } else {
        var link = document.createElement("a");
        if (link.download !== undefined) { 
            //  Browsers that support HTML5 download attribute
            var url = URL.createObjectURL(blob);
            link.setAttribute("href", url);
            link.setAttribute("download", filename);
            link.style = "visibility:hidden";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }  
    }
});



/* Some helpers */
Array.prototype.indexOfPath = function(path) {
    for (var i = 0; i < this.length; i++)
        if (this[i].path === path)
            return i;
    return -1;
}