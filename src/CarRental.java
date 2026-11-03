
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CarRental {

    // ==========================================
    // 1. БАЗОВИЙ КЛАС BASE ENTITY
    // ==========================================

    static abstract class BaseEntity {

        private final long id;
        private final String name;

        public BaseEntity(long id, String name) {

            if (id <= 0) {
                throw new IllegalArgumentException("ID має бути більшим за нуль.");
            }

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Назва не може бути порожньою.");
            }

            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            BaseEntity entity = (BaseEntity) obj;

            return id == entity.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), id);
        }
    }

    // ==========================================
    // 2. ВЛАСНИЙ ВИНЯТОК
    // ==========================================

    static class EntityNotFoundException extends RuntimeException {

        private final long id;

        public EntityNotFoundException(long id) {
            super("Транспортний засіб з ID " + id + " не знайдено.");
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    // ==========================================
    // 3. УЗАГАЛЬНЕНИЙ CRUD-ІНТЕРФЕЙС
    // ==========================================

    interface CRUDService<T extends BaseEntity> {

        T findById(long id);

        T save(T entity);

        boolean delete(long id);

        List<T> findAll();

        List<T> findAllSortedByName();
    }

    // ==========================================
    // 4. УЗАГАЛЬНЕНИЙ CRUD-СЕРВІС
    // ==========================================

    static class InMemoryCrudService<T extends BaseEntity>
            implements CRUDService<T> {

        protected final Map<Long, T> storage = new LinkedHashMap<>();

        @Override
        public T findById(long id) {

            T entity = storage.get(id);

            if (entity == null) {
                throw new EntityNotFoundException(id);
            }

            return entity;
        }

        @Override
        public T save(T entity) {

            Objects.requireNonNull(
                    entity,
                    "Сутність не може бути null."
            );

            storage.put(entity.getId(), entity);

            return entity;
        }

        @Override
        public boolean delete(long id) {
            return storage.remove(id) != null;
        }

        @Override
        public List<T> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<T> findAllSortedByName() {

            List<T> result = findAll();

            result.sort(
                    Comparator.comparing(
                            BaseEntity::getName,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            return result;
        }
    }

    // ==========================================
    // 5. ІНТЕРФЕЙС ДЛЯ ТРАНСПОРТНИХ ЗАСОБІВ
    // ==========================================

    interface VehicleService extends CRUDService<Vehicle> {

        List<Vehicle> findAllSortedByNameAndByDailyRate();
    }

    // ==========================================
    // 6. РЕАЛІЗАЦІЯ VEHICLE CRUD SERVICE
    // ==========================================

    static class VehicleCrudService
            extends InMemoryCrudService<Vehicle>
            implements VehicleService {

        @Override
        public List<Vehicle> findAllSortedByNameAndByDailyRate() {

            List<Vehicle> result = findAll();

            result.sort(
                    Comparator.comparing(
                            Vehicle::getName,
                            String.CASE_INSENSITIVE_ORDER
                    ).thenComparingDouble(Vehicle::getDailyRate)
            );

            return result;
        }
    }

    // ==========================================
    // 7. STRATEGY
    // ==========================================

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

    // ==========================================
    // 8. FACTORY METHOD
    // ==========================================

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

    // ==========================================
    // 9. OBSERVER
    // ==========================================

    interface CarObserver {

        void update(
                Vehicle vehicle,
                String oldStatus,
                String newStatus
        );
    }

    static class CarLogger implements CarObserver {

        @Override
        public void update(
                Vehicle vehicle,
                String oldStatus,
                String newStatus
        ) {

            System.out.println(
                    "[LOG] " + vehicle.getName()
                            + ": " + oldStatus + " -> " + newStatus
            );
        }
    }

    static class CarNotification implements CarObserver {

        @Override
        public void update(
                Vehicle vehicle,
                String oldStatus,
                String newStatus
        ) {

            System.out.println(
                    "[ПОВІДОМЛЕННЯ] Статус "
                            + vehicle.getName()
                            + " змінено на: " + newStatus
            );
        }
    }

    // ==========================================
    // 10. АБСТРАКТНИЙ КЛАС VEHICLE
    // ==========================================

    static abstract class Vehicle extends BaseEntity {

        private final String brand;
        private final String model;
        private final int year;
        private final double dailyRate;

        private String status;
        private RentalPrice rentalPrice;

        private final List<CarObserver> observers = new ArrayList<>();

        public Vehicle(
                long id,
                String brand,
                String model,
                int year,
                double dailyRate
        ) {

            this(
                    id,
                    brand,
                    model,
                    year,
                    dailyRate,
                    "Доступний",
                    new StandardRental()
            );
        }

        public Vehicle(
                long id,
                String brand,
                String model,
                int year,
                double dailyRate,
                String status,
                RentalPrice rentalPrice
        ) {

            super(
                    id,
                    Objects.requireNonNull(brand)
                            + " "
                            + Objects.requireNonNull(model)
            );

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

        public double getDailyRate() {
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
                    "ID: %d, %s (%d р.) - %.2f грн/доба, статус: %s",
                    getId(),
                    getName(),
                    year,
                    dailyRate,
                    status
            );
        }
    }

    // ==========================================
    // 11. КЛАС CAR
    // ==========================================

    static class Car extends Vehicle {

        private final String bodyType;

        public Car(
                long id,
                String brand,
                String model,
                int year,
                double dailyRate,
                String bodyType
        ) {

            this(
                    id,
                    brand,
                    model,
                    year,
                    dailyRate,
                    bodyType,
                    new StandardRental()
            );
        }

        public Car(
                long id,
                String brand,
                String model,
                int year,
                double dailyRate,
                String bodyType,
                RentalPrice rentalPrice
        ) {

            super(
                    id,
                    brand,
                    model,
                    year,
                    dailyRate,
                    "Доступний",
                    rentalPrice
            );

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
    }

    // ==========================================
    // 12. КЛАС ELECTRIC CAR
    // ==========================================

    static class ElectricCar extends Car {

        private final double batteryCapacity;
        private final int range;

        public ElectricCar(
                long id,
                String brand,
                String model,
                int year,
                double dailyRate,
                String bodyType,
                double batteryCapacity,
                int range
        ) {

            this(
                    id,
                    brand,
                    model,
                    year,
                    dailyRate,
                    bodyType,
                    batteryCapacity,
                    range,
                    new StandardRental()
            );
        }

        public ElectricCar(
                long id,
                String brand,
                String model,
                int year,
                double dailyRate,
                String bodyType,
                double batteryCapacity,
                int range,
                RentalPrice rentalPrice
        ) {

            super(
                    id,
                    brand,
                    model,
                    year,
                    dailyRate,
                    bodyType,
                    rentalPrice
            );

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
    }

    // ==========================================
    // 13. КЛАС MOTORCYCLE
    // ==========================================

    static class Motorcycle extends Vehicle {

        private final int engineCapacity;

        public Motorcycle(
                long id,
                String brand,
                String model,
                int year,
                double dailyRate,
                int engineCapacity
        ) {

            this(
                    id,
                    brand,
                    model,
                    year,
                    dailyRate,
                    engineCapacity,
                    new StandardRental()
            );
        }

        public Motorcycle(
                long id,
                String brand,
                String model,
                int year,
                double dailyRate,
                int engineCapacity,
                RentalPrice rentalPrice
        ) {

            super(
                    id,
                    brand,
                    model,
                    year,
                    dailyRate,
                    "Доступний",
                    rentalPrice
            );

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
    }

    // ==========================================
    // 14. ВИВЕДЕННЯ СПИСКУ
    // ==========================================

    public static void printVehicles(
            Collection<? extends Vehicle> vehicles
    ) {

        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }

    // ==========================================
    // 15. ГОЛОВНИЙ МЕТОД MAIN
    // ==========================================

    public static void main(String[] args) {

        VehicleCrudService service = new VehicleCrudService();

        // Додавання транспортних засобів

        System.out.println("=== ДОДАВАННЯ ТРАНСПОРТНИХ ЗАСОБІВ ===");

        service.save(
                new Car(
                        1, "Toyota", "Corolla",
                        2020, 1500, "Седан"
                )
        );

        service.save(
                new ElectricCar(
                        2, "Tesla", "Model 3",
                        2023, 2500, "Седан", 75, 500
                )
        );

        service.save(
                new Car(
                        3, "Toyota", "Corolla",
                        2022, 1800, "Універсал"
                )
        );

        System.out.println(
                "Кількість записів: " + service.findAll().size()
        );

        // Пошук

        System.out.println("\n=== ПОШУК ЗА ID ===");

        System.out.println(service.findById(1));

        // Оновлення

        System.out.println("\n=== ОНОВЛЕННЯ СУТНОСТІ ===");

        service.save(
                new Car(
                        1, "Toyota", "Corolla",
                        2021, 1600, "Седан"
                )
        );

        System.out.println(
                "Кількість записів після оновлення: "
                        + service.findAll().size()
        );

        System.out.println(service.findById(1));

        // Список до сортування

        System.out.println("\n=== СПИСОК ДО СОРТУВАННЯ ===");

        printVehicles(service.findAll());

        // Сортування за назвою

        System.out.println("\n=== СОРТУВАННЯ ЗА ІМЕНЕМ ===");

        printVehicles(service.findAllSortedByName());

        // Сортування за назвою та ставкою

        System.out.println(
                "\n=== СОРТУВАННЯ ЗА ІМЕНЕМ ТА ДОБОВОЮ СТАВКОЮ ==="
        );

        printVehicles(
                service.findAllSortedByNameAndByDailyRate()
        );

        // Перевірка незмінності сховища

        System.out.println("\n=== СХОВИЩЕ ПІСЛЯ СОРТУВАННЯ ===");

        printVehicles(service.findAll());

        // Видалення

        System.out.println("\n=== ВИДАЛЕННЯ ===");

        System.out.println(
                "Видалення ID 2: " + service.delete(2)
        );

        System.out.println(
                "Видалення ID 999: " + service.delete(999)
        );

        System.out.println(
                "Кількість записів: " + service.findAll().size()
        );

        // Обробка винятку

        System.out.println("\n=== ОБРОБКА ВИНЯТКУ ===");

        try {

            service.findById(999);

        } catch (EntityNotFoundException e) {

            System.out.println("Помилка: " + e.getMessage());

            System.out.println(
                    "ID відсутньої сутності: " + e.getId()
            );
        }

        // Перевірка узагальнення з іншою сутністю

        System.out.println("\n=== ПЕРЕВІРКА GENERICS ===");

        InMemoryCrudService<Car> carStorage =
                new InMemoryCrudService<>();

        carStorage.save(
                new Car(
                        10, "BMW", "X5",
                        2022, 2200, "Позашляховик"
                )
        );

        System.out.println("Окреме сховище автомобілів:");

        for (Car car : carStorage.findAll()) {
            System.out.println(car);
        }

        // Strategy

        System.out.println("\n=== ПАТЕРН STRATEGY ===");

        Vehicle selectedVehicle = service.findById(1);

        selectedVehicle.setRentalPrice(
                RentalFactory.createRental("standard")
        );

        System.out.printf(
                "Звичайна оренда за 5 днів: %.2f грн%n",
                selectedVehicle.calculateRentalPrice(5)
        );

        selectedVehicle.setRentalPrice(
                RentalFactory.createRental("long")
        );

        System.out.printf(
                "Довгострокова оренда за 5 днів: %.2f грн%n",
                selectedVehicle.calculateRentalPrice(5)
        );

        // Observer

        System.out.println("\n=== ПАТЕРН OBSERVER ===");

        selectedVehicle.addObserver(new CarLogger());

        selectedVehicle.addObserver(new CarNotification());

        selectedVehicle.setStatus("В оренді");

        System.out.println("\nРоботу програми завершено.");
    }
}
