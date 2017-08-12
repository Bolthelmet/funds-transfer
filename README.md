# Simple Accounts system
Endpoints to create user, transfer money and retrieve data about users and transfers made are presented.
In-memory mode H2 database is used as inmemory storage so every time server is restarted data will be whiped out.

# Instructions
## Start server
* sbt run
* go to localhost:9000 for api documentation/ api handler
* enjoy

## Run tests
* sbt clean test
