package consistenthashing;

import java.util.*;

public class ConsistentHashing {

    private TreeMap<Long, String> virtualNodes = new TreeMap<>();

    private int expandCount;

    public ConsistentHashing(int expandCount) {
        this.expandCount = expandCount;

        // 真实的物理节点
        List<String> physicalNodes = new ArrayList<String>() {
            {
                add("192.168.1.101");
                add("192.168.1.102");
                add("192.168.1.103");
                add("192.168.1.104");
            }
        };
        for (String physicalNode : physicalNodes) {
            addPhysicalNode(physicalNode);
        }
    }

    public ConsistentHashing() {
        this(1);
    }


    private Long getHash(String key) {
        final int constant = 16777619;
        long hash = 2166136261L;

        for (int i = 0, num = key.length(); i < num; i++) {
            hash = (hash ^ key.charAt(i)) * constant;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        return hash < 0 ? Math.abs(hash) : hash;
    }


    private void addPhysicalNode(String physicalNode) {
        for (int i = 0; i < expandCount; i++) {
            virtualNodes.put(getHash(physicalNode + '#' + i), physicalNode);
        }
    }

    private void removePhysicalNode(String physicalNode) {
        for (int i = 0; i < expandCount; i++) {
            virtualNodes.remove(getHash(physicalNode + '#' + i));
        }
    }

    private String findClosestPhysicalNode(String request) {
        SortedMap<Long, String> tailMap = virtualNodes.tailMap(getHash(request));
        long key = tailMap.isEmpty() ? virtualNodes.firstKey() : tailMap.firstKey();
        return virtualNodes.get(key);
    }

    public void getNodesMetrics(String label, int start, int end) {
        Map<String, Integer> metricsMap = new TreeMap<>();
        for (int request = start; request <= end; ++request) {
            String nodeIp = findClosestPhysicalNode(String.valueOf(request));
            metricsMap.put(nodeIp, metricsMap.getOrDefault(nodeIp, 0) + 1);
        }
        double totalCount = end - start + 1;
        System.out.println("======== " + label + " ========");
        for (Map.Entry<String, Integer> entry : metricsMap.entrySet()) {
            int percent = (int) (100 * entry.getValue() / totalCount);
            System.out.println("IP=" + entry.getKey() + ": RATE=" + percent + "%");
        }
    }

    // 统计对象与节点的映射关系
    public void getNodesMetrics(String label, int max) {
        getNodesMetrics(label, 0, max);
    }



    public static void main(String[] args) {
        ConsistentHashing ch = new ConsistentHashing(1048576);

        // 初始情况
        ch.getNodesMetrics("初始情况", 65536);

        // 删除物理节点
        ch.removePhysicalNode("192.168.1.103");
        ch.getNodesMetrics("删除物理节点", 65536);

        // 添加物理节点
        ch.addPhysicalNode("192.168.1.108");
        ch.getNodesMetrics("添加物理节点", 65536);
    }

}
