google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);
async function drawChart() {
    const fileName = document.getElementById("fileName").value;
    const piechart=document.getElementById("piechart");
    if(fileName == ""){
        piechart.style.visibility = "hidden";
        return;
    } else {
        piechart.style.visibility = "visible";
    }
    const log = await getCount(fileName, LOGS);
    const error= await getCount(fileName, ERRORS);
    let data = google.visualization.arrayToDataTable([
        ['logError',  '%'],
        ['Other Logs', log-error],
        ['Error', error]
    ]);

    const options = {
          title: '% Error'
    };

    const chart = 
        new google.visualization.PieChart(document.getElementById('piechart'));
    chart.draw(data, options);
}

async function getCount(index, fileType) {
    const params = new URLSearchParams();
    params.append('fileName', index);
    params.append('fileType', fileType);
    const response = await fetch('/getCount', {
        method: 'POST',
        body: params
    });
    const count = await response.json();
    return count;
}

addclass = () => {
    const slide1 = document.getElementById("slide1Container");
    const slide2 = document.getElementById("slide2Container");
    slide1.classList.add('active');
    slide2.classList.remove('active');

}

addToSearch = (searchString) => {
    const searchBar = document.getElementById("searchBar");
    searchBar.value += " " + searchString;
    search();
    addclass();
}
