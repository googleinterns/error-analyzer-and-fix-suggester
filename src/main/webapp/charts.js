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

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(checkAndDrawChart);

async function checkAndDrawChart() {
    const fileName = document.getElementById(FILE_NAME).value;
    const piechart=document.getElementById(PIE_CHART);
    if(fileName == ""){
        piechart.style.visibility = HIDDEN;
        return;
    } else {
        piechart.style.visibility = VISIBLE;
    }
    drawChart(fileName);
}

// fetch log and error count from database and draw piechart
async function drawChart(fileName) {
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