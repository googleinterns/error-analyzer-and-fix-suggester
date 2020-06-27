// if on 2nd slide of carousel 
addclass = () => {
    const slide1 = document.getElementById(SLIDE_1_CONTAINER);
    const slide2 = document.getElementById(SLIDE_2_CONTAINER);
    slide1.classList.add(ACTIVE);
    slide2.classList.remove(ACTIVE);

}

// add choosen filter to search
addToSearch = (searchString) => {
    const searchBar = document.getElementById(SEARCH_BAR);
    searchBar.value += " " + searchString;
    search();
    addclass();
}

// hide stackTraces 
addHideClass = () => {
    let stackTraceContainer = document.getElementById(STACK_TRACE_CONTAINER);
    stackTraceContainer.className = HIDE;
    let crossBtn = document.getElementById(CROSS);
    crossBtn.className = HIDE;
}

// search dataBase for the requested string
search = () => {
    const searchString = document.getElementById(SEARCH_BAR).value;
    if (searchString == "") {
        return;
    }
    changePage(1);
}


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

// change no of records on a page 
changeNoOfRecordsOnPage = () => {
    const records = document.getElementById(RECORDS);
    recordsPerPage = records.value;
    changePage(1);
}