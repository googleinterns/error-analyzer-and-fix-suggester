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

// lastPage contains last page no of the file user is viewing 
let lastPage = Number.MAX_VALUE;
// cuttent page user is at
let currentPage = 1;
// next true means user have asked for next 
// page false means user is asking for previous page 
let next = true;

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