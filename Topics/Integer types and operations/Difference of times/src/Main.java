import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // put your code here
        int hour1, hour2, minute1, minute2, second1, second2;
        hour1 = scanner.nextInt();
        minute1 = scanner.nextInt();
        second1 = scanner.nextInt();
        hour2 = scanner.nextInt();
        minute2 = scanner.nextInt();
        second2 = scanner.nextInt();
        System.out.println((hour2*3600+minute2*60+second2)-(hour1*3600+minute1*60+second1));
    }
}
