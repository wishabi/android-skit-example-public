# android-skit-example
This sample project utilizes the Flyer Kit API and SFML SKit library to render storefronts.  Please review the [wiki](https://github.com/wishabi/android-skit-example/wiki) for more information.

## Getting started

1. Clone the project.
```
git clone https://github.com/wishabi/skit2demo
```  
2. Obtain `username` and `password` credentials to access our [JFrog Artifactory](https://flipplib.jfrog.io/flipplib/android-skit-local).
3. Replace the username and password placeholders in the `./settings.gradle` file.
```  
username = "{{ FLIPP_PROVIDED_JFROG_USERNAME }}"  
password = "{{ FLIPP_PROVIDED_JFROG_PASSWORD }}"  
```
4. Obtain a Flyer Kit API token to access Flipp backend services.
5. Replace the api token placeholder in the `build.gradle` file.
```
buildConfigField("String", "FLYER_KIT_API_TOKEN", "{{ FLIPP_PROVIDED_FLYER_KIT_API_TOKEN }}")
```
6. Run the Gradle build command:
```
./gradlew clean build
```