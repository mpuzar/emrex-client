var allSelectableNodes, xml;


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
