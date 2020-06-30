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

// add fetched page to window we are maintaining
addToData = (fetchedData, page, fileType) => {
    let idx = recordsPerPage * ((page - 1) % noOfPages);
    for (let i = 0; i < fetchedData.length; i++) {
        data[idx] = prepareLogDomElement(fetchedData[i], fileType);
        idx++;
    }
}

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
    if(fileType == ERRORS) {
        stackTraceButton = stackTraceButton(logError);
        liElement.appendChild(stackTraceButton);
    }
    return liElement;
}

// return start and end indices for the section of data to be shown 
getOffset = (page) => {
    const start = recordsPerPage * ((page - 1) % noOfPages);
    let end = start + (recordsPerPage - 1);
    if(page == lastPage) {
        end = start + (noOfRecordsOnLastPage - 1);
    } 
    return {START: start, END: end};
}

// add logs or errors to result page
display = () => {
    
    const offset = getOffset(currentPage);
    let listing_table;
    if(currentPage % 2 == 0)
        listing_table  = document.getElementById(SLIDE_2);
    else
        listing_table =  document.getElementById(SLIDE_1);
    const page_span = document.getElementById(PAGE);
    listing_table.innerHTML = "";
    // dynamically add element to result page
    for(let i = offset.START ; i <= offset.END && data.length!=0 ; i++ ) {
         listing_table.appendChild(data[i]);
    }
    page_span.innerHTML = currentPage;
    // depending upon the page we are on show or hide navigation buttons
    showAndHideNavigationBtn();
}