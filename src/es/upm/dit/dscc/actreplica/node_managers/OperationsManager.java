package es.upm.dit.dscc.actreplica.node_managers;

import es.upm.dit.dscc.actreplica.Bank;
import es.upm.dit.dscc.actreplica.utils.NodeUtils;
import es.upm.dit.dscc.actreplica.watchers.OperationsWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by Stefano on 20/01/2018.
 */
public class OperationsManager {

    private ZooKeeper zk;

    public static String root = "/operations";
    public static String prefix = "node-";


    public OperationsManager(ZooKeeper zkInstance){
        this.zk = zkInstance;
    }

    public String createOperationsNode() throws KeeperException, InterruptedException {

        NodeUtils.znodeExistsOrCreate(zk, root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        return NodeUtils.znodeExistsOrCreate(zk, root + "/" + prefix, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);

    }

    public void listenForOperationUpdates(Bank bankInstance, String nodeName){
        OperationsWatcher operationsWatcher = new OperationsWatcher(this.zk, nodeName, bankInstance);
        try {
            this.zk.getChildren(nodeName, operationsWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
