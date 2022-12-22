package GUI;

import exceptions.UnknownCommandException;
import handlers.HandlerBinaryCode;
import models.RecordBody;
import models.SymbolicName;
import tables.OpcodeTableSingleton;
import tables.RecordingTableSingleton;
import tables.SymbolicNamesTableSingleton;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainWindow extends JFrame {
    private String nameOfProgram;
    private String startAddress;
    private String endAddress;
    private int index;
    private HandlerBinaryCode handler;

    private final JPanel rootPanel;
    private final JPanel infoPanel;
    private final JPanel firstPassPanel;
    private final JPanel secondPassPanel;
    private final JPanel buttonsPanel;

    private JLabel choosingExampleLabel;
    private JButton firstPassButton;
    private JButton oneStepButton;
    private JButton resetButton;
    private JButton choosingExample;

    //Левая панель
    private JLabel sourceTextLabel;
    //private JTextArea sourceTextArea;
    private JTextPane sourceTextArea;
    private JScrollPane scrollPaneSourceText;
    private JLabel codesOfOperationLabel;
    private JTable codesOfOperationTable;
    private DefaultTableModel tableModelCodesOfOperation;
    private JScrollPane scrollPaneCodesOfOperation;
    private JButton btnAddNewRow;
    private JButton btnDelRow;

    //Центральная панель
    private JLabel symbolicNamesLabel;
    private JTable symbolicNamesTable;
    DefaultTableModel tableModelSymbolicNames;
    private JScrollPane scrollPaneSymbolicNames;
    private JLabel errorsFirstPassLabel;
    private JTextArea errorsFirstPassText;
    private JScrollPane scrollPaneErrorFirst;

    //Правая панель
    private JLabel binaryCodeLabel;
    private JTextArea binaryCodeText;
    private JScrollPane scrollPaneBinaryCode;

    public MainWindow() {
        super("Однопросмотровый ассемблер");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        //Настройка панелей
        rootPanel = new JPanel();
        infoPanel = new JPanel();
        firstPassPanel = new JPanel();
        secondPassPanel = new JPanel();
        buttonsPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(320, 530));
        firstPassPanel.setPreferredSize(new Dimension(320, 530));
        secondPassPanel.setPreferredSize(new Dimension(320, 530));
        buttonsPanel.setPreferredSize(new Dimension(970, 100));
        infoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        firstPassPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        secondPassPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        rootPanel.add(infoPanel);
        rootPanel.add(firstPassPanel);
        rootPanel.add(secondPassPanel);
        rootPanel.add(buttonsPanel);
        //

        //Настройка кнопок
        firstPassButton = new JButton();
        oneStepButton = new JButton();
        resetButton = new JButton();
        firstPassButton.setText("Первый проход/Продолжить");
        oneStepButton.setText("Один шаг");
        resetButton.setText("Сброс");
        resetButton.setEnabled(true);
        choosingExampleLabel = new JLabel("Выбор примера: ");
        choosingExample = new JButton();
        firstPassButton.setEnabled(true);
        oneStepButton.setEnabled(true);
        buttonsPanel.add(firstPassButton);
        buttonsPanel.add(oneStepButton);
        buttonsPanel.add(resetButton);
        buttonsPanel.add(choosingExampleLabel);
        buttonsPanel.add(choosingExample);
        choosingExample.setText("Загрузить пример");
        //

        //Настройка левой панели
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        sourceTextLabel = new JLabel("Исходный текст");
        sourceTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sourceTextArea = new JTextPane();
        sourceTextArea.setEnabled(true);
        scrollPaneSourceText = new JScrollPane(sourceTextArea);
        scrollPaneSourceText.setMinimumSize(new Dimension(300, 300));
        scrollPaneSourceText.setPreferredSize(new Dimension(300, 330));

        codesOfOperationLabel = new JLabel("Таблица кодов операций");
        codesOfOperationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableModelCodesOfOperation = new DefaultTableModel(new String[] {"МКОП", "Дв. код", "Длина"}, 0);
        codesOfOperationTable = new JTable();
        codesOfOperationTable.setModel(tableModelCodesOfOperation);
        codesOfOperationTable.setEnabled(true);
        scrollPaneCodesOfOperation = new JScrollPane(codesOfOperationTable);
        btnAddNewRow = new JButton("Добавить строку");
        btnAddNewRow.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        btnAddNewRow.setPreferredSize(new Dimension(200, 10));
        btnDelRow = new JButton("Удалить строку");
        btnDelRow.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        btnDelRow.setPreferredSize(new Dimension(200, 10));

        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(sourceTextLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(scrollPaneSourceText);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(codesOfOperationLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(scrollPaneCodesOfOperation);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(btnAddNewRow);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(btnDelRow);
        //

        //Настройка центральной панели
        firstPassPanel.setLayout(new BoxLayout(firstPassPanel, BoxLayout.Y_AXIS));

        symbolicNamesLabel = new JLabel("Таблица символических имен");
        symbolicNamesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableModelSymbolicNames = new DefaultTableModel(new String[]{"СИ", "Адрес СИ", "Значение СА"}, 0);
        symbolicNamesTable = new JTable();
        symbolicNamesTable.setModel(tableModelSymbolicNames);
        symbolicNamesTable.setEnabled(false);
        scrollPaneSymbolicNames = new JScrollPane(symbolicNamesTable);

        errorsFirstPassLabel = new JLabel("Ошибки прохода");
        errorsFirstPassLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorsFirstPassText = new JTextArea();
        errorsFirstPassText.setMinimumSize(new Dimension(250, 200));
        //errorsFirstPassText.setColumns(5);
        errorsFirstPassText.setEnabled(false);
        errorsFirstPassText.setDisabledTextColor(Color.BLACK);
        scrollPaneErrorFirst = new JScrollPane(errorsFirstPassText);
        scrollPaneErrorFirst.setMinimumSize(new Dimension(250, 200));

        firstPassPanel.add(Box.createVerticalStrut(20));
        firstPassPanel.add(symbolicNamesLabel);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(scrollPaneSymbolicNames);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(errorsFirstPassLabel);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(scrollPaneErrorFirst);
        //

        //Настройка правой панели
        secondPassPanel.setLayout(new BoxLayout(secondPassPanel, BoxLayout.Y_AXIS));

        binaryCodeLabel = new JLabel("Двоичный код");
        binaryCodeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        binaryCodeText = new JTextArea();
        binaryCodeText.setDisabledTextColor(Color.BLACK);
        binaryCodeText.setEnabled(false);
        scrollPaneBinaryCode = new JScrollPane(binaryCodeText);

        secondPassPanel.add(Box.createVerticalStrut(20));
        secondPassPanel.add(binaryCodeLabel);
        secondPassPanel.add(Box.createVerticalStrut(10));
        secondPassPanel.add(scrollPaneBinaryCode);
        secondPassPanel.add(Box.createVerticalStrut(10));
        //

        index = -1;
        addListeners();

        pack();
        setContentPane(rootPanel);
        // Вывод окна на экран
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setVisible(true);
        btnDelRow.setEnabled(false);
    }

    private void addListeners() {
        firstPassButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index == -1) {
                    clearTables();
                    firstPassButton.setEnabled(false);
                    choosingExample.setEnabled(false);
                    //Обработка ТКО
                    try {
                        updateTKO();
                    }catch (UnknownCommandException exep) {
                        updateButtonsAndText();
                        errorsFirstPassText.setText(exep.getMessage());
                        return;
                    }
                    catch (Exception ex) {
                        updateButtonsAndText();
                        errorsFirstPassText.setText("Ошибка при обработке таблицы ТКО.");
                        return;
                    }

                    //Настройка текста
                    handler = new HandlerBinaryCode(sourceTextArea.getText());

                    sourceTextArea.setDisabledTextColor(Color.BLACK);
                    sourceTextArea.setEnabled(false);

                    index++;
                }

                //обработка шага
                try {
                    while(handler.isHasNext()) {
                        handler.getOneStep(index++);
                    }
                    index = -1;
                    firstPassButton.setEnabled(true);
                } catch (Exception ex) {
                    updateButtonsAndText();
                    errorsFirstPassText.setText(ex.getMessage());
                    index = -1;
                    return;
                }
                updateTSI();
                updateObjectModule();
                updateButtonsAndText();
            }
        });

        oneStepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index == -1) {
                    clearTables();
                    choosingExample.setEnabled(false);
                    //Обработка ТКО
                    try {
                        updateTKO();
                    }catch (UnknownCommandException exep) {
                        updateButtonsAndText();
                        errorsFirstPassText.setText(exep.getMessage());
                        return;
                    }
                    catch (Exception ex) {
                        updateButtonsAndText();
                        errorsFirstPassText.setText("Ошибка при обработке таблицы ТКО.");
                        return;
                    }

                    //Настройка текста
                    handler = new HandlerBinaryCode(sourceTextArea.getText());

                    sourceTextArea.setDisabledTextColor(Color.BLACK);
                    sourceTextArea.setEnabled(false);

                    index++;
                    updateText(index);
                    return;
                }

                //обработка шага
                try {
                    handler.getOneStep(index++);
                    updateText(index);
                    if (!handler.isHasNext()) {
                        index = -1;
                        choosingExample.setEnabled(true);
                        sourceTextArea.setEnabled(true);
                    }
                } catch (Exception ex) {
                    updateButtonsAndText();
                    errorsFirstPassText.setText(ex.getMessage());
                    index = -1;
                    sourceTextArea.setText(handler.getText());
                    return;
                }
                updateTSI();
                updateObjectModule();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateButtonsAndText();
                clearTables();
                index = -1;
                updateText(index);
            }
        });

        btnAddNewRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnDelRow.setEnabled(true);
                tableModelCodesOfOperation.addRow(new Object[] {"", "", ""});
            }
        });

        btnDelRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModelCodesOfOperation.removeRow(tableModelCodesOfOperation.getRowCount() - 1);
                if (tableModelCodesOfOperation.getRowCount() == 0)
                    btnDelRow.setEnabled(false);
            }
        });

        choosingExample.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModelCodesOfOperation.setRowCount(0);
                insertText();
                btnDelRow.setEnabled(true);
                updateButtonsAndText();
                clearTables();
                index = -1;
            }
        });
    }

    private void updateTKO() throws UnknownCommandException {
        OpcodeTableSingleton opcodeTable = OpcodeTableSingleton.getInstance();
        for (int i = 0; i < codesOfOperationTable.getRowCount(); i++) {
            String name = codesOfOperationTable.getValueAt(i, 0).toString();
            if (!checkValidSymbols(name)) {
                throw new UnknownCommandException("Некорректное имя команды в таблице ТКО");
            }
            String binaryCode = codesOfOperationTable.getValueAt(i, 1).toString();
            if ("".equals(name) || "".equals(binaryCode))
                return;
            int len = Integer.parseInt(codesOfOperationTable.getValueAt(i, 2).toString());
            if (len <= 0)
                throw new RuntimeException();
            opcodeTable.addCommand(name, binaryCode, len);
        }
    }

    private void updateTSI() {
        tableModelSymbolicNames.setRowCount(0);
        ArrayList<String> arrayList = new ArrayList<>();
        SymbolicNamesTableSingleton symbolicNames = SymbolicNamesTableSingleton.getInstance();
        HashMap<String, SymbolicName> hashMap = symbolicNames.getSymbolicNames();
        for (String elem : hashMap.keySet()) {
            arrayList.add(elem);
        }
        Collections.sort(arrayList);
        for (String elem : arrayList) {
            SymbolicName sName = hashMap.get(elem);
            if ("".equals(sName.getAddressName())) {
                ArrayList<String> arrayListAddress =  sName.getAddressArrayList();
                for (int i = 0; i < arrayListAddress.size(); i++) {
                    tableModelSymbolicNames.addRow(new Object[]{elem, "", arrayListAddress.get(i)});
                }
            } else {
                tableModelSymbolicNames.addRow(new Object[]{elem, hashMap.get(elem).getAddressName(), ""});
            }
        }
    }

    private void updateObjectModule() {
        ArrayList<String> arrayList = new ArrayList<>();
        RecordingTableSingleton recordingTable = RecordingTableSingleton.getInstance();
        HashMap<String , RecordBody> hashMap = recordingTable.getRecordingTable();
        for (String elem : hashMap.keySet()) {
            arrayList.add(elem);
        }
        Collections.sort(arrayList);
        StringBuilder stringBuilder = new StringBuilder();
        String end = "";
        for (String elem : arrayList) {
            if (elem.charAt(0) == 'E') {
                end = elem;
                continue;
            }
            stringBuilder.append(elem);
            stringBuilder.append(" ");
            stringBuilder.append(hashMap.get(elem).getLen());
            stringBuilder.append(" ");
            stringBuilder.append(hashMap.get(elem).getBody());
            stringBuilder.append("\n");
        }
        stringBuilder.append(end);
        binaryCodeText.setText(stringBuilder.toString());
    }

    private void clearTables() {
        OpcodeTableSingleton.clear();
        SymbolicNamesTableSingleton.clear();
        RecordingTableSingleton.clear();
        tableModelSymbolicNames.setRowCount(0);
        binaryCodeText.setText("");
        errorsFirstPassText.setText("");
    }

    private void insertText() {
        String prog = new StringBuilder()
                .append("Prog1 START 100\n")
                .append("      JMP L1\n")
                .append("A1: RESB 10\n")
                .append("A2: RESW 20\n")
                .append("B1: WORD 4096\n")
                .append("B2: BYTE x'2F4C008A'\n")
                .append("B3: BYTE c'Hello!'\n")
                .append("B4: BYTE 128\n")
                .append("L1: LOADR1 B1\n")
                .append("      LOADR2 B4\n")
                .append("      ADD R1,R2\n")
                .append("      SAVER1 B1\n")
                .append("      END 100\n")
                .toString();
        sourceTextArea.setText(prog);

        tableModelCodesOfOperation.addRow(new Object[] {"JMP", "01", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"LOADR1", "02", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"LOADR2", "03", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"ADD", "04", "2"});
        tableModelCodesOfOperation.addRow(new Object[] {"SAVER1", "05", "4"});
    }

    private void updateText(int index) {
        String[] text = sourceTextArea.getText().split("\n");
        sourceTextArea.setText("");

        StyledDocument doc = sourceTextArea.getStyledDocument();
        Style style = sourceTextArea.addStyle("Style", null);
        StyleConstants.setBackground(style, Color.WHITE);

        for (int i = 0; i < text.length; i++) {
            if (i == index) {
                StyleConstants.setBackground(style, Color.CYAN);
                try{ doc.insertString(doc.getLength(), text[i] + "\n", style); }
                catch (BadLocationException e) {}
                StyleConstants.setBackground(style, Color.WHITE);
            }
            else
                try{ doc.insertString(doc.getLength(), text[i] + "\n", style); }
                catch (BadLocationException e) {}
        }
    }

    private boolean checkValidSymbols(String str) {
        str = str.toLowerCase();
        if ("".equals(str))
            return true;
        char t = str.charAt(0);
        if ((t < 'a') || (t > 'z')) {
            return false;
        }

        for (int i = 1; i < str.length(); i++) {
            t = str.charAt(i);
            if (((t >= 'a') && (t <= 'z')) || (t == '_') || (t == '@') || (t == '$') || ((t >= '0') && (t <= '9'))) {
                continue;
            }
            else
                return false;
        }
        return true;
    }

    private void updateButtonsAndText() {
        firstPassButton.setEnabled(true);
        oneStepButton.setEnabled(true);
        btnAddNewRow.setEnabled(true);
        btnDelRow.setEnabled(true);
        sourceTextArea.setEnabled(true);
        choosingExample.setEnabled(true);
    }

    public static void main(String[] args) {
        new MainWindow();
    }
}
