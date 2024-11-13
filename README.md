# Payment Gateway 
This repository contains the source code and design documents for the Payment Gateway server.  The module exposes API endpoints for validation or verification of payments made by applicants in modules accross MOSIP. It has been developed for and by Uganda.


## Database
See [DB guide](db_scripts/README.md)

## Config-Server


## Build & run (for developers)
Prerequisites:

1. [Config Server](https://docs.mosip.io/1.2.0/modules/module-configuration#config-server)
1. JDK 1.11  
1. Build and install:
    ```
    $ cd kernel
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
## Deploy

## Configuration

## Test 

## APIs

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).

