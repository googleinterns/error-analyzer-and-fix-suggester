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

// noOfPages contains page length of window that we are maintaining
const noOfPages = 5;
const extraPageInFrontAndBack = Math.floor(noOfPages/2);
// lastPage contains last page no of the file user is viewing 
let lastPage = Number.MAX_VALUE;
// cuttent page user is at
let currentPage = 1;
// next true means user have asked for next 
// page false means user is asking for previous page 
let next = true;
// no of records to be shown on each page 
let recordsPerPage = 3;
// its not necessary that the no of records in a file will be a
//  multiple of records per page so no of records on last page
//  may vary from 0 to recordsPerPage
let noOfRecordsOnLastPage = recordsPerPage;

getCurrentPage = () => {
    return currentPage;
}

updateCurrentPage = (page) => {
    currentPage = page;
}

updateLastPage = (page) => {
    lastPage = page;
}

updateNextVariable = (value) => {
    next = value;
}

updateRecordsPerPage = (recordLength) => {
    recordsPerPage = recordLength;
}

updateNoOfRecordsOnLastPage = (recordLength) => {
    noOfRecordsOnLastPage = recordLength;
}