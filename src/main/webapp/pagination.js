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

    // reset value of lastPage when on page 1 
    if(page == 1){
        lastPage = Number.MAX_VALUE;
        noOfRecordsOnLastPage = recordsPerPage;
        if(fileType == ERRORS){
            fileLength = await getCount(fileName, fileType);
            lastPage = Math.ceil(fileLength/recordsPerPage);
            if(fileLength % recordsPerPage != 0)
                noOfRecordsOnLastPage = fileLength % recordsPerPage;
        }
    }
    if(currentPage != 1 ) {
        display();
    }
    if(fetchedPage != -1) {
        const params = new URLSearchParams();
        const pageSpecs = getPageStartAndSize(fileType, fetchedPage, searchString);
        console.log(pageSpecs.start+" "+pageSpecs.size);
        params.append('start', pageSpecs.start);
        params.append('size',  pageSpecs.size);
        params.append('searchString', searchString);
        params.append('fileType', fileType);
        params.append('fileName', fileName);
        const response = await fetch('/pagination', {
            method: 'POST',
            body: params
        });
        const fetchedData = await response.json();
        console.log(fetchedData+" "+lastPage);
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
    return {"start": start, "size": size};
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
    return {"start": start, "size": size};
}

// return no of documents in a index
async function getCount(fileName, fileType) {
    const params = new URLSearchParams();
    params.append('fileName', fileName);
    params.append('fileType', fileType);
    const response = await fetch('/getCount', {
        method: 'POST',
        body: params
    });
    const count = await response.json();
    return count;
}

// display file not found message on UI
fileNotFound = () => {
    listing_table1  = document.getElementById("slide2");
    listing_table2 =  document.getElementById("slide1");
    listing_table1.innerHTML = "File Not Found";
    listing_table2.innerHTML = "File Not Found";
    currentPage = 1;
    lastPage = 1;
    showAndHideBtn();
     
}

// add returned records to data
addToData = (fetchedData, page) => {
    let idx = recordsPerPage * ((page - 1) % noOfPages);
    for (let i = 0; i < fetchedData.length; i++) {
        data[idx] = fetchedData[i];
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
    return {"start": start, "end": end};
}

// change no of records on a page 
changeNoOfRecordsOnPage = () => {
    const records = document.getElementById("records");
    recordsPerPage = records.value;
    currentPage=1;
    changePage(1);
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
        listing_table  = document.getElementById("slide2");
    else
        listing_table =  document.getElementById("slide1");
    const page_span = document.getElementById("page");
    listing_table.innerHTML = "";
    // dynamically add element to result page
    for(let i = offset.start ; i <= offset.end && data.length!=0 ; i++ ) {
        listing_table.innerHTML += data[i] + "<br>";
    }
    page_span.innerHTML = currentPage;
    showAndHideBtn();
}

// display next and previous button 
showAndHideBtn = () => {
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
search = () => {
    const searchString = document.getElementById("searchBar").value;
    if (searchString == "") {
        return;
    }
    changePage(1);
}