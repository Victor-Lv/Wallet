/**MyPin.java
 * @作者         吕浪
 * @创建于 2016-8-8 上午10:37:51
 */
package my;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class MyPin
{
	private byte[] pinValue;
	private byte tryLimit;
	private byte pinSize;
	private byte tryCounter; //所剩尝试机会,默认为3
	private boolean[] checkPin; //false时表示尚未验证pin
	private boolean[] blockPin;//可直接用tryCounter==0代替,这里为了明了添加变量
	
	MyPin()
	{
		this.pinValue = new byte[8]; //8字节的PINValue,自动初始化为00...
		//确保初始化为00...
		Util.arrayFillNonAtomic(this.pinValue, (short)0, (short)8, (byte)0);//填充字节数组函数
		
		this.tryLimit = (byte)3;
		this.pinSize = (byte)8;
		this.tryCounter = this.tryLimit;
		//用如下函数开辟空间时会自动初始化为false
		this.checkPin = JCSystem.makeTransientBooleanArray((short)1, JCSystem.CLEAR_ON_DESELECT);//deselect或reset自动复位
		this.blockPin = JCSystem.makeTransientBooleanArray((short) 1, JCSystem.CLEAR_ON_DESELECT);//提供解锁方式：发送reset命令自动解锁
	}
	
	public boolean pinVerify()
	{
		return this.checkPin[0];
	}

	public byte check(byte[] pin,
					  short offset,
					  byte length)
	 				  throws ArrayIndexOutOfBoundsException,
	 						 NullPointerException
	{
		if(length != this.pinSize) //传入长度不等于当前长度
		{
			this.tryCounter--;
			if(this.tryCounter == (byte)0)
				this.blockPin[0] = true;
			return this.tryCounter;
		}
		byte result = Util.arrayCompare(pin, offset, this.pinValue, (short)0, (short)8);
		if(result == (byte)0) //相等
		{
			this.tryCounter = this.tryLimit;
			this.checkPin[0] = true;
			return this.tryLimit;
		}
		else
		{
			this.tryCounter--;
			if(this.tryCounter == (byte)0)
				this.blockPin[0] = true;
			return this.tryCounter;
		}
	}
	
	public boolean setPin(byte[] pin,
						  short offset,
						  byte length)
		 				  throws ArrayIndexOutOfBoundsException,
		 						 NullPointerException
	{
		/*block 和 verify这些验证都交给前端了*/
		if(length != this.pinSize)
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		Util.arrayCopyNonAtomic(pin, offset, this.pinValue, (short)0, length);
		return true;
	}
	
	public boolean pinBlock()
	{
		return blockPin[0];
	}
	
}
