# Error Analyzer and Fix Suggester
The project aims at improving the productivity of beginners & intermediate developers.

#### Given a large error log(obtained through URLs, log files, plain text etc.), the tool will:
- Identify errors and highlight them.
- Display a paginated view of log files.
- Provide log search interface for searching large files to allow user to search based on regex.
- Provide insights on the error such as relevant links.
#### Motivation
Beginners at programming find it very difficult to debug their code. A major reason for this is the need to analyze error logs. They find it difficult to find the correct error and its fix.
Moreover, manually searching for errors in large log files (say > 5M) is difficult even for more experienced programmers. Hence, creating a tool which summarizes the error logs, provides a search interface and gives suggestions to fix the logs becomes very useful.

#### Tech used
**Database**
We used elasticsearch as our database engine. We utilise elasticsearch's inbuilt features of full-text search and regex query to identify error lines.  
**Server**
The database is connected to the server using java high-level REST API. Maven app engine is used to run the application.
