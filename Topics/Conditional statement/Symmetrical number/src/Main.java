import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // start coding here
        String num = scanner.next();
        if (num.charAt(0) != num.charAt(3)) {
            System.out.println(Integer.parseInt(num) - 1);
        } else if (num.charAt(1) != num.charAt(2)) {
            System.out.println(Integer.parseInt(num) - 1);
        } else {
            System.out.println(1);
        }
    }
}