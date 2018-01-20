package es.upm.dit.dscc.actreplica.watchers;

import es.upm.dit.dscc.actreplica.Bank;
import es.upm.dit.dscc.actreplica.OperationBank;
import es.upm.dit.dscc.actreplica.OperationEnum;
import es.upm.dit.dscc.actreplica.node_managers.ElectionManager;
import es.upm.dit.dscc.actreplica.node_managers.OperationsManager;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Stefano on 20/01/2018.
 */
public class OperationWatcher implements Watcher {

    private ZooKeeper zk;
    private Bank bank;
    private String nodename;

    public OperationWatcher(ZooKeeper zk, String nodename, Bank bankInstance){
        this.zk = zk;
        this.bank = bankInstance;
        this.nodename = nodename;
    }
    @Override
    public void process(WatchedEvent event) {

        System.out.println("OperationWatcher: " + this.nodename);

        if (event.getPath().equals(this.nodename)) {
            List<String> operations = null;
            try {
                operations = zk.getChildren(this.nodename, false);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (Iterator iterator = operations.iterator(); iterator.hasNext();) {
                String operation_id = (String) iterator.next();
                String nodePath = this.nodename + "/" + operation_id;
                byte[] data = null;
                try {
                    data = zk.getData(nodePath, false, null);
                    Stat stat = zk.exists(nodePath, false);
                    zk.delete(nodePath, stat.getVersion());
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    OperationBank operation = null;
                    try {
                        operation = OperationBank.byteToObj(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    System.out.println(operation);

                    bank.handleReceiverMsg(operation);
                    if (this.bank.getIsLeader()) this.bank.sendMessages.forwardOperationToFollowers(operation);
                }
            }
        }
        try {
            zk.getChildren(this.nodename, this);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
