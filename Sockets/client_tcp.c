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

#define MAX_CLIENTS 4

typedef enum Message_Type {
    JOIN = 0,
    JOIN_ACK = 1,
    FREE = 2,
    FULL = 3,
    RETURN = 4,
} Message_Type;

typedef struct Token {
    char message[64];
    char dest_id[64];
    char source_id[64];
    in_addr_t join_ip_address;
    in_port_t join_port;
    int cnt;
    int ttl;
    Message_Type message_type;
} Token;

Token token;
char *user_id;
in_addr_t next_ip_address;
int epoll_fd;
int out_port;
int in_port;
int have_token;
int tcp_in_socket;
int tcp_out_socket;
int is_pending_message;
char pending_message[64];
char pending_id[64];

void forward_message();

void receive_message(int socket);

void clean_exit();

void error_exit(char *error_message);

void parse_args(int argc, char **argv);

void initialise_in_socket();

void initialise_out_socket();

void initialise_epoll_monitor();

void handle_join(struct sockaddr_in addr);

void handle_join_ack(struct sockaddr_in addr);

void handle_free();

void handle_full();

void handle_return();

void join_token_ring();

void handle_message();

void tcp_send();

struct sockaddr_in tcp_receive(int socket);

void register_socket(int socket);

void send_message(int sig);

void remove_socket(int socket);


int main(int argc, char **argv) {
//    atexit(clean_exit);
    signal(SIGINT, clean_exit);
    parse_args(argc, argv);
    initialise_in_socket();
    initialise_epoll_monitor();
    initialise_out_socket();

    join_token_ring();
    signal(SIGTSTP, send_message);

    token.message_type = FREE;
    struct epoll_event event;
    forward_message();
    while(1){
        if(epoll_wait(epoll_fd, &event, 1, -1) == -1)
            error_exit("epoll wait failure");

        if(event.data.fd < 0){
            register_socket(-event.data.fd);
        }else{
            receive_message(event.data.fd);
        }
    }

}



void send_message(int sig) {
    signal(SIGTSTP, send_message);

    if (is_pending_message) {
        printf("\nThere is already waiting message to send\n");
        return;
    }
    is_pending_message = 1;

    printf("\nEnter target id:\n");

    char *buffer1;
    size_t bufsize1 = 64;
    size_t characters1;
    buffer1 = (char *) malloc(bufsize1 * sizeof(char));
    characters1 = (size_t) getline(&buffer1, &bufsize1, stdin);
    buffer1[characters1 - 1] = 0;
    printf("\nEnter message:\n");

    char *buffer2;
    size_t bufsize2 = 64;
    buffer2 = (char *) malloc(bufsize2 * sizeof(char));
    getline(&buffer2, &bufsize2, stdin);

    memset(pending_id, 0, 64);
    memset(pending_message, 0, 64);

    strcpy(pending_id, buffer1);
    strcpy(pending_message, buffer2);

    token.ttl = 3;
}

void parse_args(int argc, char **argv) {
    if (argc != 6)
        error_exit("invalid arguments");

    user_id = argv[1];

    inet_aton(argv[2], (struct in_addr *) &next_ip_address);
    printf("%d\n", next_ip_address);
    if (next_ip_address == INADDR_NONE)
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

    ptr = argv[5];
    have_token = strtol(ptr, &ptr, 10) == 0 ? 0 : 1;
    if (have_token) {
        token.cnt = 0;
        token.join_port = 0;
        token.join_ip_address = 0;
    }
}

void initialise_in_socket() {

    tcp_in_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (tcp_in_socket == -1)
        error_exit("ip socket creation failure");

    int true = 1;
    setsockopt(tcp_in_socket, SOL_SOCKET, SO_REUSEADDR, &true, sizeof(int));

    struct sockaddr_in in_address;
    bzero(&in_address, sizeof(in_address));
    in_address.sin_family = AF_INET;
    in_address.sin_addr.s_addr = INADDR_ANY;
    in_address.sin_port = htons((uint16_t) in_port);

    if (bind(tcp_in_socket, (const struct sockaddr *) &in_address, sizeof(in_address)) == -1)
        error_exit("in socket bind failure");

    if (listen(tcp_in_socket, MAX_CLIENTS) == -1)
        error_exit("in socket listen failure");

    fprintf(stderr, "In socket initialised\n");
}

void initialise_out_socket() {

    if (tcp_out_socket != 0){
        if (shutdown(tcp_out_socket, SHUT_RDWR) == -1)
            error_exit("out socket shutdown failure");
        if (close(tcp_out_socket) == -1)
            error_exit("out socket close failure");
    }

    tcp_out_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (tcp_out_socket == -1)
        error_exit("out socket creation failure");
    int true = 1;
    setsockopt(tcp_out_socket, SOL_SOCKET, SO_REUSEADDR, &true, sizeof(int));

    struct sockaddr_in address;
    bzero(&address, sizeof(address));
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = next_ip_address;
    address.sin_port = htons((uint16_t) out_port);

    if (connect(tcp_out_socket, (const struct sockaddr *) &address, sizeof(address)) == -1)
        error_exit("out socket connect failure");

    fprintf(stderr, "out socket initialised\n");
}

void initialise_epoll_monitor(){
    epoll_fd= epoll_create1(0);
    if (epoll_fd == -1)
        error_exit("epoll_fd monitor creation failure");

    struct epoll_event epoll_event1;
    epoll_event1.events = EPOLLIN | EPOLLPRI;
    epoll_event1.data.fd = -tcp_in_socket;

    if (epoll_ctl(epoll_fd, EPOLL_CTL_ADD, tcp_in_socket, &epoll_event1) == -1)
        error_exit("adding socket under epoll monitoring failure");

}

void register_socket(int socket){
    int new_client = accept(socket, NULL, NULL);
    if (new_client == -1)
        error_exit("client registration failure");

    struct epoll_event epoll_event1;
    epoll_event1.events = EPOLLIN | EPOLLPRI;
    epoll_event1.data.fd = new_client;

    if(epoll_ctl(epoll_fd, EPOLL_CTL_ADD, new_client, &epoll_event1) == -1)
        error_exit("adding new client under epoll monitoring failure");

}

void join_token_ring() {
    token.message_type = JOIN;
    token.join_port = (in_port_t) in_port;
    tcp_send();
}

void forward_message() {
    if (!have_token) {
        fprintf(stderr, "Do not have token\n");
        return;
    }

    tcp_send();
    have_token = 0;
}

void receive_message(int socket) {
    struct sockaddr_in address = tcp_receive(socket);
    if (address.sin_port == 0)
        return;

    switch (token.message_type) {
        case JOIN:
            handle_join(address);
            break;
        case JOIN_ACK:
            handle_join_ack(address);
            break;
        case FREE:
            handle_free();
            break;
        case FULL:
            handle_full();
            break;
        case RETURN:
            handle_return();
            break;
    }
    forward_message();
    usleep(500000);

}

void handle_join(struct sockaddr_in addr) {
    printf("Received join request from %s %d\n", inet_ntoa(addr.sin_addr), ntohs(addr.sin_port));

    in_port_t new_out_port = token.join_port;

    token.message_type = JOIN_ACK;
    token.join_ip_address = next_ip_address;
    token.join_port = (in_port_t) out_port;

    next_ip_address = addr.sin_addr.s_addr;
    out_port = new_out_port;
    initialise_out_socket();
    tcp_send();
}

void handle_join_ack(struct sockaddr_in addr) {
    printf("Received join_ack from %s %d\n", inet_ntoa(addr.sin_addr), ntohs(addr.sin_port));

    next_ip_address = token.join_ip_address;
    out_port = token.join_port;
    initialise_out_socket();
}

void handle_free() {
    have_token = 1;
    if (is_pending_message) {
        is_pending_message = 0;
        memset(token.dest_id, 0, 64);
        memset(token.source_id, 0, 64);
        memset(token.message, 0, 64);
        strcpy(token.dest_id, pending_id);
        strcpy(token.source_id, user_id);
        strcpy(token.message, pending_message);
        memset(pending_id, 0, 64);
        memset(pending_message, 0, 64);
        token.message_type = FULL;
    }
    token.cnt++;
}

void handle_full() {
    have_token = 1;
    if (strcmp(token.dest_id, user_id) == 0) {
        handle_message();
    }
    if (strcmp(user_id, token.source_id) == 0) {
        token.ttl--;
        if (token.ttl <= 0) {
            fprintf(stderr, "\nMessage to %s deleted due to TTL\n", token.dest_id);
            memset(token.dest_id, 0, 64);
            memset(token.message, 0, 64);
            memset(token.source_id, 0, 64);
            token.message_type = FREE;
        }
    }

    token.cnt++;
}

void handle_return() {
    have_token = 1;
    if (strcmp(user_id, token.source_id) == 0) {
        fprintf(stderr, "\nMessage delivered successfully\n");
        memset(token.source_id, 0, 64);
        token.cnt++;
        token.message_type = FREE;
    }
}

void handle_message() {
    fprintf(stderr, "\n\nThis is message for me: %s\n", token.message);
    memset(token.dest_id, 0, 64);
    memset(token.message, 0, 64);
    token.message_type = RETURN;
}

void error_exit(char *error_message) {
    perror(error_message);
    exit(EXIT_FAILURE);
}

void tcp_send() {

    if (write(tcp_out_socket, &token, sizeof(token)) != sizeof(token)) {
        error_exit("message send failure");
    }
    struct sockaddr_in in_address;
    bzero(&in_address, sizeof(in_address));
    in_address.sin_family = AF_INET;
    in_address.sin_addr.s_addr = next_ip_address;
    in_address.sin_port = htons((uint16_t) in_port);
    fprintf(stderr, "\nSend message to %s %d\n", inet_ntoa(in_address.sin_addr), out_port);
}

struct sockaddr_in tcp_receive(int socket) {
    struct sockaddr_in address;
    int len = sizeof(address);
    if (read(socket, &token, sizeof(token)) != sizeof(token)){
        remove_socket(socket);
        address.sin_port = 0;
        return address;
    }

    getpeername(socket, (struct sockaddr *) &address, (socklen_t *) &len);

    if (token.message_type == FREE || token.message_type == FULL || token.message_type == RETURN)
        fprintf(stderr, "\nReceived token: %d from %s %d\n", token.cnt, inet_ntoa(address.sin_addr),
                ntohs(address.sin_port));
    return address;
}

void clean_exit() {


    if(close(epoll_fd) == -1){
        perror("closing epoll failure");
    }

    if (shutdown(tcp_out_socket, SHUT_RDWR) == -1) {
        perror("shutdown out socket failure");
    }

    if (close(tcp_out_socket) == -1) {
        perror("closing in socket failure");
    }

    if (close(tcp_in_socket) == -1) {
        perror("closing in socket failure");
    }


    printf("\nClosed sockets");
    printf("\nExited\n");
    exit(EXIT_SUCCESS);
}

void remove_socket(int socket){
    if(epoll_ctl(epoll_fd, EPOLL_CTL_DEL, socket, NULL) == -1)
        error_exit("removing socket from epoll monitor failure");
    if(shutdown(socket, SHUT_RDWR) == -1)
        error_exit("socket shutdown failure");
    if(close(socket) == -1)
        error_exit("socket close failure");
}