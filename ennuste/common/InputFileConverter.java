package ennuste.common;

import ennuste.exception.InvalidInputException;

import java.io.*;
import java.util.StringTokenizer;

public class InputFileConverter {
    private String inputFileName;

    public InputFileConverter(String inputFileName) throws IOException, InvalidInputException {
        this.inputFileName = inputFileName;
        convert();
    }

    private void convert() throws IOException, InvalidInputException{
        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter(inputFileName + ".svm"));

        if (reader.toString().length() == 0) {
            throw new InvalidInputException("Invalid input file! File is empty!");
        }

        writer.flush();
        while (true) {
            String line = reader.readLine();

            if (line == null) {
                break;
            }

            // Read up tokens to a double array
            StringTokenizer st = new StringTokenizer(line, ",");
            int index = 0;
            int n = st.countTokens();
            String[] all = new String[n];
            while (st.hasMoreTokens()) {
                String str = st.nextToken().trim();
                all[index] = str;
                index += 1;
            }

            // Write y first
            writer.write(all[n-1] + " ");

            // write x
            for(int i = 0; i < n-1; i ++){
                writer.write(Integer.toString(i) + ":" + all[i] + " ");
            }
            writer.write("\n");
        }
        reader.close();
        writer.close();
    }
}
