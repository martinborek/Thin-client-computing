# Temerarious Thirteens - Project 1 for CSE-4100

## How to start

* For the frontend you can either import the _ThinClientG13-mobile_ project into Android Studio and build the APK or simply install the APK [from the root of this repo](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g13-p1/blob/master/ThinClientG13.apk).

* Backend server can be started with a deployment script. The deployment script needs to run with administrative privileges.

```sh
sudo ./deploy.sh
```

Login credentials for the application:
* User: test
* Password: secret

## In this repository you will find

### 'ThinClientG13-mobile'

Includes the Android application source code.

The code uses the *MultiVNC* application as a starting point. In the folder _app/src/main/java/com/_ you will find _antlersoft_ and _coboltforge_ which come from the original *MultiVNC* application. Our code is in the _mccG13_ folder.

Classes named as _\*BW.java_ refer to background async tasks for interacting with the server. The login view and the app selection view are controlled respectively by the classes _MainActivity.java_ and _AppSelectionActivity.java_.

In addition, as the server IP can change, you have the option to set it from the preferences menu on the top right corner of the login view.

### 'Server'
Server is the backend for the application. It authenticates users and allows them to use prepared Virtual Machines (VMs). Front-end obtains a token when a user is authenticated with username and password. This token is used when sending commands to the backend. The backend accepts commands to list available VM applications, start and stop them. The front-end is expected to inform the backend regularly (every 10 minutes) that it is still using the running VM (heartbeat). If no heartbeat is received for 30 minutes, a running VM is stopped.

The backend is written in Python, using Flask and Gunicorn. Apache-libcloud library is used for Google Compute Engine management.

**Files**

+ main.py - Main server script
+ heartbeat.py - Heartbeat process, running when a VM is started
+ requirements.txt - Requirements for the application, installed with pip

### 'Documentation'
Includes any other relevant files for the understanding of the application
