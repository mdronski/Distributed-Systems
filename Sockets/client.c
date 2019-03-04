#include <stdlib.h>
#include <stdio.h>
#include <memory.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/un.h>
#include <sys/epoll.h>
#include <pthread.h>
#include <zconf.h>
#include <signal.h>
#include <jmorecfg.h>

typedef enum IP_Protocol {
    TCP = 0,
    UDP = 1,
}IP_Protocol;

typedef enum Message_Type{
    JOIN = 0,
    SEND = 1,
}Message_Type;

typedef struct Token {
    char message[32];
    in_addr_t dest_ip_address;
    in_port_t dest_port;
    in_addr_t join_ip_address;
    in_port_t join_port;
    int cnt;
    Message_Type message_type;
}Token;

Token token;
char *user_id;
in_addr_t ip_address;
int out_port;
int in_port;
int have_token;
int out_tcp_socket;
int in_tcp_socket;
int udp_socket;
enum IP_Protocol protocol;

void send_message(Message_Type message_type);
void receive_message();
void clean_exit();
void error_exit(char *error_message);
void parse_args(int argc, char **argv);
void connect_to_token_ring();
void *connect_out_tcp_socket(void *pVoid);
void *connect_in_tcp_socket(void *pVoid);
void handle_join(struct sockaddr_in addr);
void handle_message();

int main(int argc, char** argv){
    atexit(clean_exit);
    parse_args(argc, argv);
    connect_to_token_ring();

    send_message(JOIN);
//    receive_message();

    while(1){
        send_message(SEND);
        receive_message();
        sleep(1);
    }

}

void parse_args(int argc, char **argv){
//    if (argc != 7)
//        error_exit("invalid arguments");

    user_id = argv[1];

    inet_aton(argv[2], (struct in_addr *) &ip_address);
    printf("%d\n", ip_address);
    if (ip_address == INADDR_NONE)
        error_exit("Invalid ip address");

    char *ptr = argv[3];
    out_port = (int) strtol(ptr, &ptr, 10);
    if (*ptr != '\0')
        error_exit("Invalid port number");
    if (out_port < 1024 || out_port > 60999)
        error_exit("Invalid port number");

    ptr = argv[4];
    in_port = (int) strtol(ptr, &ptr, 10);
    if (*ptr != '\0')
        error_exit("Invalid port number");
    if (out_port < 1024 || out_port > 60999)
        error_exit("Invalid port number");


    if (strcmp(argv[5], "tcp") == 0){
        protocol = TCP;
    } else if (strcmp(argv[5], "udp") == 0){
        protocol = UDP;
    } else
        error_exit("Invalid protocol type");

    ptr = argv[6];
    have_token = strtol(ptr, &ptr, 10) == 0 ? 0 : 1;
    if (have_token){
        token.message[0] = 'H';
        token.message[1] = 'e';
        token.message[2] = 'l';
        token.message[3] = 'l';
        token.message[4] = 'o';
        token.cnt = 0;
        token.join_port = 0;
        token.join_ip_address = 0;
    }

}

void connect_to_token_ring(){

    switch (protocol) {
        case TCP: {
            pthread_t connect_out_socket_thread;
            pthread_t connect_in_socket_thread;
            pthread_create(&connect_in_socket_thread, NULL, connect_in_tcp_socket, NULL);
            pthread_create(&connect_out_socket_thread, NULL, connect_out_tcp_socket, NULL);
            pthread_join(connect_out_socket_thread, NULL);
            pthread_join(connect_in_socket_thread, NULL);
            break;
        }

        case UDP: {
            udp_socket = socket(AF_INET, SOCK_DGRAM, 0);
            if(udp_socket == -1)
                error_exit("ip socket creation failure");

            struct sockaddr_in in_address;
            bzero(&in_address, sizeof(in_address));
            in_address.sin_family = AF_INET;
            in_address.sin_addr.s_addr = INADDR_ANY;
            in_address.sin_port = htons((uint16_t) in_port);

            if (bind(udp_socket, (const struct sockaddr *) &in_address, sizeof(in_address)) == -1)
                error_exit("in socket bind failure");

            break;
        }
    }

    printf("Connected\n");
}

void *connect_out_tcp_socket(void *pVoid){
    out_tcp_socket = socket(AF_INET, SOCK_STREAM, 0);
    if(out_tcp_socket == -1)
        error_exit("ip socket creation failure");


    struct sockaddr_in out_address;
    bzero(&out_address, sizeof(out_address));
    out_address.sin_family = AF_INET;
    out_address.sin_addr.s_addr = htonl(ip_address);
    out_address.sin_port = htons((uint16_t) out_port);

    char my_ip[16];
    int len ;
    unsigned int my_port;

    if(connect(out_tcp_socket, (const struct sockaddr *) &out_address, sizeof(out_address)) == -1)
        error_exit("ip connection failure");

    bzero(&out_address, sizeof(out_address));
    getsockname(udp_socket, (struct sockaddr *) &out_address, &len);
    inet_ntop(AF_INET, &out_address.sin_addr, my_ip, sizeof(my_ip));
    my_port = ntohs(out_address.sin_port);
    printf("TCP in socket initialised with address: %s port: %d\n", my_ip, my_port);


    printf("socket bound successfully\n");
}

void *connect_in_tcp_socket(void *pVoid){
    in_tcp_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (in_tcp_socket == -1)
        error_exit("socket creation failure");

    struct sockaddr_in sockaddr_in;
    sockaddr_in.sin_family = AF_INET;
    sockaddr_in.sin_addr.s_addr = htonl(INADDR_ANY);
    sockaddr_in.sin_port = htons((uint16_t) (in_port));


    if (bind(in_tcp_socket, (const struct sockaddr *) &sockaddr_in, sizeof(sockaddr_in)) == 1)
        error_exit("socket bind failure");

    if (listen(in_tcp_socket, 2) == -1)
        error_exit("web socket listen failure");

    char my_ip[16];
    int len ;
    unsigned int my_port;
    bzero(&sockaddr_in, sizeof(sockaddr_in));
    getsockname(in_tcp_socket, (struct sockaddr *) &sockaddr_in, &len);
    inet_ntop(AF_INET, &sockaddr_in.sin_addr, my_ip, sizeof(my_ip));
    my_port = ntohs(sockaddr_in.sin_port);
    printf("TCP in socket initialised with address: %s port: %d\n", my_ip, my_port);
}

void send_message(Message_Type message_type){
    struct sockaddr_in out_address;
    bzero(&out_address, sizeof(out_address));
    out_address.sin_family = AF_INET;
    out_address.sin_addr.s_addr = ip_address;
    out_address.sin_port = htons((uint16_t) out_port);


    switch (message_type){
        case JOIN:
            token.message_type = JOIN;
            if (sendto(udp_socket, &token, sizeof(token), 0, (const struct sockaddr *) &out_address, sizeof(out_address)) !=
                sizeof(token))
                error_exit("message send failure");
            break;
        case SEND:
            if (!have_token)
                return;
            token.message_type = SEND;
            if (sendto(udp_socket, &token, sizeof(token), 0, (const struct sockaddr *) &out_address, sizeof(out_address)) !=
                sizeof(token))
                error_exit("message send failure");
            have_token = 0;
    }
}

void receive_message(){
    struct sockaddr_in address;
    int len = sizeof(address);
    if(recvfrom(udp_socket, &token, sizeof(token), 0, (struct sockaddr *) &address, &len) != sizeof(token))
        error_exit("message receive failure");

    if(token.message_type == JOIN){
        printf("Received join request\n");
        handle_join(address);
        return;
    }
    have_token = 1;
    if(token.dest_ip_address == ip_address && token.dest_port == udp_socket)
        handle_message();

    token.cnt ++;
    printf("Received message: %s, %d\n", token.message, token.cnt);
}

void handle_join(struct sockaddr_in join_address){
    struct sockaddr_in address;
    int len = sizeof(address);
//    if(!have_token){
        if(recvfrom(udp_socket, &token, sizeof(token), 0, (struct sockaddr *) &address, &len) != sizeof(token))
            error_exit("message receive failure");
        have_token = 1;
//    }

    token.dest_ip_address = address.sin_addr.s_addr;
    token.dest_port = address.sin_port;
    token.join_ip_address = join_address.sin_addr.s_addr;
    token.join_port = join_address.sin_port;

}

void handle_message(){
    token.dest_ip_address = 0;
    token.dest_port = 0;
    if(token.join_port != 0){
        ip_address = token.join_ip_address;
        udp_socket = token.join_port;
        token.join_port = 0;
        token.join_ip_address = 0;
    }
}


void error_exit(char *error_message) {
    perror(error_message);
    exit(EXIT_FAILURE);
}

void clean_exit(){

    if(close(out_tcp_socket) == -1){
        perror("closing out socket failure");
    }

    if(close(udp_socket) == -1){
        perror("closing in socket failure");
    }

    printf("\nClosed sockets");
    printf("\nExited\n");
}

