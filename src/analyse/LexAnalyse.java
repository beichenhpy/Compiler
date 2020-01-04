package analyse;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import entity.Words;
import entity.TypeWord;
import entity.MyError;

/*
 * �ʷ�����
*/
public class LexAnalyse {
	public ArrayList<Words> wordsList = new ArrayList<Words>(); //���ʱ�
	public static ArrayList<TypeWord> typeList = new ArrayList<TypeWord>(); //��������
	public ArrayList<MyError> myErrorList = new ArrayList<MyError>();// ������Ϣ�б�
	
	public boolean lexErrorFlag = false; //�жϴʷ������Ƿ����ı�־
	public boolean noteFlag = false; //����ע�͵ı�־
	public int wordCount = 0; //ͳ�Ƶ��ʵĸ���
	public int errorCount = 0;// ͳ�ƴ������
	public Stack<String> typestack = new Stack<String>();// ����ջ
	
	private ArrayList<String> lex_result_stack;//��¼�����ķ��Ŵ�
	
	//���췽��
	public LexAnalyse() {
		lex_result_stack = new ArrayList<String>();
	}
	public LexAnalyse(String str) {
		lexAnalyse(str);
	}	
	
	//�жϵ����Ƿ�Ϊ��������
	private static boolean isDigit(char ch) {
		boolean isDigit = false;
		if('0' <= ch && ch <= '9') {
			isDigit = true;
		}
		return isDigit;
	}
	
	//�жϵ����Ƿ�Ϊ����
	private static boolean isInteger(String word) {
		boolean isInteger = false;
		int i = 0;
		for(i = 0; i < word.length(); i++) {
			if(Character.isDigit(word.charAt(i))) { //�ж��ַ��Ƿ�Ϊ����
				continue;
			} else {
				break;
			}
		}
		if (i == word.length()) {
			isInteger = true;
		}
		return isInteger;
	}
	
	//�жϵ����Ƿ�Ϊ������
	private static boolean isFloat(String word) {
		boolean isFloat = false;
		int i, j = 0 ;
		for (i = 0; i <= word.length(); i++) {
			if(Character.isDigit(word.charAt(i))) {
				continue;
			} else if (word.charAt(i) == '.') {
				if (i < word.length() - 1) { //�������ַ��������ڶ�λ֮ǰ��.С���㣬������ж�
					j ++;
				}
				continue;
			} else {
				break;
			}
		}
		if(i == word.length() && j == 1) { //С����ֻ��һλ
			isFloat = true;
		}
		return isFloat;
	}
		
	//�жϵ����Ƿ�Ϊ�ַ�������
	private static int isString(String word) {
		int i = 0;
		char temp = word.charAt(0);
		if(temp == '\"') {
			for (i = 1; i < word.length(); i++) {
				temp = word.charAt(i);
				if(0 <= temp && temp <= 255 && temp != 34){
					continue;
				} else {
					break;
				}
			}
			if (temp == 34) {
				return i;
			}
		}
		return -1;
	}
	
	//�жϵ����Ƿ�Ϊ��ĸ
	private static boolean isLetter(char ch) {
		boolean isLetter = false;
		if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z')) {
			isLetter = true;
		}
		return isLetter;
	}
	
	//�жϵ����Ƿ�Ϊ�Ϸ���ʶ��
	private static boolean isID(String word) {
		boolean isID = false;
		int i = 0;
		if(Words.isKey(word)) {
			isID = true;
		}
		char temp = word.charAt(i);
		if(isLetter(temp) || temp == '_') { //���»��ߺ���ĸ��ͷ
			for(i = 1; i < word.length(); i++) {
				temp = word.charAt(i);
				if(isLetter(temp) || temp == '_' || isDigit(temp)) {
					continue;
				} else {
					break;
				}
				
			}
			if(i >= word.length()) {
				isID = true;
			}
		}
		return isID;
	}
	
	//�жϴʷ������Ƿ���� 
	public boolean isFail() {
		return lexErrorFlag;
	}
	
	//�ʷ���������
	public void analyse(String str, int line) {
		int beginIndex; //��ʼ���
		int endIndex; //�������
		int index = 0;
		int length = str.length();//�ַ�������
		TypeWord typeword = null ;
		Words word = null;
		MyError myError;
		
		char temp;
		while(index < length) {
			temp = str.charAt(index);
			if(!noteFlag) {
				if(isLetter(temp) || temp == '_') { //�ж��Ƿ�Ϊ��־��
					beginIndex = index;
					index ++;
					while((index < length) 
							&& (!Words.isBoundarySign(str.substring(index,index+1))) 
							&& (!Words.isOperator(str.substring(index, index+1)))
							&& (str.charAt(index) != '\t')
							&& (str.charAt(index) != ' ')
							&& (str.charAt(index) != '\n')
							&& (str.charAt(index) != '\r')) {
						index ++;
					}
					endIndex = index;
					word = new Words();
					wordCount ++;
					word.id = wordCount;
					word.line = line;
					word.value = str.substring(beginIndex,endIndex);
					if(word.value.equals("stdio")) {
						index = index + 2;
						word.value = str.substring(beginIndex, endIndex);
					}
					if(Words.isKey(word.value)) { //������Ϊ�ؼ��֣��������ͼ�ΪKEY
						word.type = Words.KEY;
						if(word.value.equals("int") || word.value.equals("float") || word.value.equals("char") || word.value.equals("string")) {
							typestack.push(word.value); //��int��float��char��string���ͷ�������ջ��
						}
					} else if (isID(word.value)) {
						word.type = Words.IDENTIFIER; //��ʶ��
						if(typestack.size() > 0) {
							word.attribute = typestack.lastElement(); //��ȡջ��Ԫ��
//							if(typestack.size() > 0) {
//								word.attribute = typestack.lastElement();
//							}
							//�жϱ�ʶ�������ԣ���ջ��ȡ���ñ�ʶ����Ӧ������
							if(word.attribute.equals("int") || word.attribute.equals("float") || word.attribute.equals("char") || word.attribute.equals("string")) {
								typeword = new TypeWord();
								typeword.value = word.value;
								typeword.type = word.attribute;
								typeList.add(typeword);
							}
						}
					}
					else {
						word.type = Words.UNDEFI; //δ��������
						word.flag = false;
						errorCount ++;
						myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
					index --;
				} else if(isDigit(temp)) { //�ж��Ƿ�Ϊint��float��
					beginIndex = index;
					index ++;
					while((index < length) 
							&& ((!Words.isBoundarySign(str.substring(index, index + 1))) 
									&& ((!Words.isOperator(str.substring(index, index + 1))) || (str.charAt(index) == ('.'))) 
									&& (str.charAt(index) != ' ')
									&& (str.charAt(index) != '\n') && (str.charAt(index) != '\t')
									&& (str.charAt(index) != '\t') && (str.charAt(index) != '\r'))) {
						index ++;
						
					}
					endIndex = index;
					word = new Words();
					wordCount ++;
					word.id = wordCount;
					word.line = line;
					word.value = str.substring(beginIndex, endIndex);
					if(isInteger(word.value) || isFloat(word.value)) {
						if(isInteger(word.value)) {
							word.type = Words.INT_CONST; //����
						} else {
							word.type = Words.FLOAT_CONST; //������
						}
					} else {
						word.type = Words.UNDEFI; //δ����
						word.flag = false;
						errorCount ++;
						myError = new MyError(errorCount, "�Ƿ���ʶ��", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
					index --;
				} else if(str.charAt(index) == '\"' && wordsList.get(wordsList.size()-1).value.equals("=")) { //�ж��ַ����ͳ���
					beginIndex = index;
					if(index < length && (endIndex=isString(str.substring(beginIndex, str.length() - 1)))!=-1) {
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, beginIndex+endIndex+1);
						word.type = Words.STRING_CONST; //�ַ�����
						/*index = str.length() - 2;
						if(index < length && str.charAt(index) == '\"') {
							word = new Words();
							wordCount ++;
							word.id = wordCount;
							word.line = line;
							word.value = str.substring(beginIndex, index + 1);
							word.type = Words.STRING_CONST; //�ַ�����
						} else {
							endIndex = index;
							word = new Words();
							wordCount ++;
							word.id = wordCount;
							word.line = line;
							word.value = str.substring(beginIndex, endIndex);
							word.type = Words.UNDEFI;
							word.flag = false;
							errorCount ++;
							myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
							myErrorList.add(myError);
							lexErrorFlag = true;
						}*/
					} else {
						endIndex = str.length() - 2;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.UNDEFI;
						word.flag = false;
						errorCount ++;
						myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
					index = beginIndex+endIndex;
				} else if(str.charAt(index) == '\'') { //char�ַ���
					beginIndex = index;
					index ++;
					temp = str.charAt(index);
					if(index < length && temp>=0 && temp<=255) {
						index ++;
						if(index < length && str.charAt(index) == '\'') {
							word = new Words();
							wordCount ++;
							word.id = wordCount;
							word.line = line;
							word.value = str.substring(beginIndex, index + 1);
							word.type = Words.CHAR_CONST; //�ַ���
						} else {
							endIndex = index;
							word = new Words();
							wordCount ++;
							word.id = wordCount;
							word.line = line;
							word.value = str.substring(beginIndex, endIndex);
							word.type = Words.UNDEFI;
							word.flag = false;
							errorCount ++;
							myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
							myErrorList.add(myError);
							lexErrorFlag = true;
						}
					} else {
						endIndex = index;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.UNDEFI;
						word.flag = false;
						errorCount ++;
						myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
					//index --;
				} else if (str.charAt(index) == '\"' && !wordsList.get(wordsList.size()-1).value.equals("=")) { //scanf��printf��ʽ�ж�
					beginIndex = index;
					index ++;
					//System.out.println(index);
					temp = str.charAt(index);
					while (index < length) {
						if(str.charAt(index) == '%') { //"%d","%c","%f"Ϊ�ؼ���
							index ++;
							if(index < length 
									&& str.charAt(index) == 'd' | str.charAt(index) == 'c' | str.charAt(index) == 'f') {
								index ++;
								if(str.charAt(index) == '"') {
									endIndex = index;
									word = new Words();
									wordCount ++;
									word.id = wordCount;
									word.line = line;
									word.value = str.substring(beginIndex, index + 1);
									word.type = Words.KEY;
									break;
								}
							} else {
								endIndex = index;
								word = new Words();
								wordCount ++;
								word.id = wordCount;
								word.line = line;
								word.value = str.substring(beginIndex, index);
								word.type = Words.UNDEFI;
								word.flag = false;
								errorCount ++;
								myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
								myErrorList.add(myError);
								lexErrorFlag = true;
								break;
							}
						} else if (isDigit(str.charAt(index)) || isLetter(str.charAt(index))) { //��Ϊ ��output����Ϊ�ؼ���
							while (index < length && str.charAt(index) != '"') {
								index ++;
							}
							if (str.charAt(index) == '"') {
								endIndex = index;
								word = new Words();
								wordCount ++;
								word.id = wordCount;
								word.line = line;
								word.value = str.substring(beginIndex, index + 1);
								word.type = Words.KEY;
								break;
							} else {
								endIndex = index;
								word = new Words();
								wordCount ++;
								word.id = wordCount;
								word.line = line;
								word.value = str.substring(beginIndex, index);
								word.type = Words.UNDEFI;
								word.flag = false;
								errorCount ++;
								myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
								myErrorList.add(myError);
								lexErrorFlag = true;
								break;
							}
						} else {
							endIndex = index;
							word = new Words();
							wordCount ++;
							word.id = wordCount;
							word.line = line;
							word.value = str.substring(beginIndex, index);
							word.type = Words.UNDEFI;
							word.flag = false;
							errorCount ++;
							myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
							myErrorList.add(myError);
							lexErrorFlag = true;
							break;
						}
						
					}
				} else if(temp == '=') { //�ж�=��
					beginIndex = index;
					index ++;
					if(index < length && str.charAt(index) == '=') { // ==
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
					} else { //=
						//endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index --;
					}
					
				} else if(temp == '!') {
					beginIndex = index;
					index ++;
					if(index < length && str.charAt(index) == '=') { //��=
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
						index ++;
					} else { //��
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index --;
					}
				} else if(temp == '&') {
					beginIndex = index;
					index ++;
					if(index < length && str.charAt(index) == '&') { //&&
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
					} else { //&
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index --;
					}
				} else if(temp == '|') {
					beginIndex = index;
					index ++;
					if(index < length && str.charAt(index) == '|') { //||
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
					} else {  // |
						//endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index -- ;
					}
				} else if(temp == '+') {
					beginIndex = index;
					index ++;
					if(index < length && str.charAt(index) == '+') { //++
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
					} else {  //+
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index --;
					}
				} else if(temp == '-') {
					beginIndex = index;
					index ++;
					if(index < length && str.charAt(index) == '-') { //--
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
					} else { //-
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index --;
					}
				} else if(temp == '/') { 
					index ++;
					if(index < length && str.charAt(index) == '/') { // //
						break;
					} else if(index < length && str.charAt(index) == '*') { // /*����ע�Ϳ�ʼ
						noteFlag = true; //����ע��
						continue; //����Զ���ע�ͱ��
					} else { //��/
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index --;
					}
					index --;
				} else if(temp == '<') {
					beginIndex = index;
					index ++;
					if(index < length && str.charAt(index) == '=') { //<=
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
					} else {  //<
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index --;
					}
					
				} else if (temp == '>') {
					beginIndex = index;
					index ++;
					if(index < length && str.charAt(index) == '=') { //>=
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
					} else {  //>
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(index - 1, index);
						word.type = Words.OPERATOR;
						index --;
					}
				} else {
					switch(temp) {
					case ' ':
					case '\t':
					case '\r':
					case '\n':
						word = null;
						break;
					case ';':
						if(typestack.size() > 0) {
							typestack.pop();
						}
					case '[':
					case ']':
					case '(':
					case ')':
					case '{':
					case '}':
					case ',':
					case '"':
					case '.':
					case '+':
					case '-':
					case '*':
					case '/':
					case '%':
					case '?':
					case '#':
						word = new Words();
						word.id = ++ wordCount;
						word.line = line;
						word.value = String.valueOf(temp);
						if(Words.isOperator(word.value)) {
							word.type = Words.OPERATOR;
						} else if(Words.isBoundarySign(word.value)) {
							word.type = Words.BOUNDARYSIGN;
						} else {
							word.type = Words.END;
						}
						break;
					default:
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.type = Words.UNDEFI;
						word.flag = false;
						errorCount ++;
						myError = new MyError(errorCount, "�Ƿ��ַ�", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
				} 
			} else {
				int i = str.indexOf("*/");
				if(i != -1) {
					noteFlag = false;
					index = i + 2;
					continue;
				} else 
					break;
			}
			if(word == null) {
				index ++;
				continue;
			}
			wordsList.add(word);
			index ++;
		}  
	} 
	
	//������ʵ�����
	public static ArrayList<String> getTypelist(){
		ArrayList<String> list = new ArrayList<String>();
		for(TypeWord x: typeList) {
			list.add(x.getValue());
		}
		//System.out.println(list.toString());
		return list;
	}
	
	public ArrayList<Words> lexAnalyse(String str){ //�����ַ���
		String buffer[];
		if(str == null) { //���������ַ���Ϊ�գ�����ӽ�����־
			if(!wordsList.get(wordsList.size() - 1).type.equals(Words.END)) {
				Words word = new Words(++wordCount, "#", Words.END, 1);
				wordsList.add(word);
			}
			return wordsList;
		}
		buffer = str.split("\n"); //���з�Ϊ�ָ�
		int line = 1;
		for (int i = 0; i < buffer.length; i++) 
		{
			analyse(buffer[i].trim(), line);
			line ++;
		}
		if(!wordsList.get(wordsList.size() - 1).type.equals(Words.END)) { //��wordsList���һ���ַ���Ϊ������־���������һ����ӽ������
			Words word = new Words(++wordCount, "#", Words.END, line);
			wordsList.add(word);
		}
		return wordsList;
	}
	
	//��ȡҪ�������ļ�
	public ArrayList<Words> lexAnalyse1(String filePath) throws IOException {
		FileInputStream fis = new FileInputStream(filePath);
		BufferedInputStream bis = new BufferedInputStream(fis);
		InputStreamReader isr = new InputStreamReader(bis);
		BufferedReader inbr = new BufferedReader(isr);
		String str = "";
		int line = 1;
		while((str = inbr.readLine()) != null) {
			analyse(str.trim(), line);
			line ++;
		}
		inbr.close();
		//��ӽ�����־
		if(!wordsList.get(wordsList.size() - 1).type.equals(Words.END)) {
			Words word = new Words(++wordCount, "#", Words.END, line);
			wordsList.add(word);
		}
		return wordsList;
	}
	
	//�������ļ�
	public String outputWordList() throws IOException{
		FileOutputStream fos = new FileOutputStream("wordList.txt");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		OutputStreamWriter osw1 = new OutputStreamWriter(bos);
		PrintWriter pw1 = new PrintWriter(osw1);
		pw1.println("�������\t���ʵ�ֵ\t��������\t���������� \t�����Ƿ�Ϸ�\t��������");
		Words word;
		for(int i = 0; i < wordsList.size(); i++) {
			word = wordsList.get(i);
			pw1.println(word.id + "\t\t" + word.value + "\t\t" + word.type + "\t\t\t" + word.line + "\t" + word.flag + "\t\t\t" + word.attribute);
		}
		if(lexErrorFlag) {
			MyError myError;
			pw1.println("������Ϣ���£�");
			pw1.println("�������\t������Ϣ\t���������� \t���󵥴�");
			for(int i = 0; i < myErrorList.size(); i++) {
				myError = myErrorList.get(i);
				pw1.println(myError.id + "\t\t" + myError.info + "\t\t" + myError.line + "\t\t" + myError.word);
			}
		} else {
			pw1.println("�ʷ��������");
		}
		pw1.close();
		return "wordsList.txt";
	}
	
	public ArrayList<Words> getWordList(){
		return wordsList;
	}
	
	//����Ҫ�����ķ��Ŵ�
	public ArrayList<String> get_Lex_Result(){
			Words word;
			String result_string = "";
			for(int i = 0; i < wordsList.size(); i++) {
				word = wordsList.get(i);
				if(word.type.equals("��־��")){
					lex_result_stack.add("IDN");
				}
//				else if(word.type.equals("�ؼ���")) {
//					lex_result_stack.add("KEY");
//				}
				else if(word.type.equals("���ͳ���") || word.type.equals("���㳣��")){
					lex_result_stack.add("NUM");
				}
				else if(word.type.equals("�ַ�����")){
					lex_result_stack.add("CHAR");
				}
				else if(word.type.equals("�ַ�������")){
					lex_result_stack.add("STR");
				}
				else {
					lex_result_stack.add(word.value);
				}
				result_string = result_string + lex_result_stack.get(i);
			}
			System.out.println("�ʷ�������õ�������Ϊ��\n" + result_string);
			return lex_result_stack;
		}
	
	/*public static void main(String[] args) throws IOException{
		LexAnalyse lex = new LexAnalyse();
		lex.lexAnalyse1("test.txt"); //Ҫ�������ļ�
		lex.outputWordList();
		lex.get_Lex_Result(); //���������Ŵ�
		System.out.println(lex.getTypelist());
		System.out.println("����"+lex.getTypelist());
	}*/
} 
