package com.chat.config;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author L
 */
public class ServiceDiscovery {


    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddr;

    private List<String> serviceAddressList;

    public ServiceDiscovery(String registryAddr) {
        this.registryAddr = registryAddr;
        ZooKeeper zooKeeper = connectServer();
        if (zooKeeper!=null){
            watchNode(zooKeeper);
        }
    }
    /**
     * 通过服务发现，获取服务提供方的地址
     * @return String
     */
    public String discover(){
        String data = null;
        int size = serviceAddressList.size();
        if (size>0){
            if (size ==1){
                //单节点
                data = serviceAddressList.get(0);
            }else {
                //使用随机分配法。简单的负载均衡法
                data = serviceAddressList.get(ThreadLocalRandom.current().nextInt(size));
            }
        }
        return data;
    }

    /**
     * 获取服务地址列表
     * @param zooKeeper
     */
    private void watchNode(ZooKeeper zooKeeper) {
        try {
            List<String> nodeList = zooKeeper.getChildren("/route", new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zooKeeper);
                    }
                }
            });
            this.serviceAddressList = nodeList;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接 zookeeper
     * @return
     */
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddr, 20000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return zk;
    }
}
