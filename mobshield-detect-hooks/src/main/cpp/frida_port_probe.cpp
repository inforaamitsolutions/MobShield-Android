/*
 * Copyright 2025 MobShield Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "hooks_checks.h"

#include <arpa/inet.h>
#include <cerrno>
#include <cstdio>
#include <cstring>
#include <fcntl.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <unistd.h>

namespace {

bool probe_port(uint16_t port) {
    const int fd = socket(AF_INET, SOCK_STREAM, 0);
    if (fd < 0) {
        return false;
    }

    const int flags = fcntl(fd, F_GETFL, 0);
    if (flags >= 0) {
        fcntl(fd, F_SETFL, flags | O_NONBLOCK);
    }

    struct timeval timeout {};
    timeout.tv_usec = 100000;  // 100ms
    setsockopt(fd, SOL_SOCKET, SO_SNDTIMEO, &timeout, sizeof(timeout));
    setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));

    sockaddr_in address {};
    address.sin_family = AF_INET;
    address.sin_port = htons(port);
    address.sin_addr.s_addr = htonl(INADDR_LOOPBACK);

    const int result = connect(fd, reinterpret_cast<sockaddr*>(&address), sizeof(address));
    const int err = errno;
    close(fd);

    return result == 0 || err == EISCONN;
}

}  // namespace

int mobshield_frida_port_probe(char* evidence, int evidence_len) {
    const uint16_t ports[] = {27042, 27043};
    for (uint16_t port : ports) {
        if (!probe_port(port)) {
            continue;
        }
        if (evidence != nullptr && evidence_len > 0) {
            snprintf(evidence, static_cast<size_t>(evidence_len), "port:%u", port);
        }
        return MOBSHIELD_HOOKS_DETECTED;
    }
    return MOBSHIELD_HOOKS_OK;
}
