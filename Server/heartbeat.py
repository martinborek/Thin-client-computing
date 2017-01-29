
import time
import sys
import libcloud
from libcloud.compute.types import Provider
from libcloud.compute.providers import get_driver

ComputeEngine = get_driver(Provider.GCE)
gce = ComputeEngine('860271242030-compute@developer.gserviceaccount.com', 'key/mcc-2016-g13-p1-290f94a963cb.json',
                    datacenter='europe-west1-d', project='mcc-2016-g13-p1')

def main():

    if len(sys.argv) != 2:
        sys.exit(0)
    running_node_name = sys.argv[1]
    print("Heartbeat for " + str(running_node_name))

    time.sleep(60*30) # 30 minutes
    print("No heartbeat,  killing " + str(running_node_name))

    # Initialize libcloud Google Compute Engine Driver using service account authorization

    node = gce.ex_get_node(running_node_name)
    gce.ex_stop_node(node)


main()
