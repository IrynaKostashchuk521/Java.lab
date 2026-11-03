import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
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
        void update(Vehicle vehicle, String oldStatus, String newStatus);
    }

    static class CarLogger implements CarObserver {
        @Override
        public void update(Vehicle vehicle, String oldStatus, String newStatus) {
            System.out.println(
                    "[LOG] " + vehicle.getBrand() + " " + vehicle.getModel()
                            + ": " + oldStatus + " -> " + newStatus
            );
        }
    }

    static class CarNotification implements CarObserver {
        @Override
        public void update(Vehicle vehicle, String oldStatus, String newStatus) {
            System.out.println(
                    "[ПОВІДОМЛЕННЯ] Статус транспортного засобу "
                            + vehicle.getBrand() + " " + vehicle.getModel()
                            + " змінено на: " + newStatus
            );
        }
    }

    static abstract class Vehicle {

        private String brand;
        private String model;
        private int year;
        private double dailyRate;
        private String status;

        private RentalPrice rentalPrice;

        private final List<CarObserver> observers = new ArrayList<>();

        public Vehicle(String brand, String model, int year, double dailyRate) {
            this(brand, model, year, dailyRate,
                    "Доступний", new StandardRental());
        }

        public Vehicle(String brand, String model, int year,
                       double dailyRate, String status,
                       RentalPrice rentalPrice) {

            this.brand = brand;
            this.model = model;
            this.year = year;
            this.dailyRate = dailyRate;
            this.status = status;
            this.rentalPrice = Objects.requireNonNull(rentalPrice);

            System.out.println("Викликано конструктор Vehicle");
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

        public final double getDailyRate() {
            return dailyRate;
        }

        public String getStatus() {
            return status;
        }

        public void setRentalPrice(RentalPrice rentalPrice) {
            this.rentalPrice = Objects.requireNonNull(rentalPrice);
        }

        public double calculateRentalPrice(int days) {
            if (days <= 0) {
                throw new IllegalArgumentException(
                        "Кількість днів має бути більшою за нуль."
                );
            }

            return rentalPrice.calculatePrice(dailyRate, days);
        }

        public abstract String getVehicleType();

        public void addObserver(CarObserver observer) {
            observers.add(Objects.requireNonNull(observer));
        }

        private void notifyObservers(String oldStatus) {
            for (CarObserver observer : observers) {
                observer.update(this, oldStatus, status);
            }
        }

        public void setStatus(String newStatus) {
            if (newStatus == null || newStatus.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Статус не може бути порожнім."
                );
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
                    "%s %s (%d р.) - %.2f грн/доба, статус: %s",
                    brand, model, year, dailyRate, status
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Vehicle vehicle = (Vehicle) obj;

            return year == vehicle.year
                    && Double.compare(dailyRate, vehicle.dailyRate) == 0
                    && Objects.equals(brand, vehicle.brand)
                    && Objects.equals(model, vehicle.model);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    getClass(), brand, model, year, dailyRate
            );
        }
    }

    static class Car extends Vehicle {

        private String bodyType;

        public Car(String brand, String model, int year,
                   double dailyRate, String bodyType) {

            this(brand, model, year, dailyRate,
                    bodyType, new StandardRental());
        }

        public Car(String brand, String model, int year,
                   double dailyRate, String bodyType,
                   RentalPrice rentalPrice) {

            super(brand, model, year, dailyRate,
                    "Доступний", rentalPrice);

            this.bodyType = bodyType;

            System.out.println("Викликано конструктор Car");
        }

        public String getBodyType() {
            return bodyType;
        }

        @Override
        public String getVehicleType() {
            return "Автомобіль";
        }

        @Override
        public String toString() {
            return super.toString()
                    + ", тип кузова: " + bodyType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            Car car = (Car) obj;

            return Objects.equals(bodyType, car.bodyType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), bodyType);
        }
    }

    static class ElectricCar extends Car {

        private double batteryCapacity;
        private int range;

        public ElectricCar(String brand, String model, int year,
                           double dailyRate, String bodyType,
                           double batteryCapacity, int range) {

            this(brand, model, year, dailyRate, bodyType,
                    batteryCapacity, range, new StandardRental());
        }

        public ElectricCar(String brand, String model, int year,
                           double dailyRate, String bodyType,
                           double batteryCapacity, int range,
                           RentalPrice rentalPrice) {

            super(brand, model, year, dailyRate, bodyType, rentalPrice);

            this.batteryCapacity = batteryCapacity;
            this.range = range;

            System.out.println("Викликано конструктор ElectricCar");
        }

        public double getBatteryCapacity() {
            return batteryCapacity;
        }

        public int getRange() {
            return range;
        }

        @Override
        public String getVehicleType() {
            return "Електромобіль";
        }

        @Override
        public String toString() {
            return super.toString()
                    + ", батарея: " + batteryCapacity + " кВт·год"
                    + ", запас ходу: " + range + " км";
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            ElectricCar electricCar = (ElectricCar) obj;

            return Double.compare(
                    batteryCapacity, electricCar.batteryCapacity
            ) == 0 && range == electricCar.range;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    super.hashCode(), batteryCapacity, range
            );
        }
    }

    static class Motorcycle extends Vehicle {

        private int engineCapacity;

        public Motorcycle(String brand, String model, int year,
                          double dailyRate, int engineCapacity) {

            this(brand, model, year, dailyRate,
                    engineCapacity, new StandardRental());
        }

        public Motorcycle(String brand, String model, int year,
                          double dailyRate, int engineCapacity,
                          RentalPrice rentalPrice) {

            super(brand, model, year, dailyRate,
                    "Доступний", rentalPrice);

            this.engineCapacity = engineCapacity;

            System.out.println("Викликано конструктор Motorcycle");
        }

        public int getEngineCapacity() {
            return engineCapacity;
        }

        @Override
        public String getVehicleType() {
            return "Мотоцикл";
        }

        @Override
        public String toString() {
            return super.toString()
                    + ", об'єм двигуна: " + engineCapacity + " см³";
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            Motorcycle motorcycle = (Motorcycle) obj;

            return engineCapacity == motorcycle.engineCapacity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), engineCapacity);
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try {
            List<Vehicle> vehicles = new ArrayList<>();

            System.out.print("Скільки транспортних засобів бажаєте додати? ");
            int n = scanner.nextInt();
            scanner.nextLine();

            if (n <= 0) {
                throw new IllegalArgumentException(
                        "Потрібно додати хоча б один транспортний засіб."
                );
            }

            for (int i = 0; i < n; i++) {

                System.out.println("\n--- Транспортний засіб №" + (i + 1) + " ---");

                System.out.println("1 - Автомобіль");
                System.out.println("2 - Електромобіль");
                System.out.println("3 - Мотоцикл");

                System.out.print("Оберіть тип: ");
                int type = scanner.nextInt();
                scanner.nextLine();

                System.out.print("Марка: ");
                String brand = scanner.nextLine();

                System.out.print("Модель: ");
                String model = scanner.nextLine();

                System.out.print("Рік випуску: ");
                int year = scanner.nextInt();

                System.out.print("Добова ставка (грн): ");
                double dailyRate = scanner.nextDouble();
                scanner.nextLine();

                switch (type) {

                    case 1:
                        System.out.print("Тип кузова: ");
                        String bodyType = scanner.nextLine();

                        vehicles.add(
                                new Car(brand, model, year,
                                        dailyRate, bodyType)
                        );
                        break;

                    case 2:
                        System.out.print("Тип кузова: ");
                        String electricBodyType = scanner.nextLine();

                        System.out.print("Ємність батареї (кВт·год): ");
                        double batteryCapacity = scanner.nextDouble();

                        System.out.print("Запас ходу (км): ");
                        int range = scanner.nextInt();
                        scanner.nextLine();

                        vehicles.add(
                                new ElectricCar(
                                        brand, model, year, dailyRate,
                                        electricBodyType,
                                        batteryCapacity, range
                                )
                        );
                        break;

                    case 3:
                        System.out.print("Об'єм двигуна (см³): ");
                        int engineCapacity = scanner.nextInt();
                        scanner.nextLine();

                        vehicles.add(
                                new Motorcycle(
                                        brand, model, year,
                                        dailyRate, engineCapacity
                                )
                        );
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Невідомий тип транспортного засобу."
                        );
                }
            }

            System.out.println("\n=== Список транспортних засобів ===");

            for (Vehicle vehicle : vehicles) {
                System.out.println(vehicle.getVehicleType());
                System.out.println(vehicle);
            }

            System.out.println("\n=== Перевірка instanceof ===");

            for (Vehicle vehicle : vehicles) {

                if (vehicle instanceof ElectricCar electricCar) {
                    System.out.println(
                            electricCar.getBrand() + " "
                                    + electricCar.getModel()
                                    + " - батарея: "
                                    + electricCar.getBatteryCapacity()
                                    + " кВт·год, запас ходу: "
                                    + electricCar.getRange() + " км"
                    );

                } else if (vehicle instanceof Motorcycle motorcycle) {
                    System.out.println(
                            motorcycle.getBrand() + " "
                                    + motorcycle.getModel()
                                    + " - двигун: "
                                    + motorcycle.getEngineCapacity() + " см³"
                    );
                }
            }

            System.out.print(
                    "\nВведіть порогову ставку (грн) для підрахунку: "
            );

            double threshold = scanner.nextDouble();

            int count = 0;

            for (Vehicle vehicle : vehicles) {
                if (vehicle.getDailyRate() > threshold) {
                    count++;
                }
            }

            System.out.printf(
                    "Транспортних засобів дорожчих за %.2f грн: %d%n",
                    threshold, count
            );

            System.out.println("\n=== Список ДО сортування ===");

            for (Vehicle vehicle : vehicles) {
                System.out.println(vehicle);
            }

            for (int i = 0; i < vehicles.size() - 1; i++) {
                for (int j = 0; j < vehicles.size() - 1 - i; j++) {

                    if (vehicles.get(j).getDailyRate()
                            > vehicles.get(j + 1).getDailyRate()) {

                        Vehicle temp = vehicles.get(j);
                        vehicles.set(j, vehicles.get(j + 1));
                        vehicles.set(j + 1, temp);
                    }
                }
            }

            System.out.println("\n=== Список ПІСЛЯ сортування ===");

            for (Vehicle vehicle : vehicles) {
                System.out.println(vehicle);
            }

            System.out.println("\n=== Розрахунок вартості оренди ===");

            System.out.print("Введіть кількість днів оренди: ");
            int days = scanner.nextInt();

            if (days <= 0) {
                throw new IllegalArgumentException(
                        "Кількість днів має бути більшою за нуль."
                );
            }

            Vehicle selectedVehicle = vehicles.get(0);

            System.out.println(
                    "Обраний транспорт: " + selectedVehicle
            );

            System.out.printf(
                    "Добова ставка: %.2f грн%n",
                    selectedVehicle.getDailyRate()
            );

            selectedVehicle.setRentalPrice(
                    RentalFactory.createRental("standard")
            );

            System.out.printf(
                    "Звичайна оренда: %.2f грн%n",
                    selectedVehicle.calculateRentalPrice(days)
            );

            selectedVehicle.setRentalPrice(
                    RentalFactory.createRental("long")
            );

            System.out.printf(
                    "Довгострокова оренда зі знижкою 10%%: %.2f грн%n",
                    selectedVehicle.calculateRentalPrice(days)
            );

            System.out.println(
                    "\n=== Демонстрація паттерна Observer ==="
            );

            selectedVehicle.addObserver(new CarLogger());
            selectedVehicle.addObserver(new CarNotification());

            System.out.println(
                    "Поточний статус: " + selectedVehicle.getStatus()
            );

            selectedVehicle.setStatus("В оренді");

            System.out.println(
                    "Новий статус: " + selectedVehicle.getStatus()
            );

            System.out.println("\n=== Перевірка equals і hashCode ===");

            Car car1 = new Car(
                    "Toyota", "Corolla", 2020, 1500, "Седан"
            );

            Car car2 = new Car(
                    "Toyota", "Corolla", 2020, 1500, "Седан"
            );

            System.out.println(
                    "Два однакові автомобілі: " + car1.equals(car2)
            );

            System.out.println(
                    "Однакові hashCode: "
                            + (car1.hashCode() == car2.hashCode())
            );

            ElectricCar electricCar = new ElectricCar(
                    "Tesla", "Model 3", 2023, 2500,
                    "Седан", 75, 500
            );

            System.out.println(
                    "Автомобіль дорівнює електромобілю: "
                            + car1.equals(electricCar)
            );

            System.out.println(
                    "Електромобіль дорівнює автомобілю: "
                            + electricCar.equals(car1)
            );

        } catch (InputMismatchException e) {

            System.out.println(
                    "Помилка: потрібно вводити правильні числові значення."
            );

        } catch (IllegalArgumentException e) {

            System.out.println("Помилка: " + e.getMessage());

        } finally {

            scanner.close();
            System.out.println("\nРоботу програми завершено.");
        }
    }
}