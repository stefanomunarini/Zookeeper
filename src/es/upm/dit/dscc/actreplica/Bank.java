package es.upm.dit.dscc.actreplica;

import es.upm.dit.dscc.actreplica.node_managers.ElectionManager;
import es.upm.dit.dscc.actreplica.node_managers.OperationsManager;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class Bank {

	private ClientDB clientDB;
	public SendMessagesBank sendMessages;

	public ZooKeeper zk;

	private String leader;

	// Operations
	OperationsManager operationsManager;
	private String operationNodeName; // /operations/0000000064

	// Election
	public ElectionManager electionManager;
	private String electionNodeName; // /elections/n_0000000096
	private boolean isLeader = false;

	public Bank(ZooKeeper zk) throws KeeperException, InterruptedException {
		this.zk = zk;

		electionManager = new ElectionManager(zk, this);
		this.electionNodeName = electionManager.createElectionNode();

		operationsManager = new OperationsManager(zk);
		this.operationNodeName = operationsManager.createOperationsNode();

		// We set as data for the electionNodeName the operationNodeName.
		// In this way we know who is the leader and its operationNodeName, so that followers
		// can forward the operations to the leader (which will then dispatch them to everyone).
		Stat stat = new Stat();
		zk.setData(this.electionNodeName, this.operationNodeName.getBytes(), stat.getVersion());

		this.sendMessages = new SendMessagesBank(zk, this);
		this.clientDB = new ClientDB();

		// these has to be after the clientDb has been created
		electionManager.leaderElection();
		// Set a watcher for operations
		operationsManager.listenForOperationUpdates(this, this.operationNodeName);
	}

	public synchronized void handleReceiverMsg(OperationBank operation) {
		switch (operation.getOperation()) {
			case CREATE_CLIENT:
				clientDB.createClient(operation.getClient());
				break;
			case READ_CLIENT:
				clientDB.readClient(operation.getAccountNumber());
				break;
			case UPDATE_CLIENT:
				clientDB.updateClient(operation.getClient().getAccountNumber(),
									  operation.getClient().getBalance());
				break;
			case DELETE_CLIENT:
				clientDB.deleteClient(operation.getAccountNumber());
				break;
			case CREATE_BANK:
				System.out.println("Received CREATE_BANK");

				clientDB.createBank(operation.getClientDB());
				break;
		}
	}

	public void createClient(Client client) {
		sendMessages.sendAdd(client, isLeader);
	}

	public Client readClient(Integer accountNumber) {
		// Handled locally. No need for distributing
		return clientDB.readClient(accountNumber);
	}

	public void updateClient (int accNumber, int balance) {
		Client client = clientDB.readClient(accNumber);
		client.setBalance(balance);
		sendMessages.sendUpdate(client, isLeader);
	}

	public void deleteClient(Integer accountNumber) {
		sendMessages.sendDelete(accountNumber, isLeader);
	}

	public void sendCreateBank(){
		sendMessages.sendCreateBank(clientDB, isLeader);
	}

	public String toString() {
		return clientDB.toString();
	}


	public void close() {
		System.out.println("Byeee!");
	}

	public boolean getIsLeader(){
		return isLeader;
	}

	public void setIsLeader(boolean isLeader){
		this.isLeader = isLeader;
	}

	public String getElectionNodeName(){
		return this.electionNodeName;
	}

	public void setElectionNodeName(String electionNodeName){
		this.electionNodeName = electionNodeName;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}
}
