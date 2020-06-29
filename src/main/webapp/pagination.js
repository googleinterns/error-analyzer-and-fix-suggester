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

let fileLength = Number.MAX_VALUE;

// change content of page 
async function changePage(page) {
    updateCurrentPage(page);
    const logs = document.getElementById(LOGS).getAttribute("aria-selected");
    const searchString = document.getElementById(SEARCH_BAR).value;
    const fileType = logs == "true" ? LOGS : ERRORS ;
    const fetchedPage = getPageToBeFetched();
    let fileName = document.getElementById(FILE_NAME).value;
    fileName =  fileName.trim();

    if(page == 1){
        await resetLastPage(fileName, fileType);
    } else {
        display();
    }

    if(fetchedPage != -1) {
        const pageSpecs = getPageStartAndSize(fileType, fetchedPage, searchString);
        console.log(pageSpecs.START +" "+ pageSpecs.SIZE);
        fetchedData = await callPaginationServlet(fileName, fileType, searchString, pageSpecs);
        console.log(fetchedData);
        updateLastPageBasedOnFetchedPageLength(fetchedData.length, fetchedPage);
        if(fetchedData.length == 0 && currentPage == 1) {
            fileNotFound();
            return;
        }
        addToData(fetchedData, fetchedPage, fileType);    
    }

    if(currentPage == 1 ) {
        display();
    }
}

// reset value of lastPage when on page 1 
async function resetLastPage(fileName, fileType) {
    updateLastPage(Number.MAX_VALUE);
    updateNoOfRecordsOnLastPage(recordsPerPage)
    if(fileType == ERRORS){
        fileLength = await getCount(fileName, fileType);
        updateLastPage(Math.ceil(fileLength/recordsPerPage));
        if(fileLength % recordsPerPage != 0)
            updateNoOfRecordsOnLastPage(fileLength % recordsPerPage);
    }
}

// calculating starting index and size of block to be fetched from database for given page
getPageStartAndSize = (fileType, page, searchString) => {
    if(fileType == LOGS || searchString != "") {
        return getPageStartAndSizeForLogFile(page);
    } else {
        return getPageStartAndSizeForErrorFile( page);
    }
}

//  calculating starting index and size of block to be fetched for log file 
getPageStartAndSizeForLogFile = (page) => {
    const start = ((page - 1) * recordsPerPage);
    let size = recordsPerPage;
    if( page == 1) {
        size = (noOfPages * recordsPerPage);
    } 
    return {START: start, SIZE: size};
}

// calculating starting index and size of block to be fetched for file storing errors
getPageStartAndSizeForErrorFile = (page) => {
    let start = ((lastPage - page )*recordsPerPage);
    if(fileLength % recordsPerPage != 0) {
        start = ((lastPage - page - 1)*recordsPerPage) + noOfRecordsOnLastPage;
    }
    let size = recordsPerPage;
    if(page == 1) {
        start = Math.max(0, fileLength-(noOfPages * recordsPerPage));
        size = Math.min(fileLength, (noOfPages * recordsPerPage));
    } 
    return {START: start, SIZE: size};
}

// return page no to be fetched from database
getPageToBeFetched = () => {
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

// calculate last page and no of records on last page
updateLastPageBasedOnFetchedPageLength = (fetchedPageLength, page) => {
    if (page == 1 && fetchedPageLength < recordsPerPage * noOfPages) {
        updateLastPage( Math.ceil(fetchedPageLength / recordsPerPage) );
        if(fetchedPageLength % recordsPerPage != 0){
            updateNoOfRecordsOnLastPage(fetchedPageLength % recordsPerPage);
        }
        else{
            updateNoOfRecordsOnLastPage(recordsPerPage);
        }
    } else if (page != 1 && fetchedPageLength == 0) {
        updateLastPage(page - 1);
        updateNoOfRecordsOnLastPage(recordsPerPage);
    } else if (page != 1 && fetchedPageLength < recordsPerPage) {
        updateLastPage(page);
        updateNoOfRecordsOnLastPage(fetchedPageLength);
    } 
}



