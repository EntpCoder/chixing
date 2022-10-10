package com.songyang.mq.publisherconfirms;

import com.rabbitmq.client.Channel;

import java.nio.charset.StandardCharsets;

/**
 * @author Yang Song
 * @date 2022/10/9 18:53
 */
public class PublishingMessagesInBatches {
    private static final Long MESSAGE_COUNT = 1000L;
    public static void main(String[] args) throws Exception {
        try(Channel channel = RabbitUtil.getChannel()) {
            String queueName = channel.queueDeclare().getQueue();
            // 开启发布确认
            channel.confirmSelect();
            long begin = System.currentTimeMillis();
            int outstandingMessageCount = 0;
            for(int i = 0;i < MESSAGE_COUNT;i++){
                String msg = "消息"+i;
                channel.basicPublish("",queueName,null,msg.getBytes(StandardCharsets.UTF_8));
                outstandingMessageCount++;
                // 当消息推送完毕时outstandingMessageCount归零
                if(outstandingMessageCount == MESSAGE_COUNT){
                    channel.waitForConfirms(5000);
                    outstandingMessageCount = 0;
                }
            }
            // 确保没有剩余消息
            if(outstandingMessageCount > 0){
                channel.waitForConfirms(5000);
            }
            long end = System.currentTimeMillis();
            System.out.println("发布"+MESSAGE_COUNT+"个单独确认消息，耗时"+(end-begin)+"ms");
        }
    }
}
