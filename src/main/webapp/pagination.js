let currentPage = 1;
let next = true;
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
    }
    currentPage = page;
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
    const lastPage = display.lastPage;
    show(display, page);

    if (lastPage == true)
        showAndHideBtn(page, true);
    else
        showAndHideBtn(page);


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



function show(display, page) {
    const listing_table = document.getElementById("listingTable");
    const page_span = document.getElementById("page");
    listing_table.innerHTML = "";
    // dynamically add element to result page
    display.logOrError.forEach((logError) => {
        listing_table.innerHTML += logError + "<br>";
    })
    page_span.innerHTML = page;
}

// display next and previous button 
function showAndHideBtn(page, lastPage) {
    const btnPrev = document.getElementById("btnPrev");
    const btnNext = document.getElementById("btnNext");

    // hide previous button when on page 1
    if (page == 1) {
        btnPrev.style.visibility = "hidden";
    } else {
        btnPrev.style.visibility = "visible";
    }
    // hide next button when 
    if (lastPage != undefined) {
        btnNext.style.visibility = "hidden";
    } else {
        btnNext.style.visibility = "visible";
    }
}