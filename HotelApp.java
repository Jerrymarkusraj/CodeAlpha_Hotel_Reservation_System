import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class HotelApp extends Application {

    private HotelManager manager = new HotelManager();
    private TextArea output;

    @Override
    public void start(Stage stage) {

        ComboBox<String> roomTypeBox = new ComboBox<>();
        roomTypeBox.getItems().addAll("Standard", "Deluxe", "Suite");
        roomTypeBox.setValue("Standard");

        TextField nameField = new TextField();
        nameField.setPromptText("Customer Name");

        Button searchBtn = new Button("Search Rooms");
        Button bookBtn = new Button("Book Room");
        Button cancelBtn = new Button("Cancel Booking");

        output = new TextArea();
        output.setEditable(false);

        searchBtn.setOnAction(e ->
                output.setText(manager.searchRooms(roomTypeBox.getValue()))
        );

        bookBtn.setOnAction(e ->
                output.setText(manager.bookRoom(nameField.getText(), roomTypeBox.getValue()))
        );

        cancelBtn.setOnAction(e ->
                output.setText(manager.cancelReservation(nameField.getText()))
        );

        VBox layout = new VBox(10,
                new Label("Hotel Reservation System"),
                roomTypeBox,
                nameField,
                searchBtn,
                bookBtn,
                cancelBtn,
                output
        );

        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 500, 500));
        stage.setTitle("Hotel Reservation System");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// ================= OOP CLASSES =================

class Room {
    String type;
    int price;
    boolean available = true;

    Room(String type, int price) {
        this.type = type;
        this.price = price;
    }
}

class Reservation {
    String customer;
    Room room;

    Reservation(String customer, Room room) {
        this.customer = customer;
        this.room = room;
    }

    @Override
    public String toString() {
        return customer + "," + room.type + "," + room.price;
    }
}

class HotelManager {

    private List<Room> rooms = new ArrayList<>();
    private File file = new File("bookings.txt");

    HotelManager() {
        rooms.add(new Room("Standard", 2000));
        rooms.add(new Room("Deluxe", 3500));
        rooms.add(new Room("Suite", 5000));
    }

    String searchRooms(String type) {
        for (Room r : rooms) {
            if (r.type.equals(type) && r.available) {
                return type + " Room Available | Price: ₹" + r.price;
            }
        }
        return "No " + type + " rooms available.";
    }

    String bookRoom(String name, String type) {
        if (name.isEmpty()) return "Enter customer name.";

        for (Room r : rooms) {
            if (r.type.equals(type) && r.available) {
                r.available = false;
                saveReservation(new Reservation(name, r));
                return "Booking Successful!\nRoom: " + type + "\nPaid: ₹" + r.price;
            }
        }
        return "Room not available.";
    }

    String cancelReservation(String name) {
        if (!file.exists()) return "No bookings found.";

        List<String> remaining = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(name + ",")) {
                    found = true;
                    String[] data = line.split(",");
                    for (Room r : rooms) {
                        if (r.type.equals(data[1])) {
                            r.available = true;
                        }
                    }
                } else {
                    remaining.add(line);
                }
            }
        } catch (IOException e) {
            return "Error reading bookings.";
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String s : remaining) pw.println(s);
        } catch (IOException e) {
            return "Error updating file.";
        }

        return found ? "Reservation cancelled." : "Booking not found.";
    }

    private void saveReservation(Reservation r) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(r.toString() + "\n");
        } catch (IOException ignored) {}
    }
}