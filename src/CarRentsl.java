import java.util.Scanner;

public class CarRentsl{
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.println("---Система прокату автомобілів---");
        System.out.print("Введіть марку автомобіля:");
        String brand = scanner.nextLine();

        System.out.print("Введіть модель автомобіля:");
        String model = scanner.nextLine();

        System.out.print("Введіть рік випуску:");
        int year = scanner.nextInt();

        System.out.print("Введіть добову вартість оренди?(грн): ");
        double dailyRate = scanner.nextDouble();

        System.out.print("Автомобіль доступний? (true/false): ");
        boolean available = scanner.nextBoolean();

        System.out.print("Введіть кількість днів оренди: ");
        int rentalDays = scanner.nextInt();

        double totalCost = dailyRate * rentalDays;
        if(rentalDays >7){
            totalCost *= 0.9;
        }

        System.out.println("\n---Деталі оренди---");
        System.out.printf("Автомобіль: %s %s (%d p.)%n", brand, model, year);
        System.out.printf("Добова ставка: %.2f грн%n", dailyRate);
        System.out.printf("Днів оренди: %d%n", rentalDays);
        System.out.printf("Доступність: %b%n", available);
        System.out.printf("Загальна вартість (з урахуванням знижка, якщо є): %.2f грн%n", totalCost);

        scanner.close();
    }
}