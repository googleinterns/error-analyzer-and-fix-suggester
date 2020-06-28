google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);
async function drawChart() {
    const fileName = document.getElementById(FILE_NAME).value;
    const piechart=document.getElementById(PIE_CHART);
    if(fileName == ""){
        piechart.style.visibility = HIDDEN;
        return;
    } else {
        piechart.style.visibility = VISIBLE;
    }
    addStatsToChart(fileName);
}

// fetch log and error count from database and draw piechart
async function addStatsToChart(fileName) {
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
        new google.visualization.PieChart(document.getElementById(PIE_CHART));
    chart.draw(data, options);
}