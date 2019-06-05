package com.company;

import org.apache.log4j.PropertyConfigurator;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Scanner;

public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {
    String znode;

    DataMonitor dm;

    ZooKeeper zk;

    String exec[];

    Process child;

    public Executor(String hostPort, String znode, String exec[]) throws IOException {
        this.exec = exec;
        this.znode = znode;
        zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, znode, null, this);
    }


    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("USAGE: Executor hostPort znode filename program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String znode = args[1];
        String exec[] = new String[args.length - 2];
        System.arraycopy(args, 2, exec, 0, exec.length);

        Properties props = new Properties();
        try (InputStream confStream = Executor.class.getClassLoader().getResourceAsStream("log4j.properties")) {
            props.load(confStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PropertyConfigurator.configure(props);


        try {
            new Executor(hostPort, znode, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process(WatchedEvent event) {
        dm.process(event);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (!dm.dead) {
            try {
                switch (scanner.nextLine().trim()) {
                    case "ls":
                        printZnodesStructure(znode, "");
                        break;
                    case "q":
                        if (child.isAlive())
                            child.destroy();
                        closing(0);
                        return;
                    default:
                        System.out.println("ls - print zookeeper structure\nq - quit program");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    static class StreamWriter extends Thread {
        OutputStream os;

        InputStream is;

        StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }

        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                }
            } catch (IOException e) {
            }

        }
    }

    public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void printZnodesStructure(String znd, String indent) {
        try {
            if (zk.exists(znd, false) == null)
                    return;

            System.out.println(indent + znd.substring(znd.lastIndexOf("/")));
            for (String ch : zk.getChildren(znd, false)) {
                printZnodesStructure(znd +"/" + ch, "|   " + indent);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}