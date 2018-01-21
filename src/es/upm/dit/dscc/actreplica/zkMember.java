package es.upm.dit.dscc.actreplica;

import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class zkMember implements Watcher{

	public static String rootMembers = "/members";
	private static String aMember = "/member-";
	private String myId;

	private ZooKeeper zk;

	private Watcher  watcherMember = new Watcher() {
		public void process(WatchedEvent event) {
			try {
				List<String> list = zk.getChildren(rootMembers,  watcherMember);
				printListMembers(list);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	};
	
	public zkMember(ZooKeeper zk) {

		this.zk = zk;

		if (zk != null) {
			try {
				Stat s = zk.exists(rootMembers, watcherMember);
				if (s == null) {
					zk.create(rootMembers, new byte[0],
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}

				myId = zk.create(rootMembers + aMember, new byte[0],
						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
				myId = myId.replace(rootMembers + "/", "");

				zk.getChildren(rootMembers, watcherMember, s);
			} catch (KeeperException e) {
				return;
			} catch (InterruptedException ignored) {
			}
		}
	}
	
	@Override
	public void process(WatchedEvent event) {
		try {
			List<String> list = zk.getChildren(rootMembers, watcherMember);
			printListMembers(list);
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	private void printListMembers (List<String> list) {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");				
		}
	}
}
