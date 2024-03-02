# Content Moderation System

### Description

The Content Moderation System is a service designed to process CSV files containing user 
messages, perform content moderation tasks such as translation and offensiveness scoring, and generate 
a processed CSV file with aggregated statistics.

### Features

* Accepts CSV files with user messages for processing.
* Translates messages to English using a translation service.
* Calculates offensiveness scores for messages using a scoring service.
* Generates a processed CSV file containing aggregated statistics such as total messages and average score per user.

### Technologies Used

* Java
* Spring Boot
* Lombok
* Servlet API

### Installation

1. Clone the repository: git clone https://github.com/ArtmeG/content-moderation-system.git
2. Navigate to the project directory: cd content-moderation-system
3. Build the project: ./gradlew build
4. Run the application: ./gradlew bootRun

### Usage

1. Make a POST request to the /api/process-csv endpoint with a CSV file containing user messages as a multipart form-data.
2. The processed CSV file will be downloaded automatically.


### API Documentation

* #### Endpoint: "/api/process-csv"
* #### Method: POST
* #### Request Parameter:
  * #### 'file': 
    * Multipart file containing user messages in CSV format.
* #### Response:
  * #### Content-Type: 'text/csv'
  * #### Content-Disposition: attachment; filename=processed.csv
  * Body: Processed CSV file with aggregated statistics.
