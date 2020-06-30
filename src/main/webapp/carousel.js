// display file not found message on UI
fileNotFound = () => {
    listing_table1  = document.getElementById(SLIDE_2);
    listing_table2 =  document.getElementById(SLIDE_1);
    listing_table1.innerHTML = FILE_NOT_FOUND;
    listing_table2.innerHTML = FILE_NOT_FOUND;
    updateCurrentPage(1);
    updateLastPage(1);
    showAndHideNavigationBtn();
}

// if on 2nd slide of carousel move to 1st slide
moveToFirstSlide = () => {
    const slide1 = document.getElementById(SLIDE_1_CONTAINER);
    const slide2 = document.getElementById(SLIDE_2_CONTAINER);
    slide1.classList.add(ACTIVE);
    slide2.classList.remove(ACTIVE);

}

// display next and previous button 
showAndHideNavigationBtn = () => {
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

// decrement by 1 on pressing previous button
prevPage = () => {
    updateCurrentPage(getCurrentPage()-1);
    updateNextVariable(false);
    changePage(currentPage);
}

// increment by 1 on pressing next button
nextPage = () => {
    updateCurrentPage(getCurrentPage()+1);
    updateNextVariable(true);
    changePage(currentPage);
}