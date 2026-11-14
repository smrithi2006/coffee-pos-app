import java.text.SimpleDateFormat;
import java.util.*;

public class CoffeePOS {

    // Represents an item on the coffee shop menu
    static class MenuItem {
        int id;
        String name;
        double price;

        MenuItem(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }

    // Represents a line item in the current customer order
    static class OrderItem {
        MenuItem item;
        int quantity;

        OrderItem(MenuItem item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        double getSubtotal() {
            return item.price * quantity;
        }
    }

    private static final List<MenuItem> menu = new ArrayList<>();
    private static final Map<Integer, OrderItem> currentOrder = new LinkedHashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final double TAX_RATE = 0.05; // 5% GST/Tax
    private static int orderCounter = 1001;

    public static void main(String[] args) {
        initializeMenu();
        boolean running = true;

        System.out.println("=========================================");
        System.out.println("       WELCOME TO BREW & BEAN POS        ");
        System.out.println("=========================================");

        while (running) {
            printMainMenu();
            int choice = getIntInput("Select an option: ");

            switch (choice) {
                case 1 -> displayMenu();
                case 2 -> addItemToOrder();
                case 3 -> removeItemFromOrder();
                case 4 -> viewCurrentOrder();
                case 5 -> checkout();
                case 6 -> {
                    currentOrder.clear();
                    System.out.println("Order cleared.");
                }
                case 0 -> {
                    running = false;
                    System.out.println("Shutting down POS. Have a great day!");
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    private static void initializeMenu() {
        menu.add(new MenuItem(1, "Espresso", 120.00));
        menu.add(new MenuItem(2, "Americano", 150.00));
        menu.add(new MenuItem(3, "Cappuccino", 180.00));
        menu.add(new MenuItem(4, "Cafe Latte", 190.00));
        menu.add(new MenuItem(5, "Cold Brew", 210.00));
        menu.add(new MenuItem(6, "Mocha", 220.00));
        menu.add(new MenuItem(7, "Croissant", 140.00));
        menu.add(new MenuItem(8, "Blueberry Muffin", 160.00));
    }

    private static void printMainMenu() {
        System.out.println("\n--- POS ACTIONS ---");
        System.out.println("1. View Menu");
        System.out.println("2. Add Item to Order");
        System.out.println("3. Remove / Reduce Item");
        System.out.println("4. View Current Cart");
        System.out.println("5. Checkout & Print Receipt");
        System.out.println("6. Clear Cart");
        System.out.println("0. Exit");
    }

    private static void displayMenu() {
        System.out.println("\n---------------- COFFEE MENU ----------------");
        System.out.printf("%-4s %-22s %10s%n", "ID", "Item", "Price");
        System.out.println("---------------------------------------------");
        for (MenuItem item : menu) {
            System.out.printf("%-4d %-22s %10.2f%n", item.id, item.name, item.price);
        }
        System.out.println("---------------------------------------------");
    }

    private static void addItemToOrder() {
        displayMenu();
        int id = getIntInput("Enter Item ID to add (0 to cancel): ");
        if (id == 0) return;

        MenuItem selected = findMenuItemById(id);
        if (selected == null) {
            System.out.println("Item ID not found.");
            return;
        }

        int qty = getIntInput("Enter quantity: ");
        if (qty <= 0) {
            System.out.println("Quantity must be at least 1.");
            return;
        }

        if (currentOrder.containsKey(id)) {
            OrderItem existing = currentOrder.get(id);
            existing.quantity += qty;
        } else {
            currentOrder.put(id, new OrderItem(selected, qty));
        }

        System.out.printf("Added %dx %s to order.%n", qty, selected.name);
    }

    private static void removeItemFromOrder() {
        if (currentOrder.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        viewCurrentOrder();
        int id = getIntInput("Enter Item ID to remove/reduce (0 to cancel): ");
        if (id == 0) return;

        if (!currentOrder.containsKey(id)) {
            System.out.println("Item not in current order.");
            return;
        }

        OrderItem orderItem = currentOrder.get(id);
        int qtyToRemove = getIntInput("Enter quantity to remove (current: " + orderItem.quantity + "): ");

        if (qtyToRemove >= orderItem.quantity) {
            currentOrder.remove(id);
            System.out.println("Removed " + orderItem.item.name + " from order.");
        } else if (qtyToRemove > 0) {
            orderItem.quantity -= qtyToRemove;
            System.out.println("Updated " + orderItem.item.name + " quantity to " + orderItem.quantity + ".");
        } else {
            System.out.println("Invalid quantity.");
        }
    }

    private static void viewCurrentOrder() {
        if (currentOrder.isEmpty()) {
            System.out.println("\n[Cart is currently empty]");
            return;
        }

        System.out.println("\n-------------- CURRENT ORDER --------------");
        System.out.printf("%-4s %-20s %-5s %10s%n", "ID", "Item", "Qty", "Subtotal");
        System.out.println("-------------------------------------------");

        double subtotal = 0;
        for (OrderItem orderItem : currentOrder.values()) {
            double lineTotal = orderItem.getSubtotal();
            subtotal += lineTotal;
            System.out.printf("%-4d %-20s %-5d %10.2f%n",
                    orderItem.item.id, orderItem.item.name, orderItem.quantity, lineTotal);
        }

        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        System.out.println("-------------------------------------------");
        System.out.printf("%-31s %10.2f%n", "Subtotal:", subtotal);
        System.out.printf("%-31s %10.2f%n", "Tax (5%):", tax);
        System.out.printf("%-31s %10.2f%n", "Total:", total);
        System.out.println("-------------------------------------------");
    }

    private static void checkout() {
        if (currentOrder.isEmpty()) {
            System.out.println("Cannot checkout an empty cart.");
            return;
        }

        viewCurrentOrder();

        double subtotal = 0;
        for (OrderItem item : currentOrder.values()) {
            subtotal += item.getSubtotal();
        }

        // Optional discount
        double discountPercent = getDoubleInput("Enter discount % (0 for none): ");
        if (discountPercent < 0 || discountPercent > 100) {
            System.out.println("Invalid discount. Applying 0%.");
            discountPercent = 0;
        }

        double discountAmount = subtotal * (discountPercent / 100.0);
        double discountedSubtotal = subtotal - discountAmount;
        double tax = discountedSubtotal * TAX_RATE;
        double grandTotal = discountedSubtotal + tax;

        System.out.printf("Grand Total to Pay: %.2f%n", grandTotal);

        // Payment processing
        double amountTendered = 0;
        while (amountTendered < grandTotal) {
            amountTendered = getDoubleInput("Enter cash tendered (or -1 to cancel checkout): ");
            if (amountTendered == -1) {
                System.out.println("Checkout cancelled.");
                return;
            }
            if (amountTendered < grandTotal) {
                System.out.printf("Insufficient amount. Need at least %.2f%n", grandTotal);
            }
        }

        double change = amountTendered - grandTotal;
        printReceipt(subtotal, discountPercent, discountAmount, tax, grandTotal, amountTendered, change);

        // Reset for next customer
        currentOrder.clear();
        orderCounter++;
    }

    private static void printReceipt(double subtotal, double discountPct, double discountAmt,
                                     double tax, double total, double tendered, double change) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        System.out.println("\n===========================================");
        System.out.println("              BREW & BEAN CAFE             ");
        System.out.println("             Official POS Receipt          ");
        System.out.println("===========================================");
        System.out.println("Order #: " + orderCounter);
        System.out.println("Date:    " + timestamp);
        System.out.println("-------------------------------------------");
        System.out.printf("%-22s %-5s %12s%n", "Item", "Qty", "Amount");
        System.out.println("-------------------------------------------");

        for (OrderItem orderItem : currentOrder.values()) {
            System.out.printf("%-22s %-5d %12.2f%n",
                    orderItem.item.name, orderItem.quantity, orderItem.getSubtotal());
        }

        System.out.println("-------------------------------------------");
        System.out.printf("%-28s %12.2f%n", "Subtotal:", subtotal);
        if (discountAmt > 0) {
            System.out.printf("%-28s -%11.2f%n", "Discount (" + discountPct + "%):", discountAmt);
        }
        System.out.printf("%-28s %12.2f%n", "Tax (5%):", tax);
        System.out.printf("%-28s %12.2f%n", "GRAND TOTAL:", total);
        System.out.println("-------------------------------------------");
        System.out.printf("%-28s %12.2f%n", "Cash Tendered:", tendered);
        System.out.printf("%-28s %12.2f%n", "Change Due:", change);
        System.out.println("===========================================");
        System.out.println("       Thank you! Come back soon!          ");
        System.out.println("===========================================\n");
    }

    private static MenuItem findMenuItemById(int id) {
        for (MenuItem item : menu) {
            if (item.id == id) return item;
        }
        return null;
    }

    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid amount.");
            }
        }
    }
}