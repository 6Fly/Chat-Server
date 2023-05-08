package com.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author L
 */
@Slf4j
public class ServiceRegistry {

    private static final String ROOT_NODE = "/route";

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;


    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String childNode){
        if (childNode!=null){
            ZooKeeper zooKeeper = connectServer();
            if (zooKeeper!=null){
                try {
                    if (zooKeeper.exists(ROOT_NODE,false)==null){
                        createRootNode(zooKeeper);
                    }
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
                createNode(zooKeeper,childNode);
            }
        }
    }

    /**
     * 创建子节点
     * @param zooKeeper
     * @param childNode
     */
    private void createNode(ZooKeeper zooKeeper, String childNode) {
        String node = ROOT_NODE +"/"+ childNode;
        try {
            if (zooKeeper.exists(node,false)==null){
                byte[] bytes = node.getBytes();
                zooKeeper.create(node,bytes,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建节点异常：{}",e.getMessage());
        }
    }

    /**
     * 创建顶级节点
     * @param zooKeeper -zooKeeper
     */
    private void createRootNode(ZooKeeper zooKeeper) {
        try {
            if (zooKeeper.exists(ROOT_NODE,false) ==null) {
                String path = zooKeeper.create(ROOT_NODE, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("创建根节点{}",path);
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建根节点异常：{}",e.getMessage());
        }

    }

    /**
     * 连接 zookeeper 服务器
     * @return ZooKeeper
     */
    private ZooKeeper connectServer() {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(registryAddress, 20000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState()==Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }
}
