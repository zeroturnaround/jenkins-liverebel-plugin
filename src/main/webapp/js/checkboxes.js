String.prototype.trim = function() {
  return this.replace(/^\s+|\s+$/g,"");
}

function addOnloadHandler(newFunction) {
  var oldevent = window.onload;
  if (typeof oldevent == "function") {
    window.onload = function() {
      if (oldevent) {
        oldevent();
      }
      newFunction();
    };
  }
  else {
    window.onload = newFunction;
  }
}

addOnloadHandler(checkAndAutoHide);

function checkAndAutoHide() {
  var deployOrUpdate = document.getElementsByClassName("lr-deployOrUpdate-servers");
  if (deployOrUpdate) {
    for (var i = 0; i < deployOrUpdate.length; i++) {
      var servers = deployOrUpdate[i].childNodes[0].childNodes;
      if (servers.length - 2 > 12) {
        hideChildGroups(deployOrUpdate[i].id);
      }
    }
  }

  var undeploy = document.getElementsByClassName("lr-undeploy-servers");
  if (undeploy) {
    for (var i = 0; i < undeploy.length; i++) {
      servers = undeploy[i].childNodes[0].childNodes;
      if (servers.length - 2 > 12) {
        hideChildGroups(undeploy[i].id);
      }
    }
  }

  if (deployOrUpdate){
    initVhosts(deployOrUpdate);
    initSchema(deployOrUpdate);
  }

  if (undeploy){
    initSchema(undeploy);
  }

}

/***
 * by default hides child groups for root groups which have no selected children
 * @param divId - id of currently processed step server selector
 */
function hideChildGroups(divId){
  var deployDiv = $$('#' + divId);
  deployDiv = deployDiv[0].down(0);
  var checkDivs = [];
  if (deployDiv)
    checkDivs = deployDiv.childElements();
  if (checkDivs.length == 0)
    return;

  var topLevels = [];
  for (var i = 0; i < checkDivs.length; i++) {
    if (checkDivs[i].down('div.topLevel')) {
      topLevels.push(checkDivs[i]);
    }
  }

  for (var i = 0; i < topLevels.length; i++) {
    var el = topLevels[i].next('div[name=servers]', 0);
    var canHide = true;
    var subDivs = [];
    while (el && !el.down('div.topLevel')) {
      if (el.down('input:checked["type=checkbox"]')) {
        canHide = false;
        break;
      }
      subDivs.push(el);
      el = el.next('div[name=servers]', 0);
    }
    
    if (canHide) {
      for (var j = 0; j < subDivs.length; j++) {
        subDivs[j].down('table', 0).setStyle({'display': 'none'});
      }
    }
  }

}

function toggleDependentCheckboxes(clickedElement) {
  Element.extend(clickedElement);
  var stepId = getStepId(clickedElement.up(9));
  var traverseOffset = 0;
  if (stepId == 'undefined') {
    traverseOffset = 4;
    stepId = getStepId(clickedElement.up(9 + traverseOffset));
  }
  var vhostContainer = Element.getElementsBySelector(clickedElement.up(10 + traverseOffset), 'div.lr-deployOrUpdate-vhost-list.' + stepId + '-vhost-list');
  if (vhostContainer.length == 1)
    processVirtualHosts(clickedElement, vhostContainer[0]);

  var elems = document.getElementById(clickedElement.up('div.lr-server-selection').id).getElementsByTagName('*');
  toggleUpward(clickedElement.parentNode, elems);
  toggleDownward(clickedElement, elems, vhostContainer[0]);
}


function escapeValue(value) {
  if (typeof value === "string") {
    return value.replace(/[^A-Za-z0-9]/g, '_');
  }
  return value;

}
function toggleDownward(clickedElement, elems, vhostContainer) {
  var i;

  var searchForClassName = "lr-" + escapeValue(clickedElement.value);
  for (j in elems) {
    if ((" " + elems[j].className + " ").indexOf(" " + searchForClassName + " ") > -1) {
      var childNodes = elems[j].childNodes;
      for (var i = 0; i < childNodes.length; i++) {
        if (childNodes[i].type == "checkbox") {
          if (!childNodes[i].checked && hasCheckedParent(childNodes[i].parentNode, elems)) {
            setChecked(childNodes[i], true, vhostContainer);
            childNodes[i].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="inline";
          } else if (childNodes[i].checked) {
            setChecked(childNodes[i], false, vhostContainer);
            childNodes[i].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="none";
          } else if (!childNodes[i].checked && !hasCheckedParent(childNodes[i].parentNode, elems)) {
            childNodes[i].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="none";
          }
          toggleDownward(childNodes[i], elems, vhostContainer);
          break;
        }
      }
    }
  }
}

function toggleUpward(parentDiv, elems) {
  var i;
  if (parentDiv.className == " ")
    return;
  for (i in elems) {
    if ((" " + parentDiv.className + " ").indexOf(" lr-" + escapeValue(elems[i].value) + " ") > -1) {
      if (elems[i].type == "checkbox") {
        if (elems[i].checked && !hasCheckedSibling(elems[i], elems)) {
          setChecked(elems[i], false);
        } else if (!elems[i].checked) {
          setChecked(elems[i], true);
        }
        toggleUpward(elems[i].parentNode, elems);
        break;
      }
    }
  }
}

function hasCheckedSibling(element, elems) {
  var i;
  var searchForClassName = "lr-" + escapeValue(element.value);
  for (i in elems) {
    if ((" " + elems[i].className + " ").indexOf(" " + searchForClassName + " ") > -1) {
      var childNodes = elems[i].childNodes;
      for (var j = 0; j < childNodes.length; j++) {
        if (childNodes[j].type == "checkbox" && childNodes[j].checked) {
          return true;
        }
      }
    }
  }
  return false;
}

function hasCheckedParent(parentDiv, elems) {
  var i;
  for (i in elems) {
    if ((" " + parentDiv.className + " ").indexOf(" lr-" +escapeValue(elems[i].value) + " ") > -1) {
      if (elems[i].type == "checkbox" && elems[i].checked) {
        return true;
      }
    }
  }
  return false;
}

function setChecked(element, isChecked, vhostContainer){
  element.checked = isChecked;
  // console.log(element.value + ' set to ' +isChecked);
  processVirtualHosts(element, vhostContainer);
}

// *************   virtual host related functions ************** //
/**
 * check if selected element has virtual hosts, add them to virtul host list
 */
function processVirtualHosts(element, vhostContainer){
  if (!hasVirtualHosts(element))
    return;
  var vhostList = getVhostList(element);
  if (vhostContainer)
    fillVhostBlock(vhostList, vhostContainer);
}

/**
 * check if element (server/group checkbox) has virtual host list
 */
function hasVirtualHosts(element){
  Element.extend(element);
  var vhosts = Element.getElementsBySelector(element.up(3), 'input.lr-vhost-item');
  return vhosts.length != 0;
}

/**
 * gets a list of virtual hosts for all currently selected servers
 */
function getVhostList(element){
  var vhostList = [];
  var elems = document.getElementById(element.up('div.lr-server-selection').id).getElementsByTagName('input');
  for ( var i = 0; i < elems.length; i++)
    if (elems[i].type == "checkbox" && elems[i].checked){
      Element.extend(elems[i]);
      var vhosts = Element.getElementsBySelector(elems[i].up(3), 'input.lr-vhost-item');
      if (vhosts.length ==  0)
        continue;
      else
        for (var j = 0; j < vhosts.length; j++)
          if (!containsVirtualHost(vhostList, vhosts[j].readAttribute('value')))
            vhostList.push({'name' : vhosts[j].readAttribute('value'), 'checked' : false});
    }
  return vhostList;
}

/**
 * simple check if virtual host already added to the list (result is distinct list)
 */
function containsVirtualHost(vhosts, vhost){
  for (var i = 0; i < vhosts.length; i++)
    if (vhosts[i].name == vhost)
      return true;
  return false;
}

/**
 * fill container with virtual host radios, display an option to select virtual host
 */
function fillVhostBlock(vhostList, vhostContainer){
  clearChildren(vhostContainer);
  for (var i = 0; i < vhostList.length; i++){
    addVhostRadio(vhostContainer, vhostList[i].name, vhostList[i].checked);
  }

  var vhostOptionalSwitch = vhostContainer.up(2).down('input[name=' + getStepId(vhostContainer.up(1)) +'.virtualHostForm]');
  var vhostOptionalBlock = vhostOptionalSwitch.up(1);
  if (!vhostOptionalBlock)
    return;

  if (vhostList.length > 0) {
    vhostOptionalBlock.show();
  }
  else {
    if (vhostOptionalSwitch.checked)
      vhostOptionalSwitch.click();
    vhostOptionalBlock.hide();
  }
}

/**
 * remove radio buttons and labels, event-handler-safe
 */
function clearChildren(element){
  element.childElements().each(
     function(child) {
        Event.stopObserving(child);
        child.remove();
     }
  );
}

/**
 * create new radio button and label for virtual host
 */
function addVhostRadio(container, value, checked){
  var newVhost = document.createElement('input');
  newVhost.setAttribute('type','radio');
  newVhost.setAttribute('value', value);
  if (checked)
    newVhost.setAttribute('checked', 'checked');
  var stepId = getStepId(container.parentNode.parentNode);
  newVhost.setAttribute('name', stepId +'.virtualHost');
  newVhost.setAttribute('id','virtualHost-' + escapeValue(value) + '-' + stepId);
  container.appendChild(newVhost);

  var newVhostLabel = document.createElement('label');
  newVhostLabel.setAttribute('class', 'attach-previous');
  newVhostLabel.appendChild(document.createTextNode(value));
  container.appendChild(newVhostLabel);
  Element.extend(newVhostLabel);
  newVhostLabel.observe('click', function(event){
    Element.extend(this);
    this.previous('input').checked = true;
  });
  var lineBreak = document.createElement('br');
  container.appendChild(lineBreak);
}

/**
 * fill virtual host selection for each step container
 */
function initVhosts(serversContainer){
  for (var j = 0; j < serversContainer.length; j++) {
    var vhostContainer = Element.getElementsBySelector(serversContainer[j].parentNode.parentNode.parentNode, 'div.lr-deployOrUpdate-vhost-list');
    if (!vhostContainer || vhostContainer.length != 1)
      continue;
    vhostContainer = vhostContainer.first();

    var servers = serversContainer[j].childNodes[0].childNodes;
    for (var i = 0; i < servers.length; i++){
      Element.extend(servers[i]);
      var checkbox = Element.getElementsBySelector(servers[i], 'input[type=checkbox]');
      if (checkbox.length==0)
        continue;
      checkbox = checkbox[0];
      processVirtualHosts(checkbox, vhostContainer);
    }
    var selectedVhost = Element.getElementsBySelector(serversContainer[j].parentNode.parentNode.parentNode, 'input.lr-selected-vhost');
    if (selectedVhost.length != 1)
      continue;
    selectedVhost = selectedVhost[0];
    if (selectedVhost.value)
      $$('#virtualHost-' + escapeValue(selectedVhost.value) + '-' + getStepId(selectedVhost.parentNode.parentNode.parentNode))[0].checked = true;
  }
}

// *************   schema and target proxy related functions ************** //
/**
 * when schema selected, fill migration server value, call selector switch
 */
function setTargetProxyFromSchema(element, proxyId){
  var proxyField = Element.getElementsBySelector(element.up(6), 
    'input.lr-selected-target-proxy.' + getStepId(element.up(5)) + '-selected-target-proxy')[0];
  if (proxyField)
    proxyField.value = proxyId;
  showTargetProxySelector(element);
}

/**
 * when proxy chosen from selector, fill migration server value
 */
function setTargetProxyFromProxySelector(element){
  var proxyField = Element.getElementsBySelector(element.up(8), 
    'input.lr-selected-target-proxy.' + getStepId(element.up(7)) + '-selected-target-proxy')[0];
  if (proxyField)
    proxyField.value = element.value;
}

/**
 * show proxy selector, if exists behind schema switch
 * also select proxy by value if provided
 */
function showTargetProxySelector(element, itemValue){
  Element.getElementsBySelector(element.up(), 'div.lr-proxy-selection').invoke('hide');
  var selectorDiv = Element.getElementsBySelector(element.up(), 'div.lr-proxy-selection-' + element.value)[0];
  if (selectorDiv) {
    selectorDiv.show();
    var selector = Element.getElementsBySelector(selectorDiv, 'select')[0];
    if (!selector)
      return;
    if (itemValue){
      for (var i = 0; i < selector.options.length; i++)
        if (selector.options[i].value == itemValue){
          selector.selectedIndex = i;
          setTargetProxyFromProxySelector(selector.options[i]);
          break;
        }
    }
    else
      selector.selectedIndex = 0;
  }
}

/**
 * select schema and proxy based on stored values
 */
function initSchema(serverContainer){
  for (var i = 0; i< serverContainer.length; i++) {
    var stepId = getStepId(serverContainer[i].parentNode.parentNode);
    var selectedSchema = Element.getElementsBySelector(serverContainer[i].parentNode.parentNode.parentNode,
      'input.lr-selected-schema.' + stepId + '-selected-schema').first();
    if (!selectedSchema || selectedSchema.value.length == 0)
      continue;

    var schemaSwitch = Element.getElementsBySelector(serverContainer[i].parentNode.parentNode.parentNode,
      'input[value=' + selectedSchema.value + '].dbschema-item.' + stepId + '-dbschema-item').first();
    if (!schemaSwitch)
      continue;
    schemaSwitch.checked = true;
    schemaSwitch.click();

    var targetProxyCurrentValue = Element.getElementsBySelector(serverContainer[i].parentNode.parentNode.parentNode,
      'input.lr-selected-target-proxy-value.' + stepId + '-selected-target-proxy-value').first();
    if (!targetProxyCurrentValue)
      continue;

    showTargetProxySelector(schemaSwitch, targetProxyCurrentValue.value);

  }
}

/**
 * finds a unique prefix for current build step control to use unique names
 */
function getStepId(element){
  Element.extend(element);
  var stepId = element.next('tr.dropdownList-end');
  if (stepId)
    stepId = stepId.previous('tr').down('input.lr-step-prefix');
  else
    return 'undefined';
  if (stepId){
    return stepId.value;
  }
}
