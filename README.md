# VaxApp
![image](https://github.com/Ross97/COMP47910-1/assets/32261592/06168423-8a2a-43ea-b431-50dd173c5c20)
![image](https://github.com/Ross97/COMP47910-1/assets/32261592/b69a9281-2dda-4d82-9a55-b2e025b41948)
![image](https://github.com/Ross97/COMP47910-1/assets/32261592/32bd8aed-8430-4aee-8998-c3ea37448806)
![image](https://github.com/Ross97/COMP47910-1/assets/32261592/71fe8779-ed50-4486-8be2-f50bd9b6b97c)



## About

VaxApp is a web application consisting in an online reservation system for citizens aged 18 years and over who would like to receive their first COVID vaccination (1st or 2nd dose). The system keeps track of the doses received by each individual registered in the system. It also shows statistics about the profile of the individuals who received the vaccination, and provides a forum to ask questions about the vaccination campaign. It also provides a generic chatbot and notifications system.

## Prerequisites

To run this project,you must have:

- Maven
- Java version 11
- MySQL server with

1.  A MySQL user with enough privileges to create the database and handle table creation and updates. The `application.properties` file assumes username `admin` and password `admin` (to change this, modify the `application.properties` file).
2.  A database named `vaxapp`

## Running the project

### Installation

Clone the repository into a directory of your choice using `git clone`.

### Database setup

Make sure your server is running and take note of the port. The `application.properties` assumes your MySQL server runs on default port **3306**. If this is not the case, please modify the `application.properties` file with the corresponding port number.

Log into database server using and input the password in the prompt `mysql -u admin -p` and then create a new database using `create database vaxapp;`. Ensure the database was successfully created using `show databases;`


### Secrets Setup for Chatbot [OPTIONAL]
To enable the Chatbot feature, set environment variable `openaiKey` to a valid OpenAI API key. This can be achieved via IntelliJ run configurations or by changing the system's environment variables (e.g. on Linux `export openaiKey="..."`).

### Run spring application


The project can be open in any IDE (we recommend VSCode or Intellij). To run it, use the following command:

> `mvn clean install spring-boot:run`

IntelliJ also provides in-built features for using Maven and running Springboot applications. Simply run Maven lifecycles `clean` and `install`, and then run the `VaxApplication.java` file, which is the entrypoint to the project.

When the application is up and running, the port used will be specified in the terminal. Generally this should be localhost:8080, but it may differ depending on the machine. Please check the terminal to make sure.

If port 8080 is already in use, you will need to modify the `application.properties` file and specify a new port as follows:

> `# server.port = xxxx`

## Authors (initial project)

- Dragoș Feleaga
- Andrei Costin
- Andra Antal-Berbecaru

## Authors (extension for COMP47910 2024) 
- Ross Phelan
- Jo Reilly

## Reference Material
Shared Google Folder: https://drive.google.com/drive/folders/1cMfU5nN40N-KAZ-AFYep62JUJqpvFxCi?usp=sharing.
