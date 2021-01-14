package project.gui;

import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.jdesktop.swingx.JXDatePicker;
import project.controller.Controller;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;

public class Frame extends JFrame {

    // Upper panel's components
    JLabel newTaskLabel;
    JTextField newTaskField;
    JButton newTaskBtn;

    // Middle panel's components
    JLabel timeLabel;
    JXDatePicker datePicker;
    JLabel priorityLabel;
    JComboBox<String> priorityComboBox;
    JLabel countTaskLabel;

    // Lower panel's components
    JTable todoTable;
    JScrollPane listScrollPane;

    // Panels
    JPanel upperPanel;
    JPanel middlePanel;
    JPanel lowerPanel;

    // Miscellaneous
    final static String[] priorityArray = {"High", "Medium", "Low"};
    private static final DefaultTableModel tableModel = new MyTableModel();

    public Frame() {
        super("To Do List");
        // Load the table first when creating the GUI
        System.out.println("Loading GUI");

        // Set upper panel's component
        newTaskLabel = new JLabel("New Task");
        newTaskField = new JTextField(30);
        newTaskBtn = new JButton("Add");
        newTaskBtn.setPreferredSize(new Dimension(76, 20));

        // Add components to upper panel
        upperPanel = new JPanel(new FlowLayout());
        upperPanel.add(newTaskLabel);
        upperPanel.add(newTaskField);
        upperPanel.add(newTaskBtn);

        // Middle panel's components
        timeLabel = new JLabel("Time");
        timeLabel.setPreferredSize(new Dimension(45, 16));
        datePicker = new JXDatePicker();
        datePicker.setDate(Calendar.getInstance().getTime());
        datePicker.setFormats(new SimpleDateFormat("dd/MM/yyyy"));
        datePicker.setPreferredSize(new Dimension(115, 25));
        priorityLabel = new JLabel("Priority");
        priorityComboBox = new JComboBox<>(priorityArray);
        priorityLabel.setPreferredSize(new Dimension(50, 16));
        priorityComboBox.setPreferredSize(new Dimension(115, 25));
        countTaskLabel = new JLabel();

        // Add components to middle panel
        middlePanel = new JPanel();
        middlePanel.setLayout(new FlowLayout());
        middlePanel.add(timeLabel);
        middlePanel.add(datePicker);
        middlePanel.add(priorityLabel);
        middlePanel.add(priorityComboBox);
        middlePanel.add(countTaskLabel);

        // Lower panel's components
        todoTable = new JTable(tableModel);
        listScrollPane = new JScrollPane(todoTable);
        todoTable.setFillsViewportHeight(true);
        todoTable.setCellSelectionEnabled(true);
        // Add a listener to the table
        todoTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Object[] task = new Object[3];

                // Get the information of the selected cell
                int row = todoTable.getSelectedRow();
                int col = todoTable.columnAtPoint(e.getPoint());
                int numOfColumns = todoTable.getColumnCount() - 1;

                // Get all the data from the current row
                for (int currentColumn = 0; currentColumn < numOfColumns; currentColumn++) {
                    System.out.println("Value at current row, at column " + todoTable.getColumnName(currentColumn) + ", is " + todoTable.getValueAt(row, currentColumn));
                    task[currentColumn] = todoTable.getValueAt(row, currentColumn);
                }

                // This is used to querying the database
                String name = (String) task[0];
                Date date = (Date) task[1];
                String priority = (String) task[2];

                // Set conditions when selected cell got clicked
                String fieldName;
                // Task cell got clicked
                if (col == 0) {
                    fieldName = "task";
                    String input = (String) JOptionPane.showInputDialog(
                            Frame.this,
                            "You can edit here",
                            "Edit Task",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            name);
                    if (!input.isEmpty()) {
                        System.out.println("User input: " + input);
                        UpdateResult result = Controller.updateTaskDetail(fieldName, input, name, date, priority);
                        System.out.println(result);
                        loadTable(tableModel);
                    }
                // Date cell got clicked
                } else if (col == 1) {
                    fieldName = "date";
                    JXDatePicker datePicker = new JXDatePicker();
                    String message = "You can change the date here\n";
                    Object[] params = {message, datePicker};
                    int decision = JOptionPane.showConfirmDialog(
                            Frame.this,
                            params,
                            "Change Date",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (decision == 0) {
                        Date newDate = ((JXDatePicker) params[1]).getDate();
                        System.out.println("User input: " + newDate);
                        UpdateResult result = Controller.updateTaskDetail(fieldName, newDate, name, date, priority);
                        System.out.println(result);
                        loadTable(tableModel);
                    }
                // Priority cell got clicked
                } else if (col == 2){
                    fieldName = "priority";
                    String input = (String) JOptionPane.showInputDialog(
                            Frame.this,
                            "You can reset the priority",
                            "Change Priority",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            priorityArray,
                            priority
                    );
                    if (!input.isBlank()) {
                        System.out.println("User input: " + input);
                        UpdateResult result = Controller.updateTaskDetail(fieldName, input, name, date, priority);
                        System.out.println(result);
                        loadTable(tableModel);
                    }
                // Finish task got clicked
                } else {
                    int decision = JOptionPane.showConfirmDialog(
                            Frame.this,
                            "Have you completed the task?\nThis task would be deleted",
                            "Confirmation",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (decision == 0) {
                        DeleteResult result = Controller.deleteTask(name, date, priority);
                        System.out.println(result);
                        loadTable(tableModel);
                    } else {
                        tableModel.setValueAt(Boolean.FALSE, row, col);
                    }
                }
            }
        });
        // Create a sorter to sort based on the priority
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(todoTable.getModel());
        sorter.setComparator(2, (Comparator<String>) (priority1, priority2) -> {
            int p1, p2;
            if (priority1.equals("High")) {
                p1 = 2;
            } else if (priority1.equals("Medium")) {
                p1 = 1;
            } else {
                p1 = 0;
            }
            if (priority2.equals("High")) {
                p2 = 2;
            } else if (priority2.equals("Medium")) {
                p2 = 1;
            } else {
                p2 = 0;
            }
            return p1 - p2;
        });
        todoTable.setRowSorter(sorter);
        listScrollPane.setPreferredSize(new Dimension(500,300));

        // Add components to lower panel
        lowerPanel = new JPanel();
        lowerPanel.setLayout(new FlowLayout());
        lowerPanel.add(listScrollPane);

        // Add panels to the container
        Container container = getContentPane();
        container.add(upperPanel, BorderLayout.PAGE_START);
        container.add(middlePanel, BorderLayout.CENTER);
        container.add(lowerPanel, BorderLayout.PAGE_END);
        loadTable(tableModel);

        // Implementing the add task function to the button
        newTaskBtn.addActionListener(e -> {
            try {
                if (newTaskField.getText().isBlank()) {
                    JOptionPane.showMessageDialog(Frame.this, "Please input the task name", "Alert", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Controller.addTaskIntoDatabase(newTaskField, datePicker, priorityComboBox);
                loadTable(tableModel);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        // GUI defaults
        setSize(500,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(300,200);
        pack();
        setVisible(true);
        System.out.println("GUI Created!");
    }

    public void loadTable(DefaultTableModel tableModel) {
        int count = 0;
        if (tableModel.getRowCount() > 0) {
            for (int i = tableModel.getRowCount() - 1; i > -1; i--) {
                tableModel.removeRow(i);
            }
        }
        int taskIndex = 0;
        FindIterable<Document> iterable = Controller.readFromDatabase();
        for (Document document : iterable) {
            System.out.println(document.getString("task"));
            String name = document.getString("task");
            Date date = document.getDate("date");
            String priority = document.getString("priority");
            tableModel.insertRow(taskIndex, new Object[] {name, date, priority, Boolean.FALSE});
            if (isToday(date)) count++;
            taskIndex++;
        }
        countTaskLabel.setText("You have " + count + " tasks today");
        System.out.println("Table loaded!");
    }

    public static boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar specifiedDate = Calendar.getInstance();
        specifiedDate.setTime(date);

        return today.get(Calendar.DAY_OF_MONTH) == specifiedDate.get(Calendar.DAY_OF_MONTH)
                && today.get(Calendar.MONTH) == specifiedDate.get(Calendar.MONTH)
                && today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR);
    }
}

class MyTableModel extends DefaultTableModel {
    public MyTableModel() {
        super(new String[] {"Task", "Date", "Priority", "Done"}, 0);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class columnClass = String.class;
        switch (columnIndex) {
            case 1:
                columnClass = Date.class;
                break;
            case 3:
                columnClass = Boolean.class;
                break;
        }
        return columnClass;
    }
}
