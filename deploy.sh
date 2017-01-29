#!/bin/bash

ENV_LOCATION="$HOME/backend_server"
SOURCE_FODLER="Server"

INITIAL_PWD="$PWD"

echo 'Installing dependencies...'
apt-get -y install virtualenv python-pip python-dev python3-dev
RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Dependencies installed successfully.'
else
    (>&2 echo 'ERROR: Dependencies could not be installed.')
    exit $RESULT
fi

echo 'Installing dependencies for heartbeat...'
pip install apache-libcloud pycrypto
RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Heartbeat dependencies installed successfully.'
else
    (>&2 echo 'ERROR: Heartbeat dependencies could not be installed.')
    exit $RESULT
fi

echo 'Activating virtual environment...'
virtualenv "$ENV_LOCATION"
source "$ENV_LOCATION"/bin/activate
RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Virtual environment activated.'
else
    (>&2 echo 'ERROR: Virtual environment cannot be activated.')
    exit $RESULT
fi

echo 'Installing requirements...'
pip install -r "$SOURCE_FODLER/requirements.txt"
RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Requirements installed successfully.'
else
    (>&2 echo 'ERROR: Requirements could not be installed.')
    exit $RESULT
fi

echo 'Starting the server...'
cd "$SOURCE_FODLER"

gunicorn -t 300 main:app --bind 0.0.0.0:80 &
RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Installation successful, server is running.'
else
    (>&2 echo 'ERROR: Serrver cannot be started.')
    exit $RESULT
fi

cd "$INITIAL_PWD"
deactivate

exit 0
