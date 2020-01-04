package analyse;

import entity.FourElement;
import entity.Words;
import java.io.*;
import java.util.ArrayList;

/**
 * ������������
 */
public class Asm {

	private ArrayList<String> asmCodeList = new ArrayList<String>(); //��Ż�����
	private ArrayList<FourElement> fourElemList; //�����Ԫʽ
	private ArrayList<String> id; //��ű�־��

	
	public Asm(ArrayList<FourElement> fourElemList, ArrayList<String> id, ArrayList<String> fourElemT) {
		
		this.fourElemList = fourElemList;
		this.id = id;
		asmHead(id, fourElemT); // ���ͷ��
		asmCode(fourElemList); // ���ɴ���δ���
		asmTail();// ���β��

		for (int i = 0; i < asmCodeList.size(); i++) //���������룬���
			System.out.println(asmCodeList.get(i));

	}

	// ��ȡ�����asm�ļ�
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

	//ͷ��
	public void asmHead(ArrayList<String> id, ArrayList<String> fourElemT) {
		// ������ݶδ���
		asmCodeList.add("datasg segment"); //���ݶ���ο�ʼ��־
		asmCodeList.add("tem db 6,7 dup  (0)"); //����6,7��temp�ֽ�������,��ʼ��Ϊ0
		for (int i = 0; i < id.size(); i++) { //������ʶ�����飬��ʼ����Ϊ�������ݣ�4�ֽ�
			asmCodeList.add(id.get(i) + " dw 0");
		}

		for (int j = 0; j < fourElemT.size(); j++) { //�������T�����飬��ʼ��Ϊ��������
			asmCodeList.add(fourElemT.get(j) + " dw 0");
		}
		//�������������������scanf_sum3��printf_sum20
		for (int i = 0; i < fourElemList.size(); i++) {
			if (fourElemList.get(i).op.equals("PRINTF")) {

				asmCodeList.add(
						"printf_" + fourElemList.get(i).arg1 + (i + 1) + " db '" + fourElemList.get(i).arg1 + ":$'"); //:$��ʾ���

			} else if (fourElemList.get(i).op.equals("SCANF")) {
				asmCodeList.add("scanf_" + fourElemList.get(i).arg1 + (i + 1) + " db 'input " + fourElemList.get(i).arg1
						+ ":$'");//input:$��ʾ���
			}
		}
		asmCodeList.add("datasg ends");//���ݶ���ν�����־
		asmCodeList.add("assume cs:codesg,ds:datasg");
		asmCodeList.add("codesg segment"); //����ο�ʼ��־
		//asmCodeList.add("start:");
		asmCodeList.add("begin:MOV AX,datasg"); //�����ݶηŵ��Ĵ���A��
		asmCodeList.add("MOV DS,AX"); //DS�Ĵ���:���Ҫ�������ݵĶε�ַ

	}

	//�������
	public void asmCode(ArrayList<FourElement> fourElemList) {
		int position = fourElemList.size()+1;// ��ȡ��Ԫʽ�ĳ���λ��
		System.out.println("λ��" + position);
		System.out.println("���Ŀ��������£�");

		for (int i = 0; i < fourElemList.size(); i++) {

			// asmCodeList.add((fourElemList.size()+1)+""+"----"+fourElemList.get(i).arg1);
			// asmCodeList.add("13".equals((fourElemList.size()+1+"").toString()));

			int flag = 0;// �Ƿ���ת���������ĳ���λ�ã��������㡣
			if (fourElemList.get(i).arg1.equals(position - 1 + "")) {

				if (fourElemList.get(i).op.equals("FJ") || fourElemList.get(i).op.equals("RJ")) { //����Ԫʽ����ת�����һ�У����������
					fourElemList.get(i).arg1 = "L" + (position - 1); //������־�ŵ���Ԫʽ��arg1��
					flag = 1; //������־
				}
			}

			if (fourElemList.get(i).op.equals("=")) { //��Ԫʽ�Ĳ���Ϊ=ʱ��  (=,10, /, a)

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

				if (flag == 1) {// ���=1����TheEnd,������㡣
					// jnc:���ڻ��������ת
					asmCodeList.add("L" + (i + 1) + ": jnc " + fourElemList.get(i).arg1 + ";���ڵ�������ת");//Li: jnc 19, ����
				} else {
					asmCodeList.add("L" + (i + 1) + ": jnc L" + fourElemList.get(i).arg1 + ";���ڵ�������ת");// Li: jnc L19

				}

			} else if (fourElemList.get(i).op.equals("RJ")) { //����ת

				if (flag == 1) {// ���=1����TheEnd,������㡣
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
				asmCodeList.add(";PRINTF"); //;��־ע��
				asmCodeList.add("L" + (i + 1) + ":");
				asmCodeList.add("lea dx,printf_"+fourElemList.get(i).arg1+(i+1)); //�������־�����ĵ�ַ�͵�dx
				asmCodeList.add("mov ah,9");
				asmCodeList.add("int 21h"); //9���ж�ǰ�ᣬ������DX��ֵ��ָ������ַ������ڵ���Ч��ַ
				
				asmCodeList.add("mov ax,"+fourElemList.get(i).arg1); //��sum����Ĵ���AX
				asmCodeList.add("xor cx,cx");//��0
				asmCodeList.add("mov bx,10");
				asmCodeList.add("PT0"+(i+1)+":xor dx,dx");
				asmCodeList.add("div bx");
				asmCodeList.add("or dx,0e30h;0e:��ʾ�ַ�");
				asmCodeList.add("push dx");
				asmCodeList.add("inc cx");
				asmCodeList.add("cmp ax,0;ZF=1��AX=0,ZF=0��AX��=0");
				asmCodeList.add("jnz PT0"+(i+1)+";���ʱ��ת");
				asmCodeList.add("PT1"+(i+1)+":pop ax");
				asmCodeList.add("int 10h;��ʾһ���ַ�");
				asmCodeList.add("loop PT1"+(i+1));
				asmCodeList.add("mov ah,0 ");
				asmCodeList.add(";int 16h ;�����ж�"); 

				asmCodeList.add(";����"); 
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
				 
				
				asmCodeList.add("lea dx,scanf_"+fourElemList.get(i).arg1+(i+1));//�������־�����ĵ�ַ�͵�dx
				asmCodeList.add("mov ah,9");
				asmCodeList.add("int 21h");
				
				asmCodeList.add(";�����ж�");
				asmCodeList.add("mov al,0h;");
				asmCodeList.add("mov tem[1],al;");
				asmCodeList.add("lea dx,tem;");
				asmCodeList.add(" mov ah,0ah");
				asmCodeList.add("int 21h");
				
				asmCodeList.add(";������������ݣ�����ֵ������");
				asmCodeList.add("mov cl,0000h;");
				asmCodeList.add("mov al,tem[1];");
				asmCodeList.add("sub al,1;");
				asmCodeList.add("mov cl,al;");
				
				asmCodeList.add("mov ax,0000h;");
				asmCodeList.add("mov bx,0000h;");
				
				asmCodeList.add("mov al,tem[2];");
				asmCodeList.add("sub al,30h;");
				asmCodeList.add("mov "+fourElemList.get(i).arg1+",ax;");//��ʼ��sum = 0030h = 0
				
				
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
		 
				asmCodeList.add(";�س�����"); 
				asmCodeList.add("mov dl,0dh"); 
				asmCodeList.add("mov ah,2"); 
				asmCodeList.add("int 21h"); 
				asmCodeList.add("mov dl,0ah"); 
				asmCodeList.add("mov ah,2"); 
				asmCodeList.add("int 21h"); 
				asmCodeList.add("\n");
				
				 
			}
			if (i == fourElemList.size() - 1) {
				asmCodeList.add("L" + position +": "+"nop");// ��������ĳ���λ��
			}
		}
	}

	//β��
	public void asmTail() {
		asmCodeList.add("mov ax,4c00h;(int 21h��4ch���жϣ���ȫ�˳�����)");
		asmCodeList.add("int 21h;(����ϵͳ�ж�)");
		asmCodeList.add("codesg ends");
		asmCodeList.add("end begin");
	}

	public static void main(String[] args) throws IOException {
		AnalyseList al=new AnalyseList();
		al.outputFirst();
		al.outputFollow();
		LexAnalyse lexAnalyse = new LexAnalyse();
		//��ȡ��ʶ��
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
