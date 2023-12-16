import java.util.*;

class Main {
    private static Map<String, Integer> createStatuses() {
        // implement me
        return Map.of("SUCCESS", 0, "FAIL", 1, "WARN", 2);
    }

    // do not change the code below
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Map<String, Integer> map = createStatuses();
        String status = scanner.next();
        int result = map.getOrDefault(status, -1);
        System.out.println(result);
    }
}