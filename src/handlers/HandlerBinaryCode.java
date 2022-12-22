package handlers;

import exceptions.OverflowException;
import exceptions.ProgramException;
import exceptions.UnknownCommandException;
import models.Operation;
import models.PseudoCommand;
import models.SymbolicName;
import tables.OpcodeTableSingleton;
import tables.RecordingTableSingleton;
import tables.SymbolicNamesTableSingleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class HandlerBinaryCode {
    private RecordingTableSingleton recordingTable;
    private SymbolicNamesTableSingleton symbolicNamesTable;
    private OpcodeTableSingleton opcodeTable;
    private ArrayList<String> listCommands;
    private ArrayList<String> listRegistr;
    private HashMap<Integer, String> mapOperands;
    private String text;
    private String nameOfProgram;
    private String startAddress;
    private String endAddress;
    private String currentAddress;
    private String globalAddress;
    private String command;
    private String[] str;
    private boolean hasNext;


    public HandlerBinaryCode(String text) {
        this.text = text.trim();
        //Разбиение по строкам
        str = text.split("\n");

        recordingTable = RecordingTableSingleton.getInstance();
        symbolicNamesTable = SymbolicNamesTableSingleton.getInstance();
        opcodeTable = OpcodeTableSingleton.getInstance();
        mapOperands = new HashMap<>();
        listRegistr = new ArrayList<>();
        listRegistr.add("");
        for (int i = 0; i < 16; i++)
            listRegistr.add("R" + i);
        listCommands = new ArrayList<>(Arrays.asList("start", "end", "word", "byte", "resb", "resw"));
        hasNext = true;
    }

    public void getOneStep(int index) {
        if (index >= str.length) {
            callException(index - 1, "Отстутствует конец программы");
            return;
        }
        if (index == 0) {
            //Первая строка
            str[0] = str[0].trim();
            command = str[0];
            String[] arg = str[0].split(" ");

            nameOfProgram = arg[0].trim();
            if (arg.length != 3)
                callException(0, "Неверное количество комманд.");

            if (!"start".equals(arg[1].trim().toLowerCase()))
                callException(0, "Отсутствует начало программы.");
            else
                listCommands.remove("start");

            startAddress = arg[2].replaceFirst("^0+(?!$)", "");
            globalAddress = arg[2].replaceFirst("^0+(?!$)", "");
            checkAddress(startAddress, 0);

            PseudoCommand.setStartAddress(startAddress);

            String start = "0".repeat(Math.max(0, 6 - startAddress.length())) + startAddress;
            recordingTable.addRecord("H", nameOfProgram, start, "?");
            return;
        }

        str[index] = str[index].trim();
        if ("".equals(str[index]))
            return;
        command = str[index];
        String metka = null;
        String operation = null;
        String[] operands = new String[0];
        String operand1;
        String operand2;
        //Разбиение на метку, МКОП, операнды
        try {
            metka = defineMetka();
            operation = defineCommand();
            operands = defineOperands();
        } catch (UnknownCommandException e) {
            callException(index, e.getMessage());
        }
        operand1 = operands[0];
        operand2 = operands[1];

        if (!checkValidSymbols(metka))
            throw new ProgramException(index, "Некорректное имя метки: " + metka);
        if (!checkValidSymbols(operation))
            throw new ProgramException(index, "Некорректное имя команды: " + operation);

        //обработка метки
        if (!"".equals(metka)) {
            try {
                symbolicNamesTable.addNewName(metka, globalAddress);
            } catch (UnknownCommandException e) {
                callException(index, e.getMessage());
            }
        }

        String obj = "";
        //поиск в ТКО
        if (opcodeTable.checkCommand(operation)) {
            int addr = Integer.parseInt(currentAddress, 16) + opcodeTable.getLen(operation);
            checkAddress(Integer.toHexString(addr), index);
            int code;

            int typeOfAddr;
            if ("".equals(operand2))
                typeOfAddr = getType(index, operand1);
            else
                typeOfAddr = getType(index, operand2);
            //Получение кода операции
            code = opcodeTable.getBinaryCode(operation) * 4 + typeOfAddr;

            String newStr = Integer.toHexString(code).toUpperCase();
            if (newStr.length() % 2 != 0)
                newStr = "0" + newStr;
            opcodeTable.getOpcodeTable().put(newStr, opcodeTable.getOpcodeTable().get(operation));

            obj = generateObjectModule(index, operation, newStr, operand1, operand2);

        } else if (listCommands.contains(operation.toLowerCase())) {
            PseudoCommand pseudoCommand = null;
            PseudoCommand.setCurrentAddress(globalAddress);
            try {
                pseudoCommand = new PseudoCommand(operation, operand1);
            } catch (UnknownCommandException | OverflowException e) {
                callException(index, e.getMessage());
            }

            int addr = Integer.parseInt(currentAddress, 16) + pseudoCommand.getLen();
            checkAddress(Integer.toHexString(addr), index);

            if ("end".equals(operation.toLowerCase())) {
                if (!metka.isEmpty() || !metka.isBlank()) {
                    callException(index, "Метка перед END");
                }
                listCommands.remove("end");
                hasNext = false;

                //проверка после end
                if (index + 1 < str.length)
                    callException(index + 1, "Команды после END.");

                //проверка перед end
                if (!"".equals(metka))
                    callException(index, "Для команды END нельзя объявить метку.");

                //обработка начального и конечного адреса
                endAddress = "0".repeat(Math.max(0, 6 - currentAddress.length())) + currentAddress;
                String start = "0".repeat(Math.max(0, 6 - startAddress.length())) + startAddress;
                int st = Integer.parseInt(start, 16);
                int en = Integer.parseInt(endAddress, 16);
                String len = Integer.toHexString(en - st);
                recordingTable.addRecord("H", nameOfProgram, start, "0".repeat(Math.max(0, 6 - len.length())) + len);

                //Проверка на отсутствие адреса СИ
                HashMap<String, SymbolicName> hashMap = symbolicNamesTable.getSymbolicNames();
                for (String elem : hashMap.keySet()) {
                    if ("".equals(hashMap.get(elem).getAddressName()))
                        throw new RuntimeException("Отсутствует объявление метки: " + elem);
                }

                //если адрес пуст
                if ("".equals(operand1)) {
                    recordingTable.addRecord("E", "0".repeat(Math.max(0, 6 - startAddress.length())) + startAddress, "", "");
                    return;
                }
                //если не пуст
                try {
                    int val = Integer.parseInt(operand1, 16);
                    if (val > Integer.parseInt(endAddress, 16)) {
                        throw new ProgramException("Неправильный адрес точки входа.");
                    }
                    if (val < Integer.parseInt(startAddress, 16)) {
                        throw new ProgramException("Неправильный адрес точки входа.");
                    }
                    String end = Integer.toHexString(val);
                    recordingTable.addRecord("E", "0".repeat(Math.max(0, 6 - end.length())) + end, "", "");
                    return;
                } catch (Exception ex) {
                    throw new ProgramException("Неправильный адрес точки входа.");
                }
            }

            obj = generateObjectModule(index, operation, "", operand1, operand2);

        } else
            callException(index, "Неизвестная команда: " + operation);

        recordingTable.addRecord("T", "0".repeat(Math.max(0, 6 - globalAddress.length())) + globalAddress, obj);
        globalAddress = currentAddress;
    }


    private String generateObjectModule(int index, String operation, String newStr, String operand1, String operand2) {
        //Запись в объектный модуль
        if (("RESB".equals(operation)) || ("RESW".equals(operation))) {
            return "";
        }

        if (("WORD".equals(operation)) || ("BYTE".equals(operation))) {
            String str;
            if (operand1.contains("x"))
                str = operand1.substring(2, operand1.length() - 1);
            else if (operand1.contains("c")) {
                str = operand1.substring(2, operand1.length() - 1);
                StringBuilder answer = new StringBuilder();
                for (int i = 0; i < str.length(); i++) {
                    int code = str.charAt(i);
                    answer.append(Integer.toHexString(code));
                }
                str = answer.toString();
            } else {
                int code;
                if (operand1.contains("h"))
                    code = Integer.parseInt(operand1.substring(0, operand1.length() - 1), 16);
                else
                    code = Integer.parseInt(operand1);

                str = Integer.toHexString(code);
            }
            if ("WORD".equals(operation)) {
                if (str.length() % 3 != 0)
                    str = "0".repeat(Math.max(0, 3 - str.length() % 3)) + str;
            } else {
                if (str.length() % 2 != 0)
                    str = "0" + str;
            }
            return str;
        }

        String output = newStr;
        //отсутствует второй операнд
        if ("".equals(operand2)) {
            if (opcodeTable.getLen(operation) != 1) {
                if ("".equals(operand1))
                    callException(index, "Операнд не может быть пустым для команды: " + operation);
                if (opcodeTable.getLen(operation) == 2) {
                    int number = -1;
                    try {
                        number = Integer.parseInt(operand1);
                    } catch (Exception ex) {
                        callException(index, "Некорректное значение операнда.");
                    }
                    if (number > 255)
                        callException(index, "Переполнение памяти, некорректное значение операнда.");
                }
                try {
                    output += handleOperand(operation, symbolicNamesTable.getSymbolicNames(), operand1);
                } catch (Exception ex) {
                    callException(index, ex.getMessage());
                }
            } else {
                if (!"".equals(operand1)) {
                    callException(index, "Операнд не может иметь значения для команды: " + operation);
                }
            }
        }
        //операнд 2 имеет какое-то значение
        else {
            //проверка на регистр операнда 1
            if (listRegistr.contains(operand1)) {
                String str = operand1.substring(operand1.indexOf('R') + 1);

                output += str;
            } else
                callException(index, "Некорректное значение 1 операнда: " + operand1);

            try {
                output += handleOperand(operation, symbolicNamesTable.getSymbolicNames(), operand2);
            } catch (Exception ex) {
                callException(index, ex.getMessage());
            }
        }
        return output;
    }

    private String handleOperand(String command, HashMap<String, SymbolicName> symbolicNames, String operand1) throws UnknownCommandException {
        if (symbolicNames.containsKey(operand1)) {
            SymbolicName sName = symbolicNames.get(operand1);
            if ("".equals(sName.getAddressName())) {
                symbolicNamesTable.addAddressToName(operand1, globalAddress);
                return "#" + operand1 + "#";
            } else {
                return sName.getAddressName();
            }
        } else if (listRegistr.contains(operand1)) {
            OpcodeTableSingleton opcodeTable = OpcodeTableSingleton.getInstance();
            Operation operation = opcodeTable.getOpcodeTable().get(command);
            if (operation.getLen() == 4)
                throw new UnknownCommandException("Для данной команды операнд должен быть меткой: " + command);

            String str = operand1.substring(operand1.indexOf('R') + 1);
            int num = Integer.parseInt(str);
            return Integer.toHexString(num);
        } else {
            if ("".equals(operand1)) {
                return "";
            }

            OpcodeTableSingleton opcodeTable = OpcodeTableSingleton.getInstance();
            Operation operation = opcodeTable.getOpcodeTable().get(command);
            int num;
            try {
                num = Integer.parseInt(operand1);
                if (operation.getLen() == 4)
                    throw new UnknownCommandException("Для данной команды операнд должен быть меткой: " + command);
            } catch (Exception ex) {
                symbolicNamesTable.addAddressToName(operand1, globalAddress);
                return "#" + operand1 + "#";
            }

            return Integer.toHexString(num);
        }
    }

    private void checkAddress(String address, int i) {
        long code = 0;
        try {
            code = Long.parseLong(address, 16);
        } catch (Exception ex) {
            callException(i, "Некорректный адрес: " + address);
        }
        if (code > Integer.parseInt("FFFFFF", 16))
            callException(i, "Переполнение: " + Long.toHexString(code));
        if (code <= 0)
            callException(i, "Некорректный адрес: " + address);
        currentAddress = address;
    }

    private int getType(int ind, String operand) {
        if (listRegistr.contains(operand))
            return 0;
        try {
            if (operand.contains("h"))
                Integer.parseInt(operand, 16);
            else
                Integer.parseInt(operand);
            return 0;
        } catch (Exception ex) {
            mapOperands.put(ind, operand);
            return 1;
        }
    }

    private String defineMetka() throws UnknownCommandException {
        String metka = "";
        //Разбиение на метку, МКОП, операнды
        if (command.contains(":")) {
            String[] arr = command.split(":");
            metka = arr[0];
            if ("".equals(metka))
                throw new UnknownCommandException("Отстутствует название метки");
            command = command.replaceFirst(metka, "").trim();
            command = command.substring(1).trim();
        }
        if (command.contains(":")) {
            if (command.contains("c")) {
                if (!((command.indexOf(":") > command.indexOf("c") + 1) && (command.indexOf(":") < command.lastIndexOf("'"))))
                    throw new UnknownCommandException("Некорректное имя метки: " + metka + command.substring(0, command.indexOf(":") + 1));
            }
        }
        if (!"".equals(metka) && listRegistr.contains(metka))
            throw new UnknownCommandException("Некорректное имя метки: " + metka + ". Имя зазезервировано.");
        return metka;
    }

    private String defineCommand() throws UnknownCommandException {
        String operation = command.split(" ")[0];
        if (command.contains(",")) {
            if ((operation.length() > 3) &&
                    (operation.substring(0, 4).equals("LOAD") || operation.substring(0, 4).equals("SAVE")))
                throw new UnknownCommandException("Неверное количество операндов.");
        }
        command = command.replace(operation, "").trim();
        //command = command.substring(command.indexOf(' ') + 1).trim();
        return operation;
    }

    private String[] defineOperands() throws UnknownCommandException {
        String[] operands = new String[2];

        if (command.contains(",")) {
            operands[0] = command.substring(0, command.indexOf(','));
            command = command.substring(command.indexOf(',') + 1).trim();

            operands[1] = handleKovichki();

            if (!checkValidOperand1(operands[0]))
                throw new UnknownCommandException("Недопустимое значение 1 операнда: " + operands[0]);
        } else {
            operands[0] = handleKovichki();
            operands[1] = "";
        }

        return operands;
    }

    private boolean checkValidOperand1(String operand) {
        return listRegistr.contains(operand.toUpperCase());
    }

    public String getText() {
        return text;
    }

    private void callException(int i, String message) {
        throw new ProgramException(i + 1, message);
    }

    public String getNameOfProgram() {
        return nameOfProgram;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getEndAddress() {
        return endAddress;
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
            } else
                return false;
        }
        return true;
    }

    private String handleKovichki() throws UnknownCommandException {
        int begin = command.indexOf('\'');
        int end = command.lastIndexOf('\'');

        StringBuilder stringBuilder = new StringBuilder();
        if ((begin != -1) && (end != begin)) {
            for (int i = 0; i < command.length(); i++) {
                char t = command.charAt(i);
                if ((i > begin) && (i < end))
                    stringBuilder.append(t);
                else if ((t == ' ') || (t == ','))
                    throw new UnknownCommandException("Ошибка при обработке операндов.");
                else
                    stringBuilder.append(t);
            }
            return stringBuilder.toString();
        } else if ((begin != -1) && (end == begin)) {
            throw new UnknownCommandException("Некорректное значение операнда: " + command);
        } else {
            if ((command.contains(" ")) || (command.contains(",")))
                throw new UnknownCommandException("Неверное количество операндов");
            return command;
        }
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
