#!/usr/bin/env python3

from flask import Flask
from flask import request
from flask import g
from flask import jsonify
from flask_httpauth import HTTPBasicAuth
from itsdangerous import (TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired)
from libcloud.compute.types import Provider
from libcloud.compute.providers import get_driver
from libcloud.common.google import ResourceNotFoundError
import json
import subprocess

auth = HTTPBasicAuth()

app = Flask(__name__)
app.config['DEBUG'] = True

users = {
    'test': 'bd2b1aaf7ef4f09be9f52ce2d8d599674d81aa9d6a4421696dc4d93dd0619d682ce56b4d64a9ef097761ced99e0f67265b5f76085e5b0ee7ca4696b2ad6fe2b2',
    'newuser': '857b95a7e57b32249f27d1e425fbadab6b0d51adee4e251c50d7c6b5d137d775b7d053dacf778ffb19faf840ee20d511d7450262602d4e18d539addcf9c847cb'
}

# Remote applications are defined here
apps = {'apps': [
    {
        'instanceName': 'openoffice',
        'readableName': 'OpenOffice'
    },
    {
        'instanceName': 'inkscape',
        'readableName': 'Inkscape'
    }
]}
SECRET_KEY = 'thisAppIsAwesome:)'
PASSWD_STRING = 'passwordString'
TOKEN_EXPIRATION = 3600  # 60 minutes

# Initialize libcloud Google Compute Engine Driver using service account authorization
ComputeEngine = get_driver(Provider.GCE)
gce = ComputeEngine('860271242030-compute@developer.gserviceaccount.com', 'key/mcc-2016-g13-p1-290f94a963cb.json',
                    datacenter='europe-west1-d', project='mcc-2016-g13-p1')

running_node = None
running_node_name = None
heartbeat_process = None


@auth.verify_password
def verify_password(user_token, password):
    user = verify_auth_token(user_token)
    if user:  # User from token
        g.user = user
        return True
    else:
        if user_token in users:
            if password == users.get(user_token):
                g.user = user_token
                return True

    return False


def generate_auth_token(user, expiration=TOKEN_EXPIRATION):  # 1200~20minutes
    s = Serializer(SECRET_KEY, expires_in=expiration)
    return s.dumps({'id': user})


def verify_auth_token(token):
    s = Serializer(SECRET_KEY)
    try:
        data = s.loads(token)
    except SignatureExpired:
        return None  # valid token, but expired
    except BadSignature:
        return None  # invalid token

    if data['id'] in users:
        return data['id']
    else:
        return None


@app.route('/token/')
@auth.login_required
def get_token():
    print('Token for user: ' + str(g.user))

    token = generate_auth_token(g.user)
    return jsonify({'token': token.decode('ascii')})


@app.route('/getapps/')
@auth.login_required
def get_apps():
    print('Listing apps')
    return json.dumps(apps)


@app.route('/heartbeat/')
@auth.login_required
def heartbeat():
    print('Heartbeat')
    global heartbeat_process
    if heartbeat_process is None:
        print('No heartbeat process running')
        return 'False'

    # Restart heartbeat script
    heartbeat_process.kill()
    heartbeat_process = subprocess.Popen(['python', 'heartbeat.py', str(running_node_name)])

    token = generate_auth_token(g.user)
    return jsonify({'token': token.decode('ascii')})


@app.route('/start/', methods=['POST'])
@auth.login_required
def start():
    application = request.form.get('app')
    if application is None:
        return 'False: No application submitted'

    print('Starting ' + str(application))

    # Check if application is defined in backend
    if not any(i['instanceName'] == application for i in apps['apps']):
        print('No such application, aborting: ' + str(application))
        return 'False: No such application'

    global running_node_name
    global running_node

    # If there's already a previous application instance running, stop it first
    # This might happen after connection problems, if the heartbeat process has not killed the instance yet
    if running_node_name is not None and running_node is not None:
        print('Previous application still running: ' + running_node_name)
        stop()

    running_node_name = application
    try:
        node = gce.ex_get_node(running_node_name)
    except ResourceNotFoundError:
        print('Instance not found, aborting')
        return 'False: Instance does not exist'

    result = gce.ex_start_node(node)
    if result:
        nodes = [node]
        ip = gce.wait_until_running(nodes, wait_period=3, timeout=60)
        running_node = node

        # Check if instance has a public ip
        try:
            ip[0][1]
        except IndexError:
            print('Instance public ip not found, aborting')
            return 'False: Instance failed to acquire a public IP'

        # VM has started, respond with IP and start the heartbeat process
        global heartbeat_process
        heartbeat_process = subprocess.Popen(['python', 'heartbeat.py', running_node_name])

        print('Started ' + running_node_name)

        return ip[0][0].public_ips[0]

    print('Instance failed to start')
    return 'False: Instance failed to start'


@app.route('/stop/')
@auth.login_required
def stop():
    global running_node
    global running_node_name
    global heartbeat_process
    if running_node is None:
        return 'False: Instance already stopped'

    print('Stopping ' + running_node_name)

    gce.ex_stop_node(running_node)

    heartbeat_process.kill()

    running_node = None
    running_node_name = None
    heartbeat_process = None
    print('Stopped')
    return 'True'


@app.errorhandler(404)
def page_not_found(e):
    return 'Sorry, nothing at this URL.', 404


@app.route('/form/', methods=['POST'])
def form():
    user = request.form.get('user')
    password = request.form.get('password')
    if user == 'test' and password == 'secret':
        return 'Authenticated'
    else:
        return 'Wrong credentials'


@app.route('/')
def hello():
    return 'Welcome to the backend!'
