package es.upm.dit.dscc.actreplica.watchers;

import es.upm.dit.dscc.actreplica.Main;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Created by Stefano on 22/01/2018.
 */
public class NodeDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted) {

            System.out.println("Node is down ---> " + event.getPath());

            new Main();

//            Runtime rt = Runtime.getRuntime();
//            String[] commands = {"java", "es.upm.dit.dscc.actreplica.Main"};
//            try {
//                rt.exec(commands);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
    }
}
