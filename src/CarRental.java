import java.util.Scanner;

public class CarRental {
    static class Car {
        private String brand;
        private String model;
        private int year;
        private double dailyRate;

        public Car(String brand, String model, int year, double dailyRate) {
            this.brand = brand;
            this.model = model;
            this.year = year;
            this.dailyRate = dailyRate;
        }
        public String getBrand() {return brand; }
        public String getModel() {return model; }
        public int getYear() {return year; }
        public double getDailyRate() {return dailyRate; }

        @Override
        public String toString(){
            return String.format("%s %s (%d p.) - %.2f грн/доба", brand, model, year, dailyRate );
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Скільки автомобілів бажаєте додати? ");
        int n = scanner.nextInt();
        scanner.nextLine();

        Car[] cars = new Car[n];

        for (int i = 0; i < n; i++) {
            System.out.println("\n--- Автомобіль №" + (i + 1) + "---");
            System.out.print("Марка: ");
            String brand = scanner.nextLine();

            System.out.print("Модель:");
            String model = scanner.nextLine();

            System.out.print("Рік випуску:");
            int year = scanner.nextInt();

            System.out.print("Добова ставка (грн): ");
            double dailyRate = scanner.nextDouble();
            scanner.nextLine();

            cars[i] = new Car(brand, model, year, dailyRate);
        }

        System.out.println("\n=== Список всіх фвтомобілів ===");
        for (Car car : cars) {
            System.out.println(car);
        }

        System.out.print("\nВведіть порогову ставку (грн) для підрахунку: ");
        double threshold = scanner.nextDouble();

        int count = 0;
        for (Car car : cars) {
            if (car.getDailyRate() > threshold) {
                count++;
            }
        }
        System.out.printf("Автомобілів дорожчих за %.2f грн: %d%n", threshold, count);

        System.out.println("\n=== Масив ДО сортування ===");
        for(Car car : cars){
            System.out.println(car);
        }
        for (int i = 0; i < cars.length - 1; i++) {
            for(int j = 0; j < cars.length - 1 - i; j++){
                if(cars[j].getDailyRate() > cars[j + 1].getDailyRate()){
                    Car temp = cars[j];
                    cars[j] = cars[j + 1];
                    cars[j + 1] = temp;
                }
            }
        }
        System.out.println("\n=== Масив ПІСЛЯ сортування (за добовою ставкою) ===");
        for (Car car : cars){
            System.out.println(car);
        }
        scanner.close();
    }
}