package com.diploma.stegovector.objects;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private String text;
    private List<String> binaryMessage;

    public Message() {
    }

    public Message(String text) {
        this.text = text;
    }

    public Message(String text, int binaryBlockSize) {
        this.text = text;
        this.binaryMessage = convertToBinaryBlocks(binaryBlockSize);
    }

    public void setBinaryMessage(List<String> listOfBinary) {this.binaryMessage = listOfBinary;}

    public void setBinaryMessage(int blockSize) {
        this.binaryMessage = convertToBinaryBlocks(blockSize);
    }

    public List<String> getBinaryMessage() {
        return binaryMessage;
    }

    public String getText() {
        return text;
    }

    public void setText(List<String> binaryBlocks) {
        this.text = convertToString(binaryBlocks);
    }

    public String convertToString(List<String> binaryBlocks) {
        String text = "";
        if (binaryBlocks.get(0).length() > 8){
            String joined = String.join("", binaryBlocks);
//            List<String> chunked = new ArrayList<>();
            List<String> chunked = trimString(joined, 8);
//            for (int i = 0; i < binaryBlocks.size(); i++) {
//                List<String> chunkedBlock = trimString(binaryBlocks.get(i), 8);
//                for (int j = 0; j < chunkedBlock.size(); j++) {
//                    chunked.add(chunkedBlock.get(j));
//                }
//            }
            for (int i = 0; i < chunked.size(); i++) {
                text+= binaryToText(chunked.get(i));
            }
        } else {
            for (int i = 0; i < binaryBlocks.size(); i++) {
                text += binaryToText(binaryBlocks.get(i));
            }
        }

        return text;
    }
    private List<String> trimString(String string, int trimSize) {
        List<String> splitList = List.of(string.split(""));
        List<String> trimmed = new ArrayList<>();
        for (int i = 0; i < splitList.size(); i += trimSize) {
            trimmed.add(String.join("", splitList.subList(i, i + trimSize)));
        }
        return trimmed;
    }

    private String binaryToText(String binaryText) {
        String[] binaryNumbers = binaryText.split(" ");
        String text = "";

        for (String currentBinary : binaryNumbers) {
            int decimal = binaryToDecimal(currentBinary);
            char letra = (char) decimal;
            text += letra;
        }
        return text;
    }

    private int binaryToDecimal(String binary) {
        int decimal = 0;
        int position = 0;
        for (int x = binary.length() - 1; x >= 0; x--) {
            short digit = 1;
            if (binary.charAt(x) == '0') {
                digit = 0;
            }
            double multiplier = Math.pow(2, position);
            decimal += digit * multiplier;
            position++;
        }
        return decimal;
    }

    private List<String> convertToBinaryBlocks(int blockSize) {
        List<String> binaryLetters = new ArrayList<>();
        char[] chars = this.text.toCharArray();
        for (char c : chars) {
            String binary = Integer.toBinaryString(c);
            String add = "";
            if (binary.length() < 8) {
                int flag = binary.length();
                while (flag < 8) {
                    add += '0';
                    flag++;
                }
            }
            binaryLetters.add(add.concat(binary));
        }

        List<String> res = new ArrayList<>();

        int marker = blockSize / 8;
        for (int i = 0; i < binaryLetters.size(); i += marker) {
            List<String> sublist;
            if (i + marker <= binaryLetters.size()) {
                sublist = binaryLetters.subList(i, i + marker);
            } else {
                String appendix = "00000000";
                int lastIndexOf = binaryLetters.size() - (binaryLetters.size() % marker);
                List<String> temp = new ArrayList<>(binaryLetters.subList(lastIndexOf, binaryLetters.size()));
                while (temp.size() < marker) {
                    temp.add(appendix);
                }
                sublist = temp;
            }
            res.add(String.join("", sublist));
        }
        return res;
    }

}