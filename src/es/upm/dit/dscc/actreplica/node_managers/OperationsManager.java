package es.upm.dit.dscc.actreplica.node_managers;

import es.upm.dit.dscc.actreplica.Bank;
import es.upm.dit.dscc.actreplica.utils.NodeUtils;
import es.upm.dit.dscc.actreplica.watchers.OperationWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by Stefano on 20/01/2018.
 */
public class OperationsManager {

    private ZooKeeper zk;

    public static String rootOperations = "/operations";
    public static String prefixOperations = "op-";


    public OperationsManager(ZooKeeper zkInstance){
        this.zk = zkInstance;
    }

    public String createOperationsNode() throws KeeperException, InterruptedException {

        NodeUtils.znodeExistsOrCreate(zk, rootOperations, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        return NodeUtils.znodeExistsOrCreate(zk, rootOperations + "/" + prefixOperations, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);

    }

    public void listenForOperationUpdates(Bank bankInstance, String nodeName){
        OperationWatcher operationWatcher = new OperationWatcher(this.zk, nodeName, bankInstance);
        try {
            this.zk.getChildren(nodeName, operationWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
