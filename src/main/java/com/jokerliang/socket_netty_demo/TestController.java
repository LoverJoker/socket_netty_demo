package com.jokerliang.socket_netty_demo;

import com.google.gson.Gson;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-05-27 12:27
 */
@RestController
public class TestController {

    @Autowired
    SocketIOService socketIOService;

    @GetMapping("/test")
    public Boolean test (String clientId) {
        Gson gson = new Gson();

        String orderCode = UUID.randomUUID().toString();
        ShipmentEntity shipmentEntity = new ShipmentEntity(orderCode, 1);

        String shipmentJson = gson.toJson(shipmentEntity);

        PushMessage pushMessage =
                new PushMessage(PushMessage.EVENT_SHIPMENT, shipmentJson);

        Boolean aBoolean = socketIOService.pushMessageToUser(clientId, pushMessage);
        System.out.println("当前是否成功" + aBoolean);


        return aBoolean;
    }
}
