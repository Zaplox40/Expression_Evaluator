package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	int counter = 0;
    	String result;
    	
    	for (int i=0; i < expr.length(); i++) {
    		if(Character.isLetter(expr.charAt(i))) {
    			result = FindVariable(expr.substring(i));
    			i += result.length();
    			if( i >= expr.length()) {
    				for(int a = 0; a < vars.size(); a++) {
    					if(result.equals(vars.get(a).name)) {
    						counter=1;
    						break;
    					}
    				}
    				if(counter==1) {
    					counter=0;
    					break;
    				}
    				vars.add(new Variable(result));
    				break;
    			}
    			if(expr.charAt(i) == '[') {
    				for(int b=0; b < arrays.size(); b++) {
    					if(result.contentEquals(arrays.get(b).name)) {
    						counter = 1;
    						break;
    					}
    				}
    				if(counter==1) {
    					counter = 0;
    					continue;
    				}
    				arrays.add(new Array(result));
    			}
    			else {
    				for(int c = 0; c < vars.size(); c++) {
    					if(result.contentEquals(vars.get(c).name)) {
    						counter = 1;
    						break;
    					}
    				}
    				if(counter == 1) {
    					counter = 0;
    					continue;
    				}
    				vars.add(new Variable(result));
    			}
    		}
    	}
    }
    	private static String FindVariable(String str) {
    		int c = 0;
    		String newStr = "";
    		
    		while(Character.isLetter(str.charAt(c))) {
    			newStr += str.charAt(c);
    			c++;
    			
    			if(c >= str.length()) {
    				break;
    			}
    		}
    		return newStr;
    	}
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	// following line just a placeholder for compilation
    	
    	StringTokenizer token = new StringTokenizer(expr, delims, true);
    	Stack<Float> variables = new Stack<>();
    	Stack<String> operators = new Stack<>();
    	
    	while(token.hasMoreTokens()) {
    		
    		String str = token.nextToken();
    		
    		if(str.contentEquals(" ") || str.equals("\t")) {
    		}
    		else if(NumCheck(str)) {
    			variables.push(Float.parseFloat(str));
    			continue;
    		}
    		else if(VarCheck(vars,str) != -1) {
    			variables.push((float)vars.get(VarCheck(vars,str)).value);
    		}
    		else if(str.equals("(")) {
    			operators.push("(");
    		}
    		else if(str.equals(")")) {
    			
    			while(operators.peek() != "(") {
    				float second = variables.pop();
    				float first = variables.pop();
    				String operator = operators.pop();
    				
    				switch(operator) {
    				case "+":variables.push(first + second); continue;
    				case "-":variables.push(first - second); continue;
    				case "*":variables.push(first * second); continue;
    				case "/":variables.push(first / second); continue;
    				}
    			}
    			operators.pop();
    		}
    		else if(str.contentEquals("[")) {
    			operators.push("[");
    		}
    		else if(ArrCheck(arrays,str) != -1) {
    			operators.push(str);
    		}
    		else if(str.equals("]")) {
    			
    			while(operators.peek() != "[") {
    				float second = variables.pop();
    				float first = variables.pop();
    				String operator = operators.pop();
    				
    				switch(operator) {
    				case "+":variables.push(first + second); continue;
    				case "-":variables.push(first - second); continue;
    				case "*":variables.push(first * second); continue;
    				case "/":variables.push(first / second); continue;
    				}
    			}
    			operators.pop();
    			
    			String Array = operators.pop();
    			float Index = variables.pop();
    			variables.push(ValCheck(arrays, Index, Array));
    		}
    		else if(str.equals("+") || str.equals("-") || str.contentEquals("*") || str.equals("/")) {
    			
    			while(operators.isEmpty() != true && OrderOps(str, operators.peek())) {
    				
    				float second = variables.pop();
    				float first = variables.pop();
    				String operator = operators.pop();
    				
    				switch(operator) {
    				case "+":variables.push(first + second); continue;
    				case "-":variables.push(first - second); continue;
    				case "*":variables.push(first * second); continue;
    				case "/":variables.push(first / second); continue;
    				}
    			}
    			operators.push(str);
    		}
    	}
    	
    	while(!operators.isEmpty()) {
    		
    		float second = variables.pop();
    		float first = variables.pop();
    		String operator = operators.pop();
    		
    		switch(operator) {
			case "+":variables.push(first + second); continue;
			case "-":variables.push(first - second); continue;
			case "*":variables.push(first * second); continue;
			case "/":variables.push(first / second); continue;
			}
    	}
    	return variables.pop();
    }
    
    private static boolean NumCheck(String str) {
    	
    	try
    	{
    		Integer.parseInt(str);
    		return true;
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    }
    
    private static boolean OrderOps(String str, String TopStr) {
    	
    	if((str.equals("*") || str.equals("/")) && (TopStr.equals("+") || TopStr.equals("-"))) {
    		return false;
    	}
    	if(TopStr.equals("(")) {
    		return false;
    	}
    	else if (TopStr.equals("[")) {
    		return false;
    	}
    	else { return true; }   	
    }
    
    private static int VarCheck(ArrayList<Variable> vars, String str) {
    	
    	for(int i=0; i < vars.size(); i++) {
    		
    		if(vars.get(i).name.equals(str)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    private static float ValCheck(ArrayList<Array> arrays, float index, String str) {
    	
    	int Index = (int)index;
    	
    	for(int i=0; i< arrays.size(); i++) {
    		
    		if(arrays.get(i).name.equals(str)) {
    			
    			Array arr = arrays.get(i);
    			return (float)arr.values[Index];
    		}
    	}
    	return -1;
    }
    
    private static int ArrCheck(ArrayList<Array> arrays, String str) {
    	
    	for (int i=0; i < arrays.size(); i++) {
    		
    		if(arrays.get(i).name.equals(str)) {
    			return i;
    		}
    	}
    	return -1;
    }
}


