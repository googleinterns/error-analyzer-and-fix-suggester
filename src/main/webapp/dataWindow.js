/**Copyright 2019 Google LLC++
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

// code for maintaining data window
let data = new Array();

// return page no to be fetched from database

// returning -1 would mean that the page which
//  we need to fetch next is out of bound
getPageToBeFetched = (next) => {
    if (currentPage == 1) {
        return 1;
    } else if(next == true && currentPage + extraPageInFrontAndBack <= lastPage) {
        return currentPage + extraPageInFrontAndBack;
    } else if (next == false && currentPage - extraPageInFrontAndBack > 0) {
        return currentPage - extraPageInFrontAndBack;
    } else {
        return -1;
    }
}

// add fetched page to window we are maintaining
addPageToDataWindow = (fetchedData, page, fileType) => {
    let idx = recordsPerPage * ((page - 1) % dataWindowSize);
    for (let i = 0; i < fetchedData.length; i++) {
        data[idx] = prepareLogDomElement(fetchedData[i], fileType);
        idx++;
    }
}

// prepare DOM element for log/error to be shown on resultPage
prepareLogDomElement = (logError, fileType) => {
    const liElement = document.createElement('li');
    const logLineNoElement = document.createElement('span');
    logLineNoElement.innerText = logError.logLineNumber + "  ";
    const logTextElement = document.createElement('span');
    logTextElement.innerHTML = logError.logText;
    liElement.appendChild(logLineNoElement);
    liElement.appendChild(logTextElement);
    
    // if fileType is error then we need to add 
    // btn to show stackTrace 
    if(fileType == ERRORS) {
        stackTraceButton = getStackTraceButton(logError);
        liElement.appendChild(stackTraceButton);
    }
    return liElement;
}

// return start and end indices for the section of data to be shown 
getPageInterval = (page) => {
    const start = recordsPerPage * ((page - 1) % dataWindowSize);
    let end = start + (recordsPerPage - 1);
    if(page == lastPage) {
        end = start + (noOfRecordsOnLastPage - 1);
    } 
    return {start: start, end: end};
}

