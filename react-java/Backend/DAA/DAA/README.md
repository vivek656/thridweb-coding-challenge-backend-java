# Java Spring Boot Application

## overview of changes 

- Added crud operations in [AuthController](src/main/java/com/nam/controller/AuthController.java)
- update [pom.xml](pom.xml) to add library to fix compile issues in test classes
- Added exception handlers in [GlobalExceptionHandler](src/main/java/com/nam/advice/GlobalExceptionHandler.java)
- [CustomApplicationException](src/main/java/com/nam/exception/CustomApplicationException.java) to properly format
  [ApiError](src/main/java/com/nam/payload/response/ApiErrorResponse.java)
- [AuthControllerTest](src/test/java/com/nam/controller/AuthControllerTest.java) added tests for controller
- [GlobalExceptionHandlerTest](src/test/java/com/nam/advice/GlobalExceptionHandlerTest.java) added tests for advice


## postman collection with examples

import the postman collection [here](student_APP.postman_collection.json) for example of working APIs