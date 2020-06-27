const noOfPages = 5;
const extraPageInFrontAndBack = Math.floor(noOfPages/2);
let currentPage = 1;
let next = true;
let recordsPerPage = 3;
let fileLength = Number.MAX_VALUE;
let noOfRecordsOnLastPage = recordsPerPage;
let lastPage = Number.MAX_VALUE;
let data = new Array();

// change content of page 
async function changePage(page) {
    currentPage = page;
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
        fetchedData = await callPaginationServlet(fileName, fileType, searchString, pageSpecs);
        updateLastPage(fetchedData.length, fetchedPage);
        if(fetchedData.length == 0 && currentPage == 1) {
            fileNotFound();
            return;
        }
        addToData(fetchedData, fetchedPage, fileType, fileName);    
    }

    if(currentPage == 1 ) {
        display();
    }
}

// reset value of lastPage when on page 1 
async function resetLastPage(fileName, fileType) {
    lastPage = Number.MAX_VALUE;
    noOfRecordsOnLastPage = recordsPerPage;
    if(fileType == ERRORS){
        fileLength = await getCount(fileName, fileType);
        lastPage = Math.ceil(fileLength/recordsPerPage);
        if(fileLength % recordsPerPage != 0)
            noOfRecordsOnLastPage = fileLength % recordsPerPage;
    }
}

// calculating starting index and size of block to be fetched from databasefor given page
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

// add returned records to data
addToData = (fetchedData, page, fileType, fileName) => {
    let idx = recordsPerPage * ((page - 1) % noOfPages);
    for (let i = 0; i < fetchedData.length; i++) {
        data[idx] = prepareLogDomElement(fetchedData[i], fileType, fileName);
        idx++;
    }
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
updateLastPage = (fetchedPageLength, page) => {
    if (page == 1 && fetchedPageLength < recordsPerPage * noOfPages) {
        lastPage =  Math.ceil(fetchedPageLength / recordsPerPage);
        if(fetchedPageLength % recordsPerPage != 0){
             noOfRecordsOnLastPage = fetchedPageLength % recordsPerPage;
        }
        else{
            noOfRecordsOnLastPage = recordsPerPage;
        }
    } else if (page != 1 && fetchedPageLength == 0) {
        lastPage = page - 1;
        noOfRecordsOnLastPage = recordsPerPage;
    } else if (page != 1 && fetchedPageLength < recordsPerPage) {
        lastPage = page;
        noOfRecordsOnLastPage = fetchedPageLength;
    } 
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
    showAndHideBtn();
}

// display next and previous button 
showAndHideBtn = () => {
    const btnPrev = document.getElementById(PREVIOUS_BUTTON);
    const btnNext = document.getElementById(NEXT_BUTTON);
    // hide previous button when on page 1
    if (currentPage == 1) {
        btnPrev.style.visibility = HIDDEN;
    } else {
        btnPrev.style.visibility = VISIBLE;
    }
    // hide next button when 
    if (lastPage == currentPage || data.length == 0) {
        btnNext.style.visibility = HIDDEN;
    } else if (lastPage != currentPage){
        btnNext.style.visibility = VISIBLE;
    }
}

