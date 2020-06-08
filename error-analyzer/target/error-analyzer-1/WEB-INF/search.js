
async function search() {
    const fileName = document.getElementById("fileName").value;
    const searchString=document.getElementById("searchBar").value;
    if(fileName==undefined || searchString==undefined)
    return;

    const params = new URLSearchParams();
    params.append('searchString', searchString);
    params.append('fileName', fileName);

    const response = await fetch('/search', {
        method: 'POST',
        body: params
    });

}