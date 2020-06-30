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

let lastPage = Number.MAX_VALUE;
let currentPage = 1;
let next = true;

updateCurrentPage = (page) => {
    currentPage = page;
}

decrementCurrentPageBy1 = () => {
    currentPage--;
}

incrementCurrentPageBy1 = () => {
    currentPage++;
}

updateLastPage = (page) => {
    lastPage = page;
}

updateNextVariable = (value) => {
    next = value;
}