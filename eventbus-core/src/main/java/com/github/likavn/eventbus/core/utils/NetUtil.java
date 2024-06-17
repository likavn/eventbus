/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.core.utils;

import com.github.likavn.eventbus.core.exception.EventBusException;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * net util
 *
 * @author likavn
 * @date 2024/3/30
 **/
@UtilityClass
public class NetUtil {
    private InetAddress inetAddress;

    /**
     * 获取本地ip
     *
     * @return 本地ip
     */
    public InetAddress getHostAddress() {
        if (null != inetAddress) {
            return inetAddress;
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            flag:
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress adder = addresses.nextElement();
                    if (!adder.isLoopbackAddress() && !(adder.getHostAddress() + "").contains(":")) {
                        inetAddress = adder;
                        break flag;
                    }
                }
            }
        } catch (SocketException e) {
            throw new EventBusException(e);
        }
        Assert.notNull(inetAddress, "获取本地ip失败!");
        return inetAddress;
    }

    /**
     * 获取本地hostName
     *
     * @return 本地hostName
     */
    public String getHostAddr() {
        return getHostAddress().getHostAddress();
    }
}
