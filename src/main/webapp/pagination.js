let currentPage = 1;
let next = true;
let recordsPerPage = 3;
const noOfPages = 5;
const extraPageInFrontAndBack = Math.floor(noOfPages/2);
let noOfRecordsOnLastPage = recordsPerPage;
let lastPage = Number.MAX_VALUE;
let data = new Array();
const fileName = document.getElementById("fileName");

// decrement by 1 on pressing previous button
function prevPage() {
    currentPage--;
    next = false;
    changePage(currentPage);
}

// increment by 1 on pressing next button
function nextPage() {
    currentPage++;
    next = true;
    changePage(currentPage);
}

// change content of page 
async function changePage(page) {
    const logs = document.getElementById("logs");
    const fileType = logs.checked ? "logs" : "errors";
    if (page < 1 || page == undefined) {
        page = 1;
        currentPage = 1;
    }
    const fetchedPage = getPageToBeFetched(); 
    if(currentPage != 1 ) {
        display();
    }
    if(fetchedPage != -1) {
        const params = new URLSearchParams();
        params.append('requestedPage', fetchedPage);
        params.append('fileType', fileType);
        params.append('fileName', fileName.value);
        params.append('recordsPerPage', recordsPerPage);
        // ask for data to display from java servlet 
        const response = await fetch('/pagination', {
            method: 'POST',
            body: params
        });
        const fetchedData = await response.json();
        updateLastPage(fetchedData.length, fetchedPage);
        addToData(fetchedData, fetchedPage);    
    }
    if(currentPage == 1 ) {
        display();
    }
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
        lastPage = Number.MAX_VALUE;
        return 1;
    } else if(next == true && currentPage + extraPageInFrontAndBack <= lastPage) {
        return currentPage + extraPageInFrontAndBack;
    } else if (next == false && currentPage - extraPageInFrontAndBack > 0) {
        return currentPage - extraPageInFrontAndBack;
    } else {
        return -1;
    }
}

// update value of lastPage for a file
function updateLastPage(fetchedPageLength, page) {
    if (page == 1 && fetchedPageLength < recordsPerPage * noOfPages) {
            lastPage =  Math.ceil(fetchedPageLength / recordsPerPage);
            if(fetchedPageLength % recordsPerPage != 0)
                noOfRecordsOnLastPage = fetchedPageLength % recordsPerPage;
            else
                noOfRecordsOnLastPage = recordsPerPage;
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
    const listing_table = document.getElementById("listingTable");
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
async function search() {
    const searchString = document.getElementById("searchBar").value;
    if (fileName.value == "" || searchString == "") {
        return;
    }
    const params = new URLSearchParams();
    params.append('searchString', searchString);
    params.append('fileName', fileName.value);
    await fetch('/searchString', {
        method: 'POST',
        body: params
    });
    changePage(1);
}