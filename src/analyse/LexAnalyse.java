package analyse;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import entity.Words;
import entity.TypeWord;
import entity.MyError;

/*
 * 词法分析
*/
public class LexAnalyse {
	public ArrayList<Words> wordsList = new ArrayList<Words>(); //单词表
	public static ArrayList<TypeWord> typeList = new ArrayList<TypeWord>(); //符号类型
	public ArrayList<MyError> myErrorList = new ArrayList<MyError>();// 错误信息列表
	
	public boolean lexErrorFlag = false; //判断词法分析是否出错的标志
	public boolean noteFlag = false; //多行注释的标志
	public int wordCount = 0; //统计单词的个数
	public int errorCount = 0;// 统计错误个数
	public Stack<String> typestack = new Stack<String>();// 类型栈
	
	private ArrayList<String> lex_result_stack;//记录分析的符号串
	
	//构造方法
	public LexAnalyse() {
		lex_result_stack = new ArrayList<String>();
	}
	public LexAnalyse(String str) {
		lexAnalyse(str);
	}	
	
	//判断单词是否为单个数字
	private static boolean isDigit(char ch) {
		boolean isDigit = false;
		if('0' <= ch && ch <= '9') {
			isDigit = true;
		}
		return isDigit;
	}
	
	//判断单词是否为整数
	private static boolean isInteger(String word) {
		boolean isInteger = false;
		int i = 0;
		for(i = 0; i < word.length(); i++) {
			if(Character.isDigit(word.charAt(i))) { //判断字符是否为数字
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
	
	//判断单词是否为浮点数
	private static boolean isFloat(String word) {
		boolean isFloat = false;
		int i, j = 0 ;
		for (i = 0; i <= word.length(); i++) {
			if(Character.isDigit(word.charAt(i))) {
				continue;
			} else if (word.charAt(i) == '.') {
				if (i < word.length() - 1) { //若单词字符串倒数第二位之前有.小数点，则继续判断
					j ++;
				}
				continue;
			} else {
				break;
			}
		}
		if(i == word.length() && j == 1) { //小数点只有一位
			isFloat = true;
		}
		return isFloat;
	}
		
	//判断单词是否为字符串类型
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
	
	//判断单词是否为字母
	private static boolean isLetter(char ch) {
		boolean isLetter = false;
		if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z')) {
			isLetter = true;
		}
		return isLetter;
	}
	
	//判断单词是否为合法标识符
	private static boolean isID(String word) {
		boolean isID = false;
		int i = 0;
		if(Words.isKey(word)) {
			isID = true;
		}
		char temp = word.charAt(i);
		if(isLetter(temp) || temp == '_') { //以下划线和字母开头
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
	
	//判断词法分析是否出错 
	public boolean isFail() {
		return lexErrorFlag;
	}
	
	//词法分析过程
	public void analyse(String str, int line) {
		int beginIndex; //开始标记
		int endIndex; //结束标记
		int index = 0;
		int length = str.length();//字符串长度
		TypeWord typeword = null ;
		Words word = null;
		MyError myError;
		
		char temp;
		while(index < length) {
			temp = str.charAt(index);
			if(!noteFlag) {
				if(isLetter(temp) || temp == '_') { //判断是否为标志符
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
					if(Words.isKey(word.value)) { //若单词为关键字，则将其类型记为KEY
						word.type = Words.KEY;
						if(word.value.equals("int") || word.value.equals("float") || word.value.equals("char") || word.value.equals("string")) {
							typestack.push(word.value); //将int、float、char、string类型放入类型栈中
						}
					} else if (isID(word.value)) {
						word.type = Words.IDENTIFIER; //标识符
						if(typestack.size() > 0) {
							word.attribute = typestack.lastElement(); //获取栈顶元素
//							if(typestack.size() > 0) {
//								word.attribute = typestack.lastElement();
//							}
							//判断标识符的属性，从栈顶取出该标识符对应的属性
							if(word.attribute.equals("int") || word.attribute.equals("float") || word.attribute.equals("char") || word.attribute.equals("string")) {
								typeword = new TypeWord();
								typeword.value = word.value;
								typeword.type = word.attribute;
								typeList.add(typeword);
							}
						}
					}
					else {
						word.type = Words.UNDEFI; //未定义类型
						word.flag = false;
						errorCount ++;
						myError = new MyError(errorCount, "非法字符", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
					index --;
				} else if(isDigit(temp)) { //判断是否为int、float型
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
							word.type = Words.INT_CONST; //整型
						} else {
							word.type = Words.FLOAT_CONST; //浮点型
						}
					} else {
						word.type = Words.UNDEFI; //未定义
						word.flag = false;
						errorCount ++;
						myError = new MyError(errorCount, "非法标识符", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
					index --;
				} else if(str.charAt(index) == '\"' && wordsList.get(wordsList.size()-1).value.equals("=")) { //判断字符串型常量
					beginIndex = index;
					if(index < length && (endIndex=isString(str.substring(beginIndex, str.length() - 1)))!=-1) {
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, beginIndex+endIndex+1);
						word.type = Words.STRING_CONST; //字符串型
						/*index = str.length() - 2;
						if(index < length && str.charAt(index) == '\"') {
							word = new Words();
							wordCount ++;
							word.id = wordCount;
							word.line = line;
							word.value = str.substring(beginIndex, index + 1);
							word.type = Words.STRING_CONST; //字符串型
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
							myError = new MyError(errorCount, "非法字符", word.line, word);
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
						myError = new MyError(errorCount, "非法字符", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
					index = beginIndex+endIndex;
				} else if(str.charAt(index) == '\'') { //char字符型
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
							word.type = Words.CHAR_CONST; //字符型
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
							myError = new MyError(errorCount, "非法字符", word.line, word);
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
						myError = new MyError(errorCount, "非法字符", word.line, word);
						myErrorList.add(myError);
						lexErrorFlag = true;
					}
					//index --;
				} else if (str.charAt(index) == '\"' && !wordsList.get(wordsList.size()-1).value.equals("=")) { //scanf、printf格式判断
					beginIndex = index;
					index ++;
					//System.out.println(index);
					temp = str.charAt(index);
					while (index < length) {
						if(str.charAt(index) == '%') { //"%d","%c","%f"为关键字
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
								myError = new MyError(errorCount, "非法字符", word.line, word);
								myErrorList.add(myError);
								lexErrorFlag = true;
								break;
							}
						} else if (isDigit(str.charAt(index)) || isLetter(str.charAt(index))) { //若为 “output”记为关键字
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
								myError = new MyError(errorCount, "非法字符", word.line, word);
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
							myError = new MyError(errorCount, "非法字符", word.line, word);
							myErrorList.add(myError);
							lexErrorFlag = true;
							break;
						}
						
					}
				} else if(temp == '=') { //判断=号
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
					if(index < length && str.charAt(index) == '=') { //！=
						endIndex = index + 1;
						word = new Words();
						wordCount ++;
						word.id = wordCount;
						word.line = line;
						word.value = str.substring(beginIndex, endIndex);
						word.type = Words.OPERATOR;
						index ++;
					} else { //！
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
					} else if(index < length && str.charAt(index) == '*') { // /*多行注释开始
						noteFlag = true; //多行注释
						continue; //则忽略多行注释标记
					} else { //除/
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
						myError = new MyError(errorCount, "非法字符", word.line, word);
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
	
	//输出单词的类型
	public static ArrayList<String> getTypelist(){
		ArrayList<String> list = new ArrayList<String>();
		for(TypeWord x: typeList) {
			list.add(x.getValue());
		}
		//System.out.println(list.toString());
		return list;
	}
	
	public ArrayList<Words> lexAnalyse(String str){ //分析字符串
		String buffer[];
		if(str == null) { //若分析的字符串为空，则添加结束标志
			if(!wordsList.get(wordsList.size() - 1).type.equals(Words.END)) {
				Words word = new Words(++wordCount, "#", Words.END, 1);
				wordsList.add(word);
			}
			return wordsList;
		}
		buffer = str.split("\n"); //换行符为分割
		int line = 1;
		for (int i = 0; i < buffer.length; i++) 
		{
			analyse(buffer[i].trim(), line);
			line ++;
		}
		if(!wordsList.get(wordsList.size() - 1).type.equals(Words.END)) { //若wordsList最后一个字符不为结束标志，则在最后一行添加结束标记
			Words word = new Words(++wordCount, "#", Words.END, line);
			wordsList.add(word);
		}
		return wordsList;
	}
	
	//读取要分析的文件
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
		//添加结束标志
		if(!wordsList.get(wordsList.size() - 1).type.equals(Words.END)) {
			Words word = new Words(++wordCount, "#", Words.END, line);
			wordsList.add(word);
		}
		return wordsList;
	}
	
	//输出结果文件
	public String outputWordList() throws IOException{
		FileOutputStream fos = new FileOutputStream("wordList.txt");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		OutputStreamWriter osw1 = new OutputStreamWriter(bos);
		PrintWriter pw1 = new PrintWriter(osw1);
		pw1.println("单词序号\t单词的值\t单词类型\t单词所在行 \t单词是否合法\t单词属性");
		Words word;
		for(int i = 0; i < wordsList.size(); i++) {
			word = wordsList.get(i);
			pw1.println(word.id + "\t\t" + word.value + "\t\t" + word.type + "\t\t\t" + word.line + "\t" + word.flag + "\t\t\t" + word.attribute);
		}
		if(lexErrorFlag) {
			MyError myError;
			pw1.println("错误信息如下：");
			pw1.println("错误序号\t错误信息\t错误所在行 \t错误单词");
			for(int i = 0; i < myErrorList.size(); i++) {
				myError = myErrorList.get(i);
				pw1.println(myError.id + "\t\t" + myError.info + "\t\t" + myError.line + "\t\t" + myError.word);
			}
		} else {
			pw1.println("词法分析完成");
		}
		pw1.close();
		return "wordsList.txt";
	}
	
	public ArrayList<Words> getWordList(){
		return wordsList;
	}
	
	//返回要分析的符号串
	public ArrayList<String> get_Lex_Result(){
			Words word;
			String result_string = "";
			for(int i = 0; i < wordsList.size(); i++) {
				word = wordsList.get(i);
				if(word.type.equals("标志符")){
					lex_result_stack.add("IDN");
				}
//				else if(word.type.equals("关键字")) {
//					lex_result_stack.add("KEY");
//				}
				else if(word.type.equals("整型常量") || word.type.equals("浮点常量")){
					lex_result_stack.add("NUM");
				}
				else if(word.type.equals("字符常量")){
					lex_result_stack.add("CHAR");
				}
				else if(word.type.equals("字符串常量")){
					lex_result_stack.add("STR");
				}
				else {
					lex_result_stack.add(word.value);
				}
				result_string = result_string + lex_result_stack.get(i);
			}
			System.out.println("词法分析后得到分析串为：\n" + result_string);
			return lex_result_stack;
		}
	
	/*public static void main(String[] args) throws IOException{
		LexAnalyse lex = new LexAnalyse();
		lex.lexAnalyse1("test.txt"); //要分析的文件
		lex.outputWordList();
		lex.get_Lex_Result(); //待分析符号串
		System.out.println(lex.getTypelist());
		System.out.println("类型"+lex.getTypelist());
	}*/
} 
