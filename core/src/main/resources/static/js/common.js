function serverPath(path) {
    let target;
    if (path.startsWith("http")) {
        let url = new URL(path);
        target = url.pathname;
    } else {
        target = path;
    }

    return `${window.location.protocol}//${window.location.hostname}:${window.location.port}${target}`;
}

function loadBlock(pageRef, containerId, outerHtml) {
    closeClosableResources(document.body);
    let container = document.getElementById(containerId);
    if (container !== null) {
        fetch(pageRef)
            .then(response => {
                if (response.status === 200) {
                    if (response.url.endsWith(pageRef)) {
                        return response.text();
                    } else {
                        alert('User session was closed. Need to re-login.')
                        window.location = response.url;
                    }
                } else {
                    alert('Something wrong. See the Browser console log -> F12');
                    throw JSON.stringify(response);
                }
            })
            .then(html => {
                setHTML(container, html, outerHtml)
                if (window.pageMenu !== undefined) {
                    window.pageMenu.close();
                }
            });
    }
}

function setHTML(elm, html, outerHtml) {
    if (outerHtml) {
        elm.outerHTML = html;
    } else {
        elm.innerHTML = html;
    }
    Array.from(elm.querySelectorAll("script")).forEach(oldScript => {
        const newScript = document.createElement("script");
        Array.from(oldScript.attributes)
            .forEach(attr => newScript.setAttribute(attr.name, attr.value));
        newScript.appendChild(document.createTextNode(oldScript.innerHTML));
        oldScript.parentNode.replaceChild(newScript, oldScript);
    });
}

function importClass(classNames, callback) {
    const shouldBeLoaded = classNames.length;
    let loadedCounter = 0;
    classNames.forEach(className => {
        try {
            if (typeof eval(className) === 'function') {
                console.log(`${className} already loaded`)
                loadedCounter++;
            }
        } catch (e) {// if class not loaded it produce 'ReferenceError: ClassName is not defined'
            console.log(`Loading class: ${className}`)
            const classScript = document.createElement("script");
            classScript.src = `js/classes/${className}.js`
            classScript.type = "text/javascript";
            classScript.onload = e => {
                loadedCounter++;
            }
            document.head.appendChild(classScript);
        }
    });

    let handler = setInterval(() => {
        if (shouldBeLoaded === loadedCounter) {
            clearInterval(handler)
            callback();
        } else {
            console.log(`Loaded ${loadedCounter} from ${shouldBeLoaded}`);
        }
    }, 500);
}

function handle(error) {
    console.log(error);
}

function appendChildTag(parent, tagName, innerHtml, className) {
    let tag = document.createElement(tagName);
    if (innerHtml !== undefined && innerHtml !== null) {
        tag.innerHTML = innerHtml;
    }
    if (className) {
        tag.className = className;
    }
    if (parent !== undefined && parent !== null) {
        parent.appendChild(tag)
    }
    return tag;
}

function appendSvgIcon(container, className) {
    let svgIcon = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svgIcon.classList.add(className);
    svgIcon.innerHTML = `<use xlink:href="#${className}"/>`;
    container.appendChild(svgIcon);
    return svgIcon;
}

var keys = {37: 1, 38: 1, 39: 1, 40: 1};

function preventDefault(e) {
    e.preventDefault();
}

function preventDefaultForScrollKeys(e) {
    if (keys[e.keyCode]) {
        preventDefault(e);
        return false;
    }
}

// modern Chrome requires { passive: false } when adding event
var supportsPassive = false;
try {
    window.addEventListener("test", null, Object.defineProperty({}, 'passive', {
        get: function () {
            supportsPassive = true;
        }
    }));
} catch (e) {
}

var wheelOpt = supportsPassive ? {passive: false} : false;
var wheelEvent = 'onwheel' in document.createElement('div') ? 'wheel' : 'mousewheel';

// call this to Disable
function disableScroll() {
    window.addEventListener('DOMMouseScroll', preventDefault, false); // older FF
    window.addEventListener(wheelEvent, preventDefault, wheelOpt); // modern desktop
    window.addEventListener('touchmove', preventDefault, wheelOpt); // mobile
    window.addEventListener('keydown', preventDefaultForScrollKeys, false);
}

// call this to Enable
function enableScroll() {
    window.removeEventListener('DOMMouseScroll', preventDefault, false);
    window.removeEventListener(wheelEvent, preventDefault, wheelOpt);
    window.removeEventListener('touchmove', preventDefault, wheelOpt);
    window.removeEventListener('keydown', preventDefaultForScrollKeys, false);
}

function closeClosableResources(node) {
    if (node.children && node.children.length) {
        for (let ch of node.children) {
            closeClosableResources(ch);
        }
    }
    if (node.htmlClose) {
        node.htmlClose();
    }
}

function httpGet(url, successCallback, errorCallback) {
    const getDef = {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        }
    };
    fetch(serverPath(url), getDef).then(res=>_processResponse(res, successCallback, errorCallback));
}

function httpPost(url, body, successCallback, errorCallback) {
    const postDef = {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        },
    };
    if (body) {
        postDef.body = body;
    }
    fetch(serverPath(url), postDef).then(res=>_processResponse(res, successCallback, errorCallback));
}

function httpPut(url, body, successCallback, errorCallback) {
    const putDef = {
        method: 'PUT',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        },
    };
    if (body) {
        putDef.body = body;
    }
    fetch(serverPath(url), putDef).then(res=>_processResponse(res, successCallback, errorCallback));
}

function httpDelete(url, successCallback, errorCallback) {
    const deleteDef = {
        method: 'DELETE',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        }
    };
    fetch(serverPath(url), deleteDef).then(res=>_processResponse(res, successCallback, errorCallback));
}

function _processResponse(response, successCallback, errorCallback) {
    if (response.status === 200) {
        _processSuccessResponse(response, successCallback);
    } else {
        _processFailResponse(response, errorCallback);
    }
}

function _processSuccessResponse(response, callback) {
    let contentType = response.headers.get('Content-Type');
    if (contentType === 'application/json') {
        response.json().then(json => {
            callback(json)
        })
    } else if (contentType === 'application/xml') {
        response.text().then(text => {
            callback(text)
        })
    } else {
        if (callback) {
            callback(response);
        }
    }
}

function defaultErrorCallback(response) {
    console.error(response);
}

function _processFailResponse(response, errorCallback) {
    let contentType = response.headers.get('Content-Type');
    if (contentType === 'application/json') {
        response.json().then(json => {
            if(errorCallback) {
                errorCallback(json);
            } else {
                defaultErrorCallback(json);
            }
        })
    } else if (contentType === 'application/xml') {
        response.text().then(text => {
            if(errorCallback) {
                errorCallback(text);
            } else {
                defaultErrorCallback(text);
            }
        })
    } else {
        if(errorCallback) {
            errorCallback(response);
        } else {
            defaultErrorCallback(response);
        }
    }
}

getCookie = function (name) {
    let match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    if (match) {
        return match[2];
    } else {
        return undefined;
    }
}

function language(){
    let lang = getCookie('Language');
    if(lang) {
        return lang;
    } else {
        return window.navigator.language;
    }
}