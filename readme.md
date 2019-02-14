 For this action, a proxy server is required. The proxy server handles all Be-Bound requests, and forwards them to the external API. It then handles the responses and forwards them through Be-Bound.
 
### About the project

The external API is (https://swapi.co/)[https://swapi.co/]

The Android app consists of a simple text box where the user can insert a number. This number corresponds to a Star Wars character.
- By clicking, "send by HTTP", the app makes a direct call to https://swapi.co/.
- By clicking on "send by Be-Bound", the app sends a Be-Bound request. See more details (here)[https://doc.be-bound.com/docs/request-workflow]

In both cases, the response contains the name of the character and the name of the planet where they were born.
### How to test 

This project requires several steps before working. Follow these steps before testing:

1- Download (Android)[https://github.com/Be-Bound/java-external-api-template-android] project and (Backend)[https://github.com/Be-Bound/java-external-api-template-backend]. 

2- Create a project on Be-Bound (Console)[https://console.be-bound.com]

- Replace the default ID (which is 46) located on BeApp.java (Android project) with the project ID of your new project.