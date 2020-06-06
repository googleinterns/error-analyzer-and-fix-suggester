let currentPage = 1;
let next = true;

// decrement by 1 on pressing previous button
function prevPage() {
    currentPage--;
    next = false;
    changePage(currentPage);

}

// increment by 1 on pressing next button
function nextPage() {
    currentPage++;
    changePage(currentPage);
}

// change content of page 
async function changePage(page) {

    var btnPrev = document.getElementById("btnPrev");
    var btnNext = document.getElementById("btnNext");
    const listing_table = document.getElementById("listingTable");
    const page_span = document.getElementById("page");
    const fileName = document.getElementById("fileName");
    const logs = document.getElementById("logs");
    const fileType = logs.checked ? "logs" : "errors";
    if (page < 1) page = 1;

    // if page is undefined page is refreshed fetch the correct page to show from localstorage
    if (page == undefined) {
        currentPage = localStorage.getItem("page");
    // if localstorage don't have current page value show page 1
        if (page == undefined)
            page = 1;
    }
    
    const params = new URLSearchParams();
    params.append('requestedPage', currentPage);
    params.append('fileType', fileType);
    params.append('fileName', fileName.value);
    params.append('next', next);

    // ask for data to display from java servlet 
    const response = await fetch('/pagination', {
        method: 'POST',
        body: params
    });
    const display = await response.json();
    const logsOrErrors = display.logsOrErrors;
    const totalPages = display.totalPages;
    listing_table.innerHTML = "";

    // dynamiocally add element to result page
    logsOrErrors.forEach((logError) => {
        listing_table.innerHTML += logError + "<br>";
    })

    page_span.innerHTML = page + "/" + totalPages;

    // hide previous button when on page 1
    if (page == 1) {
        btnPrev.style.visibility = "hidden";
    } else {
        btnPrev.style.visibility = "visible";
    }
    // hide next button when 
    if (page == totalPages) {
        btnNext.style.visibility = "hidden";
    } else {
        btnNext.style.visibility = "visible";
    }

    // store current page so that on refreshing the page we are not taken to page 1 again
    localStorage.setItem('page', currentPage);
}