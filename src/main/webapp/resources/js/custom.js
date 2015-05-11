var allSelectableNodes, xml;

function displayXml(rebuild, fnr) {
	var j = jQuerySun;
	var resultsDiv = j('#results');
	j(resultsDiv).children().remove();
	
	var internalId = 0;
	allSelectableNodes = [];
	var degree;
	
	var orig = jQuerySun('#elmo').val();
	if (orig == null) return;
	if (rebuild) {
		orig = jQuerySun('[id$="elmo"]').val(); //UTF8.decode(orig)).val()
		/*
		if (orig.match(/^\&lt;xml/)) {
			orig = $('<div/>').html(orig).text();
		}
		*/
		xml = j.parseXML(orig);
		
	}
	
	j(xml).find('report').each(function() {
		var report = this;

		var issuer = getTitle(j(report).find('issuer'));
		j('<header>' + issuer + '</header>').appendTo(resultsDiv);
		
		degree = "";
		var enkeltEmner = [];
		var t = j('<table class="table-light"/>');
		
		j(report).find('learningOpportunitySpecification').each(function() {
			var type = j(this).find('type').first().text().toLowerCase();
			
			var parent = j(this).parent().parent();
			var parentType = (j(parent).prop("tagName").toLowerCase() === "elmo") ? "elmo" : j(parent).find('type').first().text().toLowerCase(); 
			
			if (type === "degree" || (parentType === "degree" && type === "module")) {
				addRow(t, this, type);
			} else if (type === "module") {
				enkeltEmner.push([this, type]);
			}
		});
		
		if (enkeltEmner.length > 0) {
			var tr = j('<tr></tr>');
			j('<th class="check"><div><input type="checkbox" class="degree_' + null + '" onclick="checkDegreeCourses(this, null);"></input></div></th><th colspan="4"><div>Courses without a programme</div></th>').appendTo(tr);
			j(tr).appendTo(t);
			degree = null;
			
			enkeltEmner.forEach(function(spec) {
				addRow(t, spec[0], spec[1]);
			});
		}
		
		j(t).appendTo(resultsDiv);
	});

	
	lastInternalId = internalId;
	jQuerySun('input[type="checkbox"]').prop('checked','true');
	
		
	function addRow(t, spec, type) {
		var tr = j('<tr></tr>');
		internalId++;
		elmoIdTag = jQuerySun(spec).find('identifier[type="elmo"]');
		var elmoId = internalId; 
		if (elmoIdTag.length == 1) {
			elmoId = jQuerySun(elmoIdTag).text();
		} else {
			j('<identifier type="elmo">' + internalId + '</identifier>').appendTo(spec);
			elmoId = internalId; 
		}
		allSelectableNodes[elmoId] = spec;

		switch (type) {
			case "degree":
				degree = j(spec).children('qualification').first().children('identifier[type="fs"]').text();
				degreeQ = "'" + degree + "'";
				j('<th class="check"><div><input type="checkbox" class="degree_'+degree+'" name="id['+elmoId+']" onclick="checkDegreeCourses(this, ' + degreeQ + ');"></input></div></th>').appendTo(tr);
				var descr = getTitle(spec);
				
				j('<th colspan="4"><div>'+ descr +'</div></th>').appendTo(tr);
				break;
			case "module":
				var kode = j(spec).children('identifier[type="fs"]').text();
				degreeQ = "'" + degree + "'";

				j('<td class="check"><input type="checkbox" class="emne_'+degree+'" name="id['+elmoId+']" onclick="checkDegreeForOneCourse(this, ' + degreeQ + ')"></input></td>').appendTo(tr);
				var descr = getTitle(spec);
				
				var creds = j(spec).children('credit').children('value').text();
				var result = j(spec).children('specifies').find('result').html();
				
				j('<td class="emnekode">'+ kode +'</td><td class="descr">'+ descr + '</td><td class="creds">'+ creds +'</td><td class="result">'+ result +'</td>').appendTo(tr); 
				break;
		}
		j(tr).appendTo(t); 
	}
	
	function getTitle(parent) {
		var res = "";
		j(parent).children("title").each(function() {
			var lang = j(this).attr('xml:lang');
			var text = j(this).text();
			if (text !== "" && (res === "" || lang === "en")) {
				res = text;
			}
		});
		return res;
	}
}

function checkDegreeCourses(check, degree) {
	jQuerySun('#results').find('input[type="checkbox"].emne_' + degree).each(function() {this.checked=check.checked});
}

function checkDegreeForOneCourse(check, degree) {
	if (check.checked) {
		jQuerySun('#results').find('input[type="checkbox"].degree_' + degree).each(function() {this.checked=true});
	}
}

function enableDisableFetch() {
	var fetch = jQuerySun('[id$=":fetch"]');
	var disabled = jQuerySun('select[id$=":inst"]').val() === "" ? true : false;
	if (disabled)
		jQuerySun(fetch).attr("disabled", disabled);
	else
		jQuerySun(fetch).removeAttr("disabled");
}


function updateXml() {
	for (var id in allSelectableNodes) {
		if (allSelectableNodes.hasOwnProperty(id)) {
			var checked = jQuerySun('input[name="id['+id+']"]').prop("checked");
			if (!checked) {
				var item = allSelectableNodes[id];
				var isHasPart = jQuerySun(item).closest('hasPart');
				if (isHasPart.length > 0) 
					jQuerySun(isHasPart).remove();
				else
					jQuerySun(item).remove();
			}
		}
	}

	var ret = (new XMLSerializer()).serializeToString(xml);
	//ret = UTF8.decode(ret);
	console.log("new length " + ret.length)
	
	jQuerySun('#elmo').val(ret);
}


function hideExportForm() {
	jQuerySun('#exportForm').hide();
}

function showExportForm() {
	jQuerySun('#exportForm').show();
}


function showInfoPanel(title, text, showClose, showSpinner) {

	var panelTitle = jQuerySun('[id="infoPanel:popup"] header');  // jQuery('span[id="endreEmneForm:infoPanelTekst"]');
    var panelTekst = jQuerySun('[id="infoPanel:infoPanel_text"]');  // jQuery('span[id="endreEmneForm:infoPanelTekst"]');
    panelTitle.html(title);
    panelTekst.html(text);

    var panelClose = jQuerySun('[id="infoPanel:popup"] .ui-dialog-titlebar-close');
    if (showClose == 'true') {
        panelClose.show();
    } else {
        panelClose.hide();
    }

    var panelSpinner = jQuerySun('#infoPanel_spinner');
    if (showSpinner == 'true') {
        panelSpinner.show();
    } else {
        panelSpinner.hide();
    }

	window['infoPanel'].show();
    
}


function hideInfoPanel() {
	window['infoPanel'].hide();
}


//+ Jonas Raoni Soares Silva
//@ http://jsfromhell.com/geral/utf-8 [v1.0]

UTF8 = {
	encode: function(s){
		for(var c, i = -1, l = (s = s.split("")).length, o = String.fromCharCode; ++i < l;
			s[i] = (c = s[i].charCodeAt(0)) >= 127 ? o(0xc0 | (c >>> 6)) + o(0x80 | (c & 0x3f)) : s[i]
		);
		return s.join("");
	},
	decode: function(s){
		for(var a, b, i = -1, l = (s = s.split("")).length, o = String.fromCharCode, c = "charCodeAt"; ++i < l;
			((a = s[i][c](0)) & 0x80) &&
			(s[i] = (a & 0xfc) == 0xc0 && ((b = s[i + 1][c](0)) & 0xc0) == 0x80 ?
			o(((a & 0x03) << 6) + (b & 0x3f)) : o(128), s[++i] = "")
		);
		return s.join("");
	}
};



