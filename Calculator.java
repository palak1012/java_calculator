package pack;

import java.util.Scanner;
import java.util.Stack;

public class Calculator {

    public static void main(String[] args) {
        // Creating scanner object
        Scanner sc = new Scanner(System.in);

        while (true) {
            // Taking input from user
            System.out.println("Enter a mathematical expression or type 'e' to quit:");
            String input = sc.nextLine();

            // To exit the program
            if (input.equalsIgnoreCase("e")) {
                System.out.println("Exiting the program!");
                break;
            }

            // Check if the expression contains increment or decrement operator
            try {
                if (containsUnaryOperation(input)) {
                    int result = UnaryOperation(input);
                    System.out.println("Result: " + result);
                } else {
                    // to add * where there is direct "(", like 10(2+3)
                    input = process_Expression(input); 
                    if (isValidExpression(input)) {
                        double result = evaluateExpression(input);

                        // If the result is an integer, print it as an integer; otherwise, print as double
                        if (result == (int) result) {
                            System.out.println("Result: " + (int) result);
                        } else {
                            System.out.println("Result: " + result);
                        }
                    }
                }
            } 
            //to catch exception
            catch (IllegalArgumentException | ArithmeticException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        // Close the scanner
        sc.close();
    }

    // Method to check if the input contains a unary operation (e.g., 10++, 10--)
    public static boolean containsUnaryOperation(String input) {
        // Check if the input ends with '++' or '--' and the rest of the input is a valid number
        if (input.length() > 2 && (input.endsWith("++") || input.endsWith("--"))) {
            String num = input.substring(0, input.length() - 2);
            return isNumber(num);
        }
        return false;
    }

    // Method to solve unary increment and decrement operation
    public static int UnaryOperation(String input) {
        String num = input.substring(0, input.length() - 2); // to Get the number part
        int number = Integer.parseInt(num); // Converting the number part to integer

        if (input.endsWith("++")) {
            return number + 1;
        } else if (input.endsWith("--")) {
            return number - 1;
        }
        throw new IllegalArgumentException("Invalid unary operation.");
    }

    // Method to check if a string is a valid number
    public static boolean isNumber(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (char ch : input.toCharArray()) {
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    //check the input string and process it accordingly
    public static String process_Expression(String input) {
    	//here I used StringBuilder as we need to make changes in the string and in java, string is immutable
        StringBuilder processed = new StringBuilder();
        char prevChar = ' ';

        // to handle the case where expression starts with an operator
        if (input.startsWith("-") || input.startsWith("+") || input.startsWith("*") || input.startsWith("/")) {
            processed.append("0");
        }

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            // To Insert '*' between a number and a '(' or between ')' and a number
            if ((Character.isDigit(prevChar) && ch == '(') || (prevChar == ')' && Character.isDigit(ch))) {
                processed.append('*');
            }

            // To solve -- as +
            if (prevChar == '-' && ch == '-') {
                processed.deleteCharAt(processed.length() - 1); // Remove the previous -
                processed.append('+');
            }
            // ++ as addition
            else if (prevChar == '+' && ch == '+') {
                processed.deleteCharAt(processed.length() - 1); // Remove the previous +
                processed.append('+');
            }
            // +- as -
            else if (prevChar == '+' && ch == '-') {
                processed.deleteCharAt(processed.length() - 1); // Remove the previous +
                processed.append('-');
            }
            // -+ as -
            else if (prevChar == '-' && ch == '+') {
                processed.deleteCharAt(processed.length() - 1); // Remove the previous -
                processed.append('-');
            }
            // Append the current character
            else {
                processed.append(ch);
            }
            prevChar = ch;
        }

        if (prevChar == '+' || prevChar == '-') {
            processed.append("0");
        }

        return processed.toString();
    }

    // Method to validate the expression
    public static boolean isValidExpression(String input) {
        // Check for invalid characters
        if (!containsValidCharacters(input)) {
            throw new IllegalArgumentException("Invalid characters found in the expression.");
        }

        // Check for balanced parentheses
        if (!areParenthesesBalanced(input)) {
            throw new IllegalArgumentException("Unbalanced parentheses in the expression.");
        }

        return true;
    }

    // Method to check if the input contains only valid characters (digits, operators, parentheses, and decimal points)
    public static boolean containsValidCharacters(String input) {
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            // Check if the character is not a digit, operator, parentheses, or decimal point
            if (!Character.isDigit(ch) && !isOperator(Character.toString(ch)) && ch != '(' && ch != ')' && ch != '.') {
                return false; // Found an invalid character
            }
        }
        return true; // All characters are valid
    }

    // Method to check if parentheses are balanced using stack
    public static boolean areParenthesesBalanced(String input) {
        Stack<Character> stack = new Stack<>();
        for (char ch : input.toCharArray()) {
            if (ch == '(') {
                stack.push(ch);
            } else if (ch == ')') {
                if (stack.isEmpty() || stack.pop() != '(') {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }
    
    // to evaluate the expression using BODMAS principle by stack
    public static double evaluateExpression(String input) {
        input = input.replaceAll("\\s+", ""); // Remove whitespace

        //creating stack, one for characters and one for numbers
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int len = input.length();

        for (int i = 0; i < len; i++) {
            char ch = input.charAt(i);

            // If the current character is a digit or a decimal point, parse the number
            if (Character.isDigit(ch) || ch == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < len && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    sb.append(input.charAt(i));
                    i++;
                }
                values.push(Double.parseDouble(sb.toString()));
                i--;
            } 
            // If the current character is '(', push it to the operators stack
            else if (ch == '(') {
                operators.push(ch);
            } 
            // If the current character is ')', solve the entire bracket
            else if (ch == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop(); // Remove the '(' from the stack
            } 
            // If the current character is an operator
            else if (isOperator(Character.toString(ch))) {
                while (!operators.isEmpty() && hasPrecedence(ch, operators.peek())) {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(ch);
            }
        }

        // calculating the remaining operations in the stack
        while (!operators.isEmpty()) {
            values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

 // Method to apply the operator to two numbers
    public static double applyOperation(char operator, double b, double a) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero is not allowed.");
                }
                return a / b;
            case '^': // Power operation
                return Math.pow(a, b);
            default:
                throw new IllegalArgumentException("Invalid operator.");
        }
    }

    // Method to check if a string is a valid operator
    public static boolean isOperator(String str) {
        return str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/") || str.equals("^");
    }

    // Method to check operator precedence    
    public static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if (op1 == '^') {
            return false;
        }
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false; // * and / have higher precedence than + and -
        } else {
            return true;
        }
    }

}

