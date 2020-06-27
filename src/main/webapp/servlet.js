// call docCount servlet for no of documents in a index
async function getCount(index, fileType) {
    const params = new URLSearchParams();
    params.append(FILE_NAME, index);
    params.append(FILE_TYPE, fileType);
    const response = await fetch('/getCount', {
        method: 'POST',
        body: params
    });
    const count = await response.json();
    return count;
}

// call stackTrace servlet for stack trace of a error
async function callStackTraceServlet(logLineNo, fileName) {
    const params = new URLSearchParams();
    params.append(LOG_LINE_NUMBER, logLineNo);
    params.append(FILE_NAME, fileName);
    const response = await fetch('/stackTrace', {
        method: 'POST',
        body: params
    });
    const stackTrace = await response.json();
    return stackTrace;
}

// fetch logs/errors/search results from pagination servlet
async function callPaginationServlet(fileName, fileType, searchString, pageSpecs) {
    const params = new URLSearchParams();
    params.append(START, pageSpecs.START);
    params.append(SIZE,  pageSpecs.SIZE);
    params.append(SEARCH_STRING, searchString);
    params.append(FILE_TYPE, fileType);
    params.append(FILE_NAME, fileName);
    const response = await fetch('/pagination', {
        method: 'POST',
        body: params
    });
    const fetchedData = await response.json();
    return fetchedData;
}