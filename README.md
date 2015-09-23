Accelerometer Demo
================
An Android app that captures and displays accelerometer data on a professionally looking gauge and uploads the data to PubNub where it can be viewed on a web page. Designed to run on Android devices with API 16+.

![Accel Demo](https://github.com/JohannBlake/Accel/blob/master/graphics/accel.gif)


Description
-----------
This app is used as a coding example for Android developers on how to capture and display accelerometer data on a professionally looking gauge in real-time.

Data is also uploaded to PubNub where it can also be viewed on a web page in almost real-time.

The gauge contains a button that lets you cycle through 4 modes:

* Speed
* Current distance
* Total distance
* Demo

In Speed mode, speed is shown when you move the device. Shaking the device faster will result in a larger value being displayed.

In Current Distance mode, the app accumulates distance from the time the app starts and any motion is detected. Distance is displayed on the 7 segment display as a value and also shown on the colored bar segments.

In Total Distance mode, the app displays the total distance accumulated since the app first started. By "first started", this means that the very first time you started the app after installing it. If you exit the app and start it again, that would be the second time. Total Distance is shown only on the 7 segment display.

In Demo mode, the accelerometer data is ignored and fake speed values are incremented from zero till 10 and then back down to zero and then repeats.

After installing the app, you can also see the data on a web page by visiting:

http://johannblake.github.io/Accel/index.html

The gauge also displays a small connection icon. When it is red, it means that no data is being uploaded to PubNub. This could be because the device's wifi is turned off, or possibly because PubNub's servers are not available. When data is successfully sent, the icon will be green.

If multiple devices run the app simultaneously, the gauge data on the web page will update for each device.  This is because PubNub forwards the data to whatever clients subscribe to the data. In a real application, you would probably not want multiple devices updating a single gauge at the same time. The purpose here however is simply to demonstrate how data can be easily transmitted from a device to a web page in near real-time.

The app only forwards 5 data points per second to PubNub.



Coding Features
---------------

* Capturing accelerometer data
* Buffering the data in a FIFO
* A custom gauge widget that demonstrates a combination of png and drawing functionality
* Integration of PubNub's API
* Design patterns including separation of UI, business layer and data access layer.


Automated Tests
---------------

The project includes automated testing to perform functionality tests. The tests are located in under the androidTest folder. The tests that are carried out are:

* Test simulated guage data for each mode.
* Test the storage of local preferences.
* Test the transmission of data to PubNub.

Development
-----------

This app was developed using Android Studio 1.3.2 with a project structure being the default structure when creating a new project. It uses Gradle 2.4.


