import socket
import struct
from datetime import datetime

MCAST_GRP = '226.1.1.1'
MCAST_PORT = 5555

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind((MCAST_GRP, MCAST_PORT))

mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

logfile = open("log.txt","w+")

while True:
    data, address = sock.recvfrom(64)
    now = datetime.now()
    line = str(now)[:-7] + ' ' + str(data.decode('utf-8') + '\n')
    logfile.write(line)
    print (str(now)[:-7], data.decode('utf-8'))