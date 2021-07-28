package com.valor.mercury.manager.service;

import com.valor.mercury.manager.config.ConstantConfig;
import com.valor.mercury.manager.config.MercuryConstants;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Gavin
 * 2020/8/4 10:45
 * 高可用模块
 */
@Service
public class HAService {

    private ZooKeeper zkClient;
    private NodeStatus nodeStatus = NodeStatus.LOOKING;
    private final LogAlarmService logService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean connected = false;
    private List<String> managerNodes = new ArrayList<>();
    private String finalNodePath;

    @Autowired
    public HAService(LogAlarmService logService) {
        this.logService = logService;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public List<String> getManagerNodes() {
        return managerNodes;
    }

    @PostConstruct
    private void NodeElection() {
        try {
            zkClient = new ZooKeeper(ConstantConfig.zkClusterPath(), 10000, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connected = true;
                    nodeStatus = NodeStatus.LOOKING;
                } else {
                    logger.error("Zookeeper Connect Fail!");
                    logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                            "zookeeper", "Zookeeper Connect Fail!");
                    System.exit(1);
                }
            });
            int i = 1;
            while (!connected) {
                if (i == 100)
                    break;
                Thread.sleep(300);
                i++;
            }
            if (connected) {
                //创建父节点
                if (zkClient.exists(ConstantConfig.zkFilePath(), false) == null) {
                    zkClient.create(ConstantConfig.zkFilePath(), "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
                //创建子节点
                finalNodePath = zkClient.create(ConstantConfig.zkFilePath() + "/" + ConstantConfig.localIP() + "#", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                //开始选举出主节点
                electLeader();
            } else {
                //连接超时
                logger.error("Zookeeper Connect Timeout!");
                logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                        "zookeeper", "Zookeeper Connect Timeout!");
                System.exit(1);
            }
        } catch (Exception e) {
            logger.error("connect zookeeper error :{}", e);
            logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(), "zookeeper",
                    "connect zookeeper error :" + e.getMessage());
            System.exit(1);
        }
    }

    private void electLeader() {
        try {
            List<String> childrenList = zkClient.getChildren(ConstantConfig.zkFilePath(), (event) -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    //节点发生变化，重新开始选举
                    logger.warn("Node Changed,Start elect");
                    logService.addLog(MercuryConstants.LOG_LEVEL_WARN, this.getClass().getName(),
                            "zookeeper", "Node Changed,Start elect");
                    electLeader();
                }
            });
            childrenList.sort((v1, v2) -> {
                int num1 = Integer.parseInt(v1.split("#")[1]);
                int num2 = Integer.parseInt(v2.split("#")[1]);
                if (num1 > num2)
                    return 1;
                else if (num1 < num2)
                    return -1;
                else
                    return 0;
            });
            if (managerNodes.size() > childrenList.size()) {
                for (String child : childrenList) {
                    String childNode = child.split("#")[0];
                    if (managerNodes.contains(childNode))
                        managerNodes.remove(childNode);
                }
                logger.error("Node Delete:{}", managerNodes.toString());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(),
                        "zookeeper", "Node Delete:" + managerNodes.toString());
            }
            //得到当前mercury-manager的分布情况
            managerNodes.clear();
            childrenList.forEach(v -> {
                String ip = v.split("#")[0];
                managerNodes.add(ip);
            });
            logger.info("Zookeeper electLeader,Node Status:{}", managerNodes.toString());
            if (finalNodePath.equals(ConstantConfig.zkFilePath() + "/" + childrenList.get(0))) {
                logger.info("Master Node :{}", finalNodePath);
                nodeStatus = NodeStatus.LEADER;
            } else
                nodeStatus = NodeStatus.FOLLOWER;
        } catch (Exception e) {
            logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "zookeeper",
                    "electLeader error :" + e.getMessage());
            logger.error("electLeader error:{}", e);
        }
    }

    public enum NodeStatus {
        LOOKING, // 选举中
        LEADER, // 选举完毕，当前节点为leader
        FOLLOWER // 选举完毕，当前节点为follower
    }
}

