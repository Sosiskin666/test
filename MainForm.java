package lab15;

import javax.swing.*; // Импорт компонентов GUI из Swing
import java.awt.*; // Импорт AWT компонентов для интерфейса
import java.awt.event.ActionEvent; // Импорт события нажатия кнопки
import java.awt.event.ActionListener; // Импорт слушателя действия
import java.io.*; // Импорт для работы с файлами
import java.util.HashMap; // Импорт хэш-карты

public class MainForm {
    public static void main(String[] args) {

        JFrame frame = new JFrame("lab15");
        frame.setSize(1000, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComboBox<String> combo = new JComboBox<String>();

        HashMap<String, String> seas = new HashMap<String, String>();

        // получение текущей директории
        File dir = new File(".");

        // поиск файлов с .txt
        for(File file:dir.listFiles()) {
            String name = file.getName();
            if(name.endsWith(".txt")) {
                System.out.println(name);
                seas.put(name.substring(0, name.length()-4), name);
            }
        }

        // добавление всех ключей (имён файлов) в выпадающий список
        for(String name: seas.keySet()) {
            combo.addItem(name);
        }

        // отображ
        JPanel jp = new JPanel();
        jp.setLayout(new FlowLayout());

        //текстовое поле для отображения содержимого файла
        JTextArea label = new JTextArea();
        label.setLineWrap(true); // перенос строк
        label.setEditable(false); // только для чтения
        label.setBackground(frame.getBackground());
        label.setPreferredSize(new Dimension(500, 500));

        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setPreferredSize(new Dimension(300, 300));


        // обработка выбора из выпадающего списка
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String)combo.getSelectedItem();
                if (selectedItem == null) {
                    label.setText("");
                    return;
                }
                String text = "";
                try (BufferedReader reader = new BufferedReader(new FileReader(seas.get(selectedItem)))) {
                    String readed_text = reader.readLine();
                    while (readed_text != null) {
                        text = text.concat(readed_text + "\n");
                        readed_text = reader.readLine();
                    }
                } catch(IOException ex) {
                    ex.printStackTrace();
                    label.setText("Ошибка чтения файла: " + ex.getMessage());
                    return;
                }
                label.setText(text);
            }
        });

        // панель добавления и удаления
        Box addpanel = new Box(BoxLayout.X_AXIS);
        addpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(); //для имени нового файла
        nameField.setPreferredSize(new Dimension(100, 25));

        JTextArea area = new JTextArea(5, 20); //ввод содержимого
        area.setLineWrap(true);
        JScrollPane areaScrollPane = new JScrollPane(area);

        JButton addButton = new JButton(); //кнопка добавления файла
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = nameField.getText().trim();
                if (fileName.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Имя файла не может быть пустым!", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                //проверка на повтор
                if (seas.containsKey(fileName)) {

                    nameField.setBackground(Color.RED);

                    JOptionPane.showMessageDialog(frame, "Файл с таким именем уже существует!", "Ошибка", JOptionPane.WARNING_MESSAGE);

                    Timer timer = new Timer(500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            nameField.setBackground(UIManager.getColor("TextField.background"));
                            ((Timer)e.getSource()).stop();
                        }
                    });
                    timer.start();
                    return;
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + ".txt"))) {
                    writer.write(area.getText()); //запись в файл
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Ошибка при записи файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //обновление списка и словаря
                seas.put(fileName, fileName + ".txt");
                combo.addItem(fileName);
                nameField.setText(""); //очистить поля
                area.setText("");
                JOptionPane.showMessageDialog(frame, "Файл '" + fileName + ".txt' успешно добавлен!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton deleteButton = new JButton(); //кнопка удаления файла
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = (String) combo.getSelectedItem();
                if (filename == null) {
                    JOptionPane.showMessageDialog(frame, "Выберите файл для удаления!", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(frame, "Вы уверены, что хотите удалить файл '" + filename + ".txt'?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.NO_OPTION) {
                    return;
                }

                System.out.println(filename); //лог в консоль
                File to_del = new File(seas.get(filename)); //получ полное имя файла из HashMap
                if (to_del.delete()) { //удаление файла
                    combo.removeItem(filename); //удаление из выпадающего списка
                    seas.remove(filename); // удаление из HashMap
                    label.setText("");
                    JOptionPane.showMessageDialog(frame, "Файл '" + filename + ".txt' успешно удален!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Не удалось удалить файл '" + filename + ".txt'.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        area.setEditable(true); //поле для ввода открыто для редактирования
        addButton.setText("Добавить");
        deleteButton.setText("Удалить");

        //добавление компонентов на панель добавления/удаления
        addpanel.add(new JLabel("Имя файла:"));
        addpanel.add(nameField);
        addpanel.add(Box.createHorizontalStrut(10));
        addpanel.add(new JLabel("Содержимое:"));
        addpanel.add(areaScrollPane);
        addpanel.add(Box.createHorizontalStrut(10));
        addpanel.add(addButton);
        addpanel.add(deleteButton);




        //добавление выпадающего списка и поля вывода на основную панель
        jp.add(combo);
        jp.add(scrollPane);

        //добавление панелей в окно
        frame.add(addpanel, BorderLayout.NORTH);
        frame.add(jp, BorderLayout.CENTER);

        frame.setVisible(true);

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
            combo.getActionListeners()[0].actionPerformed(new ActionEvent(combo, ActionEvent.ACTION_PERFORMED, null));
        }
    }
}
