// display file not found message on UI
fileNotFound = () => {
    listing_table1  = document.getElementById(SLIDE_2);
    listing_table2 =  document.getElementById(SLIDE_1);
    listing_table1.innerHTML = FILE_NOT_FOUND;
    listing_table2.innerHTML = FILE_NOT_FOUND;
    currentPage = 1;
    lastPage = 1;
    showAndHideBtn();
     
}

// prepare DOM element for log/error to be shown on resultPage
prepareLogDomElement = (logError, fileType, fileName) => {
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