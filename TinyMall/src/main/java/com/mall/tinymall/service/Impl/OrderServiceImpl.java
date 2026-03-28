package com.mall.tinymall.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.tinymall.entity.dto.AddOrderDto;
import com.mall.tinymall.entity.dto.ManageOrderDto;
import com.mall.tinymall.entity.dto.PayDto;
import com.mall.tinymall.entity.pojo.Goods;
import com.mall.tinymall.entity.pojo.Order;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.vo.MallListOrderVo;
import com.mall.tinymall.entity.vo.StoreListOrderVo;
import com.mall.tinymall.entity.vo.UserOrderVo;
import com.mall.tinymall.mapper.GoodsMapper;
import com.mall.tinymall.mapper.OrderMapper;
import com.mall.tinymall.mapper.StoreMapper;
import com.mall.tinymall.mapper.UserMapper;
import com.mall.tinymall.service.OrderService;
import com.mall.tinymall.util.MsgPublisher;
import com.mall.tinymall.util.OrderStateENum;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * @date 2026/1/21 9:09
 * @description TODO: 订单表管理
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public Result userListOrder(Integer userId) {
        String userName = userMapper.getUserName(userId);
        LambdaQueryWrapper<Order> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Order::getUserId,userId);
        List<Order> orderList = orderMapper.selectList(lambdaQueryWrapper);
        List<UserOrderVo> userOrderVoList = new ArrayList<>();
        for (Order order : orderList) {
            UserOrderVo userOrderVo = new UserOrderVo();
            userOrderVo.setOrderId(order.getOrderId());
            //TODO优化 此处减少数据库查询
            userOrderVo.setUserName(userName);
            userOrderVo.setStoreId(order.getStoreId());
            userOrderVo.setStoreName(storeMapper.getStoreName(order.getStoreId()));
            userOrderVo.setGoodsId(order.getGoodsId());
            userOrderVo.setTime(order.getTime());
            userOrderVo.setState(order.getState());
            userOrderVo.setCount(order.getCount());
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            userOrderVo.setPicture(goods.getPicture());
            userOrderVo.setPrice(goods.getPrice());
            userOrderVo.setAddress(order.getAddress());
            userOrderVoList.add(userOrderVo);
        }
        return Result.success(userOrderVoList);
    }

    //用户直接购买逻辑
    @Override
    @Transactional
    public Result addOrder(AddOrderDto dto) throws Exception {
        Order order = createOrder(dto);
        if(order==null) return Result.error("订单提交失败");
        return Result.success(order);
    }

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MsgPublisher msgPublisher;

    //创建订单
    public Order createOrder(AddOrderDto dto) throws Exception {
        Goods good = goodsMapper.selectGoodsById(dto.getGoodsId());
        if(good.getCount() < dto.getCount()) return null;
        good.setCount(good.getCount()-dto.getCount());
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setStoreId(good.getStoreID());
        order.setGoodsId(dto.getGoodsId());
        order.setCount(dto.getCount());
        order.setPrice(dto.getCount()*good.getPrice());
        order.setTime(new Date());
        order.setState(OrderStateENum.ORDER_CREATE.getCode());
        order.setAddress(dto.getAddress());
        goodsMapper.updateById(good);
        this.save(order);
        Integer orderId = order.getOrderId();
        redisTemplate.opsForValue().set("pay_"+orderId.toString(), objectMapper.writeValueAsString(order));
        msgPublisher.delayDeleteTimeOurOrder("pay_"+ orderId);
        return order;
    }

    //商家查询所有订单
    @Override
    public Result mallListOrder() {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("State", OrderStateENum.ORDER_CREATE);
        List<Order> orderList = orderMapper.selectList(queryWrapper);
        List<MallListOrderVo> vos = new ArrayList<>();
        for (Order order : orderList) {
            MallListOrderVo vo = new MallListOrderVo();
            BeanUtils.copyProperties(order, vo);
            vo.setUserName(userMapper.getUserName(order.getUserId()));
            vo.setStoreName(storeMapper.getStoreName(order.getStoreId()));
            vo.setGoodsName(goodsMapper.getGoodsName(order.getGoodsId()));
            vos.add(vo);
        }
        return Result.success(vos);
    }

    //用户删除订单
    @Override
    public Result userDelOrder(Integer orderId) {
        if(redisTemplate.hasKey("pay_"+orderId)) redisTemplate.delete("pay_"+orderId);
        delOrder(orderId);
        return Result.success();
    }

    @Async
    public void delOrder(Integer orderId){
        orderMapper.deleteById(orderId);
    }

    //用户支付订单
    @Override
    public Result payOrd(PayDto dto) {
        try {
            if(!redisTemplate.hasKey("pay_"+dto.getOrderId())) return Result.error("无效订单");
            String json = Objects.requireNonNull(redisTemplate.opsForValue().get("pay_" + dto.getOrderId())).toString();
            Order order = objectMapper.readValue(json, Order.class);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    // 支付金额（单位：最小货币单位，如 100 分 = 1 美元）
                    .setAmount((long) order.getPrice()*100)
                    // 货币类型
                    .setCurrency("cny")
                    // 支付令牌（前端生成，无敏感卡信息）
                    .setPaymentMethod(dto.getPaymentToken())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    // 关键配置：禁止所有需要跳转的支付方式
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    // 附加信息（订单号）
                    .putMetadata("orderId", String.valueOf(dto.getOrderId()))
                    // 新增：测试环境强制确认支付（适配 4242 测试卡）
                    .setConfirm(true)
                    .build();
            // 调用 Stripe API 创建支付意向
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            if(paymentIntent.getStatus().equals("succeeded")){
                redisTemplate.delete("pay_"+dto.getOrderId());
                sendPaidEmail(order, new Date());
                paidOrd(dto.getOrderId());
                return Result.success();
            }else return Result.error("支付失败，" + paymentIntent.getLastPaymentError().getMessage());
        }catch (Exception e){
            log.error("e: ", e);
            return Result.error("系统异常");
        }
    }


    // 发件人邮箱（从配置文件读取）
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Autowired
    private JavaMailSender javaMailSender;

    @Async
    public void sendPaidEmail(Order order, Date time){
        try{
            String email = userMapper.getUserEmail(order.getUserId());
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【TinyShop】订单支付成功通知");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String paidTime = sdf.format(time);
            message.setText("尊敬的用户您好：\n\n" +
                    "恭喜您！您于 " + paidTime + " 成功完成订单支付。\n" +
                    "订单编号：" + order.getOrderId().toString() + "\n\n" +
                    "我们已收到您的付款，后续商品发货、物流等状态变化会及时同步，敬请留意。\n\n" +
                    "感谢您的选购，祝您购物愉快！\n\n" +
                    "TinyShop 运营团队");
            javaMailSender.send(message);
        }catch (MailException e){
            log.warn("订单 {} 邮件发送失败", order.getOrderId());
        }
    }

    @Async
    public void paidOrd(Integer orderId){
        UpdateWrapper<Order> wrapper = new UpdateWrapper<>();
        wrapper.eq("OrderID", orderId);
        wrapper.set("State", OrderStateENum.ORDER_PAID.getCode());
        orderMapper.update(wrapper);
    }

    @Override
    public Result mallManageOrder(ManageOrderDto dto) {
        //TODO
        if(dto.isRefuse()) {
            UpdateWrapper<Order> wrapper = new UpdateWrapper<>();
            wrapper.set("State", OrderStateENum.ORDER_REFUSE.getCode());
            wrapper.eq("OrderID", dto.getOrderId());
            orderMapper.update(wrapper);
            rollBackCount(dto.getOrderId());
            sendOrderRefuse(dto.getOrderId(), dto.getMsg());
        } else {
            UpdateWrapper<Order> wrapper = new UpdateWrapper<>();
            wrapper.set("State", OrderStateENum.ORDER_ACCEPT.getCode());
            wrapper.eq("OrderID", dto.getOrderId());
            orderMapper.update(wrapper);
            sendOrderAccept(dto.getOrderId());
        }
        return Result.success();
    }


    //拒绝订单后返还库存
    @Async
    @Transactional
    public void rollBackCount(Integer orderId){
        Order order = orderMapper.selectById(orderId);
        Integer count = goodsMapper.selectGoodsCount(order.getGoodsId());
        goodsMapper.updateGoodsCount(order.getGoodsId(), count+order.getCount());
    }

    //发送拒绝订单邮件给客户
    public void sendOrderRefuse(Integer orderId, String msg){
        try {
            Order order = orderMapper.selectById(orderId);
            Date time = order.getTime();
            String userEmail = userMapper.getUserEmail(order.getUserId());
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("【TinyShop】订单处理通知 - 订单已被拒绝");
            String refuseMsg = (msg == null || msg.trim().isEmpty()) ? "（暂无具体原因）" : msg.trim();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String commitTime = sdf.format(time);
            message.setText("尊敬的用户您好：\n\n" +
                    "您于 " + commitTime + " 提交的订单（订单号：" + orderId + "）已被商家拒绝处理。\n" +
                    "拒绝原因：" + refuseMsg + "\n\n" +
                    "该订单对应的支付金额已原路退回至您的支付账户，请注意查收。\n" +
                    "若有任何疑问，可联系平台客服咨询。\n\n" +
                    "TinyShop 运营团队");
            javaMailSender.send(message);
        } catch (MailException e) {
            log.warn("订单 {} 拒绝邮件发送失败", orderId);
        }
    }

    //发送派送订单邮件给供应商
    public void sendOrderAccept(Integer orderId){
        try {
            Order order = orderMapper.selectById(orderId);
            String storeEmail = storeMapper.getStoreEmail(order.getStoreId());
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(storeEmail);
            message.setSubject("【TinyShop】新订单待配送提醒");
            message.setText("尊敬的商家您好：\n\n" +
                    "有新的订单需要您处理，具体信息如下：\n" +
                    "用户ID：" + order.getUserId() + "\n" +
                    "商品ID：" + order.getGoodsId() + "\n" +
                    "购买数量：" + order.getCount() + " 件\n" +
                    "配送地址：" + order.getAddress() + "\n\n" +
                    "烦请您尽快安排该订单的配送事宜，感谢您的配合！\n\n" +
                    "TinyShop 运营团队");
            javaMailSender.send(message);
        } catch (MailException e) {
            log.info("邮件发送失败");
        }
    }


    @Override
    public Result storeListOrders(Integer storeId) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("StoreId", storeId);
        List<Order> orderList = orderMapper.selectList(queryWrapper);
        List<StoreListOrderVo> vos = new ArrayList<>();
        for (Order order : orderList) {
            if(order.getState()==OrderStateENum.ORDER_ACCEPT.getCode()||order.getState()==OrderStateENum.ORDER_DELIVERY.getCode()) {
                String goodsName = goodsMapper.getGoodsName(order.getGoodsId());
                String storeName = storeMapper.getStoreName(order.getStoreId());
                StoreListOrderVo vo = new StoreListOrderVo();
                BeanUtils.copyProperties(order, vo);
                vo.setGoodsName(goodsName);
                vo.setStoreName(storeName);
                vos.add(vo);
            }
        }
        return Result.success(vos);
    }

    @Override
    public Result delivery(Integer orderId) {
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getOrderId, orderId);
        wrapper.set(Order::getState, OrderStateENum.ORDER_DELIVERY.getCode());
        update(wrapper);
        sendDeliveryEmail(orderId);
        return Result.success();
    }

    @Async
    public void sendDeliveryEmail(Integer orderId){
        Integer userId = orderMapper.getUserId(orderId);
        String userEmail = userMapper.getUserEmail(userId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(userEmail);
        message.setSubject("【TinyShop】您的订单已完成发货");
        message.setText("尊敬的用户您好：\n\n您在TinyShop的订单（订单号：" + orderId + "）已完成发货，烦请留意物流动向，祝您购物愉快！\n\nTinyShop 运营团队");
        javaMailSender.send(message);
    }


    @Override
    public Result urge(Integer orderId, Integer storeId) {
        try {
            String email = storeMapper.getStoreEmail(storeId);
            long time = orderMapper.getOrderTime(orderId).getTime();
            long diff = System.currentTimeMillis()-time;
            double diffHours = Math.round((diff / 3600000.0) * 10) / 10.0;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【TinyShop】催促信息");
            message.setText("尊敬的商家您好：\n\n" +
                    "您有一笔订单尚未发货，具体信息如下：\n" +
                    "订单编号：" + orderId + "\n" +
                    "订单已超时：" + diffHours + "小时\n\n" +
                    "为提升消费者购物体验，恳请您尽快处理该订单的发货事宜。\n\n" +
                    "感谢您的配合与支持！\n\n" +
                    "TinyShop 运营团队");
            javaMailSender.send(message);
            return Result.success();
        } catch (MailException e) {
            return Result.error("催促邮件发送失败");
        }
    }

    @Override
    public Result checkOrder(Integer orderId) {
        UpdateWrapper<Order> wrapper = new UpdateWrapper<>();
        wrapper.eq("OrderID", orderId);
        wrapper.set("State", OrderStateENum.OREDER_CHECK.getCode());
        orderMapper.update(wrapper);
        sendCheckEmail(orderId);
        return Result.success();
    }

    @Async
    public void sendCheckEmail(Integer orderId){
        Order order = getById(orderId);
        String email = userMapper.getUserEmail(order.getUserId());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("【TinyShop】订单收货完成通知");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String orderTime = sdf.format(order.getTime());
        String receiveTime = sdf.format(new Date());

        message.setText("尊敬的用户您好：\n\n" +
                "您于 " + orderTime + " 提交的订单（订单号：" + orderId + "），已于 " + receiveTime + " 完成收货。\n\n" +
                "本次订单交易已正式结束，感谢您的选购，期待您再次光临TinyShop！\n\n" +
                "TinyShop 运营团队");
        javaMailSender.send(message);
    }

}
