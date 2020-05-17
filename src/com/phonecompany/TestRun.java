package com.phonecompany;

import com.phonecompany.billing.TelephoneBillCalculatorCSV;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestRun {

    public static void main(String[] args) {
        String csv = getCSVString();
        TelephoneBillCalculatorCSV calc = new TelephoneBillCalculatorCSV();
        BigDecimal price = calc.calculate(csv);
        System.out.println(price);
    }

    public static String getCSVString() {
        // priprava test stringu pro volani vypoctu
        StringBuilder sb = new StringBuilder();
        String row;
        String dir = System.getProperty("user.dir");
        Path path = Paths.get(dir, "src/testCsv.csv");
        try {
            BufferedReader bf = new BufferedReader(new FileReader(path.toFile()));
            while ((row = bf.readLine()) != null) {
                sb.append(row).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }
}
