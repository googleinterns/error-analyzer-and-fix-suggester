const noOfPages = 5;
const extraPageInFrontAndBack = Math.floor(noOfPages/2);
const LOGS = "logs" ;
const ERRORS = "errors";
let currentPage = 1;
let next = true;
let recordsPerPage = 3;
let fileLength = Number.MAX_VALUE;
let noOfRecordsOnLastPage = recordsPerPage;
let lastPage = Number.MAX_VALUE;
let data = new Array();

// decrement by 1 on pressing previous button
prevPage = () => {
    currentPage--;
    next = false;
    changePage(currentPage);
}

// increment by 1 on pressing next button
nextPage = () => {
    currentPage++;
    next = true;
    changePage(currentPage);
}

// change content of page 
async function changePage(page) {
    currentPage = page;
    const logs = document.getElementById("logs").getAttribute("aria-selected");
    const searchString = document.getElementById("searchBar").value;
    const fileType = logs == "true" ? LOGS : ERRORS ;
    const fetchedPage = getPageToBeFetched();
    let fileName = document.getElementById("fileName").value;

    if(page == 1 && fileType == ERRORS){
        fileLength = await getCount(fileName, fileType);
    } 
    if(currentPage != 1 ) {
        display();
    }
    if(fetchedPage != -1) {
        const params = new URLSearchParams();
        const startAndSize = getPageStartAndSize(fileType, fetchedPage);
        params.append('start', startAndSize[0]);
        params.append('size',  startAndSize[1]);
        params.append('searchString', searchString);
        params.append('fileType', fileType);
        params.append('fileName', fileName);
        const response = await fetch('/pagination', {
            method: 'POST',
            body: params
        });
        const fetchedData = await response.json();
        updateLastPage(fetchedData.length, fetchedPage);
        if(fetchedData.length == 0 && currentPage == 1) {
            fileNotFound();
            return;
        }
        addToData(fetchedData, fetchedPage);    
    }
    if(currentPage == 1 ) {
        display();
    }
}
// calculating starting index and size of block to be fetched from databasefor given page
function getPageStartAndSize(fileType, page) {
    if(fileType == LOGS) {
        return getPageStartAndSizeForLogFile(page);
    } else {
        return getPageStartAndSizeForErrorFile( page);
    }
}

//  calculating starting index and size of block to be fetched for log file 
function getPageStartAndSizeForLogFile(page) {
    let startAndSize = new Array();
    startAndSize[0] = ((page - 1) * recordsPerPage);
    startAndSize[1] = recordsPerPage;
    if( page == 1) {
        startAndSize[1] = (noOfPages * recordsPerPage);
    } 
    return startAndSize;
}

// calculating starting index and size of block to be fetched for file storing errors
function getPageStartAndSizeForErrorFile(page) {
    let startAndSize = new Array();
    startAndSize[0] = ((lastPage - page )*recordsPerPage);
    if(fileLength % recordsPerPage != 0) {
        startAndSize[0] = ((lastPage - page - 1)*recordsPerPage) + noOfRecordsOnLastPage;
    }
    startAndSize[1] = recordsPerPage;
    if(page == 1) {
        startAndSize[0] = Math.max(0, fileLength-(noOfPages * recordsPerPage));
        startAndSize[1] = Math.min(fileLength, (noOfPages * recordsPerPage));
    } 
    return startAndSize;
}

// return no of documents in a index
async function getCount(fileName, fileType) {
    const params = new URLSearchParams();
    params.append('index', fileName);
    params.append('fileType', fileType);
    const response = await fetch('/getCount', {
        method: 'POST',
        body: params
    });
    const count = await response.json();
    return count;
}

// display file not found message on UI
function fileNotFound() {
    listing_table1  = document.getElementById("slide2");
    listing_table2 =  document.getElementById("slide1");
    listing_table1.innerHTML = "File Not Found";
    listing_table2.innerHTML = "File Not Found";
    currentPage = 1;
    lastPage = 1;
    showAndHideBtn();
     
}

// add returned records to data
function addToData(fetchedData, page) {
    let idx = recordsPerPage * ((page - 1) % noOfPages);
    for (let i = 0; i < fetchedData.length; i++) {
        data[idx] = fetchedData[i];
        idx++;
    }
}

// return start and end indices for the section of data to be shown 
function getOffset(page) {
    let offset = new Array();
    const start = recordsPerPage * ((page - 1) % noOfPages);
    offset[0] = start;
    if(page == lastPage) {
        offset[1] = start + (noOfRecordsOnLastPage - 1);
    } else {
        offset[1] = start + (recordsPerPage - 1);
    }
    return offset;
}

// change no of records on a page 
function changeNoOfRecordsOnPage() {
    const records = document.getElementById("records");
    recordsPerPage = records.value;
    currentPage=1;
    changePage(1);
}

// return page no to be fetched from database
function getPageToBeFetched() {
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
function updateLastPage(fetchedPageLength, page) {
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
    } else {
        lastPage = Number.MAX_VALUE;
        noOfRecordsOnLastPage = recordsPerPage;
    }
}

// add logs or errors to result page
function display() {
    
    const offset = getOffset(currentPage);
    let listing_table;
    if(currentPage % 2 == 0)
        listing_table  = document.getElementById("slide2");
    else
        listing_table =  document.getElementById("slide1");
    const page_span = document.getElementById("page");
    listing_table.innerHTML = "";
    // dynamically add element to result page
    for(let i = offset[0] ; i <= offset[1] && data.length!=0 ; i++ ) {
        listing_table.innerHTML += data[i] + "<br>";
    }
    page_span.innerHTML = currentPage;
    showAndHideBtn();
}

// display next and previous button 
function showAndHideBtn() {
    const btnPrev = document.getElementById("btnPrev");
    const btnNext = document.getElementById("btnNext");
    // hide previous button when on page 1
    if (currentPage == 1) {
        btnPrev.style.visibility = "hidden";
    } else {
        btnPrev.style.visibility = "visible";
    }
    // hide next button when 
    if (lastPage == currentPage || data.length == 0) {
        btnNext.style.visibility = "hidden";
    } else if (lastPage != currentPage){
        btnNext.style.visibility = "visible";
    }
}

// search dataBase for the requested string
function search() {
    const searchString = document.getElementById("searchBar").value;
    if (searchString == "") {
        return;
    }
    changePage(1);
}