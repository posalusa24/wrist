import paho.mqtt.client as paho
import os
import csv
from prettytable import PrettyTable


state = {}

broker = 'broker.hivemq.com'
port = 1883
topic = 'wrist/8571391'
client_id = 'monitor1'

def on_connect(client_id, userdata, flags, rc, properties):
    if rc == 0:
        print('Connected to broker')
    else:
        print('Failed to connect')
    update_display()


def update_display():
    table = PrettyTable()
    table.field_names = ["Name", "Current Location"]

    user_name = {}
    with open('mapping_users.csv') as user_name_file:
        user_name_reader = csv.reader(user_name_file)
        for row in user_name_reader:
            user_name[row[0]] = row[1]

    mapping = {}
    with open('mapping_locations.csv') as mapping_file:
        mapping_reader = csv.reader(mapping_file)
        for row in mapping_reader:
            mapping[row[0]] = row[1]

    for userid in state:
        table.add_row([user_name[userid], mapping[state[userid]] if state[userid] in mapping.keys() else 'Unknown ({})'.format(state[userid])])

    table.title = 'Current locations of users'
    os.system('clear')
    print(table)

def on_message(client, userdata, msg):
    tokens = msg.payload.decode('utf-8').strip().split()
    if tokens[0] != 'scantoken':
        return
    userid = tokens[1]
    bssid = tokens[2]
    client_id = userid
    #print('DEBUG', bssid)
    #print(userid)


    if (not client_id in user_name.keys()):
        return
    state[client_id] = bssid
    update_display()

client = paho.Client(paho.CallbackAPIVersion.VERSION1, client_id, userdata=None, protocol=paho.MQTTv5)

client.on_connect = on_connect
client.on_message = on_message

client.connect(broker, port)


client.subscribe(topic)

client.loop_forever()
