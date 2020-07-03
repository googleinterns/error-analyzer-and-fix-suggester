# Error Analyzer and Fix Suggester
The project aims at improving the productivity of beginners & intermediate developers.


### Features
**Given a large error log(obtained through URLs, log files, plain text etc.), the tool will:**  
- Identify errors and highlight them.
- Display a paginated view of log files.
- Provide log search interface for searching large files to allow user to search based on regex.
- Provide insights on the error such as relevant links.
### Motivation
Beginners at programming find it very difficult to debug their code. A major reason for this is the need to analyze error logs. They find it difficult to find the correct error and its fix.
Moreover, manually searching for errors in large log files (say > 5M) is difficult even for more experienced programmers. Hence, creating a tool which summarizes the error logs, provides a search interface and gives suggestions to fix the logs becomes very useful.

### Tech used
- **Database:**
We used elasticsearch as our database engine. We utilise elasticsearch's inbuilt features of full-text search and regex query to identify error lines.  
- **Server:**
The database is connected to the server using java high-level REST API. Maven app engine is used to run the application.

### Prerequisite
##### Installing Maven:
    
    sudo apt-get install maven

##### Java 8:
**Installing Java 8**:

    sudo apt-get install default-jdk
    sudo apt-get install oracle-java8-installer
**Setting Java variables**:
Find where java is installed:

    sudo update-alternatives --config java
Copy the path from installation and then open /etc/environment using nano or your favorite text editor

    sudo nano /etc/environment
At the end of this file, add the following line, making sure to replace the path with your own copied path

    JAVA_HOME="/usr/lib/jvm/java-8-oracle"
Save and exit the file, and reload it

    source /etc/environment
 ##### Elasticsearch:
 
[Elasticsearch installation link](https://www.elastic.co/guide/en/elasticsearch/reference/7.x/install-elasticsearch.html)
After setting up elasticsearch, add the ip address instead of localhost in RestHighLevelClient in src/main/java/com/google/error_analyzer/backend/LogDao.java

##### Kibana:

[Kibana installation link](https://www.elastic.co/guide/en/kibana/7.x/install.html)

##### Google Custom Search API:

[CustomSearch API](https://developers.google.com/custom-search/)

##### Index template Setting

Set the index template given in
error-analyzer-and-fix-suggester/src/main/java/com/google/error_analyzer/backend/index_template.json 
using kibana. For setting index template refer [Index template setting](https://www.elastic.co/guide/en/elasticsearch/reference/7.x/indices-templates.html)

#### To run the app use:

    mvn package appengine:run

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

Apache header:

    Copyright 2020 Google LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
