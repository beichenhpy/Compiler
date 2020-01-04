package analyse;

import entity.FourElement;
import entity.Words;
import java.io.*;
import java.util.ArrayList;

/**
 * 汇编代码生成类
 */
public class Asm {

	private ArrayList<String> asmCodeList = new ArrayList<String>(); //存放汇编代码
	private ArrayList<FourElement> fourElemList; //存放四元式
	private ArrayList<String> id; //存放标志符

	
	public Asm(ArrayList<FourElement> fourElemList, ArrayList<String> id, ArrayList<String> fourElemT) {
		
		this.fourElemList = fourElemList;
		this.id = id;
		asmHead(id, fourElemT); // 汇编头部
		asmCode(fourElemList); // 生成代码段代码
		asmTail();// 汇编尾部

		for (int i = 0; i < asmCodeList.size(); i++) //遍历汇编代码，输出
			System.out.println(asmCodeList.get(i));

	}

	// 获取并输出asm文件
	public String getAsmFile() throws IOException {

		FileOutputStream fos = new FileOutputStream("c_to_asm.txt");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		OutputStreamWriter osw1 = new OutputStreamWriter(bos, "utf-8");
		PrintWriter pw1 = new PrintWriter(osw1);
		for (int i = 0; i < asmCodeList.size(); i++)
			pw1.println(asmCodeList.get(i));

		pw1.close();
		return "c_to_asm.txt";
	}

	//头部
	public void asmHead(ArrayList<String> id, ArrayList<String> fourElemT) {
		// 添加数据段代码
		asmCodeList.add("datasg segment"); //数据定义段开始标志
		asmCodeList.add("tem db 6,7 dup  (0)"); //定义6,7个temp字节型数据,初始化为0
		for (int i = 0; i < id.size(); i++) { //遍历标识符数组，初始化其为字型数据，4字节
			asmCodeList.add(id.get(i) + " dw 0");
		}

		for (int j = 0; j < fourElemT.size(); j++) { //遍历存放T的数组，初始化为字型数据
			asmCodeList.add(fourElemT.get(j) + " dw 0");
		}
		//声明定义输入输出变量scanf_sum3、printf_sum20
		for (int i = 0; i < fourElemList.size(); i++) {
			if (fourElemList.get(i).op.equals("PRINTF")) {

				asmCodeList.add(
						"printf_" + fourElemList.get(i).arg1 + (i + 1) + " db '" + fourElemList.get(i).arg1 + ":$'"); //:$提示输出

			} else if (fourElemList.get(i).op.equals("SCANF")) {
				asmCodeList.add("scanf_" + fourElemList.get(i).arg1 + (i + 1) + " db 'input " + fourElemList.get(i).arg1
						+ ":$'");//input:$提示输出
			}
		}
		asmCodeList.add("datasg ends");//数据定义段结束标志
		asmCodeList.add("assume cs:codesg,ds:datasg");
		asmCodeList.add("codesg segment"); //代码段开始标志
		//asmCodeList.add("start:");
		asmCodeList.add("begin:MOV AX,datasg"); //将数据段放到寄存器A中
		asmCodeList.add("MOV DS,AX"); //DS寄存器:存放要访问数据的段地址

	}

	//汇编代码段
	public void asmCode(ArrayList<FourElement> fourElemList) {
		int position = fourElemList.size()+1;// 获取四元式的出口位置
		System.out.println("位置" + position);
		System.out.println("汇编目标代码如下：");

		for (int i = 0; i < fourElemList.size(); i++) {

			// asmCodeList.add((fourElemList.size()+1)+""+"----"+fourElemList.get(i).arg1);
			// asmCodeList.add("13".equals((fourElemList.size()+1+"").toString()));

			int flag = 0;// 是否跳转到汇编运算的出口位置，结束运算。
			if (fourElemList.get(i).arg1.equals(position - 1 + "")) {

				if (fourElemList.get(i).op.equals("FJ") || fourElemList.get(i).op.equals("RJ")) { //若四元式的跳转到最后一行，则结束运算
					fourElemList.get(i).arg1 = "L" + (position - 1); //结束标志放到四元式的arg1中
					flag = 1; //结束标志
				}
			}

			if (fourElemList.get(i).op.equals("=")) { //四元式的操作为=时，  (=,10, /, a)

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1); //L1: mov AX, 10
				asmCodeList.add("mov " + fourElemList.get(i).result + ", AX"); // mov a, AX
			} else if (fourElemList.get(i).op.equals("+")) { //(+, sum, a, T4)

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1); //L1: mov AX, sum
				asmCodeList.add("add AX, " + fourElemList.get(i).arg2); // add AX, a
				asmCodeList.add("mov " + fourElemList.get(i).result + ", AX"); //mov T4, AX
			} else if (fourElemList.get(i).op.equals("++")) { //(++, a, /, a)

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1); //L1: mov AX, a
				asmCodeList.add("add AX, 1"); 
				asmCodeList.add("mov " + fourElemList.get(i).result + ", AX"); //mov a, AX
			} else if (fourElemList.get(i).op.equals("--")) { //(++, a, /, a)

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1); //L1: mov AX, a
				asmCodeList.add("sub AX, 1"); 
				asmCodeList.add("mov " + fourElemList.get(i).result + ", AX"); //mov a, AX
			} else if (fourElemList.get(i).op.equals("-")) {

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1);
				asmCodeList.add("sub AX, " + fourElemList.get(i).arg2);
				asmCodeList.add("mov " + fourElemList.get(i).result + ", AX");
			} else if (fourElemList.get(i).op.equals("*")) {

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1);
				asmCodeList.add("mov BX," + fourElemList.get(i).arg2);
				asmCodeList.add("mul BX");
				asmCodeList.add("mov " + fourElemList.get(i).result + ", AX");
			} else if (fourElemList.get(i).op.equals("/")) {

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1);
				asmCodeList.add("mov BX," + fourElemList.get(i).arg2);
				asmCodeList.add("div BL");
				asmCodeList.add("mov ah,0h");
				asmCodeList.add("mov " + fourElemList.get(i).result + ", Ax");
			}

			else if (fourElemList.get(i).op.equals("FJ")) { //(FJ, 19, T, /)

				if (flag == 1) {// 如果=1跳到TheEnd,完成运算。
					// jnc:大于或等于则跳转
					asmCodeList.add("L" + (i + 1) + ": jnc " + fourElemList.get(i).arg1 + ";大于等于则跳转");//Li: jnc 19, 结束
				} else {
					asmCodeList.add("L" + (i + 1) + ": jnc L" + fourElemList.get(i).arg1 + ";大于等于则跳转");// Li: jnc L19

				}

			} else if (fourElemList.get(i).op.equals("RJ")) { //真跳转

				if (flag == 1) {// 如果=1跳到TheEnd,完成运算。
					asmCodeList.add("L" + (i + 1) + ": jmp " + fourElemList.get(i).arg1);
				} else {
					asmCodeList.add("L" + (i + 1) + ": jmp L" + fourElemList.get(i).arg1);

				}

			} else if (fourElemList.get(i).op.equals("<")) { //(<, a, b, T)
				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1); //L: mov AX, a
				asmCodeList.add("sub AX, " + fourElemList.get(i).arg2); // sub AX, b; AX = a-b

			} else if (fourElemList.get(i).op.equals(">")) {

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg2);
				asmCodeList.add("sub AX, " + fourElemList.get(i).arg1);

			} else if (fourElemList.get(i).op.equals(">=")) {

				// asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg2);
				// asmCodeList.add("sub AX, " + fourElemList.get(i).arg1);

				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg1);
				asmCodeList.add("add AX, 1");
				asmCodeList.add("mov BX ,AX");
				asmCodeList.add(" mov AX, " + fourElemList.get(i).arg2);
				asmCodeList.add("sub AX, BX");

			} else if (fourElemList.get(i).op.equals("<=")) { //(<=, a, b, T)
				asmCodeList.add("L" + (i + 1) + ": mov AX, " + fourElemList.get(i).arg2); //Li: mov AX, b
				asmCodeList.add("add AX, 1"); // AX = b+1
				asmCodeList.add("mov BX ,AX"); // BX = AX
				asmCodeList.add(" mov AX, " + fourElemList.get(i).arg1); //mov AX, a
				asmCodeList.add("sub AX, BX"); //AX = a-(b+1)

			}
			else if (fourElemList.get(i).op.equals("PRINTF")) {
				asmCodeList.add("\n");
				asmCodeList.add(";PRINTF"); //;标志注释
				asmCodeList.add("L" + (i + 1) + ":");
				asmCodeList.add("lea dx,printf_"+fourElemList.get(i).arg1+(i+1)); //将输出标志变量的地址送到dx
				asmCodeList.add("mov ah,9");
				asmCodeList.add("int 21h"); //9号中断前提，给参数DX赋值，指定输出字符串所在的有效地址
				
				asmCodeList.add("mov ax,"+fourElemList.get(i).arg1); //将sum放入寄存器AX
				asmCodeList.add("xor cx,cx");//置0
				asmCodeList.add("mov bx,10");
				asmCodeList.add("PT0"+(i+1)+":xor dx,dx");
				asmCodeList.add("div bx");
				asmCodeList.add("or dx,0e30h;0e:显示字符");
				asmCodeList.add("push dx");
				asmCodeList.add("inc cx");
				asmCodeList.add("cmp ax,0;ZF=1则AX=0,ZF=0则AX！=0");
				asmCodeList.add("jnz PT0"+(i+1)+";相等时跳转");
				asmCodeList.add("PT1"+(i+1)+":pop ax");
				asmCodeList.add("int 10h;显示一个字符");
				asmCodeList.add("loop PT1"+(i+1));
				asmCodeList.add("mov ah,0 ");
				asmCodeList.add(";int 16h ;键盘中断"); 

				asmCodeList.add(";换行"); 
				asmCodeList.add("mov dl,0dh"); 
				asmCodeList.add("mov ah,2"); 
				asmCodeList.add("int 21h"); 
				asmCodeList.add("mov dl,0ah"); 
				asmCodeList.add("mov ah,2"); 
				asmCodeList.add("int 21h"); 
				asmCodeList.add("\n");
				 
			}else if (fourElemList.get(i).op.equals("SCANF")) {
				asmCodeList.add("L" + (i + 1) + ":");
				
				asmCodeList.add("\n");
				asmCodeList.add(";SCANF");
				 
				
				asmCodeList.add("lea dx,scanf_"+fourElemList.get(i).arg1+(i+1));//将输入标志变量的地址送到dx
				asmCodeList.add("mov ah,9");
				asmCodeList.add("int 21h");
				
				asmCodeList.add(";输入中断");
				asmCodeList.add("mov al,0h;");
				asmCodeList.add("mov tem[1],al;");
				asmCodeList.add("lea dx,tem;");
				asmCodeList.add(" mov ah,0ah");
				asmCodeList.add("int 21h");
				
				asmCodeList.add(";处理输入的数据，并赋值给变量");
				asmCodeList.add("mov cl,0000h;");
				asmCodeList.add("mov al,tem[1];");
				asmCodeList.add("sub al,1;");
				asmCodeList.add("mov cl,al;");
				
				asmCodeList.add("mov ax,0000h;");
				asmCodeList.add("mov bx,0000h;");
				
				asmCodeList.add("mov al,tem[2];");
				asmCodeList.add("sub al,30h;");
				asmCodeList.add("mov "+fourElemList.get(i).arg1+",ax;");//初始化sum = 0030h = 0
				
				
				asmCodeList.add("mov ax,cx"); //ax = cx = 0001h
				asmCodeList.add("sub ax,1"); //ax = 0002h
				asmCodeList.add("jc inputEnd"+(i+1));
				
				asmCodeList.add(";");
				asmCodeList.add("MOV SI,0003H;");
				
				
				asmCodeList.add("ln"+(i+1)+":mov bx,10;");
				asmCodeList.add("mov ax,"+fourElemList.get(i).arg1+";"); //sum
				
				asmCodeList.add("mul bx;");
				asmCodeList.add("mov "+fourElemList.get(i).arg1+",ax;"); //sum
				asmCodeList.add("mov ax,0000h;");
				asmCodeList.add("mov al,tem[si]");
				asmCodeList.add("sub al,30h;");
				asmCodeList.add("add ax,"+fourElemList.get(i).arg1+";");//sum
				asmCodeList.add("mov "+fourElemList.get(i).arg1+",ax");//sum
				asmCodeList.add("INC SI");
				asmCodeList.add("loop ln"+(i+1));
				asmCodeList.add("inputEnd"+(i+1)+": nop");
				asmCodeList.add("");
				asmCodeList.add("");
		 
				asmCodeList.add(";回车换行"); 
				asmCodeList.add("mov dl,0dh"); 
				asmCodeList.add("mov ah,2"); 
				asmCodeList.add("int 21h"); 
				asmCodeList.add("mov dl,0ah"); 
				asmCodeList.add("mov ah,2"); 
				asmCodeList.add("int 21h"); 
				asmCodeList.add("\n");
				
				 
			}
			if (i == fourElemList.size() - 1) {
				asmCodeList.add("L" + position +": "+"nop");// 设置运算的出口位置
			}
		}
	}

	//尾部
	public void asmTail() {
		asmCodeList.add("mov ax,4c00h;(int 21h的4ch号中断，安全退出程序)");
		asmCodeList.add("int 21h;(调用系统中断)");
		asmCodeList.add("codesg ends");
		asmCodeList.add("end begin");
	}

	public static void main(String[] args) throws IOException {
		AnalyseList al=new AnalyseList();
		al.outputFirst();
		al.outputFollow();
		LexAnalyse lexAnalyse = new LexAnalyse();
		//获取标识符
		lexAnalyse.lexAnalyse1("test.txt");
		lexAnalyse.outputWordList();
		ArrayList<Words> wordList;
		wordList = lexAnalyse.getWordList();
		ArrayList<String> id = new ArrayList<String>();
		for (int i = 0; i < wordList.size(); i++) {
			if(wordList.get(i).type.equals(Words.IDENTIFIER)){					
			if(!id.contains(wordList.get(i).value)){
			id.add(wordList.get(i).value);
			//System.out.println(wordList.get(i).value);
			}
			}
		}
		ArrayList<String> lex_result_stack = lexAnalyse.get_Lex_Result();
		TextParse parser = new TextParse(lex_result_stack, lexAnalyse);
		parser.Parsing();
		parser.outputFourElem();
		Asm asm = new Asm(parser.fourElemList, id, parser.fourElemT);
		asm.getAsmFile();
	}
}
