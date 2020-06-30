/**Copyright 2019 Google LLC
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

// add choosen filter to search bar and fetch search results from database
addFilterToSearch = (searchString) => {
    const searchBar = document.getElementById(SEARCH_BAR);
    searchBar.value += " " + searchString;
    search();
    moveCarouselToFirstSlide();
}

// search dataBase for the requested string
search = () => {
    const searchString = document.getElementById(SEARCH_BAR).value;
    if (searchString == "") {
        return;
    }
    changePage(1);
}

// change no of records to be shown on a page 
changeNoOfRecordsOnPage = () => {
    const records = document.getElementById(RECORDS);
    updateRecordsPerPage(records.value);
    changePage(1);
}