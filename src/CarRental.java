
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class CarRental {

    interface RentalPrice {
        double calculatePrice(double dailyRate, int days);
    }

    static class StandardRental implements RentalPrice {
        @Override
        public double calculatePrice(double dailyRate, int days) {
            return dailyRate * days;
        }
    }

    static class LongTermRental implements RentalPrice {
        @Override
        public double calculatePrice(double dailyRate, int days) {
            return dailyRate * days * 0.9;
        }
    }

    static class RentalFactory {
        public static RentalPrice createRental(String type) {
            switch (type) {
                case "standard":
                    return new StandardRental();
                case "long":
                    return new LongTermRental();
                default:
                    throw new IllegalArgumentException(
                            "Невідомий тип оренди: " + type
                    );
            }
        }
    }

    interface CarObserver {
        void update(Car car, String oldStatus, String newStatus);
    }

    static class CarLogger implements CarObserver {
        @Override
        public void update(Car car, String oldStatus, String newStatus) {
            System.out.println(
                    "[LOG] " + car.getBrand() + " " + car.getModel()
                            + ": " + oldStatus + " -> " + newStatus
            );
        }
    }

    static class CarNotification implements CarObserver {
        @Override
        public void update(Car car, String oldStatus, String newStatus) {
            System.out.println(
                    "[ПОВІДОМЛЕННЯ] Статус автомобіля "
                            + car.getBrand() + " " + car.getModel()
                            + " змінено на: " + newStatus
            );
        }
    }

    static class Car {
        private String brand;
        private String model;
        private int year;
        private double dailyRate;
        private String status = "Доступний";

        private final List<CarObserver> observers = new ArrayList<>();

        public Car(String brand, String model, int year, double dailyRate) {
            this.brand = brand;
            this.model = model;
            this.year = year;
            this.dailyRate = dailyRate;
        }

        public String getBrand() {
            return brand;
        }

        public String getModel() {
            return model;
        }

        public int getYear() {
            return year;
        }

        public double getDailyRate() {
            return dailyRate;
        }

        public String getStatus() {
            return status;
        }

        public void addObserver(CarObserver observer) {
            observers.add(observer);
        }

        private void notifyObservers(String oldStatus) {
            for (CarObserver observer : observers) {
                observer.update(this, oldStatus, status);
            }
        }

        public void setStatus(String newStatus) {
            if (newStatus == null || newStatus.trim().isEmpty()) {
                throw new IllegalArgumentException("Статус не може бути порожнім");
            }

            if (!status.equals(newStatus)) {
                String oldStatus = status;
                status = newStatus;
                notifyObservers(oldStatus);
            }
        }

        @Override
        public String toString() {
            return String.format(
                    "%s %s (%d р.) - %.2f грн/доба",
                    brand, model, year, dailyRate
            );
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Скільки автомобілів бажаєте додати? ");
            int n = scanner.nextInt();
            scanner.nextLine();

            Car[] cars = new Car[n];

            for (int i = 0; i < n; i++) {
                System.out.println("\n--- Автомобіль №" + (i + 1) + " ---");

                System.out.print("Марка: ");
                String brand = scanner.nextLine();

                System.out.print("Модель: ");
                String model = scanner.nextLine();

                System.out.print("Рік випуску: ");
                int year = scanner.nextInt();

                System.out.print("Добова ставка (грн): ");
                double dailyRate = scanner.nextDouble();
                scanner.nextLine();

                cars[i] = new Car(brand, model, year, dailyRate);
            }

            System.out.println("\n=== Список всіх автомобілів ===");

            for (Car car : cars) {
                System.out.println(car);
            }

            System.out.print(
                    "\nВведіть порогову ставку (грн) для підрахунку: "
            );

            double threshold = scanner.nextDouble();
            int count = 0;

            for (Car car : cars) {
                if (car.getDailyRate() > threshold) {
                    count++;
                }
            }

            System.out.printf(
                    "Автомобілів дорожчих за %.2f грн: %d%n",
                    threshold, count
            );


            System.out.println("\n=== Масив ДО сортування ===");

            for (Car car : cars) {
                System.out.println(car);
            }


            for (int i = 0; i < cars.length - 1; i++) {
                for (int j = 0; j < cars.length - 1 - i; j++) {
                    if (cars[j].getDailyRate() >
                            cars[j + 1].getDailyRate()) {

                        Car temp = cars[j];
                        cars[j] = cars[j + 1];
                        cars[j + 1] = temp;
                    }
                }
            }


            System.out.println("\n=== Масив ПІСЛЯ сортування ===");

            for (Car car : cars) {
                System.out.println(car);
            }


            System.out.println(
                    "\n=== Розрахунок вартості оренди ==="
            );

            System.out.print("Введіть кількість днів оренди: ");
            int days = scanner.nextInt();

            RentalPrice[] rentalPrices = {
                    RentalFactory.createRental("standard"),
                    RentalFactory.createRental("long")
            };

            double dailyRate = cars[0].getDailyRate();

            System.out.printf(
                    "Добова ставка: %.2f грн%n",
                    dailyRate
            );

            for (RentalPrice rentalPrice : rentalPrices) {
                double price =
                        rentalPrice.calculatePrice(dailyRate, days);

                System.out.printf(
                        "%s: %.2f грн%n",
                        rentalPrice.getClass().getSimpleName(),
                        price
                );
            }


            System.out.println(
                    "\n=== Демонстрація паттерна Observer ==="
            );

            Car selectedCar = cars[0];

            selectedCar.addObserver(new CarLogger());
            selectedCar.addObserver(new CarNotification());

            System.out.println(
                    "Поточний статус: " + selectedCar.getStatus()
            );

            selectedCar.setStatus("В оренді");

            System.out.println(
                    "Новий статус: " + selectedCar.getStatus()
            );

        } catch (InputMismatchException e) {
            System.out.println(
                    "Помилка: там, де очікується число, потрібно вводити число."
            );

        } catch (NegativeArraySizeException e) {
            System.out.println(
                    "Помилка: кількість автомобілів не може бути від'ємною."
            );

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(
                    "Помилка: потрібно додати хоча б один автомобіль."
            );

        } finally {
            scanner.close();
            System.out.println("\nРоботу програми завершено.");
        }
    }
}