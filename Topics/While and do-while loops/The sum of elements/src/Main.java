import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // start coding here
        int sum = 0;
        int num = scanner.nextInt();

        while (num != 0) {
            sum += num;
            num = scanner.nextInt();
        }

        System.out.println(sum);
    }
}