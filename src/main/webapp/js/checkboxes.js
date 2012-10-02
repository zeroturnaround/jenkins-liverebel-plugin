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
}

function hideChildGroups(divName){
  var elems = document.getElementById(divName).getElementsByTagName('*');
  var i;
  var searchForClassName = "topLevel";
  for (i in elems) {
    if((" " + elems[i].className + " ").indexOf(" " + searchForClassName + " ") == -1) {
      var childNodes = elems[i].childNodes;
      if (childNodes) {
        for (var j = 0; j < childNodes.length; j++) {
          if (childNodes[j].type == "checkbox") {
            childNodes[j].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="none";
          }
        }
      }
    }
  }
}

function toggleDependentCheckboxes(clickedElement) {
  var elems = document.getElementById(clickedElement.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.id).getElementsByTagName('*');
  toggleUpward(clickedElement.parentNode, elems);
  toggleDownward(clickedElement, elems);
}


function escapeValue(value) {
  if (typeof value === "string") {
    return value.replace(/[^A-Za-z0-9]/g, '_');
  }
  return value;

}
function toggleDownward(clickedElement, elems) {
  var i;

  var searchForClassName = "lr-"+escapeValue(clickedElement.value);
  for (i in elems) {
    if((" " + elems[i].className + " ").indexOf(" " + searchForClassName + " ") > -1) {
      var childNodes = elems[i].childNodes;
      for (var i = 0; i < childNodes.length; i++) {
        if (childNodes[i].type == "checkbox") {
          if (!childNodes[i].checked && hasCheckedParent(childNodes[i].parentNode, elems)) {
            childNodes[i].checked=true;
            childNodes[i].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="inline";
          } else if (childNodes[i].checked) {
            childNodes[i].checked=false ;
            childNodes[i].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="none";
          } else if (!childNodes[i].checked && !hasCheckedParent(childNodes[i].parentNode, elems)) {
            childNodes[i].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="none";
          }
          toggleDownward(childNodes[i], elems);
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
    if((" " + parentDiv.className + " ").indexOf(" lr-" + escapeValue(elems[i].value) + " ") > -1) {
      if (elems[i].type == "checkbox") {
        if (elems[i].checked && !hasCheckedSibling(elems[i], elems)) {
          elems[i].checked=false;
//          elems[i].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="none";
        } else if (!elems[i].checked) {
          elems[i].checked=true;
//          elems[i].parentNode.parentNode.parentNode.parentNode.parentNode.style.display="inline";
        }
        toggleUpward(elems[i].parentNode, elems);
        break;
      }
    }
  }
}

function hasCheckedSibling(element, elems) {
  var i;
  var searchForClassName = "lr-"+escapeValue(element.value);
  for (i in elems) {
    if((" " + elems[i].className + " ").indexOf(" " + searchForClassName + " ") > -1) {
      var childNodes = elems[i].childNodes;
      for (var i = 0; i < childNodes.length; i++) {
        if (childNodes[i].type == "checkbox" && childNodes[i].checked) {
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
    if((" " + parentDiv.className + " ").indexOf(" lr-" +escapeValue(elems[i].value) + " ") > -1) {
      if (elems[i].type == "checkbox" && elems[i].checked) {
        return true;
      }
    }
  }
  return false;
}
