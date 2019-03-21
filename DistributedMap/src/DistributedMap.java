import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedMap extends ReceiverAdapter implements SimpleStringMap {
    private Map<String, Integer> map = new ConcurrentHashMap<>();
    private JChannel channel;
    private String user_name = System.getProperty("user.name", "n/a");


    public static void main(String[] args) throws Exception {
        new DistributedMap().start();
    }

    private void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("mapChannel");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }

    private void eventLoop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine().toLowerCase();

                if (line.startsWith("exit"))
                    break;

                String[] msg = line.split(" ");
                switch (msg[0]) {
                    case "put":
                        put(msg[1], Integer.valueOf(msg[2]));
                        break;
                    case "get":
                        if (containsKey(msg[1]))
                            System.out.println("Value under key '" + msg[1] + "': " + get(msg[1]));
                        else
                            System.out.println("There is no key '" + msg[1]);
                        break;
                    case "remove":
                        if (containsKey(msg[1]))
                            System.out.println("Value under key '" + msg[1] + "': " + remove(msg[1]) + " removed successfully");
                        else
                            System.out.println("There is no key '" + msg[1]);
                        break;
                    case "contains":
                        String ifContains = containsKey(msg[1]) ? "contains" : "does not contains";
                        System.out.println("Map " + ifContains + " value under key '" + msg[1] + "'");
                        break;
                    default:
                        line = "[" + user_name + "] " + line;
                        channel.send(new Message(null, null, line));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void viewAccepted(View new_view) {
        System.out.println("**view: " + new_view);
    }

    public void receive(Message msg) {
        if (msg.getObject() instanceof MapSynchroniseObject){
            handleSynchronise((MapSynchroniseObject) msg.getObject());
            return;
        }

        Object content = msg.getObject();
        String line = msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);

    }

    private void handleSynchronise(MapSynchroniseObject msg){
        System.out.println(msg.getKey() + " " + msg.getValue() + " " + msg.getOperation());
        switch (msg.getOperation()){
            case REMOVE:
                map.remove(msg.getKey());
                break;
            case PUT:
                map.put(msg.getKey(), msg.getValue());
                break;
        }
    }

    private void synchroniseMap(String key) {
        MapSynchroniseObject o = new MapSynchroniseObject(key, null, MapSynchroniseObject.MapOperation.REMOVE);
        try {
            channel.send(new Message(null, null, o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchroniseMap(String key, Integer value) {
        MapSynchroniseObject o = new MapSynchroniseObject(key, value, MapSynchroniseObject.MapOperation.PUT);
        try {
            channel.send(new Message(null, null, o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getState(OutputStream output) throws Exception {
        synchronized (map) {
            Util.objectToStream(map, new DataOutputStream(output));
        }
    }

    public void setState(InputStream input) throws Exception {
        Map<String, Integer> tmpMap;
        tmpMap = (Map<String, Integer>) Util.objectFromStream(new DataInputStream(input));
        synchronized (map) {
            map.clear();
            map.putAll(tmpMap);
        }
        System.out.println(tmpMap.size() + " elements in Distributed Map:");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }


    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        map.put(key, value);
        synchroniseMap(key, value);
    }

    @Override
    public Integer remove(String key) {
        Integer val = map.remove(key);
        synchroniseMap(key);
        return val;
    }
}
