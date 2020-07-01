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

// call docCount servlet for no of documents in a index
const getCount = async (index, fileType) => {
    const params = new URLSearchParams();
    params.append(FILE_NAME, index);
    params.append(FILE_TYPE, fileType);
    const response = await fetch(GET_COUNT, {
        method: POST,
        body: params
    });
    const count = await response.json();
    return count;
}

// call stackTrace servlet for stack trace of a error
const callStackTraceServlet = async (logLineNo, fileName) => {
    const params = new URLSearchParams();
    params.append(LOG_LINE_NUMBER, logLineNo);
    params.append(FILE_NAME, fileName);
    const response = await fetch(STACK_TRACE, {
        method: POST,
        body: params
    });
    const stackTrace = await response.json();
    return stackTrace;
}

// fetch logs/errors/search results from pagination servlet
const callPaginationServlet = async(fileName, fileType, searchString, pageSpecs) => {
    const params = new URLSearchParams();
    params.append(START, pageSpecs.START);
    params.append(SIZE,  pageSpecs.SIZE);
    params.append(SEARCH_STRING, searchString);
    params.append(FILE_TYPE, fileType);
    params.append(FILE_NAME, fileName);
    const response = await fetch(PAGINATION, {
        method: POST,
        body: params
    });
    const fetchedData = await response.json();
    return fetchedData;
}

// delete all the 
const deleteIndices = async() => {
    await fetch(DELETE, {
        method: POST
});

}