/**Copyright 2019 Google LLC
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

// prepare DOM element for log/error to be shown on resultPage
prepareLogDomElement = (logError, fileType) => {
    const liElement = document.createElement('li');
    const logLineNo = document.createElement('span');
    logLineNo.innerText = logError.logLineNumber + "  ";
    const logText = document.createElement('span');
    logText.innerHTML = logError.logText;
    liElement.appendChild(logLineNo);
    liElement.appendChild(logText);
    
    // if fileType is error then we need to add 
    // btn to show stackTrace 
    if(fileType == ERRORS){
      addStackTraceBtn(liElement, logError);
    }
    return liElement;
}

// add button for stack trace
addStackTraceBtn = (liElement, logError) => {
    const stackTraceButton = document.createElement('button');
    stackTraceButton.innerText="Stack Trace";
    stackTraceButton.className = "stackTraceButton";
    stackTraceButton.addEventListener('click', () => {
        showStackTrace(logError);
    });
    liElement.appendChild(stackTraceButton);
}

// call stackTrace servlet and fetch stackTrace corresponding to error
async function showStackTrace(logError) {
    let fileName = document.getElementById(FILE_NAME).value;
    fileName =  fileName.trim();
    let stackTraceContainer = document.getElementById(STACK_TRACE_CONTAINER);
    stackTraceContainer.className = SHOW;
    let crossBtn = document.getElementById(CROSS);
    crossBtn.className = SHOW;
    stackTraceContainer.innerHTML = "";
    stackTraceContainer.innerHTML += logError.logText;
    const stackTrace = await callStackTraceServlet(logError.logLineNumber, fileName);
    addStackTracesDynamicallyToFrontEnd(stackTrace);
}

// add fetched stackTrace dynamically to frontEnd
addStackTracesDynamicallyToFrontEnd = (stackTrace) => {
    let stackTraceContainer = document.getElementById(STACK_TRACE_CONTAINER);
    for(let i=0; i<stackTrace.length; i++) {
        let stackTraceElement = document.createElement('li');
        stackTraceElement.innerHTML = stackTrace[i];
        stackTraceContainer.appendChild(stackTraceElement);
    }
}