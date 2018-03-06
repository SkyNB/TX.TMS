package com.lnet.tms.service;

import com.lnet.framework.core.Response;
import com.lnet.model.tms.order.orderEntity.OrderModel;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Administrator on 2016/12/29.
 */
//@Transactional
public class OrderServiceImplTest extends BaseTest {


    @Test
    public void create() throws Exception {
        OrderModel order = new OrderModel();
        order.setVehicleId("1");
        order.setCreateUserId("2");
        order.setOrderNo("20161229");
//        Response r = orderService.create(order);
//        Assert.assertTrue(r.isSuccess());
    }

    @Test
    public void findAll() throws Exception {
//        Response r = orderService.findAll();
//        Assert.assertTrue(r.isSuccess());
    }

}