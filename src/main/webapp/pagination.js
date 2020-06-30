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

// the function contains algo for fetching page 
// and updating current page's content
async function changePage(page) {
    // change current page (page user is viewing at present) to the 
    // value provided while calling the function 
    updateCurrentPage(page);
    const logs = document.getElementById(LOGS).getAttribute("aria-selected");
    const searchString = document.getElementById(SEARCH_BAR).value;
    const fileType = logs == "true" ? LOGS : ERRORS ;
    // get the page no to be fetched next 
    const fetchPage = getPageToBeFetched();
    let fileName = document.getElementById(FILE_NAME).value;
    // remove blank spaces from fileName
    fileName =  fileName.trim();

    // if we are on page 1 we need to reset the value of last page 
    // else we already have requested page in window which we can simply
    // display 
    if(page == 1){
        await resetLastPage(fileName, fileType);
    } else {
        display();
    }

    // if fetched page != -1 that means we need to fetch data either to create window(page == 1)
    // or to maintain window(page > 1)
    if(fetchPage != -1) {
        // get file offset for page to be fetched  
        const pageSpecs = getPageStartAndSize(fileType, fetchPage, searchString);
        // fetch data from database
        fetchedData = await callPaginationServlet(fileName, fileType, searchString, pageSpecs);
        // update value of last page
        updateLastPageBasedOnFetchedPageLength(fetchedData.length, fetchPage);
        // if the length of first page is zero that means
        //  there is no file with given name in our database
        if(fetchedData.length == 0 && currentPage == 1) {
            fileNotFound();
            return;
        }
        // add fetched page to window we are maintaining
        addToData(fetchedData, fetchPage, fileType);    
    }

    // if currentPage == 1 then we have just created window 
    // and now we have requested page with us in window and 
    // we can show it to user
    if(currentPage == 1 ) {
        display();
    }
}

// reset value of lastPage when on page 1
async function resetLastPage(fileName, fileType) {
    updateLastPage(Number.MAX_VALUE);
    updateNoOfRecordsOnLastPage(recordsPerPage)

    // if we are dealing with error file we want to 
    // show it in bottom up manner (error ranking)
    // for that reason we can't provide last page a 
    // default value because we will be needing it to 
    // calculate file offsets
    if(fileType == ERRORS){
        // get length of file by making a servlet call
        fileLength = await getCount(fileName, fileType);
        updateLastPage(Math.ceil(fileLength/recordsPerPage));
        if(fileLength % recordsPerPage != 0)
            updateNoOfRecordsOnLastPage(fileLength % recordsPerPage);
    }
}

// calculating starting index and size of block to be fetched from database for given page
getPageStartAndSize = (fileType, page, searchString) => {
    // if we are dealing with log files or searching for some keywords
    // we want to show them in top down manner but if we have a error file
    // we want to show it in bottom up manner
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

// returning -1 would mean that the page which
//  we need to fetch next is out of bound
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
    // if page == 1 we are actually creating window and if the 
    // file length is less than the length of window then the last page 
    // is between 1 and noOfPages(inclusive)
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



