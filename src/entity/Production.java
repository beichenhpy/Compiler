package entity;

import java.util.ArrayList;


//����ʽ��
public class Production{
	String left;
	String[] right; //������ʽ�ұ�ÿ�����ʷ�������

	// ��ʼ��select��
	public ArrayList<String> select = new ArrayList<String>();
	public Production(String left, String[] right){
		this.left = left;
		this.right = right;
	}
	
	public String[] returnRights(){
		return right;
	}
	
	public String returnLeft(){
		return left;
	}
	
	
	
}
